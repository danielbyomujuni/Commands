package dev.frydae.jda.commands.annotations.processors;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dev.frydae.jda.commands.annotations.AutoCompletion;
import dev.frydae.jda.commands.annotations.CommandAlias;
import dev.frydae.jda.commands.annotations.Completions;
import dev.frydae.jda.commands.annotations.Description;
import dev.frydae.jda.commands.annotations.Name;
import dev.frydae.jda.commands.annotations.SkipAnnotationProcessor;
import dev.frydae.jda.commands.annotations.Subcommand;
import dev.frydae.jda.commands.core.CommandUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes({
    "dev.frydae.jda.commands.annotations.CommandAlias",
    "dev.frydae.jda.commands.annotations.Subcommand"
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class CommandAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Map<Class<? extends Annotation>, Element> invalidElements = Maps.newHashMap();
        Map<Element, List<Class<? extends Annotation>>> conflictingElements = Maps.newHashMap();

        List<ElementKind> typeKinds = Collections.singletonList(ElementKind.CLASS);
        // let's gather all types we are interested in
        List<Element> commandElements = env
                .getElementsAnnotatedWithAny(Sets.newHashSet(CommandAlias.class))
                .stream()
                .filter(e -> typeKinds.contains(e.getKind()))
                .filter(e -> e.getAnnotation(SkipAnnotationProcessor.class) == null)
                .collect(Collectors.toList());

        commandElements.forEach(c -> checkElementForAnnotation(invalidElements, c, Description.class));

        processCommandMethods(env, invalidElements, conflictingElements);

        for (Map.Entry<Class<? extends Annotation>, Element> entry : invalidElements.entrySet()) {
            String message = String.format("\n%s %s must be annotated with @%s", CommandUtils.ucfirst(entry.getValue().getKind().name()), entry.getValue().getSimpleName(), entry.getKey().getSimpleName());

            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, entry.getValue());
        }

        for (Map.Entry<Element, List<Class<? extends Annotation>>> entry : conflictingElements.entrySet()) {
            List<String> annoNames = entry.getValue().stream().map(Class::getSimpleName).map(a -> "@" + a).collect(Collectors.toList());

            String message = String.format("Only one of (%s) can be applied to %s\n", String.join(", ", annoNames), entry.getKey().getSimpleName());
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, entry.getKey());
        }

        return false;
    }

    private void processCommandMethods(RoundEnvironment env, Map<Class<? extends Annotation>, Element> invalidElements, Map<Element, List<Class<? extends Annotation>>> conflictingElements) {
        List<ElementKind> typeKinds = Collections.singletonList(ElementKind.METHOD);
        // let's gather all types we are interested in
        List<ExecutableElement> commandElements = env
                .getElementsAnnotatedWithAny(Sets.newHashSet(CommandAlias.class, Subcommand.class))
                .stream()
                .filter(e -> typeKinds.contains(e.getKind()))
                .filter(e -> e.getAnnotation(SkipAnnotationProcessor.class) == null)
                .filter(ExecutableElement.class::isInstance)
                .map(e -> (ExecutableElement) e)
                .collect(Collectors.toList());

        for (ExecutableElement commandElement : commandElements) {
            processMethod(invalidElements, commandElement, conflictingElements);
            processParameters(invalidElements, commandElement, conflictingElements);
        }
    }

    private void processMethod(Map<Class<? extends Annotation>, Element> invalidElements, ExecutableElement commandElement, Map<Element, List<Class<? extends Annotation>>> conflictingElements) {
        checkElementForAnnotation(invalidElements, commandElement, Description.class);
    }

    private void processParameters(Map<Class<? extends Annotation>, Element> invalidElements, ExecutableElement commandElement, Map<Element, List<Class<? extends Annotation>>> conflictingElements) {
        for (VariableElement parameter : commandElement.getParameters()) {
            checkElementForAnnotation(invalidElements, parameter, Name.class);
            checkElementForAnnotation(invalidElements, parameter, Description.class);

            checkElementForMultipleAnnotations(conflictingElements, parameter, Completions.class, AutoCompletion.class);
        }
    }

    private void checkElementForAnnotation(Map<Class<? extends Annotation>, Element> invalidElements, Element element, Class<? extends Annotation> annoClass) {
        if (element.getAnnotation(annoClass) == null) {
            invalidElements.put(annoClass, element);
        }
    }

    @SafeVarargs
    private void checkElementForMultipleAnnotations(Map<Element, List<Class<? extends Annotation>>> conflictingElements, Element element, Class<? extends Annotation>... annoClasses) {
        List<Class<? extends Annotation>> foundAnnotations = Lists.newArrayList();
        for (Class<? extends Annotation> annoClass : annoClasses) {
            if (element.getAnnotation(annoClass) != null) {
                foundAnnotations.add(annoClass);
            }
        }

        if (foundAnnotations.size() > 1) {
            conflictingElements.put(element, foundAnnotations);
        }
    }
}
