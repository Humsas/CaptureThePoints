package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.ArenaData;
import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class SelectCommand extends CTPCommand {
   
    public SelectCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("setarena");
        super.aliases.add("selectarena");
        super.aliases.add("select");
        super.aliases.add("arena");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin", "ctp.admin.setarena", "ctp.admin.select", "ctp.admin.selectarena"};
        super.senderMustBePlayer = false;
        super.minParameters = 3;
        super.maxParameters = 3;
        super.usageTemplate = "/ctp select <Arena name>";
    }

    @Override
    public void perform() {
        String newarena = parameters.get(2);
        
        if (!ctp.arena_list.contains(newarena)) {
            sender.sendMessage(ChatColor.RED + "Could not load arena " + ChatColor.GOLD + newarena + ChatColor.RED + " because the name cannot be found. Check your config file and existing arenas.");
            return;
        }
        
        ArenaData loadArena = ctp.loadArena(newarena);
        
        if (loadArena == null) {
            sender.sendMessage(ChatColor.RED + "Could not load arena " + ChatColor.GOLD + newarena + ChatColor.RED + " because it isn't finished yet. Check your config file and existing arenas.");
            return;            
        }

        if (!ctp.mainArena.name.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Changed selected arena from " + ctp.mainArena.name + " to " + newarena + " to play.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Selected " + newarena + " for playing.");
        }
        
        ctp.mainArena = loadArena;
        return;
    }
}