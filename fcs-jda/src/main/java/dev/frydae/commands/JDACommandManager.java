package dev.frydae.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class JDACommandManager extends CommandManager {
    private static JDACommandManager singleton;
    private final CommandContexts<CommandExecutionContext> commandContexts;
    private final CommandCompletions commandCompletions;
    private final CommandConditions commandConditions;
    private final List<JDARegisteredCommand> rootCommands;
    private final Map<String, JDARegisteredCommand> commandCache;
    private static JDA jda;

    /**
     * Creates a new command manager.
     */
    private JDACommandManager() {
        this.commandContexts = new CommandContexts<>();
        this.commandCompletions = new CommandCompletions();
        this.commandConditions = new CommandConditions();
        this.rootCommands = Lists.newArrayList();
        this.commandCache = Maps.newHashMap();
    }

    private static JDACommandManager getSingleton() {
        if (singleton == null) {
            singleton = new JDACommandManager();
        }

        return singleton;
    }

    @TestOnly
    public static void resetSingleton() {
        singleton = null;
    }

    /**
     * @return the command manager's command contexts manager
     */
    public static CommandContexts<CommandExecutionContext> getCommandContexts() {
        return getSingleton().commandContexts;
    }

    /**
     * @return the command manager's command completions manager
     */
    public static CommandCompletions getCommandCompletions() {
        return getSingleton().commandCompletions;
    }

    /**
     * @return the command manager's command conditions manager
     */
    public static CommandConditions getCommandConditions() {
        return getSingleton().commandConditions;
    }

    /**
     * @return the command manager's root commands
     */
    public static List<JDARegisteredCommand> getRootCommands() {
        return getSingleton().rootCommands;
    }

    /**
     * @return the command manager's command cache
     */
    public static Map<String, JDARegisteredCommand> getCommandCache() {
        return getSingleton().commandCache;
    }

    public static JDA getJDA() {
        return JDACommandManager.jda;
    }

    public static void setJDA(JDA jda) {
        JDACommandManager.jda = jda;
    }

    /**
     * Searches for a matching {@link JDARegisteredCommand} and delegates to {@link CommandCompletions#processAutoComplete}.
     *
     * @param event the {@link CommandAutoCompleteInteractionEvent}
     */
    public static void processCommandAutoComplete(CommandAutoCompleteInteractionEvent event) {
        JDARegisteredCommand command = findRegisteredCommand(event.getFullCommandName());

        if (command != null) {
            getCommandCompletions().processAutoComplete(event, command);
        }
    }

    /**
     * Searches through the {@link JDACommandManager#commandCache} for the command.
     * </p>
     * If one isn't found, recursively search through all registered commands for one.
     * If one is then found, add it to {@link JDACommandManager#commandCache}
     *
     * @param fullCommandName the full discord command name
     * @return a {@link JDARegisteredCommand} if found, null otherwise
     */
    @Nullable
    static JDARegisteredCommand findRegisteredCommand(String fullCommandName) {
        if (getCommandCache().containsKey(fullCommandName)) {
            return getCommandCache().get(fullCommandName);
        }

        for (JDARegisteredCommand registeredCommand : getRootCommands()) {
            if (registeredCommand.getFullName().equalsIgnoreCase(fullCommandName)) {
                getCommandCache().put(fullCommandName, registeredCommand);

                return registeredCommand;
            }

            if (registeredCommand.hasSubcommands()) {
                /*for (JDARegisteredCommand subcommand : registeredCommand.getSubcommands0()) {
                    if (subcommand.getFullName().equalsIgnoreCase(fullCommandName)) {
                        getCommandCache().put(fullCommandName, subcommand);

                        return subcommand;
                    }
                }*/

                for (RegisteredCommand subcommand : registeredCommand.getSubcommands()) {
                    if (subcommand.getFullName().equalsIgnoreCase(fullCommandName)) {
                        getCommandCache().put(fullCommandName, (JDARegisteredCommand) subcommand);

                        return (JDARegisteredCommand) subcommand;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Registers out commands with Discord.
     */
    public static void upsertCommands() {
        CommandListUpdateAction commandListUpdateAction = getJDA().updateCommands();

        for (JDARegisteredCommand command : getRootCommands()) {
            SlashCommandData commandData = Commands.slash(command.getName(), command.getDescription());

            if (command.getPermissions() != null) {
                commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.getPermissions()));
            }

            if (!command.hasSubcommands()) {
                Arrays.stream(command.getParameters()).map(JDACommandManager::getOptionData).forEachOrdered(commandData::addOptions);
            } else {
                for (RegisteredCommand subcommand : command.getSubcommands()) {
                    SubcommandData subcommandData = new SubcommandData(subcommand.getName(), subcommand.getDescription());

                    Arrays.stream(subcommand.getParameters()).map((CommandParameter parameter) -> getOptionData((JDACommandParameter) parameter)).forEachOrdered(subcommandData::addOptions);

                    commandData.addSubcommands(subcommandData);
                }
            }

            commandData.setGuildOnly(!command.isGlobal());

            commandListUpdateAction.addCommands(commandData);
        }

        commandListUpdateAction.queue(success -> {
            System.out.println("Commands Updated :)");
        });
    }

    @NotNull
    private static OptionData getOptionData(JDACommandParameter parameter) {
        CommandContexts<CommandExecutionContext> commandContexts = JDACommandManager.getCommandContexts();
        CommandCompletions commandCompletions = JDACommandManager.getCommandCompletions();

        OptionData optionData = new OptionData(commandContexts.getMapping(parameter.getParameter().getType()), parameter.getName(), parameter.getDescription(), !parameter.isOptional());

        if (parameter.hasCompletion()) {
            if (parameter.isAutoComplete()) {
                optionData.setAutoComplete(true);
            } else {
                List<Choice> choices = commandCompletions.getChoices(parameter.getCompletion());

                if (choices != null) {
                    optionData.addChoices(choices);
                }
            }
        }
        return optionData;
    }

    public static void registerCommand(JDABaseCommand baseCommand) {
        CommandRegistration.registerCommandAliases(baseCommand);
        CommandRegistration.registerSubCommands(baseCommand);
    }
}
