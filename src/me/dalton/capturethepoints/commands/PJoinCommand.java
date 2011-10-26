package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PJoinCommand extends CTPCommand {
   
    public PJoinCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("pjoin");
        super.aliases.add("pj");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin.pjoin", "ctp.admin"};
        super.senderMustBePlayer = true;
        super.minParameters = 3;
        super.maxParameters = 3;
        super.usageTemplate = "/ctp pjoin <player>";
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
        if (!ctp.blockListener.isAlreadyInGame(bob)) {
            bob.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.WHITE + " forced you to join CTP!");
            ctp.moveToLobby(bob);
        } else {
            player.sendMessage(ChatColor.RED + "This player is already playing CTP!");
        }
        return;
    }
}