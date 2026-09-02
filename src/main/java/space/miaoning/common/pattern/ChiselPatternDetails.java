package space.miaoning.common.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;

public class ChiselPatternDetails implements IPatternDetails {
    @Override
    public AEItemKey getDefinition() {
        return null;
    }

    @Override
    public IInput[] getInputs() {
        return new IInput[0];
    }

    @Override
    public GenericStack[] getOutputs() {
        return new GenericStack[0];
    }

    public static boolean addChiselPatterns(@Nullable GenericStack input, @Nullable Collection<ItemStack> outputs, @NotNull Collection<ChiselPatternDetails> patterns, int parallel) {
//        if (input == null) return false;
//        if (outputs == null || outputs.isEmpty()) return false;
//        boolean addedPattern = false;
//        for (var itemStack : outputs) {
//            var out = GenericStack.fromItemStack(itemStack);
//            if (out != null && !input.equals(out)) {
//                patterns.add(new ChiselPatternDetails(input.copy().setStackSize(parallel), out.setStackSize(parallel)));
//                addedPattern = true;
//            }
//        }
//        return addedPattern;
    }
}
