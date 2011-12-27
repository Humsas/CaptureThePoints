package me.dalton.capturethepoints.listeners;
import java.util.ArrayList;
import java.util.List;
import me.dalton.capturethepoints.ArenaBoundaries;
import me.dalton.capturethepoints.CTPPoints;
import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.HealingItems;
import me.dalton.capturethepoints.Items;
import me.dalton.capturethepoints.Team;
import me.dalton.capturethepoints.Util;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

public class CaptureThePointsBlockListener extends BlockListener {
    private final CaptureThePoints ctp;

    public boolean capturegame = false;

    public boolean preGame = true;

    public CaptureThePointsBlockListener (CaptureThePoints ctp) {
        this.ctp = ctp;
    }

    @Override
    public void onBlockBreak (BlockBreakEvent event)
    {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // If it tries to break in lobby
        if (ctp.playerData.containsKey(player) && ctp.playerData.get(player).isInLobby) {
            // breaks block beneath player(it causes teleport event if you cancel action)
            int playerLocX = player.getLocation().getBlockX();
            int playerLocY = player.getLocation().getBlockY() - 1;
            int playerLocZ = player.getLocation().getBlockZ();

            if (playerLocX == block.getX() && playerLocY == block.getY() && playerLocZ == block.getZ()) {
                // allow teleport
                ctp.playerData.get(player).justJoined = true;
                ctp.playerNameForTeleport = player.getName();

                // player can not drop down so we need to reset teleport flag
                ctp.getServer().getScheduler().scheduleSyncDelayedTask(ctp, new Runnable() {
                    @Override
                    public void run ()
                    {
                        if (!ctp.playerNameForTeleport.isEmpty())
                        {
                            ctp.playerData.get(ctp.getServer().getPlayer(ctp.playerNameForTeleport)).justJoined = false;
                            ctp.playerNameForTeleport = "";
                        }
                    }

                }, 5L);  //I think one second is too much and can cause some troubles if player break another block
            }
            event.setCancelled(true);
            return;
        }

        
        if (!ctp.playerData.containsKey(player)) // If tries to break arena blocks out of game
        {
            for(ArenaBoundaries bound : ctp.arenasBoundaries.values())
            {
                if (ctp.playerListener.isInside(block.getLocation().getBlockX(), bound.x1, bound.x2) && ctp.playerListener.isInside(block.getLocation().getBlockY(), bound.y1, bound.y2) && ctp.playerListener.isInside(block.getLocation().getBlockZ(), bound.z1, bound.z2) && block.getLocation().getWorld().getName().equalsIgnoreCase(bound.world))
                {
                    if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.canModify"}))
                    {
                        return; // Player can edit arena
                    }

                    player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }

        if (!ctp.isGameRunning())
        {
            return;
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (isAlreadyInGame(player)) {

            // Kj -- helmet checker
            /*boolean helmetRemoved = ctp.playerListener.checkHelmet(player);
            if (helmetRemoved) {
            ctp.playerListener.fixHelmet(player);
            event.setCancelled(true);
            return;
            }*/

            // check for sign destroy
            if (state instanceof Sign) {
                event.setCancelled(true);
                return;
            }
            boolean inPoint = false; // Kj -- for block breaking checker

            //in game wool check
            if (data instanceof Wool) {
                Location loc = block.getLocation();

                for (CTPPoints point : ctp.mainArena.capturePoints) { // Kj -- s -> point
                    Location pointLocation = new Location(player.getWorld(), point.x, point.y, point.z);
                    double distance = pointLocation.distance(loc);
                    if (distance < 5.0D) 
                    {
                        // Check if player team can capture point
                        if(point.notAllowedToCaptureTeams != null && point.notAllowedToCaptureTeams.contains(ctp.playerData.get(player).team.color.toLowerCase()))
                        {
                            player.sendMessage("[CTP]" + ChatColor.RED + " Your team can't capture this point.");
                            event.setCancelled(true);
                            return;
                        }

                        inPoint = true; // Kj -- for block breaking checker
                        if (point.pointDirection == null) {
                            if (checkForFill(point, loc, ctp.playerData.get(player).team.color, ((Wool) data).getColor().toString(), true)) {
                                if (ctp.playerData.get(player).team.color.equalsIgnoreCase(((Wool) data).getColor().toString())) {
                                    event.setCancelled(true);
                                    return;
                                }
                                if (point.controledByTeam != null) {
                                    point.controledByTeam = null;
                                    Util.sendMessageToPlayers(ctp, subtractPoints(((Wool) data).getColor().toString(), point.name));
                                    break;
                                }
                            }
                        } else {
                            if (checkForFillVert(point, loc, ctp.playerData.get(player).team.color, ((Wool) data).getColor().toString(), true)) {
                                if (ctp.playerData.get(player).team.color.equalsIgnoreCase(((Wool) data).getColor().toString())) {
                                    event.setCancelled(true);
                                    return;
                                }
                                if (point.controledByTeam != null) {
                                    point.controledByTeam = null;
                                    Util.sendMessageToPlayers(ctp, subtractPoints(((Wool) data).getColor().toString(), point.name));
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // Kj -- block breaking checker blocks breaking of anything not in the CTPPoint if the config has set AllowBlockBreak to false.
            if (!ctp.mainArena.co.allowBlockPlacement && !inPoint) {
                event.setCancelled(true);
                return;
            }
            if(!ctp.globalConfigOptions.enableHardArenaRestore)
            {
                ctp.arenaRestore.addBlock(block, false);
            }


            /* Kj -- this checks to see if the event was cancelled. If it wasn't, then it's a legit block break.
             * If the config option is set to no items on block break, then cancel the event and set the block
             * to air instead. That way, it does not drop items. */
            if (!ctp.mainArena.co.breakingBlocksDropsItems) {
                if (!event.isCancelled()) {
                    event.setCancelled(true);
                    block.setType(Material.AIR);
                }
                return;
            }
        }
    }

    @Override
    public void onBlockPlace (BlockPlaceEvent event) 
    {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // If it tries to place in lobby
        if (ctp.playerData.containsKey(player) && ctp.playerData.get(player).isInLobby) {
            event.setCancelled(true);
            return;
        }

        if (!ctp.playerData.containsKey(player)) // If tries to place blocks in arena out of game
        {
            for(ArenaBoundaries bound : ctp.arenasBoundaries.values())
            {
                if (ctp.playerListener.isInside(block.getLocation().getBlockX(), bound.x1, bound.x2) && ctp.playerListener.isInside(block.getLocation().getBlockY(), bound.y1, bound.y2) && ctp.playerListener.isInside(block.getLocation().getBlockZ(), bound.z1, bound.z2) && block.getLocation().getWorld().getName().equalsIgnoreCase(bound.world))
                {
                    if (ctp.canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.canModify"}))
                    {
                        return; // Player can edit arena
                    }

                    player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }

        if (!ctp.isGameRunning())
        {
            return;
        }
        
        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (isAlreadyInGame(player)) {

            // Kj -- helmet checker
           /* boolean helmetRemoved = ctp.playerListener.checkHelmet(player);
            if (helmetRemoved) {
            ctp.playerListener.fixHelmet(player);
            block.setType(Material.AIR);
            return;
            }
             */
            boolean inPoint = false; // Kj -- for block placement checker
            if ((data instanceof Wool)) {
                Location loc = block.getLocation();

                for (CTPPoints point : ctp.mainArena.capturePoints)
                {
                    Location pointLocation = new Location(player.getWorld(), point.x, point.y, point.z);
                    double distance = pointLocation.distance(loc);
                    if (distance < 5)  // Found nearest point ( points can't be closer than 5 blocks)
                    {
                        // Check if player team can capture point
                        if(point.notAllowedToCaptureTeams != null && point.notAllowedToCaptureTeams.contains(ctp.playerData.get(player).team.color.toLowerCase()))
                        {
                            player.sendMessage("[CTP]" + ChatColor.RED + " Your team can't capture this point.");
                            event.setCancelled(true);
                            return;
                        }
                        
                        inPoint = true; // Kj -- for block placement checker

                        // If building near the point with not your own colored wool(to prevent wool destry bug)
                        if (!ctp.playerData.get(player).team.color.equalsIgnoreCase(((Wool) data).getColor().toString())) {
                            event.setCancelled(true);
                            return;
                        }
                        if (point.pointDirection == null) {
                            //Check if wool is placed on top of point
                            if (checkForWoolOnTopHorizontal(loc, point)) {
                                event.setCancelled(true);
                                return;
                            }
                            if (checkForFill(point, loc, ctp.playerData.get(player).team.color, ((Wool) data).getColor().toString(), false)) {
                                if (point.controledByTeam == null) {
                                    point.controledByTeam = ctp.playerData.get(player).team.color;
                                    Util.sendMessageToPlayers(ctp, addPoints(((Wool) data).getColor().toString(), point.name));
                                    ctp.playerData.get(player).pointCaptures++;
                                    ctp.playerData.get(player).money += ctp.mainArena.co.moneyForPointCapture;
                                    player.sendMessage("Money: " + ChatColor.GREEN + ctp.playerData.get(player).money);
                                    if (didSomeoneWin()) {
                                        loc.getBlock().setTypeId(0);
                                    }
                                    break;
                                }
                            }
                        } else {
                            //Check if wool is placed on top of point
                            if (checkForWoolOnTopVertical(loc, point)) {
                                event.setCancelled(true);
                                return;
                            }
                            if (checkForFillVert(point, loc, ctp.playerData.get(player).team.color, ((Wool) data).getColor().toString(), false)) {
                                if (point.controledByTeam == null) {
                                    point.controledByTeam = ctp.playerData.get(player).team.color;
                                    Util.sendMessageToPlayers(ctp, addPoints(((Wool) data).getColor().toString(), point.name));
                                    ctp.playerData.get(player).pointCaptures++;
                                    ctp.playerData.get(player).money += ctp.mainArena.co.moneyForPointCapture;
                                    player.sendMessage("Money: " + ChatColor.GREEN + ctp.playerData.get(player).money);
                                    if (didSomeoneWin()) {
                                        loc.getBlock().setTypeId(0);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            // Kj -- block placement checker blocks placement of anything not in the CTPPoint if the config has set AllowBlockPlacement to false.
            if (!ctp.mainArena.co.allowBlockPlacement && !inPoint) {
                event.setCancelled(true);
                return;
            }
            if(!ctp.globalConfigOptions.enableHardArenaRestore)
            {
                ctp.arenaRestore.addBlock(block, false);
            }
        }
    }

    @Override
    public void onSignChange (SignChangeEvent event) {
        if (!ctp.isGameRunning()) {
            return;
        }
        if (isAlreadyInGame(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.RED + "Cannot break sign whilst playing.");
            event.setCancelled(true);
            return;
        }
    }

    public String addPoints (String aTeam, String gainedpoint) { // Kj -- remade.
        if (this.capturegame) {
            for (Team team : ctp.mainArena.teams) {
                if (team.color.equalsIgnoreCase(aTeam)) {
                    team.controledPoints++;
                    if (!ctp.mainArena.co.useScoreGeneration) {
                        return team.chatcolor + aTeam.toUpperCase() + ChatColor.WHITE + " captured " + ChatColor.GOLD + gainedpoint + ChatColor.WHITE + ". (" + team.controledPoints + "/" + ctp.mainArena.co.pointsToWin + " points).";
                    } else {
                        return team.chatcolor + aTeam.toUpperCase() + ChatColor.WHITE + " captured " + ChatColor.GOLD + gainedpoint + ChatColor.WHITE + ". (" + team.controledPoints + "/" + ctp.mainArena.capturePoints.size() + " points).";
                    }
                }
            }
            return null;
        }
        return null;
    }

    public void assignRole (Player p, String role) {
        p.setHealth(20);
        PlayerInventory inv = p.getInventory();
        inv.clear();
        inv.setHelmet(null);
        inv.setChestplate(null);
        inv.setLeggings(null);
        inv.setBoots(null);
        p.updateInventory();

        ctp.playerData.get(p).role = role;

        for (Items item : ctp.roles.get(role.toLowerCase())) {
            if (Util.ARMORS_TYPE.contains(item.item) && (!Util.HELMETS_TYPE.contains(item.item))) {
                ItemStack i = new ItemStack(item.item, 1);
                Util.equipArmorPiece(i, inv);
            } else {
                byte offset = 0;
                if (item.item == Material.INK_SACK) {
                    offset = 15;
                }
                DyeColor dye = DyeColor.getByData((byte) Math.abs(offset - item.type));
                ItemStack stack;
                // If something is wrong in config file
                try
                {
                    stack = new ItemStack(item.item, item.amount, (short) (byte) Math.abs(offset - dye.getData()));
                }
                catch(Exception e)
                {
                    System.out.println("[CTP] There is error in your config file, with roles. Please check them!");
                    return;
                }
                inv.addItem(stack);
            }
        }
        p.updateInventory();
    }

    private boolean checkForColor (String color, Location loc1, Location loc2, Location loc3) {
        DyeColor color1 = itsColor(loc1.getBlock());
        DyeColor color2 = itsColor(loc2.getBlock());
        DyeColor color3 = itsColor(loc3.getBlock());
        if (color1 == null || color2 == null || color3 == null) {
            return false;
        }
        return color1.toString().equalsIgnoreCase(color) && color2.toString().equalsIgnoreCase(color) && color3.toString().equalsIgnoreCase(color);
    }

    private boolean checkForFill (CTPPoints point, Location loc, String color, String placedWoolColor, boolean onBlockBreak) {
        //If player is placing not his own wool
        if ((!onBlockBreak) && (!placedWoolColor.equalsIgnoreCase(color))) {
            return false;
        }
        if (isInsidePoint(point, loc)) {
            Location loc1 = new Location(loc.getWorld(), point.x, point.y, point.z);
            Location loc2 = new Location(loc.getWorld(), point.x + 1, point.y, point.z);
            Location loc3 = new Location(loc.getWorld(), point.x + 1, point.y, point.z + 1);
            Location loc4 = new Location(loc.getWorld(), point.x, point.y, point.z + 1);
            if (loc.equals(loc1)) {
                return checkForColor(placedWoolColor, loc2, loc3, loc4);
            } else if (loc.equals(loc2)) {
                return checkForColor(placedWoolColor, loc1, loc3, loc4);
            } else if (loc.equals(loc3)) {
                return checkForColor(placedWoolColor, loc1, loc2, loc4);
            } else if (loc.equals(loc4)) {
                return checkForColor(placedWoolColor, loc1, loc2, loc3);
            }
        }
        return false;
    }

    private boolean checkForFillVert (CTPPoints point, Location loc, String color, String placedWoolColor, boolean onBlockBreak) {
        //If player is placing not his own wool
        if ((!onBlockBreak) && (!placedWoolColor.equalsIgnoreCase(color))) {
            return false;
        }

        if (isInsidePointVert(point, loc)) {
            Location loc1 = new Location(loc.getWorld(), 0, 0, 0);
            Location loc2 = new Location(loc.getWorld(), 0, 0, 0);
            Location loc3 = new Location(loc.getWorld(), 0, 0, 0);
            Location loc4 = new Location(loc.getWorld(), 0, 0, 0);

            if (point.pointDirection.equals("NORTH") || point.pointDirection.equals("SOUTH")) {
                loc1 = new Location(loc.getWorld(), point.x, point.y, point.z);
                loc2 = new Location(loc.getWorld(), point.x, point.y + 1, point.z);
                loc3 = new Location(loc.getWorld(), point.x, point.y, point.z + 1);
                loc4 = new Location(loc.getWorld(), point.x, point.y + 1, point.z + 1);
            } else if (point.pointDirection.equals("WEST") || point.pointDirection.equals("EAST")) {
                loc1 = new Location(loc.getWorld(), point.x, point.y, point.z);
                loc2 = new Location(loc.getWorld(), point.x, point.y + 1, point.z);
                loc3 = new Location(loc.getWorld(), point.x + 1, point.y, point.z);
                loc4 = new Location(loc.getWorld(), point.x + 1, point.y + 1, point.z);
            }

            // This way because wool block is not placed yet
            if (loc.equals(loc1)) {
                return checkForColor(placedWoolColor, loc2, loc3, loc4);
            } else if (loc.equals(loc2)) {
                return checkForColor(placedWoolColor, loc1, loc3, loc4);
            } else if (loc.equals(loc3)) {
                return checkForColor(placedWoolColor, loc1, loc2, loc4);
            } else if (loc.equals(loc4)) {
                return checkForColor(placedWoolColor, loc1, loc2, loc3);
            }
        }
        return false;
    }

    private boolean checkForWoolOnTopHorizontal (Location loc, CTPPoints s) {
        for (int x = (int) s.x + 2; x >= s.x - 1; x--) {
            for (int y = (int) s.y + 1; y <= s.y + 2; y++) {
                for (int z = (int) s.z - 1; z <= s.z + 2; z++) {
                    if ((loc.getBlockX() == x) && (loc.getBlockY() == y) && (loc.getBlockZ() == z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkForWoolOnTopVertical (Location loc, CTPPoints point) {
        if (point.pointDirection.equals("NORTH")) {
            if (loc.getX() >= point.x - 2 && loc.getX() < point.x) {
                if (loc.getY() >= point.y - 1 && loc.getY() < point.y + 3) {
                    if (loc.getZ() >= point.z - 1 && loc.getZ() < point.z + 3) {
                        return true;
                    }
                }
            }
        } else if (point.pointDirection.equals("EAST")) {
            if (loc.getX() >= point.x - 1 && loc.getX() < point.x + 3) {
                if (loc.getY() >= point.y - 1 && loc.getY() < point.y + 3) {
                    if (loc.getZ() >= point.z - 2 && loc.getZ() < point.z) {
                        return true;
                    }
                }
            }
        } else if (point.pointDirection.equals("SOUTH")) {
            if (loc.getX() >= point.x + 1 && loc.getX() < point.x + 3) {
                if (loc.getY() >= point.y - 1 && loc.getY() < point.y + 3) {
                    if (loc.getZ() >= point.z - 1 && loc.getZ() < point.z + 3) {
                        return true;
                    }
                }
            }
        } else if (point.pointDirection.equals("WEST")) {
            if (loc.getX() >= point.x - 1 && loc.getX() < point.x + 3) {
                if (loc.getY() >= point.y - 1 && loc.getY() < point.y + 3) {
                    if (loc.getZ() >= point.z + 1 && loc.getZ() < point.z + 3) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean didSomeoneWin () {
        List<Team> winningteams = new ArrayList<Team>();
        String WinMessage = "";
        if (ctp.mainArena.co.useScoreGeneration) {
            for (Team team : ctp.mainArena.teams) {
                if (team.score >= ctp.mainArena.co.scoreToWin) {
                    winningteams.add(team);
                    WinMessage = team.chatcolor + team.color.toUpperCase() + ChatColor.WHITE + " wins!";
                }
            }
        } else {
            for (Team team : ctp.mainArena.teams) {
                if (team.controledPoints >= ctp.mainArena.co.pointsToWin) {
                    winningteams.add(team);
                    WinMessage = team.chatcolor + team.color.toUpperCase() + ChatColor.WHITE + " wins!";
                }
            }
        }

        if (winningteams.isEmpty()) {
            return false;
        } else if (winningteams.size() > 1) {
            if (ctp.mainArena.co.useScoreGeneration) {
                WinMessage = "It's a tie! " + winningteams.size() + " teams have passed " + ctp.mainArena.co.pointsToWin + " points!";
            } else {
                WinMessage = "It's a tie! " + winningteams.size() + " teams have a score of " + ctp.mainArena.co.scoreToWin + "!";
            }
        }

        for (Team team : winningteams) {
            for (Player player : ctp.playerData.keySet()) {
                if ((ctp.playerData.get(player).isInArena) && (ctp.playerData.get(player).team == team)) {
                    ctp.playerData.get(player).winner = true;
                }
            }
        }

        Util.sendMessageToPlayers(ctp, WinMessage);
        String message = "";
        if (ctp.mainArena.co.useScoreGeneration) {
            for (Team aTeam : ctp.mainArena.teams) {
                message = message + aTeam.chatcolor + aTeam.color.toUpperCase() + ChatColor.WHITE + " final score: " + aTeam.score + ChatColor.AQUA + " // ";
            }
        } else {
            for (Team aTeam : ctp.mainArena.teams) {
                message = message + aTeam.chatcolor + aTeam.color.toUpperCase() + ChatColor.WHITE + " final points: " + aTeam.controledPoints + ChatColor.AQUA + " // ";
            }
        }

        Util.sendMessageToPlayers(ctp, message);
        endGame(false);

        return true;
    }

    public void endGame (boolean noRewards) {
        Util.sendMessageToPlayers(ctp, "A Capture The Points game has ended!");

        // Task canceling
        if (ctp.CTP_Scheduler.playTimer != 0) {
            ctp.getServer().getScheduler().cancelTask(ctp.CTP_Scheduler.playTimer);
            ctp.CTP_Scheduler.playTimer = 0;
        }
        if (ctp.CTP_Scheduler.money_Score != 0) {
            ctp.getServer().getScheduler().cancelTask(ctp.CTP_Scheduler.money_Score);
            ctp.CTP_Scheduler.money_Score = 0;
        }
        if (ctp.CTP_Scheduler.pointMessenger != 0) {
            ctp.getServer().getScheduler().cancelTask(ctp.CTP_Scheduler.pointMessenger);
            ctp.CTP_Scheduler.pointMessenger = 0;
        }
        if (ctp.CTP_Scheduler.helmChecker != 0) {
            ctp.getServer().getScheduler().cancelTask(ctp.CTP_Scheduler.helmChecker);
            ctp.CTP_Scheduler.helmChecker = 0;
        }
        if (ctp.CTP_Scheduler.healingItemsCooldowns != 0) {
            ctp.getServer().getScheduler().cancelTask(ctp.CTP_Scheduler.healingItemsCooldowns);
            ctp.CTP_Scheduler.healingItemsCooldowns = 0;
        }

        for (CTPPoints s : ctp.mainArena.capturePoints) {
            s.controledByTeam = null;
        }

        this.preGame = true;
        this.capturegame = false;

        for (Player player : this.ctp.playerData.keySet())
        {
            restoreThings(player);
            if (!noRewards) {
                Util.rewardPlayer(ctp, player);
            }
        }
        //Arena restore
        if(ctp.globalConfigOptions.enableHardArenaRestore)
        {
            ctp.arenaRestore.restoreMySQLBlocks();
//            ctp.getServer().getScheduler().scheduleAsyncDelayedTask(ctp, new Runnable()
//            {
//                @Override
//                public void run ()
//                {
//                    //ctp.mysqlConnector.connectToMySql();
//                    //ctp.arenaRestore.restore(ctp.mainArena.name);
//                }
//            }, 5L);  //I think one second is too much and can cause some troubles if player break another block
        }
        else
        {
            ctp.arenaRestore.restoreAllBlocks();
        }

        for (HealingItems item : ctp.healingItems) {
            if (!item.cooldowns.isEmpty()) {
                item.cooldowns.clear();
            }
        }
        this.ctp.mainArena.lobby.clearLobbyPlayerData();
        this.ctp.previousLocation.clear();
        this.ctp.playerData.clear();
        for (int i = 0; i < ctp.mainArena.teams.size(); i++) {
            ctp.mainArena.teams.get(i).memberCount = 0;
        }
    }

    public boolean isAlreadyInGame (Player p) {
        return ctp.playerData.get(p) != null;
    }

    private boolean isInsidePoint (CTPPoints point, Location loc) {
        if (loc.getBlockX() == point.x || loc.getBlockX() == point.x + 1) {
            if (loc.getBlockY() == point.y) {
                if (loc.getBlockZ() == point.z || loc.getBlockZ() == point.z + 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInsidePointVert (CTPPoints point, Location loc) {
        if (point.pointDirection.equals("NORTH")) {
            if (loc.getBlockX() == point.x) {
                if ((loc.getBlockY() == point.y) || (loc.getBlockY() == point.y + 1)) {
                    if (loc.getBlockZ() == point.z || loc.getBlockZ() == point.z + 1) {
                        return true;
                    }
                }
            }
        } else if (point.pointDirection.equals("EAST")) {
            if ((loc.getBlockX() == point.x) || (loc.getBlockX() == point.x + 1)) {
                if ((loc.getBlockY() == point.y) || (loc.getBlockY() == point.y + 1)) {
                    if (loc.getBlockZ() == point.z) {
                        return true;
                    }
                }
            }
        } else if (point.pointDirection.equals("SOUTH")) {
            if (loc.getBlockX() == point.x) {
                if (loc.getBlockY() == point.y || loc.getBlockY() == point.y + 1) {
                    if (loc.getBlockZ() == point.z || loc.getBlockZ() == point.z + 1) {
                        return true;
                    }
                }
            }
        } else if (point.pointDirection.equals("WEST")) {
            if (loc.getBlockX() == point.x || loc.getBlockX() == point.x + 1) {
                if (loc.getBlockY() == point.y || loc.getBlockY() == point.y + 1) {
                    if (loc.getBlockZ() == point.z) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public DyeColor itsColor (Block b) {
        BlockState state = b.getState();
        MaterialData data = state.getData();
        if ((data instanceof Wool)) {
            Wool wool = (Wool) data;
            return wool.getColor();
        }
        return null;
    }

    public void restoreThings (Player p) {
        ctp.playerData.get(p).justJoined = true;
        this.ctp.restoreInv(p);

        Location loc = ctp.previousLocation.get(p);
        loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
        p.teleport(this.ctp.previousLocation.get(p));

        // do not check double signal
        if (ctp.playerData.get(p) == null) {
            return;
        }

        p.setFoodLevel(ctp.playerData.get(p).foodLevel);
        if (ctp.playerData.get(p).isInCreativeMode) {
            p.setGameMode(GameMode.CREATIVE);
        }

        if (ctp.playerData.get(p).health > 200 || ctp.playerData.get(p).health < 0) {
            p.setHealth(20);
        } else {
            p.setHealth(ctp.playerData.get(p).health);
        }
    }

    public String subtractPoints (String aTeam, String lostpoint) { // Kj -- remade.
        if (this.capturegame) {
            for (Team team : ctp.mainArena.teams) {
                if (team.color.equalsIgnoreCase(aTeam)) {
                    team.controledPoints--;
                    if (team.controledPoints < 0) {
                        team.controledPoints = 0;
                    }
                    return team.chatcolor + aTeam.toUpperCase() + ChatColor.WHITE + " lost " + ChatColor.GOLD + lostpoint + ".";
                }
            }
            return null;
        }
        return null;
    }

}