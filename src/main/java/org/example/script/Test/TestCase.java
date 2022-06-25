package org.example.script.Test;

import java.util.HashMap;
import java.util.Map;

public class TestCase {

    public static String getScript() {
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
}
