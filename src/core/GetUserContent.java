package core;

import data.UserSQL;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class GetUserContent {
    public static String personIP() throws SocketException {
        return getLocalIP();
    }

    public static String UserID() {
        return UserSQL.getAllUserIds(Config.DB_PATH).getFirst();
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
