package request_types;

import java.net.URI;

public abstract class AbstractRequestType {

	public static AbstractRequestType ofResouce(URI requestedUri) {
		if (requestedUri.equals("/blurimage")) {
			return new BlurImageRequest();
		} else if (requestedUri.equals("/enhanceimage")) {
			return new EnhanceImageRequest();
		} else if (requestedUri.equals("/raytracer")) {
			return new RayTracerRequest();
		} else {
			//maybe add error handling here
			return null;
		}
	}
}