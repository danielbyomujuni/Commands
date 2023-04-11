package dev.frydae.jda.commands.core;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

public class CommandAutoCompletionContext extends CommandOptionContext {
    @Getter private final CommandAutoCompleteInteractionEvent event;
    @Getter private final String current;

    CommandAutoCompletionContext(CommandAutoCompleteInteractionEvent event, String input, String current) {
        super(input);
        this.event = event;
        this.current = current;
    }
}
