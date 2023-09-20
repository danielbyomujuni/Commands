package dev.frydae.commands;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;

@Data
public class CommandParameter {
    @NotNull protected Parameter parameter;
    @NotNull protected String name;
    @NotNull protected String description;
    protected boolean optional;
    @Nullable protected String defaultValue;
    protected String completion;
    protected String condition;

    public CommandParameter(@NotNull Parameter parameter, @NotNull String name, @NotNull String description, boolean optional, @Nullable String defaultValue, String completion, String condition) {
        this.parameter = parameter;
        this.name = name;
        this.description = description;
        this.optional = optional;
        this.defaultValue = defaultValue;
        this.completion = completion;
        this.condition = condition;
    }

    public boolean hasCompletion() {
        return completion != null;
    }

    public boolean hasCondition() {
        return condition != null;
    }
}
