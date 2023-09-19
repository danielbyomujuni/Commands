package dev.frydae.bot.utils;

import dev.frydae.bot.DiscordBot;
import net.dv8tion.jda.api.JDA;

public class GuildUtil {
    public static void shutdown() {
        DiscordBot.getJDA().shutdownNow();
        System.exit(0);
    }
}
