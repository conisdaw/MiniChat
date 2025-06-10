package data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendsSQL {
    private static Connection getConnection(String dbPath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    // 通过friend_id获取全部信息
    public static ResultSet getFriendByID(String dbPath, String friend_id) throws SQLException {
        Connection conn = getConnection(dbPath);
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM Friends WHERE friend_id = ?");
        pstmt.setString(1, friend_id);
        return pstmt.executeQuery();
    }

    // 获取全部friend_id 手动关闭结果集合
    public static List<String> getAllFriendIDs(String dbPath) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT friend_id FROM Friends");
            List<String> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getString("friend_id"));
            }
            return ids;
        }
    }

    // 新增好友
    public static void addFriend(String dbPath, String friend_id, String ip, int port) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO Friends (friend_id, ip_address, port) VALUES (?, ?, ?)")) {

            pstmt.setString(1, friend_id);
            pstmt.setString(2, ip);
            pstmt.setInt(3, port);
            pstmt.executeUpdate();
        }
    }

    // 修改端口和IP
    public static void updateNetwork(String dbPath, String friend_id, String newIp, int newPort) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE Friends SET ip_address=?, port=? WHERE friend_id=?")) {

            pstmt.setString(1, newIp);
            pstmt.setInt(2, newPort);
            pstmt.setString(3, friend_id);
            pstmt.executeUpdate();
        }
    }

    // 修改好友昵称
    public static void updateNickname(String dbPath, String friend_id, String nickname) throws SQLException {
        updateField(dbPath, friend_id, "nickname", nickname);
    }

    // 修改好友备注名
    public static void updateRemark(String dbPath, String friend_id, String remark) throws SQLException {
        updateField(dbPath, friend_id, "remark", remark);
    }

    // 更改拉黑状态
    public static void toggleBlock(String dbPath, String friend_id, boolean isBlocked) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE Friends SET is_blocked=? WHERE friend_id=?")) {

            pstmt.setBoolean(1, isBlocked);
            pstmt.setString(2, friend_id);
            pstmt.executeUpdate();
        }
    }

    // 字段更新方法
    private static void updateField(String dbPath, String friend_id, String field, String value) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE Friends SET " + field + "=? WHERE friend_id=?")) {

            pstmt.setString(1, value);
            pstmt.setString(2, friend_id);
            pstmt.executeUpdate();
        }
    }
}
