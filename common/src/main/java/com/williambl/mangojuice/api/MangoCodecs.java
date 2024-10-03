package com.williambl.mangojuice.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AccessFlag;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilities for making {@link Codec Codecs}.
 * @see MangoStreamCodecs
 */
public class MangoCodecs {

    /**
     * Creates a {@link Codec} for a sealed class hierarchy.
     * @param sealedClass           the sealed class
     * @param  mapCodecFromClass    a function to get a {@link MapCodec} for each subclass in the sealed hierarchy
     * @return                      a Codec which serialises members of the sealed class hierarchy
     */
    public static <T> @NotNull Codec<T> sealedHierarchy(
            @NotNull Class<T> sealedClass,
            @NotNull Function<Class<? extends T>, MapCodec<? extends T>> mapCodecFromClass) {
        if (!sealedClass.isSealed()) {
            throw new IllegalArgumentException("`sealedHierarchy` can only be called on a sealed class, not %s".formatted(sealedClass.getName()));
        }
        Map<Class<? extends T>, String> classes2Strings = new HashMap<>();
        Map<String, MapCodec<? extends T>> strings2MapCodecs = new HashMap<>();
        for (var subclass : sealedClass.getPermittedSubclasses()) {
            @SuppressWarnings("unchecked")
            Class<? extends T> castedSubclass = (Class<? extends T>) subclass;
            classes2Strings.put(castedSubclass, subclass.getSimpleName());
            strings2MapCodecs.put(subclass.getSimpleName(), mapCodecFromClass.apply(castedSubclass));
        }

        return Codec.STRING.dispatch(
                instance -> classes2Strings.get(instance.getClass()),
                strings2MapCodecs::get);
    }

    /**
     * Creates a {@link Codec} for a sealed class hierarchy, finding the subclass {@link MapCodec MapCodecs} by a static field with the given name.
     * @param sealedClass       the sealed class
     * @param codecFieldName    the name of the MapCodec field present in every subclass
     * @return                  a Codec which serialises members of the sealed class hierarchy
     */
    public static <T> @NotNull Codec<T> sealedHierarchy(
            @NotNull Class<T> sealedClass,
            @NotNull String codecFieldName) {
        return MangoCodecs.sealedHierarchy(sealedClass, clazz -> {
            try {
                var field = clazz.getDeclaredField(codecFieldName);
                if (field.accessFlags().contains(AccessFlag.STATIC)) {
                    if (!field.canAccess(null)) {
                        field.setAccessible(true);
                    }
                    try {
                        @SuppressWarnings("unchecked") var res = (MapCodec<? extends T>) field.get(null);
                        return res;
                    } catch (ClassCastException e) {
                        throw new RuntimeException("Field %s on %s was not a MapCodec".formatted(codecFieldName, clazz.getName()), e);
                    }
                } else {
                    throw new RuntimeException("MapCodec field %s on %s is not static".formatted(codecFieldName, clazz.getName()));
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not find MapCodec field %s on %s".formatted(codecFieldName, clazz.getName()), e);
            }
        });
    }
}
