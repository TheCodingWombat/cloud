package request_types;

import com.sun.net.httpserver.HttpExchange;

/**
 * Abstract Type of Request Type. For every type of request there is a derived class.
 * These classes hold the parameters of the request as fields. The instances can be used to request against the MetricStroageSystem.
 * The system then will return estimations about the cpu-utilization and memory need of the request. 
 * This can be used by the LoadBalancre to define a strategy how to handle the request.
 */
public abstract class AbstractRequestType {

	public static AbstractRequestType ofRequest(HttpExchange exchange, String requestBody) {
		String uriPath = exchange.getRequestURI().getPath();
		if (uriPath.equals("/blurimage")) {
			return new BlurImageRequest(exchange, requestBody);
		} else if (uriPath.equals("/enhanceimage")) {
			return new EnhanceImageRequest(exchange, requestBody);
		} else if (uriPath.equals("/raytracer")) {
			return new RayTracerRequest(exchange, requestBody);
		} else {
			//maybe add error handling here
			return null;
		}
	}
}