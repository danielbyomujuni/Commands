package dev.frydae.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Parameter;

@Getter @Setter @Builder
public class CommandParameter {
    @NotNull protected Parameter parameter;
    @NotNull protected String name;
    @NotNull protected String description;
    protected boolean optional;
    @Nullable protected String defaultValue;
    protected String completion;
    protected String condition;

    public boolean hasCompletion() {
        return completion != null;
    }

    public boolean hasCondition() {
        return condition != null;
    }
}
