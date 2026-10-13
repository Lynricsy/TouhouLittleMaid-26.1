package com.github.tartaricacid.touhoulittlemaid.datagen;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.datagen.tag.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = TouhouLittleMaid.MOD_ID)
public class DataGenerator {
    /**
     * 26.3 起战利品表、进度、配方与世界注册表数据都改为 registry bootstrap 形式，统一在此登记
     */
    @SubscribeEvent
    public static void gatherRegistryEntries(GatherDataRegistryEntriesEvent event) {
        event.add(Registries.ENCHANTMENT, EnchantmentKeys::bootstrap)
                .add(Registries.DAMAGE_TYPE, DamageTypeProvider::bootstrap)
                .add(Registries.PAINTING_VARIANT, RegistryDataGenerator::genPainting)
                .add(Registries.TIMELINE, TimelinesProvider::bootstrap)
                .lootTable(
                        new LootTableProvider.SubProviderEntry(LootTableGenerator.ChestLootTables::new, LootContextParamSets.CHEST),
                        new LootTableProvider.SubProviderEntry(LootTableGenerator.AdvancementLootTables::new, LootContextParamSets.ADVANCEMENT_REWARD),
                        new LootTableProvider.SubProviderEntry(LootTableGenerator.EntityLootTables::new, LootContextParamSets.ENTITY),
                        new LootTableProvider.SubProviderEntry(LootTableGenerator.BlockLootTables::new, LootContextParamSets.BLOCK))
                .add(Registries.ADVANCEMENT, new AdvancementDataGen())
                .recipe(RecipeGenerator::new);
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        // Global Loot Modifier
        event.createProvider((output, lookup) -> new GlobalLootModifier(output, lookup, TouhouLittleMaid.MOD_ID));

        // Tags
        event.createBlockAndItemTags(
                (output, lookup) -> new TagBlock(output, lookup, TouhouLittleMaid.MOD_ID),
                (output, lookup, blockTags) -> new TagItem(output, lookup, TouhouLittleMaid.MOD_ID)
        );
        event.createProvider(TagEntity::new);

        // 世界注册表相关标签
        event.createProvider(output -> new TagDamage(output, event.getWorldLookupProvider()));
        event.createProvider(output -> new TagTimeline(output, event.getWorldLookupProvider()));
        event.createProvider(output -> new TagEnchantment(output, event.getWorldLookupProvider()));
        event.createProvider(output -> new TagPaintingVariant(output, event.getWorldLookupProvider()));

        event.createProvider(output -> new DataMapGenerator(output, event.getReloadableLookupProvider()));
    }
}
