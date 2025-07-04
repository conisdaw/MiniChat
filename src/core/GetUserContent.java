package core;

import data.UserSQL;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public class GetUserContent {
    public static String UserName() {
        return UserSQL.getUserInfo(Config.DB_PATH, UserID()).get("nickname");
    }

    public static String personIP() {
            try {
                return getLocalIP();
            } catch (SocketException e) {
                throw new RuntimeException("Failed to get local IP", e);
            }
    }

    public static int getPort() {
        String userId = UserID();
        if (userId == null) {
            return 5090;
        }
        return UserSQL.getUserPort(Config.DB_PATH, userId);
    }

    public static String UserID() {
        List<String> userIds = UserSQL.getAllUserIds(Config.DB_PATH);
        if (userIds == null || userIds.isEmpty()) {
            return "新用户";
        }
        return userIds.get(0);
    }

    public static int personPort() {
        return Config.PORT;
    }

    private static String getLocalIP() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;

            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.isLoopbackAddress()) continue;

                // 优先返回 IPv4 地址
                String ip = address.getHostAddress();
                if (ip.indexOf(':') == -1) {
                    return ip;
                }
            }
        }
        return null;
    }
}
