package com.github.tartaricacid.touhoulittlemaid.compat.rei.handler;

import com.github.tartaricacid.touhoulittlemaid.client.gui.item.WirelessIOContainerGui;
import com.github.tartaricacid.touhoulittlemaid.network.message.WirelessIOFilterSlotPackage;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.tartaricacid.touhoulittlemaid.inventory.handler.WirelessIOItemHandler.FILTER_LIST_SIZE;

/**
 * 允许把 REI 列表中的物品直接拖到无线 IO 的过滤槽中
 */
public class WirelessIOStackVisitor implements DraggableStackVisitor<WirelessIOContainerGui> {
    @Override
    public <R extends Screen> boolean isHandingScreen(R screen) {
        return screen instanceof WirelessIOContainerGui;
    }

    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<WirelessIOContainerGui> context, DraggableStack stack) {
        if (stack.getStack().getType() != VanillaEntryTypes.ITEM) {
            return Stream.empty();
        }
        WirelessIOContainerGui gui = context.getScreen();
        return IntStream.range(0, FILTER_LIST_SIZE).mapToObj(i -> BoundsProvider.ofRectangle(getSlotArea(gui, i)));
    }

    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<WirelessIOContainerGui> context, DraggableStack stack) {
        EntryStack<?> entry = stack.getStack();
        if (entry.getType() != VanillaEntryTypes.ITEM) {
            return DraggedAcceptorResult.PASS;
        }
        WirelessIOContainerGui gui = context.getScreen();
        Point mouse = context.getCurrentPosition();
        for (int i = 0; i < FILTER_LIST_SIZE; i++) {
            if (getSlotArea(gui, i).contains(mouse)) {
                ItemStack itemStack = entry.castValue();
                ClientPacketDistributor.sendToServer(new WirelessIOFilterSlotPackage(i, itemStack.copyWithCount(1)));
                return DraggedAcceptorResult.ACCEPTED;
            }
        }
        return DraggedAcceptorResult.PASS;
    }

    private static Rectangle getSlotArea(WirelessIOContainerGui gui, int index) {
        int x = gui.getLeftPos() + 62 + index % 3 * 18;
        int y = gui.getTopPos() + 17 + index / 3 * 18;
        return new Rectangle(x, y, 16, 16);
    }
}
