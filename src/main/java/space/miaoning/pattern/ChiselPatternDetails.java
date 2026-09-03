package space.miaoning.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import space.miaoning.registry.ModItems;

public final class ChiselPatternDetails implements IPatternDetails {
    private static final String DEFINITION_TAG = "applied_rechiseled_pattern";
    private static final String INPUT_TAG = "input";
    private static final String INPUT_AMOUNT_TAG = "input_amount";
    private static final String OUTPUT_TAG = "output";
    private static final String OUTPUT_AMOUNT_TAG = "output_amount";

    private final AEItemKey definition;
    private final IInput[] inputs;
    private final GenericStack[] outputs;

    public ChiselPatternDetails(AEItemKey definition) {
        this.definition = Objects.requireNonNull(definition, "definition");

        ChiselConversion conversion = decodeDefinition(definition);
        this.inputs = new IInput[] {
                new ChiselInput(new GenericStack(AEItemKey.of(conversion.input()), conversion.inputAmount()))
        };
        this.outputs = new GenericStack[] {
                new GenericStack(AEItemKey.of(conversion.output()), conversion.outputAmount())
        };
    }

    public static boolean isChiselPattern(ItemStack stack) {
        return !stack.isEmpty() && isChiselPattern(AEItemKey.of(stack));
    }

    public static boolean isChiselPattern(@Nullable AEItemKey definition) {
        return definition != null
                && definition.getItem() == ModItems.AE_CHISEL.get()
                && definition.getTag() != null
                && definition.getTag().contains(DEFINITION_TAG, Tag.TAG_COMPOUND);
    }

    @Override
    public AEItemKey getDefinition() {
        return this.definition;
    }

    @Override
    public IInput[] getInputs() {
        return this.inputs;
    }

    @Override
    public GenericStack[] getOutputs() {
        return this.outputs;
    }

    @Override
    public int hashCode() {
        return this.definition.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && obj.getClass() == getClass()
                && this.definition.equals(((ChiselPatternDetails) obj).definition);
    }

    public static AEItemKey createDefinition(Item input, long inputAmount, Item output, long outputAmount) {
        if (input == null || output == null || input == output || inputAmount <= 0 || outputAmount <= 0) {
            throw new IllegalArgumentException("A chiseling pattern requires distinct items and positive amounts");
        }

        CompoundTag data = new CompoundTag();
        data.putString(INPUT_TAG, itemId(input));
        data.putLong(INPUT_AMOUNT_TAG, inputAmount);
        data.putString(OUTPUT_TAG, itemId(output));
        data.putLong(OUTPUT_AMOUNT_TAG, outputAmount);

        CompoundTag stackTag = new CompoundTag();
        stackTag.put(DEFINITION_TAG, data);

        ItemStack definition = new ItemStack(ModItems.AE_CHISEL.get());
        definition.setTag(stackTag);
        return AEItemKey.of(definition);
    }

    private static ChiselConversion decodeDefinition(AEItemKey definition) {
        if (!isChiselPattern(definition)) {
            throw new IllegalArgumentException("Not an Applied Rechiseled chisel pattern");
        }

        CompoundTag data = definition.getTag().getCompound(DEFINITION_TAG);
        Item input = itemFromId(data, INPUT_TAG);
        Item output = itemFromId(data, OUTPUT_TAG);
        long inputAmount = data.getLong(INPUT_AMOUNT_TAG);
        long outputAmount = data.getLong(OUTPUT_AMOUNT_TAG);

        if (input == null || output == null || input == output || inputAmount <= 0 || outputAmount <= 0) {
            throw new IllegalArgumentException("Invalid Applied Rechiseled chisel pattern definition");
        }

        return new ChiselConversion(input, inputAmount, output, outputAmount);
    }

    private static String itemId(Item item) {
        return Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item), "Unregistered item").toString();
    }

    @Nullable
    private static Item itemFromId(CompoundTag data, String key) {
        if (!data.contains(key, Tag.TAG_STRING)) {
            return null;
        }

        try {
            return BuiltInRegistries.ITEM.getOptional(new ResourceLocation(data.getString(key))).orElse(null);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private record ChiselConversion(Item input, long inputAmount, Item output, long outputAmount) {
    }

    private static final class ChiselInput implements IInput {
        private final GenericStack[] template;
        private final long multiplier;

        private ChiselInput(GenericStack stack) {
            this.template = new GenericStack[] { new GenericStack(stack.what(), 1) };
            this.multiplier = stack.amount();
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return template;
        }

        @Override
        public long getMultiplier() {
            return multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return input.matches(template[0]);
        }

        @Override
        public @Nullable AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
