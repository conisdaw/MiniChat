package sever;

import core.Config;
import java.io.*;

public class FileTransfer {
    public void handle(InputStream in, OutputStream out, String savePath, long maxFileSize, int skipBytes) throws IOException {
        if (savePath.startsWith("/") || savePath.startsWith("\\")) {
            savePath = savePath.substring(1);
        }

        savePath = savePath.replace("\\", "/");

        if (!savePath.startsWith(Config.FILE_BASE_DIR)) {
            savePath = Config.FILE_BASE_DIR + "/" + savePath;
        }

        // 创建目录结构
        File file = new File(savePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            // 跳过已处理的字节
            if (skipBytes > 0) {
                long skipped = in.skip(skipBytes);
                if (skipped != skipBytes) {
                    throw new IOException("Failed to skip bytes");
                }
            }

            // 流式传输文件内容
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesWritten = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                totalBytesWritten += bytesRead;

                // 检查文件大小限制
                if (totalBytesWritten > maxFileSize) {
                    fileOut.close();
                    file.delete();
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