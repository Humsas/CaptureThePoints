package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class HelpCommand extends CTPCommand {
    
    public HelpCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("help");
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
            player.sendMessage(ChatColor.RED + "CTP Commands: " + ChatColor.GOLD + " Page 1/2");
            if (canAccess(player, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp help [pagenumber] " + ChatColor.WHITE + "- view this menu.");
            }
            if (canAccess(player, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp aliases " + ChatColor.WHITE + "- list of helpful command aliases"); // Kj
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.auto", "ctp.admin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp auto <worldname>" + ChatColor.WHITE + "- bring every player in the world to a random suitable arena."); // Kj
            }
            if (canAccess(player, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build help " + ChatColor.WHITE + "- arena editing commands list");
            }
            if (canAccess(player, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp colors " + ChatColor.WHITE + "- available colors and players in-game"); // Kj -- fixed typo
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.joinall"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp joinall " + ChatColor.WHITE + "- make all players join the game");
            }
            if (canAccess(player, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp join " + ChatColor.WHITE + "- join the game");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.kick"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp kick <player> " + ChatColor.WHITE + "- kicks player from the game");
            }
            if (canAccess(player, true, new String[]{"ctp.*", "ctp.play", "ctp.admin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp leave " + ChatColor.WHITE + "- leave the game");
            }
        } else if (pagenumber.equals("2")) {
            player.sendMessage(ChatColor.RED + "CTP Commands: " + ChatColor.GOLD + " Page 2/2");
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.pjoin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp pjoin <player> " + ChatColor.WHITE + "- makes this player join the game");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.reload"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp reload " + ChatColor.WHITE + "- reload CTP config files");
            }
            if (canAccess(player, true, new String[]{"ctp.*", "ctp.play", "ctp.admin", "ctp.rejoin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp rejoin " + ChatColor.WHITE + "- join a game if one has started.");
            }
            /*
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.save"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp save " + ChatColor.WHITE + "- save");
            }
             */
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setpoints"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp setpoints <TeamColor> <number> " + ChatColor.WHITE + "- Set the chosen team's points/score");
            }
            if (canAccess(player, true, new String[]{"ctp.*", "ctp.admin", "ctp.play"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp stats " + ChatColor.WHITE + "- get your in-game stats"); // Kj
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.stop"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp stop " + ChatColor.WHITE + "- stops already running game"); // Kj -- fixed typo
            }
            if (canAccess(player, true, new String[]{"ctp.*", "ctp.admin", "ctp.play"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp team  " + ChatColor.WHITE + "- gets the members on your team");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp version  " + ChatColor.WHITE + "- check this plugin's version");
            }
        }
    }
}
