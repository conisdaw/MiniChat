package sever;

import core.Config;

import java.io.*;

public class FileTransfer {
    public void handle(InputStream in, OutputStream out, String savePath, long maxFileSize, int headerLength) throws IOException {
        // 确保路径以基础目录开头
        if (!savePath.startsWith(Config.FILE_BASE_DIR)) {
            savePath = Config.FILE_BASE_DIR + File.separator + savePath;
        }
        
        // 创建目录结构
        File file = new File(savePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            // 写入已读取的部分数据
            if (headerLength > 0) {
                byte[] buffer = new byte[8192];
                int bytesToWrite = Math.min(headerLength, buffer.length);
                int read = in.read(buffer, 0, bytesToWrite);
                if (read > 0) {
                    fileOut.write(buffer, 0, read);
                }
            }

            // 流式传输剩余文件内容
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesWritten = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                totalBytesWritten += bytesRead;
                
                // 检查文件大小限制
                if (totalBytesWritten > maxFileSize) {
                    fileOut.close();
                    new File(savePath).delete();
                    ServiceUtils.sendFileErrorResponse(out, 413, "文件大小超过10GB限制");
                    return;
                }
                
                fileOut.write(buffer, 0, bytesRead);
            }

            ServiceUtils.sendFileSuccessResponse(out, savePath);
        } catch (IOException e) {
            new File(savePath).delete();
            ServiceUtils.sendFileErrorResponse(out, 500, "文件传输失败: " + e.getMessage());
        }
    }
}