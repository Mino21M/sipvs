package com.example.sipvs;

import java.io.Serial;

public class DocumentVerificationException extends Exception {
    @Serial
    private static final long serialVersionUID = 6663323796434649460L;
    public DocumentVerificationException(String s) {
        super(s);
    }
    public DocumentVerificationException(String s, Exception e) {
        super(s, e);
    }
}