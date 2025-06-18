package data;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsSQL {
    private static Connection getConnection(String dbPath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    // 通过friend_id获取全部信息
    public static Map<String, String> getFriendByID(String dbPath, String friend_id) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM Friends WHERE friend_id = ?")) {

            pstmt.setString(1, friend_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> friendMap = new HashMap<>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        friendMap.put(columnName, rs.getString(i));
                    }
                    return friendMap;
                }
            }
        }
        // 未找到记录时返回空Map
        return new HashMap<>();
    }

    // 获取全部未拉黑的friend_id
    public static List<Map<String, String>> getAllFriendIDs(String dbPath) throws SQLException {
    try (Connection conn = getConnection(dbPath);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(
                "SELECT friend_id, nickname, remark FROM Friends WHERE is_blocked = false"
            );

            List<Map<String, String>> friendsList = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> friendInfo = new HashMap<>();
                String friendId = rs.getString("friend_id");
                String nickname = rs.getString("nickname");
                String remark = rs.getString("remark");

                String displayName = (remark != null && !remark.isEmpty())
                                    ? remark : nickname;

                friendInfo.put("friend_id", friendId);
                friendInfo.put("name", displayName);
                friendsList.add(friendInfo);
            }
            return friendsList;
        }
    }

    // 获取全被被拉黑的friend_id
    public static List<Map<String, String>> getAllBlockedFriendIDs(String dbPath) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(
                    "SELECT friend_id, nickname, remark FROM Friends WHERE is_blocked = true"
            );

            List<Map<String, String>> friendsList = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> friendInfo = new HashMap<>();
                String friendId = rs.getString("friend_id");
                String nickname = rs.getString("nickname");
                String remark = rs.getString("remark");

                String displayName = (remark != null && !remark.isEmpty())
                        ? remark : nickname;

                friendInfo.put("friend_id", friendId);
                friendInfo.put("name", displayName);
                friendsList.add(friendInfo);
            }
            return friendsList;
        }
    }

    // 检查对应的ID是否被拉黑
    public static boolean isFriendUnblocked(String dbPath, String friend_id) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT is_blocked FROM Friends WHERE friend_id = ?")) {

            pstmt.setString(1, friend_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return !rs.getBoolean("is_blocked");
                }
            }
        }
        return false;
    }

    // 新增好友
    public static void addFriend(String dbPath, String friend_id, String ip, int port) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(

                     "INSERT INTO Friends (friend_id, ip_address, port) VALUES (?, ?, ?) " + "ON CONFLICT(friend_id) DO UPDATE SET ip_address=excluded.ip_address, port=excluded.port")) {

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
    public static void toggleBlockStatus(String dbPath, String friend_id) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE Friends SET is_blocked = NOT is_blocked WHERE friend_id=?")) {
            pstmt.setString(1, friend_id);
            pstmt.executeUpdate();
        }
    }

    // 删除好友
    public static void deleteFriend(String dbPath, String friend_id) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM Friends WHERE friend_id = ?")) {

            pstmt.setString(1, friend_id);
            pstmt.executeUpdate();
        }
    }

    // 获取所有好友的IP地址和端口号
    public static List<Map<String, String>> getAllFriendIPsAndPorts(String dbPath) throws SQLException {
        try (Connection conn = getConnection(dbPath);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT ip_address, port FROM Friends");

            List<Map<String, String>> resultList = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> ipPortMap = new HashMap<>();
                // 获取IP地址
                String ip = rs.getString("ip_address");
                // 将端口号从int转为String
                String port = String.valueOf(rs.getInt("port"));

                ipPortMap.put("ip", ip);
                ipPortMap.put("port", port);
                resultList.add(ipPortMap);
            }
            return resultList;
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
