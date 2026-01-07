package com.xlxyvergil.hamstercore.modification;

import com.xlxyvergil.hamstercore.HamsterCore;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;


/**
 * 改装件注册表
 */
public class ModificationRegistry extends WeightedDynamicRegistry<Modification> {

    private static final Logger LOGGER = LogManager.getLogger("HamsterCore : Modification");
    public static final ModificationRegistry INSTANCE = new ModificationRegistry();

    public ModificationRegistry() {
        super(LOGGER, "modifications", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(new ResourceLocation(HamsterCore.MODID + ":modification"), Modification.CODEC);
    }

    /**
     * 创建随机改装件物品堆
     */
    public static ItemStack createRandomModificationStack(RandomSource rand, ServerLevel level) {
        // 获取当前维度ID
        ResourceLocation dimensionId = level.dimension().location();
        
        // 过滤出适合当前维度的改装件
        List<Modification> validMods = new ArrayList<>();
        for (Modification mod : INSTANCE.getValues()) {
            // 如果维度列表为空，或者包含当前维度，则有效
            if (mod.dimensions().isEmpty() || mod.dimensions().contains(dimensionId)) {
                validMods.add(mod);
            }
        }
        
        // 如果没有有效改装件，返回空
        if (validMods.isEmpty()) return ItemStack.EMPTY;
        
        // 从有效改装件中随机选择
        Modification mod = validMods.get(rand.nextInt(validMods.size()));
        
        ItemStack stack = ModificationItem.createModificationStack(mod);
        return stack;
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
