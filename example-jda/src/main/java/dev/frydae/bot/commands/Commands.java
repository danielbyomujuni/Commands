package dev.frydae.bot.commands;

import dev.frydae.commands.CommandManager;

public class Commands {
    /**
     * Register command related things.
     */
    public static void registerCommands() {
        new MiscCommands();

        CommandManager.upsertCommands();
    }
}
