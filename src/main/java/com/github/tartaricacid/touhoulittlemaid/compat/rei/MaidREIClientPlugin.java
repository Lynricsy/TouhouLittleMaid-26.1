package com.github.tartaricacid.touhoulittlemaid.compat.rei;

import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.AbstractMaidContainerGui;
import com.github.tartaricacid.touhoulittlemaid.compat.rei.altar.ReiAltarRecipeCategory;
import com.github.tartaricacid.touhoulittlemaid.compat.rei.altar.ReiAltarRecipeDisplay;
import com.github.tartaricacid.touhoulittlemaid.compat.rei.handler.WirelessIOStackVisitor;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * REI 客户端插件：分类、折叠组、信息页、排除区与拖拽
 * <p>
 * 祭坛配方展示由 {@link MaidREICommonPlugin} 在服务端生成并同步过来
 */
@REIPluginClient
public class MaidREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ReiAltarRecipeCategory());
        registry.addWorkstations(ReiAltarRecipeDisplay.ALTAR,
                EntryStacks.of(InitItems.HAKUREI_GOHEI.get()),
                EntryStacks.of(InitItems.SANAE_GOHEI.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        Item garageKit = InitItems.GARAGE_KIT.get();
        registry.add(DefaultInformationDisplay.createFromEntry(EntryStacks.of(garageKit), Component.translatable(garageKit.getDescriptionId()))
                .line(Component.translatable("jei.touhou_little_maid.garage_kit.info")));
    }

    @Override
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        // 手办与坐垫的变种很多，折叠成组显示
        for (Item item : List.of(InitItems.GARAGE_KIT.get(), InitItems.CHAIR.get())) {
            registry.group(BuiltInRegistries.ITEM.getKey(item), Component.translatable(item.getDescriptionId()),
                    VanillaEntryTypes.ITEM, stack -> stack.getValue().is(item));
        }
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new WirelessIOStackVisitor());
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(AbstractMaidContainerGui.class, (ExclusionZonesProvider<AbstractMaidContainerGui<?>>) screen -> {
            List<Rect2i> areas = screen.getExclusionArea();
            return areas.stream().map(rect -> new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight())).toList();
        });
    }
}
