package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.List;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;

public class CodeDumper extends AbstractJavassistTool {

    public CodeDumper(List<String> packageNameList, String writeDestination) {
        super(packageNameList, writeDestination);
    }

    @Override
    protected void transform(CtClass clazz) throws Exception {
        System.out.println(String.format("[%s] Intercepting class %s", CodeDumper.class.getSimpleName(), clazz.getName()));
        super.transform(clazz);
    }

    @Override
    protected void transform(CtBehavior behavior) throws Exception {
        System.out.println(String.format("[%s] Intercepting method %s", CodeDumper.class.getSimpleName(), behavior.getName()));
        super.transform(behavior);
    }

    @Override
    protected void transform(BasicBlock block) throws CannotCompileException {
        System.out.println(String.format("[%s] Intercepting basicblock position=%s, length=%s, line=%s",
                CodeDumper.class.getSimpleName(), block.getPosition(), block.getLength(), block.getLine()));
        super.transform(block);
    }
}
