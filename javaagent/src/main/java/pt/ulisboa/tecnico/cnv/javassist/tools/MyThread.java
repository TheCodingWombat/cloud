package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.concurrent.atomic.AtomicLong;
import java.lang.Runnable;
import java.lang.Thread;

class MyThread extends Thread {
    public AtomicLong totalCpuTime;

    public MyThread(Runnable r, AtomicLong totalCpuTime) {
        super(r);
        this.totalCpuTime = totalCpuTime;
    }

    @Override
    public void run() {

        super.run();
        
        totalCpuTime.addAndGet(java.lang.management.ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime());
    }
}
