package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.Runnable;

class MyThreadFactory implements ThreadFactory {
    public AtomicLong totalCpuTime;

    public MyThreadFactory(AtomicLong totalCpuTime) {
        this.totalCpuTime = totalCpuTime;
    }

    @Override
    public Thread newThread(Runnable r) {
        System.out.println("Creating a raytracer thread yaay");
        return new MyThread(r, totalCpuTime);
    }
}
