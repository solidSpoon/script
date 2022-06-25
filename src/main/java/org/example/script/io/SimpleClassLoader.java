package org.example.script.io;

import org.example.script.AnnotatedTree;
import org.example.script.ByteCodeGen;
import org.example.script.PlayScriptCompiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

public class SimpleClassLoader {

    /**
     * 运行指定名称的Java类
     *
     * @param className
     */
    public static void runJavaClass(String className, byte[] b) {
        try {
            //java.lang.Class clazz = java.lang.Class.forName(className);
            Class clazz = loadClass(className, b);
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
    private static Class loadClass(String className, byte[] b) {
        // Override defineClass (as it is protected) and define the class.
        Class clazz = null;
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            Class cls = Class.forName("java.lang.ClassLoader");
            Method method =
                    cls.getDeclaredMethod(
                            "defineClass",
                            new Class[]{String.class, byte[].class, int.class, int.class});

            // Protected method invocation.
            method.setAccessible(true);
            try {
                Object[] args =
                        new Object[]{className, b, Integer.valueOf(0), Integer.valueOf(b.length)};
                clazz = (Class) method.invoke(loader, args);
            } finally {
                method.setAccessible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return clazz;
    }

    /**
     * 生成字节码，保存到DefaultPlayClass.class
     *
     * @param script 脚本
     */
    public static byte[] generateByteCode(String script) {
        return generateByteCode(script, "DefaultPlayClass.class");
    }

    public static byte[] generateByteCode(String script, String outputFile) {
        PlayScriptCompiler compiler = new PlayScriptCompiler();
        AnnotatedTree at = compiler.compile(script);
        ByteCodeGen bcGen = new ByteCodeGen(at);
        byte[] bc = bcGen.generate();

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

}
