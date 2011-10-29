package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class RejoinCommand extends CTPCommand {
   
    // Kj -- If there is a game on already, allow user to join. If not, deny. Useful for permissions.
    public RejoinCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("rejoin");
        super.aliases.add("rj");
        super.notOpCommand = true;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.play", "ctp.admin", "ctp.rejoin"};
        super.senderMustBePlayer = true;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp rejoin";
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