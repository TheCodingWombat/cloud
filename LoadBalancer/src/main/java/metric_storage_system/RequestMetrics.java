package metric_storage_system;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import utils.CSVSerializable;

/**
 * Holds the metrics for a request. Every instance of this class is associated with one AbstractRequestType
 */
public class RequestMetrics implements CSVSerializable {

	private static int gobalRequestCounter = 0;

	private final long cpuTime;
    private final long memory;
    private final int requestCounter;
    
    public RequestMetrics(long cpuTime, long memory) {
    	gobalRequestCounter++;

    	this.cpuTime = cpuTime;
        this.memory = memory;
        requestCounter = gobalRequestCounter;
    }
    
	public static RequestMetrics extractMetrics(HttpURLConnection connection) {
		Map<String, List<String>> headers = connection.getHeaderFields();
		long memory = Long.parseLong(headers.get("Methodmemoryallocatedbytes").get(0));
		long cpuTime = Long.parseLong(headers.get("Methodcpuexecutiontimens").get(0));
		return new RequestMetrics(cpuTime, memory);
	}

    // Returns the metrics as an array of doubles for use in a machine learning model
    public double[] toYArray() {
        return new double[]{cpuTime, memory};
    }


	public String toJson(){
		return "{\n" +
				"  \"cpuTime\": " + cpuTime + ",\n" +
				"  \"memory\": " + memory + "\n" +
				"}";
	}
	@Override
	public String serializeCsv() {
		return cpuTime + ";" + memory + ";" + requestCounter;
	}
}
