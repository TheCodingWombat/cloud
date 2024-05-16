package pt.ulisboa.tecnico.cnv.javassist.tools;

import java.util.List;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

public class ClassAnalyser extends AbstractJavassistTool {

    public ClassAnalyser(List<String> packageNameList, String writeDestination) {
        super(packageNameList, writeDestination);
    }

    private void log(String msg) {
        System.out.println(String.format("[%s] %s", this.getClass().getSimpleName(), msg));
    }

    @Override
    protected void transform(CtClass clazz) throws Exception {
        log(String.format("class: %s", clazz.getName()));
        log(String.format("extends: %s", clazz.getSuperclass().getName()));
        for (CtClass interfaces : clazz.getInterfaces()) {
            log(String.format("implements: %s", interfaces.getName()));
        }
        for (CtField field : clazz.getFields()) {
            log(String.format("field: %s", field.getName()));
        }
        for (CtMethod method : clazz.getMethods()) {
            log(String.format("method: %s", method.getLongName()));
        }
        super.transform(clazz);
    }

}
