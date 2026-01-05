package com.xlxyvergil.hamstercore.modification;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;


import java.util.List;

/**
 * 改装件词缀定义
 */
public record ModificationAffix(
    String type,      // 词缀类型：命名空间+属性id
    String name,      // 属性显示名称
    double value,     // 数值
    String operation  // 计算方法：ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL
) {

    public static final Codec<ModificationAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.STRING.fieldOf("type").forGetter(ModificationAffix::type),
        Codec.STRING.fieldOf("name").forGetter(ModificationAffix::name),
        Codec.DOUBLE.fieldOf("value").forGetter(ModificationAffix::value),
        Codec.STRING.fieldOf("operation").forGetter(ModificationAffix::operation)
    ).apply(inst, ModificationAffix::new));

    public static final Codec<List<ModificationAffix>> LIST_CODEC = CODEC.listOf();
}
