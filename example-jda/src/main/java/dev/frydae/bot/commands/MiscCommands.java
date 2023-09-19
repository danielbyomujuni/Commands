package dev.frydae.bot.commands;

import dev.frydae.bot.utils.GuildUtil;
import dev.frydae.commands.annotations.*;
import dev.frydae.commands.BaseCommand;
import net.dv8tion.jda.api.Permission;

public class MiscCommands extends BaseCommand {
    @CommandAlias("ping")
    @Description("Pong!")
    @GlobalCommand
    public void onPing() {
        replyHidden(":ping_pong: Pong!").queue();
    }

    @CommandAlias("range")
    @Description("test")
    public void onTest(@Completion("range|1-5") @Name("range") @Description("range") Integer fish) {
        replyHidden(String.valueOf(fish)).queue();
    }

    @CommandAlias("arange")
    @Description("test")
    public void onAutoRange(@Completion("@range|1-100") @Name("range") @Description("range") Integer fish) {
        replyHidden(String.valueOf(fish)).queue();
    }

    @CommandAlias("stop")
    @Description("Stops the bot")
    @CommandPermission(Permission.ADMINISTRATOR)
    public void onStop() {
        replyHidden("Shutting down now").queue(success -> {
            GuildUtil.shutdown();
        });
    }
}
