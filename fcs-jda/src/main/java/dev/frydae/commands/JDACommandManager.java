package dev.frydae.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
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
    @Getter private final JDACommandContexts<JDACommandExecutionContext> commandContexts;
    private final CommandCompletions commandCompletions;
    private final CommandConditions commandConditions;
    private static JDA jda;

    /**
     * Creates a new command manager.
     */
    private JDACommandManager() {
        this.commandContexts = new JDACommandContexts<>();
        this.commandCompletions = new CommandCompletions();
        this.commandConditions = new CommandConditions();
    }

    public static JDACommandManager getSingleton() {
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
        JDARegisteredCommand command = (JDARegisteredCommand) findRegisteredCommand(event.getFullCommandName());

        if (command != null) {
            getCommandCompletions().processAutoComplete(event, command);
        }
    }

    /**
     * Registers out commands with Discord.
     */
    public static void upsertCommands() {
        CommandListUpdateAction commandListUpdateAction = getJDA().updateCommands();

        for (RegisteredCommand cmd : getRootCommands()) {
            JDARegisteredCommand command = (JDARegisteredCommand) cmd;

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
        JDACommandContexts<JDACommandExecutionContext> commandContexts = JDACommandManager.getSingleton().getCommandContexts();
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
