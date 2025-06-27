package data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestNotSent {
    private static final Map<String, List<PendingRequest>> pendingRequests = new ConcurrentHashMap<>();

    // 封装待发送请求
    public static class PendingRequest {
        private final String requestUrl;
        private final String jsonBody;

        public PendingRequest(String requestUrl, String jsonBody) {
            this.requestUrl = requestUrl;
            this.jsonBody = jsonBody;
        }

        public String getRequestUrl() {
            return requestUrl;
        }

        public String getJsonBody() {
            return jsonBody;
        }
    }

    public static synchronized void loadAllRequests(Map<String, List<PendingRequest>> requests) {
        pendingRequests.clear();
        pendingRequests.putAll(requests);
    }

    public static synchronized Map<String, List<PendingRequest>> getAllPendingRequests() {
        Map<String, List<PendingRequest>> copy = new ConcurrentHashMap<>();
        for (Map.Entry<String, List<PendingRequest>> entry : pendingRequests.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * 添加离线请求
     * @param userId 目标用户ID
     * @param requestUrl 请求URL（如"/createLink"）
     * @param jsonBody 请求的JSON内容
     */
    public static synchronized void addRequest(String userId, String requestUrl, String jsonBody) {
        pendingRequests.computeIfAbsent(userId, k ->
                Collections.synchronizedList(new ArrayList<>())
        ).add(new PendingRequest(requestUrl, jsonBody));
    }

    /**
     * 获取并移除用户的所有待处理请求
     * @param userId 用户ID
     * @return 待处理请求列表
     */
    public static synchronized List<PendingRequest> getAndRemoveRequests(String userId) {
        List<PendingRequest> requests = pendingRequests.remove(userId);
        return requests != null ? requests : new ArrayList<>();
    }

    /**
     * 检查用户是否有待处理请求
     * @param userId 用户ID
     * @return 存在待处理请求返回true，否则false
     */
    public static synchronized boolean hasPendingRequests(String userId) {
        return pendingRequests.containsKey(userId) && !pendingRequests.get(userId).isEmpty();
    }
}