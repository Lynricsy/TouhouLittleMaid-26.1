package com.github.tartaricacid.touhoulittlemaid.entity.passive;

import com.github.tartaricacid.touhoulittlemaid.api.backpack.IBackpackData;
import com.github.tartaricacid.touhoulittlemaid.api.backpack.IMaidBackpack;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.BackpackManager;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.EmptyBackpack;
import com.github.tartaricacid.touhoulittlemaid.entity.data.BackpackData;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataAttachment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;

@MaidManagerDef(alias = "backpackManager", exposeView = true)
public class MaidBackpackManager {
    private static final String BACKPACK_DATA_TAG = "BackpackData";
    private static final String BACKPACK_DATA_TYPE_TAG = "type";
    private static final String BACKPACK_DATA_VALUE_TAG = "data";

    private final EntityMaid maid;
    private int backpackDelay = 0;
    /**
     * 当前背包的附加数据，以及创建它时对应的背包类型；类型变化时重建
     */
    private @Nullable IBackpackData backpackData = null;
    private @Nullable IMaidBackpack backpackDataType = null;

    MaidBackpackManager(EntityMaid maid) {
        this.maid = maid;
    }

    public IMaidBackpack getMaidBackpackType() {
        BackpackData data = this.maid.getData(InitDataAttachment.BACKPACK);
        Identifier id = Identifier.parse(data.type());
        IMaidBackpack emptyBackpack = BackpackManager.getEmptyBackpack();
        return BackpackManager.findBackpack(id).orElse(emptyBackpack);
    }

    public boolean hasBackpack() {
        IMaidBackpack type = this.getMaidBackpackType();
        return !(type instanceof EmptyBackpack);
    }

    public void setMaidBackpackType(IMaidBackpack backpack) {
        // 以附件中的实际类型做比较，避免缓存字段与同步数据脱节
        if (backpack == this.getMaidBackpackType()) {
            return;
        }
        this.backpackData = null;
        this.backpackDataType = null;
        BackpackData data = new BackpackData(backpack.getId().toString());
        this.maid.setData(InitDataAttachment.BACKPACK, data);
    }

    /**
     * 获取当前背包的附加数据，背包类型变化后会惰性重建
     */
    @Nullable
    public IBackpackData getBackpackData() {
        IMaidBackpack type = this.getMaidBackpackType();
        if (type != this.backpackDataType) {
            this.backpackDataType = type;
            this.backpackData = type.hasBackpackData() ? type.getBackpackData(this.maid) : null;
        }
        return this.backpackData;
    }

    public void setBackpackDelay() {
        backpackDelay = 20;
    }

    public boolean backpackHasDelay() {
        return backpackDelay > 0;
    }

    void tick() {
        if (backpackDelay > 0) {
            backpackDelay--;
        }
        if (!this.maid.level().isClientSide()) {
            IBackpackData data = this.getBackpackData();
            if (data != null) {
                data.serverTick(this.maid);
            }
        }
    }

    void save(ValueOutput output) {
        IBackpackData data = this.getBackpackData();
        if (data == null || this.backpackDataType == null) {
            return;
        }
        ValueOutput child = output.child(BACKPACK_DATA_TAG);
        child.putString(BACKPACK_DATA_TYPE_TAG, this.backpackDataType.getId().toString());
        data.save(child.child(BACKPACK_DATA_VALUE_TAG), this.maid);
    }

    void read(ValueInput input) {
        input.child(BACKPACK_DATA_TAG).ifPresent(child -> child.getString(BACKPACK_DATA_TYPE_TAG)
                .map(Identifier::tryParse)
                .flatMap(BackpackManager::findBackpack)
                .filter(IMaidBackpack::hasBackpackData)
                .ifPresent(type -> {
                    IBackpackData data = type.getBackpackData(this.maid);
                    if (data != null) {
                        data.load(child.childOrEmpty(BACKPACK_DATA_VALUE_TAG), this.maid);
                        this.backpackData = data;
                        this.backpackDataType = type;
                    }
                }));
    }

    interface View {
        MaidBackpackManager getBackpackManager();

        default IMaidBackpack getMaidBackpackType() {
            return getBackpackManager().getMaidBackpackType();
        }

        default boolean hasBackpack() {
            return getBackpackManager().hasBackpack();
        }

        default void setMaidBackpackType(IMaidBackpack backpack) {
            getBackpackManager().setMaidBackpackType(backpack);
        }

        @Nullable
        default IBackpackData getBackpackData() {
            return getBackpackManager().getBackpackData();
        }

        default void setBackpackDelay() {
            getBackpackManager().setBackpackDelay();
        }

        default boolean backpackHasDelay() {
            return getBackpackManager().backpackHasDelay();
        }
    }
}
