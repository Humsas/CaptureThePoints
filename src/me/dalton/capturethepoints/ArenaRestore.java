/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package me.dalton.capturethepoints;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;

/**
 *
 * @author Humsas
 */
public class ArenaRestore
{
    private List<CTPBlock> destroyedBlock = new LinkedList<CTPBlock>();
//    private List<Block> destroyedBlock = new LinkedList<Block>();
    // true if destroed, false if not
    private List<Boolean> blockStatus = new LinkedList<Boolean>();
    private CaptureThePoints ctp;


    public ArenaRestore(CaptureThePoints plugi)
    {
        ctp = plugi;
    }

    public void addBlock(Block block, boolean isDestroyed)
    {
        CTPBlock tmp = new CTPBlock();
        ContainerBlock dd;
        BlockState state = block.getState();
        //MaterialData data = state.getData();
        //chest
        if(state instanceof ContainerBlock)
        {
            dd = (ContainerBlock)state;
            ItemStack[] contents = dd.getInventory().getContents();
            tmp.inv = contents;
//            tmp.signDir = block.getFace(block);
        }

//        sign
//        if(state instanceof Sign)
//        {
//            tmp.sign = true;
//            Sign si = (Sign)state;
//            tmp.text = si.getLines();
//            tmp.signDir = getFacing(block);
//        }

        tmp.data = block.getData();
        tmp.loc = block.getLocation();
        tmp.material = block.getTypeId();
        //plugin.getServer().broadcastMessage("dd: " + data.getData() + " id:"+ data.getItemType());

        destroyedBlock.add(tmp);
        blockStatus.add(isDestroyed);
    }

    public void restoreAllBlocks()
    {
        for(int i = destroyedBlock.size()-1; i >= 0; i--)
        {
            Location blockLocation = destroyedBlock.get(i).loc;
            if(blockStatus.get(i))
            {
                CTPBlock tmp = destroyedBlock.get(i);
                blockLocation.getBlock().setTypeId(tmp.material);
                blockLocation.getBlock().setData(tmp.data);

                //chest
                if(tmp.inv != null && tmp.inv.length > 0)
                {
                    ContainerBlock dd = (ContainerBlock)blockLocation.getBlock().getState();
                    Inventory inv = dd.getInventory();
                    //ItemStack[] contents = tmp.inv;

                    //for (int j = 0; j < contents.length; j++)
                        //if(contents[j] != null)
                            inv.setContents(tmp.inv);//.addItem(contents[j]);
                }
//                if(tmp.sign)
//                {
//                    setFacing(tmp.signDir, blockLocation);
//                    for(int j = 0; j < tmp.text.length; j++)
//                        ((Sign) blockLocation.getBlock().getState()).setLine(j, tmp.text[j]);
//                }
            }
            else
            {
                blockLocation.getBlock().setTypeId(0);
            }
        }
        destroyedBlock.clear();
        blockStatus.clear();
    }

    public BlockFace getFacing(Block block)
    {
        return ((Directional) block.getType().getNewData(block.getData())).getFacing();
    }
    public void setFacing(BlockFace facing, Location loc)
    {
        org.bukkit.material.Sign sign = new org.bukkit.material.Sign();
        sign.setFacingDirection(facing);
        loc.getBlock().setData(sign.getData(), true);
    }
}

class CTPBlock
{
    byte data;
    Location loc;
    int material;
    ItemStack[] inv;
//    boolean sign = false;
//    String[] text;
//    BlockFace signDir;
}
