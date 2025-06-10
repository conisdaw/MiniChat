package data;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class test {
    private static final String DB_PATH = "DATA.db";

    public static void main(String[] args) {
        testUserSQL();
        testFriendsSQL();
        testChatHistorySQL();
        testGroupSQL(); // 新增群组测试
        System.out.println("所有测试执行完毕");
    }

    private static void testUserSQL() {
        System.out.println("\n=== 开始测试 UserSQL ===");

        String userId = testInitializeUser();
        testUpdateUserNetwork(userId);
        testUpdateNickname(userId);
        testGetAllUserIds();

        System.out.println("=== UserSQL 测试完成 ===");
    }

    private static String testInitializeUser() {
        System.out.print("测试 initializeUser...");
        String userId = UserSQL.initializeUser(DB_PATH, "192.168.1.1", 8080);

        try {
            assertNotNull(userId, "用户ID不应为null");
            Map<String, Object> info = UserSQL.getUserInfo(DB_PATH, userId);
            assertEquals("192.168.1.1", info.get("last_ip"), "IP地址不匹配");
            assertEquals(8080, info.get("last_port"), "端口号不匹配");
            System.out.println("通过");
        } catch (AssertionError e) {
            System.out.println("失败: " + e.getMessage());
        }
        return userId;
    }

    private static void testUpdateUserNetwork(String userId) {
        System.out.print("测试 updateUserNetwork...");
        boolean result = UserSQL.updateUserNetwork(DB_PATH, userId, "10.0.0.1", 9090);

        try {
            assertTrue(result, "更新操作应返回true");
            Map<String, Object> info = UserSQL.getUserInfo(DB_PATH, userId);
            assertEquals("10.0.0.1", info.get("last_ip"), "更新后IP不匹配");
            assertEquals(9090, info.get("last_port"), "更新后端口不匹配");
            System.out.println("通过");
        } catch (AssertionError e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    private static void testUpdateNickname(String userId) {
        System.out.print("测试 updateNickname...");
        boolean result = UserSQL.updateNickname(DB_PATH, userId, "测试用户");

        try {
            assertTrue(result, "昵称更新应返回true");
            Map<String, Object> info = UserSQL.getUserInfo(DB_PATH, userId);
            assertEquals("测试用户", info.get("nickname"), "昵称不匹配");
            System.out.println("通过");
        } catch (AssertionError e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    private static void testGetAllUserIds() {
        System.out.print("测试 getAllUserIds...");
        executeSQL("DELETE FROM UserInfo"); // 清空表

        UserSQL.initializeUser(DB_PATH, "192.168.1.1", 8080);
        UserSQL.initializeUser(DB_PATH, "192.168.1.2", 8081);

        try {
            List<String> ids = UserSQL.getAllUserIds(DB_PATH);
            assertEquals(2, ids.size(), "用户数量不匹配");
            System.out.println("通过");
        } catch (AssertionError e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    private static void testFriendsSQL() {
        System.out.println("\n=== 开始测试 FriendsSQL ===");

        String friendId = "test-friend-123";
        testAddFriend(friendId);
        testUpdateNetwork(friendId);
        testToggleBlock(friendId);
        testUpdateNickname1(friendId);

        System.out.println("=== FriendsSQL 测试完成 ===");
    }

    private static void testAddFriend(String friendId) {
        System.out.print("测试 addFriend...");
        try {
            FriendsSQL.addFriend(DB_PATH, friendId, "192.168.1.100", 8080);

            try (ResultSet rs = querySQL("SELECT * FROM Friends WHERE friend_id = ?", friendId)) {
                assertTrue(rs.next(), "应存在好友记录");
                assertEquals("192.168.1.100", rs.getString("ip_address"), "IP地址不匹配");
                assertEquals(8080, rs.getInt("port"), "端口号不匹配");
                System.out.println("通过");
            }
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    private static void testUpdateNetwork(String friendId) {
        System.out.print("测试 updateNetwork...");
        try {
            FriendsSQL.updateNetwork(DB_PATH, friendId, "10.0.0.1", 9090);

            try (ResultSet rs = querySQL("SELECT * FROM Friends WHERE friend_id = ?", friendId)) {
                assertTrue(rs.next(), "记录应存在");
                assertEquals("10.0.0.1", rs.getString("ip_address"), "更新后IP不匹配");
                assertEquals(9090, rs.getInt("port"), "更新后端口不匹配");
                System.out.println("通过");
            }
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    private static void testToggleBlock(String friendId) {
        System.out.print("测试 toggleBlock...");
        try {
            FriendsSQL.toggleBlock(DB_PATH, friendId, true);

            try (ResultSet rs = querySQL("SELECT is_blocked FROM Friends WHERE friend_id = ?", friendId)) {
                assertTrue(rs.next(), "记录应存在");
                assertTrue(rs.getBoolean("is_blocked"), "拉黑状态未更新");
                System.out.println("通过");
            }
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    private static void testUpdateNickname1(String friendId) {
        System.out.print("测试 updateNickname...");
        try {
            FriendsSQL.updateNickname(DB_PATH, friendId, "测试好友");

            try (ResultSet rs = querySQL("SELECT nickname FROM Friends WHERE friend_id = ?", friendId)) {
                assertTrue(rs.next(), "记录应存在");
                assertEquals("测试好友", rs.getString("nickname"), "昵称未更新");
                System.out.println("通过");
            }
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    // 以下是工具方法
    private static void executeSQL(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static ResultSet querySQL(String sql, Object... params) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery();
    }

    // 简单断言方法
    private static void assertNotNull(Object obj, String message) {
        if (obj == null) throw new AssertionError(message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " (预期: " + expected + ", 实际: " + actual + ")");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    // 在 test 类中添加以下测试方法
    private static void testChatHistorySQL() {
        System.out.println("\n=== 开始测试 ChatHistorySQL ===");


        testInsertAndReadMessages(50); // 50条单聊 + 50条群聊 = 100条

        System.out.println("=== ChatHistorySQL 测试完成 ===");
    }


    private static void testInsertAndReadMessages(int countPerType) {
        System.out.printf("测试插入和读取 %d 条随机数据...", countPerType * 2);

        Random random = new Random();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            // 插入测试数据
            for (int i = 0; i < countPerType; i++) {
                // 单聊消息
                ChatHistorySQL.insertChatMessage(conn, false, null,
                        "peer" + random.nextInt(10),
                        random.nextBoolean() ? "text" : "file",
                        "Message " + UUID.randomUUID(),
                        random.nextBoolean(),
                        "192.168." + random.nextInt(256) + "." + random.nextInt(256),
                        10000 + random.nextInt(10000));

                // 群聊消息
                ChatHistorySQL.insertChatMessage(conn, true, "group" + random.nextInt(5),
                        "user" + random.nextInt(20),
                        random.nextBoolean() ? "text" : "image",
                        "GroupMsg " + UUID.randomUUID(),
                        random.nextBoolean(),
                        "10.0." + random.nextInt(256) + "." + random.nextInt(256),
                        20000 + random.nextInt(10000));
            }

            // 验证数据完整性
            try (Statement stmt = conn.createStatement()) {
                // 检查单聊记录数
                ResultSet singleRs = stmt.executeQuery("SELECT COUNT(*) FROM SingleChatHistory");
                assertTrue(singleRs.getInt(1) >= countPerType, "单聊记录数不足");

                // 检查群聊记录数
                ResultSet groupRs = stmt.executeQuery("SELECT COUNT(*) FROM GroupMessages");
                assertTrue(groupRs.getInt(1) >= countPerType, "群聊记录数不足");

                System.out.println("通过");
            }
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 在 test 类中添加以下测试方法
    private static void testGroupSQL() {
        System.out.println("\n=== 开始测试 GroupSQL ===");

        String groupId = "test-group-"+UUID.randomUUID().toString().substring(0,8);
        String ownerId = UserSQL.initializeUser(DB_PATH, "192.168.0.100", 8888);

        testCreateGroup(groupId, ownerId);
        testAddRemoveMember(groupId);
        testUpdateMemberInfo(groupId, ownerId);
        testGetMethods(groupId);
        testDeleteGroup(groupId);

        System.out.println("=== GroupSQL 测试完成 ===");
    }

    private static void testCreateGroup(String groupId, String ownerId) {
        System.out.print("测试 createGroup...");
        boolean result = GroupSQL.createGroup(DB_PATH, groupId, "测试群组", ownerId, "192.168.0.100", 8888);

        try {
            assertTrue(result, "创建群组应返回true");

            // 验证群组基础信息
            try (ResultSet rs = querySQL("SELECT * FROM Groups WHERE group_id = ?", groupId)) {
                assertTrue(rs.next(), "群组记录应存在");
                assertEquals("测试群组", rs.getString("group_name"), "群组名称不匹配");
                assertEquals(ownerId, rs.getString("owner_id"), "群主ID不匹配");
            }

            // 验证初始化设置
            try (ResultSet rs = querySQL("SELECT * FROM GroupSettings WHERE group_id = ?", groupId)) {
                assertTrue(rs.next(), "群设置应存在");
            }

            // 验证群主在成员表
            try (ResultSet rs = querySQL("SELECT role FROM GroupMembers WHERE group_id = ? AND member_id = ?",
                    groupId, ownerId)) {
                assertTrue(rs.next(), "群主应存在于成员表");
                assertEquals("owner", rs.getString("role"), "角色应为owner");
            }

            System.out.println("通过");
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    private static void testAddRemoveMember(String groupId) {
        System.out.print("测试 addMember/removeMember...");
        String testMember = "test-member-001";

        try {
            // 测试添加成员
            boolean addResult = GroupSQL.addMember(DB_PATH, groupId, testMember, "192.168.1.50", 9999);
            assertTrue(addResult, "添加成员应成功");

            // 验证成员存在
            try (ResultSet rs = querySQL("SELECT * FROM GroupMembers WHERE group_id = ? AND member_id = ?",
                    groupId, testMember)) {
                assertTrue(rs.next(), "成员应存在于成员表");
            }

            // 测试重复添加
            // 修改测试用例中的断言
            boolean duplicateAdd = GroupSQL.addMember(DB_PATH, groupId, testMember, "192.168.1.50", 9999);
            assertFalse(duplicateAdd, "重复添加应返回false");


            // 测试移除成员
            boolean removeResult = GroupSQL.removeMember(DB_PATH, groupId, testMember);
            assertTrue(removeResult, "移除成员应成功");

            // 验证成员不存在
            try (ResultSet rs = querySQL("SELECT * FROM GroupMembers WHERE group_id = ? AND member_id = ?",
                    groupId, testMember)) {
                assertFalse(rs.next(), "成员应已被移除");
            }

            System.out.println("通过");
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    private static void testUpdateMemberInfo(String groupId, String memberId) {
        System.out.print("测试 updateMemberNickname/updateMemberNetwork...");

        try {
            // 测试更新昵称
            boolean nicknameResult = GroupSQL.updateMemberNickname(DB_PATH, groupId, memberId, "群主昵称");
            assertTrue(nicknameResult, "更新昵称应成功");

            try (ResultSet rs = querySQL("SELECT nickname_in_group FROM GroupMembers WHERE group_id = ? AND member_id = ?",
                    groupId, memberId)) {
                assertTrue(rs.next(), "成员记录应存在");
                assertEquals("群主昵称", rs.getString("nickname_in_group"), "昵称不匹配");
            }

            // 测试更新网络信息
            boolean networkResult = GroupSQL.updateMemberNetwork(DB_PATH, groupId, memberId, "10.0.0.1", 7777);
            assertTrue(networkResult, "更新网络信息应成功");

            try (ResultSet rs = querySQL("SELECT ip_address, port FROM GroupMembers WHERE group_id = ? AND member_id = ?",
                    groupId, memberId)) {
                assertTrue(rs.next(), "成员记录应存在");
                assertEquals("10.0.0.1", rs.getString("ip_address"), "IP地址不匹配");
                assertEquals(7777, rs.getInt("port"), "端口不匹配");
            }

            System.out.println("通过");
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    private static void testGetMethods(String groupId) {
        System.out.print("测试 getAllGroupIds/getGroupMembersNetwork...");

        try {
            // 测试获取所有群组ID
            List<String> groupIds = GroupSQL.getAllGroupIds(DB_PATH);
            assertTrue(groupIds.contains(groupId), "群组列表应包含测试群组");

            // 测试获取成员网络信息
            Map<String, Map<String, Object>> members = GroupSQL.getGroupMembersNetwork(DB_PATH, groupId);
            assertTrue(members.size() >= 1, "至少应存在群主");

            // 测试获取群名称
            String groupName = GroupSQL.getGroupName(DB_PATH, groupId);
            assertEquals("测试群组", groupName, "群组名称不匹配");

            System.out.println("通过");
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    private static void testDeleteGroup(String groupId) {
        System.out.print("测试 deleteGroup...");
        boolean result = GroupSQL.deleteGroup(DB_PATH, groupId);

        try {
            assertTrue(result, "删除群组应成功");

            // 验证关联数据删除
            try (ResultSet rs1 = querySQL("SELECT * FROM Groups WHERE group_id = ?", groupId)) {
                assertFalse(rs1.next(), "群组记录应已删除");
            }
            try (ResultSet rs2 = querySQL("SELECT * FROM GroupSettings WHERE group_id = ?", groupId)) {
                assertFalse(rs2.next(), "群设置应已删除");
            }
            try (ResultSet rs3 = querySQL("SELECT * FROM GroupMembers WHERE group_id = ?", groupId)) {
                assertFalse(rs3.next(), "群成员应已删除");
            }

            System.out.println("通过");
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }
    }

    // 在 main 方法中添加测试调用


    // 添加缺失的断言方法
    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError(message);
    }



}

