package sever.file;

import core.Config;
import sever.ServiceUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileInterfaces {
    private static final int MAX_REQUEST_SIZE = 8192;  // 请求头最大大小
    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024 * 1024;  // 2GB文件限制

    public FileInterfaces(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("File server started on port " + port);
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
            byte[] headerBuffer = new byte[MAX_REQUEST_SIZE];
            int headerBytes = in.read(headerBuffer);
            if (headerBytes == -1) return;
            
            String header = new String(headerBuffer, 0, headerBytes);
            String[] headerLines = header.split("\r\n");
            if (headerLines.length == 0) {
                ServiceUtils.sendErrorResponse(out, 400, "Invalid Request");
                return;
            }

            // 解析请求行
            String[] requestParts = headerLines[0].split(" ");
            if (requestParts.length < 2) {
                ServiceUtils.sendErrorResponse(out, 400, "Invalid Request Line");
                return;
            }
            
            String method = requestParts[0];
            String requestPath = requestParts[1];
            
            // 检查是否为POST请求
            if (!"POST".equalsIgnoreCase(method)) {
                ServiceUtils.sendErrorResponse(out, 405, "Method Not Allowed");
                return;
            }

            // 获取Content-Length
            long contentLength = -1;
            for (String line : headerLines) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Long.parseLong(line.substring(15).trim());
                }
            }
            
            // 检查文件大小限制
            if (contentLength > MAX_FILE_SIZE) {
                ServiceUtils.sendErrorResponse(out, 413, "File too large");
                return;
            }

            // 根据请求路径分发处理
            if ("/upload".equals(requestPath)) {
                handleFileUpload(in, out, contentLength, header);
            } else if ("/uploadAvatar".equals(requestPath)) {
                handleAvatarUpload(in, out, contentLength, header);
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

    private void handleFileUpload(InputStream in, OutputStream out, long contentLength, String header) throws IOException {
        // 解析参数
        Map<String, String> params = parseParams(header);
        String isGroup = params.get("isGroup");
        String id = params.get("id");
        String fileName = params.get("fileName");

        if (isGroup == null || id == null || fileName == null) {
            ServiceUtils.sendErrorResponse(out, 400, "Missing parameters");
            return;
        }

        // 构建存储路径
        String baseDir = "1".equals(isGroup) ? 
                Paths.get(Config.FILE_BASE_DIR, "群聊", id).toString() :
                Paths.get(Config.FILE_BASE_DIR, "朋友", id).toString();
        
        Path filePath = Paths.get(baseDir, fileName);
        Files.createDirectories(filePath.getParent());

        // 保存文件
        try (FileOutputStream fileOut = new FileOutputStream(filePath.toFile())) {
            long remaining = contentLength;
            byte[] buffer = new byte[8192];
            while (remaining > 0) {
                int bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (bytesRead == -1) break;
                fileOut.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
            ServiceUtils.sendSuccessResponse(out);
        } catch (Exception e) {
            ServiceUtils.sendErrorResponse(out, 500, "File upload failed");
        }
    }

    private void handleAvatarUpload(InputStream in, OutputStream out, long contentLength, String header) throws IOException {
        // 解析参数
        Map<String, String> params = parseParams(header);
        String avatarType = params.get("avatarType");
        String fileType = params.get("fileType");
        String groupId = params.get("groupId");
        String memberId = params.get("memberId");
        String friendId = params.get("friendId");

        // 验证参数
        if (avatarType == null || fileType == null) {
            ServiceUtils.sendErrorResponse(out, 400, "Missing avatarType or fileType");
            return;
        }

        // 构建存储路径
        Path avatarPath;
        switch (avatarType) {
            case "group":
                if (groupId == null) {
                    ServiceUtils.sendErrorResponse(out, 400, "Missing groupId");
                    return;
                }
                avatarPath = Paths.get(Config.FILE_BASE_DIR, "群聊", groupId, "avatar." + fileType);
                break;
            case "member":
                if (groupId == null || memberId == null) {
                    ServiceUtils.sendErrorResponse(out, 400, "Missing groupId or memberId");
                    return;
                }
                avatarPath = Paths.get(Config.FILE_BASE_DIR, "群聊", groupId, "avatar", memberId + "." + fileType);
                break;
            case "friend":
                if (friendId == null) {
                    ServiceUtils.sendErrorResponse(out, 400, "Missing friendId");
                    return;
                }
                avatarPath = Paths.get(Config.FILE_BASE_DIR, "朋友", friendId, "avatar." + fileType);
                break;
            default:
                ServiceUtils.sendErrorResponse(out, 400, "Invalid avatarType");
                return;
        }

        // 创建目录并保存文件
        Files.createDirectories(avatarPath.getParent());
        try (FileOutputStream fileOut = new FileOutputStream(avatarPath.toFile())) {
            long remaining = contentLength;
            byte[] buffer = new byte[8192];
            while (remaining > 0) {
                int bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (bytesRead == -1) break;
                fileOut.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
            ServiceUtils.sendSuccessResponse(out);
        } catch (Exception e) {
            ServiceUtils.sendErrorResponse(out, 500, "Avatar upload failed");
        }
    }

    private Map<String, String> parseParams(String header) {
        Map<String, String> params = new HashMap<>();
        String[] lines = header.split("\r\n");
        
        // 从Content-Disposition解析参数
        for (String line : lines) {
            if (line.startsWith("Content-Disposition:")) {
                String[] parts = line.split(";");
                for (String part : parts) {
                    part = part.trim();
                    if (part.startsWith("name=\"")) {
                        int start = part.indexOf('"') + 1;
                        int end = part.lastIndexOf('"');
                        String key = part.substring(start, end);
                        
                        // 查找下一个分号或行尾
                        int valueStart = part.indexOf('=', end + 1);
                        if (valueStart != -1) {
                            String value = part.substring(valueStart + 1).trim();
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            }
                            params.put(key, value);
                        }
                    }
                }
            }
        }
        return params;
    }
}