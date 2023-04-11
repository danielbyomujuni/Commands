package dev.frydae.jda.commands.core;

import com.google.common.collect.Maps;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.Map;

public final class CommandContexts<R extends CommandExecutionContext> {
    private final Map<Class<?>, ContextResolver<?, R>> contextMap;
    private final Map<Class<?>, OptionType> typeMap;

    CommandContexts() {
        contextMap = Maps.newHashMap();
        typeMap = Maps.newHashMap();

        registerContexts();
        registerMappings();
    }

    private void registerContexts() {
        registerContext(Integer.class, c -> c.getMapping().getAsInt());
        registerContext(Double.class, c -> c.getMapping().getAsDouble());
        registerContext(Boolean.class, c -> c.getMapping().getAsBoolean());
        registerContext(Long.class, c -> c.getMapping().getAsLong());
        registerContext(String.class, c -> c.getMapping().getAsString());
        registerContext(TextChannel.class, c -> c.getMapping().getAsChannel().asTextChannel());
        registerContext(VoiceChannel.class, c -> c.getMapping().getAsChannel().asVoiceChannel());
        registerContext(User.class, c -> c.getMapping().getAsUser());
        registerContext(Member.class, c -> c.getMapping().getAsMember());
    }

    private void registerMappings() {
        registerMapping(Integer.class, OptionType.INTEGER);
        registerMapping(Double.class, OptionType.NUMBER);
        registerMapping(Long.class, OptionType.NUMBER);
        registerMapping(Boolean.class, OptionType.BOOLEAN);
        registerMapping(String.class, OptionType.STRING);
        registerMapping(TextChannel.class, OptionType.CHANNEL);
        registerMapping(VoiceChannel.class, OptionType.CHANNEL);
        registerMapping(FileUpload.class, OptionType.ATTACHMENT);
        registerMapping(Member.class, OptionType.USER);
        registerMapping(User.class, OptionType.USER);
        registerMapping(Role.class, OptionType.ROLE);
    }

    public <T> void registerContext(Class<T> clazz, ContextResolver<T, R> handler) {
        contextMap.put(clazz, handler);
    }

    public <T> void registerMapping(Class<T> clazz, OptionType type) {
        typeMap.put(clazz, type);
    }

    public ContextResolver<?, R> getResolver(Class<?> clazz) {
        return contextMap.get(clazz);
    }

    public OptionType getMapping(Class<?> clazz) {
        return typeMap.get(clazz);
    }

    /**
     * Converts information from a Discord {@link OptionMapping} to a custom structure.
     *
     * @param <T> the type to resolve
     * @param <R> anonymous function to resolve the input
     */
    @FunctionalInterface
    public interface ContextResolver<T, R extends CommandExecutionContext> {
        T resolve(R context) throws IllegalCommandException;
    }
}
