package metric_storage_system;

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

}
