package data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class UnreadMessages {
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<ChatMessage>> friendMessages = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<ChatMessage>> groupMessages = new ConcurrentHashMap<>();

    // 添加好友未读消息
    public static void addFriendMessage(String friendId, ChatMessage message) {
        friendMessages.computeIfAbsent(friendId, k -> new CopyOnWriteArrayList<>()).add(message);
    }

    // 添加群组未读消息
    public static void addGroupMessage(String groupId, ChatMessage message) {
        groupMessages.computeIfAbsent(groupId, k -> new CopyOnWriteArrayList<>()).add(message);
    }

    // 获取并清除好友未读消息
    public static CopyOnWriteArrayList<ChatMessage> getAndClearFriendMessages(String friendId) {
        return friendMessages.remove(friendId);
    }

    // 获取并清除群组未读消息
    public static CopyOnWriteArrayList<ChatMessage> getAndClearGroupMessages(String groupId) {
        if (groupId == null) {
            return null;
        }
        return groupMessages.remove(groupId);
    }

    // 检查是否有未读消息
    public static boolean hasUnread() {
        return !friendMessages.isEmpty() || !groupMessages.isEmpty();
    }

    // 获取所有未读好友ID
    public static String[] getFriendsWithUnread() {
        return friendMessages.keySet().toArray(new String[0]);
    }

    // 获取所有未读群组ID
    public static String[] getGroupsWithUnread() {
        return groupMessages.keySet().toArray(new String[0]);
    }

    // 不清除的获取好友未读消息
    public static CopyOnWriteArrayList<ChatMessage> getFriendMessages(String friendId) {
        return friendMessages.getOrDefault(friendId, new CopyOnWriteArrayList<>());
    }

    // 不清除的获取群组未读消息
    public static CopyOnWriteArrayList<ChatMessage> getGroupMessages(String groupId) {
        return groupMessages.getOrDefault(groupId, new CopyOnWriteArrayList<>());
    }

    // 清除所有未读消息
    public static void clearAll() {
        friendMessages.clear();
        groupMessages.clear();
    }

    // 保存所有未读到数据库
    public static void saveToDatabase() {
        UnreadMessagesSQL.saveUnreadMessages(friendMessages, groupMessages);
    }

    // 从数据库加载未读消息
    public static void loadFromDatabase() {
        UnreadMessagesSQL.loadUnreadMessages(friendMessages, groupMessages);
    }
}