package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicMetrics {
    public final AtomicLong totalCpuTime;
    public final AtomicLong allocatedMemory;

    public AtomicMetrics() {
        this.totalCpuTime = new AtomicLong(0L);
        this.allocatedMemory = new AtomicLong(0L);
    }
}