package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.List;

import javassist.CtBehavior;
import javassist.bytecode.AccessFlag;

public class MethodAnalyser extends AbstractJavassistTool {

    public MethodAnalyser(List<String> packageNameList, String writeDestination) {
        super(packageNameList, writeDestination);
    }

    public static boolean isStatic(int accflags) {
        return (accflags & AccessFlag.STATIC) != 0;
    }

    @Override
    protected void transform(CtBehavior behavior) throws Exception {
        super.transform(behavior);
        String modifiers = "";
        switch (behavior.getModifiers()) {
        case AccessFlag.PRIVATE:
            modifiers = "private";
            break;
        case AccessFlag.PUBLIC:
            modifiers = "public";
            break;
        case AccessFlag.PROTECTED:
            modifiers = "protected";
            break;
        default:
            modifiers = "(package)";
            break;
        }

        if (isStatic(behavior.getModifiers())) {
            modifiers += " static";
        }

        System.out.println(String.format("[%s] %s %s", MethodAnalyser.class.getSimpleName(), modifiers, behavior.getLongName()));
    }
}
