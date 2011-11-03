package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class LateJoinCommand extends CTPCommand {
   
    /** Allows player to join a ctp game that is already running. Does NOT start a new one. */
    public LateJoinCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("latejoin");
        super.aliases.add("lj");
        super.notOpCommand = true;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.play", "ctp.admin", "ctp.latejoin"};
        super.senderMustBePlayer = true;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp latejoin";
    }

    @Override
    public void perform() {
        if (!ctp.blockListener.isAlreadyInGame(player)) {
            if (ctp.isGameRunning()) {
                ctp.moveToLobby(player);
            } else {
                player.sendMessage(ChatColor.RED + "Game not started yet.");
            }
            return;
        }
        player.sendMessage(ChatColor.RED + "You are already playing game!");
        return;
    }
}