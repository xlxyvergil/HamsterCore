package com.xlxyvergil.hamstercore.modification;

import com.xlxyvergil.hamstercore.HamsterCore;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.Set;

public class ModificationRegistry extends WeightedDynamicRegistry<Modification> {

    private static final Logger LOGGER = LogManager.getLogger("HamsterCore : Modification");
    public static final ModificationRegistry INSTANCE = new ModificationRegistry();

    public ModificationRegistry() {
        super(LOGGER, "modifications", true, false); // 保持delayLoad=true
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(new ResourceLocation(HamsterCore.MODID + ":modification"), Modification.CODEC);
    }

    /**
     * 创建随机改装件物品堆
     */
    @SafeVarargs
    public static ItemStack createRandomModificationStack(RandomSource rand, Level level, float luck, java.util.function.Predicate<Modification>... filter) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return ItemStack.EMPTY;
        }

        java.util.function.Predicate<Modification> finalFilter = mod -> {
            for (java.util.function.Predicate<Modification> f : filter) {
                if (!f.test(mod)) return false;
            }
            Set<ResourceLocation> modDims = mod.getDimensions();
            return modDims.isEmpty() || modDims.contains(serverLevel.dimension().location());
        };

        Modification mod = INSTANCE.getRandomItem(rand, luck, finalFilter);
        if (mod == null) {
            return ItemStack.EMPTY;
        }

        return ModificationItem.createModificationStack(mod);
    }

    /**
     * 创建指定改装件的物品堆
     */
    public static ItemStack createModificationStack(Modification modification) {
        ItemStack stack = new ItemStack(com.xlxyvergil.hamstercore.modification.ModificationItems.MODIFICATION.get());
        ModificationItem.setModification(stack, modification);
        return stack;
    }
    
}