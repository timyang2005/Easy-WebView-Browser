package com.example.webviewbrowser;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 轻量级本地 HTTP 服务器，用于在 localhost 上托管 assets 目录中的文件。
 * 解决 WebView 通过 file:// 协议无法加载 ES Module（importmap、type="module"）的问题。
 */
public class LocalAssetServer {

    private static final String TAG = "LocalAssetServer";
    private ServerSocket serverSocket;
    private int port;
    private volatile boolean running = false;
    private Thread serverThread;
    private final Context context;

    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put(".html", "text/html; charset=utf-8");
        MIME_TYPES.put(".js",   "application/javascript; charset=utf-8");
        MIME_TYPES.put(".mjs",  "application/javascript; charset=utf-8");
        MIME_TYPES.put(".css",  "text/css; charset=utf-8");
        MIME_TYPES.put(".json", "application/json; charset=utf-8");
        MIME_TYPES.put(".png",  "image/png");
        MIME_TYPES.put(".jpg",  "image/jpeg");
        MIME_TYPES.put(".jpeg", "image/jpeg");
        MIME_TYPES.put(".gif",  "image/gif");
        MIME_TYPES.put(".svg",  "image/svg+xml");
        MIME_TYPES.put(".ico",  "image/x-icon");
        MIME_TYPES.put(".woff",  "font/woff");
        MIME_TYPES.put(".woff2", "font/woff2");
        MIME_TYPES.put(".ttf",  "font/ttf");
        MIME_TYPES.put(".webmanifest", "application/manifest+json");
        MIME_TYPES.put(".webp", "image/webp");
    }

    public LocalAssetServer(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 启动本地服务器，返回基础 URL（如 http://localhost:12345/）
     */
    public String start() throws IOException {
        serverSocket = new ServerSocket(0); // 自动分配可用端口
        port = serverSocket.getLocalPort();
        running = true;

        serverThread = new Thread(() -> {
            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    handleRequest(client);
                } catch (IOException e) {
                    if (running) {
                        Log.e(TAG, "Error accepting connection", e);
                    }
                }
            }
        }, "LocalAssetServer");
        serverThread.setDaemon(true);
        serverThread.start();

        String baseUrl = "http://localhost:" + port + "/";
        Log.i(TAG, "Local asset server started at " + baseUrl);
        return baseUrl;
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing server", e);
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    public String getBaseUrl() {
        return "http://localhost:" + port + "/";
    }

    private void handleRequest(Socket client) {
        try {
            InputStream input = client.getInputStream();
            OutputStream output = client.getOutputStream();

            // 读取请求行
            String requestLine = readLine(input);
            if (requestLine == null || requestLine.isEmpty()) {
                client.close();
                return;
            }

            // 解析请求路径：GET /path HTTP/1.1
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                client.close();
                return;
            }

            String method = parts[0];
            String rawPath = parts[1];

            // 只处理 GET 请求
            if (!"GET".equals(method)) {
                sendResponse(output, 405, "Method Not Allowed", "text/plain");
                client.close();
                return;
            }

            // 去掉查询参数
            String path = rawPath.split("\\?")[0];

            // URL 解码
            path = java.net.URLDecoder.decode(path, "UTF-8");

            // 映射到 assets 路径
            String assetPath = path.substring(1); // 去掉开头的 /
            if (assetPath.isEmpty()) {
                assetPath = "index.html";
            }

            // 尝试读取 asset 文件
            InputStream assetStream = null;
            try {
                assetStream = context.getAssets().open(assetPath);
            } catch (IOException e) {
                // 文件不存在，返回 404
                sendResponse(output, 404, "Not Found", "text/plain");
                client.close();
                return;
            }

            // 确定 MIME 类型
            String mimeType = getMimeType(assetPath);

            // 读取文件内容
            byte[] content = readAllBytes(assetStream);
            assetStream.close();

            // 发送响应，带 CORS 头
            String header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + mimeType + "\r\n" +
                    "Content-Length: " + content.length + "\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "\r\n";
            output.write(header.getBytes("UTF-8"));
            output.write(content);
            output.flush();

        } catch (IOException e) {
            Log.e(TAG, "Error handling request", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void sendResponse(OutputStream output, int code, String status, String contentType) throws IOException {
        String body = code + " " + status;
        String header = "HTTP/1.1 " + code + " " + status + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.getBytes("UTF-8").length + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "\r\n";
        output.write(header.getBytes("UTF-8"));
        output.write(body.getBytes("UTF-8"));
        output.flush();
    }

    private String readLine(InputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = input.read()) != -1) {
            if (ch == '\r') {
                input.read(); // 消费 \n
                break;
            }
            if (ch == '\n') {
                break;
            }
            sb.append((char) ch);
        }
        return sb.toString();
    }

    private byte[] readAllBytes(InputStream input) throws IOException {
        byte[] buffer = new byte[8192];
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        int len;
        while ((len = input.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

    private String getMimeType(String path) {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex >= 0) {
            String ext = path.substring(dotIndex).toLowerCase();
            String mime = MIME_TYPES.get(ext);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }
}
