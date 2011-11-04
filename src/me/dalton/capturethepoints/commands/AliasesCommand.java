package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class AliasesCommand extends CTPCommand {
    
    /** Command for a helpful list of aliases. */
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
        if (ctp.canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
            sender.sendMessage(ChatColor.GREEN + "build: " + ChatColor.WHITE + "b, create, make");
        }
        if (ctp.canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin", "ctp.colors"})) {
            sender.sendMessage(ChatColor.GREEN + "colors: " + ChatColor.WHITE + "colours, teams, players");
        } 
        sender.sendMessage(ChatColor.GREEN + "help: " + ChatColor.WHITE + "?");
        if (ctp.canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.joinall"})) {
            sender.sendMessage(ChatColor.GREEN + "joinall: " + ChatColor.WHITE + "jall");
        }
        if (ctp.canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
            sender.sendMessage(ChatColor.GREEN + "join: " + ChatColor.WHITE + "j, play");
        }            
        if (ctp.canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
            sender.sendMessage(ChatColor.GREEN + "leave: " + ChatColor.WHITE + "exit, part, quit");
        }
        if (ctp.canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.pjoin"})) {
            sender.sendMessage(ChatColor.GREEN + "pjoin: " + ChatColor.WHITE + "pj");
        }
        if (ctp.canAccess(sender, true, new String[]{"ctp.*", "ctp.admin", "ctp.rejoin"})) {
            sender.sendMessage(ChatColor.GREEN + "rejoin: " + ChatColor.WHITE + "rj");
        }
        if (ctp.canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setarena", "ctp.admin.select", "ctp.admin.selectarena"})) {
            sender.sendMessage(ChatColor.GREEN + "select: " + ChatColor.WHITE + "setarena, selectarena, arena");
        }
        if (ctp.canAccess(sender, false, new String[]{"ctp.*", "ctp.admin.start", "ctp.admin"})) {
            sender.sendMessage(ChatColor.GREEN + "start: " + ChatColor.WHITE + "go");
        }
    }
}
