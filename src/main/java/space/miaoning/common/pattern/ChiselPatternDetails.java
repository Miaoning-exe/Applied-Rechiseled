package space.miaoning.common.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ChiselPatternDetails implements IPatternDetails {
    private final AEItemKey definition;
    private final IInput[] inputs;
    private final GenericStack[] outputs;

    public ChiselPatternDetails(AEItemKey definition,
                                AEItemKey input,
                                long inputAmount,
                                AEItemKey output,
                                long outputAmout
    ) {
        this.definition = definition;
        this.inputs = new IInput[]{
                new ChiselInput(new GenericStack(input, inputAmount))
        };
        this.outputs = new GenericStack[]{
                new GenericStack(output, outputAmout)
        };
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

    private static final class ChiselInput implements IInput {
        private final GenericStack[] template;
        private final long multiplier;

        public ChiselInput(GenericStack stack) {
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
