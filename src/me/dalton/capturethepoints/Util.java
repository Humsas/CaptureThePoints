package me.dalton.capturethepoints;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author Humsas
 */
public class Util {
    //Some data from MobArena utils, to make item, functions easier
    // Weapons

    public static final List<Material> WEAPONS_TYPE = new LinkedList<Material>();
    public static final List<Material> SWORDS_TYPE = new LinkedList<Material>();
    public static final List<Material> AXES_TYPE = new LinkedList<Material>();
    public static final List<Material> PICKAXES_TYPE = new LinkedList<Material>();
    public static final List<Material> SPADES_TYPE = new LinkedList<Material>();
    public static final List<Material> HOES_TYPE = new LinkedList<Material>();
    // Armor
    public static final List<Material> ARMORS_TYPE = new LinkedList<Material>();
    public static final List<Material> HELMETS_TYPE = new LinkedList<Material>();
    public static final List<Material> CHESTPLATES_TYPE = new LinkedList<Material>();
    public static final List<Material> LEGGINGS_TYPE = new LinkedList<Material>();
    public static final List<Material> BOOTS_TYPE = new LinkedList<Material>();

    static {
        // Weapons
        SWORDS_TYPE.add(Material.WOOD_SWORD);
        SWORDS_TYPE.add(Material.STONE_SWORD);
        SWORDS_TYPE.add(Material.GOLD_SWORD);
        SWORDS_TYPE.add(Material.IRON_SWORD);
        SWORDS_TYPE.add(Material.DIAMOND_SWORD);

        AXES_TYPE.add(Material.WOOD_AXE);
        AXES_TYPE.add(Material.STONE_AXE);
        AXES_TYPE.add(Material.GOLD_AXE);
        AXES_TYPE.add(Material.IRON_AXE);
        AXES_TYPE.add(Material.DIAMOND_AXE);

        PICKAXES_TYPE.add(Material.WOOD_PICKAXE);
        PICKAXES_TYPE.add(Material.STONE_PICKAXE);
        PICKAXES_TYPE.add(Material.GOLD_PICKAXE);
        PICKAXES_TYPE.add(Material.IRON_PICKAXE);
        PICKAXES_TYPE.add(Material.DIAMOND_PICKAXE);

        SPADES_TYPE.add(Material.WOOD_SPADE);
        SPADES_TYPE.add(Material.STONE_SPADE);
        SPADES_TYPE.add(Material.GOLD_SPADE);
        SPADES_TYPE.add(Material.IRON_SPADE);
        SPADES_TYPE.add(Material.DIAMOND_SPADE);

        HOES_TYPE.add(Material.WOOD_HOE);
        HOES_TYPE.add(Material.STONE_HOE);
        HOES_TYPE.add(Material.GOLD_HOE);
        HOES_TYPE.add(Material.IRON_HOE);
        HOES_TYPE.add(Material.DIAMOND_HOE);

        WEAPONS_TYPE.addAll(SWORDS_TYPE);
        WEAPONS_TYPE.addAll(AXES_TYPE);
        WEAPONS_TYPE.addAll(PICKAXES_TYPE);
        WEAPONS_TYPE.addAll(SPADES_TYPE);
        WEAPONS_TYPE.addAll(HOES_TYPE);

        // Armor
        HELMETS_TYPE.add(Material.LEATHER_HELMET);
        HELMETS_TYPE.add(Material.GOLD_HELMET);
        HELMETS_TYPE.add(Material.CHAINMAIL_HELMET);
        HELMETS_TYPE.add(Material.IRON_HELMET);
        HELMETS_TYPE.add(Material.DIAMOND_HELMET);
        HELMETS_TYPE.add(Material.PUMPKIN);

        CHESTPLATES_TYPE.add(Material.LEATHER_CHESTPLATE);
        CHESTPLATES_TYPE.add(Material.GOLD_CHESTPLATE);
        CHESTPLATES_TYPE.add(Material.CHAINMAIL_CHESTPLATE);
        CHESTPLATES_TYPE.add(Material.IRON_CHESTPLATE);
        CHESTPLATES_TYPE.add(Material.DIAMOND_CHESTPLATE);

        LEGGINGS_TYPE.add(Material.LEATHER_LEGGINGS);
        LEGGINGS_TYPE.add(Material.GOLD_LEGGINGS);
        LEGGINGS_TYPE.add(Material.CHAINMAIL_LEGGINGS);
        LEGGINGS_TYPE.add(Material.IRON_LEGGINGS);
        LEGGINGS_TYPE.add(Material.DIAMOND_LEGGINGS);

        BOOTS_TYPE.add(Material.LEATHER_BOOTS);
        BOOTS_TYPE.add(Material.GOLD_BOOTS);
        BOOTS_TYPE.add(Material.CHAINMAIL_BOOTS);
        BOOTS_TYPE.add(Material.IRON_BOOTS);
        BOOTS_TYPE.add(Material.DIAMOND_BOOTS);

        ARMORS_TYPE.addAll(HELMETS_TYPE);
        ARMORS_TYPE.addAll(CHESTPLATES_TYPE);
        ARMORS_TYPE.addAll(LEGGINGS_TYPE);
        ARMORS_TYPE.addAll(BOOTS_TYPE);
    }


    /** Helper method for equipping armor pieces. */
    public static void equipArmorPiece(ItemStack stack, PlayerInventory inv) {
        Material type = stack.getType();

        if (HELMETS_TYPE.contains(type)) {
            inv.setHelmet(stack);
        } else if (CHESTPLATES_TYPE.contains(type)) {
            inv.setChestplate(stack);
        } else if (LEGGINGS_TYPE.contains(type)) {
            inv.setLeggings(stack);
        } else if (BOOTS_TYPE.contains(type)) {
            inv.setBoots(stack);
        }
    }

    /** Send message to Players that are playing CTP (specifically, have playerData)
     * @param ctp The CTP instance
     * @param s The message to send. "[CTP] " has been included.
     * @see PlayerData */
    public static void sendMessageToPlayers(CaptureThePoints ctp, String s) {
        for (Player play : ctp.playerData.keySet()) {
            play.sendMessage("[CTP] " + s); // Kj
        }
    }
    
    /** Send message to Players that are playing CTP (specifically, have playerData) but exclude a person.
     * @param ctp The CTP instance
     * @param exclude The Player to exclude
     * @param s The message to send. "[CTP] " has been included.
     * @see PlayerData */
    public static void sendMessageToPlayers(CaptureThePoints ctp, Player exclude, String s) {
        for (Player play : ctp.playerData.keySet()) {
            if (play != null && play != exclude) {
                play.sendMessage("[CTP] " + s); // Kj
            }
        }
    }

//    public static int roleNumber(CaptureThePoints plugin, String role)
//    {
//        Configuration config = plugin.load();
//        int rolesnum = config.getInt("numberofdifferentroles", 0);
//        for(int i=0;i<rolesnum;i++)
//        {
//            String currolename = config.getString("role"+(i+1)+".name");
//            if(currolename.equalsIgnoreCase(role))
//                return i+1;
//        }
//        return 0;
//    }
    /**
     * Takes a comma-separated list of items in the <type>:<amount> format and
     * returns a list of ItemStacks created from that data.
     */
    public static List<ItemStack> makeItemStackList(String string) {
        List<ItemStack> result = new LinkedList<ItemStack>();
        if (string == null || string.isEmpty()) {
            return result;
        }

        // Trim commas and whitespace, and split items by commas
        string = string.trim();
        if (string.endsWith(",")) {
            string = string.substring(0, string.length() - 1);
        }
        String[] items = string.split(",");

        for (String item : items) {
            // Trim whitespace and split by colons.
            item = item.trim();
            String[] parts = item.split(":");

            // Grab the amount.
            int amount = 1;
            if (parts.length == 1 && parts[0].matches("\\$[0-9]+")) {
                amount = Integer.parseInt(parts[0].substring(1, parts[0].length()));
            } else if (parts.length == 2 && parts[1].matches("(-)?[0-9]+")) {
                amount = Integer.parseInt(parts[1]);
            } else if (parts.length == 3 && parts[2].matches("(-)?[0-9]+")) // For dyes
            {
                amount = Integer.parseInt(parts[2]);
            }

            ItemStack stack = new ItemStack(0);
            // Make the ItemStack.
            if (amount > 64) {
                while (amount > 64) {
                    stack = (parts.length == 3)
                            ? makeItemStack(parts[0], amount, parts[1])
                            : makeItemStack(parts[0], amount);
                    amount -= 64;
                    if (stack != null) {
                        result.add(stack);
                    }
                }
            } else {
                stack = (parts.length == 3)
                        ? makeItemStack(parts[0], amount, parts[1])
                        : makeItemStack(parts[0], amount);
                if (stack != null) {
                    result.add(stack);
                }
            }
        }
        return result;
    }

    /** Helper methods for making ItemStacks out of strings and ints */
    public static ItemStack makeItemStack(String name, int amount, String data) {
        try {
            byte offset = 0;

            Material material = (name.matches("[0-9]+"))
                    ? Material.getMaterial(Integer.parseInt(name))
                    : Material.valueOf(name.toUpperCase());

            if (material == Material.INK_SACK) {
                offset = 15;
            }

            DyeColor dye = (data.matches("[0-9]+"))
                    ? DyeColor.getByData((byte) Math.abs(offset - Integer.parseInt(data)))
                    : DyeColor.valueOf(data.toUpperCase());

            return new ItemStack(material, amount, (byte) Math.abs(offset - dye.getData()));
        } catch (Exception e) {
            return null;
        }
    }

    /** Short for makeItemStack(name, amount, "0") */
    public static ItemStack makeItemStack(String name, int amount) {
        return makeItemStack(name, amount, "0");
    }

    /** Returns whether String is a number. */
    public static boolean isItInteger(String text) {
        int id = 0;
        try {
            id = Integer.parseInt(text);
        } catch (Exception NumberFormatException) {
            return false;
        }
        return true;
    }

    //mine
    public static List<Items> getItemListFromString(String text) {
        // Trim commas and whitespace, and split items by commas
        text = text.toUpperCase();
        text = text.trim();
        if (text.endsWith(",")) {
            text = text.substring(0, text.length() - 1);
        }
        String[] items = text.split(",");
        List<Items> list = new LinkedList<Items>();
        for (String item : items) {
            // Trim whitespace and split by colons.
            item = item.trim();
            String[] parts = item.split(":");

            // Grab the amount.
            Items i = new Items();
            i.amount = 1;

            if (parts.length == 1) {
                if (Util.isItInteger(parts[0])) {
                    i.item = Material.getMaterial(Integer.parseInt(parts[0]));
                } else {
                    i.item = Material.getMaterial(parts[0]);
                }
            } else if (parts.length == 2 && parts[1].matches("(-)?[0-9]+")) {
                if (Util.isItInteger(parts[0])) {
                    i.item = Material.getMaterial(Integer.parseInt(parts[0]));
                    i.amount = Integer.parseInt(parts[1]);
                } else {
                    i.item = Material.getMaterial(parts[0]);
                    i.amount = Integer.parseInt(parts[1]);
                }
            } else if (parts.length == 3 && parts[2].matches("(-)?[0-9]+")) // For dyes
            {
                i.amount = Integer.parseInt(parts[2]);
                i.type = Integer.parseInt(parts[1]);
                if (Util.isItInteger(parts[0])) {
                    i.item = Material.getMaterial(Integer.parseInt(parts[0]));
                } else {
                    i.item = Material.getMaterial(parts[0]);
                }
            }
            if (i.item != null) {
                list.add(i);
            } else {
                CaptureThePoints.logger.warning("[CTP] Error while loading config file. Check: " + item);
            }
        }
        return list;
    }

// Tingiu mazint :/
    public static void rewardPlayer(CaptureThePoints plugin, Player player) {
        if (plugin.playerData.get(player).winner) {
            for (int i = 0; i < plugin.rewards.winnerRewardCount; i++) {
                int itemCount = 0;
                int id = random(0, plugin.rewards.winnerRewards.size()); // Kj -- Took out -1
                int amount = plugin.rewards.winnerRewards.get(id).amount;
                if (!(Util.ARMORS_TYPE.contains(plugin.rewards.winnerRewards.get(id).item) || Util.WEAPONS_TYPE.contains(plugin.rewards.winnerRewards.get(id).item))) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getTypeId() == plugin.rewards.winnerRewards.get(id).item.getId()) {
                            itemCount += item.getAmount();
                        }
                    }
                }
                //player.sendMessage(player.getName() + " " + itemCount);
                if (itemCount > 0) {
                    player.getInventory().remove(plugin.rewards.winnerRewards.get(id).item.getId());
                }
                amount += itemCount;

                ItemStack item = new ItemStack(plugin.rewards.winnerRewards.get(id).item, amount);
                player.getInventory().addItem(new ItemStack[]{item});
            }
        } else {
            for (int i = 0; i < plugin.rewards.otherTeamRewardCount; i++) {
                int itemCount = 0;
                int id = random(0, plugin.rewards.loozerRewards.size()); // Kj -- Took out -1
                int amount = plugin.rewards.loozerRewards.get(id).amount;
                if (!(Util.ARMORS_TYPE.contains(plugin.rewards.loozerRewards.get(id).item) || Util.WEAPONS_TYPE.contains(plugin.rewards.loozerRewards.get(id).item))) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getTypeId() == plugin.rewards.loozerRewards.get(id).item.getId()) {
                            itemCount += item.getAmount();
                        }
                    }
                }
                //player.sendMessage(player.getName() + " " + itemCount);
                if (itemCount > 0) {
                    player.getInventory().remove(plugin.rewards.loozerRewards.get(id).item.getId());
                }
                amount += itemCount;

                ItemStack item = new ItemStack(plugin.rewards.loozerRewards.get(id).item, amount);
                player.getInventory().addItem(new ItemStack[]{item});
            }
        }
        //reward for kills
        for (int i = 0; i < plugin.playerData.get(player).kills; i++) {
            if (plugin.rewards.rewardsForKill.size() > 0) {
                int itemCount = 0;
                int id = random(0, plugin.rewards.rewardsForKill.size()); // Kj -- Took out -1
                int amount = plugin.rewards.rewardsForKill.get(id).amount;
                if (!(Util.ARMORS_TYPE.contains(plugin.rewards.rewardsForKill.get(id).item) || Util.WEAPONS_TYPE.contains(plugin.rewards.rewardsForKill.get(id).item))) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getTypeId() == plugin.rewards.rewardsForKill.get(id).item.getId()) {
                            itemCount += item.getAmount();
                        }
                    }
                }
                //player.sendMessage(player.getName() + " " + itemCount);
                if (itemCount > 0) {
                    player.getInventory().remove(plugin.rewards.rewardsForKill.get(id).item.getId());
                }
                amount += itemCount;
                ItemStack item = new ItemStack(plugin.rewards.rewardsForKill.get(id).item, amount);
                player.getInventory().addItem(new ItemStack[]{item});
            }
        }
        //reward for capture
        for (int i = 0; i < plugin.playerData.get(player).pointCaptures; i++) {
            if (plugin.rewards.rewardsForCapture.size() > 0) {
                int itemCount = 0;
                int id = random(0, plugin.rewards.rewardsForCapture.size()); // Kj -- Took out -1
                int amount = plugin.rewards.rewardsForCapture.get(id).amount;
                if (!(Util.ARMORS_TYPE.contains(plugin.rewards.rewardsForCapture.get(id).item) || Util.WEAPONS_TYPE.contains(plugin.rewards.rewardsForCapture.get(id).item))) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        if ((item != null) && (item.getTypeId() == plugin.rewards.rewardsForCapture.get(id).item.getId())) {
                            itemCount += item.getAmount();
                        }
                    }
                }
                // player.sendMessage(player.getName() + " " + itemCount);
                if (itemCount > 0) {
                    player.getInventory().remove(plugin.rewards.rewardsForCapture.get(id).item.getId());
                }
                amount += itemCount;
                ItemStack item = new ItemStack(plugin.rewards.rewardsForCapture.get(id).item, amount);
                player.getInventory().addItem(new ItemStack[]{item});
            }
        }
        player.updateInventory();
    }

    /** Generates a random number from startV to endV
     * @param startV Starting boundary
     * @param endV End boundary
     * @return A number generated in the boundary between startV to endV */
    public static int random(int startV, int endV) { // Kj -- n must be positive checking
        if (endV > startV) {
            return new Random().nextInt(endV) + startV;
        } else if (startV > endV) {
            return new Random().nextInt(startV) + endV;
        } else { //(startV == endV) 
            return startV;
        }
    }

    /** Builds a vertical gate */
    public static void buildVert(Player player, int start_x, int start_y, int start_z, int plusX, int plusY, int plusZ, int blockID) {
        for (int x = start_x; x < start_x + plusX; x++) {
            for (int y = start_y; y < start_y + plusY; y++) {
                for (int z = start_z; z < start_z + plusZ; z++) {
                    player.getWorld().getBlockAt(x, y, z).setTypeId(blockID);
                }
            }
        }
    }

    /** Removes a vertical point */
    public static void removeVertPoint(Player player, String dir, int start_x, int start_y, int start_z, int blockID) {
        if (dir.equals("NORTH")) {
            Util.buildVert(player, start_x, start_y - 1, start_z - 1, 2, 4, 4, 0);
        } else if (dir.equals("EAST")) {
            Util.buildVert(player, start_x - 1, start_y - 1, start_z, 4, 4, 2, 0);
        } else if (dir.equals("SOUTH")) {
            Util.buildVert(player, start_x - 1, start_y - 1, start_z - 1, 2, 4, 4, 0);
        } else if (dir.equals("WEST")) {
            Util.buildVert(player, start_x - 1, start_y - 1, start_z - 1, 4, 4, 2, 0);
        }
    }

    /** Get the direction of facing from a Location's yaw. */
    public static BlockFace getFace(Location loc) {
        BlockFace direction;
        double yaw = loc.getYaw();

        while (yaw < 0) {
            yaw += 360;
        }
        if ((yaw > 315) || (yaw <= 45)) {
            direction = BlockFace.WEST;
        } else if ((yaw > 45) && (yaw <= 135)) {
            direction = BlockFace.NORTH;
        } else if ((yaw > 135) && (yaw <= 225)) {
            direction = BlockFace.EAST;
        } else {
            direction = BlockFace.SOUTH;
        }

        return direction;
    }
    
    /** 
     * Gets distance between two locations. World friendly.
     * @param loc1 The first Location
     * @param loc2 The second Location
     * @return Returns a double of the distance between them. Returns NaN if the Locations are not on the same World or distance is too great.
     */
     public static double getDistance(Location loc1, Location loc2) { // Kjhf's
        if (loc1 != null && loc2 != null && loc1.getWorld() == loc2.getWorld()) {
            return loc1.distance(loc2);
        }
        return Double.NaN;
    }
}