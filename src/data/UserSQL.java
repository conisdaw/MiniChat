package data;

import core.Config;

import java.sql.*;
import java.util.*;

public class UserSQL {
    private static final String DB_URL = "jdbc:sqlite:" + Config.DB_PATH;

    // 用户登录
    public static Map<String, String> login(String account, String password) {
        String sql = "SELECT user_id FROM UserInfo WHERE account = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                Map<String, String> result = new HashMap<>();
                if (rs.next()) {
                    result.put("success", "true");
                    result.put("user_id", rs.getString("user_id"));
                } else {
                    result.put("success", "false");
                    result.put("user_id","Ciallo～(∠・ω< )⌒★账号或密码错误!!!");
                }
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Map<String, String> result = new HashMap<>();
            result.put("success", "false");
            return result;
        }
    }

    // 获取账号
    public static String getUserAccount(String userId) {
        String sql = "SELECT account FROM UserInfo WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("account");
                }
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 检查账户是否被禁用
    public static boolean isAccountBlocked(String account) {
        String sql = "SELECT is_blocked FROM UserInfo WHERE account = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("is_blocked") == 1;
                }
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 用户注册
    public static boolean register(String nickname, String account, String password, String ip, int port) {
        String sql = "INSERT INTO UserInfo(user_id, nickname, account, password, ip, port) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (isAccountExists(conn, account)) {
                return false;
            }

            String userId;
            int maxAttempts = 10;
            do {
                userId = generateUserId();
                maxAttempts--;
            } while (isUserIdExists(conn, userId) && maxAttempts > 0);

            if (maxAttempts <= 0) return false;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, nickname);
                pstmt.setString(3, account);
                pstmt.setString(4, password);
                pstmt.setString(5, ip);
                pstmt.setInt(6, port);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新用户昵称
    public static boolean updateNickname(String userId, String nickname) {
        String sql = "UPDATE UserInfo SET nickname = ? WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nickname);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新用户网络信息
    public static boolean updateNetwork(String userId, String ip, int port) {
        String sql = "UPDATE UserInfo SET ip = ?, port = ? WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ip);
            pstmt.setInt(2, port);
            pstmt.setString(3, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 反转用户状态
    public static boolean toggleUserBlockStatus(String userId) {
        String sql = "UPDATE UserInfo SET is_blocked = 1 - is_blocked WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取所有用户
    public static List<Map<String, String>> getAllUsers() {
        List<Map<String, String>> users = new ArrayList<>();
        String sql = "SELECT user_id, nickname FROM UserInfo";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, String> user = new HashMap<>();
                user.put("user_id", rs.getString("user_id"));
                user.put("nickname", rs.getString("nickname"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 获取所有用户
    public static List<String> getAllUsersId() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT user_id, nickname FROM UserInfo";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) users.add(rs.getString("user_id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 获取IP和端口信息
    public static Map<String, String> getUserNetworkInfo(String userId) {
        String sql = "SELECT ip, port FROM UserInfo WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> networkInfo = new HashMap<>();
                    networkInfo.put("ip", rs.getString("ip"));
                    networkInfo.put("port", String.valueOf(rs.getInt("port")));
                    return networkInfo;
                }
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取用户名
    public static String getUserNickname(String userId) {
        String sql = "SELECT nickname FROM UserInfo WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nickname");
                }
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取用户ID
    public static String getUserId(String account) {
        String sql = "SELECT user_id FROM UserInfo WHERE account = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user_id");
                }
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    // 辅助方法

    // 生成用户ID方法
    private static String generateUserId() {
        Random random = new Random();
        return UUID.randomUUID().toString() + (10000000 + random.nextInt(90000000));
    }

    // 检查用户ID是否存在
    private static boolean isUserIdExists(Connection conn, String userId) throws SQLException {
        String query = "SELECT 1 FROM UserInfo WHERE user_id = ? LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // 检查账号是否存在
    private static boolean isAccountExists(Connection conn, String account) throws SQLException {
        String query = "SELECT 1 FROM UserInfo WHERE account = ? LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, account);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

}
