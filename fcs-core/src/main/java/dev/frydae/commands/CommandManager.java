package dev.frydae.commands;

import org.slf4j.Logger;

public abstract class CommandManager {
    private static Logger logger;
    private static Annotations annotations;

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        CommandManager.logger = logger;
    }

    /**
     * @return the command manager's annotations manager
     */
    public static Annotations getAnnotations() {
        if (annotations == null) {
            annotations = new Annotations();
        }

        return annotations;
    }
}
