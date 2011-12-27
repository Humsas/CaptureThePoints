package me.dalton.capturethepoints.listeners;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import me.dalton.capturethepoints.CTPPoints;
import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.HealingItems;
import me.dalton.capturethepoints.Items;
import me.dalton.capturethepoints.Lobby;
import me.dalton.capturethepoints.PlayerData;
import me.dalton.capturethepoints.PlayersAndCooldowns;
import me.dalton.capturethepoints.Spawn;
import me.dalton.capturethepoints.Team;
import me.dalton.capturethepoints.Util;
import me.dalton.capturethepoints.commands.PJoinCommand;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CaptureThePointsPlayerListener extends PlayerListener {
    private final CaptureThePoints ctp;

    public List<Player> waitingToMove = new LinkedList<Player>();

    public CaptureThePointsPlayerListener (CaptureThePoints plugin) {
        this.ctp = plugin;
    }

//    @Override
//    public void onPlayerJoin(PlayerJoinEvent event)
//    {
//        Player player = event.getPlayer();
//
//        plugin.playerInventory.setInventory(player);
//    }
    @Override
    public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        //String error = ctp.checkMainArena(player, ctp.mainArena);  // No error checking on commands!
        //if (error.isEmpty()) { // Error not found, main arena exists.
        if (ctp.mainArena != null && ctp.mainArena.co != null && !ctp.mainArena.co.allowCommands) {
            String[] args = event.getMessage().split(" ");
            if (!ctp.canAccess(player, false, new String[] { "ctp.*", "ctp.admin" }) && ctp.isGameRunning() && ctp.playerData.containsKey(player)
                    && !args[0].equalsIgnoreCase("/ctp")) {
                player.sendMessage(ChatColor.RED + "You can't use commands while playing!");
                event.setCancelled(true);
            }
        }
        //}
    }

    @Override
    public void onPlayerDropItem (PlayerDropItemEvent event) {
        if (ctp.playerData.containsKey(event.getPlayer())) {
            Player player = event.getPlayer();
            //Player in the lobby
            if (ctp.playerData.get(player).isInLobby) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot drop items in the lobby!");
                return;
            }
            if (!ctp.mainArena.co.allowDropItems) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You may not drop items.");
                return;
            }
            /*else { // Must be playing a game
            boolean helmetRemoved = checkHelmet(player);
            if (helmetRemoved) {
            fixHelmet(player);
            event.getItemDrop().remove();
            }
            }*/
        }
    }

    @Override
    public void onPlayerInteract (PlayerInteractEvent event) {
        if (ctp.mainArena == null) {
            return;
        }
        if (ctp.mainArena.lobby == null) {
            return;
        }

        if (ctp.playerData.containsKey(event.getPlayer())) {
            Player p = event.getPlayer();
            // Iron block
            if (event.hasBlock() && event.getClickedBlock().getTypeId() == 42) {
                //If this role exists
                if (ctp.roles.containsKey(ctp.playerData.get(p).role)) {
                    if (!ctp.playerData.get(p).isReady) {
                        Util.sendMessageToPlayers(ctp, p, ChatColor.GREEN + p.getName() + ChatColor.WHITE + " is ready.");
                    }
                    ctp.playerData.get(p).isReady = true;
                    ctp.mainArena.lobby.playersinlobby.put(p, true); // Kj
                    checkLobby(p);
                } else {
                    p.sendMessage(ChatColor.RED + "Please select a role.");
                }
                return;
            }

            // Sign
            if (event.hasBlock() && event.getClickedBlock().getState() instanceof Sign) {
                // Cast the block to a sign to get the text on it.
                Sign sign = (Sign) event.getClickedBlock().getState();
                // Check if the first line of the sign is a class name.
                String role = sign.getLine(0);
                if (role.equalsIgnoreCase("[CTP]")) {
                    shop(p, sign);
                } else if (!ctp.roles.containsKey(role.toLowerCase()) && !role.equalsIgnoreCase("random")) {
                    return;
                } else {
                    /* Sign looks like:
                     * ########## 
                     * #  ROLE  # <-- getLine(0)
                     * #  PRICE # <-- getLine(1)
                     * #        # <-- getLine(2)
                     * #        # <-- getLine(3)
                     * ##########
                     *     #
                     *     # 
                     */
                    
                    // Player is in Lobby choosing role.
                    if (ctp.mainArena.lobby.playersinlobby.containsKey(p)) {

                        // Kj's
                        if (role.equalsIgnoreCase("random")) {
                            int size = ctp.roles.size();
                            if (size > 1) { // If there is more than 1 role to choose from
                                Random random = new Random();
                                int nextInt = random.nextInt(size); // Generate a random number between 0 (inclusive) -> Number of roles (exclusive)
                                List<String> roles = new LinkedList<String>(ctp.roles.keySet()); // Get a list of available roles and convert to a String List
                                role =
                                        roles.get(nextInt) == null
                                        ? roles.get(0)
                                        : roles.get(nextInt); // Change the role based on the random number. (Ternary null check)
                            }
                        }

                        if (ctp.playerData.get(p).role != null && !ctp.playerData.get(p).role.isEmpty()) {
                            String oldRole = ctp.playerData.get(p).role;
                            p.sendMessage(ChatColor.LIGHT_PURPLE + "Changing your role from " + ChatColor.GOLD + oldRole.substring(0, 1).toUpperCase() + oldRole.substring(1).toLowerCase()
                                    + ChatColor.LIGHT_PURPLE + " to " + ChatColor.GOLD + role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase() + ChatColor.LIGHT_PURPLE + ".");
                            p.sendMessage("Remember to hit the iron block to ready up!");

                        } else {
                            p.sendMessage(ChatColor.GOLD + role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase()
                                    + ChatColor.LIGHT_PURPLE + " selected. Hit the iron block to ready up!");
                        }
                        ctp.blockListener.assignRole(p, role.toLowerCase()); // Assign new role
                        ctp.playerData.get(p).isReady = false; // Un-ready the player
                        ctp.mainArena.lobby.playersinlobby.put(p, false);
                        /*
                        ctp.playerData.get(p).lobbyJoinTime = System.currentTimeMillis(); // Restart the lobby activity timer
                        ctp.playerData.get(p).warnedAboutActivity = false; 
                         */
                        return;

                        // Player is in game choosing role.
                    } else if (ctp.playerData.get(p).isInArena) {
                        
                        int price = 0;
                        String pricestr = sign.getLine(1) == null ? "" : sign.getLine(1);
                        
                        if (!pricestr.isEmpty()) {
                            try {
                                price = Integer.parseInt(pricestr); // Get price. 
                            } catch (Exception e) {
                                price = Integer.MAX_VALUE; // Sign's price is illegal.
                            }
                        }                                                    

                        // Kj's
                        if (role.equalsIgnoreCase("random")) {
                            int size = ctp.roles.size();
                            if (size > 1) { // If there is more than 1 role to choose from
                                Random random = new Random();
                                int nextInt = random.nextInt(size); // Generate a random number between 0 (inclusive) -> Number of roles (exclusive)
                                List<String> roles = new LinkedList<String>(ctp.roles.keySet()); // Get a list of available roles and convert to a String List
                                role =
                                        roles.get(nextInt) == null
                                        ? roles.get(0)
                                        : roles.get(nextInt); // Change the role based on the random number. (Ternary null check)
                            }
                        }
                        
                        if (price == 0) {
                            String oldRole = ctp.playerData.get(p).role;
                            p.sendMessage(ChatColor.LIGHT_PURPLE + "Changing your role from " + ChatColor.GOLD + oldRole.substring(0, 1).toUpperCase() + oldRole.substring(1).toLowerCase()
                                    + ChatColor.LIGHT_PURPLE + " to " + ChatColor.GOLD + role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase() + ChatColor.LIGHT_PURPLE + ".");

                            ctp.blockListener.assignRole(p, role.toLowerCase()); // Assign new role
                        } else {
                            if (canPay(p, price)) {
                                chargeAccount(p, price);
                                String oldRole = ctp.playerData.get(p).role;
                                p.sendMessage(ChatColor.LIGHT_PURPLE + "Successfully bought new role for " + ChatColor.GREEN + price + ChatColor.LIGHT_PURPLE + ". "
                                        + "You changed from " + ChatColor.GOLD + oldRole.substring(0, 1).toUpperCase() + oldRole.substring(1).toLowerCase()
                                        + ChatColor.LIGHT_PURPLE + " to " + ChatColor.GOLD + role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase() + ChatColor.LIGHT_PURPLE + ".");
                                ctp.blockListener.assignRole(p, role.toLowerCase()); // Assign new role
                                return;
                            } else {
                                String message =
                                        price != Integer.MAX_VALUE
                                        ? "Not enough money! You have " + ChatColor.GREEN + ctp.playerData.get(p).money + ChatColor.WHITE + " money, but you need " + ChatColor.GREEN + price + ChatColor.WHITE + " money."
                                        : ChatColor.RED + "This sign does not have a legal price. Please inform an admin.";
                                p.sendMessage(message);
                                return;
                            }
                        }
                    }
                }
                return;
            }
            // check for Healing item usage
            useHealingItem(event, p);
        }
    }

    @Override
    public void onPlayerMove (PlayerMoveEvent event) {
        if (!(ctp.isGameRunning())) {
            return;
        }
        Location loc = event.getTo();
        // Find if player is in arena
        if (this.ctp.playerData.get(event.getPlayer()) != null && !ctp.playerData.get(event.getPlayer()).isInLobby) {
            Player player = event.getPlayer();
            if (ctp.playerData.get(player).moveChecker >= 10) {
                ctp.playerData.get(player).moveChecker = 0;
                if (isInside(loc.getBlockY(), ctp.mainArena.y1, ctp.mainArena.y2) && isInside(loc.getBlockX(), ctp.mainArena.x1, ctp.mainArena.x2) && isInside(loc.getBlockZ(), ctp.mainArena.z1, ctp.mainArena.z2) && loc.getWorld().getName().equalsIgnoreCase(ctp.mainArena.world)) {
                    return;
                } else {
                    String color = ctp.playerData.get(player).team.color;
                    Location loc2 = new Location(ctp.getServer().getWorld(ctp.mainArena.world), ctp.mainArena.teamSpawns.get(color).x, ctp.mainArena.teamSpawns.get(color).y + 1, ctp.mainArena.teamSpawns.get(color).z);
                    loc2.setYaw((float) ctp.mainArena.teamSpawns.get(color).dir);
                    loc2.getWorld().loadChunk(loc2.getBlockX(), loc2.getBlockZ());
                    player.teleport(loc2);
                }
            } else {
                ctp.playerData.get(player).moveChecker++;
            }
        }
    }

    @Override
    public void onPlayerQuit (PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (this.ctp.playerData.get(player) != null) {
            ctp.leaveGame(player);
        }
    }

    @Override
    public void onPlayerTeleport (PlayerTeleportEvent event) {
        if (!(ctp.isGameRunning())) {
            if (this.ctp.playerData.get(event.getPlayer()) != null && ctp.playerData.get(event.getPlayer()).isInLobby) {
                if (isInside(event.getTo().getBlockX(), ctp.mainArena.x1, ctp.mainArena.x2) && isInside(event.getTo().getBlockY(), ctp.mainArena.y1, ctp.mainArena.y2) && isInside(event.getTo().getBlockZ(), ctp.mainArena.z1, ctp.mainArena.z2) && event.getTo().getWorld().getName().equalsIgnoreCase(ctp.mainArena.world)) {
                    ctp.playerData.get(event.getPlayer()).justJoined = false;
                    return;
                } else {
                    if (this.ctp.playerData.get(event.getPlayer()).justJoined) { // allowed to teleport
                        this.ctp.playerData.get(event.getPlayer()).justJoined = false;
                        return;
                    } else {
                        event.setCancelled(true);
                        ctp.playerData.get(event.getPlayer()).isInArena = false;
                        ctp.playerData.get(event.getPlayer()).isInLobby = false;
                        ctp.mainArena.lobby.playersinlobby.remove(event.getPlayer());
                        ctp.leaveGame(event.getPlayer());
                        event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] You left the CTP game.");
                    }
                }
            }
            return;
        }

        Player play = event.getPlayer();
        if (ctp.playerData.get(play) == null) {
            return;
        }

        //If ctp leave command
        if (ctp.playerData.get(event.getPlayer()).justJoined) {
            ctp.playerData.get(event.getPlayer()).justJoined = false;
            return;
        }

        // Find if player is in arena
        if (ctp.playerData.get(play).isInArena) {
            Spawn playerspawn = ctp.playerData.get(play).team.spawn; // Get the player's spawnpoint
            if (event.getTo().getX() == playerspawn.x && event.getTo().getZ() == playerspawn.z) {
                // The player is going to their spawn.
                return;
            }
            if (isInside(event.getTo().getBlockX(), ctp.mainArena.x1, ctp.mainArena.x2) && isInside(event.getTo().getBlockZ(), ctp.mainArena.z1, ctp.mainArena.z2) && event.getTo().getWorld().getName().equalsIgnoreCase(ctp.mainArena.world)) {
                // The player is teleporting in the arena.
                return;
            } else {
                // The player is teleporting out of the arena!
                event.setCancelled(true);
                play.sendMessage(ChatColor.RED + "Not allowed to teleport out of the arena!");
            }
        }
    }

    /** Check if the player can afford this price */
    public boolean canPay (Player player, int price) {
        return (price != Integer.MAX_VALUE && ctp.playerData.get(player).money >= price);
    }

    /** Deduct the price from the player's account. Returns boolean whether play had enough funds to do so. */
    public boolean chargeAccount (Player player, int price) {
        if (ctp.playerData.get(player).money >= price) {
            ctp.playerData.get(player).money -= price;
            return true;
        }
        return false;
    }

    // Ideally we want to take out the Player parameter (without losing its purpose, of course).
    /** Check the lobby to see if player[s] can be transferred. If not, it returns false. */
    private void checkLobby (Player p) {

        // Kj -- If autostart is turned off, might as well ignore this. However, if a game has started and someone wants to join, that's different.
        if (ctp.mainArena.co.autoStart || !ctp.isPreGame()) {

            Lobby lobby = ctp.mainArena.lobby;
            int readypeople = lobby.countReadyPeople();

            // The maximum number of players must be greater than the players already playing.
            if (ctp.mainArena.maximumPlayers > ctp.mainArena.getPlayersPlaying(ctp).size()) {

                // Game not yet started
                if (ctp.isPreGame()) {
                    if (!lobby.hasUnreadyPeople()) {
                        if (readypeople >= ctp.mainArena.minimumPlayers) {
                            if (readypeople % 2 == 0) {
                                moveToSpawns();
                            } else {
                                if (ctp.mainArena.co.exactTeamMemberCount) {
                                    if (readypeople / ctp.mainArena.teams.size() >= 1) {
                                        moveToSpawns();
                                    } else {
                                        p.sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] There are already an even number of players. Please wait for a new player to ready up."); // Kj
                                        return;
                                    }
                                    // Does not require exact count and everyone is ready. Move them.
                                } else {
                                    moveToSpawns();
                                }
                            }
                        } else {
                            if (ctp.hasSuitableArena(readypeople)) {
                                Util.sendMessageToPlayers(ctp, ChatColor.RED + "Not enough players for a game. Attempting to change arena. [Needed " + ctp.mainArena.minimumPlayers + " players, found " + readypeople + "].");
                                List<Player> transport = new LinkedList<Player>(lobby.playersinlobby.keySet());
                                ctp.blockListener.endGame(true);
                                ctp.chooseSuitableArena(readypeople);
                                for (Player aPlayer : transport) {
                                    PJoinCommand pj = new PJoinCommand(ctp); 
                                    pj.execute(ctp.getServer().getConsoleSender(), Arrays.asList("ctp", "pjoin", aPlayer.getName()));
                                }
                            } else {
                                Util.sendMessageToPlayers(ctp, ChatColor.RED + "Not enough players for a game. No other suitable arenas found. [Needed " + ctp.mainArena.minimumPlayers + " players, found " + readypeople + "].");
                                ctp.blockListener.endGame(true);
                            }
                        }
                    } else {
                        p.sendMessage(ChatColor.GREEN + "Thank you for readying. Waiting for " + lobby.countUnreadyPeople() + "/" + lobby.countAllPeople() + " people to ready up."); // Kj
                    }
                    // Game already started
                } else {
                    if (!ctp.mainArena.co.allowLateJoin) {
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] A game has already started. You may not join."); // Kj
                        return;
                    }

                    // Player is ready.
                    if (lobby.playersinlobby.get(p)) {
                        if (ctp.mainArena.co.exactTeamMemberCount) {

                            // Uneven number of people and balanced teams is on.  
                            if (ctp.mainArena.getPlayersPlaying(ctp).size() % ctp.mainArena.teams.size() != 0) {
                                moveToSpawns(p);
                                return;

                            // Even number of people and balanced teams is on.  
                            } else if (lobby.playersinlobby.get(p)) {
                                if (waitingToMove.isEmpty()) {
                                    waitingToMove.add(p); // Add to queue
                                    p.sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] There is an even number of players. Please wait or do /ctp leave."); // Kj
                                } else {
                                    // Already someone waiting, both can now join. Queue is cleared.
                                    moveToSpawns(waitingToMove.get(0));
                                    moveToSpawns(p);
                                }
                                return;
                            }

                        // Exact player count off. Player can be moved.
                        } else {
                            moveToSpawns(p);
                        }
                    }
                }
            } else {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] This arena is full."); // Kj
                return;
            }
        }
    }

    // Kj's helmet check
    /*public boolean checkHelmet(Player p) {
    if (p.getInventory().getHelmet() == null) {
    return true;
    }
    return ((p.getInventory().getHelmet().getType() != Material.WOOL) && (ctp.playerData.get(p).isInArena));
    }*/
    public void fixHelmet (Player p) {
        PlayerInventory inv = p.getInventory();
        p.sendMessage(ChatColor.RED + "Do not remove your helmet.");
        DyeColor color1 = DyeColor.valueOf(ctp.playerData.get(p).team.color.toUpperCase());
        ItemStack helmet = new ItemStack(Material.WOOL, 1, (short) color1.getData());

        inv.remove(Material.WOOL);
        p.getInventory().setHelmet(helmet);
        p.updateInventory();
    }

    public boolean isInside (int loc, int first, int second) {
        int point1 = 0;
        int point2 = 0;
        if (first < second) {
            point1 = first;
            point2 = second;
        } else {
            point2 = first;
            point1 = second;
        }

        return (point1 < loc) && (loc < point2);
    }

    public void moveToSpawns () {
        if (ctp.mainArena.co.exactTeamMemberCount) {
            for (Player play : ctp.playerData.keySet()) {
                PlayerData data = ctp.playerData.get(play);
                if ((data.isInLobby) && (data.isReady)) {
                    if (waitingToMove.isEmpty()) {
                        waitingToMove.add(play); // Add to queue
                    } else {
                        moveToSpawns(waitingToMove.get(0)); // Already someone waiting, both can now join. Queue is cleared.
                        moveToSpawns(play);
                    }
                }
            }
            if (waitingToMove != null && !waitingToMove.isEmpty()) {
                if (waitingToMove.size() >= 1) {
                    waitingToMove.get(0).sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] There are already an even number of players. Please wait for a new player to ready up."); // Kj
                }
            }
        } else {
            for (Player player : ctp.playerData.keySet()) {
                moveToSpawns(player);
            }
        }

        //Game settings
        for (Team team : ctp.mainArena.teams) {
            team.controledPoints = 0;
            team.score = 0;
        }
        if ((!ctp.mainArena.co.useScoreGeneration) && (ctp.mainArena.co.pointsToWin > ctp.mainArena.capturePoints.size())) {
            ctp.mainArena.co.pointsToWin = ctp.mainArena.capturePoints.size();
        }
        ctp.blockListener.capturegame = true;
        ctp.getServer().broadcastMessage("A Capture The Points game has started!");
        //Util.sendMessageToPlayers(ctp, "A Capture The Points game has started!"); // Kj change to message rather than broadcast
        ctp.blockListener.preGame = false;
        ctp.blockListener.didSomeoneWin();

        // Play time for points only
        ctp.CTP_Scheduler.playTimer = ctp.getServer().getScheduler().scheduleSyncDelayedTask(ctp, new Runnable() {
            @Override
            public void run () {
                if ((ctp.isGameRunning()) && (!ctp.mainArena.co.useScoreGeneration)) {
                    int maxPoints = -9999;
                    for (Team team : ctp.mainArena.teams) {
                        if (team.controledPoints > maxPoints) {
                            maxPoints = team.controledPoints;
                        }
                    }
                    HashMap<String, String> colors = new HashMap<String, String>();

                    for (Team team : ctp.mainArena.teams) {
                        if (team.controledPoints == maxPoints) {
                            colors.put(team.color, team.color);
                        }
                    }

                    for (Player player : ctp.playerData.keySet()) {
                        if ((ctp.playerData.get(player).isInArena) && (colors.containsKey(ctp.playerData.get(player).team.color))) {
                            ctp.playerData.get(player).winner = true;
                        }
                    }

                    Util.sendMessageToPlayers(ctp, "Time out! " + ChatColor.GREEN + colors.values().toString().toUpperCase().replace(",", " and") + ChatColor.WHITE + " wins!");
                    ctp.CTP_Scheduler.playTimer = 0;
                    ctp.blockListener.endGame(false);
                }
            }

        }, ctp.mainArena.co.playTime * 20 * 60);

        //Money giving and score generation
        ctp.CTP_Scheduler.money_Score = ctp.getServer().getScheduler().scheduleSyncRepeatingTask(ctp, new Runnable() {
            @Override
            public void run () {
                if (ctp.isGameRunning()) {
                    for (PlayerData data : ctp.playerData.values()) {
                        if (data.isInArena) {
                            data.money += ctp.mainArena.co.moneyEvery30Sec;
                        }
                    }
                    if (ctp.mainArena.co.useScoreGeneration)
                    {
                        for (Team team : ctp.mainArena.teams)
                        {
                            int dublicator = 1;
                            int maxPossiblePointsToCapture = 0;
                            for (CTPPoints point : ctp.mainArena.capturePoints)
                            {
                                if(point.notAllowedToCaptureTeams == null || !point.notAllowedToCaptureTeams.contains(team.color))
                                    maxPossiblePointsToCapture++;
                            }

                            if (team.controledPoints == maxPossiblePointsToCapture && maxPossiblePointsToCapture > 0)
                            {
                                dublicator = ctp.mainArena.co.scoreMyltiplier;
                            }
                            team.score += ctp.mainArena.co.onePointGeneratedScoreEvery30sec * team.controledPoints * dublicator;
                        }
                    }
                    ctp.blockListener.didSomeoneWin();
                }
            }

        }, 600L, 600L);//30 sec

        //Messages about score
        ctp.CTP_Scheduler.pointMessenger = ctp.getServer().getScheduler().scheduleSyncRepeatingTask(ctp, new Runnable() {
            @Override
            public void run () {
                if ((ctp.isGameRunning()) && (ctp.mainArena.co.useScoreGeneration)) {
                    String s = "";
                    for (Team team : ctp.mainArena.teams) {
                        s = s + team.chatcolor + team.color.toUpperCase() + ChatColor.WHITE + " score: " + team.score + ChatColor.AQUA + " // "; // Kj -- Added teamcolour
                    }
                    for (Player play : ctp.playerData.keySet()) {
                        play.sendMessage("Max Score: " + ChatColor.GOLD + ctp.mainArena.co.scoreToWin); // Kj -- Green -> Gold
                        play.sendMessage(s);
                    }
                }
            }

        }, ctp.mainArena.co.scoreAnnounceTime * 20, ctp.mainArena.co.scoreAnnounceTime * 20);

        // Healing items cooldowns
        ctp.CTP_Scheduler.healingItemsCooldowns = ctp.getServer().getScheduler().scheduleSyncRepeatingTask(ctp, new Runnable() {
            @Override
            public void run () {
                if (ctp.isGameRunning()) {
                    for (HealingItems item : ctp.healingItems) {
                        if (item != null && item.cooldowns != null && item.cooldowns.size() > 0) {
                            for (String playName : item.cooldowns.keySet()) {
                                PlayersAndCooldowns data = item.cooldowns.get(playName);
                                if (data.cooldown == 1) // This is cause we begin from top
                                {
                                    ctp.getServer().getPlayer(playName).sendMessage(ChatColor.GREEN + item.item.item.toString() + ChatColor.WHITE + " cooldown has refreshed!");
                                }

                                if (data.healingTimesLeft > 0 && data.intervalTimeLeft <= 0) {
                                    if (ctp.getServer().getPlayer(playName).getHealth() + item.hotHeal > ctp.mainArena.co.maxPlayerHealth) {
                                        ctp.getServer().getPlayer(playName).setHealth(ctp.mainArena.co.maxPlayerHealth);
                                    } else {
                                        ctp.getServer().getPlayer(playName).setHealth(ctp.getServer().getPlayer(playName).getHealth() + item.hotHeal);
                                    }
                                    data.intervalTimeLeft = item.hotInterval;
                                    data.healingTimesLeft--;
                                }
                                //ctp.getServer().getPlayer(playName).sendMessage(ChatColor.GREEN + item.item.item.toString() + ChatColor.WHITE + " cooldown: " + data.cooldown);
                                data.intervalTimeLeft--;
                                data.cooldown--;

                                if (data.cooldown <= 0 && data.healingTimesLeft <= 0) {
                                    item.cooldowns.remove(playName);
                                }
                            }
                        }
                    }
                }
            }

        }, 20L, 20L); // Every one sec

        //Helmet Checking
        ctp.CTP_Scheduler.helmChecker = ctp.getServer().getScheduler().scheduleSyncRepeatingTask(ctp, new Runnable() {
            @Override
            public void run () {
                if (ctp.isGameRunning()) {
                    for (Player player : ctp.playerData.keySet()) {
                        PlayerInventory inv = player.getInventory();
                        if (!ctp.playerData.get(player).isInArena) {
                            return;
                        }
                        if (inv.getHelmet() != null && (inv.getHelmet().getType() == Material.WOOL)) {
                            return;                            
                        }
                        DyeColor color1 = DyeColor.valueOf(ctp.playerData.get(player).team.color.toUpperCase());
                        
                        inv.remove(Material.WOOL);
                        ctp.entityListener.respawnPlayer(player, null);
                        
                        ItemStack helmet = new ItemStack(Material.WOOL, 1, color1.getData());
                        player.getInventory().setHelmet(helmet);
                        player.updateInventory();
                    }
                }
            }

        }, 100L, 100L);
    }

    public void moveToSpawns (Player player) {
        if (waitingToMove != null && !waitingToMove.isEmpty()) {
            if (waitingToMove.size() >= 1) {
                if (waitingToMove.get(0) == player) {
                    clearWaitingQueue(); // The queued player is joining. We can clear the waiting queue ready for next pair.
                }
            }
        }

        //Assign team
        int smallest = 99999;
        String color = null;
        Team team = null;
        int teamNR = -1;

        for (int i = 0; i < ctp.mainArena.teams.size(); i++) {
            if (ctp.mainArena.teams.get(i).memberCount < smallest) {
                team = ctp.mainArena.teams.get(i);
                smallest = team.memberCount;
                color = team.color;
                teamNR = i;
            }
        }

        try {
            ctp.mainArena.teams.get(teamNR).chatcolor = ChatColor.valueOf(team.color.toUpperCase()); // Kj
        } catch (Exception ex) {
            ctp.mainArena.teams.get(teamNR).chatcolor = ChatColor.GREEN;
        }

        ctp.mainArena.teams.get(teamNR).memberCount++;

        //Give wool
        DyeColor color1 = DyeColor.valueOf(color.toUpperCase());
        ItemStack helmet = new ItemStack(Material.WOOL, 1, color1.getData());
        player.getInventory().setHelmet(helmet);
        if (ctp.mainArena.co.givenWoolNumber != -1) {  // Kj -- if it equals -1, skip the giving of wool.
            ItemStack wool = new ItemStack(Material.WOOL, ctp.mainArena.co.givenWoolNumber, color1.getData());
            player.getInventory().addItem(wool);
            //player.getInventory().addItem(new ItemStack[]{wool});
        }
        player.updateInventory();

        //Move to spawn     TODO do not move players to same point
        ctp.playerData.get(player).team = team;

        Spawn spawn =
                ctp.mainArena.teamSpawns.get(ctp.playerData.get(player).team.color) != null
                ? ctp.mainArena.teamSpawns.get(ctp.playerData.get(player).team.color)
                : team.spawn;

        Location loc = new Location(ctp.getServer().getWorld(ctp.mainArena.world), ctp.mainArena.teamSpawns.get(color).x, ctp.mainArena.teamSpawns.get(color).y + 1D, ctp.mainArena.teamSpawns.get(color).z); // Kj -- Y+1
        loc.setYaw((float) ctp.mainArena.teamSpawns.get(color).dir);
        loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
        boolean teleport = player.teleport(loc);
        if (!teleport) {
            player.teleport(new Location(player.getWorld(), spawn.x, spawn.y, spawn.z, 0.0F, (float) spawn.dir));
        }
        ctp.mainArena.lobby.playersinlobby.remove(player);
        ctp.playerData.get(player).isInLobby = false;
        ctp.playerData.get(player).isInArena = true;
    }

    public void shop (Player p, Sign sign) {
//        if(p.getName().equalsIgnoreCase("Humsas"))
//            plugin.playerData.get(p).money = 100000;

        /* Sign looks like:
         * #######################
         * #        [CTP]        # <-- getLine(0)
         * #  MaterialInt:Amount # <-- getLine(1)
         * #        Price        # <-- getLine(2)
         * #      Team Color     # <-- getLine(3)
         * #######################
         *            #
         *            # 
         */
        String teamcolor = sign.getLine(3) == null ? "" : sign.getLine(3);

        // If sign requires team color to buy
        if(!teamcolor.isEmpty())
        {
            if (ctp.playerData.get(p).team == null || ctp.playerData.get(p).team.color == null)
            {
                return;
            }

            // Kj -- If player does not match the teamcolour if it is specified.
            if (!teamcolor.isEmpty() && !ctp.playerData.get(p).team.color.trim().equalsIgnoreCase(teamcolor.trim()))
            {
                p.sendMessage(ChatColor.RED + "You are not on the " + teamcolor.toUpperCase() + " team.");
                return;
            }
        }

        List<Items> list = new LinkedList<Items>();
        list = Util.getItemListFromString(sign.getLine(1));

        if (list.get(0).item == null) { // Kj -- changed bracing from != null ... to == null return;
            return;
        }

        String pricestr = sign.getLine(2) == null ? "" : sign.getLine(2);
        int price = 0;

        if (!pricestr.isEmpty()) {
            try {
                price = Integer.parseInt(pricestr); // Get price. 
            } catch (Exception e) {
                price = Integer.MAX_VALUE; // Sign's price is illegal.
            }
        }    

        if (canPay(p, price)) {
            int amount = 1;
            if (list.get(0).item == Material.ARROW) {
                amount = 64;
            }

            if (list.get(0).type == -1) {
                ItemStack stack = new ItemStack(list.get(0).item, amount);
                p.getInventory().addItem(stack);
                //p.getInventory().addItem(new ItemStack[]{stack});
            } else {
                byte offset = 0;
                if (list.get(0).item == Material.INK_SACK) {
                    offset = 15;
                }
                DyeColor dye = DyeColor.getByData((byte) Math.abs(offset - list.get(0).type));
                ItemStack stack = new ItemStack(list.get(0).item, list.get(0).amount, (short) (byte) Math.abs(offset - dye.getData()));
                p.getInventory().addItem(stack);
                //p.getInventory().addItem(new ItemStack[]{tmp});
            }

            chargeAccount(p, price);
            //ItemStack i = new ItemStack(mat.getId(), kiekis);
            //p.getInventory().addItem(i);

            p.sendMessage("You bought " + ChatColor.AQUA + list.get(0).amount + " " + list.get(0).item.toString().toLowerCase() + ChatColor.WHITE + " for " + ChatColor.GREEN + price + ChatColor.WHITE + " money.");
            p.sendMessage("You now have " + ChatColor.GREEN + ctp.playerData.get(p).money + ChatColor.WHITE + " money.");
            p.updateInventory();
            return;
        } else {
            String message = price != Integer.MAX_VALUE
                    ? "Not enough money! You have " + ChatColor.GREEN + ctp.playerData.get(p).money + ChatColor.WHITE + " money, but you need " + ChatColor.GREEN + price + ChatColor.WHITE + " money."
                    : ChatColor.RED + "This sign does not have a legal price. Please inform an admin.";
            p.sendMessage(message);
            return;
        }
    }

    public void useHealingItem (PlayerInteractEvent event, Player p) {
        if (ctp.isGameRunning() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            Material mat = p.getItemInHand().getType();
            for (HealingItems item : ctp.healingItems) {
                if (item.item.item == mat) {
                    PlayersAndCooldowns cooldownData = null;
                    boolean alreadyExists = false;
                    if (item.cooldowns != null && item.cooldowns.size() > 0) {
                        for (String playName : item.cooldowns.keySet()) {
                            if (p.getHealth() >= ctp.mainArena.co.maxPlayerHealth) {
                                p.sendMessage(ChatColor.RED + "You are healthy!");
                                return;
                            }
                            if (playName.equalsIgnoreCase(p.getName()) && item.cooldowns.get(playName).cooldown > 0) {
                                p.sendMessage(ChatColor.GREEN + item.item.item.toString() + ChatColor.WHITE + " is on cooldown! Time left: " + ChatColor.GREEN + item.cooldowns.get(playName).cooldown);
                                return;
                            } else if (playName.equalsIgnoreCase(p.getName())) {
                                cooldownData = item.cooldowns.get(playName);
                                break;
                            }
                        }
                    }
                    if (cooldownData == null) {
                        cooldownData = new PlayersAndCooldowns();
                    } else {
                        alreadyExists = true;
                    }

                    // If we are here item has no cooldown, but it can have HOT ticking, but we do not check that.
                    if (item.cooldown == 0) {
                        cooldownData.cooldown = -1;
                    } else {
                        cooldownData.cooldown = item.cooldown;
                    }

                    if (p.getHealth() + item.instantHeal > ctp.mainArena.co.maxPlayerHealth) {
                        p.setHealth(ctp.mainArena.co.maxPlayerHealth);
                    } else {
                        p.setHealth(p.getHealth() + item.instantHeal);
                        //p.sendMessage("" + p.getHealth());
                    }

                    if (item.duration > 0) {
                        cooldownData.healingTimesLeft = item.duration;
                        cooldownData.intervalTimeLeft = item.hotInterval;
                    }

                    if (!alreadyExists) {
                        item.cooldowns.put(p.getName(), cooldownData);
                    }

                    if (p.getItemInHand().getAmount() > 1) {
                        p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                    } else {
                        p.setItemInHand(null);
                    }
                    // Cancel event to not heal like with golden apple
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    public void clearWaitingQueue() {
        if (waitingToMove != null) {
            waitingToMove.clear();
        }
    }
}