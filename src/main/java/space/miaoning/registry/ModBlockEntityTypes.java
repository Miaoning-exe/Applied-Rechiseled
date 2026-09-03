package space.miaoning.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import space.miaoning.AppliedRechiseled;
import space.miaoning.block.AEChiselBlockEntity;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AppliedRechiseled.MODID);

    public static final RegistryObject<BlockEntityType<AEChiselBlockEntity>> AE_CHISEL = TILES.register(
            "ae_chisel",
            () -> BlockEntityType.Builder.of(AEChiselBlockEntity::new, ModBlocks.AE_CHISEL.get()).build(null)
    );

}
