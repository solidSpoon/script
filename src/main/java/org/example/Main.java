package org.example;

import org.example.script.AnnotatedTree;
import org.example.script.PlayScript;
import org.example.script.PlayScriptCompiler;
import org.example.script.Test.TestCase;
import org.example.script.enums.ParamFlag;
import org.example.script.io.SimpleClassLoader;
import org.example.script.io.TextFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String args[]) {
        //脚本
        String script = TestCase.getScript();
        PlayScript.parseParams(args);

        if (ParamFlag.help.isEnable()) {
            PlayScript.showHelp();
            return;
        }

        //从源代码读取脚本
        if (ParamFlag.scriptFile.isEnable()) {
            String scriptFile = ParamFlag.scriptFile.getAddition();
            try {
                script = TextFile.readTextFile(scriptFile);
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
        if (ParamFlag.genAsm.isEnable()) {
            //输出文件
            if (ParamFlag.outputFile.isEnable()) {
                String outputFile = ParamFlag.outputFile.getAddition();
                PlayScript.generateAsm(script, outputFile);
            } else {
                PlayScript.generateAsm(script, null);
            }
            return;
        }

        //生成Java字节码
        if (ParamFlag.genByteCode.isEnable()) {
            //输出文件
            //String outputFile = params.containsKey("outputFile") ? (String)params.get("outputFile") : null;
            byte[] bc = SimpleClassLoader.generateByteCode(script);
            SimpleClassLoader.runJavaClass("DefaultPlayClass", bc);
            return;
        }

        //执行脚本
        PlayScriptCompiler compiler = new PlayScriptCompiler();
        AnnotatedTree at = compiler.compile(script, ParamFlag.verbose.isEnable(), ParamFlag.ast_dump.isEnable());
        if (!at.hasCompilationError()) {
            Object result = compiler.Execute(at);
            //System.out.println(result);
        }
    }

    /**
     * REPL
     */
    public static void REPL(boolean verbose, boolean ast_dump) {
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
}