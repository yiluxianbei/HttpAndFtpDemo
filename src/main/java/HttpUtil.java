import response.DownloadResponse;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {

    /**
     * Send a get request
     *
     * @param url
     * @return response
     * @throws IOException
     */
    public static String get(String url, Map<String, String> headers) {
        return fetch("GET", url, null, headers);
    }

    /**
     * Send a post request
     *
     * @param url  Url as string
     * @param body Request body as string
     * @return response   Response as string
     * @throws IOException
     */
    public static String postJson(String url, String body, Map<String, String> headers) throws IOException {
        if (headers == null) {
            headers = new HashMap<>();
        }
        // set content type
        headers.put("Content-Type", "application/json;charset=utf-8");
        return fetch("POST", url, body, headers);
    }

    /**
     * Post a form with parameters
     *
     * @param url    Url as string
     * @param params map with parameters/values
     * @return response   Response as string
     * @throws IOException
     */
    public static String postForm(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        if (headers == null) {
            headers = new HashMap<>();
        }
        // set content type
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        // parse parameters
        String body = "";
        if (params != null) {
            boolean first = true;
            for (String param : params.keySet()) {
                if (first) {
                    first = false;
                } else {
                    body += "&";
                }
                String value = params.get(param);
                body += URLEncoder.encode(param, "UTF-8") + "=";
                body += URLEncoder.encode(value, "UTF-8");
            }
        }
        return fetch("POST", url, body, headers);
    }

    /**
     * Send a put request
     *
     * @param url Url as string
     * @return response   Response as string
     * @throws IOException
     */
    public static String put(String url, String body, Map<String, String> headers) throws IOException {
        return fetch("PUT", url, body, headers);
    }

    /**
     * Send a delete request
     *
     * @param url     Url as string
     * @param headers Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    public static String delete(String url, Map<String, String> headers) throws IOException {
        return fetch("DELETE", url, null, headers);
    }

    /**
     * Send a request
     *
     * @param method  HTTP method, for example "GET" or "POST"
     * @param url     Url as string
     * @param body    Request body as string
     * @param headers Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    public static String fetch(String method, String url, String body, Map<String, String> headers) {
        String response = null;
        HttpURLConnection conn = null;
        // connection
        try {
            URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36");

            // method
            if (method != null) {
                conn.setRequestMethod(method);
            }

            // headers
            if (headers != null && headers.size() > 0) {
                for (String key : headers.keySet()) {
                    conn.setRequestProperty(key, headers.get(key));
                }
            }

            // body
            if (body != null) {
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(body.getBytes());
                os.flush();
                os.close();
            }

            if (200 == conn.getResponseCode()) {
                InputStream is = conn.getInputStream();
                StringBuffer out = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    out.append(line);
                }

                response = out.toString();
                is.close();
            } else if (conn.getResponseCode() == 301) {
                // handle redirects
                String location = conn.getHeaderField("Location");
                return fetch(method, location, body, headers);
            } else {
                InputStream is = conn.getErrorStream();
                StringBuffer out = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    out.append(line);
                }
                response = out.toString();
                is.close();
                throw new RuntimeException("请求接口失败，失败信息:" + response);
            }
        } catch (MalformedURLException | ProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return response;
    }

    /**
     * 下载文件
     *
     * @param headers 请求头
     * @return
     */
    public static DownloadResponse download(HttpURLConnection conn, Map<String, String> headers) {
        DownloadResponse downloadResponse = new DownloadResponse();
        try {
            conn.setRequestMethod("GET");
            //设置超时间为3秒
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36");
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                String contentType = conn.getHeaderField("Content-Type");
                if (contentType!=null && !contentType.equals("") && contentType.contains("json")) {
                    InputStream is = conn.getInputStream();
                    StringBuffer out = new StringBuffer();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        out.append(line);
                    }
                    throw new RuntimeException(out.toString());
                }
                downloadResponse.setInputStream(conn.getInputStream());
                //文件名
                String headerField = conn.getHeaderField("Content-Disposition");
                headerField = headerField.substring(headerField.indexOf("filename=") + 9);
                String fileName = headerField.substring(0, headerField.indexOf(";"));
                downloadResponse.setFileName(URLDecoder.decode(fileName, "UTF-8"));
            }else {
                StringBuffer response = new StringBuffer();
                String readLine;
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                while ((readLine = responseReader.readLine()) != null) {
                    response.append(readLine).append("\n");
                }
                throw new RuntimeException(response.toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return downloadResponse;
    }


    /**
     * 实现参数传输以及文件传输
     *
     * @param actionUrl         访问的服务器URL
     * @param headers           请求头
     * @param params            普通参数
     * @param fileNameAndStream map<文件名，对应的流>
     * @return
     * @throws IOException
     */
    public static String uploadWithParms(String actionUrl, Map<String, String> headers, Map<String, String> params, Map<String, InputStream> fileNameAndStream) {
        //http协议的分隔符
        String BOUNDARY = String.valueOf(System.currentTimeMillis());
        String PREFIX = "--";
        DataOutputStream outStream = null;
        BufferedReader responseReader = null;
        String result = "";
        HttpURLConnection conn = null;
        try {
            URL uri = new URL(actionUrl);
            conn = (HttpURLConnection) uri.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data" + ";boundary=" + BOUNDARY);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36");
            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            outStream = new DataOutputStream(conn.getOutputStream());
            // 首先组拼文本类型的参数
            if (params != null && params.size() > 0) {
                StringBuilder param = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    param.append(PREFIX);
                    param.append(BOUNDARY);
                    param.append("\r\n");
                    param.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n");
                    param.append("Content-Type: text/plain; charset=" + "UTF-8" + "\r\n");
                    param.append("Content-Transfer-Encoding: 8bit\r\n");
                    param.append("\r\n");
                    param.append(entry.getValue());
                    param.append("\r\n");
                }

                outStream.write(param.toString().getBytes());
            }

            // 发送文件数据
            for (Map.Entry<String, InputStream> entry : fileNameAndStream.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append("\r\n");
                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + entry.getKey() + "\"\r\n");
                sb.append("Content-Type: application/octet-stream; charset=" + "UTF-8" + "\r\n");
                sb.append("\r\n");
                outStream.write(sb.toString().getBytes());

                InputStream is = entry.getValue();
                byte[] buffer = new byte[1024 * 3];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                is.close();
                outStream.write("\r\n".getBytes());
            }
            //请求结束标志
            outStream.write((PREFIX + BOUNDARY + PREFIX + "\r\n").getBytes());
            outStream.flush();
            int responseCode = conn.getResponseCode();
            StringBuffer response = new StringBuffer();
            String readLine;
            responseReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while ((readLine = responseReader.readLine()) != null) {
                response.append(readLine).append("\n");
            }
            result = response.toString();
            if (HttpURLConnection.HTTP_OK != responseCode) {
                throw new RuntimeException(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (responseReader != null) {
                        try {
                            responseReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally {
                            if (conn != null) {
                                conn.disconnect();
                            }
                        }
                    }
                }

            }
        }
        return result;
    }


}