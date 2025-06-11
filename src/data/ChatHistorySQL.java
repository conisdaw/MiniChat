package data;

import java.sql.*;

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
    public static ResultSet readChatHistory(String dbPath, boolean isGroup, String targetID) throws SQLException {
        try (Connection conn = getConnection(dbPath)) {
            String sql;
            if (isGroup) {
                sql = "SELECT * FROM GroupMessages WHERE group_id = ? ORDER BY timestamp";
            } else {
                sql = "SELECT * FROM SingleChatHistory WHERE peer_id = ? ORDER BY timestamp";
            }

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, targetID);
            return pstmt.executeQuery();
        }
    }
}
