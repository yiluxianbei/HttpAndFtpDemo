import response.DownloadResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class Test {

    /**
     * 下载demo
     * @param args
     */
    public static void main(String[] args) {
        URL urlObj = null;
        HttpURLConnection connection =null;
        try {
            urlObj = new URL("https://itapis.cvte.com/cfile" + "/"
                    + "9dde3f3e-7411-46ad-8c66-cf4f58fbed09"
                    + "/v1/download_by_name?fileName="
                    + "202011051051223723.jpg"
                    + "&categoryId="
                    + "emall_id");
            connection = (HttpURLConnection) urlObj.openConnection();
            DownloadResponse download = HttpUtil.download(connection, null);
            InputStream inputStream = download.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (connection !=null) {
                connection.disconnect();
            }
        }

    }
}
