package data;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ChatHistorySQL {

    private static Connection getConnection(String dbPath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    // 写入聊天记录 群聊需传入groupID
    public static void insertChatMessage(String dbPath, boolean isGroup, String groupID,
                                         String peerID, String messageType, String content,
                                         boolean isSent, String ip, int port) throws SQLException {
        try (Connection conn = getConnection(dbPath)) {
            String sql = isGroup ?
                    "INSERT INTO GroupMessages (group_id, sender_id, message_type, content, is_sent, ip_address, port) VALUES (?, ?, ?, ?, ?, ?, ?)" :
                    "INSERT INTO SingleChatHistory (peer_id, message_type, message, is_sent, ip_address, port) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (isGroup) {
                    pstmt.setString(1, groupID);
                    pstmt.setString(2, peerID); // 这里peerID作为sender_id使用
                    pstmt.setString(3, messageType);
                    pstmt.setString(4, content);
                    pstmt.setBoolean(5, isSent);
                    pstmt.setString(6, ip);
                    pstmt.setInt(7, port);
                } else {
                    pstmt.setString(1, peerID);
                    pstmt.setString(2, messageType);
                    pstmt.setString(3, content);
                    pstmt.setBoolean(4, isSent);
                    pstmt.setString(5, ip);
                    pstmt.setInt(6, port);
                }
                pstmt.executeUpdate();
            }
        }
    }

    // 读取聊天记录 群聊需传入groupID
    public static List<ChatMessage> readChatHistory(String dbPath, boolean isGroup, String targetID) throws SQLException {
        List<ChatMessage> messages = new ArrayList<>();
        try (Connection conn = getConnection(dbPath)) {
            if (isGroup) {
                Map<String, String> groupNicknames = GroupSQL.getGroupMemberNicknames(dbPath, targetID);

                String sql = "SELECT * FROM GroupMessages WHERE group_id = ? ORDER BY timestamp";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, targetID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            ChatMessage message = new ChatMessage();
                            message.setChatType("group");
                            message.setMessageId(rs.getInt("message_id"));
                            message.setGroupId(targetID);

                            String senderId = rs.getString("sender_id");
                            String nickname = groupNicknames.get(senderId);
                            message.setSenderId(nickname != null ? nickname : senderId);

                            message.setMessageType(rs.getString("message_type"));
                            message.setContent(rs.getString("content"));
                            message.setSent(rs.getBoolean("is_sent"));
                            message.setIpAddress(rs.getString("ip_address"));
                            message.setPort(rs.getInt("port"));
                            message.setTimestamp(rs.getTimestamp("timestamp"));
                            messages.add(message);
                        }
                    }
                }
            } else {
                String sql = "SELECT * FROM SingleChatHistory WHERE peer_id = ? ORDER BY timestamp";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, targetID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            ChatMessage message = new ChatMessage();
                            message.setChatType("single");
                            message.setMessageId(rs.getInt("message_id"));
                            message.setPeerId(targetID);
                            if (rs.getBoolean("is_sent")) {
                                message.setSenderId("我");
                            } else {
                                message.setSenderId(targetID);
                            }
                            message.setMessageType(rs.getString("message_type"));
                            message.setContent(rs.getString("message"));
                            message.setSent(rs.getBoolean("is_sent"));
                            message.setIpAddress(rs.getString("ip_address"));
                            message.setPort(rs.getInt("port"));
                            message.setTimestamp(rs.getTimestamp("timestamp"));
                            messages.add(message);
                        }
                    }
                }
            }
        }
        return messages;
    }
}
