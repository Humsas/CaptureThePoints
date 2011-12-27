package me.dalton.capturethepoints.commands;

import java.util.ArrayList;
import java.util.List;
import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.ConfigOptions;
import me.dalton.capturethepoints.Team;
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
        super.maxParameters = 3;
        super.usageTemplate = "/ctp debug [1|2]";
    }

    @Override
    public void perform() {
        int size = parameters.size();
        String pagenumber = size > 2 ? parameters.get(2) : "";
        if (pagenumber.equalsIgnoreCase("2")) {
            sender.sendMessage("Outputting CTP info (2) to Console.");
            CaptureThePoints.logger.info("-----------========== CTP DEBUG ==========-----------");
            String checkMainArena = ctp.checkMainArena(player, ctp.mainArena); // Kj -- Check arena, if there is an error, an error message is returned.
            if (!checkMainArena.isEmpty()) {
                CaptureThePoints.logger.info("Main Arena errors: " + checkMainArena);
                CaptureThePoints.logger.info("-----------========== ######### ==========-----------");
                return;
            } else {
                CaptureThePoints.logger.info(ctp.mainArena.name+"'s Config Options:");
                ConfigOptions co = ctp.mainArena.co;
                CaptureThePoints.logger.info("   PointsToWin: " + co.pointsToWin);
                CaptureThePoints.logger.info("   PlayTime: " + co.playTime);
                CaptureThePoints.logger.info("   UseScoreGeneration: " + co.useScoreGeneration);
                CaptureThePoints.logger.info("   ScoreToWin: " + co.scoreToWin);
                CaptureThePoints.logger.info("   OnePointGeneratedScoreEvery30sec: " + co.onePointGeneratedScoreEvery30sec);
                CaptureThePoints.logger.info("   ScoreAnnounceTime: " + co.scoreAnnounceTime);
                CaptureThePoints.logger.info("   AllowBlockBreak: " + co.allowBlockBreak);
                CaptureThePoints.logger.info("   AllowBlockPlacement: " + co.allowBlockPlacement);
                CaptureThePoints.logger.info("   AllowCommands: " + co.allowCommands);
                CaptureThePoints.logger.info("   AllowDropItems: " + co.allowDropItems);
                CaptureThePoints.logger.info("   AllowLateJoin: " + co.allowLateJoin);
                CaptureThePoints.logger.info("   AutoStart: " + co.autoStart);
                CaptureThePoints.logger.info("   BreakingBlocksDropsItems: " + co.breakingBlocksDropsItems);
                CaptureThePoints.logger.info("   DamageImmunityNearSpawnDistance: " + co.protectionDistance);
                CaptureThePoints.logger.info("   DropWoolOnDeath: " + co.dropWoolOnDeath);
                CaptureThePoints.logger.info("   ExactTeamMemberCount: " + co.exactTeamMemberCount);
                CaptureThePoints.logger.info("   GiveNewRoleItemsOnRespawn: " + co.giveNewRoleItemsOnRespawn);
                CaptureThePoints.logger.info("   GivenWoolNumber: " + co.givenWoolNumber);
                CaptureThePoints.logger.info("   LobbyKickTime: " + co.lobbyKickTime);
                CaptureThePoints.logger.info("   MaxPlayerHealth: " + co.maxPlayerHealth);
                CaptureThePoints.logger.info("   MoneyAtTheLobby: " + co.moneyAtTheLobby);
                CaptureThePoints.logger.info("   MoneyEvery30sec: " + co.moneyEvery30Sec);
                CaptureThePoints.logger.info("   MoneyForKill: " + co.moneyForKill);
                CaptureThePoints.logger.info("   MoneyForPointCapture: " + co.moneyForPointCapture);
                CaptureThePoints.logger.info("   RingBlock: " + co.ringBlock);
                CaptureThePoints.logger.info("   UseSelectedArenaOnly: " + co.useSelectedArenaOnly);
            }
            CaptureThePoints.logger.info("-----------========== ######### ==========-----------");
            return;
        }
        sender.sendMessage("Outputting CTP info (1) to Console.");
        CaptureThePoints.logger.info("-----------========== CTP DEBUG ==========-----------");
        CaptureThePoints.logger.info("Game running: "+ctp.isGameRunning());
        String checkMainArena = ctp.checkMainArena(player, ctp.mainArena); // Kj -- Check arena, if there is an error, an error message is returned.
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
            result.add("No suitable arena for the number of people playing: "+ctp.mainArena.getPlayersPlaying(ctp).size());
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
        
        for (Team aTeam : ctp.mainArena.teams) {
            boolean insane = aTeam.sanityCheck(ctp);
            if (insane) {
                int players = aTeam.getTeamPlayers(ctp) == null ? 0 : aTeam.getTeamPlayers(ctp).size(); 
                result.add("Team "+aTeam.color+" has incorrect memberCount. It is different to TeamPlayers size: ["+players+" | "+aTeam.memberCount+"]");
            }
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
        
        result.clear();
        
        CaptureThePoints.logger.info("Number of Arenas: "+ctp.arena_list.size()+": "+ctp.arena_list);   
        CaptureThePoints.logger.info("Current Arena: \""+ctp.mainArena.name+"\" in World \""+ctp.mainArena.world+"\"");
        if (ctp.mainArena.hasLobby()) {
            CaptureThePoints.logger.info("    Lobby: "+(int)ctp.mainArena.lobby.x+ ", "+(int)ctp.mainArena.lobby.y+ ", "+(int)ctp.mainArena.lobby.z+".");
        } else {
            CaptureThePoints.logger.info("    Lobby: not made");
        }
        CaptureThePoints.logger.info("    Number of capture points: "+ctp.mainArena.capturePoints.size());
        CaptureThePoints.logger.info("    Number of teams: "+ctp.mainArena.teamSpawns.size());
        CaptureThePoints.logger.info("    Minimum Players for this arena: "+ctp.mainArena.minimumPlayers);
        CaptureThePoints.logger.info("    Maxmimum Players for this arena: "+ctp.mainArena.maximumPlayers);
        CaptureThePoints.logger.info("    Players ready in the lobby: "+ctp.mainArena.lobby.countReadyPeople()+"/"+ctp.mainArena.lobby.countAllPeople());
        CaptureThePoints.logger.info(ctp.roles.size() + " Roles found: "+ctp.roles.keySet().toString());
        
        int running = 0, total = 0;
        if (ctp.CTP_Scheduler.healingItemsCooldowns != 0) {
            running++; total++;
            result.add("Item Cooldowns");
        } else {
            total++;
        }
        if (ctp.CTP_Scheduler.helmChecker != 0) {
            running++; total++;
            result.add("Helmet Checker");
        } else {
            total++;
        }
        if (ctp.CTP_Scheduler.lobbyActivity != 0) {
            running++; total++;
            result.add("Lobby Activity");
        } else {
            total++;
        }
        if (ctp.CTP_Scheduler.money_Score != 0) {
            running++; total++;
            result.add("Money Adder");
        } else {
            total++;
        }
        if (ctp.CTP_Scheduler.playTimer != 0) {
            running++; total++;
            result.add("Play Timer");
        } else {
            total++;
        }
        if (ctp.CTP_Scheduler.pointMessenger != 0) {
            running++; total++;
            result.add("Points Messenger");
        } else {
            total++;
        }
        
        CaptureThePoints.logger.info(running+"/"+total+" Schedulers running: ");
        for (String schedule : result) {
            CaptureThePoints.logger.info("    "+schedule);
        }

        CaptureThePoints.logger.info("End of page 1. To view page 2 (Main Arena Config Options), type /ctp debug 2"); 
        CaptureThePoints.logger.info("-----------========== ######### ==========-----------");
    }
}