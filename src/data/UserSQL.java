package data;

import java.sql.*;
import java.util.*;

public class UserSQL {
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 初始化用户
    public static String initializeUser(String dbPath, String initialIp, int initialPort) {
        Random random = new Random();
        String userId = UUID.randomUUID().toString() + (10000000 + random.nextInt(90000000));
        String sql = "INSERT INTO UserInfo (user_id, last_ip, last_port) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, initialIp);
            pstmt.setInt(3, initialPort);
            pstmt.executeUpdate();
            return userId;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 更新用户IP和端口
    public static boolean updateUserNetwork(String dbPath, String userId, String newIp, int newPort) {
        String sql = "UPDATE UserInfo SET last_ip = ?, last_port = ? WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newIp);
            pstmt.setInt(2, newPort);
            pstmt.setString(3, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新用户昵称
    public static boolean updateNickname(String dbPath, String userId, String newNickname) {
        String sql = "UPDATE UserInfo SET nickname = ? WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newNickname);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取用户信息
    public static Map<String, Object> getUserInfo(String dbPath, String userId) {
        String sql = "SELECT last_ip, last_port, nickname FROM UserInfo WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> info = new HashMap<>();
                info.put("last_ip", rs.getString("last_ip"));
                info.put("last_port", rs.getInt("last_port"));
                info.put("nickname", rs.getString("nickname"));
                return info;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取全部ID
    public static List<String> getAllUserIds(String dbPath) {
        List<String> userIds = new ArrayList<>();
        String sql = "SELECT user_id FROM UserInfo";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                userIds.add(rs.getString("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userIds;
    }

}
