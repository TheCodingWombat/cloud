package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

public class HttpRequestUtils {

	/**
	 * Reads request body and returns the content as String
	 * 
	 * !!Watch out... the body can only be read once. If you have requested it once
	 * the InputStream is null afterwards.
	 * 
	 * @param exchange
	 * @return
	 */
	public static String getRequestBodyString(HttpExchange exchange) {
		InputStream requestBodyStream = exchange.getRequestBody();
		String requestBody = null;
		try {
			requestBody = new String(requestBodyStream.readAllBytes(), StandardCharsets.UTF_8);

		} catch (IOException e) {
			throw new RuntimeException();
		}
		return requestBody;
	}

	public static HttpURLConnection forwardRequest(URL url, HttpExchange exchange, String requestBody)
			throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(exchange.getRequestMethod());
		copyHeaders(connection, exchange);
		setRequestBody(connection, requestBody);
		return connection;
	}

	private static void copyHeaders(HttpURLConnection connection, HttpExchange exchange) {
		for (Map.Entry<String, List<String>> header : exchange.getRequestHeaders().entrySet()) {
			for (String value : header.getValue()) {
				connection.addRequestProperty(header.getKey(), value);
			}
		}
	}

	private static void setRequestBody(HttpURLConnection connection, String requestBody) throws IOException {
		connection.setDoOutput(true);
		try (OutputStream connectionOutputStream = connection.getOutputStream()) {
			byte[] buffer = requestBody.getBytes("UTF-8");
			connectionOutputStream.write(buffer);
		}
	}
	
	public static int sendResponseToClient(HttpExchange exchange, HttpURLConnection connection) throws IOException {
		int responseCode = connection.getResponseCode();
		exchange.sendResponseHeaders(responseCode, connection.getContentLength());
		appendResponseBody(exchange, connection);
        return responseCode;
	}

	private static void appendResponseBody(HttpExchange exchange, HttpURLConnection connection) throws IOException {
		InputStream responseBodyStream = connection.getInputStream();		
        try (OutputStream responseBody = exchange.getResponseBody()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = responseBodyStream.read(buffer)) != -1) {
                responseBody.write(buffer, 0, bytesRead);
            }
        }
	}
}