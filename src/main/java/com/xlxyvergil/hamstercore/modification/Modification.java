package com.xlxyvergil.hamstercore.modification;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xlxyvergil.hamstercore.weapon.WeaponCategory;
import com.xlxyvergil.hamstercore.weapon.WeaponType;
import com.xlxyvergil.hamstercore.weapon.WeaponTypeDetector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

public record Modification(
    ResourceLocation id,
    int weight,
    float quality,
    Set<ResourceLocation> dimensions,
    ModificationCategory category,
    boolean unique,
    ModificationRarity rarity,
    List<ModificationAffix> affixes,
    List<WeaponCategory> applicableCategories,
    List<WeaponType> applicableTypes,
    Set<String> mutualExclusionGroups
) {
    public static final Codec<Modification> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        ResourceLocation.CODEC.fieldOf("variant").forGetter(Modification::id),
        Codec.INT.fieldOf("weight").forGetter(Modification::weight),
        Codec.FLOAT.fieldOf("quality").forGetter(Modification::quality),
        ResourceLocation.CODEC.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("dimensions").forGetter(Modification::dimensions),
        ModificationCategory.CODEC.fieldOf("category").forGetter(Modification::category),
        Codec.BOOL.optionalFieldOf("unique", false).forGetter(Modification::unique),
        ModificationRarity.CODEC.fieldOf("rarity").forGetter(Modification::rarity),
        ModificationAffix.LIST_CODEC.fieldOf("affixes").forGetter(Modification::affixes),
        WeaponCategory.CODEC.listOf().optionalFieldOf("applicableCategories", List.of()).forGetter(Modification::applicableCategories),
        WeaponType.CODEC.listOf().optionalFieldOf("applicableTypes", List.of()).forGetter(Modification::applicableTypes),
        Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("mutualExclusionGroups", Set.<String>of()).forGetter(Modification::mutualExclusionGroups)
    ).apply(inst, Modification::new));

    public boolean canApplyTo(ItemStack socketed, ItemStack modification) {
        // 获取物品上已有的所有改装件
        List<Modification> existingMods = ModificationHelper.getModifications(socketed).streamValidModifications()
            .map(ModificationInstance::modification)
            .toList();
        
        // 检查唯一性
        if (this.unique) {
            for (Modification mod : existingMods) {
                if (mod.id().equals(this.id())) {
                    return false;
                }
            }
        }
        
        // 检查互斥组
        if (!this.mutualExclusionGroups.isEmpty()) {
            for (Modification existingMod : existingMods) {
                // 检查现有改装件是否与当前改装件共享任何互斥组
                for (String group : this.mutualExclusionGroups) {
                    if (existingMod.mutualExclusionGroups().contains(group)) {
                        return false;
                    }
                }
            }
        }
        
        // 检测武器类型
        WeaponType weaponType = WeaponTypeDetector.detectWeaponType(socketed);
        if (weaponType == null) {
            return false;
        }
        
        // 检查适用分类
        if (!applicableCategories.isEmpty()) {
            boolean categoryMatch = applicableCategories.stream()
                    .anyMatch(category -> category.allowsWeaponType(weaponType));
            if (!categoryMatch) {
                return false;
            }
        }
        
        // 检查适用类型
        if (!applicableTypes.isEmpty()) {
            boolean typeMatch = applicableTypes.contains(weaponType);
            if (!typeMatch) {
                return false;
            }
        }
        
        return true;
    }
}
