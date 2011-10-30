package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class AutoCommand extends CTPCommand {

    /** This command will bring all players on a world into a random lobby which is guaranteed to hold everyone (if not, use the already selected arena) */
    public AutoCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("auto");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin", "ctp.admin.auto"};
        super.senderMustBePlayer = false;
        super.minParameters = 3;
        super.maxParameters = 3;
        super.usageTemplate = "/ctp auto <worldname>";
    }

    @Override
    public void perform() {
        if (ctp.mainArena == null) {
            sender.sendMessage(ChatColor.RED + "Please create an arena first!");
            return;
        }
        if (ctp.mainArena.lobby == null) {
            sender.sendMessage(ChatColor.RED + "Please create arena lobby!");
            return;
        }

        World world = ctp.getServer().getWorld(parameters.get(2));
        if (world == null) {
            sender.sendMessage(ChatColor.RED + parameters.get(2) + " is not a recognised world.");
            sender.sendMessage(ChatColor.RED + "Hint: your first world's name is \"" + ctp.getServer().getWorlds().get(0).getName() + "\".");
            return;
        }

        ctp.chooseSuitableArena(world.getPlayers().size()); // Choose a suitable arena based on the number of players in the world.
        
        if (ctp.isGameRunning()) {
            sender.sendMessage("[CTP] A previous Capture The Points game has been terminated.");
            ctp.blockListener.endGame(true);
        }

        for (Player p : world.getPlayers()) {
            ctp.moveToLobby(p);
        }

        return;
    }
}