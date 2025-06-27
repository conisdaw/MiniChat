package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Disable {
    private static final List<String> disableWords = new ArrayList<>();
    private static final Map<String, Long> disableUser = new HashMap<>();

    public static boolean isDisableUser(String userId) {
        if(disableUser.containsKey(userId)){
            if ((System.currentTimeMillis() - disableUser.get(userId)) > 300000){
                removeDisableUser(userId);
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public static void loadWords(List<String> words) {
        disableWords.clear();
        disableWords.addAll(words);
    }

    public static List<String> getDisableWords() {
        return disableWords;
    }

    public static boolean addDisableWord(String word) {
        if (disableWords.contains(word)) {
            return false;
        }
        return disableWords.add(word);
    }

    public static boolean removeDisableWord (String word) {
        return disableWords.remove(word);
    }

    public static boolean isDisableMessage(String userId, String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        for (String word : disableWords) {
            if (message.contains(word)) {
                addDisableUser(userId);
                return true;
            }
        }
        return false;
    }

    // 辅助方法

    // 将使用违禁词的用户加入限制
    private static void addDisableUser(String userId) {
        disableUser.put(userId, System.currentTimeMillis());
    }

    private static void removeDisableUser(String userId) {
        disableUser.remove(userId);
    }
}
