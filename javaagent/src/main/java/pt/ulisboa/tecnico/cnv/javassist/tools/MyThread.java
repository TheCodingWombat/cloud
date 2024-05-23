package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.concurrent.atomic.AtomicLong;
import java.lang.Runnable;
import java.lang.Thread;

public class MyThread extends Thread {
    public AtomicMetrics metrics;

    public MyThread(Runnable r, AtomicMetrics metrics) {
        super(r);
        this.metrics = metrics;
    }

    @Override
    public void run() {
        super.run();
        
        metrics.totalCpuTime.addAndGet(java.lang.management.ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime());
        metrics.allocatedMemory.addAndGet(((com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean()).getCurrentThreadAllocatedBytes());
    }
}
