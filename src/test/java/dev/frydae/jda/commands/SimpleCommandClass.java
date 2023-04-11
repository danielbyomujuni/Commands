package dev.frydae.jda.commands;

import dev.frydae.jda.commands.annotations.CommandAlias;
import dev.frydae.jda.commands.annotations.Subcommand;

@CommandAlias("simple")
class SimpleCommandClass {

    @Subcommand("simple")
    public void onSubCommand() {

    }
}
