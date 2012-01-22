package me.dalton.capturethepoints;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/** A CTP Team */
public class Team {
    /** The associated ChatColor of this Team. Defaults to GREEN 
     * @see ChatColor */
    public ChatColor chatcolor = ChatColor.GREEN; // Kjhf  
    
    /** This Team's color */
    public String color;
    
    /** The number of players in this Team */
    public int memberCount;
    
    /** This Team's score */
    public int score;
    
    /** The number of control points this Team has */
    public int controledPoints;
    
    /** This Team's spawn point */
    public Spawn spawn;    
    
    /** Get a Team from its color 
     * @param ctp CaptureThePoints instance
     * @param color The team's color
     * @return The Team corresponding to this color. */
    public static Team getTeamFromColor(CaptureThePoints ctp, String color) {
        for (Team aTeam : ctp.mainArena.teams) {
            if (aTeam.color.equalsIgnoreCase(color)) {
                return aTeam;
            }
        }
        return null;
    }
    
    /** Get all Players in this team as a list of Players
     * @param ctp CaptureThePoints instance
     * @return The Player list */
    public List<Player> getTeamPlayers(CaptureThePoints ctp)
    {
        List<Player> teamplayers = new ArrayList<Player>();

        Set<Player> players = ctp.playerData.keySet();
        for (Player p : players)
        {
            if (ctp.playerData.get(p).team == null)
            {
                continue; // Player is not yet in game.
            }
            if (ctp.playerData.get(p).team.color.equalsIgnoreCase(this.color))
            {
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
        if (!ctp.mainArena.teams.contains(this)) {
            return null;
        }
        List<String> teamplayers = new ArrayList<String>();

        Set<Player> players = ctp.playerData.keySet();
        for (Player p : players) {
            if (ctp.playerData.get(p).team == null || ctp.playerData.get(p).team.color == null) {
                continue; // Player is not yet in game.
            }
            if (ctp.playerData.get(p).team == this && ctp.playerData.get(p).team.color.equalsIgnoreCase(this.color)) {
                teamplayers.add(p.getName());
            }
        }
        return teamplayers;
    }
    
    /** Get a Random Player in this Team
     * @param ctp CaptureThePoints instance
     * @return The Player */
    public Player getRandomPlayer(CaptureThePoints ctp) {
        List<Player> teamPlayers = getTeamPlayers(ctp);
        Random random = new Random();
        int nextInt = random.nextInt(teamPlayers.size());
        return teamPlayers.get(nextInt);
    }
    
    /** Check this Team for errors. Currently only checks memberCount against TeamPlayers size.
     * @return boolean Has error? */
    public boolean sanityCheck(CaptureThePoints ctp) {
        if (this.getTeamPlayers(ctp) == null) {
            return this.memberCount != 0;
        } else {
            return this.getTeamPlayers(ctp).size() != this.memberCount;    
        }
    }
}
