package request_types;

import com.sun.net.httpserver.HttpExchange;

import utils.CSVSerializable;

/**
 * Abstract Type of Request Type. For every type of request there is a derived class.
 * These classes hold the parameters of the request as fields. The instances can be used to request against the MetricStroageSystem.
 * The system then will return estimations about the cpu-utilization and memory need of the request. 
 * This can be used by the LoadBalancre to define a strategy how to handle the request.
 */
public abstract class AbstractRequestType implements CSVSerializable {

	public static AbstractRequestType ofRequest(HttpExchange exchange, String requestBody) {
		String uriPath = exchange.getRequestURI().getPath();
		if (uriPath.equals("/blurimage")) {
			return new ImageProcessingRequest(exchange, requestBody);
		} else if (uriPath.equals("/enhanceimage")) {
			return new ImageProcessingRequest(exchange, requestBody);
		} else if (uriPath.equals("/raytracer")) {
			return new RayTracerRequest(exchange, requestBody);
		} else {
			//maybe add error handling here
			return null;
		}
	}

	// Returns the parameters of the request as an array of doubles to be used in a machine learning model
	public abstract double[] toXArray();

	public abstract String toJson();
}