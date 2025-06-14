package sever;

import core.Config;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class UserInterfaces {
    private static final int MAX_REQUEST_SIZE = 8192;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024 * 1024; // 10GB
    private ServerSocket serverSocket;
    private volatile boolean running = true; // 添加运行状态标志

    public UserInterfaces(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
        startServer();
    }

    private void startServer() {
        while (running) { // 使用运行状态标志
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            } catch (IOException e) {
                if (!running) {
                    System.out.println("Server stopped intentionally.");
                } else {
                    e.printStackTrace();
                }
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

            String request = new String(buffer, 0, bytesRead);
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

            String requestPath = requestParts[1];
            System.out.println("Request Path: " + requestPath);

            String requestBody = "";
            if (request.contains("\r\n\r\n")) {
                String[] parts = request.split("\r\n\r\n", 2);
                if (parts.length > 1) {
                    requestBody = parts[1].trim();
                }
            }

            // 根据路径分发处理逻辑
            if (requestPath.equals("/chat")) {
                new Chat().handle(requestBody, out, Config.DB_PATH);
            } else if (requestPath.equals("/file")) {
                String savePath = ServiceUtils.extractStringField(requestBody, "savePath");
                if (savePath.isEmpty()) {
                    ServiceUtils.sendFileErrorResponse(out, 400, "缺少文件保存路径参数");
                    return;
                }
                new FileTransfer().handle(in, out, savePath, MAX_FILE_SIZE, bytesRead - (request.indexOf("\r\n\r\n") + 4));
            } else if (requestPath.equals("/createLink")) {
                new CreateLink().handle(requestBody, out, Config.DB_PATH);
            } else if (requestPath.equals("/friend/nickname")) {
                new UpdataFriendsNickname().handle(requestBody, out, Config.DB_PATH);
            } else if (requestPath.equals("/group/creation")) {
                new CreationGroup().handle(requestBody, out, Config.DB_PATH);
            } else if (requestPath.equals("/group/dismiss")) {
                new DismissGroup().handle(requestBody, out, Config.DB_PATH);
            } else if (requestPath.equals("/group/name")) {
                new UpdateGroupName().handle(requestBody, out, Config.DB_PATH);
            } else if (requestPath.equals("/group/update/network")) {
                new UpdateGroupMembers().handle(requestBody, out, Config.DB_PATH);
            } else if (requestPath.equals("/group/update/nickname")) {
                new UpdateGroupNickname().handle(requestBody, out, Config.DB_PATH);
            } else {
                ServiceUtils.sendErrorResponse(out, 404, "Not Found");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 服务器停止方法
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}
