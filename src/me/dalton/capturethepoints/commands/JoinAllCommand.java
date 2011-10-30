package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class JoinAllCommand extends CTPCommand {
   
    public JoinAllCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("joinall");
        super.aliases.add("jall");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin", "ctp.admin.joinall"};
        super.senderMustBePlayer = false;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp joinall";
    }

    @Override
    public void perform() {
        if (ctp.mainArena == null) {
            sender.sendMessage(ChatColor.RED + "Please create an arena first");
            return;
        }
        if (ctp.mainArena.lobby == null) {
            sender.sendMessage(ChatColor.RED + "Please create arena lobby");
            return;
        }
            
        if (ctp.isGameRunning()) {
            sender.sendMessage("[CTP] A previous Capture The Points game has been terminated.");
            ctp.blockListener.endGame(true);
        }
        int numberofplayers = ctp.getServer().getOnlinePlayers().length;
        ctp.chooseSuitableArena(numberofplayers); // Choose a suitable arena based on the number of players on the server.

        for (Player p : ctp.getServer().getOnlinePlayers()) {
            ctp.moveToLobby(p);
        }

        return;
    }
}