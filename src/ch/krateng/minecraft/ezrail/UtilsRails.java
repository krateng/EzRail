package ch.krateng.minecraft.ezrail;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rail;
import org.bukkit.util.Vector;
import org.bukkit.entity.minecart.RideableMinecart;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;


public class UtilsRails {





    public static Sign getNextControlSign(RideableMinecart cart, Vector cartDirection, int limitDistance) {

        Location cartLocation = cart.getLocation();
        Block nextRailSegment = cartLocation.getBlock();
        BlockFace comingFrom = getOriginDirection(cartDirection);

        int i = EzRailConfig.MAX_DISTANCE_SECONDARY_CONTROL_BLOCK;
        while (i>0) {

            i--;

            Rail rail = (Rail) nextRailSegment.getBlockData();
            Block indicatorBlock = nextRailSegment.getRelative(0,-2,0);

            if (EnumSet.of(Material.COPPER_BLOCK, Material.EXPOSED_COPPER, Material.OXIDIZED_COPPER).contains(indicatorBlock.getType())) {
                Sign potentialSign = UtilsSigns.getSignInfo(indicatorBlock,comingFrom);
                if (potentialSign != null) {
                    return potentialSign;
                }
            }

            Rail.Shape shape = rail.getShape();
            if (comingFrom == BlockFace.EAST) {
                switch (shape) {
                    case EAST_WEST:
                    case ASCENDING_EAST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.WEST);
                        comingFrom = BlockFace.EAST;
                        break;
                    case NORTH_EAST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.NORTH);
                        comingFrom = BlockFace.SOUTH;
                        break;
                    case SOUTH_EAST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.SOUTH);
                        comingFrom = BlockFace.NORTH;
                        break;
                    case ASCENDING_WEST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.WEST).getRelative(BlockFace.UP);
                        comingFrom = BlockFace.EAST;
                        break;
                    default:
                        return null;
                }
            } else if (comingFrom == BlockFace.WEST) {
                switch (shape) {
                    case EAST_WEST:
                    case ASCENDING_WEST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.EAST);
                        comingFrom = BlockFace.WEST;
                        break;
                    case NORTH_WEST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.NORTH);
                        comingFrom = BlockFace.SOUTH;
                        break;
                    case SOUTH_WEST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.SOUTH);
                        comingFrom = BlockFace.NORTH;
                        break;
                    case ASCENDING_EAST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.EAST).getRelative(BlockFace.UP);
                        comingFrom = BlockFace.WEST;
                        break;
                    default:
                        return null;
                }
            } else if (comingFrom == BlockFace.NORTH) {
                switch (shape) {
                    case NORTH_SOUTH:
                    case ASCENDING_NORTH:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.SOUTH);
                        comingFrom = BlockFace.NORTH;
                        break;
                    case NORTH_WEST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.WEST);
                        comingFrom = BlockFace.EAST;
                        break;
                    case NORTH_EAST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.EAST);
                        comingFrom = BlockFace.WEST;
                        break;
                    case ASCENDING_SOUTH:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP);
                        comingFrom = BlockFace.NORTH;
                        break;
                    default:
                        return null;
                }
            } else if (comingFrom == BlockFace.SOUTH) {
                switch (shape) {
                    case NORTH_SOUTH:
                    case ASCENDING_SOUTH:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.NORTH);
                        comingFrom = BlockFace.SOUTH;
                        break;
                    case SOUTH_WEST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.WEST);
                        comingFrom = BlockFace.EAST;
                        break;
                    case SOUTH_EAST:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.EAST);
                        comingFrom = BlockFace.WEST;
                        break;
                    case ASCENDING_NORTH:
                        nextRailSegment = nextRailSegment.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP);
                        comingFrom = BlockFace.SOUTH;
                        break;
                    default:
                        return null;
                }
            }

            else {
                return null;
            }

            // if the next rail is going down, it's not in this block

            if (! (nextRailSegment.getBlockData() instanceof Rail)) {
                nextRailSegment = nextRailSegment.getRelative(0,-1,0);
                if (nextRailSegment.getBlockData() instanceof Rail) {
                    Rail nextRail = (Rail) nextRailSegment.getBlockData();
                    if (nextRail.getShape() == Rail.Shape.ASCENDING_EAST && comingFrom == BlockFace.EAST) continue;
                    if (nextRail.getShape() == Rail.Shape.ASCENDING_WEST && comingFrom == BlockFace.WEST) continue;
                    if (nextRail.getShape() == Rail.Shape.ASCENDING_NORTH && comingFrom == BlockFace.NORTH) continue;
                    if (nextRail.getShape() == Rail.Shape.ASCENDING_SOUTH && comingFrom == BlockFace.SOUTH) continue;
                }
                return null;
            }
        }
        return null;
    }

    public static BlockFace getOriginDirection(Vector move_direction) {
        Vector move_direction_norm = move_direction.normalize();
        if (move_direction_norm.getX() > 0.6) {
            return BlockFace.WEST;
        } else if (move_direction_norm.getX() < -0.6) {
            return BlockFace.EAST;
        } else if (move_direction_norm.getZ() > 0.6) {
            return BlockFace.NORTH;
        } else if (move_direction_norm.getZ() < -0.6) {
            return BlockFace.SOUTH;
        }

        //alternative
        if (Math.abs(move_direction.getX()) > Math.abs(move_direction.getZ())) {
            if (move_direction.getX() < 0) {
                return BlockFace.EAST;
            }
            else {
                return BlockFace.WEST;
            }
        }
        else {
            if (move_direction.getZ() < 0) {
                return BlockFace.NORTH;
            }
            else {
                return BlockFace.SOUTH;
            }
        }
    }

    public static Boolean validCommandBlock(Block testBlock) {
        return (EnumSet.of(
                Material.COPPER_BLOCK,
                Material.EXPOSED_COPPER,
                Material.WEATHERED_COPPER,
                Material.OXIDIZED_COPPER).contains(testBlock.getType()));
    }
    public static Boolean validPrimaryCommandBlock(Block testBlock) {
        if (validCommandBlock(testBlock)) {
            if (UtilsSigns.getSignInfo(testBlock, BlockFace.EAST) != null) return true;
            if (UtilsSigns.getSignInfo(testBlock, BlockFace.WEST) != null) return true;
            if (UtilsSigns.getSignInfo(testBlock, BlockFace.NORTH) != null) return true;
            if (UtilsSigns.getSignInfo(testBlock, BlockFace.SOUTH) != null) return true;
        }
        return false;
    }

    public static final Logger logger = Logger.getLogger("Minecraft");

    public static HashSet<Block> getCommandBlocksInArea(Block originBlock, HashSet<Block> alreadyFound) {
        int[] range_x = {originBlock.getX()-EzRailConfig.MAX_DISTANCE_PLATFORMS[0], originBlock.getX()+EzRailConfig.MAX_DISTANCE_PLATFORMS[0]};
        int[] range_y = {originBlock.getY()-EzRailConfig.MAX_DISTANCE_PLATFORMS[1], originBlock.getY()+EzRailConfig.MAX_DISTANCE_PLATFORMS[1]};
        int[] range_z = {originBlock.getZ()-EzRailConfig.MAX_DISTANCE_PLATFORMS[2], originBlock.getZ()+EzRailConfig.MAX_DISTANCE_PLATFORMS[2]};

        World world = originBlock.getWorld();
        if (alreadyFound == null) {
            alreadyFound = new HashSet<Block>();
        }

        for (int loc_x=range_x[0]; loc_x<=range_x[1];loc_x++) {
            for (int loc_y=range_y[0]; loc_y<=range_y[1];loc_y++) {
                for (int loc_z=range_z[0]; loc_z<=range_z[1];loc_z++) {
                    Block testblock = world.getBlockAt(loc_x,loc_y,loc_z);

                    if (validCommandBlock(testblock) && !alreadyFound.contains(testblock)) {
                        //logger.info("Valid block" + loc_x + "/" + loc_y + "/" + loc_z + "/");
                        for (BlockFace face : new BlockFace[]{BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH}) {
                            Sign signInfo = UtilsSigns.getSignInfo(testblock,face);
                            if (signInfo != null) {
                                alreadyFound.add(testblock);
                                alreadyFound.addAll(getCommandBlocksInArea(testblock,alreadyFound));
                                break;
                            }
                        }
                    }
                }
            }
        }

        return alreadyFound;
    }

    public static HashMap<Integer, String[]> getOtherPlatformDestinations(Block indicatorBlock, String station) {
        HashSet<Block> cmdBlocks = getCommandBlocksInArea(indicatorBlock, null);

        HashMap<Integer, String[]> destinations = new HashMap<>();
        for (Block cmdBlock : cmdBlocks) {
            for (BlockFace face : new BlockFace[]{BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH}) {
                Sign signInfo = UtilsSigns.getSignInfo(cmdBlock,face);
                if (signInfo != null) {
                    SignInfo info = UtilsSigns.extractSignInfo(signInfo);
                    if (info.station.equals(station)) {
                        destinations.put(info.platform,info.nextStops);
                    }
                    break;
                }
            }
        }
        return destinations;

    }

}
