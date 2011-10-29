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
        for (Player p : ctp.getServer().getOnlinePlayers())
        {
            if (ctp.blockListener.isAlreadyInGame(player)) {
                continue;
            }
            if (ctp.mainArena == null) {
                player.sendMessage(ChatColor.RED + "Please create an arena first");
                return;
            }
            if (ctp.mainArena.lobby == null) {
                player.sendMessage(ChatColor.RED + "Please create arena lobby");
                return;
            }

            ctp.moveToLobby(p);
        }

        return;
    }
}