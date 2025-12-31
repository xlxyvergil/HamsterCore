package com.xlxyvergil.hamstercore.modification;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record ModificationAffix(
    String type,
    String operation,
    double value,
    String source
) {
    public static final Codec<ModificationAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.STRING.fieldOf("type").forGetter(ModificationAffix::type),
        Codec.STRING.fieldOf("operation").forGetter(ModificationAffix::operation),
        Codec.DOUBLE.fieldOf("value").forGetter(ModificationAffix::value),
        Codec.STRING.fieldOf("source").forGetter(ModificationAffix::source)
    ).apply(inst, ModificationAffix::new));

    public static final Codec<List<ModificationAffix>> LIST_CODEC = CODEC.listOf();
}
