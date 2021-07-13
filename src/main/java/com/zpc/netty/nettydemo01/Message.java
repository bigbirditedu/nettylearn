package com.zpc.netty.nettydemo01;

import java.util.Date;

public class Message {
    private String username;
    private Date sentTime;
    private String msg;

    public Message(String username, Date sentTime, String msg) {
        this.username = username;
        this.sentTime = sentTime;
        this.msg = msg;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}