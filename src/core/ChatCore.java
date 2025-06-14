package core;

import clients.*;
import data.*;

import java.io.IOException;
import java.net.SocketException;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ChatCore {
    /**
     * 当isGroup为true时，friendId做为group_id使用。
     * List列表中的内容格式为data包下ChatMessage类中的格式。
     * @param friendId
     * @param isGroup
     * @return 返回一个List，内容为群聊或好友的聊天记录。
     */
    public static List<ChatMessage> getChatHistory(String friendId, boolean isGroup) {
        try {
            return ChatHistorySQL.readChatHistory(Config.DB_PATH, isGroup, friendId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get chat history", e);
        }
    }

    /**
     * 进行文本发送，当isGrup为false时，grupId可以为空。
     * 当isGrup为true时，friendId做member_id使用。
     * @param isGrup
     * @param grupId
     * @param friendId
     * @param message
     * @return message
     */
    public static String setChatHistory(boolean isGrup, String grupId, String friendId, String message) {
        if (isGrup) {
            Map<String, Map<String, String>> groupNetwork = GroupSQL.getGroupMembersNetwork(Config.DB_PATH, grupId);
            try {
                ChatHistorySQL.insertChatMessage(Config.DB_PATH, true, grupId, friendId, "text", message,true, Config.IP,Config.PORT);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            for (Map<String, String> netWork : groupNetwork.values()) {
                if (Config.IP.equals(netWork.get("ip"))) continue;
                Chat.handle(true, "text", message, grupId,netWork.get("ip"), Integer.parseInt(netWork.get("port")));
            }
            return message;
        } else {
            try {
                Map<String, String> friendByID = FriendsSQL.getFriendByID(Config.DB_PATH, friendId);
                ChatHistorySQL.insertChatMessage(Config.DB_PATH, false, null, friendId, "text", message, true, Config.IP, Config.PORT);
                Chat.handle(false, "text", message, null,friendByID.get("ip_address"), Integer.parseInt(friendByID.get("port")));
                return message;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @return 返回一个Map，K为friendId，V为好友名
     */
    public static List<Map<String, String>> getFriendList() {
        try {
            return FriendsSQL.getAllFriendIDs(Config.DB_PATH);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return 返回一个Map，K为被拉黑的friendId，V为被拉黑的好友名
     */
    public static List<Map<String, String>> getBlockedFriendList() {
        try {
            return FriendsSQL.getAllBlockedFriendIDs(Config.DB_PATH);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return 返回一个Map，K为groupId，V为群名
     */
    public static List<Map<String, String>> getGroupList() {
        return GroupSQL.getAllGroupInfo(Config.DB_PATH);
    }

    /**
     * @param groupId
     * @return 返回一个Map，K为member_id,即friendId，V为群昵称
     */
    public static Map<String, String> getGroupMemberName(String groupId) {
        return GroupSQL.getGroupMemberNicknames(Config.DB_PATH, groupId);
    }

    /**
     * 进行图片发送，当isGrup为false时，grupId可以为空。
     *当isGrup为true时，friendId做member_id使用。
     * @param isGrup
     * @param grupId
     * @param friendId
     * @param input
     * @return 文件的路径
     *
     */
    public static String setImage(boolean isGrup,String grupId, String friendId, String input) {
        String fileId = LocalDate.now().toString();
        String imagePath = grupId + "/" + friendId + "/" + fileId;
        String imageName = copyFile(input, Config.FILE_BASE_DIR + imagePath);
        String finalPath = imagePath + imageName;
        if (isGrup) {
            Map<String, Map<String, String>> groupNetwork = GroupSQL.getGroupMembersNetwork(Config.DB_PATH, grupId);
            try {
                ChatHistorySQL.insertChatMessage(Config.DB_PATH, true,grupId, friendId, "image", finalPath,true, Config.IP, Config.PORT);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            for (Map<String, String> netWork : groupNetwork.values()) {
                if (Config.IP.equals(netWork.get("ip"))) continue;
                Chat.handle(true, "image",finalPath, grupId,netWork.get("ip"), Integer.parseInt(netWork.get("port")));
                try {
                    FileSender.handle(netWork.get("ip"), Integer.parseInt(netWork.get("port")), input, finalPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return finalPath;
        } else {
            try {
                Map<String, String> friendByID = FriendsSQL.getFriendByID(Config.DB_PATH, friendId);
                ChatHistorySQL.insertChatMessage(Config.DB_PATH, false, null, friendId, "image", finalPath, true, Config.IP, Config.PORT);
                Chat.handle(false, "image", finalPath, null,friendByID.get("ip_address"), Integer.parseInt(friendByID.get("port")));
                FileSender.handle(friendByID.get("ip_address"), Integer.parseInt(friendByID.get("port")), input, finalPath);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
            return finalPath;
        }
    }

    /**
     * 进行文件发送，当isGrup为false时，grupId可以为空。
     *当isGrup为true时，friendId做member_id使用。
     * @param isGrup
     * @param grupId
     * @param friendId
     * @param input
     * @return 文件的路径
     *
     */
    public static String setFile(boolean isGrup,String grupId, String friendId, String input) {
        String fileId = LocalDate.now().toString();
        String filePath = grupId + "/" + friendId + "/" + fileId;
        String fileName = copyFile(input, Config.FILE_BASE_DIR + filePath);
        String finalPath = filePath + fileName;
        if (isGrup) {
            Map<String, Map<String, String>> groupNetwork = GroupSQL.getGroupMembersNetwork(Config.DB_PATH, grupId);
            try {
                ChatHistorySQL.insertChatMessage(Config.DB_PATH, true,grupId, friendId, "file", finalPath,true, Config.IP, Config.PORT);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            for (Map<String, String> netWork : groupNetwork.values()) {
                if (Config.IP.equals(netWork.get("ip"))) continue;
                Chat.handle(true, "file",finalPath, grupId,netWork.get("ip"), Integer.parseInt(netWork.get("port")));
                try {
                    FileSender.handle(netWork.get("ip"), Integer.parseInt(netWork.get("port")), input, finalPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return finalPath;
        } else {
            try {
                Map<String, String> friendByID = FriendsSQL.getFriendByID(Config.DB_PATH, friendId);
                ChatHistorySQL.insertChatMessage(Config.DB_PATH, false, null, friendId, "file", finalPath, true, Config.IP, Config.PORT);
                Chat.handle(false, "file", finalPath, null,friendByID.get("ip_address"), Integer.parseInt(friendByID.get("port")));
                FileSender.handle(friendByID.get("ip_address"), Integer.parseInt(friendByID.get("port")), input, finalPath);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
            return finalPath;
        }
    }

    /**
     * 完成好友添加
     *输入示例: 192.123.543.123:8080
     * @param network
     * @return
     * 成功时返回true,否则返回false
     */
    public static boolean addFriend(String network) {
        try {
            String back = CreateLink.handle(network.split(":")[0], Integer.parseInt(network.split(":")[1]));
            String[] temp = back.split(",");
            boolean isTrue = isValidFormat(temp[0]);
            try {
                if (isTrue) {
                    FriendsSQL.addFriend(Config.DB_PATH, temp[0], network.split(":")[0], Integer.parseInt(network.split(":")[1]));
                    FriendsSQL.updateNickname(Config.DB_PATH, temp[0], temp[1]);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return isTrue;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 方法在调用后会将friendId所对应的好友的拉黑状态进行反转。
     * @param friendId
     */
    public static void blockedFriend(String friendId) {
        try {
            FriendsSQL.toggleBlockStatus(Config.DB_PATH, friendId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param friendId
     * 方法在调用后会将friendId所对应的好友删除。
     */
    public static void deleteFriend(String friendId) {
        try {
            FriendsSQL.deleteFriend(Config.DB_PATH, friendId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 重新设置网络。
     * 输入格式示例: 182.212.123.3:8080。
     * @param network
     * @return 返回输入内容。
     */
    public static String resetNetwork(String network) {
        Config.IP = network.split(":")[0];
        Config.PORT = SetUserContent.setPort(Integer.parseInt(network.split(":")[1]));
        try {
            List<Map<String, String>> friends = FriendsSQL.getAllFriendIPsAndPorts(Config.DB_PATH);
            for (Map<String, String> friend : friends) UpdataFriendsNetwork.handle(Config.USER_ID, Config.IP, Config.PORT, friend.get("ip"), Integer.parseInt(friend.get("port")));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return network;
    }

    /**
     * 删除群指定Id的群
     * @param groupsId
     * @return 删除失败时返回false，成功时返回true
     */
    public static boolean deleteGroups(String groupsId) {
        boolean isTrue = false;
        List<String[]> networks = GroupSQL.getGroupMemberNetworks(Config.DB_PATH, groupsId);
        for (String[] net : networks) isTrue = DismissGroup.handle(groupsId, net[0], Integer.parseInt(net[1]));
        return isTrue;
    }

    /**
     * 更新自己的昵称
     * @param nickname
     */
    public static void updataNickname(String nickname) {
        try {
            List<Map<String, String>> friends = FriendsSQL.getAllFriendIPsAndPorts(Config.DB_PATH);
            for (Map<String, String> friend : friends) UpdataFriendsNickname.handle(Config.USER_ID, nickname, friend.get("ip"), Integer.parseInt(friend.get("port")));
            UserSQL.updateNickname(Config.DB_PATH, Config.USER_ID, nickname);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将friendId对应的备注改为remark
     * @param remark
     * @param friendId
     */
    public static void UpdataFriendsRemark (String remark, String friendId) {
        try {
            FriendsSQL.updateRemark(Config.DB_PATH, friendId, remark);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将groupId群组中的memberId成员的昵称改为nickname
     * @param groupId
     * @param memberId
     * @param nickname
     */
    public static void updateGroupNickname (String groupId,String memberId,String nickname) {
        List<String[]> networks = GroupSQL.getGroupMemberNetworks(Config.DB_PATH, groupId);
        for (String[] net : networks) UpdateGroupNickname.handle(groupId, memberId, nickname, net[0], Integer.parseInt(net[1]));
    }

    /**
     * 将groupId的群组名改为groupName
     *
     * @param groupId
     * @param groupName
     */
    public static void updateGroupName (String groupId,String groupName) {
        List<String[]> networks = GroupSQL.getGroupMemberNetworks(Config.DB_PATH, groupId);
        for (String[] net : networks) UpdateGroupName.handle(groupId, groupName, net[0], Integer.parseInt(net[1]));
    }

    /**
     * 该方法用于创建新群组其中
     *
     * @param groupName 群名
     * @param nicknames 成员名的集合。格式示例: 你,好,世,界
     * @param userIds 成员ID的集合。格式示例: userId1,userId2,userId3,userId4
     * @param ips 成员IP的集合。格式示例: 192.168.1.1,192.168.1.2,192.168.1.3,192.168.1.4
     * @param ports 成员端口的集合。格式示例: 9090,1231,3454,6433
     */

    public static void creationGroup (String groupName, String nicknames, String userIds, String ips, String ports) {
        try {
            CreationGroup.handle(groupName, nicknames, userIds, ips, ports);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 为群组添加一名成员
     * @param groupId 要添加的群
     * @param memberId 用户的ID
     * @param nickname 用户昵称
     * @param userIP 用户IP
     * @param userPort 用户端口
     */
    public static void updateGroupMembers (String groupId,String memberId, String nickname,String userIP, int userPort) {
        List<String[]> networks = GroupSQL.getGroupMemberNetworks(Config.DB_PATH, groupId);
        for (String[] net : networks) {
            UpdateGroupMembers.handle(groupId, memberId, userIP, userPort, net[0], Integer.parseInt(net[1]));
            UpdateGroupNickname.handle(groupId, memberId, nickname, net[0], Integer.parseInt(net[1]));
        }
    }



    // 辅助方法
    public static boolean isValidFormat(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        if (input.length() != 44) {
            return false;
        }
        String uuidPart = input.substring(0, 36);
        String numberPart = input.substring(36);

        if (!numberPart.matches("\\d{8}")) {
            return false;
        }

        String[] segments = uuidPart.split("-");

        if (segments.length != 5) {
            return false;
        }

        int[] expectedLengths = {8, 4, 4, 4, 12};
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].length() != expectedLengths[i]) {
                return false;
            }
        }

        String hexString = String.join("", segments);
        return hexString.matches("[0-9a-fA-F]{32}");
    }

    private static String copyFile(String input, String output) {
        try {
            Path source = Paths.get(input);
            Path targetDir = Paths.get(output);
            if (!Files.exists(source)) {
                throw new IOException("源文件不存在: " + source);
            }
            if (!Files.isRegularFile(source)) {
                throw new IOException("源路径不是文件: " + source);
            }
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String originalName = source.getFileName().toString();
            String extension = getFileExtension(originalName);
            String secureName = generateSecureFileName(extension);
            Path destination = targetDir.resolve(secureName);

            // 复制文件
            Files.copy(
                    source,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
            );

            return secureName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateSecureFileName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        long randomNum = ThreadLocalRandom.current().nextLong(0, 281_474_976_710_656L);
        String randomHex = String.format("%012x", randomNum);
        return uuid + randomHex + (extension.isEmpty() ? "" : "." + extension);
    }

    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

}
