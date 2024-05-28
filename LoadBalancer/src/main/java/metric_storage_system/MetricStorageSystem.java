package metric_storage_system;

import java.time.Instant;
import java.util.HashMap;

import deployment_manager.AwsEc2Manager;
import load_balancer.LoadBalancer;
import request_types.AbstractRequestType;

/**
 * This class will hold the data that were transfered from the instrumented
 * classes and will return the calculated results for a new request.
 */
public class MetricStorageSystem {
	private static HashMap<AbstractRequestType, RequestMetrics> metrics = new HashMap<AbstractRequestType, RequestMetrics>();

	public static void storeMetric(AbstractRequestType requestType, RequestMetrics metric) {
		metrics.put(requestType, metric);
		CsvExporter.mapToCsv(metrics);

		//Implement db store
		// Get the current timestamp
		String timestamp = Instant.now().toString();
		// Store the metric in DynamoDB
		// Serialize the requestType to JSON
		String requestTypeJson = requestType.toJson();
		String metricJson = metric.toJson();

		if (LoadBalancer.DEBUG) {
			AwsEc2Manager.storeMetricInDynamoDB(timestamp, requestTypeJson, metricJson);
		}
	}

	public static RequestEstimation calculateEstimation(AbstractRequestType requestType) {
		return null;
	}
}