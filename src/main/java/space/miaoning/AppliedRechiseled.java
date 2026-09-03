package space.miaoning;

import appeng.api.crafting.PatternDetailsHelper;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import space.miaoning.common.registry.ModBlockEntityTypes;
import space.miaoning.common.registry.ModBlocks;
import space.miaoning.common.registry.ModItems;
import space.miaoning.common.util.ChiselPatternDecoder;

@Mod(AppliedRechiseled.MODID)
public final class AppliedRechiseled {
    public static final String MODID = "applied_rechiseled";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AppliedRechiseled() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntityTypes.TILES.register(modEventBus);

        PatternDetailsHelper.registerDecoder(ChiselPatternDecoder.INSTANCE);
    }
}
