package cn.nukkit.item;

import cn.nukkit.Server;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockMobSpawner;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawner;
import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.EntityZombie;
import cn.nukkit.entity.passive.*;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.EntityUtils;

import java.util.Random;

/**
 * @author MagicDroidX
 * Nukkit Project
 */
public class ItemSpawnEgg extends Item {

    public ItemSpawnEgg() {
        this(0, 1);
    }

    public ItemSpawnEgg(Integer meta) {
        this(meta, 1);
    }

    public ItemSpawnEgg(Integer meta, int count) {
        super(SPAWN_EGG, meta, count, "Spawn Entity Egg");
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
        if (!Server.getInstance().getPropertyBoolean("spawn-eggs", true)) {
            player.sendMessage("\u00A7cSpawn eggs are disabled on this server");
            return false;
        }

        if (target instanceof BlockMobSpawner) {
            BlockEntity blockEntity = level.getBlockEntity(target);
            if (blockEntity instanceof BlockEntitySpawner) {
                ((BlockEntitySpawner) blockEntity).setSpawnEntityType(this.getDamage());

                if (!player.isCreative()) {
                    player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                }
            } else {
                if (blockEntity != null) {
                    blockEntity.close();
                }

                CompoundTag nbt = new CompoundTag()
                        .putString("id", BlockEntity.MOB_SPAWNER)
                        .putInt("EntityId", this.getDamage())
                        .putInt("x", (int) target.x)
                        .putInt("y", (int) target.y)
                        .putInt("z", (int) target.z);
                new BlockEntitySpawner(level.getChunk((int) target.x >> 4, (int) target.z >> 4), nbt);

                if (!player.isCreative()) {
                    player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                }
            }

            return true;
        }

        FullChunk chunk = level.getChunk((int) block.getX() >> 4, (int) block.getZ() >> 4);

        if (chunk == null) {
            return false;
        }

        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<DoubleTag>("Pos")
                        .add(new DoubleTag("", block.getX() + 0.5))
                        .add(new DoubleTag("", target.getBoundingBox() == null ? block.getY() : target.getBoundingBox().maxY + 0.0001f))
                        .add(new DoubleTag("", block.getZ() + 0.5)))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", new Random().nextFloat() * 360))
                        .add(new FloatTag("", 0)));

        if (this.hasCustomName()) {
            nbt.putString("CustomName", this.getCustomName());
        }

        Entity entity = Entity.createEntity(this.meta, chunk, nbt);

        if (entity != null) {
            if (!player.isCreative()) {
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
            }

            entity.spawnToAll();

            if (EntityUtils.rand(1, 20) == 1 &&
                    (entity instanceof EntityCow ||
                    entity instanceof EntityChicken ||
                    entity instanceof EntityPig ||
                    entity instanceof EntitySheep ||
                    entity instanceof EntityZombie)) {

                ((BaseEntity) entity).setBaby(true);
            }

            return true;
        }
        return false;
    }
}
