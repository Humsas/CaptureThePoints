package me.dalton.capturethepoints;

import me.dalton.capturethepoints.listeners.CaptureThePointsPlayerListener;
import me.dalton.capturethepoints.listeners.CaptureThePointsBlockListener;
import me.dalton.capturethepoints.listeners.CaptureThePointsEntityListener;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import me.dalton.capturethepoints.commands.*;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class CaptureThePoints extends JavaPlugin {

    public static PermissionHandler Permissions;
    public static boolean UsePermissions;
    
    /** "plugins/CaptureThePoints" */
    public static final String mainDir = "plugins/CaptureThePoints";
    
    /** "plugins/CaptureThePoints/CaptureSettings.yml" */
    public static final File myfile = new File("plugins/CaptureThePoints" + File.separator + "CaptureSettings.yml");
    
    public static final Logger logger = Logger.getLogger("Minecraft");
    public static PluginDescriptionFile info = null;
    public static PluginManager pluginManager = null;
    
    /** List of commands accepted by CTP */
    private static List<CTPCommand> commands = new ArrayList<CTPCommand>(); // Kj
    
    public final CaptureThePointsBlockListener blockListener = new CaptureThePointsBlockListener(this);
    public final CaptureThePointsEntityListener entityListener = new CaptureThePointsEntityListener(this);
    public final CaptureThePointsPlayerListener playerListener = new CaptureThePointsPlayerListener(this);
    public ArenaRestore arenaRestore = new ArenaRestore(this);
    public final HashMap<Player, ItemStack[]> Inventories = new HashMap<Player, ItemStack[]>();
    private HashMap<Player, ItemStack[]> armor = new HashMap<Player, ItemStack[]>();
    public final HashMap<Player, Integer> health = new HashMap<Player, Integer>();
    //public HashMap<Player, PlayerData> playerData = new HashMap<Player, PlayerData>();
    
    /** The PlayerData stored by CTP. (HashMap: Player, and their data) */
    public Map<Player, PlayerData> playerData = new ConcurrentHashMap<Player, PlayerData>();  // To avoid concurent modification exceptions    
    
    /** The Teams stored by CTP. */
    public List<Team> teams = new LinkedList<Team>();
    
    /** Player's previous Locations before they started playing CTP. */
    public final HashMap<Player, Location> previousLocation = new HashMap<Player, Location>();
    
    /** The Lobbies stored by CTP. */
    public List<Lobby> lobbies = new LinkedList<Lobby>();
    
    /** The config options for CTP. */
    public ConfigOptions configOptions = new ConfigOptions();
    
    /** The list of arena names stored by CTP. */
    public List<String> arena_list = new LinkedList<String>();
    
    /** The selected arena for playing. */
    public ArenaData mainArena = new ArenaData();
    
    /** The arena currently being edited. */
    public String editingArenaName = "";
    
    /** The roles/classes stored by CTP. (HashMap: Role's name, and the Items it contains) 
     * @see Items */
    public HashMap<String, List<Items>> roles = new HashMap<String, List<Items>>();
    
    /** The list of Healing Items stored by CTP. */
    public List<HealingItems> healingItems = new LinkedList<HealingItems>();
    
    /** The list of Rewards stored by CTP. */
    public CTPRewards rewards = new CTPRewards();
    
    /** The timers used by CTP. */
    public CTPScheduler CTP_Scheduler = new CTPScheduler();
    
    /** Name of the player who needs teleporting. */
    public String playerNameForTeleport = ""; // Block destroy - teleport protection
    
    // Arenos issaugojimui -- Arena currently being edited
    public int x1, y1, z1, x2, y2, z2;

    public Configuration load() { //YamlConfiguration
        try {
            Configuration PluginPropConfig = new Configuration(myfile);
            PluginPropConfig.load();
            return PluginPropConfig;
        } catch (Exception localException) {
        }
        return null;
    }

    @Override
    public void onDisable() {
        disableCTP(false);
    }
    
    public void disableCTP(boolean reloading) {
        if (CTP_Scheduler.lobbyActivity != 0) {
            getServer().getScheduler().cancelTask(CTP_Scheduler.lobbyActivity);
            CTP_Scheduler.lobbyActivity = 0;
        }
        clearConfig();
        pluginManager = null;
        Permissions = null;
        if(!reloading) {
            commands.clear();
            logger.info("[" + info.getName() + "] Disabled");
            info = null;
        }
    }
        

    public void clearConfig() {
        if (this.blockListener.capturegame) {
            this.blockListener.endGame(true);
        }
        if (!this.playerData.isEmpty()) {
            for (Player players : playerData.keySet()) {
                blockListener.restoreThings(players);
                players.sendMessage(ChatColor.RED + "[CTP] Server shutting down or reloading. The CTP game has been terminated.");  // Kj
            }
        }
        arena_list.clear();
        lobbies.clear();
        playerData.clear();
        healingItems.clear();
        rewards = null;
        mainArena = null;
        editingArenaName = "";
        teams.clear();
        roles.clear();
    }

    public void resetArenaList() {
        arena_list.clear();
        //Load existing arenas
        File file = new File(mainDir + File.separator + "Arenas");
        loadArenas(file);
    }

    private void setupPermissions() {
        Plugin test = getServer().getPluginManager().getPlugin("Permissions");
        info = getDescription();
        if (Permissions == null) {
            if (test != null) {
                UsePermissions = true;
                Permissions = ((Permissions) test).getHandler();
                logger.info("[CTP] Permissions was found and enabled.");
            } else {
                logger.info("[CTP] Permission system not detected, defaulting to OP");
                UsePermissions = false;
            }
        }
    }

    @Override
    public void onEnable() {
        enableCTP(false);
    }
    
    public void enableCTP(boolean reloading) {
        setupPermissions();
        pluginManager = getServer().getPluginManager();

        // REGISTER EVENTS-----------------------------------------------------------------------------------
        pluginManager.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Highest, this); // Because when game starts you must deal damage to enemy
        pluginManager.registerEvent(Event.Type.PLAYER_MOVE, this.playerListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Event.Priority.Highest, this);
        pluginManager.registerEvent(Event.Type.PLAYER_TELEPORT, this.playerListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Event.Priority.Normal, this);

        PluginDescriptionFile pdfFile = getDescription();

        loadConfigFiles();
        
        if(!reloading) {
            commands.add(new AliasesCommand(this));
            commands.add(new AutoCommand(this));
            commands.add(new BuildCommand(this));
            commands.add(new ColorsCommand(this));
            commands.add(new HelpCommand(this));
            commands.add(new JoinAllCommand(this));
            commands.add(new JoinCommand(this));
            commands.add(new KickCommand(this));
            commands.add(new LeaveCommand(this));
            commands.add(new PJoinCommand(this));
            commands.add(new RejoinCommand(this));
            commands.add(new ReloadCommand(this));
            //commands.add(new SaveCommand(this));
            commands.add(new SelectCommand(this));
            commands.add(new SetpointsCommand(this));
            commands.add(new StartCommand(this));
            commands.add(new StatsCommand(this));
            commands.add(new StopCommand(this));
            commands.add(new TeamCommand(this));
            commands.add(new VersionCommand(this));
        }

        //Kj: LobbyActivity timer.
        CTP_Scheduler.lobbyActivity = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            
            @Override
            public void run() {
                if (playerData == null) {
                    return;
                }
                if (playerData.isEmpty()) {
                    return;
                }
                if (configOptions.lobbyKickTime <= 0) {
                    return;
                }

                for (Player player : playerData.keySet()) {
                    PlayerData data = playerData.get(player);
                    if (data.isInLobby && !data.isReady) {
                        // Kj -- Time inactivity warning.
                        if (((System.currentTimeMillis() - data.lobbyJoinTime) >= ((configOptions.lobbyKickTime * 1000) / 2)) && !data.warnedAboutActivity) {
                            player.sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] Please choose your class and ready up, else you will be kicked from the lobby!");
                            data.warnedAboutActivity = true;
                        }

                        // Kj -- Time inactive in the lobby is greater than the lobbyKickTime specified in config (in ms)
                        if ((System.currentTimeMillis() - data.lobbyJoinTime >= (configOptions.lobbyKickTime * 1000)) && data.warnedAboutActivity) {
                            data.isInLobby = false;
                            data.isInArena = false;
                            data.warnedAboutActivity = false;
                            leaveGame(player);
                            player.sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] You have been kicked from the lobby for not being ready on time.");
                        }
                    }
                }
            }
        }, 200L, 200L); // 10 sec

        if(!reloading) {
            logger.info("[CTP] " + pdfFile.getVersion() + " version is enabled.");
        }
    }

    private void loadArenas(File file) {
        if (file.isDirectory()) {
            String[] internalNames = file.list();
            for (String name : internalNames) {
                loadArenas(new File(file.getAbsolutePath() + File.separator + name));
            }
        } else {
            String fileName = file.getName().split("\\.")[0];
            if (!arena_list.contains(fileName)) {
                arena_list.add(fileName);
            }
        }
    }

    public void loadConfigFiles() {
        loadRoles();
        loadRewards();
        loadHealingItems();

        //Load existing arenas
        File file = new File(mainDir + File.separator + "Arenas");
        loadArenas(file);
        Configuration config = load();
        String arenaName = config.getString("Arena");
        if (arenaName == null) {
            mainArena = null;
        } else {
            mainArena = loadArena(arenaName);
        }
        editingArenaName = "";
        if (mainArena == null) {
            config.removeProperty("Arena");
        }

        // Kj -- documentation for the different options can be found under the configOptions class.
        configOptions.allowBlockPlacement = config.getBoolean("AllowBlockPlacement", true); // Kj
        configOptions.allowCommands = config.getBoolean("AllowCommands", false);
        configOptions.allowLateJoin = config.getBoolean("AllowLateJoin", true); // Kj
        configOptions.autoStart = config.getBoolean("AutoStart", true); // Kj
        configOptions.breakingBlocksDropsItems = config.getBoolean("BreakingBlocksDropsItems", false); // Kj
        configOptions.dropWoolOnDeath = config.getBoolean("DropWoolOnDeath", true); // Kj
//        configOptions.enableHardArenaRestore = config.getBoolean("EnableHardArenaRestore", false);
        configOptions.exactTeamMemberCount = config.getBoolean("ExactTeamMemberCount", false);
        configOptions.giveNewRoleItemsOnRespawn = config.getBoolean("GiveNewRoleItemsOnRespawn", true);
        configOptions.givenWoolNumber = config.getInt("GivenWoolNumber", 64) <= 0 ? -1 : config.getInt("GivenWoolNumber", 64); // Kj
        configOptions.lobbyKickTime = config.getInt("LobbyKickTime", 60); // Kj
        configOptions.maxPlayerHealth = config.getInt("MaxPlayerHealth", 20);
        configOptions.moneyAtTheLobby = config.getInt("MoneyAtTheLobby", 0);
        configOptions.moneyEvery30Sec = config.getInt("MoneyEvery30sec", 100);
        configOptions.moneyForKill = config.getInt("MoneyForKill", 100);
        configOptions.moneyForPointCapture = config.getInt("MoneyForPointCapture", 100);
//        configOptions.mysqlAddress = config.getString("Mysql.Address", "localhost");
//        configOptions.mysqlDatabase = config.getString("Mysql.Database", "");
//        configOptions.mysqlPass = config.getString("Mysql.Pass", "");
//        configOptions.mysqlPort = config.getInt("Mysql.Port", 3306);
//        configOptions.mysqlUser = config.getString("Mysql.User", "root");
        configOptions.onePointGeneratedScoreEvery30sec = config.getInt("OnePointGeneratedScoreEvery30sec", 1);
        configOptions.playTime = config.getInt("PlayTime", 10);
        configOptions.pointsToWin = config.getInt("PointsToWin", 1);
        configOptions.protectionDistance = config.getInt("DamageImmunityNearSpawnDistance", 10);
        configOptions.ringBlock = config.getInt("RingBlock", 7);
        configOptions.scoreAnnounceTime = config.getInt("ScoreAnnounceTime", 30);
        configOptions.scoreToWin = config.getInt("ScoreToWin", 15);
        configOptions.useScoreGeneration = config.getBoolean("UseScoreGeneration", false);
        configOptions.useSelectedArenaOnly = config.getBoolean("UseSelectedArenaOnly", true); // Kj

        config.save();

        CTP_Scheduler.money_Score = 0;
        CTP_Scheduler.playTimer = 0;
        CTP_Scheduler.pointMessenger = 0;
        CTP_Scheduler.helmChecker = 0;
        CTP_Scheduler.lobbyActivity = 0;
        CTP_Scheduler.healingItemsCooldowns = 0;
    }

    //Loads mainArena data
    public ArenaData loadArena(String name) {
        ArenaData arena = new ArenaData();

        if (arena_list.contains(name)) {
            File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + name + ".yml");
            Configuration arenaConf = new Configuration(arenaFile);
            arenaConf.load();
            String world = arenaConf.getString("World");
            
            // Kj -- check the world to see if it exists. 
            if (getServer().getWorld(world) == null) {
                logger.warning("[CTP] ### WARNING: "+name+" has an incorrect World. The World in the config, \""+world+"\", could not be found. ###");
                List<String> worlds = new LinkedList<String>();
                for (World aWorld : getServer().getWorlds()) {
                    worlds.add(aWorld.getName());
                }
                if (worlds.size() == 1) {
                    arena.world = worlds.get(0);
                    logger.info("[CTP] Successfully resolved. \""+arena.world+"\" will be used.");
                } else {
                    logger.info("[CTP] Could not resolve. Please fix this manually. Hint: Your installed worlds are: "+worlds);
                }
            } else {
                arena.world = world;
            }

            arena.name = name;
            arena.maximumPlayers = arenaConf.getInt("MaximumPlayers", 9999); // Kj
            arena.minimumPlayers = arenaConf.getInt("MinimumPlayers", 2); // Kj
            if (arenaConf.getString("Points") != null) {
                for (String str : arenaConf.getKeys("Points")) {
                    CTPPoints tmps = new CTPPoints();
                    tmps.name = str;
                    str = "Points." + str;
                    tmps.x = arenaConf.getInt(str + ".X", 0);
                    tmps.y = arenaConf.getInt(str + ".Y", 0);
                    tmps.z = arenaConf.getInt(str + ".Z", 0);
                    if (arenaConf.getString(str + ".Dir") != null) {
                        tmps.pointDirection = arenaConf.getString(str + ".Dir");
                    }
                    arena.capturePoints.add(tmps);
                }
            }
            if (arenaConf.getString("Team-Spawns") != null) {
                for (String str : arenaConf.getKeys("Team-Spawns")) {
                    Spawn spawn = new Spawn();
                    spawn.name = str;
                    str = "Team-Spawns." + str;
                    spawn.x = arenaConf.getDouble(str + ".X", 0.0D);
                    spawn.y = arenaConf.getDouble(str + ".Y", 0.0D);
                    spawn.z = arenaConf.getDouble(str + ".Z", 0.0D);
                    spawn.dir = arenaConf.getDouble(str + ".Dir", 0.0D);
                    arena.teamSpawns.put(spawn.name, spawn);

                    Team team = new Team();
                    team.spawn = spawn;
                    team.color = spawn.name;
                    team.memberCount = 0;
                    try {
                        team.chatcolor = ChatColor.valueOf(spawn.name.toUpperCase());
                    } catch (Exception ex) {
                        team.chatcolor = ChatColor.GREEN;
                    }

                    // Kj -- copied previous team check here to double check if teams are being duped.
                    // Check if this spawn is already in the list
                    boolean hasTeam = false;

                    for (Team aTeam : teams) {
                        if (aTeam.color.equalsIgnoreCase(spawn.name)) {
                            hasTeam = true;
                            //ctp.teams.remove(aTeam);
                        }
                    }

                    if (!hasTeam) {
                        teams.add(team);
                    }
                }
            }
            // boundarys
            arena.x1 = arenaConf.getInt("Boundarys.X1", 0);
            arena.z1 = arenaConf.getInt("Boundarys.Z1", 0);
            arena.x2 = arenaConf.getInt("Boundarys.X2", 0);
            arena.z2 = arenaConf.getInt("Boundarys.Z2", 0);


            Lobby lobby = new Lobby(
                    arenaConf.getDouble("Lobby.X", 0.0D),
                    arenaConf.getDouble("Lobby.Y", 0.0D),
                    arenaConf.getDouble("Lobby.Z", 0.0D),
                    arenaConf.getDouble("Lobby.Dir", 0.0D));
            arena.lobby = lobby;
            if ((lobby.x == 0.0D) && (lobby.y == 0.0D) && (lobby.z == 0.0D) && (lobby.dir == 0.0D)) {
                arena.lobby = null;
            }
            
            // Kj -- Test that the spawn points are within the map boundaries
            for (Spawn aSpawn : arena.teamSpawns.values()) {
                if (!playerListener.isInside((int)aSpawn.x, arena.x1, arena.x2) || !playerListener.isInside((int)aSpawn.z, arena.z1, arena.z2)) {
                    logger.warning("[CTP] ### WARNING: The spawn point \"" + aSpawn.name + "\" in the arena \"" + arena.name + "\" is out of the arena boundaries. ###");
                    continue;
                }
            }
            return arena;
        } else {
            logger.warning("[CTP] Could not load arena! Check your config file and existing arenas");
            return null;
        }
    }

    public void loadHealingItems() {
        Configuration config = load();
        // Healing items loading
        if (config.getString("HealingItems") == null) {
            config.setProperty("HealingItems.BREAD.HOTHeal", "1");
            config.setProperty("HealingItems.BREAD.HOTInterval", "1");
            config.setProperty("HealingItems.BREAD.Duration", "5");
            config.setProperty("HealingItems.BREAD.Cooldown", "0");
            config.setProperty("HealingItems.BREAD.ResetCooldownOnDeath", "true");
            config.setProperty("HealingItems.GOLDEN_APPLE.InstantHeal", "20");
            config.setProperty("HealingItems.GOLDEN_APPLE.Duration", "5");
            config.setProperty("HealingItems.GOLDEN_APPLE.ResetCooldownOnDeath", "true");
            config.setProperty("HealingItems.GRILLED_PORK.HOTHeal", "1");
            config.setProperty("HealingItems.GRILLED_PORK.HOTInterval", "3");
            config.setProperty("HealingItems.GRILLED_PORK.Duration", "5");
            config.setProperty("HealingItems.GRILLED_PORK.Cooldown", "10");
            config.setProperty("HealingItems.GRILLED_PORK.InstantHeal", "5");
            config.setProperty("HealingItems.GRILLED_PORK.ResetCooldownOnDeath", "true");
        }
        int itemNR = 0;
        for (String str : config.getKeys("HealingItems")) {
            itemNR++;
            HealingItems hItem = new HealingItems();
            try {
                hItem.item = Util.getItemListFromString(str).get(0);
                hItem.instantHeal = config.getInt("HealingItems." + str + ".InstantHeal", 0);
                hItem.hotHeal = config.getInt("HealingItems." + str + ".HOTHeal", 0);
                hItem.hotInterval = config.getInt("HealingItems." + str + ".HOTInterval", 0);
                hItem.duration = config.getInt("HealingItems." + str + ".Duration", 0);
                hItem.cooldown = config.getInt("HealingItems." + str + ".Cooldown", 0);
                hItem.resetCooldownOnDeath = config.getBoolean("HealingItems." + str + ".ResetCooldownOnDeath", true);
            } catch (Exception e) {
                logger.warning("[CTP] Error while loading Healing items! " + itemNR + " item!");
            }

            healingItems.add(hItem);
        }
        config.save();
    }

    public void loadRoles() {
        Configuration config = load();
        if (config.getKeys("Roles") == null) {
            config.setProperty("Roles.Tank.Items", "268, 297:16, DIAMOND_CHESTPLATE, 308, 309, SHEARS, CAKE");
            config.setProperty("Roles.Fighter.Items", "272, 297:4, 261, 262:32, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS");
            config.setProperty("Roles.Ranger.Items", "268, 297:6, 261, 262:256, 299, 300, 301");
            config.setProperty("Roles.Berserker.Items", "267, GOLDEN_APPLE:2");
        }
        for (String str : config.getKeys("Roles")) {
            String text = config.getString("Roles." + str + ".Items");

            roles.put(str.toLowerCase(), Util.getItemListFromString(text));
        }
        config.save();
    }

    public void loadRewards() {
        Configuration config = load();
        if (config.getKeys("Rewards") == null) {
            config.setProperty("Rewards.WinnerTeam.ItemCount", "2");
            config.setProperty("Rewards.WinnerTeam.Items", "DIAMOND_LEGGINGS, DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_BOOTS, DIAMOND_AXE, DIAMOND_HOE, DIAMOND_PICKAXE, DIAMOND_SPADE, DIAMOND_SWORD");
            config.setProperty("Rewards.OtherTeams.ItemCount", "1");
            config.setProperty("Rewards.OtherTeams.Items", "CAKE, RAW_FISH:5, COAL:5, 56, GOLDEN_APPLE");
            config.setProperty("Rewards.ForKillingEnemy", "APPLE, BREAD, ARROW:10");
            config.setProperty("Rewards.ForCapturingThePoint", "CLAY_BRICK, SNOW_BALL:2, SLIME_BALL, IRON_INGOT");
        }
        rewards = new CTPRewards();
        rewards.winnerRewardCount = config.getInt("Rewards.WinnerTeam.ItemCount", 2);
        rewards.winnerRewards = Util.getItemListFromString(config.getString("Rewards.WinnerTeam.Items"));
        rewards.otherTeamRewardCount = config.getInt("Rewards.OtherTeams.ItemCount", 1);
        rewards.loozerRewards = Util.getItemListFromString(config.getString("Rewards.OtherTeams.Items"));
        rewards.rewardsForCapture = Util.getItemListFromString(config.getString("Rewards.ForCapturingThePoint"));
        rewards.rewardsForKill = Util.getItemListFromString(config.getString("Rewards.ForKillingEnemy"));
        config.save();
    }

//  public boolean isDebugging(Player player)
//  {
//    if (this.debugees.containsKey(player))
//    {
//        return ((Boolean)this.debugees.get(player)).booleanValue();
//    }
//    return false;
//  }
//
//  public void setDebugging(Player player, boolean value)
//  {
//    this.debugees.put(player, Boolean.valueOf(value));
//  }
//  public boolean enabled(Player player) {
//    return this.basicUsers.containsKey(player);
//  }
    public void checkForGameEndThenPlayerLeft() {
        if (this.playerData.size() < 2 && !isPreGame()) {
            //maybe dc or something. it should be moved to cheking to see players who left the game
            boolean zeroPlayers = true;
            for (int i = 0; i < teams.size(); i++) {
                if (teams.get(i).memberCount == 1) {
                    zeroPlayers = false;
                    Util.sendMessageToPlayers(this, "The game has stopped because there are too few players. "
                            + teams.get(i).chatcolor + teams.get(i).color.toUpperCase() + ChatColor.WHITE + " wins! (With a final score of "
                            + teams.get(i).score + ")");
                    blockListener.endGame(true);
                    break;
                }
            }
            if (zeroPlayers == true) {
                //getServer().broadcastMessage("[CTP] No players left, game closes!");
                Util.sendMessageToPlayers(this, "No players left. Resetting game.");
                blockListener.endGame(true);
            }
        }
    }

    public void checkForKillMSG(Player player, boolean died) {
        PlayerData data = playerData.get(player);
        if (died) {
            data.deaths++;
            data.deathsInARow++;
            data.killsInARow = 0;
        } else {
            data.kills++;
            data.killsInARow++;
            data.deathsInARow = 0;
            if (data.killsInARow == 2) {
                Util.sendMessageToPlayers(this, playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE + " strikes again!");
            }
            if (data.killsInARow == 3) {
                Util.sendMessageToPlayers(this, playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE + " is on a killing spree!");
            }
            if (data.killsInARow == 4) {
                Util.sendMessageToPlayers(this, playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE + " is on a rampage!");
            }
            if (data.killsInARow == 5) {
                Util.sendMessageToPlayers(this, playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE + " is unstoppable!");
            }
            if (data.killsInARow >= 6) {
                Util.sendMessageToPlayers(this, playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE + " is GOD-LIKE!");
            }
        }

        playerData.put(player, data);
    }

    public void saveInv(Player player) {
        //this.playerInventory.storeInventory(player);
        PlayerInventory PlayerInv = player.getInventory();
        this.Inventories.put(player, PlayerInv.getContents());
        PlayerInv.clear();
        this.armor.put(player, PlayerInv.getArmorContents());
        PlayerInv.setHelmet(null);
        PlayerInv.setChestplate(null);
        PlayerInv.setLeggings(null);
        PlayerInv.setBoots(null);
    }

    public void restoreInv(Player player) {
        PlayerInventory PlayerInv = player.getInventory();

        // Just to be sure that inventory is saved
        if (Inventories.get(player) != null) {
            PlayerInv.setContents(this.Inventories.get(player));
            this.Inventories.remove(player);

            PlayerInv.setBoots(this.armor.get(player)[0].getTypeId() == 0 ? null : this.armor.get(player)[0]); // Kj -- removed redundant casts
            PlayerInv.setLeggings(this.armor.get(player)[1].getTypeId() == 0 ? null : this.armor.get(player)[1]);
            PlayerInv.setChestplate(this.armor.get(player)[2].getTypeId() == 0 ? null : this.armor.get(player)[2]);
            PlayerInv.setHelmet(this.armor.get(player)[3].getTypeId() == 0 ? null : this.armor.get(player)[3]);
            this.armor.remove(player);
            player.updateInventory();
        }
    }

    public boolean isPreGame() {
        return this.blockListener.preGame;
    }

    public boolean isGameRunning() {
        return this.blockListener.capturegame;
    }

    public void leaveGame(Player player) {
        //On exit we get double sygnal
        if (playerData.get(player) == null) {
            return;
        }

        // Removing player cooldowns
        for (HealingItems item : healingItems) {
            if (item != null && item.cooldowns != null && item.cooldowns.size() > 0) {
                for (String playName : item.cooldowns.keySet()) {
                    if (playName.equalsIgnoreCase(player.getName())) {
                        item.cooldowns.remove(playName);
                    }
                }
            }
        }

        for (Player play : playerData.keySet()) {
            if (play == player) {
                continue; //Kj -- don't want a message being sent to an offline player!
            }
            play.sendMessage("[CTP] " + ChatColor.GREEN + player.getName() + ChatColor.WHITE + " left the CTP game!");
        }

        int originalMemberCount = 0;
        if (playerData.get(player).color != null) {
            for (int i = 0; i < teams.size(); i++) {
                if (teams.get(i).color.equalsIgnoreCase(playerData.get(player).color)) {
                    originalMemberCount = teams.get(i).memberCount;
                    teams.get(i).memberCount--;
                    break;
                }
            }
        }
        this.blockListener.restoreThings(player);
        this.previousLocation.remove(player);
        this.health.remove(player);
        this.playerData.remove(player);

        // Check for player replacement if there is somone waiting to join the game
        boolean wasReplaced = false;
        if (configOptions.exactTeamMemberCount && isGameRunning()) {
            for (Player play : playerData.keySet()) {
                if (playerData.get(play).isInLobby && playerData.get(play).isReady) {
                    this.playerListener.moveToSpawns(play);
                    wasReplaced = true;
                    break;
                }
            }
        }

        //check for player count, only then were no replacement
        if (!wasReplaced) {
            checkForGameEndThenPlayerLeft();
        }

        // If there was no replacement we should move one member to lobby
        if (!wasReplaced && configOptions.exactTeamMemberCount && this.isGameRunning()) {
            balanceTeams(originalMemberCount);
        }
    }

    public void balanceTeams(int originalMemberCount) {
        for (Player play : playerData.keySet()) {
            if (playerData.get(play).isInArena && playerData.get(play).team.memberCount == originalMemberCount) {
                playerData.get(play).team.memberCount--;
                //Reseting cooldowns
                for (HealingItems item : healingItems) {
                    if (item != null && item.cooldowns != null && item.cooldowns.size() > 0) {
                        for (String playName : item.cooldowns.keySet()) {
                            if (playName.equalsIgnoreCase(play.getName())) {
                                item.cooldowns.remove(playName);
                            }
                        }
                    }
                }

                // Reseting player data
                playerData.get(play).isInArena = false;
                playerData.get(play).isInLobby = true;
                mainArena.lobby.playersinlobby.put(play, false);
                playerData.get(play).color = null;
                playerData.get(play).isReady = false;
                // Flag for teleport
                playerData.get(play).justJoined = true;
                playerData.get(play).lobbyJoinTime = System.currentTimeMillis();
                playerData.get(play).role = null;
                playerData.get(play).team = null;
                playerData.get(play).warnedAboutActivity = false;

                // Get lobby location and move player to it.
                Location loc = new Location(getServer().getWorld(mainArena.world), mainArena.lobby.x, mainArena.lobby.y + 1, mainArena.lobby.z);
                loc.setYaw((float) mainArena.lobby.dir);
                loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
                play.teleport(loc); // Teleport player to lobby

                Util.sendMessageToPlayers(this, "[CTP] " + ChatColor.GREEN + play.getName() + ChatColor.WHITE + " was moved to lobby!");
            }
        }
    }

    /** 
     * This method changes the mainArena to a suitable arena using the number of players you have.
     * Note, it will not change the mainArena if useSelectedArenaOnly is set to true.
     * @param numberofplayers The number of players that want to play.
     */
    public void chooseSuitableArena(int numberofplayers) {
        // Is the config set to allow the random choosing of arenas?
        if (!configOptions.useSelectedArenaOnly) {

            int size = arena_list.size();

            if (size > 1) {
                // If there is more than 1 arena to choose from
                List<String> arenas = new ArrayList<String>();
                for (String arena : arena_list) {
                    ArenaData loadArena = loadArena(arena);
                    if (loadArena.maximumPlayers >= numberofplayers && loadArena.minimumPlayers <= numberofplayers) {
                        arenas.add(arena);
                        mainArena = loadArena; // Change the mainArena based on this.
                    }
                }
                if (arenas.size() > 1) {
                    Random random = new Random();
                    int nextInt = random.nextInt(size); // Generate a random number between 0 (inclusive) -> Number of arenas (exclusive)
                    mainArena = loadArena(arena_list.get(nextInt)) == null ? mainArena : loadArena(arena_list.get(nextInt)); // Change the mainArena based on this. (Ternary null check)
                }
                logger.info("[CTP] ChooseSuitableArena: Players found: " + numberofplayers + ", total arenas found: " + size + " " + arena_list + ", of which " + arenas.size() + " were suitable: " + arenas);

                // else ctp.mainArena = ctp.mainArena;
            }
            logger.info("[CTP] The selected arena, " + mainArena.name + ", has a minimum of " + mainArena.minimumPlayers + ", and a maximum of " + mainArena.maximumPlayers + ".");
        }
    }

    public void moveToLobby(Player player) {
        String checkMainArena = checkMainArena(player); // Kj -- Check arena, if there is an error, an error message is returned.
        if (!checkMainArena.isEmpty()) {
            player.sendMessage(checkMainArena);
            return;
        }

        // Some more checks
        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }
        if (player.isSleeping()) {
            player.kickPlayer("Banned for life... Nah, just don't join from a bed ;)");
            return;
        }

        if (playerData.isEmpty()) {
            mainArena.lobby.playersinlobby.clear();   //Reset if someone has left
        }
        // Assign player's PlayerData
        PlayerData data = new PlayerData();
        data.deaths = 0;
        data.deathsInARow = 0;
        data.kills = 0;
        data.killsInARow = 0;
        data.money = configOptions.moneyAtTheLobby;
        data.pointCaptures = 0;
        data.isReady = false;
        data.isInLobby = true;
        data.isInArena = false;
        data.foodLevel = player.getFoodLevel();
        data.lobbyJoinTime = System.currentTimeMillis();
        playerData.put(player, data);

        // Save player's previous state 
        player.setFoodLevel(20);
        if (player.getGameMode() == GameMode.CREATIVE) {
            data.isInCreativeMode = true;
            player.setGameMode(GameMode.SURVIVAL);
        }

        health.put(player, Integer.valueOf(player.getHealth()));
        player.setHealth(configOptions.maxPlayerHealth);
        mainArena.lobby.playersinlobby.put(player, false); // Kj

        Double X = Double.valueOf(player.getLocation().getX());
        Double y = Double.valueOf(player.getLocation().getY());
        Double z = Double.valueOf(player.getLocation().getZ());

        Location previous = new Location(player.getWorld(), X.doubleValue(), y.doubleValue(), z.doubleValue());
        previousLocation.put(player, previous);

        Util.sendMessageToPlayers(this, ChatColor.GREEN + player.getName() + ChatColor.WHITE + " joined a CTP game.");

        // Get lobby location and move player to it.        
        Location loc = new Location(getServer().getWorld(mainArena.world), mainArena.lobby.x, mainArena.lobby.y + 1, mainArena.lobby.z);
        loc.setYaw((float) mainArena.lobby.dir);
        loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
        player.teleport(loc); // Teleport player to lobby
        player.sendMessage(ChatColor.GREEN + "Joined CTP lobby " + ChatColor.GOLD + mainArena.name + ChatColor.GREEN + ".");
        saveInv(player);
    }

    /** Checks whether the current mainArena is fit for purpose.
     * @param p Player doing the checking
     * @return An error message, else empty if the arena is safe. */
    public String checkMainArena(Player p) {
        if (arena_list == null) { // Kj -- null checks
             return "Oops, looks like an arena hasn't been built yet.";
        }
        if (arena_list.isEmpty()) {
             return "Oops, looks like an arena hasn't been built yet.";
        }
        if (mainArena == null || mainArena.lobby == null) {
            return "Oops, looks like an arena hasn't been built yet.";
        }
        if (getServer().getWorld(mainArena.world) == null) {
            if (canAccess(p, true, new String[]{"ctp.*", "ctp.admin"})) {
                return "The arena config is incorrect. The world \"" + mainArena.world + "\" could not be found. The world you are currently playing in is \"" + p.getWorld().getName() + "\".";
            } else {
                return "Sorry, this arena has not been set up properly. Please tell an admin. [Incorrect World]";
            }
        }
        // Kj -- Test that the spawn points are within the map boundaries
        for (Spawn aSpawn : mainArena.teamSpawns.values()) {
            if (!playerListener.isInside((int)aSpawn.x, mainArena.x1, mainArena.x2) || !playerListener.isInside((int)aSpawn.z, mainArena.z1, mainArena.z2)) {
                if (canAccess(p, true, new String[]{"ctp.*", "ctp.admin"})) {
                    return "The spawn point \"" + aSpawn.name + "\" in the arena \"" + mainArena.name + "\" is out of the arena boundaries. "
                            + "[Spawn is "+(int)aSpawn.x+", "+(int)aSpawn.z+". Boundaries are "+mainArena.x1+"<==>"+mainArena.x2+", "+mainArena.z1+"<==>"+mainArena.z2+"].";
                } else {
                    return "Sorry, this arena has not been set up properly. Please tell an admin. [Incorrect Boundaries]";
                }
            }
        }
        return "";
    }
    
    public boolean canAccess(Player player, boolean notOpCommand, String... permissions) {
        if (UsePermissions) {
            for (String perm : permissions) {
                if (Permissions.has(player, perm)) {
                    return true;
                }
            }
        } else {
            if (notOpCommand) {
                return true;
            } else {
                return player.isOp();
            }
        }
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("ctp")) {
            return true;
        }

        List<String> parameters = new ArrayList<String>();
        parameters.add(command.getName());
        parameters.addAll(Arrays.asList(args));

        if (parameters.size() == 1) {
            HelpCommand helpCommand = new HelpCommand(this);
            helpCommand.execute(sender, parameters);
            return true;
        }

        for (CTPCommand each : commands) {
            if (each.aliases.contains(parameters.get(1))) { // Search the command aliases for the first argument given. If found, execute command.
                each.execute(sender, parameters);
                return true;
            }
        }
        
        HelpCommand helpCommand = new HelpCommand(this);
        helpCommand.execute(sender, parameters);
        
        return true;
    }
}