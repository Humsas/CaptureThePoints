package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;

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
        CaptureThePoints.logger.info("CTP Game running: "+ctp.isGameRunning());
        String checkMainArena = ctp.checkMainArena(player); // Kj -- Check arena, if there is an error, an error message is returned.
        if (!checkMainArena.isEmpty()) {
            CaptureThePoints.logger.info("CTP Main Arena errors: "+checkMainArena);
            CaptureThePoints.logger.info("-----------========== ######### ==========-----------");
            return;
        } else {
            CaptureThePoints.logger.info("CTP Main Arena is playable.");
        }
        CaptureThePoints.logger.info("CTP Current Arena: \""+ctp.mainArena.name+"\" in World \""+ctp.mainArena.world+"\"");   
        CaptureThePoints.logger.info("CTP Number of Arenas: "+ctp.arena_list.size()+": "+ctp.arena_list);   
        if (ctp.mainArena.hasLobby()) {
            CaptureThePoints.logger.info("CTP Arena Lobby: "+(int)ctp.mainArena.lobby.x+", "+(int)ctp.mainArena.lobby.y+", "+(int)ctp.mainArena.lobby.z+".");
        } else {
            CaptureThePoints.logger.info("CTP Arena Lobby: not made");
        }
        CaptureThePoints.logger.info("CTP Number of capture points: "+ctp.mainArena.capturePoints.size());
        CaptureThePoints.logger.info("CTP Number of teams: "+ctp.mainArena.teamSpawns.size());
        CaptureThePoints.logger.info("CTP Minimum Players for this arena: "+ctp.mainArena.minimumPlayers);
        CaptureThePoints.logger.info("CTP Maxmimum Players for this arena: "+ctp.mainArena.maximumPlayers);
        CaptureThePoints.logger.info("CTP Players ready in the lobby: "+ctp.mainArena.lobby.countReadyPeople()+"/"+ctp.mainArena.lobby.getAmountOfPlayersInLobby());
        CaptureThePoints.logger.info("-----------========== ######### ==========-----------");
    }
}