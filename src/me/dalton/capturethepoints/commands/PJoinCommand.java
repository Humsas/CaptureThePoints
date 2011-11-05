package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PJoinCommand extends CTPCommand {
   
    /** Allows admin to force a player into playing a CTP game. */
    public PJoinCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("pjoin");
        super.aliases.add("pj");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin.pjoin", "ctp.admin"};
        super.senderMustBePlayer = false;
        super.minParameters = 3;
        super.maxParameters = 3;
        super.usageTemplate = "/ctp pjoin <player>";
    }

    @Override
    public void perform() {
        if (sender instanceof Player) {
            String error = ctp.checkMainArena(player, ctp.mainArena);
            if (!error.isEmpty()) {
                sender.sendMessage(error);
                return;
            }
        } else {
            if (ctp.mainArena == null) {
                sender.sendMessage(ChatColor.RED + "Please create an arena first");
                return;
            }
            if (ctp.mainArena.lobby == null) {
                sender.sendMessage(ChatColor.RED + "Please create arena lobby");
                return;
            }
        }
        Player bob = ctp.getServer().getPlayer(parameters.get(2));
        if (bob == null) {
            sender.sendMessage(ChatColor.RED+"Could not find the online player " + ChatColor.GOLD + parameters.get(2) + ChatColor.RED +".");
            return;
        }
        if (!ctp.blockListener.isAlreadyInGame(bob)) {
            bob.sendMessage(ChatColor.GREEN + sender.getName() + ChatColor.WHITE + " forced you to join CTP!");
            ctp.moveToLobby(bob);
        } else {
            sender.sendMessage(ChatColor.GOLD + parameters.get(2) + ChatColor.RED +" is already playing CTP!");
        }
        return;
    }
}