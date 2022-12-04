package git.jbredwards.well.common.item;

import git.jbredwards.well.common.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public class ItemBlockColoredWell extends ItemBlock
{
    public ItemBlockColoredWell(@Nonnull Block block) { super(block); }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        final IBlockState state = worldIn.getBlockState(pos);
        if(state.getBlock() instanceof BlockCauldron) {
            if(!worldIn.isRemote && !player.isCreative()) {
                final int level = state.getValue(BlockCauldron.LEVEL);
                if(level > 0) {
                    player.getHeldItem(hand).shrink(1);
                    player.addStat(StatList.CAULDRON_USED);
                    ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(ModItems.WELL));
                    ((BlockCauldron)state.getBlock()).setWaterLevel(worldIn, pos, state, level - 1);

                }
            }

            return EnumActionResult.SUCCESS;
        }

        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }
}
