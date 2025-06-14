package clients;

import core.Config;
import java.io.*;
import java.net.Socket;

public class FileSender {
    public static void sendFile(String serverHost, int serverPort, String localFilePath, String serverRelativePath) throws IOException {
        File file = new File(localFilePath);
        if (!file.exists()) {
            System.err.println("文件不存在: " + localFilePath);
            return;
        }

        // 拼接服务器完整路径
        String serverSavePath = Config.FILE_BASE_DIR + File.separator + serverRelativePath;
        String jsonPayload = String.format(
            "{\"savePath\":\"%s\"}",
            JsonPayloadBuilder.escapeJson(serverSavePath)
        );

        try (Socket socket = new Socket(serverHost, serverPort);
             OutputStream out = socket.getOutputStream();
             InputStream fileIn = new FileInputStream(file)) {

            // 构建请求头
            String header = "POST /file HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + (jsonPayload.length() + file.length()) + "\r\n\r\n";

            // 发送请求头+JSON
            out.write(header.getBytes());
            out.write(jsonPayload.getBytes());

            // 发送文件内容
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            // 获取响应
            InputStream in = socket.getInputStream();
            byte[] resBuffer = new byte[4096];
            int resBytes = in.read(resBuffer);
            String response = new String(resBuffer, 0, resBytes);
            System.out.println("服务器响应: " + response);
        }
    }
}
