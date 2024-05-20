package metric_storage_system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import request_types.AbstractRequestType;
import request_types.BlurImageRequest;

/**
 * This class will hold the data that were transfered from the instrumented classes and will return the calculated results for a new request.
 */
public class MetricStorageSystem {
	
	ArrayList<HashMap<BlurImageRequest, RequestMetrics>> blureImageMetrics = new ArrayList<HashMap<BlurImageRequest,RequestMetrics>>(); 
	
	public void storeMetric(AbstractRequestType requestType, RequestMetrics metric) {
		
	}

	public static RequestEstimation calculateEstimation(AbstractRequestType requestType) {
		
		return null;
		
	}
	
	
}
