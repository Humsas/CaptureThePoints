package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class RejoinCommand extends CTPCommand {
  
    /** Allows player to join a ctp game that is already running if they have previously disconnected from it. Does NOT start a new one. */
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
                if (ctp.mainArena.lobby.playerswhowereinlobby.contains(player)) {
                    ctp.moveToLobby(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Cannot rejoin -- you haven't disconnected from this game.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Game not started yet.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You are already playing game!");
        }
    }
}