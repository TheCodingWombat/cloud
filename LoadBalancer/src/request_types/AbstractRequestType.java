package request_types;

import com.sun.net.httpserver.HttpExchange;

public abstract class AbstractRequestType {

	public static AbstractRequestType ofRequest(HttpExchange exchange) {
		String uriPath = exchange.getRequestURI().getPath();
		if (uriPath.equals("/blurimage")) {
			return new BlurImageRequest(exchange);
		} else if (uriPath.equals("/enhanceimage")) {
			return new EnhanceImageRequest(exchange);
		} else if (uriPath.equals("/raytracer")) {
			return new RayTracerRequest(exchange);
		} else {
			//maybe add error handling here
			return null;
		}
	}
}