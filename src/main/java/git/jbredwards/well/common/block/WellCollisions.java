package git.jbredwards.well.common.block;

import com.google.common.collect.ImmutableList;
import git.jbredwards.well.common.util.AxisAlignedBBRotated;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
public final class WellCollisions
{
    @Nonnull
    public static final List<AxisAlignedBB>
            BOTTOM_BB = ImmutableList.of(
                    box(0,  0, 0,   3,  16, 16),
                    box(13, 0, 0,   16, 16, 16),
                    box(0,  0, 0,   16, 16, 3),
                    box(0,  0, 13,  16, 16, 16),
                    box(3,  0, 3,   10, 1,  10)
            ),
            TOP_TRACE_BB_X = ImmutableList.of(
                    box(7.5, 0,    14,  8.5,  15,   15),
                    box(7.5, 7,    2,   8.5,  8,    14),
                    box(5,   4.5,  4.5, 11,   10.5, 11.5),
                    box(7.5, 0,    1,   8.5,  15,   2),
                    box(7.3, 15,   0,   8.7,  15.7, 16),
                    new AxisAlignedBBRotated(box(6.9, 11.3, 0, 16.9, 12.3, 16), new Vec3d(-45, 0, 0))
            ),
            TOP_COLLIDE_BB_X = ImmutableList.of(
                    box(7.5, 0,    14,  8.5,  15,   15),
                    box(7.5, 7,    2,   8.5,  8,    14),
                    box(5,   4.5,  4.5, 11,   10.5, 11.5),
                    box(7.5, 0,    1,   8.5,  15,   2),
                    box(7.3, 15,   0,   8.7,  15.7, 16)
            ),
            TOP_TRACE_BB_Z = ImmutableList.of(

            ),
            TOP_COLLIDE_BB_Z = ImmutableList.of(

            );

    @Nonnull
    public static List<AxisAlignedBB> getTraceBoxList(@Nonnull IBlockState state) {
        return state.getValue(BlockWell.IS_BOTTOM) ? BOTTOM_BB :
                (state.getValue(BlockWell.AXIS) == EnumFacing.Axis.X ? TOP_TRACE_BB_X : TOP_TRACE_BB_Z);
    }

    @Nonnull
    public static List<AxisAlignedBB> getCollisionBoxList(@Nonnull IBlockState state) {
        return state.getValue(BlockWell.IS_BOTTOM) ? BOTTOM_BB :
                (state.getValue(BlockWell.AXIS) == EnumFacing.Axis.X ? TOP_COLLIDE_BB_X : TOP_COLLIDE_BB_Z);
    }

    @Nonnull
    static AxisAlignedBB box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new AxisAlignedBB(minX / 16, minY / 16, minZ / 16, maxX / 16, maxY / 16, maxZ / 16);
    }
}
