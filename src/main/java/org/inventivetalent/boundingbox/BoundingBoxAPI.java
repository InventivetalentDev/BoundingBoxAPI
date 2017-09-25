package org.inventivetalent.boundingbox;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.inventivetalent.particle.ParticleEffect;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.vectors.d3.Vector3DDouble;
import org.inventivetalent.vectors.d3.Vector3DInt;

@SuppressWarnings({"unused", "WeakerAccess"})
public class BoundingBoxAPI {

	static NMSClassResolver nmsClassResolver = new NMSClassResolver();

	static Class<?> Entity = nmsClassResolver.resolveSilent("Entity");
	static Class<?> World = nmsClassResolver.resolveSilent("World");
	static Class<?> Block = nmsClassResolver.resolveSilent("Block");
	static Class<?> BlockPosition = nmsClassResolver.resolveSilent("BlockPosition");
	static Class<?> Chunk = nmsClassResolver.resolveSilent("Chunk");
	static Class<?> IBlockData = nmsClassResolver.resolveSilent("IBlockData");
	static Class<?> IBlockAccess = nmsClassResolver.resolveSilent("IBlockAccess");

	static FieldResolver EntityFieldResolver = new FieldResolver(Entity);
	static FieldResolver BlockFieldResolver = new FieldResolver(Block);

	static MethodResolver BlockMethodResolver = new MethodResolver(Block);
	static MethodResolver ChunkMethodResolver = new MethodResolver(Chunk);
	static MethodResolver IBlockDataMethodResolver = new MethodResolver(IBlockData);
	static MethodResolver EntityMethodResolver = new MethodResolver(Entity);

	public static BoundingBox getBoundingBox(Entity entity) {
		return getAbsoluteBoundingBox(entity).translate(new Vector3DDouble(entity.getLocation().toVector().multiply(-1)));
	}

	public static BoundingBox getAbsoluteBoundingBox(Entity entity) {
		try {
			return BoundingBox.fromNMS(EntityFieldResolver.resolve("boundingBox").get(Minecraft.getHandle(entity)));
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void setBoundingBox(Entity entity, BoundingBox boundingBox) {
		try {
			EntityFieldResolver.resolve("boundingBox").set(Minecraft.getHandle(entity), boundingBox.toNMS());
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
			Object iBlockData = ChunkMethodResolver.resolve(new ResolverQuery("getBlockData", BlockPosition)).invoke(Minecraft.getHandle(block.getChunk()), blockPosition);
			Object iBlockAccess = Minecraft.getHandle(location.getWorld());
			Object nmsBlock = IBlockDataMethodResolver.resolve("getBlock").invoke(iBlockData);

			if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_9_R1)) {
				return BoundingBox.fromNMS(BlockMethodResolver.resolve(new ResolverQuery("a", IBlockData, IBlockAccess, BlockPosition)).invoke(nmsBlock, iBlockData, iBlockAccess, blockPosition));
			} else {
				return BoundingBox.fromNMS(BlockMethodResolver.resolve(new ResolverQuery("a", World, BlockPosition, IBlockData)).invoke(nmsBlock, iBlockAccess/*world*/, blockPosition, iBlockData));
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BoundingBox getAbsoluteBoundingBox(Block block) {
		return getBoundingBox(block).translate(new Vector3DDouble(block.getLocation().toVector()));
	}

	public static Runnable drawParticleOutline(final BoundingBox boundingBox, final World world, final org.bukkit.Particle particle) {
		return () -> {
			Vector3DInt minCorner = new Vector3DInt((int) (boundingBox.minX * 1000), (int) (boundingBox.minY * 1000), (int) (boundingBox.minZ * 1000));
			Vector3DInt maxCorner = new Vector3DInt((int) (boundingBox.maxX * 1000), (int) (boundingBox.maxY * 1000), (int) (boundingBox.maxZ * 1000));

			int i = 0;
			for (int x = minCorner.getX(); x <= maxCorner.getX(); x += 20) {
				for (int z = minCorner.getZ(); z <= maxCorner.getZ(); z += 20) {
					for (int y = minCorner.getY(); y <= maxCorner.getY(); y += 20) {
						Vector3DDouble vector = new Vector3DDouble(x / 1000.D, y / 1000.0D, z / 1000.0D);
						int edgeIntersectionCount = 0;

						//https://bukkit.org/threads/calculating-the-edges-of-a-rectangle-cuboid-discussion.78267/#post-1142330
						if (Math.abs(x - minCorner.getX()) < 8 || Math.abs(x - maxCorner.getX()) < 8) {
							edgeIntersectionCount++;
						}
						if (Math.abs(y - minCorner.getY()) < 8 || Math.abs(y - maxCorner.getY()) < 8) {
							edgeIntersectionCount++;
						}
						if (Math.abs(z - minCorner.getZ()) < 8 || Math.abs(z - maxCorner.getZ()) < 8) {
							edgeIntersectionCount++;
						}
						if (edgeIntersectionCount >= 2) {
							if (i++ % 9 != 0) {
								continue;
							}
							world.spawnParticle(particle, vector.toBukkitLocation(world), 1);
						}
					}
				}
			}
		};
	}

	public static Runnable drawParticleOutline(final BoundingBox boundingBox, final World world, final ParticleEffect particle) {
		return () -> {
			Vector3DInt minCorner = new Vector3DInt((int) (boundingBox.minX * 1000), (int) (boundingBox.minY * 1000), (int) (boundingBox.minZ * 1000));
			Vector3DInt maxCorner = new Vector3DInt((int) (boundingBox.maxX * 1000), (int) (boundingBox.maxY * 1000), (int) (boundingBox.maxZ * 1000));

			int i = 0;
			for (int x = minCorner.getX(); x <= maxCorner.getX(); x += 20) {
				for (int z = minCorner.getZ(); z <= maxCorner.getZ(); z += 20) {
					for (int y = minCorner.getY(); y <= maxCorner.getY(); y += 20) {
						Vector3DDouble vector = new Vector3DDouble(x / 1000.D, y / 1000.0D, z / 1000.0D);
						int edgeIntersectionCount = 0;

						//https://bukkit.org/threads/calculating-the-edges-of-a-rectangle-cuboid-discussion.78267/#post-1142330
						if (Math.abs(x - minCorner.getX()) < 8 || Math.abs(x - maxCorner.getX()) < 8) {
							edgeIntersectionCount++;
						}
						if (Math.abs(y - minCorner.getY()) < 8 || Math.abs(y - maxCorner.getY()) < 8) {
							edgeIntersectionCount++;
						}
						if (Math.abs(z - minCorner.getZ()) < 8 || Math.abs(z - maxCorner.getZ()) < 8) {
							edgeIntersectionCount++;
						}
						if (edgeIntersectionCount >= 2) {
							if (i++ % 9 != 0) {
								continue;
							}
							//								world.spawnParticle(particle, vector.toBukkitLocation(world), 1);
							particle.send(world.getPlayers(), vector.toBukkitLocation(world), 0, 0, 0, 0, 1);
						}
					}
				}
			}
		};
	}

}
