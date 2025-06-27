package data;

import java.util.*;

public class MemberList {
    private static final Map<String, Boolean> MemberList = new HashMap<>();
    private static final Set<String> OnlineList = new HashSet<>();

    static  {
        List<String> temp = UserSQL.getAllUsersId();
        for (String a : temp) MemberList.put(a, false);
    }

    public static Set<String> getOnlineList() {
        return OnlineList;
    }

    public static void online(String userId) {
        OnlineList.add(userId);
        MemberList.put(userId, true);
    }

    public static void offline(String userId) {
        OnlineList.remove(userId);
        MemberList.put(userId, false);
    }

    public static boolean isOnline(String userId) {
        return MemberList.getOrDefault(userId, false);
    }
}
