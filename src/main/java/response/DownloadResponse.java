package response;

import java.io.InputStream;

public class DownloadResponse {
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件流
     */
    private InputStream inputStream;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
