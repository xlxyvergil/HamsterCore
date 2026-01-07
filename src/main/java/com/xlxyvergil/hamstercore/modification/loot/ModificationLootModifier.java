package com.xlxyvergil.hamstercore.modification.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.xlxyvergil.hamstercore.modification.ModificationConfig;
import com.xlxyvergil.hamstercore.modification.ModificationRegistry;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

// 完全复制Apotheosis的GemLootModifier，只修改必要的部分
public class ModificationLootModifier extends LootModifier {

    public static final Codec<ModificationLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, ModificationLootModifier::new));

    protected ModificationLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // 遍历所有战利品规则
        for (ModificationConfig.LootPatternMatcher m : ModificationConfig.LOOT_RULES) {
            if (m.matches(context.getQueriedLootTableId())) {
                if (context.getRandom().nextFloat() <= m.chance()) {
                    // 获取幸运值
                    float luck = context.getLuck();
                    
                    // 生成随机改装件 - 使用IDimensional过滤器，与Apotheosis保持一致
                    ItemStack modification = ModificationRegistry.createRandomModificationStack(
                        context.getRandom(),
                        context.getLevel(),
                        luck,
                        IDimensional.matches(context.getLevel())
                    );
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