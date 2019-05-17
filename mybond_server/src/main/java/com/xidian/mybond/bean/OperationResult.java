package com.xidian.mybond.bean;

public class OperationResult {

    private boolean status;   //操作结果
    private String operation;  //操作
    private String message;    //描述

    public OperationResult(){

    }

    public OperationResult(boolean status, String operation, String message) {
        this.status = status;
        this.operation = operation;
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "OperationResult{" +
                "status=" + status +
                ", operation='" + operation + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
