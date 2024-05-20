package load_balancer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import metric_storage_system.MetricStorageSystem;
import metric_storage_system.RequestEstimation;
import request_types.AbstractRequestType;
import utils.HttpRequestUtils;

public class LoadBalancer implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {	
		String requestBody = HttpRequestUtils.getRequestBodyString(exchange);
		AbstractRequestType requestType = AbstractRequestType.ofRequest(exchange, requestBody);
		RequestEstimation estimation = MetricStorageSystem.calculateEstimation(requestType);
		forwardRequest(exchange, requestBody, estimation);
	}

	private void forwardRequest(HttpExchange exchange, String requestBody, RequestEstimation estimation) throws IOException {
		//use estimation later to do forward logic
		//Url of local workerWebServer
		URL url = new URL("http", "localhost", 8000, exchange.getRequestURI().getPath());	
		HttpURLConnection connection = HttpRequestUtils.forwardRequest(url, exchange, requestBody);
		int statusCode = HttpRequestUtils.sendResponseToClient(exchange, connection);
	}
}