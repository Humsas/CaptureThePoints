package me.dalton.capturethepoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.entity.Player;

/** Arena Data of the saved arenas for playing CTP. */
public class ArenaData {
    /** The name of this arena */
    public String name;
    
    /** The name of the world this arena is in */
    public String world;
    
    /** The teamspawns this arena has (Hashmap of Teamcolor, Spawn). 
     * @see Spawn */
    public HashMap<String, Spawn> teamSpawns = new HashMap<String, Spawn>();
    
    /** The capture points this arena has. 
     * @see CTPPoints */
    public List<CTPPoints> capturePoints = new LinkedList<CTPPoints>();
    
    /** This arena's Lobby 
     * @see Lobby */
    public Lobby lobby;
    
    /** The first X co-ordinate representing the boundary of this arena. */
    public int x1;
    
    /** The first Z co-ordinate representing the boundary of this arena. */
    public int z1;
    
    /** The second X co-ordinate representing the boundary of this arena. */
    public int x2;
    
    /** The second Z co-ordinate representing the boundary of this arena. */
    public int z2;
    
    /** The minimum number of players this arena can take. [Default: 2] */
    public int minimumPlayers = 2;
    
    /** The maximum number of players this arena can take. [Default: 9999] */
    public int maximumPlayers = 9999;

    /** Get all Players in this arena as a list of playername strings
     * @param ctp CaptureThePoints instance
     * @return The playername list */
    public List<String> getPlayersPlaying(CaptureThePoints ctp) {
        if (!ctp.mainArena.equals(this)) {
            return null;
        }
        List<String> players = new ArrayList<String>();
        for (Player p : ctp.playerData.keySet()) {
            if (ctp.playerData.get(p).team == null || ctp.playerData.get(p).color == null) {
                continue; // Player is not yet in game.
            } else {
                players.add(p.getName());
            }
        }
        return players;
    }
}
