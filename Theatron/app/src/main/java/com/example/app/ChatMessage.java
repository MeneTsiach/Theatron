package com.example.app;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private boolean showCancelIcon; // νέο

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.showCancelIcon = false; // default
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setShowCancelIcon(boolean showCancelIcon) {
        this.showCancelIcon = showCancelIcon;
    }

    public boolean shouldShowCancelIcon() {
        return showCancelIcon;
    }
}
