package dev.frydae.commands;


import com.google.common.collect.Lists;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

@Getter
public class RegisteredCommand {
    @NotNull private final BaseCommand instance;
    private final RegisteredCommand parent;
    @NotNull private final Class<?> baseClass;
    private final Method method;
    @NotNull private final String name;
    @NotNull private final String description;
    private final List<CommandParameter> parameters;
    private List<RegisteredCommand> subcommands;

    public RegisteredCommand(@NotNull BaseCommand instance, RegisteredCommand parent, @NotNull Class<?> baseClass, Method method, @NotNull String name, @NotNull String description, List<CommandParameter> parameters) {
        this.instance = instance;
        this.parent = parent;
        this.baseClass = baseClass;
        this.method = method;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.subcommands = Lists.newArrayList();
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

    public void addSubcommand(RegisteredCommand subcommand) {
        subcommands.add(subcommand);
    }

    public boolean hasSubcommands() {
        return !subcommands.isEmpty();
    }
}
