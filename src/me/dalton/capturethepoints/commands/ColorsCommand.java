package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.Team;
import org.bukkit.ChatColor;

public class ColorsCommand extends CTPCommand {
    
    public ColorsCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("colors");
        super.aliases.add("colours");
        super.aliases.add("players");
        super.aliases.add("teams");
        super.notOpCommand = true;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.play", "ctp.admin"};
        super.senderMustBePlayer = true;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp colors";
    }

    @Override
    public void perform() {       
        if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin"})) {
            player.sendMessage(ChatColor.RED + "Admin: " + ChatColor.BLUE +"Available team colors:"); // Kj -- typo ;)
            player.sendMessage(ChatColor.GREEN + "WHITE, LIGHTGRAY, GRAY, BLACK, RED, ORANGE, YELLOW, LIME, LIGHTBLUE, GREEN, CYAN, BLUE, PURPLE, MAGENTA, PINK, BROWN");
        }
        
        if (ctp.teams.size() > 0) {
            String theteams = "";
            for (int i = 0; i < ctp.teams.size(); i++) {
                theteams = theteams + ctp.teams.get(i).chatcolor + ctp.teams.get(i).color + ChatColor.WHITE + ", "; // Kj -- added colour, changed team to team color (its name)
            }
            player.sendMessage("Teams: " + ChatColor.GREEN + theteams.toLowerCase().substring(0, theteams.length() - 2)); // minus ", " from end

            String playernames = "";
            ChatColor cc = ChatColor.GREEN;
            for (Team aTeam : ctp.teams) {
                cc = aTeam.chatcolor;
                playernames += cc;
                playernames += aTeam.getTeamPlayerNames(ctp);
                playernames += " ";
            }
            player.sendMessage(ChatColor.GREEN + String.valueOf(ctp.playerData.size()) + " players: " + playernames);
            return;
        }

        player.sendMessage(ChatColor.BLUE + "There are no existing teams to join.");
        return;
    }
}
