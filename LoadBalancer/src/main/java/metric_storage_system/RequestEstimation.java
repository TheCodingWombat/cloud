package metric_storage_system;

/**
 * Instances of this class are results of the calculation of the MetricStorageSystem.
 * The field could be used by the LoadBalancer to make a decision how the next request should be performed. 
 */
public class RequestEstimation {
    public long cpuTime;
    public long memory;
    public boolean empty = false;

    public RequestEstimation(long cpuTime, long memory) {
        this.cpuTime = cpuTime;
        this.memory = memory;
    }

    // Empty request estimation. TODO: OOP
    public RequestEstimation() {
        this.empty = true;
    }
    
}
