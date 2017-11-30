package com.github.genju83.simplepreference.compiler;

import com.github.genju83.simplepreference.SimplePreference;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static javax.lang.model.type.TypeKind.BOOLEAN;
import static javax.lang.model.type.TypeKind.FLOAT;
import static javax.lang.model.type.TypeKind.INT;
import static javax.lang.model.type.TypeKind.LONG;

public class SimplePreferenceProcessor extends AbstractProcessor {

    private static final ClassName classString = ClassName.get("java.lang", "String");
    private static final ClassName classContext = ClassName.get("android.content", "Context");
    private static final ClassName classSharedPreference = ClassName.get("android.content", "SharedPreferences");

    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    private List<TypeElement> collect(RoundEnvironment roundEnvironment, Class<? extends Annotation> annotation) {
        List<TypeElement> generateSignatureList = new ArrayList<>();
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotation);
        for (Element element : elements) {
            if (element.getKind() == ElementKind.CLASS && element instanceof TypeElement) {
                generateSignatureList.add((TypeElement) element);
            }
        }
        return generateSignatureList;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!roundEnvironment.processingOver()) {
            List<TypeElement> collected = collect(roundEnvironment, SimplePreference.class);
            for (TypeElement t : collected) {
                String s = t.getQualifiedName().toString();
                String packageName = s.substring(0, s.lastIndexOf("."));
                String clazzName = t.getAnnotation(SimplePreference.class).value();

                MethodSpec privateConstructor = MethodSpec
                        .constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(ParameterSpec.builder(classContext, "context").build())
                        .beginControlFlow("if ($L == null)", "preference")
                        .addStatement("$L = $L.getSharedPreferences($L, Context.MODE_PRIVATE)", "preference", "context", "NAME")
                        .endControlFlow()
                        .build();

                FieldSpec preferenceName = FieldSpec.builder(classString, "NAME", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S + \"_\" + $S", clazzName, "preferences").build();

                FieldSpec preference = FieldSpec.builder(classSharedPreference, "preference", Modifier.PRIVATE).build();

                ClassName classTarget = ClassName.get(packageName, clazzName);

                FieldSpec instance = FieldSpec.builder(classTarget, "instance", Modifier.PRIVATE, Modifier.STATIC).initializer("null").build();

                MethodSpec getInstance = MethodSpec
                        .methodBuilder("getInstance")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(ParameterSpec.builder(classContext, "context").build())
                        .beginControlFlow("if ($L == null)", "instance")
                        .addStatement("$L = new $L(context)", "instance", classTarget)
                        .endControlFlow()
                        .addStatement("return $L", "instance")
                        .returns(classTarget)
                        .build();

                TypeSpec.Builder targetClass = TypeSpec
                        .classBuilder(clazzName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(preference)
                        .addField(preferenceName)
                        .addMethod(privateConstructor)
                        .addField(instance)
                        .addMethod(getInstance);

                for (Element element : t.getEnclosedElements()) {
                    if (element.getKind() == ElementKind.FIELD) {
                        final VariableElement variableElement = (VariableElement) element;
                        messager.printMessage(Diagnostic.Kind.NOTE, variableElement.toString());
                        messager.printMessage(Diagnostic.Kind.NOTE, variableElement.asType().toString());

                        String name = capitalize(variableElement.getSimpleName().toString());

                        TypeInitiator entry = TypeInitiator.getTypeInitiator(variableElement.asType());
                        if (entry != null) {
                            Object defaultValue = variableElement.getConstantValue();
                            MethodSpec getMethod = MethodSpec
                                    .methodBuilder("get" + name)
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(TypeName.get(variableElement.asType()))
                                    .addStatement("return $L.get" + entry.method + "(\"$L\", " + entry.formatter + ")", "preference", name, defaultValue).build();

                            targetClass.addMethod(getMethod);

                            MethodSpec setMethod = MethodSpec
                                    .methodBuilder("set" + name)
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(TypeName.get(variableElement.asType()), "value")
                                    .returns(TypeName.VOID)
                                    .addStatement("$L.edit().put" + entry.method + "(\"$L\", " + "$L" + ").apply()", "preference", name, "value").build();
                            targetClass.addMethod(setMethod);
                        }
                    }
                }

                try {
                    JavaFile.builder(packageName, targetClass.build()).build().writeTo(filer);
                } catch (IOException ignored) {
                }
            }
        }

        return true;
    }

    private static class TypeInitiator {
        private static Map<TypeKind, TypeInitiator> methodReferenceMap = new HashMap<>();

        static {
            methodReferenceMap.put(BOOLEAN, new TypeInitiator("Boolean", "$L", false));
            methodReferenceMap.put(INT, new TypeInitiator("Int", "$L", 0));
            methodReferenceMap.put(LONG, new TypeInitiator("Long", "$Ll", 0L));
            methodReferenceMap.put(FLOAT, new TypeInitiator("Float", "$Lf", 0.f));
        }

        private final String method;
        private final String formatter;
        private final Object defaultValue;

        TypeInitiator(String method, String formatter, Object defaultValue) {
            this.method = method;
            this.formatter = formatter;
            this.defaultValue = defaultValue;
        }

        static TypeInitiator getTypeInitiator(TypeMirror typeMirror) {
            TypeInitiator v = methodReferenceMap.get(typeMirror.getKind());
            if (v != null) {
                return v;
            } else if (typeMirror.toString().equals(classString.toString())) {
                return new TypeInitiator("String", "$S", null);
            } else {
                return null;
            }
        }
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationSet = new HashSet<>();
        annotationSet.add(SimplePreference.class.getCanonicalName());
        return annotationSet;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}