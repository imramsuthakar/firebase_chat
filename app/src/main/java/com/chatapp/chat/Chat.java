package com.chatapp.chat;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Chat {
    @SerializedName("key")
    @Expose
    private String key;
    @SerializedName("chat_path")
    @Expose
    private String chatPath;
    @SerializedName("sender")
    @Expose
    private Integer sender;
    @SerializedName("timestamp")
    @Expose
    private Long timestamp;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("reciever")
    @Expose
    private Integer receiver;
    @SerializedName("read")
    @Expose
    private Integer read;

    @SerializedName("lat")
    @Expose
    private String lat;

    @SerializedName("lat")
    @Expose
    private String lng;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getChatPath() {
        return chatPath;
    }

    public void setChatPath(String chatPath) {
        this.chatPath = chatPath;
    }

    public Integer getSender() {
        return sender;
    }

    public void setSender(Integer sender) {
        this.sender = sender;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getReceiver() {
        return receiver;
    }

    public void setReceiver(Integer receiver) {
        this.receiver = receiver;
    }

    public Integer getRead() {
        return read;
    }

    public void setRead(Integer read) {
        this.read = read;
    }
}
