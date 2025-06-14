package clients;

public class JsonPayloadBuilder {

    // 点对点消息
    public static String buildPeerMessage(boolean isGroup, String message, String ip, int port, String peerID, String groupId) {
        return String.format(
                "{\"isGroup\":%b,\"message\":\"%s\",\"ip\":\"%s\",\"port\":%d,\"peerID\":\"%s\",\"groupId\":\"%s\"}",
                isGroup, escapeJson(message), escapeJson(ip), port, escapeJson(peerID), escapeJson(groupId)
        );
    }

    // 好友请求
    public static String buildFriendRequest(String friendId, String ip, int port) {
        return String.format(
                "{\"friend_id\":\"%s\",\"ip\":\"%s\",\"port\":%d}",
                escapeJson(friendId), escapeJson(ip), port
        );
    }

    // 创建群组
    public static String buildCreateGroup(String groupId, String groupName, String ports, String ips, String userIds, String nickname) {
        return String.format(
                "{\"groupId\":\"%s\",\"groupName\":\"%s\",\"port\":\"%s\",\"ip\":\"%s\",\"userID\":\"%s\",\"nickname\":\"%s\"}",
                escapeJson(groupId), escapeJson(groupName), escapeJson(ports), escapeJson(ips), escapeJson(userIds), escapeJson(nickname)
        );
    }

    // 群组删除操作
    public static String buildSimpleGroupOp(String groupId) {
        return String.format("{\"groupId\":\"%s\"}", escapeJson(groupId));
    }

    // 更新群组名称
    public static String buildUpdateGroupName(String groupId, String groupName) {
        return String.format(
                "{\"groupId\":\"%s\",\"GroupName\":\"%s\"}",
                escapeJson(groupId), escapeJson(groupName)
        );
    }

    // 添加群成员
    public static String buildAddGroupMember(String groupId, String memberId, String ip, int port) {
        return String.format(
                "{\"groupId\":\"%s\",\"memberId\":\"%s\",\"ip\":\"%s\",\"port\":%d}",
                escapeJson(groupId), escapeJson(memberId), escapeJson(ip), port
        );
    }

    // 更新成员昵称
    public static String buildUpdateMemberNickname(String groupId, String memberId, String nickname) {
        return String.format(
                "{\"groupId\":\"%s\",\"memberId\":\"%s\",\"nickname\":\"%s\"}",
                escapeJson(groupId), escapeJson(memberId), escapeJson(nickname)
        );
    }

    // 更新好友昵称
    public static String buildUpdateFriendNickname(String friendId, String nickname) {
        return String.format(
                "{\"friendID\":\"%s\",\"nickname\":\"%s\"}",
                escapeJson(friendId), escapeJson(nickname)
        );
    }

    // 处理JSON特殊字符转义
    static String escapeJson(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }
}
