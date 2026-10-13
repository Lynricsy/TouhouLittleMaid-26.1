package com.github.tartaricacid.touhoulittlemaid.client.proxy;

import net.minecraft.client.Minecraft;

/**
 * 残局道具的客户端调用隔离，避免服务端加载客户端类
 */
public class ItemBoardStateProxy {
    public static boolean hasShiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }
}
