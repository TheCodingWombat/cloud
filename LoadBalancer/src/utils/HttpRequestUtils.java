package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

public class HttpRequestUtils {
	
	public static String getRequestBody(HttpExchange exchange) {
		InputStream requestBodyStream = exchange.getRequestBody();
		String requestBody = null;
		try {
			requestBody = new String(requestBodyStream.readAllBytes(), StandardCharsets.UTF_8);

		} catch (IOException e) {
			throw new RuntimeException();
		}
		return requestBody;
	}

}
