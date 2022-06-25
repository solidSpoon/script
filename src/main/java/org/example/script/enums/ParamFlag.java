package org.example.script.enums;

import java.util.ArrayList;
import java.util.List;

public enum ParamFlag {
    /**
     * 输出汇编代码
     */
    genAsm(false, "-S"),
    /**
     * 生成字节码
     */
    genByteCode(false, "-bc"),
    /**
     * 帮助
     */
    help(false, "-h", "--help"),
    verbose(false, "-v", "--verbose"),
    ast_dump(false, "-ast-dump"),
    outputFile(true, "-o", "--output"),
    scriptFile(true, "-f", "--file");

    private final List<String> flags = new ArrayList<>();

    private final boolean haveAddition;
    private boolean enable = false;

    private String addition;

    public String getAddition() {
        return addition;
    }
    public void setAddition(String addition) {
        this.addition = addition;
    }

    public boolean enableIfMatch(String s) {
        if (flags.contains(s)) {
            enable = true;
            return true;
        }
        return false;
    }

    public boolean haveAddition() {
        return haveAddition;
    }

    public boolean isEnable() {
        return enable;
    }

    ParamFlag(boolean haveAddition, String... flag) {
        this.haveAddition = haveAddition;
        flags.addAll(List.of(flag));
    }

    public String getFlag() {
        return flags.get(0);
    }
}
