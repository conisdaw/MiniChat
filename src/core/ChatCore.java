package core;

import data.ChatHistorySQL;
import data.ChatMessage;
import data.FriendsSQL;
import data.GroupSQL;
import clients.Chat;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static data.GroupSQL.getGroupMemberNicknames;

public class ChatCore {
    public static void main(String[] args) throws SQLException {
        System.out.println(getChatHistory("test-friend-123", false).toString());
    }

    public static List<ChatMessage> getChatHistory(String friendId, boolean isGroup) {
        try {
            return ChatHistorySQL.readChatHistory(Config.DB_PATH, isGroup, friendId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get chat history", e);
        }
    }

    public static String setChatHistory(boolean isGrup, String grupId, String friendId, String message) throws SQLException {
        if (isGrup) {
            Map<String, Map<String, String>> groupNetwork = GroupSQL.getGroupMembersNetwork(Config.DB_PATH, grupId);
            ChatHistorySQL.insertChatMessage(Config.DB_PATH, true, grupId, friendId, "text", message,true, Config.IP,Config.PORT);
            for (Map<String, String> netWork : groupNetwork.values()) {
                if (Config.IP.equals(netWork.get("ip"))) continue;
                Chat.handle(true, message, grupId,netWork.get("ip"), Integer.parseInt(netWork.get("port")));
            }
            return message;
        } else {
            try {
                Map<String, String> friendByID = FriendsSQL.getFriendByID(Config.DB_PATH, friendId);
                ChatHistorySQL.insertChatMessage(Config.DB_PATH, false, null, friendId, "text", message, true, Config.IP, Config.PORT);
                Chat.handle(false, message, null,friendByID.get("ip_address"), Integer.parseInt(friendByID.get("port")));
                return message;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<Map<String, String>> getFriendList() throws SQLException {
        return FriendsSQL.getAllFriendIDs(Config.DB_PATH);
    }

    public static List<Map<String, String>> getBlockedFriendList() throws SQLException {
        return FriendsSQL.getAllBlockedFriendIDs(Config.DB_PATH);
    }

    public static List<Map<String, String>> getGroupList() {
        return GroupSQL.getAllGroupInfo(Config.DB_PATH);
    }

    public static Map<String, String> getGroupMemberName(String groupId) {
        return getGroupMemberNicknames(Config.DB_PATH, groupId);
    }

}
