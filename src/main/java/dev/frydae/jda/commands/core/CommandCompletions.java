package dev.frydae.jda.commands.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CommandCompletions {
    private final Map<String, Function<CommandOptionContext, List<Choice>>> resolverMap;
    private final Map<String, Function<CommandAutoCompletionContext, List<Choice>>> autoResolverMap;

    CommandCompletions() {
        resolverMap = Maps.newHashMap();
        autoResolverMap = Maps.newHashMap();

        registerCompletion("fruit", c -> {
            List<String> fruits = Lists.newArrayList("apple", "pear", "banana", "blueberry", "strawberry");

            return fruits.stream().map(f -> new Choice(f, f)).collect(Collectors.toList());
        });

        registerCompletion("range", c -> {
            String config = c.getConfig();

            String[] split = config.split("-");

            int min;
            int max;
            if (split.length == 1) {
                min = 0;
                max = Integer.parseInt(split[0]);
            } else {
                min = Integer.parseInt(split[0]);
                max = Integer.parseInt(split[1]);
            }

            return IntStream.rangeClosed(min, max).limit(25).mapToObj(i -> new Choice(String.valueOf(i), String.valueOf(i))).collect(Collectors.toList());
        });

        registerAutoCompletion("range", c -> {
            String config = c.getConfig();

            String[] split = config.split("-");

            int min;
            int max;
            if (split.length == 1) {
                min = 0;
                max = Integer.parseInt(split[0]);
            } else {
                min = Integer.parseInt(split[0]);
                max = Integer.parseInt(split[1]);
            }

            return IntStream.rangeClosed(min, max)
                    .mapToObj(String::valueOf)
                    .filter(s -> s.startsWith(c.getCurrent()))
                    .limit(25)
                    .map(i -> new Choice(i, i))
                    .collect(Collectors.toList());
        });
    }

    /**
     * Finds completion choices for a key.
     *
     * @param key the key to search for
     * @return a list of {@link Choice choices} for this key
     */
    @Nullable
    public List<Choice> getChoices(String key) {
        String[] inputSplit = key.split("\\|");

        String input = inputSplit[0];

        Function<CommandOptionContext, List<Choice>> resolver = getResolver(input);

        if (resolver == null) {
            return null;
        }

        CommandOptionContext commandOptionContext = new CommandOptionContext(key);

        return resolver.apply(commandOptionContext);
    }

    public void registerCompletion(String input, Function<CommandOptionContext, List<Choice>> resolver) {
        resolverMap.put(input, resolver);
    }

    public void registerAutoCompletion(String input, Function<CommandAutoCompletionContext, List<Choice>> resolver) {
        autoResolverMap.put(input, resolver);
    }

    private Function<CommandOptionContext, List<Choice>> getResolver(String input) {
        return resolverMap.get(input);
    }

    public Function<CommandAutoCompletionContext, List<Choice>> getAutoResolver(String input) {
        return autoResolverMap.get(input);
    }

    /**
     * Processes an auto complete event.
     *
     * @param event The {@link CommandAutoCompleteInteractionEvent} to be processed
     * @param command the found {@link RegisteredCommand}
     */
    public void processAutoComplete(CommandAutoCompleteInteractionEvent event, RegisteredCommand command) {
        String name = event.getFocusedOption().getName();
        CommandParameter parameter = command.getParameter(name);

        if (!parameter.hasCompletion()) {
            return;
        }

        Function<CommandAutoCompletionContext, List<Choice>> autoResolver = getAutoResolver(parameter.getAutoCompletion().split("\\|")[0]);

        if (autoResolver == null) {
            return;
        }

        CommandAutoCompletionContext context = new CommandAutoCompletionContext(event, parameter.getAutoCompletion(), event.getFocusedOption().getValue());

        event.replyChoices(autoResolver.apply(context)).queue();
    }
}
