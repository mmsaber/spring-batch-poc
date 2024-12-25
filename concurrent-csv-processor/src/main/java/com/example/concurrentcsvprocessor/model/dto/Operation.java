package com.example.concurrentcsvprocessor.model.dto;

import java.util.ArrayList;
import java.util.List;

public enum Operation {
    SUM("Sum"),
    MULTIPLY("Multiplication"),
    DIVIDE("Division");

    private final String hm;

    Operation(String hm) {
        this.hm = hm;
    }

    public static Operation[] from(String... operations) {
        if (operations == null || operations.length == 0) {
            return new Operation[0];
        }
        List<Operation> operationList = new ArrayList<>();
        for (String operation : operations) {
            try {
                operationList.add(valueOf(operation.toUpperCase()));
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }

        return operationList.toArray(new Operation[0]);
    }

    public static String[] to(Operation[] operations) {
        if (operations == null || operations.length == 0) {
            return new String[0];
        }

        String[] returnVal = new String[operations.length];
        for (int i = 0; i < operations.length; i++) {
            returnVal[i] = operations[i].hm;
        }

        return returnVal;
    }

    @Override
    public String toString() {
        return "Operation{" + "name='" + hm + "'}";
    }
}
