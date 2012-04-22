package org.jmendeth.redasm;

public class RABCDasmException extends Exception {
    private final int code;
    private final String out;

    public RABCDasmException(int code, String out, String message) {
        super(message);
        this.code = code;
        this.out = out;
    }
}