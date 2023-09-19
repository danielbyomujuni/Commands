package dev.frydae.bot.commands;

import dev.frydae.jda.commands.core.CommandManager;

public class Commands {
    /**
     * Register command related things.
     */
    public static void registerCommands() {
        new MiscCommands();

        CommandManager.upsertCommands();
    }
}
