package LoadBalancer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.sun.net.httpserver.HttpExchange;

public class RequestForwarder {

    public void forwardRequest(HttpExchange exchange, String targetUrl) throws IOException {
        // Parse request URI and query
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawQuery();
        String path = requestedUri.getPath();
        
        // Construct the target URL
        String fullUrl = targetUrl + path + (query != null ? "?" + query : "");

        // Open connection to the target URL
        HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();
        connection.setRequestMethod(exchange.getRequestMethod());

        // Forward headers
        exchange.getRequestHeaders().forEach((key, values) -> {
            values.forEach(value -> {
                connection.addRequestProperty(key, value);
            });
        });

        // Forward request body if necessary
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) || "PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream();
                 InputStream is = exchange.getRequestBody()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }

        // Get response from the target URL
        int responseCode = connection.getResponseCode();
        exchange.sendResponseHeaders(responseCode, connection.getContentLengthLong());

        // Forward response headers
        connection.getHeaderFields().forEach((key, values) -> {
            if (key != null) {
                exchange.getResponseHeaders().put(key, values);
            }
        });

        // Forward response body
        try (InputStream is = connection.getInputStream();
             OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        // Close the exchange
        exchange.close();
    }
}