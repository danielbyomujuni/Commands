package dev.frydae.commands;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

@RequiredArgsConstructor
public final class RegisteredCommand {
    @Getter @NotNull private final BaseCommand instance;
    private final RegisteredCommand parent;
    @Getter @NotNull private final Class<?> baseClass;
    @Getter private final Method method;
    @Getter @NotNull private final String name;
    @Getter @NotNull private final String description;
    @Getter private final List<JDACommandParameter> parameters;
    @Getter private final boolean global;
    @Getter private final Permission[] permissions;
    @Getter private List<RegisteredCommand> subcommands = Lists.newArrayList();

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

    /**
     * Finds a {@link JDACommandParameter} by name.
     *
     * @param name the name of the parameter
     * @return a {@link JDACommandParameter} if found, null otherwise
     */
    public JDACommandParameter getParameter(String name) {
        return parameters.stream()
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

    public void addSubcommand(RegisteredCommand command) {
        subcommands.add(command);
    }
}
