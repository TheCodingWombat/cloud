package metric_storage_system;

/**
 * Holds the metrics for a request. Every instance of this class is associated with one AbstractRequestType
 */
public class RequestMetrics {

    long cpuTime;

    public RequestMetrics(long cpuTime) {
        this.cpuTime = cpuTime;
    }

}
