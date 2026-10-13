package com.github.tartaricacid.touhoulittlemaid.compat.ysm;

import com.github.tartaricacid.touhoulittlemaid.entity.data.YsmData;
import com.github.tartaricacid.touhoulittlemaid.init.registry.CompatRegistry;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

public class YsmCompat {
    private static final VersionRange VERSION_RANGE;
    private static boolean INSTALLED = false;

    static {
        try {
            VERSION_RANGE = VersionRange.createFromVersionSpec("[2.3.3,)");
        } catch (InvalidVersionSpecificationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        ModList.get().getModContainerById(CompatRegistry.YSM).ifPresent(modContainer -> {
            ArtifactVersion version = modContainer.getModInfo().getVersion();
            if (VERSION_RANGE.containsVersion(version)) {
                INSTALLED = true;
            } else {
                // 开发环境下，version 是空的，所以需要额外判断
                INSTALLED = !FMLEnvironment.isProduction();
            }
        });
    }

    public static boolean isInstalled() {
        return INSTALLED;
    }

    /**
     * 从物品存储的女仆实体 NBT 中读取 YSM 数据，未安装 YSM 时返回 {@link YsmData#EMPTY}
     */
    public static YsmData getYsmMaidInfo(CompoundTag maidData) {
        if (isInstalled()) {
            return YsmData.directGet(maidData);
        }
        return YsmData.EMPTY;
    }
}
