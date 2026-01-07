package com.xlxyvergil.hamstercore.modification;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xlxyvergil.hamstercore.weapon.WeaponCategory;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.ILuckyWeighted;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 改装件定义
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
) implements CodecProvider<Modification>, ILuckyWeighted, IDimensional {

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
        Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(Modification::uuid)
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
    
    @Override
    public Set<ResourceLocation> getDimensions() {
        return this.dimensions;
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
        if (stack.getItem() != ModificationItems.MODIFICATION.get()) {
            tooltip.add(Component.translatable("hamstercore.modification.installed").withStyle(ChatFormatting.GRAY));
        }
        
        // 2. 显示适用于 category里的分类
        tooltip.add(Component.translatable("hamstercore.modification.socketable_into").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("hamstercore.modification.dot_prefix",
            Component.literal(this.category.getDisplayName())).withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.empty());
        
        // 3. 显示安装时 affixes里的属性
        tooltip.add(Component.translatable("hamstercore.modification.when_modified").withStyle(ChatFormatting.GRAY));
        for (ModificationAffix affix : this.affixes) {
            String attrKey = "attribute.name." + affix.type().replace(':', '.');
            Component attrName = Component.translatable(attrKey);

            // 根据操作类型和值来格式化显示
            double value = affix.value();
            String operation = affix.operation();
            Component valueText;

            if ("ADDITION".equals(operation)) {
                // 加法操作：显示百分比，标明增加或减少
                double percentValue = Math.abs(value) * 100;
                String langKey = value >= 0 ? "hamstercore.modification.value.addition" : "hamstercore.modification.value.reduction";
                valueText = Component.translatable(langKey, String.format("%.0f", percentValue));
            } else if ("MULTIPLY_BASE".equals(operation) || "MULTIPLY_TOTAL".equals(operation)) {
                // 乘法操作：显示百分比，标明增加或减少
                double percentValue = Math.abs(value) * 100;
                String langKey = value >= 0 ? "hamstercore.modification.value.multiply_increase" : "hamstercore.modification.value.multiply_decrease";
                valueText = Component.translatable(langKey, String.format("%.0f", percentValue));
            } else {
                // 默认情况
                valueText = Component.literal(String.format("%.2f", value));
            }

            tooltip.add(Component.translatable("hamstercore.modification.dot_prefix",
                Component.translatable("%s: %s", attrName, valueText)).withStyle(ChatFormatting.GOLD));
        }
        tooltip.add(Component.empty());
        
        // 4. 显示稀有度 rarity
        tooltip.add(Component.translatable("hamstercore.modification.rarity." + this.rarity.name().toLowerCase())
                .withStyle(this.rarity.getColor()));
        
        // 5. 显示安装槽位 use_special_socket
        if (this.useSpecialSocket) {
            tooltip.add(Component.translatable("hamstercore.modification.special_socket").withStyle(ChatFormatting.BLUE));
        } else {
            tooltip.add(Component.translatable("hamstercore.modification.normal_socket").withStyle(ChatFormatting.GREEN));
        }
        
        // 6. 显示互斥组 mutualExclusionGroups
        java.util.Set<Component> mutualExclusions = new java.util.HashSet<>();
        for (String group : this.mutualExclusionGroups()) {
            // 查找所有同组的其他改装件
            for (Modification otherMod : ModificationRegistry.INSTANCE.getValues()) {
                if (otherMod != this && otherMod.mutualExclusionGroups().contains(group)) {
                    // 添加其他改装件的名称
                    Component modName = Component.translatable("item.hamstercore.modification:" + otherMod.id().getPath());
                    mutualExclusions.add(modName);
                }
            }
        }
        
        if (!mutualExclusions.isEmpty()) {
            tooltip.add(Component.translatable("hamstercore.modification.mutual_exclusion_groups").withStyle(ChatFormatting.YELLOW));
            for (Component modName : mutualExclusions) {
                tooltip.add(Component.translatable("hamstercore.modification.dot_prefix",
                    modName).withStyle(ChatFormatting.YELLOW));
            }
        }
        
        // 显示唯一属性
        if (this.unique) {
            tooltip.add(Component.translatable("hamstercore.modification.unique").withStyle(ChatFormatting.GOLD));
        }
    }
}
