package git.jbredwards.well.client;

import git.jbredwards.well.common.CommonProxy;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.MinecraftForgeClient;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy
{
    @Override
    public boolean isTranslucentActive() {
        return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
    }
}
