package request_types;

import com.sun.net.httpserver.HttpExchange;

public class BlurImageRequest extends ImageProcessingRequest {
    public BlurImageRequest(HttpExchange exchange, String requestBody) {
        super(exchange, requestBody);
    }

}
