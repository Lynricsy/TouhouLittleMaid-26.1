package com.github.tartaricacid.touhoulittlemaid.network.client;

import com.github.tartaricacid.touhoulittlemaid.client.entity.GeckoMaidEntity;
import com.github.tartaricacid.touhoulittlemaid.compat.ysm.event.UpdateRemoteStructEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.message.SyncYsmMaidDataPackage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.common.NeoForge;

public final class SyncYsmMaidDataPackageProxy {
    public static void handle(SyncYsmMaidDataPackage message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (!(level.getEntity(message.entityId()) instanceof EntityMaid maid)) {
            return;
        }
        NeoForge.EVENT_BUS.post(new UpdateRemoteStructEvent(maid, message.roamingVars()));
        // 女仆模组自带的 Gecko 模型也同步漫游变量（仅已创建动画实体时）
        GeckoMaidEntity<?> geckoEntity = maid.getExistingDataOrNull(GeckoMaidEntity.TYPE);
        if (geckoEntity != null) {
            geckoEntity.updateRoamingVars(message.roamingVars());
        }
        if (message.isRouletteAnimPlaying()) {
            maid.playRouletteAnim(message.rouletteAnim());
        } else {
            maid.stopRouletteAnim();
        }
    }
}
