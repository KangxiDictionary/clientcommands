package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.clientcommands.platform.CClientCommandSource;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.Collection;

public class CIdentifierArgument implements ArgumentType<Identifier> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

	public static CIdentifierArgument id() {
		return new CIdentifierArgument();
	}

	public static Identifier getId(final CommandContext<CClientCommandSource> context, final String name) {
		return context.getArgument(name, Identifier.class);
	}

	@Override
	public Identifier parse(final StringReader stringReader) throws CommandSyntaxException {
		return Identifier.read(stringReader);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
