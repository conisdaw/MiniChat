package sever;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServiceInterfaces {
    private static final int MAX_REQUEST_SIZE = 8192;

    public ServiceInterfaces(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream()) {

            // 读取请求头
            byte[] buffer = new byte[MAX_REQUEST_SIZE];
            int bytesRead = in.read(buffer);
            if (bytesRead == -1) return;

            // 解析请求内容
            String request = new String(buffer, 0, bytesRead);

            // 解析请求行获取请求路径
            String[] requestLines = request.split("\r\n");
            if (requestLines.length == 0) {
                ServiceUtils.sendErrorResponse(out, 400, "Invalid Request");
                return;
            }

            String[] requestParts = requestLines[0].split(" ");
            if (requestParts.length < 2) {
                ServiceUtils.sendErrorResponse(out, 400, "Invalid Request Line");
                return;
            }

            String requestPath = requestParts[1]; // 获取请求路径
            System.out.println("Request Path: " + requestPath);

            String requestBody = "";
            if (request.contains("\r\n\r\n")) {
                String[] parts = request.split("\r\n\r\n", 2);
                if (parts.length > 1) {
                    requestBody = parts[1].trim();  // 获取请求体
                }
            }

            // 根据路径分发处理逻辑
            if (requestPath.equals("/chat")) {
                new Chat().handle(requestBody, out);
            } else if (requestPath.equals("/createLink")) {
                new CreateLink().handle(requestBody, out);
            } else {
                ServiceUtils.sendErrorResponse(out, 404, "Not Found");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

