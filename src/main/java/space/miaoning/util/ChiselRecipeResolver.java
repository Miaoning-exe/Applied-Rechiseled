package space.miaoning.util;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingBlockShape;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingEntry;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingRecipe;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingRecipeManager;
import com.supermartijn642.rechiseled.api.chiseling.ItemWithWorth;
import com.supermartijn642.rechiseled.api.chiseling.conversion.ChiselingConversionHelper;

import java.util.*;

import com.supermartijn642.rechiseled.api.chiseling.conversion.ConversionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import space.miaoning.pattern.ChiselPatternDetails;

public final class ChiselRecipeResolver {
    /**
     * Keep one generated pattern within a normal item-stack-sized input batch.
     */
    private static final int MAX_PATTERN_INPUT = 64;

    private ChiselRecipeResolver() {
    }

    public static List<IPatternDetails> resolve(Level level, ItemStack itemStack, int parallel) {
        if (parallel <= 0 || level == null || itemStack.isEmpty()) {
            return List.of();
        }

        Item inputItem = itemStack.getItem();
        ChiselingRecipe recipe = ChiselingRecipeManager.get(level).getRecipeForItem(inputItem);
        if (recipe == null) {
            return List.of();
        }

        ItemWithWorth input = recipe.getWorth(inputItem);
        List<IPatternDetails> patterns = new ArrayList<>();

        for (ChiselingEntry entry : recipe.entries()) {
            for (ChiselingBlockShape shape : ChiselingBlockShape.values()) {
                ItemWithWorth outputRegularItem = entry.getRegularItem(shape);
                ItemWithWorth outputConnectingItem = entry.getConnectingItem(shape);

                addPattern(patterns, input, outputRegularItem, parallel);
                addPattern(patterns, input, outputConnectingItem, parallel);
            }
        }

        return patterns;
    }

    private static void addPattern(List<IPatternDetails> patterns, ItemWithWorth input, ItemWithWorth output, int parallel) {
        ConversionResult result = convert(input, output);
        if (output != null && result != null) {
            int consumed = result.numberOfConversions();
            int inputAmount = consumed * parallel;
            int converted = result.result();
            int outputAmount = converted * parallel;

            if (input.item() == output.item()) {
                return ;
            }
            AEItemKey definition = ChiselPatternDetails.createDefinition(input.item(), inputAmount, output.item(), outputAmount);

            if (definition != null) {
                patterns.add(new ChiselPatternDetails(definition));
            }
        }
    }

    @Nullable
    private static ConversionResult convert(ItemWithWorth input, ItemWithWorth output) {
        if (input == null || output == null) {
            return null;
        }

        for (int inputAmount = 1; inputAmount <= MAX_PATTERN_INPUT; inputAmount++) {
            ConversionResult result = ChiselingConversionHelper.convert(inputAmount, input, output);
            if (result.result() > 0) {
                return result;
            }
        }

        return null;
    }
}
