package space.miaoning.common.util;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import space.miaoning.common.pattern.ChiselPatternDetails;
import space.miaoning.common.registry.ModItems;

/**
 * Restores a ChiselPatternDetails from the definition persisted by AE2.
 */
public final class ChiselPatternDecoder implements IPatternDetailsDecoder {
    public static final ChiselPatternDecoder INSTANCE = new ChiselPatternDecoder();

    private ChiselPatternDecoder() {
    }

    @Override
    public boolean isEncodedPattern(ItemStack stack) {
        return stack.is(ModItems.AE_CHISEL.get())
                && ChiselRecipeResolver.hasDefinitionTag(stack);
    }

    @Override
    @Nullable
    public IPatternDetails decodePattern(@Nullable AEItemKey what, Level level) {
        if (what == null) {
            return null;
        }

        ChiselRecipeResolver.ChiselConversion conversion =
                ChiselRecipeResolver.getConversionFromDefinition(what);
        if (conversion == null) {
            return null;
        }

        return new ChiselPatternDetails(
                what,
                AEItemKey.of(conversion.input()),
                conversion.inputAmount(),
                AEItemKey.of(conversion.output()),
                conversion.outputAmount());
    }

    @Override
    @Nullable
    public IPatternDetails decodePattern(ItemStack stack, Level level, boolean tryRecovery) {
        if (!isEncodedPattern(stack)) {
            return null;
        }

        return decodePattern(AEItemKey.of(stack), level);
    }
}
