package git.jbredwards.well.common.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used to accurately ray trace slopes
 * @author jbred
 *
 */
public class AxisAlignedBBRotated extends AxisAlignedBB
{
    @Nonnull
    public final Vec3d centerPos, inputRotation;
    public final double rotX, rotY, rotZ;
    //cache values as to not recalculate later
    protected final double mxx, mxy, mxz, myx, myy, myz, mzx, mzy, mzz;

    //input rotations must be in degrees
    public AxisAlignedBBRotated(@Nonnull AxisAlignedBB parent, @Nonnull Vec3d rotation) {
        super(parent.minX, parent.minY, parent.minZ, parent.maxX, parent.maxY, parent.maxZ);
        centerPos = new Vec3d(minX + (maxX - minX) * 0.5, minY + (maxY - minY) * 0.5, minZ + (maxZ - minZ) * 0.5);
        inputRotation = rotation;
        rotX = Math.toRadians(MathHelper.wrapDegrees(-rotation.x));
        rotY = Math.toRadians(MathHelper.wrapDegrees(-rotation.y));
        rotZ = Math.toRadians(MathHelper.wrapDegrees(-rotation.z));
        final double cosX = Math.cos(rotX);
        final double sinX = Math.sin(rotX);
        final double cosY = Math.cos(rotY);
        final double sinY = Math.sin(rotY);
        final double cosZ = Math.cos(rotZ);
        final double sinZ = Math.sin(rotZ);
        //calculate x
        mxx = cosX * cosY;
        mxy = cosX * sinY * sinZ - sinX * cosZ;
        mxz = cosX * sinY * cosZ + sinX * sinZ;
        //calculate y
        myx = sinX * cosY;
        myy = sinX * sinY * sinZ + cosX * cosZ;
        myz = sinX * sinY * cosZ - cosX * sinZ;
        //calculate z
        mzx = -sinY;
        mzy = cosY * sinZ;
        mzz = cosY * cosZ;
    }

    @Nonnull
    public Vec3d rotateVec(@Nonnull Vec3d vec) {
        vec = vec.subtract(centerPos);
        final double prevX = vec.x;
        final double prevY = vec.y;
        final double prevZ = vec.z;
        final double x = mxx * prevX + mxy * prevY + mxz * prevZ;
        final double y = myx * prevX + myy * prevY + myz * prevZ;
        final double z = mzx * prevX + mzy * prevY + mzz * prevZ;
        return new Vec3d(x, y, z).add(centerPos);
    }

    @Nullable
    @Override
    public RayTraceResult calculateIntercept(@Nonnull Vec3d vecA, @Nonnull Vec3d vecB) {
        final @Nullable RayTraceResult result = super.calculateIntercept(rotateVec(vecA), rotateVec(vecB));
        if(result == null) return null;
        //fix sideHit
        final float fixedX = (float)(result.sideHit.getXOffset() - rotX * 2 / Math.PI);
        final float fixedY = (float)(result.sideHit.getYOffset() - rotY * 2 / Math.PI);
        final float fixedZ = (float)(result.sideHit.getZOffset() - rotZ * 2 / Math.PI);
        result.sideHit = EnumFacing.getFacingFromVector(fixedX, fixedY, fixedZ);
        return result;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public Vec3d getCenter() { return centerPos; }

    @Override
    public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        final AxisAlignedBBRotated other = (AxisAlignedBBRotated)o;
        if(Double.compare(other.rotX, rotX) != 0) return false;
        if(Double.compare(other.rotY, rotY) != 0) return false;
        return Double.compare(other.rotZ, rotZ) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long bits;
        bits = Double.doubleToLongBits(rotX);
        result = 31 * result + (int)(bits ^ (bits >>> 32));
        bits = Double.doubleToLongBits(rotY);
        result = 31 * result + (int)(bits ^ (bits >>> 32));
        bits = Double.doubleToLongBits(rotZ);
        result = 31 * result + (int)(bits ^ (bits >>> 32));
        return result;
    }
}
