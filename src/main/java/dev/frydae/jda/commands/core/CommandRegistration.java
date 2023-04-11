package dev.frydae.jda.commands.core;

import com.google.common.collect.Lists;
import dev.frydae.jda.commands.annotations.AutoCompletion;
import dev.frydae.jda.commands.annotations.CommandAlias;
import dev.frydae.jda.commands.annotations.CommandPermission;
import dev.frydae.jda.commands.annotations.Completions;
import dev.frydae.jda.commands.annotations.Condition;
import dev.frydae.jda.commands.annotations.Default;
import dev.frydae.jda.commands.annotations.Description;
import dev.frydae.jda.commands.annotations.Disabled;
import dev.frydae.jda.commands.annotations.GlobalCommand;
import dev.frydae.jda.commands.annotations.Name;
import dev.frydae.jda.commands.annotations.Optional;
import dev.frydae.jda.commands.annotations.Subcommand;
import net.dv8tion.jda.api.Permission;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

// TODO: If base-level class has permissions attached, so to all contained command methods
public final class CommandRegistration {
    static void registerCommandAliases(BaseCommand baseCommand) {
        Class<? extends BaseCommand> cmdClass = baseCommand.getClass();

        Permission[] classPerms = CommandManager.getAnnotations().getAnnotationValue(cmdClass, CommandPermission.class, new Permission[]{});

        for (Method method : cmdClass.getMethods()) {
            if (method.isAnnotationPresent(CommandAlias.class) && !method.isAnnotationPresent(Disabled.class)) {
                String alias = CommandManager.getAnnotations().getAnnotationValue(method, CommandAlias.class);
                String description = CommandManager.getAnnotations().getAnnotationValue(method, Description.class);
                Permission[] methodPerms = CommandManager.getAnnotations().getAnnotationValue(method, CommandPermission.class, new Permission[]{});

                Permission[] mergedPerms = Stream.concat(Arrays.stream(classPerms), Arrays.stream(methodPerms)).distinct().toArray(Permission[]::new);

                boolean global = method.isAnnotationPresent(GlobalCommand.class);

                List<CommandParameter> commandParameters = getMethodParameters(method);

                RegisteredCommand command = new RegisteredCommand(baseCommand, null, cmdClass, method, alias, description, commandParameters, global, mergedPerms);

                CommandManager.getRootCommands().add(command);
            }
        }
    }

    static void registerSubCommands(BaseCommand baseCommand) {
        Class<? extends BaseCommand> cmdClass = baseCommand.getClass();

        if (cmdClass.isAnnotationPresent(CommandAlias.class) && !cmdClass.isAnnotationPresent(Disabled.class)) {
            String alias = CommandManager.getAnnotations().getAnnotationValue(cmdClass, CommandAlias.class);
            String description = CommandManager.getAnnotations().getAnnotationValue(cmdClass, Description.class);
            Permission[] permissions = CommandManager.getAnnotations().getAnnotationValue(cmdClass, CommandPermission.class, null);

            boolean global = cmdClass.isAnnotationPresent(GlobalCommand.class);

            RegisteredCommand parent = new RegisteredCommand(baseCommand, null, cmdClass, null, alias, description, null, global, permissions);

            for (Method method : cmdClass.getMethods()) {
                if (method.isAnnotationPresent(Subcommand.class) && !method.isAnnotationPresent(Disabled.class)) {
                    String subName = CommandManager.getAnnotations().getAnnotationValue(method, Subcommand.class);
                    String subDesc = CommandManager.getAnnotations().getAnnotationValue(method, Description.class);
                    Permission[] subPerms = CommandManager.getAnnotations().getAnnotationValue(method, CommandPermission.class, null);

                    List<CommandParameter> subParams = getMethodParameters(method);

                    parent.addSubcommand(new RegisteredCommand(parent.getInstance(), parent, parent.getBaseClass(), method, subName, subDesc, subParams, global, subPerms));
                }
            }

            CommandManager.getRootCommands().add(parent);
        }
    }

    private static List<CommandParameter> getMethodParameters(Method method) {
        List<CommandParameter> commandParameters = Lists.newArrayList();
        for (Parameter parameter : method.getParameters()) {
            boolean optional = parameter.isAnnotationPresent(Optional.class) || parameter.isAnnotationPresent(Default.class);

            CommandParameter commandParameter = CommandParameter.builder()
                    .parameter(parameter)
                    .name(CommandManager.getAnnotations().getAnnotationValue(parameter, Name.class))
                    .description(CommandManager.getAnnotations().getAnnotationValue(parameter, Description.class))
                    .defaultValue(CommandManager.getAnnotations().getAnnotationValue(parameter, Default.class, null))
                    .autoCompletion(CommandManager.getAnnotations().getAnnotationValue(parameter, AutoCompletion.class, null))
                    .completion(CommandManager.getAnnotations().getAnnotationValue(parameter, Completions.class, null))
                    .condition(CommandManager.getAnnotations().getAnnotationValue(parameter, Condition.class, null))
                    .optional(optional)
                    .build();

            commandParameters.add(commandParameter);
        }

        return commandParameters;
    }
}
