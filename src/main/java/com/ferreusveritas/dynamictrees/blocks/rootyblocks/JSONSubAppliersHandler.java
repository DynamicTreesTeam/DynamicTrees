package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.treepacks.JsonApplierRegistryEvent;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Max Hyper
 */
@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class JSONSubAppliersHandler {
    /**
     * This class handles custom appliers for special soil blocks. Addons that want soils with custom properties can
     * copy this whole class to do so.
     */
    @SubscribeEvent
    public static void registerAppliers(final JsonApplierRegistryEvent<SoilProperties> event) {
        // Add to reload appliers only.
        if (!event.isReloadApplier()) {
            return;
        }

        event.getApplierList().register("required_light", SpreadableSoilProperties.class, Integer.class, SpreadableSoilProperties::setRequiredLight)
                .register("spread_item", SpreadableSoilProperties.class, Item.class, SpreadableSoilProperties::setSpreadItem)
                .registerArrayApplier("spreadable_soils", SpreadableSoilProperties.class, SoilProperties.class,
                        (soilProp, soil) -> SoilProperties.REGISTRY.runOnNextLock(() -> soilProp.addSpreadableSoils(soil)));
    }

}
