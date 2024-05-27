package load_balancer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
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
	// CPU usage threshold in percentage
	private static final int MAX_INSTANCES = 5; // Maximum number of instances to deploy
	private static final int MIN_INSTANCES = 1; // Minimum number of instances to keep running
	private static int CURRENT_INSTANCES = 0; // Current number of instances
	private static final List<Instance> instances = new ArrayList<>(); // Array with instances
	private static final int MAX_MEMORY = 1000000000; // 1GB
	private static final int REQUEST_COUNT_MAX = 3; // Maximum number of requests per instance

	// do the same as below but do map with string and then list with 2 elements integer and integet
	//private static final Map<String, List<Integer>> instanceRequestCount = new HashMap<>(); // Map to store VM ip : <request counts, estimated memory usage>

	private static final Map<String, Integer> instanceRequestCount = new HashMap<>(); // Map to store request counts
	private static String instanceIP = "";
	private static String instanceID = "";

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String requestBody = HttpRequestUtils.getRequestBodyString(exchange);
		AbstractRequestType requestType = AbstractRequestType.ofRequest(exchange, requestBody);
		RequestEstimation estimation = MetricStorageSystem.calculateEstimation(requestType);

		boolean instanceAvailable = AwsEc2Manager.checkAvailableInstances();

		if (instances.isEmpty() && !instanceAvailable) {
			deployNewInstance();
			CURRENT_INSTANCES++;

		} else if (instanceAvailable && instances.isEmpty()) {
			System.out.println("Instance already available and we are going to distribute the call");
			instances.addAll(AwsEc2Manager.getAllRunningInstances());
			instanceIP = instances.get(0).publicIpAddress();
			instanceID = instances.get(0).instanceId();

			for (Instance inst : instances) {
				instanceRequestCount.put(inst.instanceId(), 0);
				CURRENT_INSTANCES++;
			}
		} else if (!instances.isEmpty()) {
			System.out.println("Instance already available and we are going to distribute the call");
			// Check if there is an instance with 3 current requests if yes deploy new instance
			if (instanceRequestCount.get(instanceID) == REQUEST_COUNT_MAX && CURRENT_INSTANCES <= MAX_INSTANCES) {
				deployNewInstance();
			}
			else {
				System.out.println("MAX INSTANCES IS REACHED");
			}
		}

		// Increment request count for the chosen instance
		instanceRequestCount.put(instanceID, instanceRequestCount.get(instanceID) + 1);
		System.out.println("Request count for instance: " + instanceID + " is: " + instanceRequestCount.get(instanceID));


		//instanceIP = "localhost";
		forwardRequest(exchange, requestBody, estimation, requestType, instanceIP, instanceID);
	}

	private void forwardRequest(HttpExchange exchange, String requestBody, RequestEstimation estimation, AbstractRequestType requestType, String instanceIP, String instanceID) throws IOException {
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

		// Decrement request count for the chosen instance after response is sent
		instanceRequestCount.put(instanceID, instanceRequestCount.get(instanceID) - 1);
		System.out.println("Request count for instance now is: " + instanceID + " is: " + instanceRequestCount.get(instanceID));

		RequestMetrics metrics = extractMetrics(connection);
		MetricStorageSystem.storeMetric(requestType, metrics);
		System.out.println("---------------------------------------------");

	}

	private RequestMetrics extractMetrics(HttpURLConnection connection) {
		Map<String, List<String>> headers = connection.getHeaderFields();

		long memory = Long.parseLong(headers.get("Methodmemoryallocatedbytes").get(0));
		System.out.println("Request memory: " + memory);

		long cpuTime = Long.parseLong(headers.get("Methodcpuexecutiontimens").get(0));
		System.out.println("Requestt cpuTime: " + cpuTime);

		return new RequestMetrics(cpuTime, memory);
	}

	private static void deployNewInstance() {
		Instance newInst = AwsEc2Manager.deployNewInstance();
		instances.add(newInst);
		instanceRequestCount.put(newInst.instanceId(), 0);
		instanceID = newInst.instanceId();
		instanceIP = newInst.publicIpAddress();
		System.out.println("New instance deployed with id: " + instanceID + " and IP: " + instanceIP);
	}
}