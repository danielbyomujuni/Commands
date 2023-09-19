package dev.frydae.commands;


import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Heavily inspired by <a href="https://github.com/aikar/commands/blob/master/core/src/main/java/co/aikar/commands/Annotations.java">ACF Annotations.java</a>.
 */
public final class Annotations {
    private final Map<Class<? extends Annotation>, Method> valueMethods = Maps.newIdentityHashMap();
    private final Map<Class<? extends Annotation>, Void> noValueAnnotations = Maps.newIdentityHashMap();

    /**
     * Searches a provided type for a given annotation.
     *
     * @param element the element to search
     * @param annotationClass the class to search for
     * @param defaultValue the default value if an annotation cannot be found
     * @param <T> the value of the annotation
     * @return the value of the annotation
     */
    public <T> T getAnnotationValue(AnnotatedElement element, Class<? extends Annotation> annotationClass, T defaultValue) {
        T value = getAnnotationValue(element, annotationClass);

        return value == null ? defaultValue : value;
    }

    /**
     * Searches a provided type for a given annotation.
     *
     * @param element the element to search
     * @param annotationClass the class to search for
     * @param <T> the value of the annotation
     * @return the value of the annotation
     */
    @SuppressWarnings("unchecked")
    public <T> T getAnnotationValue(AnnotatedElement element, Class<? extends Annotation> annotationClass) {
        Annotation annotation = getAnnotation(element, annotationClass);
        T value = null;

        if (annotation != null) {
            if (noValueAnnotations.containsKey(annotationClass)) {
                value = (T) "";
            } else {
                try {
                    Method valueMethod = valueMethods.get(annotationClass);

                    if (valueMethod == null) {
                        valueMethod = annotationClass.getMethod("value");
                        valueMethod.setAccessible(true);
                        valueMethods.put(annotationClass, valueMethod);
                    }

                    value = (T) valueMethod.invoke(annotation);
                } catch (NoSuchMethodException e) {
                    noValueAnnotations.put(annotationClass, null);
                    value = (T) "";
                } catch (InvocationTargetException | IllegalAccessException e) {
                    CommandManager.getLogger().error("Unable to find annotation value");
                }
            }
        }

        return value;
    }

    /**
     * Gets an annotation from a provided type.
     *
     * @param element the element to search
     * @param annotationClass the Class to search for
     * @return the {@link Annotation} if one is found, null otherwise
     */
    @Nullable
    private Annotation getAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationClass) {
        if (element.isAnnotationPresent(annotationClass)) {
            return element.getAnnotation(annotationClass);
        }

        return null;
    }
}
