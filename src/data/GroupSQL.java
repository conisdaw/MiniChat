package data;

import core.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupSQL {
    // 检查groupId是否存在
    public static boolean isGroupIdExists(String dbPath, String groupId) {
        String sql = "SELECT 1 FROM Groups WHERE group_id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("检查群ID失败: " + e.getMessage());
            return true;
        }
    }


    // 通过群ID获取全部群成员ID
    public static List<String> getGroupMemberIds(String groupId) {
        List<String> memberIds = new ArrayList<>();
        String sql = "SELECT member_id FROM GroupMembers WHERE group_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Config.DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, groupId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                memberIds.add(rs.getString("member_id"));
            }
        } catch (SQLException e) {
            System.err.println("获取群成员失败: " + e.getMessage());
        }
        return memberIds;
    }

    // 创建群聊
    public static boolean createGroup(String dbPath, String groupId, String groupName,
                                      String ownerId) {
        String sqlGroups = "INSERT INTO Groups (group_id, group_name, owner_id) VALUES (?, ?, ?)";
        String sqlMember = "INSERT INTO GroupMembers (group_id, member_id, role, ip_address, port) VALUES (?, ?, 'owner', ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            try (PreparedStatement pstmt1 = conn.prepareStatement(sqlGroups);
                 PreparedStatement pstmt3 = conn.prepareStatement(sqlMember)) {

                // 插入群组基本信息
                pstmt1.setString(1, groupId);
                pstmt1.setString(2, groupName);
                pstmt1.setString(3, ownerId);
                pstmt1.executeUpdate();

                // 添加群主到成员表
                pstmt3.setString(1, groupId);
                pstmt3.setString(2, ownerId);
                pstmt3.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // 删除群聊
    public static boolean deleteGroup(String dbPath, String groupId) {
        String[] sqls = {
                "DELETE FROM GroupMembers WHERE group_id = ?",
                "DELETE FROM Groups WHERE group_id = ?"
        };
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            conn.setAutoCommit(false);
            try {
                for (String sql : sqls) {
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, groupId);
                        pstmt.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // 添加群成员
    public static boolean addMember(String dbPath, String groupId, String memberId) {
        String sql = "INSERT INTO GroupMembers (group_id, member_id) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, groupId);
            pstmt.setString(2, memberId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 检查用户是否为owner或admin
    public static boolean isOwnerOrAdmin(String groupId, String userId) {
        String sql = "SELECT role FROM GroupMembers WHERE group_id = ? AND member_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Config.DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, groupId);
            pstmt.setString(2, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                return "owner".equals(role) || "admin".equals(role);
            }
        } catch (SQLException e) {
            System.err.println("权限检查失败: " + e.getMessage());
        }
        return false;
    }

    // 检查用户是否为owner
    public static boolean isOwner(String groupId, String userId) {
        String sql = "SELECT role FROM GroupMembers WHERE group_id = ? AND member_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + Config.DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, groupId);
            pstmt.setString(2, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return "owner".equals(rs.getString("role"));
            }
        } catch (SQLException e) {
            System.err.println("权限检查失败: " + e.getMessage());
        }
        return false;
    }
}