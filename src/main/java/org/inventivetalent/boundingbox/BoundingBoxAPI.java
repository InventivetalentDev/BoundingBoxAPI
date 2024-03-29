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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.inventivetalent.reflection.accessor.FieldAccessor;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.minecraft.MinecraftVersion;
import org.inventivetalent.reflection.resolver.ClassResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.vectors.d3.Vector3DDouble;

public class BoundingBoxAPI {

    static ClassResolver classResolver = new ClassResolver();
    static NMSClassResolver nmsClassResolver = new NMSClassResolver();

    static Class<?> Entity = nmsClassResolver.resolveSilent("world.entity.Entity", "Entity");
    static Class<?> World = nmsClassResolver.resolveSilent("world.level.World", "World");
    static Class<?> Block = nmsClassResolver.resolveSilent("world.level.block.Block", "Block");
    static Class<?> BlockPosition = nmsClassResolver.resolveSilent("core.BlockPosition");
    static Class<?> Chunk = nmsClassResolver.resolveSilent("world.level.chunk.Chunk", "Chunk");
    static Class<?> IBlockData = nmsClassResolver.resolveSilent("world.level.block.state.IBlockData", "IBlockData");
    static Class<?> IBlockAccess = nmsClassResolver.resolveSilent("world.level.IBlockAccess", "IBlockAccess");
    static Class<?> BlockData = nmsClassResolver.resolveSilent("world.level.block.state.BlockBase$BlockData", "BlockBase$BlockData");
    static Class<?> AxisAlignedBB = nmsClassResolver.resolveSilent("world.phys.AxisAlignedBB", "AxisAlignedBB");
    static Class<?> VoxelShape;
    static Class<?> VoxelShapeCollision;

    static FieldResolver EntityFieldResolver = new FieldResolver(Entity);
    static FieldResolver BlockFieldResolver = new FieldResolver(Block);

    static MethodResolver BlockMethodResolver = new MethodResolver(Block);
    static MethodResolver ChunkMethodResolver = new MethodResolver(Chunk);
    static MethodResolver IBlockDataMethodResolver = new MethodResolver(IBlockData);
    static MethodResolver EntityMethodResolver = new MethodResolver(Entity);
    static MethodResolver BlockDataMethodResolver;
    static MethodResolver VoxelShapeMethodResolver;
    static MethodResolver VoxelShapeCollisionMethodResolver;

    public static BoundingBox getBoundingBox(Entity entity) {
        return getAbsoluteBoundingBox(entity).translate(new Vector3DDouble(entity.getLocation().toVector().multiply(-1)));
    }

    public static BoundingBox getAbsoluteBoundingBox(Entity entity) {
        try {
            FieldAccessor field = EntityFieldResolver.resolveByLastTypeAccessor(AxisAlignedBB);
            return field.get(Minecraft.getHandle(entity));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setBoundingBox(Entity entity, BoundingBox boundingBox) {
        try {
            FieldAccessor field = EntityFieldResolver.resolveByLastTypeAccessor(AxisAlignedBB);
            field.set(Minecraft.getHandle(entity), boundingBox.toNMS());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setSize(Entity entity, float width, float length) {
        try {
            EntityMethodResolver.resolve(new ResolverQuery("setSize", float.class, float.class)).invoke(Minecraft.getHandle(entity), width, length);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BoundingBox getBoundingBox(Block block) {
        try {
            Location location = block.getLocation();

            Object blockPosition = BlockPosition.getConstructor(double.class, double.class, double.class).newInstance(location.getX(), location.getY(), location.getZ());

            ResolverQuery getTypeQuery = new ResolverQuery(MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_18_R1) ? "a_" : MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_13_R1) ? "getType" : "getBlockData", BlockPosition);
            Object iBlockData = ChunkMethodResolver.resolve(getTypeQuery).invoke(Minecraft.getHandle(block.getChunk()), blockPosition);
            Object iBlockAccess = Minecraft.getHandle(location.getWorld());
            MethodResolver blockResolver = IBlockDataMethodResolver;
            if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_16_R1)) {
                if (BlockDataMethodResolver == null) {
                    BlockDataMethodResolver = new MethodResolver(BlockData);
                }
                blockResolver = BlockDataMethodResolver;
            }
            Object nmsBlock;
            if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_18_R1)) {
                nmsBlock = blockResolver.resolveSignature("Block b()").invoke(iBlockData);
            } else {
                nmsBlock = blockResolver.resolve("getBlock", "b").invoke(iBlockData);
            }

            Object axisAlignedBB;
            if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_13_R1)) {
                if (VoxelShape == null) {
                    VoxelShape = nmsClassResolver.resolveSilent("world.phys.shapes.VoxelShape", "VoxelShape");
                }
                if (VoxelShapeMethodResolver == null) {
                    VoxelShapeMethodResolver = new MethodResolver(VoxelShape);
                }
                Object voxelShape;
                if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_14_R1)) {
                    if (VoxelShapeCollision == null) {
                        VoxelShapeCollision = nmsClassResolver.resolveSilent("world.phys.shapes.VoxelShapeCollision", "VoxelShapeCollision");
                    }
                    if (VoxelShapeCollisionMethodResolver == null) {
                        VoxelShapeCollisionMethodResolver = new MethodResolver(VoxelShapeCollision);
                    }
                    /// static VoxelShapeCollision a()
                    Object collision = VoxelShapeCollisionMethodResolver.resolveSignature("VoxelShapeCollision a()").invoke(null);
                    if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_16_R1)) {
                        voxelShape = BlockDataMethodResolver.resolve(new ResolverQuery("a", IBlockAccess, BlockPosition, VoxelShapeCollision)).invoke(iBlockData, iBlockAccess, blockPosition, collision);
                    } else {
                        voxelShape = BlockMethodResolver.resolve(new ResolverQuery("a", IBlockData, IBlockAccess, BlockPosition, VoxelShapeCollision)).invoke(nmsBlock, iBlockData, iBlockAccess, blockPosition, collision);
                    }
                } else {
                    voxelShape = BlockMethodResolver.resolve(new ResolverQuery("a", IBlockData, IBlockAccess, BlockPosition)).invoke(nmsBlock, iBlockData, iBlockAccess, blockPosition);
                }
                axisAlignedBB = VoxelShapeMethodResolver.resolveSignature("AxisAlignedBB a()", "AxisAlignedBB getBoundingBox()").invoke(voxelShape);
            } else if (MinecraftVersion.VERSION.newerThan(Minecraft.Version.v1_9_R1)) {
                axisAlignedBB = BlockMethodResolver.resolve(new ResolverQuery("a", IBlockData, IBlockAccess, BlockPosition)).invoke(nmsBlock, iBlockData, iBlockAccess, blockPosition);
            } else {
                axisAlignedBB = BlockMethodResolver.resolve(new ResolverQuery("a", World, BlockPosition, IBlockData)).invoke(nmsBlock, iBlockAccess/*world*/, blockPosition, iBlockData);
            }
            return BoundingBox.fromNMS(axisAlignedBB);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BoundingBox getAbsoluteBoundingBox(Block block) {
        return getBoundingBox(block).translate(new Vector3DDouble(block.getLocation().toVector()));
    }

}
