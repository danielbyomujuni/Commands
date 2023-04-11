package dev.frydae.jda.commands.core;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import javax.annotation.CheckReturnValue;
import java.util.Arrays;

public abstract class BaseCommand {
    private SlashCommandInteractionEvent event;

    public BaseCommand() {
        CommandManager.registerCommand(this);
    }

    void setSlashCommandEvent(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    void tearDown() {
        this.event = null;
    }

    public SlashCommandInteractionEvent getEvent() {
        return event;
    }

    public Member getSender() {
        return event.getMember();
    }

    @CheckReturnValue
    protected ReplyCallbackAction replyHidden(String message) {
        return getEvent().getInteraction().reply(message).setEphemeral(true);
    }

    @CheckReturnValue
    protected ReplyCallbackAction replyHidden(MessageEmbed... embeds) {
        return getEvent().getInteraction().replyEmbeds(Arrays.asList(embeds)).setEphemeral(true);
    }
}
