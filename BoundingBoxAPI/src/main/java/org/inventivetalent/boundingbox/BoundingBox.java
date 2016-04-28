/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.boundingbox;

import com.google.gson.annotations.Expose;
import lombok.Data;
import org.bukkit.util.Vector;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.vectors.d3.Vector3DDouble;

@Data
public class BoundingBox {

	static NMSClassResolver nmsClassResolver = new NMSClassResolver();

	static Class<?> AxisAlignedBB = nmsClassResolver.resolveSilent("AxisAlignedBB");

	static FieldResolver AxisAlignedBBFieldResolver = new FieldResolver(AxisAlignedBB);

	@Expose public final double minX;
	@Expose public final double minY;
	@Expose public final double minZ;
	@Expose public final double maxX;
	@Expose public final double maxY;
	@Expose public final double maxZ;

	public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
		this.minX = Math.min(x1, x2);
		this.minY = Math.min(y1, y2);
		this.minZ = Math.min(z1, z2);
		this.maxX = Math.max(x1, x2);
		this.maxY = Math.max(y1, y2);
		this.maxZ = Math.max(z1, z2);
	}

	public BoundingBox(Vector vector1, Vector vector2) {
		this(vector1.getX(), vector1.getY(), vector1.getZ(), vector2.getX(), vector2.getY(), vector2.getZ());
	}

	public BoundingBox(Vector3DDouble vector1, Vector3DDouble vector2) {
		this(vector1.getX(), vector1.getY(), vector1.getZ(), vector2.getX(), vector2.getY(), vector2.getZ());
	}

	public BoundingBox expand(double x, double y, double z) {
		double minX = this.minX - x;
		double minY = this.minY - y;
		double minZ = this.minZ - z;
		double maxX = this.maxX + x;
		double maxY = this.maxY + y;
		double maxZ = this.maxZ + z;
		return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public BoundingBox expand(double d) {
		return expand(d, d, d);
	}

	public BoundingBox shrink(double x, double y, double z) {
		return expand(-x, -y, -z);
	}

	public BoundingBox shrink(double d) {
		return shrink(d, d, d);
	}

	public BoundingBox add(double x, double y, double z) {
		double minX = this.minX;
		double minY = this.minY;
		double minZ = this.minZ;
		double maxX = this.maxX;
		double maxY = this.maxY;
		double maxZ = this.maxZ;

		if (x < 0.0D) {
			minX += x;
		} else if (x > 0.0D) {
			maxX += x;
		}

		if (y < 0.0D) {
			minY += y;
		} else if (y > 0.0D) {
			maxY += y;
		}

		if (z < 0.0D) {
			minZ += z;
		} else if (z > 0.0D) {
			maxZ += z;
		}

		return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public BoundingBox translate(double x, double y, double z) {
		return new BoundingBox(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
	}

	public BoundingBox translate(Vector3DDouble vector) {
		return translate(vector.getX(), vector.getY(), vector.getZ());
	}

	public boolean contains(double x, double y, double z) {
		return (x > this.minX && x < this.maxX) && (y > this.minY && y < this.maxY) && (z > this.minZ && z < this.maxZ);
	}

	public boolean contains(Vector3DDouble vector) {
		return contains(vector.getX(), vector.getY(), vector.getZ());
	}

	public boolean contains(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return (this.minX < minX && this.maxX > maxX) && (this.minY < minY && this.maxY > maxY) && (this.minZ < minZ && this.maxZ > maxZ);
	}

	public boolean contains(BoundingBox other) {
		return contains(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
	}

	public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
	}

	public boolean intersects(BoundingBox boundingBox) {
		return intersects(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
	}

	public BoundingBox combine(BoundingBox boundingBox) {
		double minX = Math.min(this.minX, boundingBox.minX);
		double minY = Math.min(this.minY, boundingBox.minY);
		double minZ = Math.min(this.minZ, boundingBox.minZ);
		double maxX = Math.max(this.maxX, boundingBox.maxX);
		double maxY = Math.max(this.maxY, boundingBox.maxY);
		double maxZ = Math.max(this.maxZ, boundingBox.maxZ);
		return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public Vector3DDouble size() {
		return new Vector3DDouble(maxX - minX, maxY - minY, maxZ - minZ);
	}

	public Vector3DDouble getMinVector() {
		return new Vector3DDouble(minX, minY, minZ);
	}

	public Vector3DDouble getMaxVector() {
		return new Vector3DDouble(maxX, maxY, maxZ);
	}

	public Vector getMinBukkitVector() {
		return new Vector(minX, minY, minZ);
	}

	public Vector getMaxBukkitVector() {
		return new Vector(maxX, maxY, maxZ);
	}

	@Override
	public String toString() {
		return "BoundingBox{" +
				"(" + minX + "," + minY + "," + minZ + ")-(" + maxX + "," + maxY + "," + maxZ + ")" +
				"size=(" + ((maxX - minX) + "," + (maxY - minY) + "," + (maxZ - minZ)) + ")" +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		BoundingBox that = (BoundingBox) o;

		if (Double.compare(that.minX, minX) != 0) { return false; }
		if (Double.compare(that.minY, minY) != 0) { return false; }
		if (Double.compare(that.minZ, minZ) != 0) { return false; }
		if (Double.compare(that.maxX, maxX) != 0) { return false; }
		if (Double.compare(that.maxY, maxY) != 0) { return false; }
		return Double.compare(that.maxZ, maxZ) == 0;

	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(minX);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minY);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minZ);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(maxX);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(maxY);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(maxZ);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public Object toNMS() {
		try {
			return AxisAlignedBB.getConstructor(double.class, double.class, double.class, double.class, double.class, double.class).newInstance(minX, minY, minZ, maxX, maxY, maxZ);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BoundingBox fromNMS(Object axisAlignedBB) {
		try {
			double a = (double) AxisAlignedBBFieldResolver.resolve("a").get(axisAlignedBB);
			double b = (double) AxisAlignedBBFieldResolver.resolve("b").get(axisAlignedBB);
			double c = (double) AxisAlignedBBFieldResolver.resolve("c").get(axisAlignedBB);
			double d = (double) AxisAlignedBBFieldResolver.resolve("d").get(axisAlignedBB);
			double e = (double) AxisAlignedBBFieldResolver.resolve("e").get(axisAlignedBB);
			double f = (double) AxisAlignedBBFieldResolver.resolve("f").get(axisAlignedBB);

			return new BoundingBox(a, b, c, d, e, f);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
