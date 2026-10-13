package com.github.tartaricacid.touhoulittlemaid.compat.rei;

import com.github.tartaricacid.touhoulittlemaid.compat.rei.altar.ReiAltarRecipeDisplay;
import com.github.tartaricacid.touhoulittlemaid.compat.rei.altar.ReiAltarRecipeMaker;
import com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.forge.REIPluginCommon;

/**
 * REI 通用插件：双端都会加载，只能引用通用类
 */
@REIPluginCommon
public class MaidREICommonPlugin implements REICommonPlugin {
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(IdentifierUtil.modLoc("altar"), ReiAltarRecipeDisplay.SERIALIZER);
    }

    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        ReiAltarRecipeMaker.registerAltarRecipes(registry);
    }
}
