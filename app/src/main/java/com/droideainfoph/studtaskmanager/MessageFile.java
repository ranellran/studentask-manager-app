package com.droideainfoph.studtaskmanager;

public class MessageFile {
    private String senderCode;
    private String messageText;
    private String messageTextOnly;

    public MessageFile(String senderCode, String messageText) {
        this.senderCode = senderCode;
        this.messageText = messageText;
    }



    public String getSenderCode() {
        return senderCode;
    }

    public String getMessageText() {
        return messageText;
    }
}
