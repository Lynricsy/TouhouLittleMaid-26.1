package com.github.tartaricacid.touhoulittlemaid.inventory.tooltip;

import com.github.tartaricacid.touhoulittlemaid.entity.data.YsmData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

public record ItemMaidTooltip(String modelId, @Nullable Component customName, YsmData ysmData) implements TooltipComponent {
}
