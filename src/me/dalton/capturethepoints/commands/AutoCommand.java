package me.dalton.capturethepoints.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import me.dalton.capturethepoints.ArenaData;
import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class AutoCommand extends CTPCommand {
    /** Kj -- This command will bring all players on a world into a random lobby which is guaranteed to hold everyone (if not, use the already selected arena) */
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
            player.sendMessage(ChatColor.RED + "Please create an arena first!");
            return;
        }
        if (ctp.mainArena.lobby == null) {
            player.sendMessage(ChatColor.RED + "Please create arena lobby!");
            return;
        }

        World world = ctp.getServer().getWorld(parameters.get(2));
        if (world == null) {
            player.sendMessage(ChatColor.RED + parameters.get(2) + " is not a recognised world.");
            player.sendMessage(ChatColor.RED + "Hint: your first world's name is \"" + ctp.getServer().getWorlds().get(0).getName()+"\".");
            return;
        }
        int numberofplayers = world.getPlayers().size();

        if (!ctp.configOptions.useSelectedArenaOnly) {
            int size = ctp.arena_list.size();

            if (size > 1) {
                // If there is more than 1 arena to choose from
                List<String> arenas = new ArrayList<String>();
                for (String arena : ctp.arena_list) {
                    ArenaData loadArena = ctp.loadArena(arena);
                    if (loadArena.maximumPlayers >= numberofplayers && loadArena.minimumPlayers <= numberofplayers) {
                        arenas.add(arena);
                        ctp.mainArena = loadArena; // Change the mainArena based on this.
                    }
                }


                if (arenas.size() > 1) {
                    Random random = new Random();
                    int nextInt = random.nextInt(size); // Generate a random number between 0 (inclusive) -> Number of arenas (exclusive)
                    ctp.mainArena = ctp.loadArena(ctp.arena_list.get(nextInt)) == null ? ctp.mainArena : ctp.loadArena(ctp.arena_list.get(nextInt)); // Change the mainArena based on this. (Ternary null check)
                }
            // else ctp.mainArena = ctp.mainArena;
            }
        }
        if (ctp.isGameRunning()) {
            player.sendMessage("[CTP] A previous Capture The Points game has been terminated.");
            ctp.blockListener.endGame(true);
        }

        for (Player p : world.getPlayers()) {
            ctp.moveToLobby(p);
        }

        return;
    }
}