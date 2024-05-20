package pt.ulisboa.tecnico.cnv.javassist;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;

import pt.ulisboa.tecnico.cnv.javassist.tools.AbstractJavassistTool;

public class JavassistAgent {

    private static AbstractJavassistTool getTransformer(String toolName, List<String> packageNameList, String writeDestination) throws Exception {
        Class<?> transformerClass = Class.forName("pt.ulisboa.tecnico.cnv.javassist.tools." + toolName);
        return (AbstractJavassistTool) transformerClass.getDeclaredConstructor(List.class, String.class).newInstance(packageNameList, writeDestination);
    }

    /**
     * This method is invoked before the target 'main' method is invoked.
     * @param agentArgs
     * @param inst
     * @throws Exception
     */
    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        String[] argSplits = agentArgs.split(":");
        String toolName = argSplits[0];
        String packageNames = argSplits[1];
        String writeDestination = argSplits[2];
        List<String> packageNameList = Arrays.asList(packageNames.split(","));
        inst.addTransformer(getTransformer(toolName, packageNameList, writeDestination), true);
    }
}
