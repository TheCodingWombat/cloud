package load_balancer;

import software.amazon.awssdk.services.ec2.model.Instance;

public abstract class InstanceType {
}

class Lambda extends InstanceType {
}

class EC2 extends InstanceType {
    private final Instance instance;

    public EC2(Instance instance) {
        this.instance = instance;
    }

    public Instance getInstance() {
        return instance;
    }
}

class Local extends InstanceType {
    // isntanceId and ip
    private final String instanceId;
    private final String instanceIp;

    public Local(String instanceId, String instanceIp) {
        this.instanceId = instanceId;
        this.instanceIp = instanceIp;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getInstanceIp() {
        return instanceIp;
    }
}