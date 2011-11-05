package me.dalton.capturethepoints;

import java.util.Set;
import org.bukkit.entity.Player;

/** Player Data of the people playing CTP. */
public class PlayerData {
    
    /** The team this player is on. */
    public Team team; // Kjhf  
    
    /** The teamcolor this player is on. */
    public String color;
    
    /** The class/role this player has chosen. */
    public String role;
    
    /** The game money this player has. */
    public int money;

    public int health;
    
    /** The number of kills this player has. */
    public int kills;
    
    /** The number of kills in a row this player has. */
    public int killsInARow;
    
    /** The number of deaths this player has. */
    public int deaths;
    
    /** The number of deaths in a row this player has. */
    public int deathsInARow;

    public int moveChecker;
    
    /** The number of points this player has captured. */
    public int pointCaptures;
    
    /** The player's food level before joining a game. */
    public int foodLevel;
    
    /** The time the player joined the lobby (in ms -- gotten from System.currentTimeMillis). */
    public long lobbyJoinTime; // Kjhf  
    
    /** The ready state of this player. */
    public boolean isReady = false;
    
    /** The win state of this player. */
    public boolean winner = false;
    
    /** If this player is in the lobby. */
    public boolean isInLobby = false;
    
    /** If this player is in the arena. */
    public boolean isInArena = false;
    
    /** If this player has just joined the lobby. */
    public boolean justJoined = true;
    
    /** If this player was in creative mode before joining a game. */
    public boolean isInCreativeMode = false;
    
    /** If this player has been warned to ready up or be kicked. */
    public boolean warnedAboutActivity = false; // Kjhf

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
