package data;

import java.sql.Timestamp;

public class ChatMessage {
    private int messageId;
    private String chatType; // "single" 或 "group"
    private String groupId;
    private String peerId;
    private String senderId;
    private String messageType;
    private String content;
    private boolean isSent;
    private String ipAddress;
    private int port;
    private Timestamp timestamp;

    // 构造器
    public ChatMessage() {}

    // Getter 和 Setter 方法
    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }
    
    public String getChatType() { return chatType; }
    public void setChatType(String chatType) { this.chatType = chatType; }
    
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    
    public String getPeerId() { return peerId; }
    public void setPeerId(String peerId) { this.peerId = peerId; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public boolean isSent() { return isSent; }
    public void setSent(boolean sent) { isSent = sent; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId=" + messageId +
                ", chatType='" + chatType + '\'' +
                ", groupId='" + groupId + '\'' +
                ", peerId='" + peerId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", content='" + content + '\'' +
                ", isSent=" + isSent +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", timestamp=" + timestamp +
                '}';
    }

}