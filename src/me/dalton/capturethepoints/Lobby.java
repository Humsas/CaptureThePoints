package me.dalton.capturethepoints;

// Kjhf's
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.entity.Player;

/** A Lobby in a CTP arena */
public class Lobby {
    /** List of Players and their ready status */
    public HashMap<Player, Boolean> playersinlobby = new HashMap<Player, Boolean>();
    
    /** List of Players who have been in this ctp lobby. They may still be in the Lobby. */
    public List<Player> playerswhowereinlobby = new ArrayList<Player>();
    
    /** X co-ordinate of this lobby's spawn */
    public double x = 0D;
    
    /** Y co-ordinate of this lobby's spawn */
    public double y = 0D;
    
    /** Z co-ordinate of this lobby's spawn */
    public double z = 0D;
    
    /** Direction players spawn in this lobby */
    public double dir = 0D;

    /** A Lobby in a CTP arena */
    public Lobby(double x, double y, double z, double dir) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dir = dir;
    }
        
    /** Returns boolean stating whether any players in the lobby hashmap have "false" as their ready status boolean. */
    public boolean hasUnreadyPeople() {
        return playersinlobby.values().contains(false);
    }
    
    /** Return the number of players with a false ready status. */
    public int countUnreadyPeople() {
        if (playersinlobby.values().contains(false)) {
            int counter = 0;
            for (Boolean aBool : playersinlobby.values()) {
                if (aBool == false) {
                    counter++;
                    continue;
                } else {
                    continue;
                }
            }
            return counter;
        } else {
            return 0;
        }
    }
    /** Return the number of players with a true ready status. */
    public int countReadyPeople() {
        if (playersinlobby.values().contains(true)) {
            int counter = 0;
            for (Boolean aBool : playersinlobby.values()) {
                if (aBool == true) {
                    counter++;
                    continue;
                } else {
                    continue;
                }
            }
            return counter;
        } else {
            return 0;
        }
    }
    
    /** Return a list of players with a false ready status. */
    public List<String> getUnreadyPeople() {
        if (playersinlobby.values().contains(false)) {
            List<String> players = new ArrayList<String>();
            for (Player player : playersinlobby.keySet()) {
                if (playersinlobby.get(player) == false) {
                    players.add(player.getName());
                } else {
                    continue;
                }
            }
            return players;
        } else {
            return null;
        }
    }
    
    /** Return number of players in lobby hashmap. */
    public int countAllPeople() {
        return playersinlobby.size();        
    }
    
    /** Clears the data for the players and their statuses saved by this lobby */
    public void clearLobbyPlayerData() {
        this.playersinlobby.clear();
        this.playerswhowereinlobby.clear();
    }
    

    /** Get the last person to join this Lobby who is still online.
     * @param canBeInLobby if true, may return someone who is still in the lobby. If false, ignores those in the lobby.
     * @return The player or null if none found. */
    public Player getLastJoiner(boolean canBeInLobby) {
        List<Player> players = this.playerswhowereinlobby;
        for (int i = 1; i < players.size() ; i++) {
            if (players.get(players.size()-i) != null) {
                Player testplayer = players.get(players.size()-i);
                if (canBeInLobby) {
                    return testplayer; // Don't bother checking the lobby
                } else {
                    if (playersinlobby.get(testplayer) != null) {
                        continue; // Player is in the lobby.
                    } else {
                        return testplayer; // Player not in the lobby.
                    }
                }
            }
        }
        return null;
    }
}
