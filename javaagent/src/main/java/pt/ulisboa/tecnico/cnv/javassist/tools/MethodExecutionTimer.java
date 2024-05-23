package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import javassist.ClassPool;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;

import java.util.concurrent.atomic.AtomicLong;


public class MethodExecutionTimer extends CodeDumper {

    public static final AtomicMetrics metrics = new AtomicMetrics();

    public MethodExecutionTimer(List<String> packageNameList, String writeDestination) {
        super(packageNameList, writeDestination);
    }

    @Override
    protected void transform(CtBehavior behavior) throws Exception {
        super.transform(behavior);

        

        // Return metrics through http:
        if (behavior instanceof CtMethod && behavior.getName().equals("handle")) {

            //startAllocatedMemory
            behavior.addLocalVariable("startAllocatedMemory", CtClass.longType);
            behavior.insertBefore("startAllocatedMemory = ((com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean()).getCurrentThreadAllocatedBytes();");
            behavior.addLocalVariable("endAllocatedMemory", CtClass.longType);
            behavior.addLocalVariable("allocatedMemory", CtClass.longType);


            behavior.addLocalVariable("startCpuTime", CtClass.longType);

            behavior.insertBefore("startCpuTime = java.lang.management.ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();");
            StringBuilder builder = new StringBuilder();

            behavior.addLocalVariable("endCpuTime", CtClass.longType);
            behavior.addLocalVariable("cpuTime", CtClass.longType); // Does not include http stuff


            behavior.instrument(new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("sendResponseHeaders")) {
                        m.replace("{"
                            // allocated memory
                            // TODO: think about: If we keep the service on, the second time we run the raytracer for example, it allocates a lot less memory, since it was still allocated, what do we think of this?
                            + " endAllocatedMemory = ((com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean()).getCurrentThreadAllocatedBytes();\n"
                            + " allocatedMemory = endAllocatedMemory - startAllocatedMemory;\n"

                            //cpu
                            + " endCpuTime = java.lang.management.ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();\n"
                            + " cpuTime = endCpuTime - startCpuTime;\n"

                            + MethodExecutionTimer.class.getName() + ".metrics.allocatedMemory.addAndGet(allocatedMemory);\n"
                            + " System.out.println(\"Allocated memory all threads: \" + " + MethodExecutionTimer.class.getName() + ".metrics.allocatedMemory);\n"
                            + " System.out.println(\"Allocated memory main thread: \" + allocatedMemory);\n"

                            + MethodExecutionTimer.class.getName() + ".metrics.totalCpuTime.addAndGet(cpuTime);\n"
                            + " System.out.println(\"Total time all threads : \" + " + MethodExecutionTimer.class.getName() + ".metrics.totalCpuTime);\n"
                            + " System.out.println(\"Total time main thread : \" + cpuTime);\n"

                            + "    $0.getResponseHeaders().add(\"methodCpuExecutionTimeNs\", \"\" + " + MethodExecutionTimer.class.getName() + ".metrics.totalCpuTime);\n"
                            + "    $0.getResponseHeaders().add(\"methodMemoryAllocatedBytes\", \"\" + " + MethodExecutionTimer.class.getName() + ".metrics.allocatedMemory);\n"
                            + "    $proceed($$);"
                            + "}");
                    }
                }
            });
        }

        // Assumes that totalCpuTime is a static field in the class, and that the class has a handle method
        // Supply custom thread factory to threadpoolexecutor, no need to check if we are in raytracer, since others dont have this threadpoolexecutor thing
        behavior.instrument(new ExprEditor() {
            public void edit(NewExpr e) throws CannotCompileException {
                if (e.getClassName().equals("java.util.concurrent.ThreadPoolExecutor")) {

                    try {
                        behavior.addLocalVariable("myThreadFactory", ClassPool.getDefault().get("pt.ulisboa.tecnico.cnv.javassist.tools.MyThreadFactory"));
                    } catch (NotFoundException ex) {
                        
                        ex.printStackTrace();
                    }

                    e.replace("{"
                    + "$_ = $proceed($$);\n"
                    + MyThreadFactory.class.getName() + " myThreadFactory = new " + MyThreadFactory.class.getName() + "(" + MethodExecutionTimer.class.getName() + ".metrics);\n"
                    + "$_.setThreadFactory(myThreadFactory);\n"
                    + "}");
                    
                }
            }
        });

        // builder.append(String.format("System.out.println(\"[%s] %s method call completed in: \" + opTime + \" ns!\");",
        //         this.getClass().getSimpleName(), behavior.getLongName()));
        // builder.append(String.format("System.out.println(\"[%s] %s method call completed in: \" + cpuTime + \" ns!\");",
        //         this.getClass().getSimpleName(), behavior.getLongName()));
        // behavior.insertAfter(builder.toString());
    }
}
