package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.Runnable;

public class MyThreadFactory implements ThreadFactory {
    public AtomicMetrics metrics;

    public MyThreadFactory(AtomicMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new MyThread(r, metrics);
    }
}
