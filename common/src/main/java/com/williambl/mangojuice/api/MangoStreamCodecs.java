package com.williambl.mangojuice.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AccessFlag;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilities for making {@link StreamCodec StreamCodecs}.
 */
public class MangoStreamCodecs {
    /**
     * Creates a {@link StreamCodec} for an enum class.
     * @param enumClass the enum class
     * @return          a StreamCodec which serialises members of the class.
     */
    public static <B extends ByteBuf, E extends Enum<E>> @NotNull StreamCodec<B, E> enumCodec(
            @NotNull Class<E> enumClass) {
        var values = enumClass.getEnumConstants();
        // if you have more than MAX_SHORT enum constants then something else is going to break before this does
        return values.length < Byte.MAX_VALUE
                ? ByteBufCodecs.BYTE.<B>cast().map(i -> values[i.intValue()], e -> (byte) e.ordinal())
                : ByteBufCodecs.SHORT.<B>cast().map(i -> values[i.intValue()], e -> (short) e.ordinal());
    }

    /**
     * Creates a {@link StreamCodec} for a sealed class hierarchy.
     * @param sealedClass           the sealed class
     * @param streamCodecFromClass  a function to get a StreamCodec for each subclass in the sealed hierarchy
     * @return                      a StreamCodec which serialises members of the sealed class hierarchy
     */
    public static <B extends ByteBuf, T> @NotNull StreamCodec<B, T> sealedHierarchy(
            @NotNull Class<T> sealedClass,
            @NotNull Function<Class<? extends T>, StreamCodec<B, ? extends T>> streamCodecFromClass) {
        if (!sealedClass.isSealed()) {
            throw new IllegalArgumentException("`sealedHierarchy` can only be called on a sealed class, not %s".formatted(sealedClass.getName()));
        }
        Map<Class<? extends T>, String> classes2Strings = new HashMap<>();
        Map<String, StreamCodec<B,  ? extends T>> strings2StreamCodecs = new HashMap<>();
        for (var subclass : sealedClass.getPermittedSubclasses()) {
            @SuppressWarnings("unchecked")
            Class<? extends T> castedSubclass = (Class<? extends T>) subclass;
            classes2Strings.put(castedSubclass, subclass.getSimpleName());
            strings2StreamCodecs.put(subclass.getSimpleName(), streamCodecFromClass.apply(castedSubclass));
        }

        return ByteBufCodecs.STRING_UTF8.<B>cast().dispatch(
                instance -> classes2Strings.get(instance.getClass()),
                strings2StreamCodecs::get);
    }

    /**
     * Creates a {@link StreamCodec} for a sealed class hierarchy, finding the subclass StreamCodecs by a static field with the given name.
     * @param sealedClass       the sealed class
     * @param codecFieldName    the name of the StreamCodec field present in every subclass
     * @return                      a StreamCodec which serialises members of the sealed class hierarchy
     */
    public static <B extends ByteBuf, T> @NotNull StreamCodec<B, T> sealedHierarchy(
            @NotNull Class<T> sealedClass,
            @NotNull String codecFieldName) {
        return MangoStreamCodecs.sealedHierarchy(sealedClass, clazz -> {
            try {
                var field = clazz.getDeclaredField(codecFieldName);
                if (field.accessFlags().contains(AccessFlag.STATIC)) {
                    if (!field.canAccess(null)) {
                        field.setAccessible(true);
                    }
                    try {
                        @SuppressWarnings("unchecked") var res = (StreamCodec<B, ? extends T>) field.get(null);
                        return res;
                    } catch (ClassCastException e) {
                        throw new RuntimeException("Field %s on %s was not a StreamCodec".formatted(codecFieldName, clazz.getName()), e);
                    }
                } else {
                    throw new RuntimeException("StreamCodec field %s on %s is not static".formatted(codecFieldName, clazz.getName()));
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not find StreamCodec field %s on %s".formatted(codecFieldName, clazz.getName()), e);
            }
        });
    }
}
