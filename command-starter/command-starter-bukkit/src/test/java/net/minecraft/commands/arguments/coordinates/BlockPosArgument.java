package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;

/**
 * Mock class for testing purposes
 */
@SuppressWarnings("unused")
public final class BlockPosArgument implements ArgumentType<Object> {

    @Override
    public Object parse(final StringReader reader) {
        throw new UnsupportedOperationException();
    }

}
