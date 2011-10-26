package me.dalton.capturethepoints;

import java.util.Set;
import org.bukkit.entity.Player;
public class PlayerData {
    public Team team; // Kjhf  
    public String color;
    public String role;
    public int money;
    public int kills;
    public int killsInARow;
    public int deaths;
    public int deathsInARow;
    public int pointCaptures;
    public int foodLevel;
    public long lobbyJoinTime; // Kjhf 
    public boolean isReady = false;
    public boolean winner = false;
    public boolean isInLobby = false;
    public boolean isInArena = false;
    public boolean justJoined = true;
    public boolean isInCreativeMode = false;
    public boolean warnedAboutActivity = false; // Kjhf 

    // Kjhf
    /** Get the player associated with this PlayerData
     * @param ctp CaptureThePoints instance
     * @return The Player */
    public Player getPlayer(CaptureThePoints ctp) {
        if (ctp.playerData.containsValue(this)) {
            Set<Player> players = ctp.playerData.keySet();
            for (Player p : players) {
                if (ctp.playerData.get(p) == this) {
                    return p;
                }
            }
        }
        return null;
    }
}
