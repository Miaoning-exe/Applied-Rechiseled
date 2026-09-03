package space.miaoning.common.util;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingBlockShape;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingEntry;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingRecipe;
import com.supermartijn642.rechiseled.api.chiseling.ChiselingRecipeManager;
import com.supermartijn642.rechiseled.api.chiseling.ItemWithWorth;
import com.supermartijn642.rechiseled.api.chiseling.conversion.ChiselingConversionHelper;
import com.supermartijn642.rechiseled.api.chiseling.conversion.ConversionResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import space.miaoning.common.pattern.ChiselPatternDetails;
import space.miaoning.common.registry.ModItems;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ChiselRecipeResolver {
    /**
     * Keep one generated pattern within a normal item-stack-sized input batch.
     */
    private static final int MAX_PATTERN_INPUT = 64;

    private static final String DEFINITION_TAG = "applied_rechiseled_pattern";
    private static final String INPUT_TAG = "input";
    private static final String INPUT_AMOUNT_TAG = "input_amount";
    private static final String OUTPUT_TAG = "output";
    private static final String OUTPUT_AMOUNT_TAG = "output_amount";

    private ChiselRecipeResolver() {
    }

    public record ChiselConversion(Item input, long inputAmount, Item output, long outputAmount) {
    }

    public static List<IPatternDetails> getAllChiselPattern(Level level, ItemStack itemStack) {
        List<IPatternDetails> patterns = new ArrayList<>();

        for (ChiselConversion conversion : getAllChiselConversionsFromItemStack(level, itemStack)) {
            patterns.add(new ChiselPatternDetails(
                    getDefinitionFromConversion(conversion),
                    AEItemKey.of(conversion.input()),
                    conversion.inputAmount(),
                    AEItemKey.of(conversion.output()),
                    conversion.outputAmount()));
        }

        return List.copyOf(patterns);
    }

    public static List<ChiselConversion> getAllChiselConversionsFromItemStack(Level level, ItemStack itemStack) {
        if (level == null || itemStack.isEmpty()) {
            return List.of();
        }

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
                addCandidate(candidates, entry.getRegularItem(shape));
                addCandidate(candidates, entry.getConnectingItem(shape));
            }
        }

        List<ChiselConversion> conversions = new ArrayList<>();
        for (ItemWithWorth output : candidates.values()) {
            if (output.item() == input.item()) {
                continue;
            }

            findExactConversion(input, output).ifPresent(conversions::add);
        }

        return List.copyOf(conversions);
    }

    private static void addCandidate(
            Map<Item, ItemWithWorth> candidates,
            @Nullable ItemWithWorth candidate
    ) {
        if (candidate != null) {
            candidates.putIfAbsent(candidate.item(), candidate);
        }
    }

    private static Optional<ChiselConversion> findExactConversion(
            ItemWithWorth input,
            ItemWithWorth output
    ) {
        for (int requestedInput = 1; requestedInput <= MAX_PATTERN_INPUT; requestedInput++) {
            ConversionResult result = ChiselingConversionHelper.convert(
                    requestedInput, input, output);

            if (result.leftover() == 0
                    && result.numberOfConversions() > 0
                    && result.result() > 0) {
                return Optional.of(new ChiselConversion(
                        input.item(),
                        result.numberOfConversions(),
                        output.item(),
                        result.result()));
            }
        }

        return Optional.empty();
    }

    /**
     * Creates the stable AE2 identity used to persist a custom pattern.
     */
    public static AEItemKey getDefinitionFromConversion(ChiselConversion conversion) {
        CompoundTag data = new CompoundTag();
        data.putString(INPUT_TAG, itemId(conversion.input()));
        data.putLong(INPUT_AMOUNT_TAG, conversion.inputAmount());
        data.putString(OUTPUT_TAG, itemId(conversion.output()));
        data.putLong(OUTPUT_AMOUNT_TAG, conversion.outputAmount());

        CompoundTag stackTag = new CompoundTag();
        stackTag.put(DEFINITION_TAG, data);

        ItemStack definition = new ItemStack(ModItems.AE_CHISEL.get());
        definition.setTag(stackTag);
        return AEItemKey.of(definition);
    }

    public static boolean hasDefinitionTag(ItemStack stack) {
        return !stack.isEmpty()
                && stack.is(ModItems.AE_CHISEL.get())
                && stack.getTag() != null
                && stack.getTag().contains(DEFINITION_TAG, Tag.TAG_COMPOUND);
    }

    /**
     * Reconstructs the logical conversion stored in a custom pattern definition.
     */
    @Nullable
    public static ChiselConversion getConversionFromDefinition(@Nullable AEItemKey definition) {
        if (definition == null || definition.getItem() != ModItems.AE_CHISEL.get()) {
            return null;
        }

        CompoundTag stackTag = definition.getTag();
        if (stackTag == null || !stackTag.contains(DEFINITION_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag data = stackTag.getCompound(DEFINITION_TAG);
        Item input = itemFromId(data, INPUT_TAG);
        Item output = itemFromId(data, OUTPUT_TAG);
        long inputAmount = data.getLong(INPUT_AMOUNT_TAG);
        long outputAmount = data.getLong(OUTPUT_AMOUNT_TAG);

        if (input == null
                || output == null
                || input == output
                || inputAmount <= 0
                || outputAmount <= 0) {
            return null;
        }

        return new ChiselConversion(input, inputAmount, output, outputAmount);
    }

    private static String itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    @Nullable
    private static Item itemFromId(CompoundTag data, String key) {
        if (!data.contains(key, Tag.TAG_STRING)) {
            return null;
        }

        try {
            ResourceLocation id = new ResourceLocation(data.getString(key));
            return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
