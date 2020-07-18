package biz.donvi.jakesRTP;

import org.bukkit.*;

public abstract class SafeLocationUtils {

    /* ================================================== *\
                    Material checking utils
    \* ================================================== */

    /**
     * Checks the given material against a <u>whitelist</u> of materials deemed to be "safe to be in"
     *
     * @param mat The material to check
     * @return Whether it is safe or not to be there
     */
    static boolean isSafeToBeIn(Material mat) {
        switch (mat) {
            case AIR:
            case SNOW:
            case FERN:
            case LARGE_FERN:
            case VINE:
            case GRASS:
            case TALL_GRASS:
                return true;
            case WATER:
            case LAVA:
            case CAVE_AIR:
            default:
                return false;
        }
    }

    /**
     * Checks the given material against a <u>whitelist</u> of materials deemed to be "safe to go through".
     * Materials that are "safe to go through" are generally where that you can stand safely on, or be safely in,
     * though in general if you are finding a safe location, you would prefer not not end up on or in them. <p>
     * Note: Safe to go through is in the context of looking for a safe spot. These materials may not necessarily
     * allow players to walk through them.
     *
     * @param mat The material to check
     * @return Whether it is safe to go through
     */
    static boolean isSafeToGoThrough(Material mat) {
        //At the time of writing this, I can not think of any materials other than leaves that fit this category.
        //I am leaving the method in place though so if I decide to add more materials later, it will be easy.
        return isTreeLeaves(mat);
    }

    /**
     * Checks the given material against a <u>blacklist</u> of materials deemed to be "safe to be on"
     *
     * @param mat The material to check
     * @return Whether it is safe or not to be there
     */
    static boolean isSafeToBeOn(Material mat) {
        switch (mat) {
            case LAVA:
            case MAGMA_BLOCK:
            case WATER:
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
            case CACTUS:
            case SEAGRASS:
            case TALL_SEAGRASS:
            case LILY_PAD:
                return false;
            case GRASS_BLOCK:
            case STONE:
            case DIRT:
            default:
                return true;
        }
    }

    /**
     * Checks if the given material is any type of tree leaf.
     *
     * @param mat The material to check
     * @return Whether it is a type of leaf
     */
    static boolean isTreeLeaves(Material mat) {
        switch (mat) {
            case ACACIA_LEAVES:
            case BIRCH_LEAVES:
            case DARK_OAK_LEAVES:
            case JUNGLE_LEAVES:
            case OAK_LEAVES:
            case SPRUCE_LEAVES:
                return true;
            default:
                return false;
        }
    }

    /* ================================================== *\
                    Location checking utils
    \* ================================================== */

    /**
     * Checks if the location is in a tree. To be in a tree, you must both be on a log, and in leaves.<p>
     * Note: This should only be run from the main thread.
     *
     * @param loc The location to check.
     * @return True if the location is in a tree.
     */
    static boolean isInATree(final Location loc) {
        requireMainThread();
        for (Material material : new Material[]{
                loc.clone().add(0, 1, 0).getBlock().getType(),
                loc.clone().add(0, 2, 0).getBlock().getType()})
            if (isTreeLeaves(material)) return true;
        return false;
    }

    /**
     * Checks if the location is in a tree. To be in a tree, you must both be on a log, and in leaves.<p>
     * Note: This can be run from any thread.
     *
     * @param loc   The location to check.
     * @param chunk The chunk snapshot that contains the {@code Location}'s data.
     * @return True if the location is in a tree.
     */
    static boolean isInTree(final Location loc, ChunkSnapshot chunk) {
        for (Material material : new Material[]{
                locMatFromSnapshot(loc.clone().add(0, 1, 0), chunk),
                locMatFromSnapshot(loc.clone().add(0, 2, 0), chunk)})
            if (isTreeLeaves(material)) return true;
        return false;
    }

    /* ================================================== *\
                    Location moving utils
    \* ================================================== */

    /**
     * Takes the given location, and moves it downwards until it is no longer inside something that is
     * considered safe to be in by {@code isSafeToBeIn()}<p>
     * Note: This should only be run from the main thread.
     *
     * @param loc The location to modify
     */
    static void dropToGround(final Location loc) {
        requireMainThread();
        while (isSafeToBeIn(loc.getBlock().getType()) || isSafeToGoThrough(loc.getBlock().getType()))
            loc.add(0, -1, 0);
    }

    /**
     * Takes the given location, and moves it downwards until it is no longer inside something that is
     * considered safe to be in by {@code isSafeToBeIn()}<p>
     * Note: This can be run from any thread.
     *
     * @param loc   The location to modify
     * @param chunk The chunk snapshot that contains the {@code Location}'s data.
     */
    static void dropToGround(final Location loc, ChunkSnapshot chunk) {
        while (isSafeToBeIn(locMatFromSnapshot(loc, chunk))
               || isSafeToGoThrough(locMatFromSnapshot(loc, chunk)))
            loc.add(0, -1, 0);
    }

    /**
     * Takes the given location, and moves it downwards until it is no longer inside something that is
     * considered safe to be in by {@code isSafeToBeIn()}<p>
     * Note: This should only be run from the main thread.
     *
     * @param loc      The location to modify
     * @param lowBound The lowest the location can go
     */
    static void dropToGround(final Location loc, int lowBound) {
        requireMainThread();
        while (loc.getBlockY() > lowBound && (
                isSafeToBeIn(loc.getBlock().getType())
                || isSafeToGoThrough(loc.getBlock().getType())))
            loc.add(0, -1, 0);
    }

    /**
     * Takes the given location, and moves it downwards until it is no longer inside something that is
     * considered safe to be in by {@code isSafeToBeIn()}<p>
     * Note: This can be run from any thread.
     *
     * @param loc      The location to modify
     * @param lowBound The lowest the location can go
     * @param chunk    The chunk snapshot that contains the {@code Location}'s data.
     */
    static void dropToGround(final Location loc, int lowBound, ChunkSnapshot chunk) {
        while (loc.getBlockY() > lowBound && (
                isSafeToBeIn(locMatFromSnapshot(loc, chunk))
                || isSafeToGoThrough(locMatFromSnapshot(loc, chunk))))
            loc.add(0, -1, 0);
    }

    /* ================================================== *\
                    Chunk cache utils
    \* ================================================== */

    static Material locMatFromSnapshot(Location loc, ChunkSnapshot chunk) {
        if (!isLocationInsideChunk(loc, chunk)) throw new Error("The given location is not within given chunk!");
        int x = loc.getBlockX() % 16;
        int z = loc.getBlockZ() % 16;
        if (x < 0) x += 16;
        if (z < 0) z += 16;
        return chunk.getBlockData(x, loc.getBlockY(), z).getMaterial();
    }

    static boolean isLocationInsideChunk(Location loc, ChunkSnapshot chunk) {
        return (int) Math.floor((double) loc.getBlockX() / 16) == chunk.getX() &&
               (int) Math.floor((double) loc.getBlockZ() / 16) == chunk.getZ();
    }

    /* ================================================== *\
                Misc (but still related) utils
    \* ================================================== */

    /**
     * Checks if the current thread is the primary Bukkit thread.
     * If it is, nothing happens, if not, it throws an unchecked exception.
     */
    static void requireMainThread() {
        if (!Bukkit.isPrimaryThread()) throw new AccessFromNonMainThreadError();
    }

    /**
     * Exists purely to throw an exception before an attempt is made
     * to access the Bukkit API from a thread other than the main
     */
    private static class AccessFromNonMainThreadError extends Error {}
}