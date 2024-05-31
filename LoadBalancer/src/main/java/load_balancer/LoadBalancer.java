package load_balancer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import metric_storage_system.MetricStorageSystem;
import metric_storage_system.RequestEstimation;
import metric_storage_system.RequestMetrics;
import request_types.AbstractRequestType;
import request_types.BlurImageRequest;
import request_types.EnhanceImageRequest;
import request_types.RayTracerRequest;
import utils.HttpRequestUtils;
import deployment_manager.AwsEc2Manager;
import software.amazon.awssdk.services.ec2.model.Instance;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


public class LoadBalancer implements HttpHandler {
	// CPU usage threshold in percentage
	private static final AutoScaler autoScaler = new AutoScaler();
	static final List<Instance> instances = new ArrayList<>(); // Array with instances

	private static final int REQUEST_COUNT_MAX = 5; // Maximum number of requests per instance
	private static final String USER = "ec2-user";

	// For each instance, keep a list of the current requests for that machine and their complexity estimation
	public static final Map<String, Map<AbstractRequestType, RequestEstimation>> instanceRequests = new ConcurrentHashMap<>();
	public static final AtomicBoolean isCurrentlyDeploying = new AtomicBoolean(false);
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

	public static final boolean DEBUG = false;
	private static final String KEYPATH = "C:/Users/tedoc/newkey.pem";

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		try {
			String requestBody = HttpRequestUtils.getRequestBodyString(exchange);
			try {
				AbstractRequestType requestType = AbstractRequestType.ofRequest(exchange, requestBody);
			} catch (IllegalArgumentException e) {
				// TODO: temp fix, since it works on richards laptop without this, but not on teos
				// System.out.println("Empty request body, ignore");
				return;
			}
			AbstractRequestType requestType = AbstractRequestType.ofRequest(exchange, requestBody);
			RequestEstimation estimation = MetricStorageSystem.calculateEstimation(requestType);

			while (true) {
				InstanceType instance = chooseInstance();
				String instanceID = "";
				String instanceIP = "";

				if (instance instanceof Lambda) {
					// assume lambda cannot fail
					forwardLambdaRequestAndSendToUser(exchange, requestBody, estimation, requestType);
					return; // forwardLambdaRequest sends back response to client
				} else if (instance instanceof Local) {
					instanceIP = ((Local) instance).getInstanceIp();
					instanceID = ((Local) instance).getInstanceId();

					// TODO foward and fail safe
				} else if (instance instanceof EC2) {
					EC2 ec2Instance = (EC2) instance;
					instanceIP = ec2Instance.getInstance().publicIpAddress();
					instanceID = ec2Instance.getInstance().instanceId();
				}

				if (instance instanceof EC2) addRequestEstimation(instanceID, requestType, estimation);// Add the request and estimation to the instance
				HttpURLConnection connection = forwardRequest(exchange, requestBody, estimation, requestType, instanceIP, instanceID);

				try {
					Thread.sleep(1000);
				} catch (Exception e) {

				}

				try {
					int responseCode = connection.getResponseCode(); // vm finished or crashed
					if (instance instanceof EC2) removeRequestEstimation(instanceID, requestType);
					System.out.println(responseCode);
					// if success: finish
					if (responseCode == 200 || responseCode == 204) {
						HttpRequestUtils.sendResponseToClient(exchange, connection);

						RequestMetrics metrics = RequestMetrics.extractMetrics(connection);
						MetricStorageSystem.storeMetric(requestType, metrics);
						break;
					}
				} catch (IOException e) {
					if (instance instanceof EC2) removeRequestEstimation(instanceID, requestType);
					System.out.println("Request failed, error on gettign response code");
				}

				synchronized (instances) {
					// Check that the instance is still running through Amazon SDK
					if (instance instanceof EC2) {
						final String finalInstanceID = instanceID;
						// check if instance corresponding to instanceID is still in the instances list
						if (!instances.stream().anyMatch(inst -> inst.instanceId().equals(finalInstanceID))) {
							System.out.println("Instance was already killed by different thread");
							continue;
						}

						if (!AwsEc2Manager.isInstanceRunning(instanceID)) {
							System.out.println("VM crashed, removing from instances");
							instances.removeIf(inst -> inst.instanceId().equals(finalInstanceID));
							instanceRequests.remove(instanceID);
							// Kill on amazon to be sure
							AwsEc2Manager.terminateInstance(instanceID);
						
						}
					}	
				}

				// Fail, try again
				System.out.println("Request failed, trying again");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private InstanceType chooseInstance() {
		if (DEBUG) {
			System.out.println("Running in debug mode");
					// will be overwritten if not in debug mode
			String instanceIP = "localhost";
			String instanceID = "i-0927c392dd954b616";
			return new Local(instanceID, instanceIP);
		}
		else {
			synchronized (instances) {
				if (instances.isEmpty()) {
					instances.addAll(AwsEc2Manager.getAllRunningInstances());

					for (Instance inst : instances) {
						instanceRequests.putIfAbsent(inst.instanceId(), new HashMap<>());
					}

					if (instances.isEmpty()) {

						if (isCurrentlyDeploying.compareAndSet(false, true)) {
							System.out.println("No instances available, deploying new instance");
							AutoScaler.deployNewInstance();
							isCurrentlyDeploying.set(false);
						} else {
							System.out.println("No instance available, but another thread is already deploying it");
						}
					}
				}
			}
			if (!instances.isEmpty()) {
				//print_current_loads();

				Optional<Instance> chosen_instance;
				synchronized (instances) {
					chosen_instance = getLeastBusyInstance();
				}

				if (chosen_instance.isEmpty()) {
					// check if we don't already have the maximum number of instances
					if (instances.size() < AutoScaler.MAX_INSTANCES) {
						if (isCurrentlyDeploying.compareAndSet(false, true)) {
							System.out.println("All instances are full, deploying new instance");
							new Thread(() -> {
								try {
									AutoScaler.deployNewInstance();
								} finally {
									isCurrentlyDeploying.set(false);
								}
							}).start();
						} else {
							System.out.println("Another thread is already deploying an instance");
						}
					}
					return new Lambda();
				} else {
					System.out.println("Instance: " + chosen_instance.get().instanceId() + " fine and available for request");
					return new EC2(chosen_instance.get());
				}
			} else {
				throw new RuntimeException("No instances available, while there should always be at least one instance available");
			}
		}
	}

	private HttpURLConnection forwardRequest(HttpExchange exchange, String requestBody, RequestEstimation estimation,
			AbstractRequestType requestType, String instanceIP, String instanceID) throws IOException {
		// URL of local workerWebServer
		String uri = exchange.getRequestURI().getPath();
		String query = exchange.getRequestURI().getQuery();
		if (query != null) {
			uri += "?" + query;
		}

		URL url = new URL("http", instanceIP, 8000, uri);
		System.out.println("Handling request: " + uri);
		return HttpRequestUtils.forwardRequest(url, exchange, requestBody);

	}
	private void forwardLambdaRequestAndSendToUser(HttpExchange exchange, String requestBody, RequestEstimation estimation, AbstractRequestType requestType) throws IOException {
		// Use estimation later to do forward logic
		System.out.println("Redirecting request to Lambda function");
		String formattedResponse = "";
		String lambdaFunctionName = "";

		if (requestType instanceof BlurImageRequest) {
			lambdaFunctionName = "blur-service";
		}
		else if (requestType instanceof EnhanceImageRequest) {
			lambdaFunctionName = "enhance-service";
		}
		else if (requestType instanceof RayTracerRequest) {
			lambdaFunctionName = "tracer-service";
		}

		if (requestType instanceof BlurImageRequest || requestType instanceof EnhanceImageRequest) {
			// Construct the payload
			String base64Image = extractBase64Data(requestBody);
			String imageType = extractImageType(requestBody);


			String payload = "{ \"body\": \"" + base64Image + "\", \"fileFormat\": \"" + imageType + "\" }";
			String lambdaResponse = AwsEc2Manager.invokeLambdaFunction(lambdaFunctionName, payload);
			formattedResponse = formatLambdaResponse(lambdaResponse, imageType);

		}
		else if (requestType instanceof RayTracerRequest) {
			if (requestBody == null || requestBody.isEmpty()) {
				throw new IOException("Request body is empty for RayTracerRequest");
			}

			System.out.println(requestBody);

			// Extract query parameters
			Map<String, String> queryParams = extractQueryParams(exchange.getRequestURI().getQuery());

			// Extract the scene and texmap from the request body
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> body = mapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {});

			byte[] input = ((String) body.get("scene")).getBytes(StandardCharsets.UTF_8);
			byte[] texmap = null;
			if (body.containsKey("texmap")) {
				ArrayList<Integer> texmapBytes = (ArrayList<Integer>) body.get("texmap");
				texmap = new byte[texmapBytes.size()];
				for (int i = 0; i < texmapBytes.size(); i++) {
					texmap[i] = texmapBytes.get(i).byteValue();
				}
			} else {
				texmap = new byte[0];  // handle case where texmap is missing or empty
			}
			// Construct the JSON payload
			String jsonPayload = "{"
					+ "\"aa\": \"" + queryParams.getOrDefault("aa", "false") + "\", "
					+ "\"multi\": \"false\", "
					+ "\"scols\": \"" + queryParams.getOrDefault("scols", "400") + "\", "
					+ "\"srows\": \"" + queryParams.getOrDefault("srows", "300") + "\", "
					+ "\"wcols\": \"" + queryParams.getOrDefault("wcols", "400") + "\", "
					+ "\"wrows\": \"" + queryParams.getOrDefault("wrows", "300") + "\", "
					+ "\"coff\": \"" + queryParams.getOrDefault("coff", "0") + "\", "
					+ "\"roff\": \"" + queryParams.getOrDefault("roff", "0") + "\", "
					+ "\"input\": \"" + Base64.getEncoder().encodeToString(input) + "\", "
					+ "\"texmap\": \"" + Base64.getEncoder().encodeToString(texmap) + "\""
					+ "}";

			// Invoke Lambda function directly
			String lambdaResponse = AwsEc2Manager.invokeLambdaFunction(lambdaFunctionName, jsonPayload);
			formattedResponse = formatLambdaResponse(lambdaResponse, "json");

		}

		// Send the response back to the client
		byte[] responseBytes = formattedResponse.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(200, responseBytes.length);
		OutputStream os = exchange.getResponseBody();
		os.write(responseBytes);
		os.close();
	}

	public static void addRequestEstimation(String instanceId, AbstractRequestType request, RequestEstimation estimation) {
        synchronized (instanceRequests) {
            instanceRequests
                .computeIfAbsent(instanceId, k -> new ConcurrentHashMap<>())
                .put(request, estimation);
        }
    }
	public static void removeRequestEstimation(String instanceId, AbstractRequestType request) {
        synchronized (instanceRequests) {
            Map<AbstractRequestType, RequestEstimation> estimations = instanceRequests.get(instanceId);
            if (estimations != null) {
                estimations.remove(request);
            }
        }
    }

	public Map<String, String> queryToMap(String query) {
		if (query == null) {
			return null;
		}
		Map<String, String> result = new HashMap<>();
		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				result.put(entry[0], entry[1]);
			} else {
				result.put(entry[0], "");
			}
		}
		return result;
	}

	private static void print_current_loads() {
		synchronized (instanceRequests) {
			// print for each instance the current requests and their associated cpu time
			for (Map.Entry<String, Map<AbstractRequestType, RequestEstimation>> entry : instanceRequests.entrySet()) {
				System.out.println("Instance: " + entry.getKey() + " has the following requests:");
				for (Map.Entry<AbstractRequestType, RequestEstimation> reqEntry : entry.getValue().entrySet()) {
					System.out.println("Request: " + reqEntry.getKey().getClass().getSimpleName() + " has cpu time: " + reqEntry.getValue().cpuTime);
				}
			}
		}
	}

	// filter instances that are not full, then find the one with minimal cpu usage
	private static Optional<Instance> getLeastBusyInstance() {
		return instances.stream()
			.filter(inst -> instanceRequests.get(inst.instanceId()).size() < REQUEST_COUNT_MAX)
			.min(Comparator.comparingLong(LoadBalancer::getWeightedComplexity));
	}

	// Function that gives total cpu and memory complexity given instance using weighted heuristic
	private static long getWeightedComplexity(Instance instance) {
		long totalCpu = 0;
		long totalMemory = 0;
		if (!instanceRequests.containsKey(instance.instanceId())) {
			instanceRequests.put(instance.instanceId(), new HashMap<>());
		}
		for (Map.Entry<AbstractRequestType, RequestEstimation> entry : instanceRequests.get(instance.instanceId()).entrySet()) {
			totalCpu += entry.getValue().cpuTime;
			totalMemory += entry.getValue().memory;
		}
		return totalCpu + 2 * totalMemory;
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

	private Map<String, String> extractQueryParams(String query) {
		Map<String, String> queryParams = new HashMap<>();
		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				queryParams.put(entry[0], entry[1]);
			} else {
				queryParams.put(entry[0], "");
			}
		}
		return queryParams;
	}
	// Helper method to extract base64 encoded data from requestBody
	private String extractBase64Data(String requestBody) {
		int startIndex = requestBody.indexOf("base64,") + 7;
		return requestBody.substring(startIndex);
	}

	// Helper method to extract image type from requestBody
	private String extractImageType(String requestBody) {
		int startIndex = requestBody.indexOf("data:image/") + 11;
		int endIndex = requestBody.indexOf(";base64,");
		return requestBody.substring(startIndex, endIndex);
	}

	private String formatLambdaResponse(String lambdaResponse, String imageType) {
		// Remove quotes from the response
		String base64Image = lambdaResponse.replace("\"", "");

		// Prepend the appropriate header
		return "data:image/" + imageType + ";base64," + base64Image;
	}
}
