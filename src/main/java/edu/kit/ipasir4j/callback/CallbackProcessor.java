package edu.kit.ipasir4j.callback;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@SupportedAnnotationTypes("edu.kit.ipasir4j.callback.IpasirCallback")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class CallbackProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            var elements = roundEnv.getElementsAnnotatedWith(annotation);
            if ("IpasirCallback".contentEquals(annotation.getSimpleName())) {
                processCallbacks(elements);
            } else {
                throw new AssertionError("Unknown annotation type: " + annotation);
            }
        }
        return false;
    }

    private void processCallbacks(Set<? extends Element> methodDefinitions) {
        for (Element def : methodDefinitions) {
            IpasirCallback annotationValue = def.getAnnotation(IpasirCallback.class);
            try {
                switch (annotationValue.type()) {
                    case LEARN -> processLearnCallback(annotationValue, def);
                    case TERMINATE -> processTerminateCallback(annotationValue, def);
                }
            } catch (IOException e) {
                e.printStackTrace();
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), def);
            }
        }
    }

    private void processTerminateCallback(IpasirCallback value, Element methodDef) throws IOException {
        var type = (ExecutableType) methodDef.asType();
        boolean valid = methodDef.getModifiers().containsAll(Set.of(Modifier.PUBLIC, Modifier.STATIC))
                && type.getReturnType().getKind() == TypeKind.BOOLEAN
                && type.getParameterTypes().size() == 1
                && isSolverData(type.getParameterTypes().get(0));

        if (!valid) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Terminate Callbacks must be of the following kind:\n" +
                            "public static boolean (? extends SolverData)", methodDef);
            return;
        }

        generateSourceFile(value, methodDef, "TerminateCallbackClass.java");
    }

    private void processLearnCallback(IpasirCallback value, Element methodDef) throws IOException {
        var type = (ExecutableType) methodDef.asType();
        boolean valid = methodDef.getModifiers().containsAll(Set.of(Modifier.PUBLIC, Modifier.STATIC))
                && type.getReturnType().getKind() == TypeKind.VOID
                && type.getParameterTypes().size() == 2
                && isSolverData(type.getParameterTypes().get(0))
                && isIntArray(type.getParameterTypes().get(1));

        if (!valid) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Learn callbacks must be of the following kind:\n" +
                            "public static void (? extends SolverData, int[])", methodDef);
            return;
        }

        generateSourceFile(value, methodDef, "LearnCallbackClass.java");
    }

    private void generateSourceFile(IpasirCallback value, Element methodDef, String templateName) throws IOException {
        var params = formattingParameters(value.target(), methodDef);
        var sourceFile = processingEnv.getFiler()
                .createSourceFile(params[0] + '.' + params[1], methodDef);
        var template = new String(getClass().getResourceAsStream(templateName).readAllBytes(), StandardCharsets.UTF_8);
        try (var writer = sourceFile.openWriter()) {
            writer.write(String.format(template, (Object[]) params));
            writer.flush();
        }
    }

    private String[] formattingParameters(String target, Element methodDef) {
        var enclosingType = (TypeElement) methodDef.getEnclosingElement();
        var containingPackage = (PackageElement) enclosingType.getEnclosingElement();
        var targetClassName = target.isBlank()
                ? capitalize(methodDef.getSimpleName().toString()) + "Callback"
                : target;
        var dataType = (DeclaredType) ((ExecutableType) methodDef.asType()).getParameterTypes().get(0);
        return new String[] {
                containingPackage.getQualifiedName().toString(),
                targetClassName,
                enclosingType.getQualifiedName().toString(),
                methodDef.getSimpleName().toString(),
                ((TypeElement) dataType.asElement()).getQualifiedName().toString()
        };
    }

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private boolean isSolverData(TypeMirror parameter) {
        if (parameter.getKind() != TypeKind.DECLARED) {
            return false;
        }
        // parameter needs to be related to SolverData somewhere down the line
        var element = (TypeElement) ((DeclaredType) parameter).asElement();
        while (!element.getQualifiedName().contentEquals("edu.kit.ipasir4j.callback.SolverData")) {
            var superclass = element.getSuperclass();
            if (superclass.getKind() == TypeKind.NONE) {
                return false;
            }
            element = (TypeElement) ((DeclaredType) superclass).asElement();
        }
        return true;
    }

    private boolean isIntArray(TypeMirror parameter) {
        return parameter.getKind() == TypeKind.ARRAY
                && ((ArrayType) parameter).getComponentType().getKind() == TypeKind.INT;
    }

}
