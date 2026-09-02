package space.miaoning.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import space.miaoning.common.registry.ModBlockEntityTypes;

public class AEChiselBlockEntity extends BlockEntity {
    public AEChiselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.AE_CHISEL.get(), pos, state);
    }

    
}
