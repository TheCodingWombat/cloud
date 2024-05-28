package metric_storage_system;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Holds the metrics for a request. Every instance of this class is associated with one AbstractRequestType
 */
public class RequestMetrics {

    long cpuTime;
    long memory;

    public RequestMetrics(long cpuTime, long memory) {
        this.cpuTime = cpuTime;
        this.memory = memory;
    }
    
	public static RequestMetrics extractMetrics(HttpURLConnection connection) {
		Map<String, List<String>> headers = connection.getHeaderFields();

		long memory = Long.parseLong(headers.get("Methodmemoryallocatedbytes").get(0));
		System.out.println("Request memory: " + memory);

		long cpuTime = Long.parseLong(headers.get("Methodcpuexecutiontimens").get(0));
		System.out.println("Requestt cpuTime: " + cpuTime);

		return new RequestMetrics(cpuTime, memory);
	}

	public String toJson(){
		return "{\n" +
				"  \"cpuTime\": " + cpuTime + ",\n" +
				"  \"memory\": " + memory + "\n" +
				"}";
	}
}
