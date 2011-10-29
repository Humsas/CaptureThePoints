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
        sender.sendMessage(ChatColor.RED + "Aliases for some commands:");
            sender.sendMessage(ChatColor.GREEN + "alias: " + ChatColor.WHITE + "aliases");
        if (canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
            sender.sendMessage(ChatColor.GREEN + "join: " + ChatColor.WHITE + "j");
        }
        if (canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
            sender.sendMessage(ChatColor.GREEN + "leave: " + ChatColor.WHITE + "exit, part, quit");
        }
        if (canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.pjoin"})) {
            sender.sendMessage(ChatColor.GREEN + "pjoin: " + ChatColor.WHITE + "pj");
        }
        if (canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.joinall"})) {
            sender.sendMessage(ChatColor.GREEN + "joinall: " + ChatColor.WHITE + "jall");
        }
        if (canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
            sender.sendMessage(ChatColor.GREEN + "colors: " + ChatColor.WHITE + "colours, teams, senders");
        }
        if (canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
            sender.sendMessage(ChatColor.GREEN + "build: " + ChatColor.WHITE + "create, make");
        }
    }
}
