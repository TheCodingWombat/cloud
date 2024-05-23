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

    public static AtomicLong totalCpuTime = new AtomicLong(0L);

    public MethodExecutionTimer(List<String> packageNameList, String writeDestination) {
        super(packageNameList, writeDestination);
    }

    @Override
    protected void transform(CtBehavior behavior) throws Exception {
        super.transform(behavior);

        // Return metrics through http:
        if (behavior instanceof CtMethod && behavior.getName().equals("handle")) {
            //TODO: Do this only for raytracer, which we can check through http exchange object, which contains the handle used, in our case /raytracer


            //startAllocatedMemory
            behavior.addLocalVariable("startAllocatedMemory", CtClass.longType);
            behavior.insertBefore("startAllocatedMemory = ((com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean()).getCurrentThreadAllocatedBytes();");
            behavior.addLocalVariable("endAllocatedMemory", CtClass.longType);
            behavior.addLocalVariable("allocatedMemory", CtClass.longType);


            CtClass declaringClass = behavior.getDeclaringClass(); // TODO: probably need to remove this as field, only local variable in the future

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
                            // TODO: multithreaded
                            + " endAllocatedMemory = ((com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean()).getCurrentThreadAllocatedBytes();\n"
                            + " allocatedMemory = endAllocatedMemory - startAllocatedMemory;\n"
                            + " System.out.println(\"Allocated memory: \" + allocatedMemory);\n"

                            //cpu
                            + " endCpuTime = java.lang.management.ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();\n"
                            + " cpuTime = endCpuTime - startCpuTime;\n"
                            + MethodExecutionTimer.class.getName() + ".totalCpuTime.addAndGet(cpuTime);\n"
                            + " System.out.println(\"Total time all threads : \" + " + MethodExecutionTimer.class.getName() + ".totalCpuTime);\n"
                            + " System.out.println(\"Total time main thread : \" + cpuTime);\n"

                            // + "    $0.getResponseHeaders().add(\"methodCpuExecutionTimeNs\", \"\" + totalCpuTime);\n"
                            + "    $0.getResponseHeaders().add(\"methodMemoryAllocatedBytes\", \"\" + allocatedMemory);\n"
                            + "    $proceed($$);"
                            + "}");
                    }
                }
            });
        }

        // Assumes that totalCpuTime is a static field in the class, and that the class has a handle method
        // Supply custom thread factory to threadpoolexecutor
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
                    + MyThreadFactory.class.getName() + " myThreadFactory = new " + MyThreadFactory.class.getName() + "(" + MethodExecutionTimer.class.getName() + ".totalCpuTime);\n" // TODO REMOVE HARDCODED RAYTRACERHANDLER AND executor
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
