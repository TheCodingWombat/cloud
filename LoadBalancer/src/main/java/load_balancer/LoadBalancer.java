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
import java.util.ArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import metric_storage_system.MetricStorageSystem;
import metric_storage_system.RequestEstimation;
import metric_storage_system.RequestMetrics;
import request_types.AbstractRequestType;
import utils.HttpRequestUtils;
import deployment_manager.AwsEc2Manager;
import software.amazon.awssdk.services.ec2.model.Instance;

public class LoadBalancer implements HttpHandler {

	private static final double CPU_THRESHOLD = 75.0; // CPU usage threshold in percentage
	private static final int MAX_INSTANCES = 5; // Maximum number of instances to deploy
	private static final int MIN_INSTANCES = 1; // Minimum number of instances to keep running
	private static final List<Instance> instances = new ArrayList<>(); // Array with instances

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String requestBody = HttpRequestUtils.getRequestBodyString(exchange);
		AbstractRequestType requestType = AbstractRequestType.ofRequest(exchange, requestBody);
		RequestEstimation estimation = MetricStorageSystem.calculateEstimation(requestType);
		String instanceIP = "";
		boolean instanceAvailable = AwsEc2Manager.checkAvailableInstances();

		if (instances.isEmpty() && !instanceAvailable) {
			Instance newInst = AwsEc2Manager.deployNewInstance();
			instances.add(newInst);
			instanceIP = newInst.publicIpAddress();
		} else if (instanceAvailable && instances.isEmpty()) {
			System.out.println("Instance already available and we are going to distribute the call");
			instances.addAll(AwsEc2Manager.getAllRunningInstances());
			instanceIP = instances.get(0).publicIpAddress();
		}

		System.out.println("Forwarding request to instance: " + instanceIP);

		forwardRequest(exchange, requestBody, estimation, requestType, instanceIP);


	}

	private void forwardRequest(HttpExchange exchange, String requestBody, RequestEstimation estimation, AbstractRequestType requestType, String instanceIP) throws IOException {
		// Use estimation later to do forward logic
		// URL of local workerWebServer
        String uri = exchange.getRequestURI().getPath();
		String query = exchange.getRequestURI().getQuery();
		if (query != null) {
			uri += "?" + query;
		}
		// print first 50 characters of query string


		URL url = new URL("http", instanceIP, 8000, uri);
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