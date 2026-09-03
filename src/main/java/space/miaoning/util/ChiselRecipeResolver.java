package space.miaoning.util;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingBlockShape;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingEntry;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingRecipe;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingRecipeManager;
import com.supermartijn642.rechiseled.api.chiseling.ItemWithWorth;
import com.supermartijn642.rechiseled.api.chiseling.conversion.ChiselingConversionHelper;
import com.supermartijn642.rechiseled.api.chiseling.conversion.ConversionResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public static final int MAX_PARALLEL = 64;

    private ChiselRecipeResolver() {
    }

    public static List<IPatternDetails> resolve(Level level, ItemStack itemStack, int parallel) {
        if (parallel <= 0 || level == null || itemStack.isEmpty()) {
            return List.of();
        }
        parallel = Math.min(parallel, MAX_PARALLEL);

        Item inputItem = itemStack.getItem();
        ChiselingRecipe recipe = ChiselingRecipeManager.get(level).getRecipeForItem(inputItem);
        if (recipe == null) {
            return List.of();
        }

        ItemWithWorth input = recipe.getWorth(inputItem);
        if (input == null) {
            return List.of();
        }

        Map<Item, ItemWithWorth> candidates = new LinkedHashMap<>();
        for (ChiselingEntry entry : recipe.entries()) {
            for (ChiselingBlockShape shape : ChiselingBlockShape.values()) {
                addCandidate(candidates, recipe, entry.getRegularItem(shape));
                addCandidate(candidates, recipe, entry.getConnectingItem(shape));
            }
        }

        List<IPatternDetails> patterns = new ArrayList<>();
        for (ItemWithWorth output : candidates.values()) {
            addPattern(patterns, input, output, parallel);
        }

        return List.copyOf(patterns);
    }

    private static void addCandidate(Map<Item, ItemWithWorth> candidates, ChiselingRecipe recipe, @Nullable ItemWithWorth candidate
    ) {
        if (candidate == null) {
            return;
        }

        ItemWithWorth canonical = recipe.getWorth(candidate.item());
        if (canonical != null) {
            candidates.putIfAbsent(candidate.item(), canonical);
        }
    }

    private static void addPattern(List<IPatternDetails> patterns, ItemWithWorth input, ItemWithWorth output, int parallel) {
        if (input == null || output == null || input.item() == output.item()) {
            return;
        }

        ConversionResult result = convert(input, output);
        if (result == null) {
            return;
        }

        long inputAmount = (long) result.numberOfConversions() * parallel;
        long outputAmount = (long) result.result() * parallel;
        AEItemKey definition = ChiselPatternDetails.createDefinition(
                input.item(), inputAmount, output.item(), outputAmount);
        patterns.add(new ChiselPatternDetails(definition));
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
