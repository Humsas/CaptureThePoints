package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class JoinCommand extends CTPCommand {
   
    /** Allows player to join ctp game. Starts a new one if one isn't running already. */
    public JoinCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("join");
        super.aliases.add("j");
        super.notOpCommand = true;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.play", "ctp.admin", "ctp.join"};
        super.senderMustBePlayer = true;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp join";
    }

    @Override
    public void perform() {
        if (!ctp.blockListener.isAlreadyInGame(player)) {
            if (ctp.mainArena == null) {
                player.sendMessage(ChatColor.RED + "Please create an arena first");
                return;
            }
            if (ctp.mainArena.lobby == null) {
                player.sendMessage(ChatColor.RED + "Please create arena lobby");
                return;
            }

            ctp.moveToLobby(player);
            return;
        }
        player.sendMessage(ChatColor.RED + "You are already playing game!");
        return;
    }
}