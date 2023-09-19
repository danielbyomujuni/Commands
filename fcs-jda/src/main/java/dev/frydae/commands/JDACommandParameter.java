package dev.frydae.commands;

public class JDACommandParameter extends CommandParameter {

    public boolean isAutoComplete() {
        return completion != null && completion.startsWith("@");
    }
}
