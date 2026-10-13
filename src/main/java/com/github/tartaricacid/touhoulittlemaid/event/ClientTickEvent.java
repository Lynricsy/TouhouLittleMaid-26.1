package com.github.tartaricacid.touhoulittlemaid.event;

import com.mojang.blaze3d.platform.VideoMode;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientTickEvent {
    private static int tickCount;
    private static int refreshRate = 60;

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Pre event) {
        tickCount++;
        VideoMode videoMode = Minecraft.getInstance().getWindow().getActiveVideoMode();
        refreshRate = videoMode == null ? 60 : Math.round(videoMode.getRefreshRate());
    }

    public static int getTickCount() {
        return tickCount;
    }

    public static int getRefreshRate() {
        return refreshRate;
    }
}
