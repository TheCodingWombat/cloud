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
	// TODO: WHy is it an arraylist of a map? Why not just a hashmap? Current implementation will be a list of single  element maps
	static ArrayList<HashMap<BlurImageRequest, RequestMetrics>> blureImageMetrics = new ArrayList<HashMap<BlurImageRequest,RequestMetrics>>(); 
	
	MyModel blurImagePNGModel = new LinearModel();
	MyModel blurImageJPEGModel = new LinearModel();

	// TODO: Shouldn't we just have a class for this for each metric type?
	public static void storeMetric(AbstractRequestType requestType, RequestMetrics metrics) {
		if(requestType instanceof BlurImageRequest) {
			HashMap<BlurImageRequest, RequestMetrics> map = new HashMap<BlurImageRequest, RequestMetrics>();
			map.put((BlurImageRequest) requestType, metrics);
			blureImageMetrics.add(map);

			if (((BlurImageRequest) requestType).PictureFormat == BlurImageRequest.PictureFormatEnum.PNG) {
				blurImagePNGModel.refitModel(requestType.toXArray(), metrics.toYArray());
			}
		}
	}

	public static RequestMetrics calculateEstimation(AbstractRequestType requestType) {
		
		LinearModel model = chooseModel(requestType); // Chooses blur image png linear regression model for blur image png for example, and jpeg linear regression model for blur image jpeg
		RequestMetrics metrics = model.predict(requestType.getXData()); // Predicts the metrics for the request type

		return metrics;
	}

	public static MyModel chooseModel(AbstractRequestType requestType) {
		if (requestType instanceof BlurImageRequest) {
			if (((BlurImageRequest) requestType).PictureFormat == BlurImageRequest.PictureFormatEnum.PNG) {
				return blurImagePNGModel;
			} else if (((BlurImageRequest) requestType).PictureFormat == BlurImageRequest.PictureFormatEnum.JPEG) {
				return blurImageJPEGModel;
			}
		}
	}
	
	
}
