package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class LeaveCommand extends CTPCommand {
   
    /** Allows player to leave ctp game. */
    public LeaveCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("leave");
        super.aliases.add("exit");
        super.aliases.add("part");
        super.aliases.add("quit");
        super.aliases.add("l");
        super.notOpCommand = true;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.play", "ctp.admin"};
        super.senderMustBePlayer = true;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp leave";
    }

    @Override
    public void perform() {
        if (!ctp.blockListener.isAlreadyInGame(player)) {
            player.sendMessage(ChatColor.RED + "You are not in the game!");
            return;
        }
        ctp.leaveGame(player);
        return;
    }
}