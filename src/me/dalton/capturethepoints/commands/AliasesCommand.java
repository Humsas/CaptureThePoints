package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class AliasesCommand extends CTPCommand {
    
    // Kj's. Lists available aliases
    public AliasesCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("alias");
        super.aliases.add("aliases");
        super.notOpCommand = true;
        super.senderMustBePlayer = false;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp aliases";
    }

    @Override
    public void perform() {
        player.sendMessage(ChatColor.RED + "Aliases for some commands:");
            player.sendMessage(ChatColor.GREEN + "alias: " + ChatColor.WHITE + "aliases");
        if (canAccess(player, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
            player.sendMessage(ChatColor.GREEN + "join: " + ChatColor.WHITE + "j");
        }
        if (canAccess(player, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
            player.sendMessage(ChatColor.GREEN + "leave: " + ChatColor.WHITE + "exit, part, quit");
        }
        if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.pjoin"})) {
            player.sendMessage(ChatColor.GREEN + "pjoin: " + ChatColor.WHITE + "pj");
        }
        if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.joinall"})) {
            player.sendMessage(ChatColor.GREEN + "joinall: " + ChatColor.WHITE + "jall");
        }
        if (canAccess(player, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
            player.sendMessage(ChatColor.GREEN + "colors: " + ChatColor.WHITE + "colours, teams, players");
        }
        if (canAccess(player, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
            player.sendMessage(ChatColor.GREEN + "build: " + ChatColor.WHITE + "create, make");
        }
    }
}
