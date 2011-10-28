package me.dalton.capturethepoints.listeners;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import me.dalton.capturethepoints.CaptureThePoints;
import me.dalton.capturethepoints.HealingItems;
import me.dalton.capturethepoints.Items;
import me.dalton.capturethepoints.Lobby;
import me.dalton.capturethepoints.PlayerData;
import me.dalton.capturethepoints.PlayersAndCooldowns;
import me.dalton.capturethepoints.Team;
import me.dalton.capturethepoints.Util;
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
import org.bukkit.material.Wool;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

public class CaptureThePointsPlayerListener extends PlayerListener {

    private CaptureThePoints ctp;

    public double loadDouble(String s) {
        Configuration config = this.ctp.load();
        return config.getDouble(s, 0.0D);
    }

    public Integer loadInt(String s) {
        Configuration config = this.ctp.load();
        return Integer.valueOf(config.getInt(s, 0));
    }

    public boolean loadBoolean(String s) {
        Configuration config = this.ctp.load();
        return config.getBoolean(s, false);
    }

    public String loadString(String s) {
        Configuration config = this.ctp.load();
        return config.getString(s, "");
    }

    public CaptureThePointsPlayerListener(CaptureThePoints plugin) {
        this.ctp = plugin;
    }

    Plugin checkPlugin(String pluginname) {
        return this.ctp.getServer().getPluginManager().getPlugin(pluginname);
    }

//    @Override
//    public void onPlayerJoin(PlayerJoinEvent event)
//    {
//        Player player = event.getPlayer();
//
//        plugin.playerInventory.setInventory(player);
//    }
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (this.ctp.playerData.get(player) != null) {
            ctp.leaveGame(player);
        }
    }

    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if(!ctp.configOptions.allowCommands)
        {
            Player player = event.getPlayer();
            String[] args = event.getMessage().split(" ");

            if( !ctp.canAccess(player, false, "ctp.*", "ctp.admin") && ctp.isGameRunning() && ctp.playerData.containsKey(player)
                    && !args[0].equalsIgnoreCase("/ctp"))
            {
                player.sendMessage(ChatColor.RED + "You can't use commands while playing!");
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (ctp.playerData.containsKey(event.getPlayer()))
        {
            Player p = event.getPlayer();
            // Iron block
            if (event.hasBlock() && event.getClickedBlock().getTypeId() == 42)
            {
                //If this role exists
                if (ctp.roles.containsKey(ctp.playerData.get(p).role)) {
                    if (!ctp.playerData.get(p).isReady) {
                        Util.sendMessageToPlayers(ctp, ChatColor.GREEN + p.getName() + ChatColor.WHITE + " is ready.");
                    }
                    ctp.playerData.get(p).isReady = true;
                    ctp.mainArena.lobby.playersinlobby.put(p, true); // Kj
                    checkLobby(p);
                } else {
                    p.sendMessage(ChatColor.RED + "Please select a role.");
                }
            }
            
            // Sign
            if (event.hasBlock() && event.getClickedBlock().getState() instanceof Sign)
            {
                // Cast the block to a sign to get the text on it.
                Sign sign = (Sign) event.getClickedBlock().getState();
                // Check if the first line of the sign is a class name.
                String role = sign.getLine(0);
                if (role.equalsIgnoreCase("[CTP]")) {
                    shop(p, sign);
                } else if (!ctp.roles.containsKey(role.toLowerCase()) && !role.equalsIgnoreCase("random")) {
                    return;
                } else {
                    
                // Kj's
                    if (role.equalsIgnoreCase("random")) {
                        int size = ctp.roles.size();
                        if (size > 1) { // If there is more than 1 role to choose from
                            Random random = new Random();
                            int nextInt = random.nextInt(size); // Generate a random number between 0 (inclusive) -> Number of roles (exclusive)
                            Set<String> keySet = ctp.roles.keySet(); // Get a list of available roles... 
                            List<String> roles = new LinkedList<String>(keySet); // And convert to a String List
                            role = roles.get(nextInt) == null ? roles.get(0) : roles.get(nextInt); // Change the role based on the random number. (Ternary null check)
                        }
                    }
                    
                    ctp.blockListener.assignRole(p, role.toLowerCase());
                    ctp.playerData.get(p).isReady = false;
                    ctp.mainArena.lobby.playersinlobby.put(p, false);
                    p.sendMessage(ChatColor.GOLD + role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase()
                            + ChatColor.LIGHT_PURPLE + " selected. Hit the iron block to ready up!");
                }

                return;
            }
            // check for Healing item usage
            if (ctp.isGameRunning() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
            {
                Material mat = p.getItemInHand().getType();
                for (HealingItems item : ctp.healingItems)
                {
                    if(item.item.item == mat)
                    {
                        PlayersAndCooldowns cooldownData = null;
                        boolean alreadyExists = false;
                        if(item.cooldowns != null && item.cooldowns.size() > 0)
                        {
                            for(String playName : item.cooldowns.keySet())
                            {
                                if(p.getHealth() >= ctp.configOptions.maxPlayerHealth)
                                {
                                    p.sendMessage(ChatColor.RED + "You are healty!");
                                    return;
                                }
                                if(playName.equalsIgnoreCase(p.getName()) && item.cooldowns.get(playName).cooldown > 0)
                                {
                                    p.sendMessage(ChatColor.GREEN + item.item.item.toString() + ChatColor.WHITE + " is on cooldown!");
                                    return;
                                }
                                else if(playName.equalsIgnoreCase(p.getName()))
                                {
                                    cooldownData = item.cooldowns.get(playName);
                                    break;
                                }
                            }
                        }
                        if(cooldownData == null)
                            cooldownData = new PlayersAndCooldowns();
                        else
                            alreadyExists = true;

                        // If we are here item has no cooldown, but it can have HOT ticking, but we do not check that.
                        if(item.cooldown == 0)
                            cooldownData.cooldown = -1;
                        else
                            cooldownData.cooldown = item.cooldown;

                        if(p.getHealth() + item.instantHeal > ctp.configOptions.maxPlayerHealth)
                        {
                            p.setHealth(ctp.configOptions.maxPlayerHealth);
                        }
                        else
                        {
                            p.setHealth(p.getHealth() + item.instantHeal);
                            //p.sendMessage("" + p.getHealth());
                        }

                        if(item.duration > 0)
                        {
                            cooldownData.healingTimesLeft = item.duration;
                            cooldownData.intervalTimeLeft = item.hotInterval;
                        }

                        if(!alreadyExists)
                            item.cooldowns.put(p.getName(), cooldownData);
                        
                        if(p.getItemInHand().getAmount() > 1)
                        {
                            p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                        }
                        else
                        {
                            p.setItemInHand(null);
                        }
                        // Cancel event to not heal like with golden apple
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    public void shop(Player p, Sign sign) {
//        if(p.getName().equalsIgnoreCase("Humsas"))
//            plugin.playerData.get(p).money = 100000;
        List<Items> list = new LinkedList<Items>();
        list = Util.getItemListFromString(sign.getLine(1));

        if (list.get(0).item == null) { // Kj -- changed bracing from != null ... to == null return;
            return;
        }

        String teamcolor = sign.getLine(3) == null ? "" : sign.getLine(3);

        // Kj -- If player does not match the teamcolour if it is specified.
        if (!ctp.playerData.get(p).color.trim().equalsIgnoreCase(teamcolor.trim()) && !teamcolor.isEmpty()) {
            p.sendMessage(ChatColor.RED + "You are not on the " + teamcolor.toUpperCase() + " team.");
            return;
        }

        int price = 999999;
        try {
            price = Integer.parseInt(sign.getLine(2));
        } catch (Exception NumberFormatException) {
            return;
        }

        if (ctp.playerData.get(p).money >= price) {
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
            ctp.playerData.get(p).money -= price;

            //ItemStack i = new ItemStack(mat.getId(), kiekis);
            //p.getInventory().addItem(i);
            p.sendMessage("You bought " + ChatColor.AQUA + list.get(0).amount + " " + list.get(0).item.toString().toLowerCase() + ChatColor.WHITE + " for " + ChatColor.GREEN + price + ChatColor.WHITE + " money.");
            p.sendMessage("You now have " + ChatColor.GREEN + ctp.playerData.get(p).money + ChatColor.WHITE + " money.");
            p.updateInventory();
        } else {
            p.sendMessage("Not enough money! You have " + ChatColor.GREEN + ctp.playerData.get(p).money + ChatColor.WHITE + " money, but you need " + ChatColor.GREEN + price + ChatColor.WHITE + " money.");
        }

    }

    public void moveToSpawns() {
        int readyPpl;
        int movedPeople;
        if (ctp.configOptions.exactTeamMemberCount) {
            readyPpl = 0;
            for (PlayerData data : ctp.playerData.values()) {
                if ((data.isReady) && (data.isInLobby)) {
                    readyPpl++;
                }
            }
            movedPeople = 0;
            for (Player play : ctp.playerData.keySet()) {
                PlayerData data = ctp.playerData.get(play);
                if ((data.isInLobby) && (data.isReady) && (movedPeople <= readyPpl / ctp.teams.size() * ctp.teams.size())) {
                    moveToSpawns(play);
                    movedPeople++;
                }
            }
        } else {
            for (Player player : ctp.playerData.keySet()) {
                moveToSpawns(player);
            }
        }

        //Game settings
        for (Team team : ctp.teams) {
            team.controledPoints = 0;
            team.score = 0;
        }
        if ((!ctp.configOptions.useScoreGeneration) && (ctp.configOptions.pointsToWin > ctp.mainArena.capturePoints.size())) {
            ctp.configOptions.pointsToWin = ctp.mainArena.capturePoints.size();
        }
        ctp.blockListener.capturegame = true;
        ctp.getServer().broadcastMessage("A Capture The Points game has started!");
        //Util.sendMessageToPlayers(ctp, "A Capture The Points game has started!"); // Kj change to message rather than broadcast
        ctp.blockListener.preGame = false;
        ctp.blockListener.didSomeoneWin();

        // Play time for points only
        ctp.CTP_Scheduler.playTimer = ctp.getServer().getScheduler().scheduleSyncDelayedTask(ctp, new Runnable() {

            @Override
            public void run() {
                if ((ctp.isGameRunning()) && (!ctp.configOptions.useScoreGeneration)) {
                    int maxPoints = -9999;
                    for (Team team : ctp.teams) {
                        if (team.controledPoints > maxPoints) {
                            maxPoints = team.controledPoints;
                        }
                    }
                    HashMap<String, String> colors = new HashMap<String, String>();

                    for (Team team : ctp.teams) {
                        if (team.controledPoints == maxPoints) {
                            colors.put(team.color, team.color);
                        }
                    }

                    for (Player player : ctp.playerData.keySet()) {
                        if ((ctp.playerData.get(player).isInArena) && (colors.containsKey(ctp.playerData.get(player).color))) {
                            ctp.playerData.get(player).winner = true;
                        }
                    }

                    Util.sendMessageToPlayers(ctp, "Time out! " + ChatColor.GREEN + colors.values().toString().toUpperCase().replace(",", " and") + ChatColor.WHITE + " wins!");
                    ctp.CTP_Scheduler.playTimer = 0;
                    ctp.blockListener.endGame(false);
                }
            }
        }, ctp.configOptions.playTime * 20 * 60);

        //Money giving and score generation
        ctp.CTP_Scheduler.money_Score = ctp.getServer().getScheduler().scheduleSyncRepeatingTask(ctp, new Runnable() {

            @Override
            public void run() {
                if (ctp.isGameRunning()) {
                    for (PlayerData data : ctp.playerData.values()) {
                        if (data.isInArena) {
                            data.money += ctp.configOptions.moneyEvery30Sec;
                        }
                    }
                    if (ctp.configOptions.useScoreGeneration) {
                        for (Team team : ctp.teams) {
                            int dublicator = 1;
                            if (team.controledPoints == ctp.mainArena.capturePoints.size() && ctp.mainArena.capturePoints.size() > 1) {
                                dublicator = 2;
                            }
                            team.score += ctp.configOptions.onePointGeneratedScoreEvery30sec * team.controledPoints * dublicator;
                        }
                    }
                    ctp.blockListener.didSomeoneWin();
                }
            }
        }, 600L, 600L);//30 sec

        //Messages about score
        ctp.CTP_Scheduler.pointMessenger = ctp.getServer().getScheduler().scheduleSyncRepeatingTask(ctp, new Runnable() {

            @Override
            public void run() {
                if ((ctp.isGameRunning()) && (ctp.configOptions.useScoreGeneration)) {
                    String s = "";
                    for (Team team : ctp.teams) {
                        s = s + team.chatcolor + team.color.toUpperCase() + ChatColor.WHITE + " score: " + team.score + ChatColor.AQUA + " // "; // Kj -- Added teamcolour
                    }
                    for (Player play : ctp.playerData.keySet()) {
                        play.sendMessage("Max Score: " + ChatColor.GOLD + ctp.configOptions.scoreToWin); // Kj -- Green -> Gold
                        play.sendMessage(s);
                    }
                }
            }
        }, ctp.configOptions.scoreAnnounceTime * 20, ctp.configOptions.scoreAnnounceTime * 20);

        // Healing items cooldowns
        ctp.CTP_Scheduler.healingItemsCooldowns = ctp.getServer().getScheduler().scheduleSyncRepeatingTask(ctp, new Runnable()
        {
            @Override
            public void run()
            {
                if (ctp.isGameRunning())
                {
                    for (HealingItems item : ctp.healingItems)
                    {
                        if( item != null && item.cooldowns != null && item.cooldowns.size() > 0)
                        {
                            for(String playName : item.cooldowns.keySet())
                            {
                                PlayersAndCooldowns data = item.cooldowns.get(playName);
                                if(data.cooldown == 1)  // This is cause we begin from top
                                {
                                    ctp.getServer().getPlayer(playName).sendMessage(ChatColor.GREEN + item.item.item.toString() + ChatColor.WHITE + " cooldown has refreshed!");
                                }
                                
                                if(data.healingTimesLeft > 0 && data.intervalTimeLeft <= 0)
                                {
                                    if(ctp.getServer().getPlayer(playName).getHealth() + item.hotHeal > ctp.configOptions.maxPlayerHealth)
                                    {
                                        ctp.getServer().getPlayer(playName).setHealth(ctp.configOptions.maxPlayerHealth);
                                    }
                                    else
                                    {
                                        ctp.getServer().getPlayer(playName).setHealth(ctp.getServer().getPlayer(playName).getHealth() + item.hotHeal);
                                    }
                                    data.intervalTimeLeft = item.hotInterval;
                                    data.healingTimesLeft--;
                                }
                                //ctp.getServer().getPlayer(playName).sendMessage(ChatColor.GREEN + item.item.item.toString() + ChatColor.WHITE + " cooldown: " + data.cooldown);
                                data.intervalTimeLeft--;
                                data.cooldown--;

                                if(data.cooldown <= 0 && data.healingTimesLeft <= 0)
                                {
                                    item.cooldowns.remove(playName);
                                }
                            }
                        }
                    }
                }
            }
        }, 20L, 20L); // Every one sec


        ctp.CTP_Scheduler.helmChecker = ctp.getServer().getScheduler().scheduleSyncRepeatingTask(ctp, new Runnable()
        {
            @Override
            public void run()
            {
                if(ctp.isGameRunning())
                {
                    for(Player player: ctp.playerData.keySet())
                    {
                        PlayerInventory inv = player.getInventory();
                        if(!(inv.getHelmet().getData() instanceof Wool) && ctp.playerData.get(player).isInArena)
                        {
                            DyeColor color1 = DyeColor.valueOf(ctp.playerData.get(player).color.toUpperCase());
                            ItemStack helmet = new ItemStack(Material.WOOL, 1, color1.getData());
                            player.getInventory().setHelmet(helmet);
                            player.updateInventory();
                        }
                    }
                }
            }
        }, 100L, 100L);
    }

    public void moveToSpawns(Player player) {
        //Assign team
        int smallest = 99999;
        String color = null;
        Team team = null;
        int teamNR = -1;

        for (int i = 0; i < ctp.teams.size(); i++) {
            if (ctp.teams.get(i).memberCount < smallest) {
                team = ctp.teams.get(i);
                smallest = team.memberCount;
                color = team.color;
                teamNR = i;
            }
        }

        try {
            ctp.teams.get(teamNR).chatcolor = ChatColor.valueOf(team.color.toUpperCase()); // Kj
        } catch (Exception ex) {
            ctp.teams.get(teamNR).chatcolor = ChatColor.GREEN;
        }

        ctp.teams.get(teamNR).memberCount++;

        //Give wool
        DyeColor color1 = DyeColor.valueOf(color.toUpperCase());
        ItemStack helmet = new ItemStack(Material.WOOL, 1, color1.getData());
        player.getInventory().setHelmet(helmet);
        if (ctp.configOptions.givenWoolNumber != -1) {  // Kj -- if it equals -1, skip the giving of wool.
            ItemStack wool = new ItemStack(Material.WOOL, ctp.configOptions.givenWoolNumber, color1.getData());
            player.getInventory().addItem(wool);
            //player.getInventory().addItem(new ItemStack[]{wool});
        }
        player.updateInventory();

        //Move to spawn     TODO do not move players to same point
        ctp.playerData.get(player).team = team;
        ctp.playerData.get(player).color = color;
        ctp.playerData.get(player).isInLobby = false;
        ctp.mainArena.lobby.playersinlobby.remove(player);
        Location loc = new Location(ctp.getServer().getWorld(ctp.mainArena.world), ctp.mainArena.teamSpawns.get(color).x, ctp.mainArena.teamSpawns.get(color).y + 1D, ctp.mainArena.teamSpawns.get(color).z); // Kj -- Y+1
        loc.setYaw((float) ctp.mainArena.teamSpawns.get(color).dir);
        loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
        player.teleport(loc);
        ctp.playerData.get(player).isInArena = true;
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!(ctp.isGameRunning())) {
            return;
        }

        Location loc = event.getTo();
        // Find if player is in arena
        if (this.ctp.playerData.get(event.getPlayer()) != null && !ctp.playerData.get(event.getPlayer()).isInLobby) {
            if (isInside(loc.getBlockX(), ctp.mainArena.x1, ctp.mainArena.x2) && isInside(loc.getBlockZ(), ctp.mainArena.z1, ctp.mainArena.z2) && loc.getWorld().getName().equalsIgnoreCase(ctp.mainArena.world)) {
                return;
            } else {
                String color = ctp.playerData.get(event.getPlayer()).color;
                Location loc2 = new Location(ctp.getServer().getWorld(ctp.mainArena.world), ctp.mainArena.teamSpawns.get(color).x, ctp.mainArena.teamSpawns.get(color).y + 1, ctp.mainArena.teamSpawns.get(color).z);
                loc.setYaw((float) ctp.mainArena.teamSpawns.get(color).dir);
                loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
                event.getPlayer().teleport(loc2);
            }
        }
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!(ctp.isGameRunning())) {
            if (this.ctp.playerData.get(event.getPlayer()) != null && ctp.playerData.get(event.getPlayer()).isInLobby)
            {
                if (isInside(event.getTo().getBlockX(), ctp.mainArena.x1, ctp.mainArena.x2) && isInside(event.getTo().getBlockZ(), ctp.mainArena.z1, ctp.mainArena.z2) && event.getTo().getWorld().getName().equalsIgnoreCase(ctp.mainArena.world))
                {
                    ctp.playerData.get(event.getPlayer()).justJoined = false;
                    return;
                } 
                else
                {
                    if (this.ctp.playerData.get(event.getPlayer()).justJoined) // allowed to teleport
                    {
                        this.ctp.playerData.get(event.getPlayer()).justJoined = false;
                        return;
                    } 
                    else
                    {
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
        if(ctp.playerData.get(play) == null)
            return;

        //If ctp leave command
        if(ctp.playerData.get(event.getPlayer()).justJoined) {
            ctp.playerData.get(event.getPlayer()).justJoined = false;
            return;
        }

        // Find if player is in arena
        if (ctp.playerData.get(play).isInArena) {
            if (isInside(event.getTo().getBlockX(), ctp.mainArena.x1, ctp.mainArena.x2) && isInside(event.getTo().getBlockZ(), ctp.mainArena.z1, ctp.mainArena.z2) && event.getTo().getWorld().getName().equalsIgnoreCase(ctp.mainArena.world)) {
                return;
            } else {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (ctp.playerData.containsKey(event.getPlayer())) {
            Player player = event.getPlayer();
            //Player in the lobby
            if (ctp.playerData.get(player).color == null) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot drop items in the lobby!"); // Kj item -> items
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

    public boolean isInside(int loc, int first, int second) {
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

    // Ideally we want to take out the Player parameter (without losing its purpose, of course).
    /** Check the lobby to see if player[s] can be transferred. If not, it returns false. */
    private void checkLobby(Player p) {
        
        // Kj -- If autostart is turned off, might as well ignore this. However, if a game has started and someone wants to join, that's different.
        if (ctp.configOptions.autoStart || !ctp.isPreGame()) {
            
            Lobby lobby = ctp.mainArena.lobby;
            int readypeople = lobby.countReadyPeople();
            
            // The maximum number of players must be greater than the players already playing.
            if (ctp.mainArena.maximumPlayers > ctp.mainArena.getPlayersPlaying(ctp).size()) {
                
                // Game not yet started
                if (ctp.isPreGame()) {
                    if (ctp.configOptions.exactTeamMemberCount) {

                        if (readypeople / ctp.teams.size() >= 1 /*&& !lobby.hasUnreadyPeople() */&& readypeople >= ctp.mainArena.minimumPlayers) {
                            moveToSpawns();
                        }
                    } else if (/*(readypeople == ctp.playerData.size()) && */!lobby.hasUnreadyPeople() && readypeople >= ctp.mainArena.minimumPlayers) {
                        moveToSpawns();
                    }

                // Game already started
                } else {
                    if (!ctp.configOptions.allowLateJoin) {
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] A game has already started. You may not join."); // Kj
                        return;
                    }

                    // If move players then exact number for team creating is up
                    if (ctp.configOptions.exactTeamMemberCount)
                    {
                        if (readypeople / ctp.teams.size() >= 1) 
                        {
                            int movedPeople = 0;
                            for (Player play : ctp.playerData.keySet())
                            {
                                PlayerData data = ctp.playerData.get(play);
                                if ((data.isInLobby) && (data.isReady) && (movedPeople <= (readypeople / ctp.teams.size() * ctp.teams.size()))) {
                                    moveToSpawns(play);
                                    movedPeople++;
                                }
                            }
                            // Uneven number of people and balanced teams is on.    
                        } else if (lobby.playersinlobby.get(p)) {
                            // Player is ready.
                            p.sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] There are already an even number of players. Please wait for a new player to ready up."); // Kj
                        }
                    } else { 
                        // Exact player count off. Player can be moved.
                        moveToSpawns(p);
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

    public void fixHelmet(Player p) {
        PlayerInventory inv = p.getInventory();
        p.sendMessage(ChatColor.RED + "Do not remove your helmet.");
        DyeColor color1 = DyeColor.valueOf(ctp.playerData.get(p).color.toUpperCase());
        ItemStack helmet = new ItemStack(Material.WOOL, 1, (short) color1.getData());

        inv.remove(Material.WOOL);
        p.getInventory().setHelmet(helmet);
        p.updateInventory();
    }
}
