package request_types;

public abstract class AbstractRequestType {

	public static AbstractRequestType ofResouce(String resourceName) {
		if (resourceName.equals("/blurimage")) {
			return new BlurImageRequest();
		} else if (resourceName.equals("/enhanceimage")) {
			return new EnhanceImageRequest();
		} else if (resourceName.equals("/raytracer")) {
			return new RayTracerRequest();
		} else {
			//maybe add error handling here
			return null;
		}
	}
}