package dev.frydae.commands;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jetbrains.annotations.Nullable;

public final class CommandConditions {
    private final Table<Class<?>, String, Condition<?>> conditions;

    /**
     * Creates a new command conditions instance.
     */
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

    /**
     * Adds a condition to the manager.
     *
     * @param clazz the class of the condition
     * @param id the id of the condition
     * @param condition the condition
     * @param <P> the type of the condition
     */
    public <P> void addCondition(Class<P> clazz, String id, Condition<P> condition) {
        conditions.put(clazz, id.toLowerCase(), condition);
    }

    /**
     * Gets a condition from the manager.
     *
     * @param clazz the class of the condition
     * @param id the id of the condition
     * @return the condition
     */
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
