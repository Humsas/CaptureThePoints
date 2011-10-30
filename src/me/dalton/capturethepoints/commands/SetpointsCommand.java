package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.Team;
import org.bukkit.ChatColor;

public class SetpointsCommand extends CTPCommand {
   
    /** Allows admin to set the points/score that a team has. May screw up if points system is used rather than score. */
    public SetpointsCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("setpoints");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin.setpoints", "ctp.admin"};
        super.senderMustBePlayer = false;
        super.minParameters = 4;
        super.maxParameters = 4;
        super.usageTemplate = "/ctp setpoints <Teamcolor> <number>";
    }

    @Override
    public void perform() {
        int points = 0;
        try {
            points = Integer.parseInt(parameters.get(3));
        } catch (Exception NumberFormatException) {
            sender.sendMessage(ChatColor.RED + "Incorect number format. Usage: " + ChatColor.GREEN + "/ctp setpoints <Teamcolor> <number>");
            return;
        }

        if (ctp.configOptions.useScoreGeneration) {
            for (Team team : ctp.teams) {
                if (team.color.equalsIgnoreCase(parameters.get(2))) {
                    team.score = points;
                }
            }
            ctp.blockListener.didSomeoneWin();
        } else {
            for (Team team : ctp.teams) {
                if (team.color.equalsIgnoreCase(parameters.get(2))) {
                    team.controledPoints = points;
                }
            }
            ctp.blockListener.didSomeoneWin();
        }
        sender.sendMessage(ChatColor.RED + "There is no such color!");
        return;
    }
}