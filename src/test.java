import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class test {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080; // 与服务端端口保持一致

    public static void main(String[] args) {
        testChat();
        testCreateLink();
        testCreationGroup();
        testDismissGroup();
        testUpdateGroupNetwork();
        testUpdateGroupNickname();
        testUpdateGroupName();
        testUpdateFriendsNickname();
    }

    /**
     * 测试对话
     */
    private static void testChat() {
        String jsonPayload = "{\"isGroup\":false,\"message\":\"你好世界\",\"ip\":\"你好世界\",\"port\":7070,\"peerID\":\"你好世界\"}";
        String httpRequest = "POST /chat HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonPayload.length() + "\r\n" +
                "\r\n" +
                jsonPayload;

        sendTestRequest(httpRequest, "正常请求测试");
    }

    /**
     * 测试连接创建
     */
    private static void testCreateLink() {
        String jsonPayload = "{\"friend_id\":\"tes7tyu\",\"ip\":\"测试用3户2\",\"port\":7000}";
        String httpRequest = "POST /createLink HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonPayload.length() + "\r\n" +
                "\r\n" +
                jsonPayload;

        sendTestRequest(httpRequest, "正常请求测试");
    }

    /**
     * 测试群创建
     */
    private static void testCreationGroup() {
        String jsonPayload = "{\"groupId\":\"tefdsds1sdar\",\"groupName\":\"测试用3户2\",\"port\":\"7000,9090,9090,9090,23214\",\"ip\":\"7000,9000,9000,9000,2198\",\"userID\":\"你,好,世,界,testuser\"}";
        String httpRequest = "POST /group/creation HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonPayload.length() + "\r\n" +
                "\r\n" +
                jsonPayload;

        sendTestRequest(httpRequest, "正常请求测试");
    }

    /**
     * 测试移除群组
     */
    private static void testDismissGroup() {
        String jsonPayload = "{\"groupId\":\"tefdsds1r\"}";
        String httpRequest = "POST /group/dismiss HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonPayload.length() + "\r\n" +
                "\r\n" +
                jsonPayload;

        sendTestRequest(httpRequest, "正常请求测试");
    }

    /**
     * 测试群名更新
     */
    private static void testUpdateGroupName() {
        String jsonPayload = "{\"groupId\":\"tefdsds1sdar\",\"GroupName\":\"已然离世的理想之城\"}";
        String httpRequest = "POST /group/name HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonPayload.length() + "\r\n" +
                "\r\n" +
                jsonPayload;

        sendTestRequest(httpRequest, "正常请求测试");
    }

    /**
     * 测试更新网络
     */
    private static void testUpdateGroupNetwork() {
        String jsonPayload = "{\"groupId\":\"tes7t11r\",\"memberId\":\"你\",\"port\":8700,\"ip\":\"7000oi0\"}";
        String httpRequest = "POST /group/update/network HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonPayload.length() + "\r\n" +
                "\r\n" +
                jsonPayload;

        sendTestRequest(httpRequest, "正常请求测试");
    }

    /**
     * 测试群昵称更新
     */
    private static void testUpdateGroupNickname() {
        String jsonPayload = "{\"groupId\":\"tes7t11r\",\"memberId\":\"你\",\"nickname\":\"你好世界\"}";
        String httpRequest = "POST /group/update/nickname HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonPayload.length() + "\r\n" +
                "\r\n" +
                jsonPayload;

        sendTestRequest(httpRequest, "正常请求测试");
    }

    /**
     * 测试好友昵称更新
     */
    private static void testUpdateFriendsNickname() {
        String jsonPayload = "{\"friendID\":\"tes7tyu\",\"nickname\":\"你好世界\"}";
        String httpRequest = "POST /friend/nickname HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonPayload.length() + "\r\n" +
                "\r\n" +
                jsonPayload;

        sendTestRequest(httpRequest, "正常请求测试");
    }

    private static void sendTestRequest(String request, String testName) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            System.out.println("\n=== " + testName + " ===");

            // 发送请求
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();

            // 读取响应
            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);
            String response = new String(buffer, 0, bytesRead);
            System.out.println("服务器响应：\n" + response);

        } catch (IOException e) {
            System.err.println("测试失败：" + e.getMessage());
        }
    }
}
