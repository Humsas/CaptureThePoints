package me.dalton.capturethepoints;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ArenaData
{
    public String name;
    public String world;
    public HashMap<String, CTPPoints> teamSpawns = new HashMap<String, CTPPoints>();
    public List<CTPPoints> capturePoints = new LinkedList<CTPPoints>();
    public Lobby lobby;
    public int x1;
    public int z1;
    public int x2;
    public int z2;
}