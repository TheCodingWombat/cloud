package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.List;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class HttpHandlerTransformer extends AbstractJavassistTool {

    public HttpHandlerTransformer(List<String> packageNameList, String writeDestination) {
        super(packageNameList, writeDestination);
    }

    @Override
    protected void transform(BasicBlock block) throws CannotCompileException {
        // No need to implement this if not needed for BasicBlock level transformation.
    }

    @Override
    protected void transform(CtBehavior behavior) throws Exception {
        if (behavior instanceof CtMethod && behavior.getName().equals("handle")) {
            behavior.instrument(new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("sendResponseHeaders")) {
                        m.replace("{"
                            + " System.out.println(\"yes here\");\n"
                            + "    $0.getResponseHeaders().add(\"Custom-Header\", \"Value:20\");\n"
                            + "    $proceed($$);"
                            + "}");
                    }
                }
            });
        }
    }
}
