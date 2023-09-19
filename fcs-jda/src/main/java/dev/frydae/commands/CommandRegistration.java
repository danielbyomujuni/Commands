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
import java.util.stream.Stream;

public final class CommandRegistration {
    /**
     * Registers registers command aliases for a given base command.
     *
     * @param baseCommand The base command to register.
     */
    static void registerCommandAliases(BaseCommand baseCommand) {
        Class<? extends BaseCommand> cmdClass = baseCommand.getClass();

        Permission[] classPerms = JDACommandManager.getAnnotations().getAnnotationValue(cmdClass, CommandPermission.class, new Permission[]{});

        for (Method method : cmdClass.getMethods()) {
            if (method.isAnnotationPresent(CommandAlias.class) && !method.isAnnotationPresent(Disabled.class)) {
                String alias = JDACommandManager.getAnnotations().getAnnotationValue(method, CommandAlias.class);
                String description = JDACommandManager.getAnnotations().getAnnotationValue(method, Description.class);
                Permission[] methodPerms = JDACommandManager.getAnnotations().getAnnotationValue(method, CommandPermission.class, new Permission[]{});

                Permission[] mergedPerms = Stream.concat(Arrays.stream(classPerms), Arrays.stream(methodPerms)).distinct().toArray(Permission[]::new);

                boolean global = method.isAnnotationPresent(GlobalCommand.class);

                List<CommandParameter> commandParameters = getMethodParameters(method);

                RegisteredCommand command = new RegisteredCommand(baseCommand, null, cmdClass, method, alias, description, commandParameters, global, mergedPerms);

                JDACommandManager.getRootCommands().add(command);
            }
        }
    }

    /**
     * Registers subcommands for a given base command.
     *
     * @param baseCommand The base command to register.
     */
    static void registerSubCommands(BaseCommand baseCommand) {
        Class<? extends BaseCommand> cmdClass = baseCommand.getClass();

        if (cmdClass.isAnnotationPresent(CommandAlias.class) && !cmdClass.isAnnotationPresent(Disabled.class)) {
            String alias = JDACommandManager.getAnnotations().getAnnotationValue(cmdClass, CommandAlias.class);
            String description = JDACommandManager.getAnnotations().getAnnotationValue(cmdClass, Description.class);
            Permission[] permissions = JDACommandManager.getAnnotations().getAnnotationValue(cmdClass, CommandPermission.class, null);

            boolean global = cmdClass.isAnnotationPresent(GlobalCommand.class);

            RegisteredCommand parent = new RegisteredCommand(baseCommand, null, cmdClass, null, alias, description, null, global, permissions);

            for (Method method : cmdClass.getMethods()) {
                if (method.isAnnotationPresent(Subcommand.class) && !method.isAnnotationPresent(Disabled.class)) {
                    String subName = JDACommandManager.getAnnotations().getAnnotationValue(method, Subcommand.class);
                    String subDesc = JDACommandManager.getAnnotations().getAnnotationValue(method, Description.class);
                    Permission[] subPerms = JDACommandManager.getAnnotations().getAnnotationValue(method, CommandPermission.class, null);

                    List<CommandParameter> subParams = getMethodParameters(method);

                    parent.addSubcommand(new RegisteredCommand(parent.getInstance(), parent, parent.getBaseClass(), method, subName, subDesc, subParams, global, subPerms));
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
    private static List<CommandParameter> getMethodParameters(Method method) {
        List<CommandParameter> commandParameters = Lists.newArrayList();
        for (Parameter parameter : method.getParameters()) {
            boolean optional = parameter.isAnnotationPresent(Optional.class) || parameter.isAnnotationPresent(Default.class);

            CommandParameter commandParameter = CommandParameter.builder()
                    .parameter(parameter)
                    .name(JDACommandManager.getAnnotations().getAnnotationValue(parameter, Name.class))
                    .description(JDACommandManager.getAnnotations().getAnnotationValue(parameter, Description.class))
                    .defaultValue(JDACommandManager.getAnnotations().getAnnotationValue(parameter, Default.class, null))
                    .completion(JDACommandManager.getAnnotations().getAnnotationValue(parameter, Completion.class, null))
                    .condition(JDACommandManager.getAnnotations().getAnnotationValue(parameter, Condition.class, null))
                    .optional(optional)
                    .build();

            commandParameters.add(commandParameter);
        }

        return commandParameters;
    }
}
