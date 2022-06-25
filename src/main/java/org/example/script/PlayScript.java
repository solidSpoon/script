package org.example.script;

import org.example.script.enums.ParamFlag;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 命令行工具。可以执行一个脚本，或者以REPL模式运行。
 */
public class PlayScript {

    private static String getScript() {
        Map<Integer, String> result = new HashMap<>();

        String a = """
                45 + 10 * 2;
                """;
        String b = """
                int age = 44;
                {
                    int i = 10;
                    age + i;
                }
                """;
        String c = """
                int age = 44;
                for(int i = 0; i < 10; i++) {
                    age = age + 2;
                }
                int i = 8;
                """;
        String d = """
                int b= 10;
                int myFunc(int a) {
                    return a + b + 3;
                }
                myFunc(2);
                """;
        String e = """
                class MyClass{
                    int a = 2;
                    int b;
                    MyClass(){
                        b = 3;
                    }
                }
                MyClass c = MyClass();
                c.b;
                """;
        String f = """
                class class1{
                    int a=2;
                    int b;
                    void method1() {
                        println("in class1");
                    }
                }
                                    
                class class2 extends class1{
                    int b = 5;
                    void method1() {
                        println("in class2");
                    }
                }
                                    
                class1 c = class2();
                println(c.a);
                println(c.b);
                c.method1();
                """;
        String g = """
                class MyClass{
                    int a;
                    int b;
                    MyClass() {
                        a = 1;
                        b = 2;
                    }
                    int calc() {
                        return a + b;
                    }
                }
                MyClass c = MyClass();
                c.calc();
                """;
        String h = """
                println(2);
                """;
        String i = """
                int fun1(int a) {
                    return a + 1;
                }
                                
                println(fun1(2));
                                
                function int(int)
                    fun2 = fun1;
                                
                fun2(3);
                """;
        String j = """
                int a=0;
                function int() fun1(){
                    int b = 0;
                    int inner() {
                        a = a + 1;
                        b = b + 1;
                        return b;
                    }
                    return inner;
                }
                                
                function int() fun2 = fun1();
                                
                println(fun2());
                println(fun2());
                """;
        String k = """
                println(2 + 3.5);
                println("Hello " + 45);
                """;

        //test asm generation
        String l = """
                int a = 1;
                int b = 2;
                int c;
                c = a + b;
                """;
        String m = """
                int fun1(int x1, int x2, int x3, int x4, int x5, int x6, int x7, int x8) {
                    int c = 10;
                    return x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8 + c;
                }
                println("fun1: %d", fun1(1,2,3,4,5,6,7,8));
                """;
        return a;
    }

    public static void main(String args[]) {
        //脚本
        String script = getScript();

        Map<String, Object> params = null;

        //解析参数
        try {
            parseParams(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        if (ParamFlag.help.isEnable()) {
            showHelp();
            return;
        }

        //从源代码读取脚本
        if (ParamFlag.scriptFile.isEnable()) {
            String scriptFile = ParamFlag.scriptFile.getAddition();
            try {
                script = readTextFile(scriptFile);
            } catch (IOException e) {
                System.out.println("unable to read from : " + scriptFile);
                return;
            }
        }

        //进入REPL
        if (script == null) {
            REPL(ParamFlag.verbose.isEnable(), ParamFlag.ast_dump.isEnable());
            return;
        }

        //生成汇编代码
        else if (ParamFlag.genAsm.isEnable()) {
            //输出文件
            if (ParamFlag.outputFile.isEnable()) {
                String outputFile = ParamFlag.outputFile.getAddition();
                generateAsm(script, outputFile);
            } else {
                generateAsm(script, null);
            }
        }

        //生成Java字节码
        else if (ParamFlag.genByteCode.isEnable()) {
            //输出文件
            //String outputFile = params.containsKey("outputFile") ? (String)params.get("outputFile") : null;
            byte[] bc = generateByteCode(script);
            runJavaClass("DefaultPlayClass", bc);
        }

        //执行脚本
        else {
            PlayScriptCompiler compiler = new PlayScriptCompiler();
            AnnotatedTree at = compiler.compile(script, ParamFlag.verbose.isEnable(), ParamFlag.ast_dump.isEnable());

            if (!at.hasCompilationError()) {
                Object result = compiler.Execute(at);
                //System.out.println(result);
            }
        }

    }

    /**
     * 解析参数
     *
     * @param args
     * @return
     */
    private static void parseParams(String args[]) throws Exception {
        Optional<ParamFlag> additon = Optional.empty();
        for (int i = 0; i < args.length; i++) {
            if (additon.isPresent()) {
                additon.get().setAddition(args[i]);
                additon = Optional.empty();
                break;
            }
            for (ParamFlag value : ParamFlag.values()) {
                if (value.enableIfMatch(args[i])) {
                    if (value.haveAddition()) {
                        additon = Optional.of(value);
                    }
                    break;
                }
                throw new Exception("Unknow parameter : " + args[i]);
            }
        }
        if (additon.isPresent()) {
            throw new Exception("Expecting a filename after " + additon.get().getFlag());
        }
    }

    /**
     * 打印帮助信息
     */
    private static void showHelp() {
        String output = """
                usage: java play.PlayScript [-h | --help | -o outputfile | -S | -bc | -v | -ast-dump] [scriptfile]
                \t-h or --help : print this help information
                \t-v verbose mode : dump AST and symbols
                \t-ast-dump : dump AST in lisp style
                \t-o outputfile : file pathname used to save generated code, eg. assembly code
                \t-S : compile to assembly code
                \t-bc : compile to java byte code
                \tscriptfile : file contains playscript code
                                
                examples:
                \tjava play.PlayScript
                \t>>interactive REPL mode
                                
                \tjava play.PlayScript -v
                \t>>enter REPL with verbose mode, dump ast and symbols
                                
                \tjava play.PlayScript scratch.play
                \t>>compile and execute scratch.play
                                
                \tjava play.PlayScript -v scratch.play
                \t>>compile and execute scratch.play in verbose mode, dump ast and symbols
                                
                \tjava play.PlayScript -bc scratch.play
                \t>>compile to bytecode, save as DefaultPlayClass.class and run it
                """;
        System.out.println(output);
    }

    /**
     * 生成ASM
     *
     * @param script     脚本
     * @param outputFile 输出的文件名
     */
    private static void generateAsm(String script, String outputFile) {
        PlayScriptCompiler compiler = new PlayScriptCompiler();
        AnnotatedTree at = compiler.compile(script);
        AsmGen asmGen = new AsmGen(at);
        String asm = asmGen.generate();
        if (outputFile != null) {
            try {
                writeTextFile(outputFile, asm);
            } catch (IOException e) {
                System.out.println("unable to write to : " + outputFile);
                return;
            }
        } else {
            System.out.println(asm);
        }
    }

    /**
     * 生成字节码，保存到DefaultPlayClass.class
     *
     * @param script 脚本
     */
    private static byte[] generateByteCode(String script) {
        PlayScriptCompiler compiler = new PlayScriptCompiler();
        AnnotatedTree at = compiler.compile(script);
        ByteCodeGen bcGen = new ByteCodeGen(at);
        byte[] bc = bcGen.generate();


        String outputFile = "DefaultPlayClass.class";

        try {
            File file = new File(outputFile);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bc);
            fos.close();
        } catch (IOException e) {
            System.out.println("unable to write to : " + outputFile);
        }

        return bc;
    }

    /**
     * 读文本文件
     *
     * @param pathName
     * @return
     * @throws IOException
     */
    private static String readTextFile(String pathName) throws IOException {
        StringBuffer buffer = new StringBuffer();
        try (FileReader reader = new FileReader(pathName);
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line).append('\n');
            }
        }
        return buffer.toString();
    }

    /**
     * 写文本文件
     *
     * @param pathName
     * @param text
     * @throws IOException
     */
    public static void writeTextFile(String pathName, String text) throws IOException {
        File file = new File(pathName);
        file.createNewFile();
        try (FileWriter writer = new FileWriter(file);
             BufferedWriter out = new BufferedWriter(writer)) {
            StringReader reader = new StringReader(text);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                out.write(line);
                out.newLine();
            }
            out.flush(); // 把缓存区内容压入文件
        }
    }

    /**
     * REPL
     */
    private static void REPL(boolean verbose, boolean ast_dump) {
        System.out.println("Enjoy PlayScript!");

        PlayScriptCompiler compiler = new PlayScriptCompiler();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String script = "";

        String scriptLet = "";
        System.out.print("\n>");   //提示符

        while (true) {
            try {
                String line = reader.readLine().trim();
                if ("exit();".equals(line)) {
                    System.out.println("good bye!");
                    break;
                }
                scriptLet += line + "\n";
                if (line.endsWith(";")) {

                    // 解析整个脚本文件
                    AnnotatedTree at = compiler.compile(script + scriptLet, verbose, ast_dump);

                    //重新执行整个脚本
                    if (!at.hasCompilationError()) {
                        Object result = compiler.Execute(at);
                        System.out.println(result);
                        script = script + scriptLet;
                    }

                    System.out.print("\n>");   //提示符

                    scriptLet = "";
                }

            } catch (Exception e) {
                // e.printStackTrace();

                System.out.println(e.getLocalizedMessage());
                System.out.print("\n>");   //提示符
                scriptLet = "";
            }
        }
    }

    /**
     * 批量运行所有的示例程序，每个示例程序执行完毕以后，需要按一个键，再继续执行下一个程序。
     */
    private static void batchTest() {

    }

    /**
     * 运行指定名称的Java类
     *
     * @param className
     */
    private static void runJavaClass(String className, byte[] b) {
        try {
            //java.lang.Class clazz = java.lang.Class.forName(className);
            java.lang.Class clazz = loadClass(className, b);
            Method method = clazz.getMethod("main", String[].class);
            method.invoke(null, (Object) new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 从byte数组加载类
     *
     * @param className
     * @param b
     * @return
     */
    private static java.lang.Class loadClass(String className, byte[] b) {
        // Override defineClass (as it is protected) and define the class.
        java.lang.Class clazz = null;
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            java.lang.Class cls = java.lang.Class.forName("java.lang.ClassLoader");
            Method method =
                    cls.getDeclaredMethod(
                            "defineClass",
                            new java.lang.Class[]{String.class, byte[].class, int.class, int.class});

            // Protected method invocation.
            method.setAccessible(true);
            try {
                Object[] args =
                        new Object[]{className, b, Integer.valueOf(0), Integer.valueOf(b.length)};
                clazz = (java.lang.Class) method.invoke(loader, args);
            } finally {
                method.setAccessible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return clazz;
    }

}