package metric_storage_system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import request_types.AbstractRequestType;
import request_types.ImageProcessingRequest;

/**
 * This class will hold the data that were transfered from the instrumented classes and will return the calculated results for a new request.
 */
public class MetricStorageSystem {
	// TODO: WHy is it an arraylist of a map? Why not just a hashmap? Current implementation will be a list of single  element maps
	static ArrayList<HashMap<ImageProcessingRequest, RequestMetrics>> blureImageMetrics = new ArrayList<HashMap<ImageProcessingRequest,RequestMetrics>>(); 
	
	// TODO: Shouldn't we just have a class for this for each metric type?
	public static void storeMetric(AbstractRequestType requestType, RequestMetrics metric) {
		if(requestType instanceof ImageProcessingRequest) {
			HashMap<ImageProcessingRequest, RequestMetrics> map = new HashMap<ImageProcessingRequest, RequestMetrics>();
			map.put((ImageProcessingRequest) requestType, metric);
			blureImageMetrics.add(map);
		}
	}

	public static RequestEstimation calculateEstimation(AbstractRequestType requestType) {
		
		return null;
		
	}
	
	
}
