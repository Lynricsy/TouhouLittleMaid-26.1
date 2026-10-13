package com.github.tartaricacid.touhoulittlemaid.entity.passive;

import com.github.tartaricacid.touhoulittlemaid.compat.ysm.YsmCompat;
import com.github.tartaricacid.touhoulittlemaid.compat.ysm.event.YsmMaidClientTickEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.data.YsmData;
import com.github.tartaricacid.touhoulittlemaid.network.message.SyncYsmMaidDataPackage;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;

import static com.github.tartaricacid.touhoulittlemaid.init.InitDataAttachment.YSM;

/**
 * YSM（Yes Steve Model）兼容管理器
 * <p>
 * 模型数据存放在 {@link YsmData} 附件中（自动持久化与同步），
 * 轮盘动画与漫游变量为 YSM 运行时数据，由本管理器保存并通过 {@link SyncYsmMaidDataPackage} 同步
 */
@MaidManagerDef(alias = "ysmManager", exposeView = true)
public class MaidYsmManager {
    private static final String YSM_ROULETTE_ANIM_TAG = "YsmRouletteAnim";
    private static final String YSM_ROAMING_VARS_TAG = "YsmRoamingVars";
    private static final String YSM_ROAMING_UPDATE_FLAG_TAG = "YsmRoamingUpdateFlag";
    private static final String EMPTY_ROULETTE_ANIM = "empty";
    private static final Codec<Map<String, Float>> ROAMING_VARS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.FLOAT);

    private final EntityMaid maid;

    public boolean rouletteAnimPlaying = false;
    public String rouletteAnim = EMPTY_ROULETTE_ANIM;
    public boolean rouletteAnimDirty = false;
    public int roamingVarsUpdateFlag = 0;
    public Object2FloatOpenHashMap<String> roamingVars = new Object2FloatOpenHashMap<>();

    MaidYsmManager(EntityMaid maid) {
        this.maid = maid;
    }

    void tick() {
        if (!YsmCompat.isInstalled() || !this.isYsmModel()) {
            return;
        }
        if (maid.level().isClientSide()) {
            // 触发 YSM 模型的客户端事件
            NeoForge.EVENT_BUS.post(new YsmMaidClientTickEvent(maid));
        } else if (this.rouletteAnimDirty) {
            // 同步 YSM 轮盘数据
            this.rouletteAnimDirty = false;
            PacketDistributor.sendToPlayersTrackingEntity(maid, this.createSyncPackage());
        }
    }

    void save(ValueOutput output) {
        output.putString(YSM_ROULETTE_ANIM_TAG, this.rouletteAnim);
        output.putInt(YSM_ROAMING_UPDATE_FLAG_TAG, this.roamingVarsUpdateFlag);
        output.store(YSM_ROAMING_VARS_TAG, ROAMING_VARS_CODEC, this.roamingVars);
    }

    void read(ValueInput input) {
        this.rouletteAnim = input.getStringOr(YSM_ROULETTE_ANIM_TAG, EMPTY_ROULETTE_ANIM);
        this.roamingVarsUpdateFlag = input.getIntOr(YSM_ROAMING_UPDATE_FLAG_TAG, 0);
        this.roamingVars = input.read(YSM_ROAMING_VARS_TAG, ROAMING_VARS_CODEC)
                .map(Object2FloatOpenHashMap::new)
                .orElseGet(Object2FloatOpenHashMap::new);
    }

    /**
     * 构建当前轮盘动画与漫游变量的同步包
     */
    public SyncYsmMaidDataPackage createSyncPackage() {
        return new SyncYsmMaidDataPackage(maid.getId(), this.rouletteAnim, this.rouletteAnimPlaying, this.roamingVars);
    }

    public boolean isYsmModel() {
        return this.getYsmData().isYsmModel();
    }

    public void setIsYsmModel(boolean isYsmModel) {
        this.setYsmData(this.getYsmData().withYsmModel(isYsmModel));
    }

    public String getYsmModelId() {
        return this.getYsmData().modelId();
    }

    public String getYsmModelTexture() {
        return this.getYsmData().textureId();
    }

    public Component getYsmModelName() {
        return this.getYsmData().name();
    }

    public void setYsmModel(String modelId, String texture, Component name) {
        if (!modelId.equals(this.getYsmModelId())) {
            this.roamingVars = new Object2FloatOpenHashMap<>();
            this.stopRouletteAnim();
        }
        this.setYsmData(this.getYsmData().withModel(modelId, texture, name));
    }

    public void playRouletteAnim(String rouletteAnim) {
        this.rouletteAnimPlaying = true;
        this.rouletteAnim = rouletteAnim;
        this.rouletteAnimDirty = true;
    }

    public void stopRouletteAnim() {
        this.rouletteAnimPlaying = false;
        this.rouletteAnimDirty = true;
    }

    private YsmData getYsmData() {
        return this.maid.getData(YSM);
    }

    private void setYsmData(YsmData data) {
        this.maid.setData(YSM, data);
    }

    public interface View {
        MaidYsmManager getYsmManager();

        default boolean isYsmModel() {
            return getYsmManager().isYsmModel();
        }

        default void setIsYsmModel(boolean isYsmModel) {
            getYsmManager().setIsYsmModel(isYsmModel);
        }

        default String getYsmModelId() {
            return getYsmManager().getYsmModelId();
        }

        default String getYsmModelTexture() {
            return getYsmManager().getYsmModelTexture();
        }

        default Component getYsmModelName() {
            return getYsmManager().getYsmModelName();
        }

        default void setYsmModel(String modelId, String texture, Component name) {
            getYsmManager().setYsmModel(modelId, texture, name);
        }

        default void playRouletteAnim(String rouletteAnim) {
            getYsmManager().playRouletteAnim(rouletteAnim);
        }

        default void stopRouletteAnim() {
            getYsmManager().stopRouletteAnim();
        }
    }
}
