package load_balancer;

import java.io.*;
import java.net.*;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class LoadBalancer implements HttpHandler {
	// CPU usage threshold in percentage
	private static final int MAX_INSTANCES = 5; // Maximum number of instances to deploy
	private static final int MIN_INSTANCES = 1; // Minimum number of instances to keep running
	private static int CURRENT_INSTANCES = 0; // Current number of instances
	private static final List<Instance> instances = new ArrayList<>(); // Array with instances
	private static final int MEMORY_THRESHOLD = 100; // 80% memory usage
	private static final int MAX_MEMORY = 970 - MEMORY_THRESHOLD; // 1GB
	private static final int MAX_CPU = 80; // 80%
	private static final int REQUEST_COUNT_MAX = 3; // Maximum number of requests per instance
	private static final String USER = "ec2-user";
	private static final Map<String, Integer> instanceRequestCount = new HashMap<>(); // Map to store request counts

	// do the same as below but do map with string and then list with 2 elements integer and integet
	//private static final Map<String, List<Integer>> instanceRequestCount = new HashMap<>(); // Map to store VM ip : <request counts, estimated memory usage>

	/*
	 * Debug flag
	 * SET DEBUG TO TRUE TO RUN WITH ONE VM IN AWS
	 *
	 * PASTE THE VM IP IN THE VARIABLE instanceIP.
	 * PASTE THE VM ID IN THE VARIABLE instanceID.
	 *
	 */
	public static final boolean DEBUG = true;
	private static String instanceIP = "localhost";
	private static String instanceID = "i-0927c392dd954b616";
	private static final String KEYPATH = "C:/Users/tedoc/newkey.pem";

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		String requestBody = HttpRequestUtils.getRequestBodyString(exchange);
		AbstractRequestType requestType = AbstractRequestType.ofRequest(exchange, requestBody);
		RequestEstimation estimation = MetricStorageSystem.calculateEstimation(requestType);

		if (DEBUG) {
			System.out.println("Running in debug mode");
		}
		else {
			boolean instanceAvailable = AwsEc2Manager.checkAvailableInstances();

			if (instances.isEmpty()) {
				instances.addAll(AwsEc2Manager.getAllRunningInstances());
				for (Instance inst : instances) {
					instanceRequestCount.put(inst.instanceId(), 0);
					CURRENT_INSTANCES++;
				}
				if (instances.isEmpty()) {
					System.out.println("No instances available, deploying new instance");
					deployNewInstance();
				}
			}
			if (!instances.isEmpty()) {
				boolean isFound = false;
				System.out.println("Instance already available and we are going to distribute the call");
				// Check if there is an instance with 3 current requests if yes deploy new instance
				for (Instance inst : instances) {

					if (CURRENT_INSTANCES < MAX_INSTANCES && instanceRequestCount.get(inst.instanceId()) < REQUEST_COUNT_MAX) {
						System.out.println("Instance: " + inst.instanceId() + " fine and available for request");
						instanceID = inst.instanceId();
						instanceIP = inst.publicIpAddress();
						isFound = true;
						break;
					}
				}
				if (!isFound) {
					System.out.println("All instances are full, deploying new instance");
					// deployNewInstance();
				}
			}
			double cpuUsage = AwsEc2Manager.getCpuUtilization(instanceID);
			System.out.println("Current CPU usage: " + cpuUsage);
			// Increment request count for the chosen instance
			instanceRequestCount.put(instanceID, instanceRequestCount.get(instanceID) + 1);
			System.out.println("Request count for instance: " + instanceID + " is: " + instanceRequestCount.get(instanceID));
		}

		//List<Double> usageMetrics = getCurrentUsage(instanceIP);
		//System.out.println("Current CPU usage: " + usageMetrics.get(0));
		//System.out.println("Current memory usage: " + usageMetrics.get(1));

		forwardRequest(exchange, requestBody, estimation, requestType, instanceIP, instanceID);
	}

	private void forwardRequest(HttpExchange exchange, String requestBody, RequestEstimation estimation,
			AbstractRequestType requestType, String instanceIP, String instanceID) throws IOException {
		// Use estimation later to do forward logic
		// URL of local workerWebServer
		String uri = exchange.getRequestURI().getPath();
		String query = exchange.getRequestURI().getQuery();
		if (query != null) {
			uri += "?" + query;
		}

		URL url = new URL("http", instanceIP, 8000, uri);
		System.out.println("Handling request: " + uri);
		HttpURLConnection connection = HttpRequestUtils.forwardRequest(url, exchange, requestBody);
		int statusCode = HttpRequestUtils.sendResponseToClient(exchange, connection);

		if (!DEBUG) {
			// Decrement request count for the chosen instance after response is sent
			instanceRequestCount.put(instanceID, instanceRequestCount.get(instanceID) - 1);
			System.out.println("Request count for instance now is: " + instanceID + " is: " + instanceRequestCount.get(instanceID));
		}

		RequestMetrics metrics = RequestMetrics.extractMetrics(connection);
		MetricStorageSystem.storeMetric(requestType, metrics);
	}

	private static void deployNewInstance() {
		Instance newInst = AwsEc2Manager.deployNewInstance();
		instances.add(newInst);
		instanceRequestCount.put(newInst.instanceId(), 0);
		instanceID = newInst.instanceId();
		instanceIP = newInst.publicIpAddress();
		CURRENT_INSTANCES++;
		System.out.println("New instance deployed with id: " + instanceID + " and IP: " + instanceIP);
	}

	private String getUsageFromRemoteVM(String command) {
		StringBuilder output = new StringBuilder();
		int retryCount = 5; // Number of retries
		int retryDelay = 5000; // Delay between retries in milliseconds

		for (int attempt = 1; attempt <= retryCount; attempt++) {
			try {
				ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
				processBuilder.redirectErrorStream(true);
				Process process = processBuilder.start();

				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
				}
				int exitCode = process.waitFor();
				if (exitCode != 0) {
					throw new RuntimeException("Shell script exited with non-zero status");
				}
				return output.toString().trim();
			} catch (IOException | InterruptedException e) {
				System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
				if (attempt == retryCount) {
					e.printStackTrace();
					throw new RuntimeException("Failed to execute remote command after " + retryCount + " attempts", e);
				}
				try {
					System.out.println("Retrying in " + retryDelay / 1000 + " seconds...");
					Thread.sleep(retryDelay);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Retry interrupted", ie);
				}
			}
		}
		return output.toString().trim();
	}

	private double parseMemoryUsage(String memoryUsage) {
		if (memoryUsage.endsWith("G")) {
			return Double.parseDouble(memoryUsage.substring(0, memoryUsage.length() - 1)) * 1024; // Convert GB to MB
		} else if (memoryUsage.endsWith("M")) {
			return Double.parseDouble(memoryUsage.substring(0, memoryUsage.length() - 1));
		} else {
			return Double.parseDouble(memoryUsage);
		}
	}

	private List<Double> getCurrentUsage(String instanceIP) {
		String command = String.format(
				"ssh -o StrictHostKeyChecking=no -i %s %s@%s 'free -h | grep Mem && mpstat | grep \"all\"'",
				KEYPATH, USER, instanceIP
		);
		List<Double> usage = new ArrayList<>();
		String output = getUsageFromRemoteVM(command);

		String[] lines = output.split("\n");
		if (lines.length >= 2) {
			String memoryLine = lines[0];
			String cpuLine = lines[1];

			double memoryUsed = parseMemoryUsage(memoryLine.split("\\s+")[2]); // Adjust index if necessary
			double cpuIdle = Double.parseDouble(cpuLine.split("\\s+")[3]); // Adjust index if necessary

			usage.add(cpuIdle);
			usage.add(memoryUsed);
		}
		return usage;
	}

	private boolean isInstanceFull(List<Double> currentUsage) {
		double cpuUsage = currentUsage.get(0);
		double memoryUsage = currentUsage.get(1);

		return cpuUsage >= MAX_CPU || memoryUsage >= MAX_MEMORY;
	}

}
