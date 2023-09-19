package dev.frydae.commands;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class JDARegisteredCommand extends RegisteredCommand {
    @Getter private final boolean global;
    @Getter private final Permission[] permissions;
    @Getter private List<JDARegisteredCommand> subcommands = Lists.newArrayList();

    public JDARegisteredCommand(@NotNull JDABaseCommand instance, JDARegisteredCommand parent, @NotNull Class<?> baseClass, Method method, @NotNull String name, @NotNull String description, JDACommandParameter[] parameters, boolean global, Permission[] permissions) {
        super(instance, parent, baseClass, method, name, description, parameters);
        this.global = global;
        this.permissions = permissions;
    }

    //<region Child overrides>
    @Override
    public @NotNull JDABaseCommand getInstance() {
        return (JDABaseCommand) super.getInstance();
    }

    @Override
    public JDACommandParameter[] getParameters() {
        return (JDACommandParameter[]) super.getParameters();
    }

    //<endregion>

    /**
     * Finds a {@link JDACommandParameter} by name.
     *
     * @param name the name of the parameter
     * @return a {@link JDACommandParameter} if found, null otherwise
     */
    public JDACommandParameter getParameter(String name) {
        return Arrays.stream(getParameters())
                .filter(p -> p.getName() != null)
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public boolean hasSubcommands() {
        return !subcommands.isEmpty();
    }

    public boolean hasPermissions() {
        return permissions != null;
    }

    public void addSubcommand(JDARegisteredCommand command) {
        subcommands.add(command);
    }
}
