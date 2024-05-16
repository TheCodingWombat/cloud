package load_balancer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import request_types.AbstractRequestType;

public class LoadBalancer implements HttpHandler {
	
	String endPoint = "http://localhost:8001";

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		//analyze incoming request
		URI requestedUri = exchange.getRequestURI();
		System.out.println("Requested resource:" + requestedUri.getPath());

		
		AbstractRequestType requestType = AbstractRequestType.ofResouce(requestedUri.toString());
		
		
		String query = requestedUri.getRawQuery();
		System.out.println(query);

		exchange.sendResponseHeaders(200, 0);

		String response = "You requested" + query;
		OutputStream os = exchange.getResponseBody();
		os.write(response.getBytes());
		os.close();

		requestWorker();
	}

	private void requestWorker() throws MalformedURLException, IOException {
		String targetUrl = "http://localhost:8001";

		HttpURLConnection connection = (HttpURLConnection) new URL(targetUrl).openConnection();
		connection.setRequestMethod("GET");

		int responseCode = connection.getResponseCode();
		System.out.println("Response Code: " + responseCode);

		try (InputStream is = connection.getInputStream()) {
			System.out.println(is.readAllBytes());
		}
	}
}