package com.github.tartaricacid.touhoulittlemaid.api.backpack;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 女仆背包的附加数据，例如熔炉背包的燃烧进度、储罐背包的流体。
 * <p>
 * 由 {@link IMaidBackpack#getBackpackData(EntityMaid)} 创建，随女仆实体一起保存。
 */
public interface IBackpackData {
    /**
     * 需要通过菜单同步到客户端的整型数据
     */
    ContainerData getDataAccess();

    /**
     * 从女仆存档中读取数据
     */
    void load(ValueInput input, EntityMaid maid);

    /**
     * 将数据写入女仆存档
     */
    void save(ValueOutput output, EntityMaid maid);

    /**
     * 每 tick 在服务端调用
     */
    void serverTick(EntityMaid maid);
}
