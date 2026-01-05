package com.xlxyvergil.hamstercore.modification;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xlxyvergil.hamstercore.weapon.WeaponCategory;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.ILuckyWeighted;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 改装件定义 - 模仿 Apotheosis 的 Gem
 */
public record Modification(
    ResourceLocation id,
    int weight,
    Set<ResourceLocation> dimensions,
    WeaponCategory category,
    boolean unique,
    ModificationRarity rarity,
    List<ModificationAffix> affixes,
    boolean useSpecialSocket,     // 是否使用特殊槽位，true为特殊槽位，false为普通槽位
    List<String> mutualExclusionGroups,  // 互斥组，同组改装件不能同时安装
    UUID uuid                           // 改装件UUID，用于词缀生成
) implements CodecProvider<Modification>, ILuckyWeighted {

    public static final Codec<Modification> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        ResourceLocation.CODEC.fieldOf("variant").forGetter(Modification::id),
        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(Modification::weight),
        Codec.STRING.listOf().xmap(
            strings -> strings.stream().map(ResourceLocation::new).collect(java.util.stream.Collectors.toSet()),
            set -> set.stream().map(ResourceLocation::toString).collect(java.util.stream.Collectors.toList())
        ).optionalFieldOf("dimensions", Set.of()).forGetter(Modification::dimensions),
        WeaponCategory.CODEC.fieldOf("category").forGetter(Modification::category),
        Codec.BOOL.optionalFieldOf("unique", false).forGetter(Modification::unique),
        ModificationRarity.CODEC.fieldOf("rarity").forGetter(Modification::rarity),
        ModificationAffix.LIST_CODEC.fieldOf("affixes").forGetter(Modification::affixes),
        Codec.BOOL.optionalFieldOf("use_special_socket", false).forGetter(Modification::useSpecialSocket),
        Codec.STRING.listOf().optionalFieldOf("mutualExclusionGroups", List.of()).forGetter(Modification::mutualExclusionGroups),
        Codec.STRING.xmap(UUID::fromString, UUID::toString).optionalFieldOf("uuid", UUID.randomUUID()).forGetter(Modification::uuid)
    ).apply(inst, Modification::new));

    @Override
    public Codec<? extends Modification> getCodec() {
        return CODEC;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public float getQuality() {
        // 固定返回1.0f，因为我们不需要品质字段
        return 1.0f;
    }

    /**
     * 检查改装件是否可以应用到物品上
     */
    public boolean canApplyTo(ItemStack socketed, ItemStack modification) {
        return true; // 简化实现，不需要复杂检查
    }

    /**
     * 添加信息到 tooltip
     */
    public void addInformation(ItemStack stack, List<Component> tooltip) {
        // 只有当改装件是装备上的一部分时才显示"已安装"
        if (stack.getItem() != com.xlxyvergil.hamstercore.modification.ModificationItems.MODIFICATION.get()) {
            tooltip.add(Component.translatable("hamstercore.modification.installed").withStyle(ChatFormatting.GRAY));
        }
        
        // 添加词缀信息
        for (ModificationAffix affix : this.affixes) {
            String attrKey = "attribute.name." + affix.type().replace(':', '.');
            Component attrName = Component.translatable(attrKey);
            Component valueText = Component.literal(String.format("%.2f", affix.value()));
            tooltip.add(Component.translatable("hamstercore.modification.dot_prefix",
                Component.translatable("%s: %s", attrName, valueText)).withStyle(ChatFormatting.GOLD));
        }
        
        tooltip.add(Component.translatable("hamstercore.modification.rarity." + this.rarity.name().toLowerCase())
                .withStyle(this.rarity.getColor()));
        
        // 显示槽位类型
        if (this.useSpecialSocket) {
            tooltip.add(Component.translatable("hamstercore.modification.special_socket").withStyle(ChatFormatting.BLUE));
        } else {
            tooltip.add(Component.translatable("hamstercore.modification.normal_socket").withStyle(ChatFormatting.GREEN));
        }
        
        if (this.unique) {
            tooltip.add(Component.translatable("hamstercore.modification.unique").withStyle(ChatFormatting.GOLD));
        }
    }
}
