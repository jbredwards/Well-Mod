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
                    box(0,  0, 0,  3,  16, 16),
                    box(13, 0, 0,  16, 16, 16),
                    box(0,  0, 0,  16, 16, 3),
                    box(0,  0, 13, 16, 16, 16),
                    box(3,  0, 3,  10, 1,  10)
            ),
            TOP_TRACE_BB_X = ImmutableList.of(
                    box(7.5, 0,   14,  8.5,  15,   15),
                    box(7.5, 7,   2,   8.5,  8,    14),
                    box(5,   4.5, 4.5, 11,   10.5, 11.5),
                    box(7.5, 0,   1,   8.5,  15,   2),
                    box(7.3, 15,  0,   8.7,  15.7, 16),
                    //roof
                    new AxisAlignedBBRotated(box(6.9,  11.3, 0, 16.9, 12.3, 16), new Vec3d(-45, 0, 0)),
                    new AxisAlignedBBRotated(box(-0.9, 11.3, 0, 9.1,  12.3, 16), new Vec3d(45, 0, 0))
            ),
            TOP_COLLIDE_BB_X = ImmutableList.of(
                    box(7.5, 0,   14,  8.5, 15,   15),
                    box(7.5, 7,   2,   8.5, 8,    14),
                    box(5,   4.5, 4.5, 11,  10.5, 11.5),
                    box(7.5, 0,   1,   8.5, 15,   2),
                    box(7.3, 15,  0,   8.7, 15.7, 16),
                    //roof
                    box(7,  14, 0, 8,  15, 16),
                    box(6,  13, 0, 7,  14, 16),
                    box(5,  12, 0, 6,  13, 16),
                    box(4,  11, 0, 5,  12, 16),
                    box(3,  10, 0, 4,  11, 16),
                    box(2,  9 , 0, 3,  10, 16),
                    box(1,  8 , 0, 2,  9,  16),
                    box(0,  7 , 0, 1,  8,  16),
                    box(8,  14, 0, 9,  15, 16),
                    box(9,  13, 0, 10, 14, 16),
                    box(10, 12, 0, 11, 13, 16),
                    box(11, 11, 0, 12, 12, 16),
                    box(12, 10, 0, 13, 11, 16),
                    box(13, 9 , 0, 14, 10, 16),
                    box(14, 8 , 0, 15, 9,  16),
                    box(15, 7 , 0, 16, 8,  16)
            ),
            TOP_TRACE_BB_Z = ImmutableList.of(
                    box(14,  0,   7.5, 15,   15,   8.5),
                    box(2,   7,   7.5, 14,   8,    8.5),
                    box(4.5, 4.5, 5,   11.5, 10.5, 11),
                    box(1,   0,   7.5, 2,    15,   8.5),
                    box(0,   15,  7.3, 16,   15.7, 8.7),
                    //roof
                    new AxisAlignedBBRotated(box(0, 11.3, 6.9,  16, 12.3, 16.9), new Vec3d(0, 0, 45)),
                    new AxisAlignedBBRotated(box(0, 11.3, -0.9, 16, 12.3, 9.1),  new Vec3d(0, 0, -45))
            ),
            TOP_COLLIDE_BB_Z = ImmutableList.of(
                    box(14,  0,   7.5, 15,   15,   8.5),
                    box(2,   7,   7.5, 14,   8,    8.5),
                    box(4.5, 4.5, 5,   11.5, 10.5, 11),
                    box(1,   0,   7.5, 2,    15,   8.5),
                    box(0,   15,  7.3, 16,   15.7, 8.7),
                    //roof
                    box(0, 14, 7,  16, 15, 8),
                    box(0, 13, 6,  16, 14, 7),
                    box(0, 12, 5,  16, 13, 6),
                    box(0, 11, 4,  16, 12, 5),
                    box(0, 10, 3,  16, 11, 4),
                    box(0, 9 , 2,  16, 10, 3),
                    box(0, 8 , 1,  16, 9,  2),
                    box(0, 7 , 0,  16, 8,  1),
                    box(0, 14, 8,  16, 15, 9),
                    box(0, 13, 9,  16, 14, 10),
                    box(0, 12, 10, 16, 13, 11),
                    box(0, 11, 11, 16, 12, 12),
                    box(0, 10, 12, 16, 11, 13),
                    box(0, 9 , 13, 16, 10, 14),
                    box(0, 8 , 14, 16, 9,  15),
                    box(0, 7 , 15, 16, 8,  16)
            ),

            //flipped AABBs
            BOTTOM_BB_FLIPPED = flip(BOTTOM_BB),
            TOP_TRACE_BB_X_FLIPPED = flip(TOP_TRACE_BB_X),
            TOP_COLLIDE_BB_X_FLIPPED = flip(TOP_COLLIDE_BB_X),
            TOP_TRACE_BB_Z_FLIPPED = flip(TOP_TRACE_BB_Z),
            TOP_COLLIDE_BB_Z_FLIPPED = flip(TOP_COLLIDE_BB_Z);

    @Nonnull
    public static List<AxisAlignedBB> getTraceBoxList(@Nonnull IBlockState state) {
        if(state.getValue(BlockWell.UPSIDE_DOWN))
            return state.getValue(BlockWell.IS_BOTTOM) ? BOTTOM_BB_FLIPPED :
                    (state.getValue(BlockWell.AXIS) == EnumFacing.Axis.X ? TOP_TRACE_BB_X_FLIPPED : TOP_TRACE_BB_Z_FLIPPED);

        return state.getValue(BlockWell.IS_BOTTOM) ? BOTTOM_BB :
                (state.getValue(BlockWell.AXIS) == EnumFacing.Axis.X ? TOP_TRACE_BB_X : TOP_TRACE_BB_Z);
    }

    @Nonnull
    public static List<AxisAlignedBB> getCollisionBoxList(@Nonnull IBlockState state) {
        if(state.getValue(BlockWell.UPSIDE_DOWN))
            return state.getValue(BlockWell.IS_BOTTOM) ? BOTTOM_BB_FLIPPED :
                    (state.getValue(BlockWell.AXIS) == EnumFacing.Axis.X ? TOP_COLLIDE_BB_X_FLIPPED : TOP_COLLIDE_BB_Z_FLIPPED);

        return state.getValue(BlockWell.IS_BOTTOM) ? BOTTOM_BB :
                (state.getValue(BlockWell.AXIS) == EnumFacing.Axis.X ? TOP_COLLIDE_BB_X : TOP_COLLIDE_BB_Z);
    }

    @Nonnull
    static AxisAlignedBB box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new AxisAlignedBB(minX / 16, minY / 16, minZ / 16, maxX / 16, maxY / 16, maxZ / 16);
    }

    @Nonnull
    static List<AxisAlignedBB> flip(@Nonnull List<AxisAlignedBB> list) {
        final ImmutableList.Builder<AxisAlignedBB> builder = ImmutableList.builder();
        for(AxisAlignedBB aabb : list) {
            //preserve rotation
            final AxisAlignedBB newBB = new AxisAlignedBB(aabb.minX, 1 - aabb.maxY, aabb.minZ, aabb.maxX, 1 - aabb.minY, aabb.maxZ);
            if(aabb instanceof AxisAlignedBBRotated) {
                final Vec3d rot = ((AxisAlignedBBRotated)aabb).inputRotation;
                builder.add(new AxisAlignedBBRotated(newBB, new Vec3d(-rot.x, -rot.y, -rot.z)));
            }
            //no rotation
            else builder.add(newBB);
        }

        return builder.build();
    }
}
