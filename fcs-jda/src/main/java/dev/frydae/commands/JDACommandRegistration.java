package dev.frydae.commands;

import com.google.common.collect.Lists;
import dev.frydae.commands.annotations.CommandAlias;
import dev.frydae.commands.annotations.CommandPermission;
import dev.frydae.commands.annotations.Completion;
import dev.frydae.commands.annotations.Condition;
import dev.frydae.commands.annotations.Default;
import dev.frydae.commands.annotations.Description;
import dev.frydae.commands.annotations.Disabled;
import dev.frydae.commands.annotations.GlobalCommand;
import dev.frydae.commands.annotations.Name;
import dev.frydae.commands.annotations.Optional;
import dev.frydae.commands.annotations.Subcommand;
import net.dv8tion.jda.api.Permission;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JDACommandRegistration extends CommandRegistration {
    /**
     * Registers registers command aliases for a given base command.
     *
     * @param baseCommand The base command to register.
     */
    static void registerCommandAliases(JDABaseCommand baseCommand) {
        Class<? extends JDABaseCommand> cmdClass = baseCommand.getClass();

        Permission[] classPerms = JDACommandManager.getAnnotations().getAnnotationValue(cmdClass, CommandPermission.class, new Permission[]{});

        collectCommandAliases(baseCommand)
                .stream()
                .map(cmd -> {
                    Method method = cmd.getMethod();

                    Permission[] methodPerms = JDACommandManager.getAnnotations().getAnnotationValue(method, CommandPermission.class, new Permission[]{});
                    Permission[] mergedPerms = Stream.concat(Arrays.stream(classPerms), Arrays.stream(methodPerms)).distinct().toArray(Permission[]::new);

                    boolean global = method.isAnnotationPresent(GlobalCommand.class);

                    return new JDARegisteredCommand(
                            (JDABaseCommand) cmd.getInstance(),
                            (JDARegisteredCommand) cmd.getParent(),
                            cmd.getBaseClass(),
                            cmd.getMethod(),
                            cmd.getName(),
                            cmd.getDescription(),
                            cmd.getParameters().stream().map(JDACommandParameter::new).toList(),
                            global,
                            mergedPerms
                    );
                })
                .forEach(command -> JDACommandManager.getRootCommands().add(command));
    }

    /**
     * Registers subcommands for a given base command.
     *
     * @param baseCommand The base command to register.
     */
    static void registerSubCommands(JDABaseCommand baseCommand) {
        Class<? extends JDABaseCommand> cmdClass = baseCommand.getClass();

        if (cmdClass.isAnnotationPresent(CommandAlias.class) && !cmdClass.isAnnotationPresent(Disabled.class)) {
            String alias = JDACommandManager.getAnnotations().getAnnotationValue(cmdClass, CommandAlias.class);
            String description = JDACommandManager.getAnnotations().getAnnotationValue(cmdClass, Description.class);
            Permission[] permissions = JDACommandManager.getAnnotations().getAnnotationValue(cmdClass, CommandPermission.class, null);

            boolean global = cmdClass.isAnnotationPresent(GlobalCommand.class);

            JDARegisteredCommand parent = new JDARegisteredCommand(baseCommand, null, cmdClass, null, alias, description, Lists.newArrayList(), global, permissions);

            for (Method method : cmdClass.getMethods()) {
                if (method.isAnnotationPresent(Subcommand.class) && !method.isAnnotationPresent(Disabled.class)) {
                    String subName = JDACommandManager.getAnnotations().getAnnotationValue(method, Subcommand.class);
                    String subDesc = JDACommandManager.getAnnotations().getAnnotationValue(method, Description.class);
                    Permission[] subPerms = JDACommandManager.getAnnotations().getAnnotationValue(method, CommandPermission.class, null);

                    List<JDACommandParameter> subParams = getMethodParameters(method);

                    parent.addSubcommand(new JDARegisteredCommand(parent.getInstance(), parent, parent.getBaseClass(), method, subName, subDesc, subParams, global, subPerms));
                }
            }

            JDACommandManager.getRootCommands().add(parent);
        }
    }

    /**
     * Gets the parameters for a given method.
     *
     * @param method The method to get the parameters for.
     * @return A list of command parameters.
     */
    private static List<JDACommandParameter> getMethodParameters(Method method) {
        List<JDACommandParameter> commandParameters = Lists.newArrayList();
        for (Parameter parameter : method.getParameters()) {
            boolean optional = parameter.isAnnotationPresent(Optional.class) || parameter.isAnnotationPresent(Default.class);

            JDACommandParameter commandParameter = new JDACommandParameter(
                    parameter,
                    JDACommandManager.getAnnotations().getAnnotationValue(parameter, Name.class),
                    JDACommandManager.getAnnotations().getAnnotationValue(parameter, Description.class),
                    optional,
                    JDACommandManager.getAnnotations().getAnnotationValue(parameter, Default.class, null),
                    JDACommandManager.getAnnotations().getAnnotationValue(parameter, Completion.class, null),
                    JDACommandManager.getAnnotations().getAnnotationValue(parameter, Condition.class, null)
            );

            commandParameters.add(commandParameter);
        }

        return commandParameters;
    }
}
