package com.xlxyvergil.hamstercore.modification.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.xlxyvergil.hamstercore.modification.ModificationConfig;
import com.xlxyvergil.hamstercore.modification.ModificationRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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
        net.minecraft.resources.ResourceLocation lootTableId = context.getQueriedLootTableId();

        for (ModificationConfig.LootPatternMatcher m : ModificationConfig.LOOT_RULES) {
            boolean matches = m.matches(lootTableId);

            if (matches) {
                float roll = context.getRandom().nextFloat();
                boolean chancePassed = roll <= m.chance();

                if (chancePassed) {
                    // 查找玩家（用于获取幸运值和游戏阶段等）
                    Player player = findPlayer(context);
                    
                    ItemStack modification = ModificationRegistry.createRandomModificationStack(
                        context.getRandom(),
                        context.getLevel(),
                        player != null ? context.getLuck() : 0
                    );

                    if (!modification.isEmpty()) {
                        generatedLoot.add(modification);
                    }
                }
                break;
            }
        }
        return generatedLoot;
    }

    /**
     * 查找LootContext中的玩家
     * 参考Apotheosis的GemLootPoolEntry.findPlayer实现
     */
    private static Player findPlayer(LootContext ctx) {
        if (ctx.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player p) return p;
        if (ctx.getParamOrNull(LootContextParams.DIRECT_KILLER_ENTITY) instanceof Player p) return p;
        if (ctx.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof Player p) return p;
        if (ctx.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER) != null) return ctx.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);
        return null;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}