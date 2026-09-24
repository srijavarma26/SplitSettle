package com.splitsettle.settlementservice.exception;

public class SettlementNotFoundException extends RuntimeException {
    public SettlementNotFoundException(String message) {
        super(message);
    }
}
