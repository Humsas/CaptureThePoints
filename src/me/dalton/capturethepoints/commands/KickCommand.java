package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KickCommand extends CTPCommand {
   
    public KickCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("kick");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin.kick", "ctp.admin"};
        super.senderMustBePlayer = true;
        super.minParameters = 3;
        super.maxParameters = 3;
        super.usageTemplate = "/ctp kick <player>";
    }

    @Override
    public void perform() {
        if (ctp.mainArena == null) {
            player.sendMessage(ChatColor.RED + "Please create an arena first");
            return;
        }
        if (ctp.mainArena.lobby == null) {
            player.sendMessage(ChatColor.RED + "Please create arena lobby");
            return;
        }
        Player bob = ctp.getServer().getPlayer(parameters.get(2));
        if (bob == null) {
            player.sendMessage("Player " + ChatColor.RED + parameters.get(2) + ChatColor.WHITE + " is currently offline!");
            return;
        }
        if (ctp.blockListener.isAlreadyInGame(bob)) {
            bob.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.WHITE + " kicked you from CTP game!");
            ctp.leaveGame(bob);
        } else {
            player.sendMessage(ChatColor.RED + "This player is not playing CTP!");
        }
        return;
    }
}