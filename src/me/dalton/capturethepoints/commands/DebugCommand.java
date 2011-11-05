package me.dalton.capturethepoints.commands;

import java.util.ArrayList;
import java.util.List;
import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.entity.Player;

public class DebugCommand extends CTPCommand {
   
    /** Output states to Console. */
    public DebugCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("debug");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.play", "ctp.admin", "ctp.debug"};
        super.senderMustBePlayer = false;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp debug";
    }

    @Override
    public void perform() {
        sender.sendMessage("Outputting CTP info to Console.");
        CaptureThePoints.logger.info("-----------========== CTP DEBUG ==========-----------");
        CaptureThePoints.logger.info("Game running: "+ctp.isGameRunning());
        String checkMainArena = ctp.checkMainArena(player); // Kj -- Check arena, if there is an error, an error message is returned.
        if (!checkMainArena.isEmpty()) {
            CaptureThePoints.logger.info("Main Arena errors: "+checkMainArena);
            CaptureThePoints.logger.info("-----------========== ######### ==========-----------");
            return;
        } else {
            CaptureThePoints.logger.info("Main Arena is playable.");
        }
        CaptureThePoints.logger.info("Running sanity checks ... ");
        
        List<String> result = new ArrayList<String>();
        if (ctp.mainArena.getPlayers(ctp).size() != ctp.playerData.size() || ctp.playerData.size() != (ctp.mainArena.lobby.playersinlobby.size() + ctp.mainArena.getPlayersPlaying(ctp).size())) {
            result.add("Inconsistant number of Players: ["+ctp.mainArena.getPlayersPlaying(ctp).size()+" | "+ctp.playerData.size()+" | "+(ctp.mainArena.lobby.countAllPeople() + ctp.mainArena.getPlayersPlaying(ctp).size())+"]");
        }
        if (!ctp.hasSuitableArena(ctp.mainArena.getPlayersPlaying(ctp).size()) && ctp.isGameRunning()) {
            result.add("No suitable arena for the number of people playing: "+ctp.mainArena.getPlayersPlaying(ctp).size()+" (or config is set to useSelectedArenaOnly)");
        }
        boolean error = false;
        for (Player p : ctp.playerData.keySet()) {
            if (p == null) {
                result.add("There is a null player in the playerData.");
                continue;
            }
            boolean isReady = false;
            if (ctp.mainArena.lobby.playersinlobby.get(p) == null) {
                isReady = true;
            } else {
                isReady = ctp.mainArena.lobby.playersinlobby.get(p);
            }
            if (ctp.playerData.get(p).isReady != isReady) {
                error = true; // Needs to be separate otherwise for loop will spam.
            }
        }
        if (error) {
            result.add("There is a discrepancy between playerData ready and the player's ready status in the lobby.");
        }
        
        if (ctp.mainArena.minimumPlayers > ctp.mainArena.maximumPlayers) {
            result.add("Minimum players greater than maximum players! ["+ctp.mainArena.minimumPlayers+" > "+ctp.mainArena.maximumPlayers+"]");
        }
        
        if (result.isEmpty()) {
            CaptureThePoints.logger.info("    Passed.");
        } else {
            for (String anError : result) {
                CaptureThePoints.logger.info("    "+anError);
            }
        }
        
        CaptureThePoints.logger.info("Number of Arenas: "+ctp.arena_list.size()+": "+ctp.arena_list);   
        CaptureThePoints.logger.info("Current Arena: \""+ctp.mainArena.name+"\" in World \""+ctp.mainArena.world+"\"");
        if (ctp.mainArena.hasLobby()) {
            CaptureThePoints.logger.info("    Lobby: "+(int)ctp.mainArena.lobby.x+", "+(int)ctp.mainArena.lobby.y+", "+(int)ctp.mainArena.lobby.z+".");
        } else {
            CaptureThePoints.logger.info("    Lobby: not made");
        }
        CaptureThePoints.logger.info("    Number of capture points: "+ctp.mainArena.capturePoints.size());
        CaptureThePoints.logger.info("    Number of teams: "+ctp.mainArena.teamSpawns.size());
        CaptureThePoints.logger.info("    Minimum Players for this arena: "+ctp.mainArena.minimumPlayers);
        CaptureThePoints.logger.info("    Maxmimum Players for this arena: "+ctp.mainArena.maximumPlayers);
        CaptureThePoints.logger.info("    Players ready in the lobby: "+ctp.mainArena.lobby.countReadyPeople()+"/"+ctp.mainArena.lobby.countAllPeople());
        CaptureThePoints.logger.info(ctp.roles.size() + " Roles found: "+ctp.roles.keySet().toString());   
        CaptureThePoints.logger.info("-----------========== ######### ==========-----------");
    }
}