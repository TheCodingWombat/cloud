package request_types;

import com.sun.net.httpserver.HttpExchange;

public class EnhanceImageRequest extends  ImageProcessingRequest{

    public EnhanceImageRequest(HttpExchange exchange, String requestBody) {
        super(exchange, requestBody);
    }
}
