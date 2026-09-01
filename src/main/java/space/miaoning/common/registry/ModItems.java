package space.miaoning.common.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import space.miaoning.AppliedRechiseled;
import space.miaoning.common.block.AEChiselBlockItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AppliedRechiseled.MODID);

    public static final RegistryObject<Item> AE_CHISEL = ITEMS.register("ae_chisel",
            () -> new BlockItem(ModBlocks.AE_CHISEL.get(), new Item.Properties()));
}
