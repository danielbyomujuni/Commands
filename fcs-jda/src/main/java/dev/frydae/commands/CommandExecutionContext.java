package dev.frydae.commands;

import lombok.Data;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

@Data
public final class CommandExecutionContext {
    private final RegisteredCommand command;
    private final CommandParameter parameter;
    private final OptionMapping mapping;
    private final SlashCommandInteractionEvent event;
}
