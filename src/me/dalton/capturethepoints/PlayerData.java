 package me.dalton.capturethepoints;

import org.bukkit.Location;

public class PlayerData
{
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

    public boolean isReady;
    public boolean winner = false;
    public boolean isInLobby = false;
    public boolean isInArena = false;
    public boolean justJoined = true;
    public boolean isInCreativeMode = false;
    public boolean warnedAboutActivity = false; // Kjhf
}