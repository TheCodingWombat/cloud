package metric_storage_system;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import deployment_manager.AwsEc2Manager;
import load_balancer.LoadBalancer;
import request_types.AbstractRequestType;
import request_types.ImageProcessingRequest;

import utils.PictureFormat;

/**
 * This class will hold the data that were transfered from the instrumented
 * classes and will return the calculated results for a new request.
 */
public class MetricStorageSystem {
	// TODO: WHy is it an arraylist of a map? Why not just a hashmap? Current implementation will be a list of single  element maps
	static HashMap<AbstractRequestType, RequestMetrics> metrics = new ConcurrentHashMap<AbstractRequestType,RequestMetrics>(); 
	
	static MultipleOutputLinearModel blurImagePNGModel = new MultipleOutputLinearModel(2);
	static MultipleOutputLinearModel blurImageJPEGModel = new MultipleOutputLinearModel(2);

	// TODO: Shouldn't we just have a class for this for each metric type?
	public static void storeMetric(AbstractRequestType requestType, RequestMetrics requestMetrics) {
		metrics.put(requestType, requestMetrics);
		CsvExporter.mapToCsv(metrics);

		System.out.println("Storing metrics");

		// TODO: Batch metrics and do not refit on every request.
		if(requestType instanceof ImageProcessingRequest) {
			if (((ImageProcessingRequest) requestType).pictureFormat == utils.PictureFormat.PNG) {
				blurImagePNGModel.refit(requestType.toXArray(), requestMetrics.toYArray());
			} else if (((ImageProcessingRequest) requestType).pictureFormat == utils.PictureFormat.JPEG) {
				blurImageJPEGModel.refit(requestType.toXArray(), requestMetrics.toYArray());
			}
		}
		//Implement db store
		// Get the current timestamp
		String timestamp = Instant.now().toString();
		// Store the metric in DynamoDB
		// Serialize the requestType to JSON
		String requestTypeJson = requestType.toJson();
		String metricJson = requestMetrics.toJson();

		if (!LoadBalancer.DEBUG) {
			AwsEc2Manager.storeMetricInDynamoDB(timestamp, requestTypeJson, metricJson);
		}
	}

	public static RequestEstimation calculateEstimation(AbstractRequestType requestType) {
		
		try {
			MultipleOutputLinearModel model = chooseModel(requestType); // Chooses blur image png linear regression model for blur image png for example, and jpeg linear regression model for blur image jpeg
			double[] outputs = model.predict(requestType.toXArray()); // Predicts the metrics for the request type

			RequestEstimation estimation = new RequestEstimation((long) outputs[0], (long) outputs[1]);

			System.out.println("CPU time estimation: "+ estimation.cpuTime);
			System.out.println("Memory estimation: "+ estimation.memory);

			return estimation;
		} catch (Exception e) {
			System.out.println("Cannot make CPU and memory estimation");
			e.printStackTrace();
		}

		return new RequestEstimation(); 
		
	}

	

	public static MultipleOutputLinearModel chooseModel(AbstractRequestType requestType) {
		if (requestType instanceof ImageProcessingRequest) { //TODO: Assume for now only blur images
			if (((ImageProcessingRequest) requestType).pictureFormat == utils.PictureFormat.PNG) {
				return blurImagePNGModel;
			} else if (((ImageProcessingRequest) requestType).pictureFormat == utils.PictureFormat.JPEG) {
				return blurImageJPEGModel;
			}

			System.out.println("Picture format not supported");
		}

		throw new IllegalArgumentException("Request type not supported");
	}
}