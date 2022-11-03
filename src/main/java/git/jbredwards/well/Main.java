package git.jbredwards.well;

import git.jbredwards.well.common.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;

/**
 *
 * @author jbred
 *
 */
@Mod(modid = "well", name = "Well Mod", version = "1.0")
public final class Main
{
    @SidedProxy(clientSide = "git.jbredwards.well.client.ClientProxy", serverSide = "git.jbredwards.well.common.CommonProxy")
    public static CommonProxy proxy;
}
