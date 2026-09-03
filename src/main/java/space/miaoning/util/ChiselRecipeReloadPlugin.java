package space.miaoning.util;

import com.supermartijn642.rechiseled.api.chiseling.plugin.ChiselingRecipePlugin;
import com.supermartijn642.rechiseled.api.chiseling.plugin.ChiselingRecipesLoadedContext;
import com.supermartijn642.rechiseled.api.chiseling.plugin.RechiseledChiselingRecipePlugin;
import space.miaoning.block.AEChiselBlockEntity;

/**
 * Refreshes loaded AEChisel providers after Rechiseled rebuilds its recipe list.
 */
@RechiseledChiselingRecipePlugin(identifier = "pattern_refresh", priority = 100)
public final class ChiselRecipeReloadPlugin implements ChiselingRecipePlugin {
    @Override
    public void onRecipesLoaded(ChiselingRecipesLoadedContext context) {
        AEChiselBlockEntity.refreshAllPatterns();
    }
}
