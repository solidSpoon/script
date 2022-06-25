package org.example.script;

import org.example.script.enums.ParamFlag;
import org.example.script.io.TextFile;

import java.io.*;
import java.util.Optional;

/**
 * 命令行工具。可以执行一个脚本，或者以REPL模式运行。
 */
public class PlayScript {

    /**
     * 解析参数
     *
     * @param args
     * @return
     */
    public static void parseParams(String args[]) {
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
                throw new RuntimeException("Unknow parameter : " + args[i]);
            }
        }
        if (additon.isPresent()) {
            throw new RuntimeException("Expecting a filename after " + additon.get().getFlag());
        }
    }

    /**
     * 打印帮助信息
     */
    public static void showHelp() {
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
    public static void generateAsm(String script, String outputFile) {
        PlayScriptCompiler compiler = new PlayScriptCompiler();
        AnnotatedTree at = compiler.compile(script);
        AsmGen asmGen = new AsmGen(at);
        String asm = asmGen.generate();
        if (outputFile != null) {
            try {
                TextFile.writeTextFile(outputFile, asm);
            } catch (IOException e) {
                System.out.println("unable to write to : " + outputFile);
                return;
            }
        } else {
            System.out.println(asm);
        }
    }


}