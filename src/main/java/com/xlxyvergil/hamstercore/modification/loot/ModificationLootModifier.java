package com.xlxyvergil.hamstercore.modification.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.xlxyvergil.hamstercore.modification.ModificationConfig;
import com.xlxyvergil.hamstercore.modification.ModificationRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

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
                    
                    // 直接使用ModificationRegistry的createRandomModificationStack方法
                    // 不需要额外的ServerLevel检查，createRandomModificationStack内部会处理
                    ItemStack modification = ModificationRegistry.createRandomModificationStack(
                        context.getRandom(),
                        context.getLevel(),
                        luck
                    );
                    
                    // 只有当改装件生成成功时才添加到掉落物中
                    if (!modification.isEmpty()) {
                        generatedLoot.add(modification);
                    }
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