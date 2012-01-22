package me.dalton.capturethepoints;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;

/**
 *
 * @author Humsas
 */
public class ArenaRestore {

    public String arenaToDelete = null;
    private List<CTPBlock> destroyedBlock = new LinkedList<CTPBlock>();
    // true if destroed, false if not
    private List<Boolean> blockStatus = new LinkedList<Boolean>();
    private CaptureThePoints ctp;

    public ArenaRestore(CaptureThePoints plugi) {
        ctp = plugi;
    }

    public void addBlock(Block block, boolean isDestroyed) {
        CTPBlock tmp = new CTPBlock();
        ContainerBlock dd;
        BlockState state = block.getState();
        //chest
        if (state instanceof ContainerBlock) {
            dd = (ContainerBlock) state;
            ItemStack[] contents = dd.getInventory().getContents();
            tmp.inv = contents;
        }

        tmp.data = block.getData();
        tmp.loc = block.getLocation();
        tmp.material = block.getTypeId();

        destroyedBlock.add(tmp);
        blockStatus.add(isDestroyed);
    }

    public void restoreAllBlocks() {
        for (int i = destroyedBlock.size() - 1; i >= 0; i--) {
            Location blockLocation = destroyedBlock.get(i).loc;
            if (blockStatus.get(i)) {
                CTPBlock tmp = destroyedBlock.get(i);
                blockLocation.getBlock().setTypeId(tmp.material);
                blockLocation.getBlock().setData(tmp.data);

                //chest
                if (tmp.inv != null && tmp.inv.length > 0)
                {
                    ContainerBlock dd = (ContainerBlock) blockLocation.getBlock().getState();
                    Inventory inv = dd.getInventory();
                    inv.setContents(tmp.inv);
                }
            }
            else
            {
                blockLocation.getBlock().setTypeId(0);
            }
        }
        destroyedBlock.clear();
        blockStatus.clear();
    }


    public void checkForArena(String arenaName, String world)
    {
        // lets find arena name
        ResultSet lala = ctp.mysqlConnector.getData("SELECT * FROM Arena WHERE name LIKE '"+ arenaName +".%'");
        try
        {
            if (lala.next())   // We found an egzisting arena
            {
                // delete data from mysql
                deleteArenaData(arenaName);
            }
            else
            {
                String arenaCodedName = arenaName + "." + ctp.mainArena.x1 + "." + ctp.mainArena.y1 + "." + ctp.mainArena.z1 + "." + ctp.mainArena.x2 +
                        "." + ctp.mainArena.y2 + "." + ctp.mainArena.z2;
                ctp.mysqlConnector.modifyData("INSERT INTO `Arena` (`name`, `world`) VALUES ( '" + arenaCodedName + "','" + world + "');");
            }
        }
        catch (SQLException ex)
        {
            Logger.getLogger(ArenaRestore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deleteArenaData(String arenaName)
    {
        try
        {
            ResultSet rs = ctp.mysqlConnector.getData("SELECT id FROM Simple_block where arena_name = '" + arenaName + "'");
            while (rs.next())
            {
                ctp.mysqlConnector.modifyData("DELETE FROM Sign WHERE block_ID = " + rs.getInt("id"));
                ctp.mysqlConnector.modifyData("DELETE FROM Item WHERE block_ID = " + rs.getInt("id"));
                ctp.mysqlConnector.modifyData("DELETE FROM Spawner_block WHERE block_ID = " + rs.getInt("id"));
                ctp.mysqlConnector.modifyData("DELETE FROM Note_block WHERE block_ID = " + rs.getInt("id"));
            }
            ctp.mysqlConnector.modifyData("DELETE FROM Simple_block where arena_name = '" + arenaName + "'");
            ctp.mysqlConnector.modifyData("DELETE FROM Arena where name = '" + arenaName + "'");
        }
        catch (SQLException ex)
        {
            Logger.getLogger(ArenaRestore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void storeBlock(Block block, Spawn firstPoint, Spawn secondPoint, String arenaName)
    {
        int type = block.getTypeId();
        int data = block.getData();
//        Location loc = block.getLocation();

        switch (type)
        {
            case BlockID.WALL_SIGN:
            case BlockID.SIGN_POST:
            {
                Sign sign = (Sign) block.getState();
                String dir = ((Directional) block.getType().getNewData(block.getData())).getFacing().toString();
                ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `z2`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," + (int)firstPoint.x + "," +
                        (int)firstPoint.y + "," + (int)firstPoint.z + "," + (int)secondPoint.z + ",'" + arenaName + "'," + type + ",'" + dir + "');");
                int id = ctp.mysqlConnector.getLastInsertedId();
                ctp.mysqlConnector.modifyData("INSERT INTO `Sign` (`block_ID`, `first_line`, `second_line`, `third_line`, `fourth_line`) VALUES ( " + id + ",'" + sign.getLine(0) + "','" +
                        sign.getLine(1) + "','" + sign.getLine(2) + "','" + sign.getLine(3) + "');");
                break;
            }

            case BlockID.CHEST:
            {
                Chest chest = (Chest) block.getState();
                ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `z2`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," + (int)firstPoint.x + "," +
                        (int)firstPoint.y + "," + (int)firstPoint.z + "," + (int)secondPoint.z + ",'" + arenaName + "'," + type + ",'NO');");
                int id = ctp.mysqlConnector.getLastInsertedId();

                storeInventory(id, chest.getInventory());
                break;
            }

            case BlockID.FURNACE:
            case BlockID.BURNING_FURNACE:
            {
                Furnace furnace = (Furnace) block.getState();
                String dir = ((Directional) block.getType().getNewData(block.getData())).getFacing().toString();
                ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `z2`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," + (int)firstPoint.x + "," +
                        (int)firstPoint.y + "," + (int)firstPoint.z + "," + (int)secondPoint.z + ",'" + arenaName + "'," + type + ",'" + dir + "');");
                int id = ctp.mysqlConnector.getLastInsertedId();

                storeInventory(id, furnace.getInventory());
                break;
            }

            case BlockID.DISPENSER:
            {
                Dispenser dispenser = (Dispenser) block.getState();
                String dir = ((Directional) block.getType().getNewData(block.getData())).getFacing().toString();
                ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `z2`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," + (int)firstPoint.x + "," +
                        (int)firstPoint.y + "," + (int)firstPoint.z + "," + (int)secondPoint.z + ",'" + arenaName + "'," + type + ",'" + dir + "');");
                int id = ctp.mysqlConnector.getLastInsertedId();

                storeInventory(id, dispenser.getInventory());
                break;
            }

            case BlockID.MOB_SPAWNER:
            {
                CreatureSpawner mobSpawner = (CreatureSpawner) block.getState();
                ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `z2`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," + (int)firstPoint.x + "," +
                        (int)firstPoint.y + "," + (int)firstPoint.z + "," + (int)secondPoint.z + ",'" + arenaName + "'," + type + ",'NO');");
                int id = ctp.mysqlConnector.getLastInsertedId();
                ctp.mysqlConnector.modifyData("INSERT INTO `Spawner_block` (`block_ID`, `creature_type`, `delay`) VALUES ( " + id + ",'" + mobSpawner.getCreatureTypeId() + "'," + mobSpawner.getDelay() + ");");
                break;
            }

            case BlockID.NOTE_BLOCK:
            {
                NoteBlock noteBlock = (NoteBlock) block.getState();
                ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `z2`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," + (int)firstPoint.x + "," +
                        (int)firstPoint.y + "," + (int)firstPoint.z + "," + (int)secondPoint.z + ",'" + arenaName + "'," + type + ",'NO');");
                int id = ctp.mysqlConnector.getLastInsertedId();

                ctp.mysqlConnector.modifyData("INSERT INTO `Note_block` (`block_ID`, `note_type`) VALUES ( " + id + "," + noteBlock.getRawNote() + ");");
                break;
            }

            default:
            {
                // Get if this is a directional block
                String dir = "NO";
                if(block.getType().getNewData(block.getData()) instanceof Directional)
                {
                    dir = ((Directional) block.getType().getNewData(block.getData())).getFacing().toString();
                }
                ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `z2`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," + (int)firstPoint.x + "," +
                        (int)firstPoint.y + "," + (int)firstPoint.z + "," + (int)secondPoint.z + ",'" + arenaName + "'," + type + ",'" + dir + "');");
                break;
            }
        }
    }

    private void storeInventory(int id, Inventory inv)
    {
        for(int i = 0; i < inv.getSize(); i++)
        {
            ItemStack item = inv.getItem(i);
            if(item != null && item.getTypeId() != 0)
                ctp.mysqlConnector.modifyData("INSERT INTO `Item` (`type`, `block_ID`, `durability`, `amount`, `place_in_inv`, `data`) VALUES ( " + item.getTypeId() + "," + id + "," +
                        item.getDurability() + "," + item.getAmount() + "," + i + "," + item.getData().getData() + ");");
        }
    }


    public void restore(String arenaName, int timesRestored, boolean restorePuttableItems)
    {
        try
        {
            // Find if arena exists in database
            ResultSet rs1 = ctp.mysqlConnector.getData("SELECT * FROM Arena WHERE name like '"+ arenaName +"%'");
            rs1.next();
            
            int restoreCount = 2500;

            String signz = "";
            if(restorePuttableItems)
                signz = "IN";
            else
                signz = "NOT IN";
                
            ResultSet blocksRez = ctp.mysqlConnector.getData("SELECT * FROM (((Simple_block LEFT JOIN Sign on Sign.block_ID = id) LEFT JOIN note_block on note_block.block_ID = id)"+
                    " LEFT JOIN spawner_block on spawner_block.block_ID = id) where arena_name = '" + arenaName + "' and `block_type`" + signz + "(6, 8, 9, 10, 11, 27, 28, 31, 32," +
                    "50, 51, 55, 59, 63, 64, 65, 66, 68, 69, 70, 71, 72, 75, 76, 77, 78, 81, 83, 90, 92, 93, 94, 96, 104, 105, 111, 115, 117, 342, 343, 328, 333) LIMIT " + timesRestored * restoreCount + ", " + restoreCount);

            while(blocksRez.next())
            {
                for(int z = blocksRez.getInt("z"); z <= blocksRez.getInt("z2"); z++)
                {
                    int id = blocksRez.getInt("id");
                    int data = blocksRez.getInt("data");
                    Location loc = new Location(ctp.getServer().getWorld(rs1.getString("world")), blocksRez.getInt("x"), blocksRez.getInt("y"), z);
                    int type = blocksRez.getInt("block_type");

                    String dir = blocksRez.getString("direction");
                    Block block = loc.getBlock();

                    // Set main block info
                    block.setTypeId(type);
                    block.setData((byte)data);
                    if(!dir.equalsIgnoreCase("NO") && id != 50)
                    {
                        try
                        {
                            ((Directional) block.getType().getNewData(block.getData())).setFacingDirection(BlockFace.valueOf(dir));
                        }
                        catch(Exception e)
                        {
                            //System.out.println("Failed to change Direction!");
                        }
                    }

                    // restore block
                    switch (type)
                    {
                        case BlockID.WALL_SIGN:
                        case BlockID.SIGN_POST:
                        {
                            Sign sign = (Sign) block.getState();
                            String line1 = blocksRez.getString("first_line");
                            String line2 = blocksRez.getString("second_line");
                            String line3 = blocksRez.getString("third_line");
                            String line4 = blocksRez.getString("fourth_line");

                            if(line1 != null && !line1.equalsIgnoreCase("NULL"))
                                sign.setLine(0, line1);
                            if(line2 != null && !line2.equalsIgnoreCase("NULL"))
                                sign.setLine(1, line2);
                            if(line3 != null && !line3.equalsIgnoreCase("NULL"))
                                sign.setLine(2, line3);
                            if(line4 != null && !line4.equalsIgnoreCase("NULL"))
                                sign.setLine(3, line4);
                            break;
                        }

                        case BlockID.CHEST:
                        {
                            Chest chest = (Chest) block.getState();
                            Inventory inv = chest.getInventory();

                            ResultSet rs = ctp.mysqlConnector.getData("SELECT * FROM Item where Item.block_ID = " + id);
                            while (rs.next())
                            {
                                ItemStack item = new ItemStack(rs.getInt("type"), rs.getInt("amount"), (short)rs.getInt("durability"), (byte)rs.getInt("data"));
                                inv.setItem(rs.getInt("place_in_inv"), item);
                            }
                            break;
                        }

                        case BlockID.FURNACE:
                        case BlockID.BURNING_FURNACE:
                        {
                            Furnace furnace = (Furnace) block.getState();
                            Inventory inv = furnace.getInventory();

                            ResultSet rs = ctp.mysqlConnector.getData("SELECT * FROM Item where Item.block_ID = " + id);
                            while (rs.next())
                            {
                                ItemStack item = new ItemStack(rs.getInt("type"), rs.getInt("amount"), (short)rs.getInt("durability"), (byte)rs.getInt("data"));
                                inv.setItem(rs.getInt("place_in_inv"), item);
                            }
                            break;
                        }

                        case BlockID.DISPENSER:
                        {
                            Dispenser dispenser = (Dispenser) block.getState();
                            Inventory inv = dispenser.getInventory();

                            ResultSet rs = ctp.mysqlConnector.getData("SELECT * FROM Item where Item.block_ID = " + id);
                            while (rs.next())
                            {
                                ItemStack item = new ItemStack(rs.getInt("type"), rs.getInt("amount"), (short)rs.getInt("durability"), (byte)rs.getInt("data"));
                                inv.setItem(rs.getInt("place_in_inv"), item);
                            }
                            break;
                        }

                        case BlockID.MOB_SPAWNER:
                        {
                            CreatureSpawner mobSpawner = (CreatureSpawner) block.getState();

                            mobSpawner.setCreatureTypeId(blocksRez.getString("creature_type"));
                            mobSpawner.setDelay(blocksRez.getInt("delay"));

                            break;
                        }

                        case BlockID.NOTE_BLOCK:
                        {
                            NoteBlock noteBlock = (NoteBlock) block.getState();
                            noteBlock.setRawNote((byte)blocksRez.getInt("note_type"));
                            break;
                        }
                    }
                } // End of for Z cycle
            }
        }
        catch (SQLException ex)
        {
            Logger.getLogger(ArenaRestore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void restoreMySQLBlocks()
    {
        try
        {
            ResultSet rs1 = ctp.mysqlConnector.getData("SELECT * FROM Arena WHERE name like '"+ ctp.mainArena.name +"%'");

            if (rs1.next())
            {
                ResultSet blockCountRez = ctp.mysqlConnector.getData("SELECT COUNT(*) FROM (((Simple_block LEFT JOIN Sign on Sign.block_ID = id) LEFT JOIN note_block on note_block.block_ID = id)"+
                        " LEFT JOIN spawner_block on spawner_block.block_ID = id) where arena_name = '" + ctp.mainArena.name + "' and `block_type` NOT IN(6, 8, 9, 10, 11, 27, 28, 31, 32," +
                        "50, 51, 55, 59, 63, 64, 65, 66, 68, 69, 70, 71, 72, 75, 76, 77, 78, 81, 83, 90, 92, 93, 94, 96, 104, 105, 111, 115, 117, 342, 343, 328, 333)");

                int count = 0;
                if(blockCountRez.next())
                {
                    count = blockCountRez.getInt("count(*)");
                }

                ctp.arenaRestoreTimesRestored = 0;
                ctp.arenaRestoreMaxRestoreTimes = 0;
                int restoreCount = 2500;
                ctp.arenaRestoreMaxRestoreTimes = count / restoreCount;
                if((count % restoreCount) != 0)
                    ctp.arenaRestoreMaxRestoreTimes++;

                // For second time :/
                blockCountRez = ctp.mysqlConnector.getData("SELECT COUNT(*) FROM (((Simple_block LEFT JOIN Sign on Sign.block_ID = id) LEFT JOIN note_block on note_block.block_ID = id)"+
                        " LEFT JOIN spawner_block on spawner_block.block_ID = id) where arena_name = '" + ctp.mainArena.name + "' and `block_type` IN(6, 8, 9, 10, 11, 27, 28, 31, 32," +
                        "50, 51, 55, 59, 63, 64, 65, 66, 68, 69, 70, 71, 72, 75, 76, 77, 78, 81, 83, 90, 92, 93, 94, 96, 104, 105, 111, 115, 117, 342, 343, 328, 333)");

                count = 0;
                if(blockCountRez.next())
                {
                    count = blockCountRez.getInt("count(*)");
                }

                ctp.arenaRestoreTimesRestoredSec = 0;
                ctp.arenaRestoreMaxRestoreTimesSec = 0;

                ctp.arenaRestoreMaxRestoreTimesSec = count / restoreCount;
                if((count % restoreCount) != 0)
                    ctp.arenaRestoreMaxRestoreTimesSec++;
            }
            else
            {
                return;
            }
        }
        catch (SQLException ex)
        {
            Logger.getLogger(ArenaRestore.class.getName()).log(Level.SEVERE, null, ex);
        }

        ctp.CTP_Scheduler.arenaRestore = ctp.getServer().getScheduler().scheduleSyncRepeatingTask(ctp, new Runnable()
        {
            @Override
            public void run ()
            {
                ctp.mysqlConnector.connectToMySql();
                ctp.arenaRestore.restore(ctp.mainArena.name, ctp.arenaRestoreTimesRestored, false);
                ctp.arenaRestoreTimesRestored++;

                if(ctp.arenaRestoreTimesRestored == ctp.arenaRestoreMaxRestoreTimes)
                {
                    ctp.arenaRestoreTimesRestored = 0;
                    ctp.arenaRestoreMaxRestoreTimes = 0;
                    ctp.getServer().getScheduler().cancelTask(ctp.CTP_Scheduler.arenaRestore);
                    ctp.CTP_Scheduler.arenaRestore = 0;

                    // For second time
                    ctp.CTP_Scheduler.arenaRestoreSec  = ctp.getServer().getScheduler().scheduleSyncRepeatingTask(ctp, new Runnable()
                    {
                        @Override
                        public void run ()
                        {
                            ctp.mysqlConnector.connectToMySql();
                            ctp.arenaRestore.restore(ctp.mainArena.name, ctp.arenaRestoreTimesRestoredSec, true);
                            ctp.arenaRestoreTimesRestoredSec++;

                            if(ctp.arenaRestoreTimesRestoredSec == ctp.arenaRestoreMaxRestoreTimesSec)
                            {
                                ctp.arenaRestoreTimesRestoredSec = 0;
                                ctp.arenaRestoreMaxRestoreTimesSec = 0;
                                ctp.getServer().getScheduler().cancelTask(ctp.CTP_Scheduler.arenaRestoreSec);
                                ctp.CTP_Scheduler.arenaRestoreSec = 0;
                            }
                        }
                    }, 1L, 10L);
                }
            }
        }, 1L, 10L);
    }


    // Returns if blocks can be stacked in one entry at database.
    public boolean canStackBlocksToMySQL(int id, int blockToRestore, boolean first, int data, int newBlockData)
    {
        if(id != blockToRestore && !first)
            return false;
        if(data != newBlockData && !first)  // If block data is not equal
            return false;

        int[] unstackableBlocks = { 23, 29, 33, 34, 52, 54, 61, 62, 63, 64, 68, 71, 84, 93, 94, 95, 323, 324, 342, 343, 356 };
        for(int i = 0; i < unstackableBlocks.length; i++)
        {
            if(unstackableBlocks[i] == id)
                return false;
        }

        return true;
    }

}

class CTPBlock
{
    byte data;
    Location loc;
    int material;
    ItemStack[] inv;
}