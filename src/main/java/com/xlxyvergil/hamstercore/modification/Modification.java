package com.xlxyvergil.hamstercore.modification;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xlxyvergil.hamstercore.weapon.WeaponCategory;
import com.xlxyvergil.hamstercore.weapon.WeaponType;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.ILuckyWeighted;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;

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
        // 显示具体的武器类型
        for (WeaponType weaponType : this.category.getAllowedTypes()) {
            tooltip.add(Component.translatable("hamstercore.modification.dot_prefix",
                weaponType.getDisplayName()).withStyle(ChatFormatting.GREEN));
        }
        tooltip.add(Component.empty());
        
        // 3. 显示安装时 affixes里的属性
        tooltip.add(Component.translatable("hamstercore.modification.when_modified").withStyle(ChatFormatting.GRAY));
        for (ModificationAffix affix : this.affixes) {
            // 根据affix.type()获取Attribute实例
            ResourceLocation attrRl = ResourceLocation.parse(affix.type());
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attrRl);
            
            if (attribute != null) {
                // 使用Attribute的descriptionId作为翻译键，这是最准确的方式
                Component attrName = Component.translatable(attribute.getDescriptionId());
                
                double value = affix.value();
                Component valueText;
                
                // 检查Attribute是否实现了IFormattableAttribute接口
                if (attribute instanceof IFormattableAttribute formattableAttr) {
                    // 使用IFormattableAttribute格式化属性值
                    Operation op = Operation.valueOf(affix.operation());
                    TooltipFlag flag = TooltipFlag.Default.NORMAL;
                    valueText = formattableAttr.toValueComponent(op, value, flag);
                    
                    // 对于IFormattableAttribute，直接使用其格式化的结果，因为它已经包含了百分号
                    // 获取前缀的翻译键
                    String langKey = value >= 0 ? "hamstercore.modification.value.increase_prefix" : "hamstercore.modification.value.decrease_prefix";
                    Component prefix = Component.translatable(langKey);
                    Component finalValueText = Component.literal(prefix.getString() + valueText.getString());
                    
                    tooltip.add(Component.translatable("hamstercore.modification.dot_prefix",
                        Component.translatable("%s: %s", attrName, finalValueText)).withStyle(ChatFormatting.GOLD));
                    continue;
                } else {
                    // 对于未实现IFormattableAttribute的属性，使用标准格式化
                    double percentValue = Math.abs(value) * 100;
                    String formattedValue = String.format("%.0f", percentValue);
                    valueText = Component.literal(formattedValue);
                }
                
                // 根据数值正负添加前缀
                String langKey = value >= 0 ? "hamstercore.modification.value.increase" : "hamstercore.modification.value.decrease";
                Component finalValueText = Component.translatable(langKey, valueText.getString());
                
                tooltip.add(Component.translatable("hamstercore.modification.dot_prefix",
                    Component.translatable("%s: %s", attrName, finalValueText)).withStyle(ChatFormatting.GOLD));
            }
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
