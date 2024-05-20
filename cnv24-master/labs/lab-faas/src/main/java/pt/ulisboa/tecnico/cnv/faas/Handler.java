package pt.ulisboa.tecnico.cnv.faas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, String>, Integer> {

	private int factorial(int n ) {
		int fact = 1;
		for(int i = 1; i <= n; i++){
		      fact = fact * i;
		  }
		return fact;
	}

    @Override
    public Integer handleRequest(Map<String,String> event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Input: " + event.get("number"));
        return factorial(Integer.valueOf(event.get("number")));
    }
}