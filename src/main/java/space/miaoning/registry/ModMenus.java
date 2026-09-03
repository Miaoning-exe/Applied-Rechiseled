package space.miaoning.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import space.miaoning.AppliedRechiseled;
import space.miaoning.menu.AEChiselMenu;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, AppliedRechiseled.MODID);

    public static final RegistryObject<MenuType<AEChiselMenu>> AE_CHISEL = MENUS.register(
            "ae_chisel",
            () -> IForgeMenuType.create(AEChiselMenu::fromNetwork)
    );

    private ModMenus() {
    }
}
