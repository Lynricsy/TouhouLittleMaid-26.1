package com.github.tartaricacid.touhoulittlemaid.compat.rei.altar;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import com.github.tartaricacid.touhoulittlemaid.init.InitRecipes;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class ReiAltarRecipeMaker {
    private ReiAltarRecipeMaker() {
    }

    /**
     * REI 26.x 的配方展示在服务端由配方管理器生成，再经序列化器同步到客户端
     */
    public static void registerAltarRecipes(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(AltarRecipe.class)
                .filterType(InitRecipes.ALTAR_RECIPE.get())
                .fill(ReiAltarRecipeMaker::create);
    }

    private static ReiAltarRecipeDisplay create(RecipeHolder<AltarRecipe> holder) {
        AltarRecipe recipe = holder.value();
        List<EntryIngredient> inputs = EntryIngredients.ofIngredients(recipe.getIngredients());
        List<EntryIngredient> outputs = List.of(EntryIngredients.of(recipe.getResult()));
        return new ReiAltarRecipeDisplay(holder.id().identifier(), inputs, outputs, recipe.getPower(), recipe.getLangKey());
    }
}
