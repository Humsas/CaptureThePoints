package me.dalton.capturethepoints;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Humsas
 */
public class HealingItems
{
    public int hotHeal = 0;
    public int duration = 0;
    public int cooldown = 0;
    public int instantHeal = 0;

    public Items item = null;

    public List<PlayersAndCooldowns> cooldowns = new LinkedList<PlayersAndCooldowns>();
}