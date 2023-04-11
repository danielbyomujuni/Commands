package dev.frydae.jda.commands.core;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jetbrains.annotations.Nullable;

public final class CommandConditions {
    private final Table<Class<?>, String, Condition<?>> conditions;

    CommandConditions() {
        conditions = HashBasedTable.create();

        addCondition(Integer.class, "limits", (c, exec, value) -> {
            if (value == null) {
                return;
            }

            if (c.hasConfig("min") && c.getConfig("min", 0) > value) {
                throw new IllegalCommandException("Min value must be " + c.getConfig("min", 0));
            }

            if (c.hasConfig("max") && c.getConfig("max", 5) < value) {
                throw new IllegalCommandException("Max value must be " + c.getConfig("max", 5));
            }
        });
    }

    public <P> void addCondition(Class<P> clazz, String id, Condition<P> condition) {
        conditions.put(clazz, id.toLowerCase(), condition);
    }

    @Nullable
    public Condition<?> getCondition(Class<?> clazz, String id) {
        return conditions.get(clazz, id.toLowerCase());
    }

    /**
     * Validates that the input type matches certain criteria.
     *
     * @param <T> the type to validate
     */
    @FunctionalInterface
    public interface Condition<T> {
        void validate(CommandOptionContext context, CommandExecutionContext executionContext, T value) throws IllegalCommandException;
    }
}
