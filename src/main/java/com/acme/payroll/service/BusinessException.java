package com.acme.payroll.service;

/** A business rule was violated; the message is shown to the user as is. */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
