package metric_storage_system;

import java.util.HashMap;

import request_types.AbstractRequestType;
import request_types.ImageProcessingRequest;

import utils.PictureFormat;

/**
 * This class will hold the data that were transfered from the instrumented
 * classes and will return the calculated results for a new request.
 */
public class MetricStorageSystem {
	// TODO: WHy is it an arraylist of a map? Why not just a hashmap? Current implementation will be a list of single  element maps
	static HashMap<AbstractRequestType, RequestMetrics> metrics = new HashMap<AbstractRequestType,RequestMetrics>(); 
	
	static MultipleOutputLinearModel blurImagePNGModel = new MultipleOutputLinearModel(2);
	static MultipleOutputLinearModel blurImageJPEGModel = new MultipleOutputLinearModel(2);

	// TODO: Shouldn't we just have a class for this for each metric type?
	public static void storeMetric(AbstractRequestType requestType, RequestMetrics requestMetrics) {
		metrics.put(requestType, requestMetrics);

		System.out.println("Storing metrics");

		// TODO: Batch metrics and do not refit on every request.
		if(requestType instanceof ImageProcessingRequest) {
			if (((ImageProcessingRequest) requestType).pictureFormat == utils.PictureFormat.PNG) {
				blurImagePNGModel.refit(requestType.toXArray(), requestMetrics.toYArray());
			} else if (((ImageProcessingRequest) requestType).pictureFormat == utils.PictureFormat.JPEG) {
				blurImageJPEGModel.refit(requestType.toXArray(), requestMetrics.toYArray());
			}
		}
	}

	public static RequestEstimation calculateEstimation(AbstractRequestType requestType) {
		
		MultipleOutputLinearModel model = chooseModel(requestType); // Chooses blur image png linear regression model for blur image png for example, and jpeg linear regression model for blur image jpeg
		double[] outputs = model.predict(requestType.toXArray()); // Predicts the metrics for the request type

		return new RequestEstimation((long) outputs[0], (long) outputs[1]);
	}

	public static MultipleOutputLinearModel chooseModel(AbstractRequestType requestType) {
		if (requestType instanceof ImageProcessingRequest) { //TODO: Assume for now only blur images
			if (((ImageProcessingRequest) requestType).pictureFormat == utils.PictureFormat.PNG) {
				return blurImagePNGModel;
			} else if (((ImageProcessingRequest) requestType).pictureFormat == utils.PictureFormat.JPEG) {
				return blurImageJPEGModel;
			}
		}

		throw new IllegalArgumentException("Request type not supported");
	}
}