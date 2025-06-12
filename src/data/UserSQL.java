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
    public static String initialize(String dbPath, String account, String password, String nickname) {
        Random random = new Random();
        String userId = UUID.randomUUID().toString() + (10000000 + random.nextInt(90000000));
        String sql = "INSERT INTO UserInfo (user_id, account, password, nickname) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, account);
            pstmt.setString(3, password);
            pstmt.setString(4, nickname);
            pstmt.executeUpdate();
            return userId;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
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

    // 更新用户端口
    public static boolean updatePort(String dbPath, int port, String userId) {
        String sql = "UPDATE UserInfo SET port = ? WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, port);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取用户上次登录端口
    public static int getUserPort(String dbPath, String userId) {
        String sql = "SELECT port FROM UserInfo WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) return rs.getInt("port");
            return 5090;
        } catch (SQLException e) {
            e.printStackTrace();
            return 5090;
        }
    }

    // 获取用户信息
    public static Map<String, Object> getUserInfo(String dbPath, String userId) {
        String sql = "SELECT account, password, nickname FROM UserInfo WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> info = new HashMap<>();
                info.put("account", rs.getString("account"));
                info.put("password", rs.getString("password"));
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
