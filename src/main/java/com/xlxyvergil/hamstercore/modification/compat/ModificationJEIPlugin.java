package com.xlxyvergil.hamstercore.modification.compat;

import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import com.xlxyvergil.hamstercore.modification.ModificationItem;
import com.xlxyvergil.hamstercore.modification.ModificationItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

@JeiPlugin
public class ModificationJEIPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID = new ResourceLocation("hamstercore", "modification_jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        // 注册改装件的子类型解释器
        reg.registerSubtypeInterpreter(ModificationItems.MODIFICATION.get(), new ModificationSubtypes());
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        // 不需要注册额外配方
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        // 不需要注册配方催化剂
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        // 不需要注册配方分类
    }

    /**
     * 改装件子类型解释器，用于JEI识别不同的改装件
     */
    static class ModificationSubtypes implements IIngredientSubtypeInterpreter<ItemStack> {

        @Override
        public String apply(ItemStack stack, UidContext context) {
            // 获取改装件实例
            ModificationInstance inst = ModificationItem.getModification(stack);
            if (!inst.isValid()) {
                return ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
            }
            // 使用改装件ID作为子类型，这样JEI就能区分不同的改装件
            return inst.modificationId();
        }
    }
}