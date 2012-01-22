package me.dalton.capturethepoints;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

/** Simpler ItemStack class  */
public class Items
{
    /** This Item's Material 
     * @see org.bukkit.Material */
    public Material item;
    
    /** The amount of this Item */
    public int amount;
    
    /** The items data type */
    public short type = -1;

    /** The items enchantments */
    public List<Enchantment> enchantments = new LinkedList<Enchantment>();

    /** The items levels of enchantments */
    public List<Integer> enchLevels = new LinkedList<Integer>();

    /** Money reward */
    public int money = 0;

    /** exp reward */
    public int exp = 0;
}
