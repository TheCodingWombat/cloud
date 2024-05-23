package load_balancer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import metric_storage_system.MetricStorageSystem;
import metric_storage_system.RequestEstimation;
import metric_storage_system.RequestMetrics;
import request_types.AbstractRequestType;
import utils.HttpRequestUtils;

public class LoadBalancer implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {	
		String requestBody = HttpRequestUtils.getRequestBodyString(exchange);
		AbstractRequestType requestType = AbstractRequestType.ofRequest(exchange, requestBody);
		RequestEstimation estimation = MetricStorageSystem.calculateEstimation(requestType);
		forwardRequest(exchange, requestBody, estimation, requestType);
	}

	private void forwardRequest(HttpExchange exchange, String requestBody, RequestEstimation estimation, AbstractRequestType requestType) throws IOException {
		//use estimation later to do forward logic
		//Url of local workerWebServer
		String uri = exchange.getRequestURI().getPath();
		String query = exchange.getRequestURI().getQuery();
		if (query != null) {
			uri += "?" + query;
		}
		// print first 50 characters of query string

		
		URL url = new URL("http", "localhost", 8000, uri);	
		System.out.println("Handling request: " + uri);
		HttpURLConnection connection = HttpRequestUtils.forwardRequest(url, exchange, requestBody);
		int statusCode = HttpRequestUtils.sendResponseToClient(exchange, connection);

		RequestMetrics metrics = extractMetrics(connection);
		MetricStorageSystem.storeMetric(requestType, metrics);
	}

	private RequestMetrics extractMetrics(HttpURLConnection connection) {
		Map<String, List<String>> headers = connection.getHeaderFields();

		long memory = Long.parseLong(headers.get("Methodmemoryallocatedbytes").get(0));
		System.out.println("Request memory: " + memory);

		long cpuTime = Long.parseLong(headers.get("Methodcpuexecutiontimens").get(0));
		System.out.println("Requestt cpuTime: " + cpuTime);

		return new RequestMetrics(cpuTime, memory);
	}
}