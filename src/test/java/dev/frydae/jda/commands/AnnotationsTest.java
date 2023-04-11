package dev.frydae.jda.commands;

import dev.frydae.jda.commands.annotations.CommandAlias;
import dev.frydae.jda.commands.annotations.Subcommand;
import dev.frydae.jda.commands.core.Annotations;
import dev.frydae.jda.commands.core.CommandManager;
import dev.frydae.jda.testing.annotations.CommandTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CommandTest
public class AnnotationsTest {
    private Annotations annotations;

    @BeforeEach
    public void setup() {
        annotations = CommandManager.getAnnotations();
    }

    @Test
    public void testSimpleAlias() {
        String alias = annotations.getAnnotationValue(SimpleCommandClass.class, CommandAlias.class);

        assertNotNull(alias);
        assertEquals("simple", alias);
    }

    @Test
    public void testSimpleSubcommand() throws NoSuchMethodException {
        Method subCommandMethod = SimpleCommandClass.class.getMethod("onSubCommand");

        String subcommand = annotations.getAnnotationValue(subCommandMethod, Subcommand.class);

        assertNotNull(subcommand);
        assertEquals("simple", subcommand);

        fail();
    }
}
