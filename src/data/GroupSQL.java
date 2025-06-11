package data;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupSQL {

    // 建立新群组
    public static boolean createGroup(String dbPath, String groupId, String groupName,
                                      String ownerId, String ip, int port) {
        String sqlGroups = "INSERT INTO Groups (group_id, group_name, owner_id) VALUES (?, ?, ?)";
        String sqlSettings = "INSERT INTO GroupSettings (group_id) VALUES (?)";
        String sqlMember = "INSERT INTO GroupMembers (group_id, member_id, role, ip_address, port) VALUES (?, ?, 'owner', ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            try (PreparedStatement pstmt1 = conn.prepareStatement(sqlGroups);
                 PreparedStatement pstmt2 = conn.prepareStatement(sqlSettings);
                 PreparedStatement pstmt3 = conn.prepareStatement(sqlMember)) {

                // 插入群组基本信息
                pstmt1.setString(1, groupId);
                pstmt1.setString(2, groupName);
                pstmt1.setString(3, ownerId);
                pstmt1.executeUpdate();

                // 初始化群设置
                pstmt2.setString(1, groupId);
                pstmt2.executeUpdate();

                // 添加群主到成员表
                pstmt3.setString(1, groupId);
                pstmt3.setString(2, ownerId);
                pstmt3.setString(3, ip);
                pstmt3.setInt(4, port);
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

    // 解散群组
    public static boolean deleteGroup(String dbPath, String groupId) {
        String[] sqls = {
                "DELETE FROM GroupMembers WHERE group_id = ?",
                "DELETE FROM GroupSettings WHERE group_id = ?",
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
    public static boolean addMember(String dbPath, String groupId, String memberId,
                                    String ip, int port, String nickname) {
        String sql = "INSERT OR IGNORE INTO GroupMembers (group_id, member_id, ip_address, port, nickname_in_group) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            pstmt.setString(2, memberId);
            pstmt.setString(3, ip);
            pstmt.setInt(4, port);
            pstmt.setString(5, nickname);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 移除群成员
    public static boolean removeMember(String dbPath, String groupId, String memberId) {
        String sql = "DELETE FROM GroupMembers WHERE group_id = ? AND member_id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            pstmt.setString(2, memberId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 更新成员群昵称
    public static boolean updateMemberNickname(String dbPath, String groupId,
                                               String memberId, String nickname) {
        String sql = "UPDATE GroupMembers SET nickname_in_group = ? WHERE group_id = ? AND member_id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nickname);
            pstmt.setString(2, groupId);
            pstmt.setString(3, memberId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 更新成员IP和端口
    public static boolean updateMemberNetwork(String dbPath, String groupId,
                                              String memberId, String ip, int port) {
        String sql = "UPDATE GroupMembers SET ip_address = ?, port = ? WHERE group_id = ? AND member_id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ip);
            pstmt.setInt(2, port);
            pstmt.setString(3, groupId);
            pstmt.setString(4, memberId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 获取所有群组ID
    public static List<String> getAllGroupIds(String dbPath) {
        String sql = "SELECT group_id FROM Groups";
        List<String> groupIds = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                groupIds.add(rs.getString("group_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupIds;
    }

    // 获取指定群组所有成员的IP和端口
    public static Map<String, Map<String, Object>> getGroupMembersNetwork(String dbPath, String groupId) {
        String sql = "SELECT member_id, ip_address, port FROM GroupMembers WHERE group_id = ?";
        Map<String, Map<String, Object>> members = new HashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> network = new HashMap<>();
                    network.put("ip", rs.getString("ip_address"));
                    network.put("port", rs.getInt("port"));
                    members.put(rs.getString("member_id"), network);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    // 获取群组设置信息
    public static Map<String, Object> getGroupSettings(String dbPath, String groupId) {
        String sql = "SELECT * FROM GroupSettings WHERE group_id = ?";
        Map<String, Object> settings = new HashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    settings.put("allow_invites", rs.getBoolean("allow_invites"));
                    settings.put("approval_required", rs.getBoolean("approval_required"));
                    settings.put("history_visible", rs.getBoolean("history_visible"));
                    settings.put("max_admins", rs.getInt("max_admins"));
                    settings.put("pinned_message_id", rs.getInt("pinned_message_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return settings;
    }

    // 获取指定群组所有成员的群昵称
    public static Map<String, String> getGroupMemberNicknames(String dbPath, String groupId) {
        String sql = "SELECT member_id, nickname_in_group FROM GroupMembers WHERE group_id = ?";
        Map<String, String> nicknames = new HashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String memberId = rs.getString("member_id");
                    String nickname = rs.getString("nickname_in_group");
                    nicknames.put(memberId, nickname != null ? nickname : "");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nicknames;
    }


    // 获取群组名称
    public static String getGroupName(String dbPath, String groupId) {
        String sql = "SELECT group_name FROM Groups WHERE group_id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("group_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 以下方法以弃用
    // 更新成员群组头像路径
    public static boolean updateMemberAvatar(String dbPath, String groupId,
                                             String memberId, String avatarPath) {
        String sql = "UPDATE GroupMembers SET avatar_path_group = ? WHERE group_id = ? AND member_id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, avatarPath);
            pstmt.setString(2, groupId);
            pstmt.setString(3, memberId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 获取成员群组头像路径
    public static String getMemberAvatar(String dbPath, String groupId, String memberId) {
        String sql = "SELECT avatar_path_group FROM GroupMembers WHERE group_id = ? AND member_id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            pstmt.setString(2, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("avatar_path_group") : null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    // 更新群组头像路径
    public static boolean updateGroupAvatar(String dbPath, String groupId, String avatarPath) {
        String sql = "UPDATE Groups SET avatar_path = ? WHERE group_id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, avatarPath);
            pstmt.setString(2, groupId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 获取群组头像路径
    public static String getGroupAvatar(String dbPath, String groupId) {
        String sql = "SELECT avatar_path FROM Groups WHERE group_id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("avatar_path") : null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    // 获取所有群组的头像信息和群ID
    public static Map<String, String> getAllGroupAvatars(String dbPath) {
        String sql = "SELECT group_id, avatar_path FROM Groups";
        Map<String, String> avatars = new HashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                avatars.put(rs.getString("group_id"), rs.getString("avatar_path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return avatars;
    }

    public static boolean updateGroupName(String dbPath, String groupId, String newGroupName) {
    String sql = "UPDATE Groups SET group_name = ? WHERE group_id = ?";
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, newGroupName);
        pstmt.setString(2, groupId);
        return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
        return false;
    }
}



}
