package metric_storage_system;

import java.util.HashMap;

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

	}

	public static RequestEstimation calculateEstimation(AbstractRequestType requestType) {
		return null;
	}
}