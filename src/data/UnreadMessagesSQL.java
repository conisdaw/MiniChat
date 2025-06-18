package data;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

import core.Config;

public class UnreadMessagesSQL {
    private static final String DB_URL = "jdbc:sqlite:" + Config.DB_PATH;

    // 将未读消息写入数据库
    public static void saveUnreadMessages(
            ConcurrentHashMap<String, CopyOnWriteArrayList<ChatMessage>> friendMessages,
            ConcurrentHashMap<String, CopyOnWriteArrayList<ChatMessage>> groupMessages) {

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // 开启事务
            conn.setAutoCommit(false);

            // 清空表
            stmt.execute("DELETE FROM UnreadMessages");

            String sql = "INSERT INTO UnreadMessages (chat_type, target_id, sender_id, message_type, content, timestamp) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // 保存好友未读消息
                for (Map.Entry<String, CopyOnWriteArrayList<ChatMessage>> entry : friendMessages.entrySet()) {
                    String friendId = entry.getKey();
                    for (ChatMessage msg : entry.getValue()) {
                        addMessageToBatch(pstmt, "friend", friendId, msg);
                    }
                }

                // 保存群组未读消息
                for (Map.Entry<String, CopyOnWriteArrayList<ChatMessage>> entry : groupMessages.entrySet()) {
                    String groupId = entry.getKey();
                    for (ChatMessage msg : entry.getValue()) {
                        addMessageToBatch(pstmt, "group", groupId, msg);
                    }
                }

                pstmt.executeBatch();
            }

            // 提交事务
            conn.commit();
        } catch (SQLException e) {
            System.err.println("保存未读消息失败: " + e.getMessage());
        }
    }

    private static void addMessageToBatch(PreparedStatement pstmt, String chatType,
                                          String targetId, ChatMessage msg) throws SQLException {
        pstmt.setString(1, chatType);
        pstmt.setString(2, targetId);
        pstmt.setString(3, msg.getSenderId());
        pstmt.setString(4, msg.getMessageType());
        pstmt.setString(5, msg.getContent());
        pstmt.setTimestamp(6, msg.getTimestamp());
        pstmt.addBatch();
    }

    public static void loadUnreadMessages(
            ConcurrentHashMap<String, CopyOnWriteArrayList<ChatMessage>> friendMessages,
            ConcurrentHashMap<String, CopyOnWriteArrayList<ChatMessage>> groupMessages) {

        String sql = "SELECT chat_type, target_id, sender_id, message_type, content, timestamp " +
                "FROM UnreadMessages";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ChatMessage msg = new ChatMessage();
                msg.setSenderId(rs.getString("sender_id"));
                msg.setMessageType(rs.getString("message_type"));
                msg.setContent(rs.getString("content"));
                msg.setTimestamp(rs.getTimestamp("timestamp"));

                String chatType = rs.getString("chat_type");
                String targetId = rs.getString("target_id");

                if ("friend".equals(chatType)) {
                    friendMessages.computeIfAbsent(targetId, k -> new CopyOnWriteArrayList<>()).add(msg);
                } else if ("group".equals(chatType)) {
                    groupMessages.computeIfAbsent(targetId, k -> new CopyOnWriteArrayList<>()).add(msg);
                }
            }
        } catch (SQLException e) {
            System.err.println("加载未读消息失败: " + e.getMessage());
        }
    }
}