package com.example.chatapplication.Model;

import java.util.Date;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    private Date time;
    private boolean isseen;
    private String type;

    public Chat() {
    }

    public Chat(String sender, String receiver, String message, Date time, String type) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}