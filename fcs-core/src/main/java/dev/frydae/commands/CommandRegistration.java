package dev.frydae.commands;

import com.google.common.collect.Lists;
import dev.frydae.commands.annotations.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandRegistration {
    protected static List<RegisteredCommand> collectCommandAliases(BaseCommand baseCommand) {
        Class<? extends BaseCommand> cmdClass = baseCommand.getClass();

        return Arrays.stream(cmdClass.getMethods())
                .filter(method -> method.isAnnotationPresent(CommandAlias.class))
                .filter(method -> !method.isAnnotationPresent(Disabled.class))
                .map(method -> {
                    String alias = CommandManager.getAnnotations().getAnnotationValue(method, CommandAlias.class);
                    String description = CommandManager.getAnnotations().getAnnotationValue(method, Description.class);

                    CommandParameter[] commandParameters = collectMethodParameters(method);

                    return new RegisteredCommand(baseCommand, null, cmdClass, method, alias, description, commandParameters);
                })
                .toList();
    }

    protected static CommandParameter[] collectMethodParameters(Method method) {
        List<CommandParameter> commandParameters = Lists.newArrayList();

        for (Parameter parameter : method.getParameters()) {
            boolean optional = parameter.isAnnotationPresent(Optional.class) || parameter.isAnnotationPresent(Default.class);

            CommandParameter commandParameter = CommandParameter.builder()
                    .parameter(parameter)
                    .optional(optional)
                    .name(CommandManager.getAnnotations().getAnnotationValue(parameter, Name.class))
                    .description(CommandManager.getAnnotations().getAnnotationValue(parameter, Description.class))
                    .defaultValue(CommandManager.getAnnotations().getAnnotationValue(parameter, Default.class, null))
                    .completion(CommandManager.getAnnotations().getAnnotationValue(parameter, Completion.class, null))
                    .condition(CommandManager.getAnnotations().getAnnotationValue(parameter, Condition.class, null))
                    .build();

            commandParameters.add(commandParameter);
        }

        return commandParameters.toArray(new CommandParameter[0]);
    }
}
