package sever;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class FileSender {
    public static void handle(String localFilePath, String serverRelativePath, String userIp, int userPort) throws IOException {
        File file = new File(localFilePath);
        if (!file.exists()) {
            System.err.println("文件不存在: " + localFilePath);
            return;
        }

        // 使用标准JSON格式
        JSONObject json = new JSONObject();
        json.put("savePath", serverRelativePath);
        String jsonPayload = json.toString();
        int jsonLength = jsonPayload.getBytes("UTF-8").length;

        try (Socket socket = new Socket(userIp, userPort);
             OutputStream out = socket.getOutputStream();
             InputStream fileIn = new FileInputStream(file)) {

            // 添加JSON长度头信息
            String header = "POST /file HTTP/1.1\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Json-Length: " + jsonLength + "\r\n" +
                    "Content-Length: " + (jsonLength + file.length()) + "\r\n\r\n";

            // 发送请求头
            out.write(header.getBytes("UTF-8"));
            out.write(jsonPayload.getBytes("UTF-8"));
            out.flush();

            // 发送文件内容
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            socket.shutdownOutput();

            // 获取响应
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String response = reader.readLine();
            System.out.println("服务器响应: " + response);
        }
    }
}