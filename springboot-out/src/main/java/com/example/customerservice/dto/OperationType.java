package com.example.customerservice.dto;

public enum OperationType {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    LIST,
    UNKNOWN;

    public static OperationType fromString(String text) {
        for (OperationType op : OperationType.values()) {
            if (op.name().equalsIgnoreCase(text)) {
                return op;
            }
        }
        return UNKNOWN;
    }
}
