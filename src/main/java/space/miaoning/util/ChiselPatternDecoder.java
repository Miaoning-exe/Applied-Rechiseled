package space.miaoning.util;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import space.miaoning.pattern.ChiselPatternDetails;

/**
 * Restores ChiselPatternDetails instances from definitions persisted by AE2.
 */
public final class ChiselPatternDecoder implements IPatternDetailsDecoder {
    public static final ChiselPatternDecoder INSTANCE = new ChiselPatternDecoder();

    private ChiselPatternDecoder() {
    }

    @Override
    public boolean isEncodedPattern(ItemStack stack) {
        return ChiselPatternDetails.isChiselPattern(stack);
    }

    @Override
    @Nullable
    public IPatternDetails decodePattern(@Nullable AEItemKey what, Level level) {
        if (!ChiselPatternDetails.isChiselPattern(what)) {
            return null;
        }

        try {
            return new ChiselPatternDetails(what);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
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
