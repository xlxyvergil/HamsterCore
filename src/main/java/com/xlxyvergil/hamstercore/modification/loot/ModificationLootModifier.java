package com.xlxyvergil.hamstercore.modification.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.modification.ModificationConfig;
import com.xlxyvergil.hamstercore.modification.ModificationRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class ModificationLootModifier extends LootModifier {
    
    public static final Codec<ModificationLootModifier> CODEC = RecordCodecBuilder.create(inst -> 
        codecStart(inst).apply(inst, ModificationLootModifier::new));
    
    protected ModificationLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }
    
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // 1. 只处理实体掉落
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof Mob)) {
            return generatedLoot;
        }
        
        // 2. 只允许MONSTER分类的生物掉落
        Mob mob = (Mob) entity;
        if (mob.getType().getCategory() != MobCategory.MONSTER) {
            return generatedLoot;
        }
        
        // 3. 应用配置的掉落规则
        for (ModificationConfig.LootPatternMatcher m : ModificationConfig.LOOT_RULES) {
            if (m.matches(context.getQueriedLootTableId())) {
                // 4. 检查掉落概率
                if (context.getRandom().nextFloat() <= m.chance()) {
                    // 5. 生成随机改装件
                    ItemStack modification = ModificationRegistry.createRandomModificationStack(
                        context.getRandom(),
                        context.getLevel()
                    );
                    // 6. 添加到掉落物
                    generatedLoot.add(modification);
                }
                break;
            }
        }
        
        return generatedLoot;
    }
    
    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}