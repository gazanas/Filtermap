package org.filtermap;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilterInformation {

    private List<String> classPath;

    private List<String> variablePath;

    private String type;

    private String name;

    private Integer i = 0;

    public FilterInformation(ProcessingEnvironment processingEnvironment, Element object, String field) {
        this.initializeInformation(processingEnvironment, object, field.split("\\."));
    }

    private void initializeInformation(ProcessingEnvironment processingEnvironment, Element object, String[] fieldPath) {
        this.classPath = this.initializeClassPath(processingEnvironment, object, fieldPath);
        this.variablePath = Arrays.asList(fieldPath);
        this.type = this.classPath.get(this.classPath.size() - 1);
        this.name = this.variablePath.get(this.variablePath.size() - 1);
    }

    private List initializeClassPath(ProcessingEnvironment processingEnvironment, Element object, String[] fieldPath) {
        if (i > fieldPath.length-1) {
            return Arrays.asList();
        }
        String field = fieldPath[i];
        i = i + 1;

        return (List) object.getEnclosedElements().stream()
                .filter(property -> property.getKind().isField())
                .filter(property -> property.getSimpleName().toString().equals(field))
                .flatMap(property -> Stream.concat(Stream.of(property.asType().toString().split("\\.")[property.asType().toString().split("\\.").length-1]),
                        initializeClassPath(processingEnvironment, processingEnvironment.getTypeUtils().asElement(property.asType()), fieldPath).stream()))
                .collect(Collectors.toList());
    }

    public static FilterInformation forFilter(ProcessingEnvironment processingEnvironment, Element object, String field) {
        return new FilterInformation(processingEnvironment, object, field);
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public List<String> getVariablePath() {
        return variablePath;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
