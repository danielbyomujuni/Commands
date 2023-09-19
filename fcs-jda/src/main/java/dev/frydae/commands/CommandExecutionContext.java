package dev.frydae.commands;

import lombok.Data;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

@Data
public final class CommandExecutionContext {
    private final JDARegisteredCommand command;
    private final JDACommandParameter parameter;
    private final OptionMapping mapping;
    private final SlashCommandInteractionEvent event;
}
