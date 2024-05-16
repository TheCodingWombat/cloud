package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class MethodExecutionTimer extends CodeDumper {

    public MethodExecutionTimer(List<String> packageNameList, String writeDestination) {
        super(packageNameList, writeDestination);
    }

    @Override
    protected void transform(CtBehavior behavior) throws Exception {
        super.transform(behavior);
        behavior.addLocalVariable("startTime", CtClass.longType);
        behavior.insertBefore("startTime = System.nanoTime();");
        StringBuilder builder = new StringBuilder();
        behavior.addLocalVariable("endTime", CtClass.longType);
        behavior.addLocalVariable("endTime2", CtClass.longType);
        behavior.addLocalVariable("opTime", CtClass.longType);
        behavior.addLocalVariable("opTime2", CtClass.longType); // this does not include the time for sending the http stuff


        // Return metrics through http:
        if (behavior instanceof CtMethod && behavior.getName().equals("handle")) {
            behavior.instrument(new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("sendResponseHeaders")) {
                        m.replace("{"
                            + " System.out.println(\"yes here\");\n"
                            + " endTime2 = System.nanoTime();\n"
                            + " opTime2 = endTime2-startTime;\n"
                            + "    $0.getResponseHeaders().add(\"methodExecutionTimeNs\", \"\" + opTime2);\n"
                            + "    $proceed($$);"
                            + "}");
                    }
                }
            });
        }

        builder.append("endTime = System.nanoTime();");
        builder.append("opTime = endTime-startTime;");
        builder.append(String.format("System.out.println(\"[%s] %s method call completed in: \" + opTime + \" ns!\");",
                this.getClass().getSimpleName(), behavior.getLongName()));
        behavior.insertAfter(builder.toString());
    }
}
