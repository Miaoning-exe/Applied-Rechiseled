package space.miaoning.registry;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import space.miaoning.AppliedRechiseled;
import space.miaoning.block.AEChiselBlock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AppliedRechiseled.MODID);

    public static final RegistryObject<Block> AE_CHISEL = BLOCKS.register("ae_chisel", AEChiselBlock::new);

}
