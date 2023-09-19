package dev.frydae.commands;


import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class RegisteredCommand {
    @Getter @NotNull private final BaseCommand instance;
    private final RegisteredCommand parent;
    @Getter @NotNull private final Class<?> baseClass;
    @Getter private final Method method;
    @Getter @NotNull private final String name;
    @Getter @NotNull private final String description;
    @Getter private final CommandParameter[] parameters;

    public RegisteredCommand(@NotNull BaseCommand instance, RegisteredCommand parent, @NotNull Class<?> baseClass, Method method, @NotNull String name, @NotNull String description, CommandParameter[] parameters) {
        this.instance = instance;
        this.parent = parent;
        this.baseClass = baseClass;
        this.method = method;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    /**
     * Prepends the parent's name if one exists.
     *
     * @return the full name
     */
    @NotNull
    public String getFullName() {
        if (parent != null) {
            return parent.getName() + " " + getName();
        }

        return getName();
    }
}
