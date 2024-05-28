package metric_storage_system;

import java.util.HashMap;

import request_types.AbstractRequestType;

/**
 * This class will hold the data that were transfered from the instrumented
 * classes and will return the calculated results for a new request.
 */
public class MetricStorageSystem {
	// TODO: WHy is it an arraylist of a map? Why not just a hashmap? Current implementation will be a list of single  element maps
	static HashMap<AbstractRequestType, RequestMetrics> metrics = new HashMap<AbstractRequestType,RequestMetrics>(); 
	
	LinearModel blurImagePNGModel = new LinearModel();
	LinearModel blurImageJPEGModel = new LinearModel();

	// TODO: Shouldn't we just have a class for this for each metric type?
	public static void storeMetric(AbstractRequestType requestType, RequestMetrics metrics) {
		metrics.put(requestType, metrics);

		if(requestType instanceof ImageProcessingRequest) {
			if (((ImageProcessingRequest) requestType).PictureFormat == ImageProcessingRequest.PictureFormatEnum.PNG) {
				blurImagePNGModel.refitModel(requestType.toXArray(), metrics.toYArray());
			} else if (((ImageProcessingRequest) requestType).PictureFormat == ImageProcessingRequest.PictureFormatEnum.JPEG) {
				blurImageJPEGModel.refitModel(requestType.toXArray(), metrics.toYArray());
			}
		}
	}

	public static RequestMetrics calculateEstimation(AbstractRequestType requestType) {
		
		LinearModel model = chooseModel(requestType); // Chooses blur image png linear regression model for blur image png for example, and jpeg linear regression model for blur image jpeg
		RequestMetrics metrics = model.predict(requestType.getXData()); // Predicts the metrics for the request type

		return metrics;
	}

	public static MyModel chooseModel(AbstractRequestType requestType) {
		if (requestType instanceof ImageProcessingRequest) { //TODO: Assume for now only blur images
			if (((ImageProcessingRequest) requestType).PictureFormat == ImageProcessingRequest.PictureFormatEnum.PNG) {
				return blurImagePNGModel;
			} else if (((ImageProcessingRequest) requestType).PictureFormat == ImageProcessingRequest.PictureFormatEnum.JPEG) {
				return blurImageJPEGModel;
			}
		}
	}
}