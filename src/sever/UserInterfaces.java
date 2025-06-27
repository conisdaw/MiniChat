package sever;

import core.Config;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class UserInterfaces {
    private static final int MAX_REQUEST_SIZE = 8192;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024 * 1024; // 10GB
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private static final Map<String, Handler> HANDLER_MAP = new HashMap<>();

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

            switch (requestPath) {
                case "/file":
                    handleFileRequest(requestLines, in, out);
                    break;
                default:
                    Handler handler = HANDLER_MAP.get(requestPath);
                    if (handler != null) {
                        handler.handle(requestBody, out);
                    } else {
                        ServiceUtils.sendErrorResponse(out, 404, "Not Found");
                    }
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

    // 文件请求单独处理
    private void handleFileRequest(String[] requestLines, InputStream in, OutputStream out) throws IOException {
        int jsonLength = -1;
        for (String line : requestLines) {
            if (line.toLowerCase().startsWith("json-length:")) {
                try {
                    jsonLength = Integer.parseInt(line.substring(12).trim());
                } catch (NumberFormatException e) {
                    ServiceUtils.sendFileErrorResponse(out, 400, "Invalid Json-Length format");
                    return;
                }
                break;
            }
        }

        if (jsonLength <= 0) {
            ServiceUtils.sendFileErrorResponse(out, 400, "Missing or invalid Json-Length header");
            return;
        }

        ByteArrayOutputStream jsonBuffer = new ByteArrayOutputStream();
        byte[] tempBuffer = new byte[1024];
        int totalRead = 0;
        while (totalRead < jsonLength) {
            int bytesToRead = Math.min(tempBuffer.length, jsonLength - totalRead);
            int count = in.read(tempBuffer, 0, bytesToRead);
            if (count == -1) break;
            jsonBuffer.write(tempBuffer, 0, count);
            totalRead += count;
        }

        if (totalRead < jsonLength) {
            ServiceUtils.sendFileErrorResponse(out, 400, "Incomplete JSON data");
            return;
        }

        String jsonRaw = jsonBuffer.toString("UTF-8").trim();
        if (jsonRaw.isEmpty() || jsonRaw.charAt(0) != '{') { // 关键修复点
            ServiceUtils.sendFileErrorResponse(out, 400, "Invalid JSON: must start with '{'");
            return;
        }

        try {
            JSONObject json = new JSONObject(jsonRaw);
            String savePath = json.optString("savePath", "");
            if (savePath.isEmpty()) {
                ServiceUtils.sendFileErrorResponse(out, 400, "Missing savePath parameter");
                return;
            }
            new FileTransfer().handle(in, out, savePath, MAX_FILE_SIZE, 0);
        } catch (org.json.JSONException e) {
            ServiceUtils.sendFileErrorResponse(out, 400, "Malformed JSON: " + e.getMessage());
        }
    }

    static {
        HANDLER_MAP.put("/chat", (reqBody, out) -> new Chat().handle(reqBody, out, Config.DB_PATH));
        HANDLER_MAP.put("/createLink", (reqBody, out) -> new CreateLink().handle(reqBody, out, Config.DB_PATH));
        HANDLER_MAP.put("/friend/nickname", (reqBody, out) -> new UpdataFriendsNickname().handle(reqBody, out, Config.DB_PATH));
        HANDLER_MAP.put("/friend/network", (reqBody, out) -> new UpdataFriendsNetwork().handle(reqBody, out));
        HANDLER_MAP.put("/group/creation", (reqBody, out) -> new CreationGroup().handle(reqBody, out, Config.DB_PATH));
        HANDLER_MAP.put("/group/dismiss", (reqBody, out) -> new DismissGroup().handle(reqBody, out, Config.DB_PATH));
        HANDLER_MAP.put("/register", (reqBody, out) -> new Register().handle(reqBody, out, Config.DB_PATH));
        HANDLER_MAP.put("/login", (reqBody, out) -> new Login().handle(reqBody, out, Config.DB_PATH));
        HANDLER_MAP.put("/online", (reqBody, out) -> new Online().handle(reqBody));
        HANDLER_MAP.put("/offline", (reqBody, out) -> new Offline().handle(reqBody, out, Config.DB_PATH));
        HANDLER_MAP.put("/radioMode", (reqBody, out) -> new RadioMode().handle(reqBody, out));
    }

    @FunctionalInterface
    private interface Handler {
        void handle(String requestBody, OutputStream out) throws Exception;
    }


    // 服务器停止方法
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}
