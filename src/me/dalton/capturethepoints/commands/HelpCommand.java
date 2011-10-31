package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class HelpCommand extends CTPCommand {
    
    /** Help command. Also displays if just "/ctp" is typed. */
    public HelpCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("help");
        super.aliases.add("?");
        super.notOpCommand = true;
        super.senderMustBePlayer = false;
        super.minParameters = 1;
        super.maxParameters = 3;
        super.usageTemplate = "/ctp help [pagenumber]";
    }

    @Override
    public void perform() {
        String pagenumber = "";
        if (parameters.size() == 3) {
            pagenumber = parameters.get(2);
        }
        if (parameters.size() == 1 || parameters.size() == 2 || pagenumber.isEmpty() || pagenumber.equals("1")) {
            sender.sendMessage(ChatColor.RED + "CTP Commands: " + ChatColor.GOLD + " Page 1/2");
            if (canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp help [pagenumber] " + ChatColor.WHITE + "- view this menu.");
            }
            if (canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp aliases " + ChatColor.WHITE + "- list of helpful command aliases");
            }
            if (canAccess(sender, false, new String[]{"ctp.*", "ctp.auto", "ctp.admin"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp auto <worldname>" + ChatColor.WHITE + "- bring every player in the world to a random suitable arena.");
            }
            if (canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp build help " + ChatColor.WHITE + "- arena editing commands list");
            }
            if (canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp colors " + ChatColor.WHITE + "- available colors and senders in-game");
            }
            if (canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.joinall"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp joinall " + ChatColor.WHITE + "- make all players join the game");
            }
            if (canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp join " + ChatColor.WHITE + "- join the game");
            }
            if (canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.kick"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp kick <sender> " + ChatColor.WHITE + "- kicks player from the game");
            }
            if (canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp leave " + ChatColor.WHITE + "- leave the game");
            }
        } else if (pagenumber.equals("2")) {
            sender.sendMessage(ChatColor.RED + "CTP Commands: " + ChatColor.GOLD + " Page 2/2");
            if (canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.pjoin"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp pjoin <sender> " + ChatColor.WHITE + "- makes this player join the game");
            }
            if (canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.reload"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp reload " + ChatColor.WHITE + "- reload CTP config files");
            }
            if (canAccess(sender, true, new String[]{"ctp.*", "ctp.play", "ctp.admin", "ctp.rejoin"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp rejoin " + ChatColor.WHITE + "- join a game if one has started.");
            }
            /*
            if (canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.save"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp save " + ChatColor.WHITE + "- save");
            }
             */
            if (canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setpoints"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp setpoints <TeamColor> <number> " + ChatColor.WHITE + "- Set the chosen team's points/score");
            }
            if (canAccess(sender, true, new String[]{"ctp.*", "ctp.admin", "ctp.play"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp stats " + ChatColor.WHITE + "- get your in-game stats");
            }
            if (canAccess(sender, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.stop"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp stop " + ChatColor.WHITE + "- stops already running game");
            }
            if (canAccess(sender, true, new String[]{"ctp.*", "ctp.admin", "ctp.play"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp team  " + ChatColor.WHITE + "- gets the members on your team");
            }
            if (canAccess(sender, false, new String[]{"ctp.*", "ctp.admin"})) {
                sender.sendMessage(ChatColor.GREEN + "/ctp version  " + ChatColor.WHITE + "- check this plugin's version");
            }
        }
    }
}
