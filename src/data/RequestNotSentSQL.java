package data;

import core.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestNotSentSQL {
    private static final String DB_URL = "jdbc:sqlite:" + Config.DB_PATH;
    private static final String CLEAR_TABLE_SQL = "DELETE FROM PendingRequests";
    private static final String INSERT_SQL = "INSERT INTO PendingRequests (user_id, request_url, json_body) VALUES (?, ?, ?)";
    private static final String SELECT_BY_USER_SQL = "SELECT * FROM PendingRequests WHERE user_id = ?";
    private static final String DELETE_BY_USER_SQL = "DELETE FROM PendingRequests WHERE user_id = ?";

    /**
     * 清空待发送请求表并重新插入所有待发送请求
     * @param pendingRequests 待发送请求映射表
     */
    public static void saveAll(Map<String, List<RequestNotSent.PendingRequest>> pendingRequests) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // 开启事务
            conn.setAutoCommit(false);

            // 清空表
            stmt.executeUpdate(CLEAR_TABLE_SQL);

            // 批量插入新数据
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {
                for (Map.Entry<String, List<RequestNotSent.PendingRequest>> entry : pendingRequests.entrySet()) {
                    String userId = entry.getKey();
                    for (RequestNotSent.PendingRequest request : entry.getValue()) {
                        pstmt.setString(1, userId);
                        pstmt.setString(2, request.getRequestUrl());
                        pstmt.setString(3, request.getJsonBody());
                        pstmt.addBatch();
                    }
                }
                pstmt.executeBatch();
            }

            // 提交事务
            conn.commit();
        } catch (SQLException e) {
            System.err.println("保存待发送请求失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从数据库加载所有待发送请求
     * @return 待发送请求映射表
     */
    public static Map<String, List<RequestNotSent.PendingRequest>> loadAll() {
        Map<String, List<RequestNotSent.PendingRequest>> map = new ConcurrentHashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, request_url, json_body FROM PendingRequests")) {

            while (rs.next()) {
                String userId = rs.getString("user_id");
                String url = rs.getString("request_url");
                String json = rs.getString("json_body");

                map.computeIfAbsent(userId, k -> new ArrayList<>())
                        .add(new RequestNotSent.PendingRequest(url, json));
            }
        } catch (SQLException e) {
            System.err.println("加载待发送请求失败: " + e.getMessage());
            e.printStackTrace();
        }
        return map;
    }
}