package me.dalton.capturethepoints;

// Kjhf's
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.entity.Player;

public class Lobby
{
    public HashMap<Player, Boolean> playersinlobby = new HashMap<Player, Boolean>(); // Player and their ready status
    public double x;
    public double y;
    public double z;
    public double dir;

    public Lobby(double x, double y, double z, double dir)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dir = dir;
    }

    /** Returns boolean stating whether any players in the lobby hashmap have "false" as their ready status boolean. */
    public boolean hasUnreadyPeople()
    {
        return playersinlobby.values().contains(false);
    }

    /** Return the number of players with a false ready status. */
    public int countUnreadyPeople()
    {
        if (playersinlobby.values().contains(false))
        {
            int counter = 0;
            for (Boolean aBool : playersinlobby.values())
            {
                if (aBool == false)
                {
                    counter++;
                } else
                {
                    continue;
                }
            }
            return counter;
        } else
        {
            return 0;
        }
    }
    /** Return the number of players with a true ready status. */
    public int countReadyPeople()
    {
        if (playersinlobby.values().contains(true))
        {
            int counter = 0;
            for (Boolean aBool : playersinlobby.values())
            {
                if (aBool == true)
                {
                    counter++;
                }
                else
                {
                    continue;
                }
            }
            return counter;
        } 
        else
        {
            return 0;
        }
    }

    /** Return a list of players with a true ready status. */
    public List<Player> getReadyPeople()
    {
        if (playersinlobby.values().contains(true))
        {
            List<Player> players = new ArrayList<Player>();
            for (Player player : playersinlobby.keySet())
            {
                if (playersinlobby.get(player) == true)
                {
                    players.add(player);
                } 
                else
                {
                    continue;
                }
            }
            return players;
        } 
        else
        {
            return null;
        }
    }

    /** Return number of players in lobby hashmap. */
    public int getAmountOfPlayersInLobby()
    {
        return playersinlobby.size();
    }
}
