package me.dalton.capturethepoints;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Team {
  public ChatColor chatcolor = ChatColor.GREEN; // Kjhf  
  public String color;
  public int memberCount;
  public int score;
  public int controledPoints;
  
      // Kjhf
    /** Get all Players in this team as a list of Players
     * @param ctp CaptureThePoints instance
     * @return The Player list */
    public List<Player> getTeamPlayers(CaptureThePoints ctp) {
        if (!ctp.teams.contains(this)) {
            return null;
        }
        List<Player> teamplayers = new ArrayList<Player>();
        
        Set<Player> players = ctp.playerData.keySet();
        for (Player p : players) {
            if (ctp.playerData.get(p).team == null || ctp.playerData.get(p).color == null) { 
                continue; // Player is not yet in game.
            }
            if (ctp.playerData.get(p).team == this || ctp.playerData.get(p).color.equalsIgnoreCase(this.color)) {
                teamplayers.add(p);
            }
        }
        return teamplayers;        
    }
    
      // Kjhf
    /** Get all Players in this team as a list of playername strings
     * @param ctp CaptureThePoints instance
     * @return The playername list */
    public List<String> getTeamPlayerNames(CaptureThePoints ctp) {
        if (!ctp.teams.contains(this)) {
            return null;
        }
        List<String> teamplayers = new ArrayList<String>();
        
        Set<Player> players = ctp.playerData.keySet();
        for (Player p : players) {
            if (ctp.playerData.get(p).team == null || ctp.playerData.get(p).color == null) { 
                continue; // Player is not yet in game.
            }
            if (ctp.playerData.get(p).team == this || ctp.playerData.get(p).color.equalsIgnoreCase(this.color)) {
                teamplayers.add(p.getName());
            }
        }
        return teamplayers;        
    }
}

