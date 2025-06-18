package data;

import java.util.HashSet;
import java.util.Set;

public class ListIsUpdated {
    private static final Set<Byte> friendList = new HashSet<>();
    private static final Set<Byte> groupList = new HashSet<>();
    private static final byte aByte = 1;

    // 检查是否有新的私人天列表
    public static boolean friendIsNull () {
        if (friendList.isEmpty()) {
            return false;
        } else {
            friendList.clear();
            return true;
        }
    }

    // 检查是否有新的群聊天列表
    public static boolean groupIsNull () {
        if (groupList.isEmpty()) {
            return false;
        } else {
            groupList.clear();
            return true;
        }
    }

    // 让私人聊天列表不为空
    public static void friendNotNull() {
        friendList.add(aByte);
    }

    // 让群组聊天列表不为空
    public static void groupNotNull() {
        groupList.add(aByte);
    }

}
