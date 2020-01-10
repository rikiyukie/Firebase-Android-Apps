package com.fans.edu.Model;

public class ChatMessage {

    private String userEmailChat, userMessageChat, sendDate;

    public ChatMessage() {
    }

    public ChatMessage(String userEmailChat, String userMessageChat, String sendDate) {
        this.userEmailChat = userEmailChat;
        this.userMessageChat = userMessageChat;
        this.sendDate = sendDate;
    }

    public String getUserEmailChat() {
        return userEmailChat;
    }

    public void setUserEmailChat(String userEmailChat) {
        this.userEmailChat = userEmailChat;
    }

    public String getUserMessageChat() {
        return userMessageChat;
    }

    public void setUserMessageChat(String userMessageChat) {
        this.userMessageChat = userMessageChat;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }
}
