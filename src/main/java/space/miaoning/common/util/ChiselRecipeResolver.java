package space.miaoning.common.util;

import com.supermartijn642.rechiseled.api.chiseling.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ChiselRecipeResolver {
    public static getAllChiselingOutputs(Level level, ItemStack itemStack) {
        if (level == null || itemStack.isEmpty()) {
            return List.of();
        }

        ChiselingRecipeManager manager = ChiselingRecipeManager.get(level);
        Item item = itemStack.getItem();

        ChiselingRecipe recipe = manager.getRecipeForItem(item);
        if (recipe == null) {
            return List.of();
        }

        ItemWithWorth inputItemWithWorth = recipe.getWorth(itemStack.getItem());
        if (inputItemWithWorth == null) {
            return List.of();
        }

        List<ItemWithWorth> outputs = new ArrayList<>();

        for (ChiselingEntry entry : recipe.entries()) {
            for (ChiselingBlockShape shape : ChiselingBlockShape.values()) {
                outputs.
            }
        }
    }
}
