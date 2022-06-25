package org.example.script.io;

import java.io.*;

public class TextFile {
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
     * 读文本文件
     *
     * @param pathName
     * @return
     * @throws IOException
     */
    public static String readTextFile(String pathName) throws IOException {
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
}
