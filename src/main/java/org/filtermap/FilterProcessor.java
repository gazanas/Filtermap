package org.filtermap;

import org.filtermap.annotations.Filter;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

@SupportedAnnotationTypes({"org.filtermap.annotations.Filter"})
public class FilterProcessor extends AbstractProcessor {

    Filer filer;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element object : roundEnv.getElementsAnnotatedWith(Filter.class)) {
            try {
                PackageElement packageElement = (PackageElement) object.getEnclosingElement();
                JavaFileObject file = filer.createSourceFile(packageElement.getQualifiedName() +"."+ object.getSimpleName()+"Filter");
                BufferedWriter bufferedWriter = new BufferedWriter(file.openWriter());

                List<String> orFilters = Arrays.asList(object.getAnnotation(Filter.class).filters());

                bufferedWriter.append("package " + packageElement.getQualifiedName().toString() + ";");

                bufferedWriter.newLine();
                bufferedWriter.newLine();

                bufferedWriter.append("import org.filtermap.AbstractFilter;\n" +
                        "import java.util.HashMap;\n" +
                        "import java.util.List;\n" +
                        "import java.util.Arrays;\n" +
                        "import java.util.Map;");

                bufferedWriter.newLine();
                bufferedWriter.newLine();

                bufferedWriter.append("public class "+ object.getSimpleName().toString()+"Filter implements AbstractFilter {");

                bufferedWriter.newLine();
                bufferedWriter.newLine();

                Map<String, FilterInformation> filterInformation = new HashMap<>();
                for (String filter : orFilters) {
                    filterInformation.put(
                            filter,
                            FilterInformation.forFilter(processingEnv, object, filter)
                    );
                }

                for (String filter : orFilters) {

                    bufferedWriter.append("\tpublic List<"+filterInformation.get(filter).getType()+"> " + filterInformation.get(filter).getName() + ";");
                    bufferedWriter.newLine();
                }

                bufferedWriter.newLine();

                bufferedWriter.append("\tpublic HashMap<String, HashMap> filterMap = new HashMap();");

                bufferedWriter.newLine();
                bufferedWriter.newLine();

                for (String filter : orFilters) {
                    bufferedWriter.append("\tpublic List<"+filterInformation.get(filter).getType()+"> get" + filterInformation.get(filter).getName().substring(0, 1).toUpperCase() + filterInformation.get(filter).getName().substring(1) + "() {");
                    bufferedWriter.newLine();
                    bufferedWriter.append("\t\treturn this."+filterInformation.get(filter).getName()+";");
                    bufferedWriter.newLine();
                    bufferedWriter.append("\t}");
                    bufferedWriter.newLine();
                    bufferedWriter.newLine();

                    bufferedWriter.append("\tpublic void set" + filterInformation.get(filter).getName().substring(0,1).toUpperCase() + filterInformation.get(filter).getName().substring(1) + "(List<"+filterInformation.get(filter).getType()+"> "+filterInformation.get(filter).getName()+") {");
                    bufferedWriter.newLine();
                    bufferedWriter.append("\t\tthis."+filterInformation.get(filter).getName()+" = "+filterInformation.get(filter).getName()+";");
                    bufferedWriter.newLine();
                    bufferedWriter.append("\t\tfilterMap.put(\""+filterInformation.get(filter).getName()+"\", new HashMap() {{ " +
                            "\n\t\t\tput(\"value\", "+filterInformation.get(filter).getName()+");" +
                            "\n\t\t\tput(\"filter\", Arrays.asList(new String[] {"+String.join(",", filterInformation.get(filter).getVariablePath().stream().map(f -> "\""+f+"\"").toArray(String[]::new))+"}));" +
                            "\n\t\t\tput(\"type\", Arrays.asList(new Class[]{"+String.join(",", filterInformation.get(filter).getClassPath().stream().map(f -> f+".class").toArray(String[]::new))+"})); }}); ");

                    bufferedWriter.newLine();
                    bufferedWriter.append("\t}");
                    bufferedWriter.newLine();
                    bufferedWriter.newLine();
                }

                bufferedWriter.newLine();

                bufferedWriter.append("\t public Map getFilterMap() {");

                bufferedWriter.newLine();

                bufferedWriter.append("\t\treturn filterMap;");
                bufferedWriter.newLine();
                bufferedWriter.append("\t}");
                bufferedWriter.newLine();
                bufferedWriter.append("}");

                bufferedWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;

    }
}
