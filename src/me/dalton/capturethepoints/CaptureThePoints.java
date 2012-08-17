package me.dalton.capturethepoints;
import java.io.IOException;
import java.util.logging.Level;
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
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.minecraft.server.Packet;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.minecraft.server.Packet51MapChunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.CraftWorld;

public class CaptureThePoints extends JavaPlugin
{
    public static PermissionHandler Permissions;
    public static Economy economyHandler = null;

    public static boolean UsePermissions;

    /** "plugins/CaptureThePoints" */
    public static final String mainDir = "plugins/CaptureThePoints";

    /** "plugins/CaptureThePoints/CaptureSettings.yml" */
    //public static final File myFile = new File(mainDir + File.separator + "CaptureSettings.yml");
    /** "plugins/CaptureThePoints/Global.yml" */
    public static final File globalConfigFile = new File(mainDir + File.separator + "CaptureSettings.yml");

    public static final Logger logger = Logger.getLogger("Minecraft");

    public static PluginDescriptionFile info = null;

    public static PluginManager pluginManager = null;

    /** List of commands accepted by CTP */
    private static List<CTPCommand> commands = new ArrayList<CTPCommand>(); // Kj

    public final CaptureThePointsBlockListener blockListener = new CaptureThePointsBlockListener(this);

    public final CaptureThePointsEntityListener entityListener = new CaptureThePointsEntityListener(this);

    public final CaptureThePointsPlayerListener playerListener = new CaptureThePointsPlayerListener(this);

    public ArenaRestore arenaRestore = new ArenaRestore(this);
    public MysqlConnector mysqlConnector = new MysqlConnector(this);

    public final HashMap<Player, ItemStack[]> Inventories = new HashMap<Player, ItemStack[]>();

    private HashMap<Player, ItemStack[]> armor = new HashMap<Player, ItemStack[]>();
    //public HashMap<Player, PlayerData> playerData = new HashMap<Player, PlayerData>();

    /** The PlayerData stored by CTP. (HashMap: Player, and their data) */
    public Map<Player, PlayerData> playerData = new ConcurrentHashMap<Player, PlayerData>();  // To avoid concurent modification exceptions    

    /** Player's previous Locations before they started playing CTP. */
    public final HashMap<Player, Location> previousLocation = new HashMap<Player, Location>();

    /** The Lobbies stored by CTP. */
    public List<Lobby> lobbies = new LinkedList<Lobby>();
    // public ConfigOptions configOptions = new ConfigOptions();

    /** The global config options for CTP. */
    public ConfigOptions globalConfigOptions = new ConfigOptions();

    /** The list of arena names stored by CTP. */
    public List<String> arena_list = new LinkedList<String>();

    /** The selected arena for playing. */
    public ArenaData mainArena = new ArenaData();

    /** All arenas boundaries (HashMap: Arena's name, and its boundaries)**/
    //public List<ArenaBoundarys> arenasBoundaries = new LinkedList<ArenaBoundarys>();
    public HashMap<String, ArenaBoundaries> arenasBoundaries = new HashMap<String, ArenaBoundaries>();

    /** The arena currently being edited. */
    public ArenaData editingArena = new ArenaData();

    /** The roles/classes stored by CTP. (HashMap: Role's name, and the Items it contains) 
     * @see Items */
    public HashMap<String, List<Items>> roles = new HashMap<String, List<Items>>();

    /** The list of Healing Items stored by CTP. */
    public List<HealingItems> healingItems = new LinkedList<HealingItems>();

    /** The list of Rewards stored by CTP. */
    public CTPRewards rewards = new CTPRewards();

    /** The timers used by CTP. */
    public CTPScheduler CTP_Scheduler = new CTPScheduler();

    public int arenaRestoreTimesRestored = 0;
    public int arenaRestoreMaxRestoreTimes = 0;
    public int arenaRestoreTimesRestoredSec = 0;   //For second time
    public int arenaRestoreMaxRestoreTimesSec = 0;

    /** Name of the player who needs teleporting. */
    public String playerNameForTeleport = ""; // Block destroy - teleport protection

    //final FileConfiguration config = this.getConfig();
    /** Load from CaptureSettings.yml */
    public FileConfiguration load () { //Yaml Configuration
        return load(globalConfigFile);
    }

    /** Load yml from specified file */
    public FileConfiguration load (File file)
    {
        try
        {
            FileConfiguration PluginPropConfig = YamlConfiguration.loadConfiguration(file);
            return PluginPropConfig;
        } 
        catch (Exception localException)
        {
        }
        return null;
    }

    @Override
    public void onEnable () {
        enableCTP(false);
    }

    public void enableCTP (boolean reloading) {
        if (!reloading)
        {
            setupPermissions();
            setupEconomy();
            pluginManager = getServer().getPluginManager();

            // REGISTER EVENTS-----------------------------------------------------------------------------------
            pluginManager.registerEvents(this.blockListener, this);
            pluginManager.registerEvents(this.entityListener, this);
            pluginManager.registerEvents(this.playerListener, this);

            populateCommands();
        }
        loadConfigFiles();

        // Checks for mysql
        if(this.globalConfigOptions.enableHardArenaRestore)
            mysqlConnector.checkMysqlData();

        //Kj: LobbyActivity timer.
        CTP_Scheduler.lobbyActivity = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run () {
                if (playerData == null) {
                    return;
                }
                if (playerData.isEmpty()) {
                    return;
                }
                if (globalConfigOptions.lobbyKickTime <= 0) {
                    return;
                }

                for (Player player : playerData.keySet()) {
                    PlayerData data = playerData.get(player);
                    if (data.isInLobby && !data.isReady) {
                        // Kj -- Time inactivity warning.
                        if (((System.currentTimeMillis() - data.lobbyJoinTime) >= ((globalConfigOptions.lobbyKickTime * 1000) / 2)) && !data.warnedAboutActivity) {
                            player.sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] Please choose your class and ready up, else you will be kicked from the lobby!");
                            data.warnedAboutActivity = true;
                        }

                        // Kj -- Time inactive in the lobby is greater than the lobbyKickTime specified in config (in ms)
                        if ((System.currentTimeMillis() - data.lobbyJoinTime >= (globalConfigOptions.lobbyKickTime * 1000)) && data.warnedAboutActivity) {
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

        if (!reloading) {
            PluginDescriptionFile pdfFile = getDescription();
            logger.info("[CTP] " + pdfFile.getVersion() + " version is enabled.");
        }
    }

    @Override
    public void onDisable () {
        if (CTP_Scheduler.lobbyActivity != 0) {
            getServer().getScheduler().cancelTask(CTP_Scheduler.lobbyActivity);
            CTP_Scheduler.lobbyActivity = 0;
        }
        clearConfig();
        logger.info("[" + info.getName() + "] Disabled");
        info = null;
        pluginManager = null;
        Permissions = null;
        commands.clear();
    }

    @Override
    public boolean onCommand (CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("ctp")) {
            return true;
        }

        if (commands == null || commands.isEmpty()) { // Really weird bug that rarely occurs. Could call it a sanity check.
            populateCommands();
        }

        List<String> parameters = new ArrayList<String>();
        parameters.add(command.getName());
        parameters.addAll(Arrays.asList(args));

        if (parameters.size() == 1) {
            sendHelp(sender);
            return true;
        }

        for (CTPCommand each : commands) {
            for (String aString : each.aliases) {
                if (aString.equalsIgnoreCase(parameters.get(1))) { // Search the command aliases for the first argument given. If found, execute command.
                    each.execute(sender, parameters);
                    return true;
                }
            }
        }
        
        // Comand not found

        logger.info(sender.getName() + " issued an unknown CTP command. It has " + parameters.size() + " Parameters: " + parameters + ". Displaying help to them.");
        sendHelp(sender);
        return true;
    }

    /** Send the CTP help to this sender */
    public void sendHelp (CommandSender sender) {
        HelpCommand helpCommand = new HelpCommand(this);
        helpCommand.execute(sender, Arrays.asList("ctp"));
    }

    /** Attempt to balance the teams.
     * @param loop Times this has recursed (prevents overruns).
     * @return Whether the teams are balanced. */
    public boolean balanceTeams(int loop, int balanceThreshold) {
        if (loop > 5) {
            logger.warning("balanceTeams hit over 5 recursions. Aborting.");
            return false;
        }
        //int balancethreshold = mainArena.co.balanceTeamsWhenPlayerLeaves; // Get the balance threshold from config. We know this is over 0 already.
        
        Team lowestTeam = null; // Team with the lower number of players
        int lowestmembercount = -1;
        Team highestTeam = null; // Team with the higher number of players
        int highestmembercount = -1;
        
        int difference = 0;

        for (Team aTeam : mainArena.teams) {
            if (lowestmembercount == -1) {
                lowestmembercount = aTeam.memberCount;
                lowestTeam = aTeam;
                highestmembercount = aTeam.memberCount;
                highestTeam = aTeam;
                continue;
            } else {
                if (aTeam.memberCount != lowestmembercount || aTeam.memberCount != highestmembercount) {
                    if (aTeam.memberCount < lowestmembercount) {
                        lowestmembercount = aTeam.memberCount; // Reassign new low
                        lowestTeam = aTeam;
                    } else if (aTeam.memberCount > highestmembercount) {
                        highestmembercount = aTeam.memberCount; // Reassign new high
                        highestTeam = aTeam;
                    } else {
                        continue; // Logic error
                    }
                } else {
                    continue; // These teams are balanced.
                }
            }
        }

        difference = highestmembercount - lowestmembercount;
        if ((highestTeam == lowestTeam) || difference < balanceThreshold) {
            // The difference between the teams is not great enough to balance the teams as defined by balancethreshold.
            return true;
        }

        if (difference % mainArena.teams.size() == 0) {
            // The teams balance evenly.
            balancePlayer(highestTeam.getRandomPlayer(this), lowestTeam); // Move one player from the team with the higher number of players to the lower.
        } else {
            // The teams balance unevenly.
            balancePlayer(highestTeam.getRandomPlayer(this), null); // Move one player from the team with the higher number of players to lobby.
        }
        loop++;
        boolean balanced = balanceTeams(loop, balanceThreshold); // Check Teams again to check if balanced.
        return balanced;
    }

    private void balancePlayer (Player p, Team newTeam) {        
        /*
        //Reseting cooldowns
        for (HealingItems item : healingItems) {
            if (item != null && item.cooldowns != null && item.cooldowns.size() > 0) {
                for (String playName : item.cooldowns.keySet()) {
                    if (playName.equalsIgnoreCase(p.getName())) {
                        item.cooldowns.remove(playName);
                    }
                }
            }
        }
         */

        // Reseting player data       
        if (newTeam == null) {
            // Moving to Lobby
            playerData.get(p).team.memberCount--;
            //playerData.get(p).color = null;
            playerData.get(p).team = null;
            playerData.get(p).isInArena = false;
            playerData.get(p).isInLobby = true;
            mainArena.lobby.playersinlobby.put(p, false);
            playerData.get(p).isReady = false;
            playerData.get(p).justJoined = true; // Flag for teleport
            playerData.get(p).lobbyJoinTime = System.currentTimeMillis();     
            playerData.get(p).warnedAboutActivity = false;
            playerData.get(p).role = null;
            
            // Remove Helmet
            p.getInventory().setHelmet(null);
            p.getInventory().remove(Material.WOOL);
            p.updateInventory();
        
            // Get lobby location and move player to it.
            Location loc = new Location(getServer().getWorld(mainArena.world), mainArena.lobby.x, mainArena.lobby.y + 1, mainArena.lobby.z);
            loc.setYaw((float) mainArena.lobby.dir);
            loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
            p.teleport(loc); // Teleport player to lobby
            Util.sendMessageToPlayers(this, ChatColor.GREEN + p.getName() + ChatColor.WHITE + " was moved to lobby! [Team-balancing]");
            
        } else {
            // Moving to other Team
            String oldteam = playerData.get(p).team.color;
            ChatColor oldcc = playerData.get(p).team.chatcolor;
            
            playerData.get(p).team.memberCount--;
            playerData.get(p).team = newTeam;
            //playerData.get(p).color = newTeam.color;
                                   
            // Change wool colour and Helmet
            ItemStack[] contents = p.getInventory().getContents();
            int amountofwool = 0;
            for (ItemStack item : contents) {
                if (item == null) {
                    continue;
                }
                if (item.getType() == Material.WOOL) {
                    amountofwool += item.getAmount();
                }
            }
            
            p.getInventory().remove(Material.WOOL);
            
            //Give wool
            DyeColor color1 = DyeColor.valueOf(newTeam.color.toUpperCase());
            ItemStack helmet = new ItemStack(Material.WOOL, 1, color1.getData());
            p.getInventory().setHelmet(helmet);
            
            if (amountofwool !=0) {
                ItemStack wool = new ItemStack(Material.WOOL, amountofwool, color1.getData());
                p.getInventory().addItem(wool);
            }

            p.updateInventory();
            
            // Get team spawn location and move player to it.
            Spawn spawn =
                    mainArena.teamSpawns.get(newTeam.color) != null ?
                    mainArena.teamSpawns.get(newTeam.color) :
                    newTeam.spawn;
            Location loc = new Location(getServer().getWorld(mainArena.world), spawn.x, spawn.y, spawn.z);
            loc.setYaw((float) spawn.dir);
            getServer().getWorld(mainArena.world).loadChunk(loc.getBlockX(), loc.getBlockZ());
            boolean teleport = p.teleport(loc);
            if (!teleport) {
                p.teleport(new Location(p.getWorld(), spawn.x, spawn.y, spawn.z, 0.0F, (float)spawn.dir));
            }
            Util.sendMessageToPlayers(this, 
                    newTeam.chatcolor + p.getName() + ChatColor.WHITE + " changed teams from " 
                    + oldcc + oldteam + ChatColor.WHITE + " to "+ newTeam.chatcolor + newTeam.color + ChatColor.WHITE + "! [Team-balancing]");
            newTeam.memberCount++;
        }
    }
    
    /**  
     * Test whether a command sender can use this CTP command.
     * @param sender The sender issuing the command
     * @param notOpCommand Set to true if anyone can use the command, else false if the command issuer has to be an op or CTP admin.
     * @param permissions The permissions to check against that are associated with the command.
     * @return True if sender has permission, else false. 
     */
    public boolean canAccess (CommandSender sender, boolean notOpCommand, String[] permissions) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        } else if (!(sender instanceof Player)) {
            return false;
        } else {
            return canAccess((Player) sender, notOpCommand, permissions);
        }
    }

    /**  
     * Test whether a player can use this CTP command.
     * @param p The player issuing the command
     * @param notOpCommand Set to true if anyone can use the command, else false if the command issuer has to be an op or CTP admin.
     * @param permissions The permissions to check against that are associated with the command.
     * @return True if player has permission, else false. 
     */
    public boolean canAccess (Player p, boolean notOpCommand, String[] permissions) {
        if (permissions == null) {
            return true;
        }

        if (CaptureThePoints.UsePermissions) {
            for (String perm : permissions) {
                if (CaptureThePoints.Permissions.has(p, perm)) {
                    return true;
                }
            }
        } else {
            if (notOpCommand) {
                return true;
            }
            return p.isOp();
        }
        return false;
    }

    public void checkForGameEndThenPlayerLeft () {
        if (this.playerData.size() < 2 && !isPreGame()) {
            //maybe dc or something. it should be moved to cheking to see players who left the game
            boolean zeroPlayers = true;
            for (int i = 0; i < mainArena.teams.size(); i++) {
                if (mainArena.teams.get(i).memberCount == 1) {
                    zeroPlayers = false;
                    Util.sendMessageToPlayers(this, "The game has stopped because there are too few players. "
                            + mainArena.teams.get(i).chatcolor + mainArena.teams.get(i).color.toUpperCase() + ChatColor.WHITE + " wins! (With a final score of "
                            + mainArena.teams.get(i).score + ")");
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

    /** Checks and calculates the Player's killstreak and deathstreak and outputs an appropriate message according to config.
     * @param player The player
     * @param died If they died (false if they were the killer). */
    public void checkForKillMSG (Player player, boolean died) {
        PlayerData data = playerData.get(player);
        if (died) {
            data.deaths++;
            data.deathsInARow++;
            data.killsInARow = 0;
        } else {
            data.kills++;
            data.killsInARow++;
            data.deathsInARow = 0;
            String message = mainArena.co.killStreakMessages.getMessage(data.killsInARow);

            if (!message.isEmpty()) {
                Util.sendMessageToPlayers(this, message.replace("%player", playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE));
            }
        }

        playerData.put(player, data);
    }

    /** Checks whether the current mainArena is fit for purpose.
     * @param p Player doing the checking
     * @return An error message, else empty if the arena is safe. */
    public String checkMainArena (CommandSender sender, ArenaData arena) {
        if (arena == null) {
            // Arenas were loaded but a main arena wasn't selected.
            if (arena_list == null) {
                return "An arena hasn't been built yet.";
            } else if (!arena_list.isEmpty() && arena_list.get(0) != null) {
                String anArena = arena_list.get(0);
                mainArena = loadArena(anArena);
                editingArena = mainArena;
                if (mainArena == null) {
                    return "An arena hasn't been built yet.";
                } else {
                    arena = mainArena;
                }
            }
        }
        if (arena.lobby == null) {
            return "No lobby for main arena " + arena.name + ".";
        }
        if (getServer().getWorld(arena.world) == null) {
            if (canAccess(sender, true, new String[] { "ctp.*", "ctp.admin" })) {
                return "The arena config is incorrect. The world \"" + arena.world + "\" could not be found. Hint: your first world's name is \"" + getServer().getWorlds().get(0).getName() + "\".";
            } else {
                return "Sorry, this arena has not been set up properly. Please tell an admin. [Incorrect World]";
            }
        }
        // Kj -- Test that the spawn points are within the map boundaries
        for (Spawn aSpawn : arena.teamSpawns.values()) {
            if (!playerListener.isInside((int) aSpawn.x, arena.x1, arena.x2) || !playerListener.isInside((int) aSpawn.z, arena.z1, arena.z2)) {
                if (canAccess(sender, true, new String[] { "ctp.*", "ctp.admin" })) {
                    return "The spawn point \"" + aSpawn.name + "\" in the arena \"" + arena.name + "\" is out of the arena boundaries. "
                            + "[Spawn is " + (int) aSpawn.x + ", " + (int) aSpawn.z + ". Boundaries are " + arena.x1 + "<==>" + arena.x2 + ", " + arena.z1 + "<==>" + arena.z2 + "].";
                } else {
                    return "Sorry, this arena has not been set up properly. Please tell an admin. [Incorrect Boundaries]";
                }
            }
        }
        return "";
    }

    /** This method changes the mainArena to a suitable arena using the number of players you have.
     * Note, it will not change the mainArena if useSelectedArenaOnly is set to true.
     * @param numberofplayers The number of players that want to play.
     * @return The name of the selected mainArena, else empty String. */
    public String chooseSuitableArena (int numberofplayers) {
        // Is the config set to allow the random choosing of arenas?
        if (!mainArena.co.useSelectedArenaOnly) {
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
                    mainArena = loadArena(arena_list.get(nextInt)) == null
                            ? mainArena : loadArena(arena_list.get(nextInt)); // Change the mainArena based on this. (Ternary null check)
                }
                logger.info("[CTP] ChooseSuitableArena: Players found: " + numberofplayers + ", total arenas found: " + size + " " + arena_list + ", of which " + arenas.size() + " were suitable: " + arenas);

                // else ctp.mainArena = ctp.mainArena;
            }
            logger.info("[CTP] The selected arena, " + mainArena.name + ", has a minimum of " + mainArena.minimumPlayers + ", and a maximum of " + mainArena.maximumPlayers + ".");
            return mainArena.name;
        }
        return mainArena.name == null ? "" : mainArena.name;
    }

    public void clearConfig () {
        if (this.blockListener.capturegame) {
            this.blockListener.endGame(true);
        }
        if (!this.playerData.isEmpty()) {
            for (Player players : playerData.keySet()) {
                blockListener.restoreThings(players);
                players.sendMessage(ChatColor.RED + "[CTP] Reloading plugin configuration. The CTP game has been terminated.");  // Kj
            }
        }
        clearSchedule();
        arena_list.clear();
        lobbies.clear();
        playerData.clear();
        healingItems.clear();
        rewards = null;
        mainArena = null;
        editingArena = null;
        roles.clear();
    }

    public void clearSchedule () {
        CTP_Scheduler.healingItemsCooldowns = 0;
        CTP_Scheduler.helmChecker = 0;
        CTP_Scheduler.lobbyActivity = 0;
        CTP_Scheduler.money_Score = 0;
        CTP_Scheduler.playTimer = 0;
        CTP_Scheduler.pointMessenger = 0;
        getServer().getScheduler().cancelTasks(this);
    }

//    /** Get the configOptions from this file. */
//    public ConfigOptions getConfigOptions (File arenafile) {
//        return getConfigOptions(load(arenafile));
//    }

    /** Get the configOptions from the config file. */
    public ConfigOptions getConfigOptions (File arenafile)
    {
        setConfigOptions(arenafile);
        FileConfiguration config = load(arenafile);
        
        config.addDefault("Version", 2);
        ConfigOptions co = new ConfigOptions();
        String pointCapture = "";
        String pointCaptureWithScore = "";
        String global = "";
        String mySql = "";
//        boolean updateConfig = false;
//
//        if (config.getProperty("Version") == null) {
//            // old configuration
//            updateConfig = true;
//        } else {
            //version 2
            pointCapture = "GlobalSettings.GameMode.PointCapture.";
            pointCaptureWithScore = "GlobalSettings.GameMode.PointCaptureWithScoreGeneration.";
            global = "GlobalSettings.";
            mySql = "GlobalSettings.MySql.";
//        }
        //Game mode configuration
        co.pointsToWin = config.getInt(pointCapture + "PointsToWin", globalConfigOptions.pointsToWin);
        co.playTime = config.getInt(pointCapture + "PlayTime", globalConfigOptions.playTime);

        // Score mod
        co.useScoreGeneration = config.getBoolean(pointCaptureWithScore + "UseScoreGeneration", globalConfigOptions.useScoreGeneration);
        co.scoreMyltiplier = config.getInt(pointCaptureWithScore + "ScoreMultiplier", globalConfigOptions.scoreMyltiplier);
        if(co.scoreMyltiplier < 1)
        {
            co.scoreMyltiplier = 2;
            config.addDefault(pointCaptureWithScore + "ScoreMultiplier", co.scoreMyltiplier);
        }
        co.scoreToWin = config.getInt(pointCaptureWithScore + "ScoreToWin", globalConfigOptions.scoreToWin);
        co.onePointGeneratedScoreEvery30sec = config.getInt(pointCaptureWithScore + "OnePointGeneratedScoreEvery30sec", globalConfigOptions.onePointGeneratedScoreEvery30sec);
        co.scoreAnnounceTime = config.getInt(pointCaptureWithScore + "ScoreAnnounceTime", globalConfigOptions.scoreAnnounceTime);

        // My sql
        co.mysqlAddress = config.getString(mySql + "Address", globalConfigOptions.mysqlAddress);
        co.mysqlDatabase = config.getString(mySql + "Database", globalConfigOptions.mysqlDatabase);
        co.mysqlPort = config.getInt(mySql + "Port", globalConfigOptions.mysqlPort);
        co.mysqlUser = config.getString(mySql + "User", globalConfigOptions.mysqlUser);
        co.mysqlPass = config.getString(mySql + "Pass", globalConfigOptions.mysqlPass);

        // Global configuration
        // Kj -- documentation for the different options, including their default values, can be found under the ConfigOptions class.
        co.allowBlockBreak = config.getBoolean(global + "AllowBlockBreak", globalConfigOptions.allowBlockBreak);
        co.allowBlockPlacement = config.getBoolean(global + "AllowBlockPlacement", globalConfigOptions.allowBlockPlacement);
        co.allowCommands = config.getBoolean(global + "AllowCommands", globalConfigOptions.allowCommands);
        co.allowDropItems = config.getBoolean(global + "AllowDropItems", globalConfigOptions.allowDropItems);
        co.allowLateJoin = config.getBoolean(global + "AllowLateJoin", globalConfigOptions.allowLateJoin);
        co.autoStart = config.getBoolean(global + "AutoStart", globalConfigOptions.autoStart);
        co.breakingBlocksDropsItems = config.getBoolean(global + "BreakingBlocksDropsItems", globalConfigOptions.breakingBlocksDropsItems);
        co.disableKillMessages = config.getBoolean(global + "DisableKillMessages", globalConfigOptions.disableKillMessages);
        co.dropWoolOnDeath = config.getBoolean(global + "DropWoolOnDeath", globalConfigOptions.dropWoolOnDeath);
        co.enableHardArenaRestore = config.getBoolean(global + "EnableHardArenaRestore", globalConfigOptions.enableHardArenaRestore);
        co.economyMoneyCostForJoiningArena = config.getInt(global + "EconomyMoneyCostForJoiningArena", globalConfigOptions.economyMoneyCostForJoiningArena);
        co.exactTeamMemberCount = config.getBoolean(global + "ExactTeamMemberCount", globalConfigOptions.exactTeamMemberCount);
        co.balanceTeamsWhenPlayerLeaves = config.getInt(global + "BalanceTeamsWhenPlayerLeaves", globalConfigOptions.balanceTeamsWhenPlayerLeaves);
        co.giveNewRoleItemsOnRespawn = config.getBoolean(global + "GiveNewRoleItemsOnRespawn", globalConfigOptions.giveNewRoleItemsOnRespawn);
        co.givenWoolNumber = config.getInt(global + "GivenWoolNumber", 64) <= 0
                ? -1
                : config.getInt(global + "GivenWoolNumber", globalConfigOptions.givenWoolNumber);
        co.lobbyKickTime = config.getInt(global + "LobbyKickTime", globalConfigOptions.lobbyKickTime);
        co.maxPlayerHealth = config.getInt(global + "MaxPlayerHealth", globalConfigOptions.maxPlayerHealth);
        co.moneyAtTheLobby = config.getInt(global + "MoneyAtTheLobby", globalConfigOptions.moneyAtTheLobby);
        co.moneyEvery30Sec = config.getInt(global + "MoneyEvery30sec", globalConfigOptions.moneyEvery30Sec);
        co.moneyForKill = config.getInt(global + "MoneyForKill", globalConfigOptions.moneyForKill);
        co.moneyForPointCapture = config.getInt(global + "MoneyForPointCapture", globalConfigOptions.moneyForPointCapture);
        co.protectionDistance = config.getInt(global + "DamageImmunityNearSpawnDistance", globalConfigOptions.protectionDistance);
        co.ringBlock = config.getInt(global + "RingBlock", globalConfigOptions.ringBlock);
        co.useSelectedArenaOnly = config.getBoolean(global + "UseSelectedArenaOnly", globalConfigOptions.useSelectedArenaOnly);

        KillStreakMessages ksm = new KillStreakMessages();

        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        for (int i = 0; i < 50; i++) {
            if (config.getString("StreakMessage." + i) != null) {
                hm.put(i, config.getString("StreakMessage." + i));
            } else if (!ksm.getMessage(i).isEmpty()) {
                hm.put(i, ksm.getMessage(i));
                config.addDefault("StreakMessage." + i, ksm.getMessage(i));
            }
        }
        co.killStreakMessages = new KillStreakMessages(hm);

        try {
            config.options().copyDefaults(true);
            config.save(arenafile);
        } catch (IOException ex) {
            Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
        }

        return co;
    }

    //Sets config options if they does not exist
    public void setConfigOptions (File arenafile)
    {
        FileConfiguration config = load(arenafile);
        
        String pointCapture = "";
        String pointCaptureWithScore = "";
        String global = "";
        String mySql = "";

        pointCapture = "GlobalSettings.GameMode.PointCapture.";
        pointCaptureWithScore = "GlobalSettings.GameMode.PointCaptureWithScoreGeneration.";
        global = "GlobalSettings.";
        mySql = "GlobalSettings.MySql.";

        //Game mode configuration
        if(!config.contains(pointCapture + "PointsToWin"))
            config.set(pointCapture + "PointsToWin", globalConfigOptions.pointsToWin);
        if(!config.contains(pointCapture + "PlayTime"))
            config.set(pointCapture + "PlayTime", globalConfigOptions.playTime);

        // Score mod
        if(!config.contains(pointCaptureWithScore + "UseScoreGeneration"))
            config.set(pointCaptureWithScore + "UseScoreGeneration", globalConfigOptions.useScoreGeneration);
        if(!config.contains(pointCaptureWithScore + "ScoreMultiplier"))
            config.set(pointCaptureWithScore + "ScoreMultiplier", globalConfigOptions.scoreMyltiplier);
        if(!config.contains(pointCaptureWithScore + "ScoreToWin"))
            config.set(pointCaptureWithScore + "ScoreToWin", globalConfigOptions.scoreToWin);
        if(!config.contains(pointCaptureWithScore + "OnePointGeneratedScoreEvery30sec"))
            config.set(pointCaptureWithScore + "OnePointGeneratedScoreEvery30sec", globalConfigOptions.onePointGeneratedScoreEvery30sec);
        if(!config.contains(pointCaptureWithScore + "ScoreAnnounceTime"))
            config.set(pointCaptureWithScore + "ScoreAnnounceTime", globalConfigOptions.scoreAnnounceTime);

        // My sql
        if(!config.contains(mySql + "Address"))
            config.set(mySql + "Address", globalConfigOptions.mysqlAddress);
        if(!config.contains(mySql + "Database"))
            config.set(mySql + "Database", globalConfigOptions.mysqlDatabase);
        if(!config.contains(mySql + "Port"))
            config.set(mySql + "Port", globalConfigOptions.mysqlPort);
        if(!config.contains(mySql + "User"))
            config.set(mySql + "User", globalConfigOptions.mysqlUser);
        if(!config.contains(mySql + "Pass"))
            config.set(mySql + "Pass", globalConfigOptions.mysqlPass);

        // Global configuration
        if(!config.contains(global + "AllowBlockBreak"))
            config.set(global + "AllowBlockBreak", globalConfigOptions.allowBlockBreak);
        if(!config.contains(global + "AllowBlockPlacement"))
            config.set(global + "AllowBlockPlacement", globalConfigOptions.allowBlockPlacement);
        if(!config.contains(global + "AllowCommands"))
            config.set(global + "AllowCommands", globalConfigOptions.allowCommands);
        if(!config.contains(global + "AllowDropItems"))
            config.set(global + "AllowDropItems", globalConfigOptions.allowDropItems);
        if(!config.contains(global + "AllowLateJoin"))
            config.set(global + "AllowLateJoin", globalConfigOptions.allowLateJoin);
        if(!config.contains(global + "AutoStart"))
            config.set(global + "AutoStart", globalConfigOptions.autoStart);
        if(!config.contains(global + "BreakingBlocksDropsItems"))
            config.set(global + "BreakingBlocksDropsItems", globalConfigOptions.breakingBlocksDropsItems);
        if(!config.contains(global + "DisableKillMessages"))
            config.set(global + "DisableKillMessages", globalConfigOptions.disableKillMessages);
        if(!config.contains(global + "DropWoolOnDeath"))
            config.set(global + "DropWoolOnDeath", globalConfigOptions.dropWoolOnDeath);
        if(!config.contains(global + "EnableHardArenaRestore"))
            config.set(global + "EnableHardArenaRestore", globalConfigOptions.enableHardArenaRestore);
        if(!config.contains(global + "EconomyMoneyCostForJoiningArena"))
            config.set(global + "EconomyMoneyCostForJoiningArena", globalConfigOptions.economyMoneyCostForJoiningArena);
        if(!config.contains(global + "ExactTeamMemberCount"))
            config.set(global + "ExactTeamMemberCount", globalConfigOptions.exactTeamMemberCount);
        if(!config.contains(global + "BalanceTeamsWhenPlayerLeaves"))
            config.set(global + "BalanceTeamsWhenPlayerLeaves", globalConfigOptions.balanceTeamsWhenPlayerLeaves);
        if(!config.contains(global + "GiveNewRoleItemsOnRespawn"))
            config.set(global + "GiveNewRoleItemsOnRespawn", globalConfigOptions.giveNewRoleItemsOnRespawn);
        if(!config.contains(global + "GivenWoolNumber"))
            config.set(global + "GivenWoolNumber", globalConfigOptions.givenWoolNumber);
        if(!config.contains(global + "LobbyKickTime"))
            config.set(global + "LobbyKickTime", globalConfigOptions.lobbyKickTime);
        if(!config.contains(global + "MaxPlayerHealth"))
            config.set(global + "MaxPlayerHealth", globalConfigOptions.maxPlayerHealth);
        if(!config.contains(global + "MoneyAtTheLobby"))
            config.set(global + "MoneyAtTheLobby", globalConfigOptions.moneyAtTheLobby);
        if(!config.contains(global + "MoneyEvery30sec"))
            config.set(global + "MoneyEvery30sec", globalConfigOptions.moneyEvery30Sec);
        if(!config.contains(global + "MoneyForKill"))
            config.set(global + "MoneyForKill", globalConfigOptions.moneyForKill);
        if(!config.contains(global + "MoneyForPointCapture"))
            config.set(global + "MoneyForPointCapture", globalConfigOptions.moneyForPointCapture);
        if(!config.contains(global + "DamageImmunityNearSpawnDistance"))
            config.set(global + "DamageImmunityNearSpawnDistance", globalConfigOptions.protectionDistance);
        if(!config.contains(global + "RingBlock"))
            config.set(global + "RingBlock", globalConfigOptions.ringBlock);
        if(!config.contains(global + "UseSelectedArenaOnly"))
            config.set(global + "UseSelectedArenaOnly", globalConfigOptions.useSelectedArenaOnly);

        try
        {
            config.options().copyDefaults(true);
            config.save(arenafile);
        } 
        catch (IOException ex)
        {
            Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ConfigOptions getArenaConfigOptions (File arenafile)
    {
        setArenaConfigOptions(arenafile);
        FileConfiguration config = load(arenafile);

        ConfigOptions co = new ConfigOptions();

        String pointCapture = "GlobalSettings.GameMode.PointCapture.";
        String pointCaptureWithScore = "GlobalSettings.GameMode.PointCaptureWithScoreGeneration.";
        String global = "GlobalSettings.";

        //Game mode configuration
        co.pointsToWin = config.getInt(pointCapture + "PointsToWin", globalConfigOptions.pointsToWin);
        co.playTime = config.getInt(pointCapture + "PlayTime", globalConfigOptions.playTime);

        // Score mod
        co.useScoreGeneration = config.getBoolean(pointCaptureWithScore + "UseScoreGeneration", globalConfigOptions.useScoreGeneration);
        co.scoreMyltiplier = config.getInt(pointCaptureWithScore + "ScoreMultiplier", globalConfigOptions.scoreMyltiplier);
        if(co.scoreMyltiplier < 1)
        {
            co.scoreMyltiplier = 2;
            config.addDefault(pointCaptureWithScore + "ScoreMultiplier", co.scoreMyltiplier);
        }
        co.scoreToWin = config.getInt(pointCaptureWithScore + "ScoreToWin", globalConfigOptions.scoreToWin);
        co.onePointGeneratedScoreEvery30sec = config.getInt(pointCaptureWithScore + "OnePointGeneratedScoreEvery30sec", globalConfigOptions.onePointGeneratedScoreEvery30sec);
        co.scoreAnnounceTime = config.getInt(pointCaptureWithScore + "ScoreAnnounceTime", globalConfigOptions.scoreAnnounceTime);


        // Global configuration
        // Kj -- documentation for the different options, including their default values, can be found under the ConfigOptions class.
        co.allowBlockBreak = config.getBoolean(global + "AllowBlockBreak", globalConfigOptions.allowBlockBreak);
        co.allowBlockPlacement = config.getBoolean(global + "AllowBlockPlacement", globalConfigOptions.allowBlockPlacement);
        co.allowCommands = globalConfigOptions.allowCommands;
        co.allowDropItems = config.getBoolean(global + "AllowDropItems", globalConfigOptions.allowDropItems);
        co.allowLateJoin = globalConfigOptions.allowLateJoin;
        co.autoStart = globalConfigOptions.autoStart;
        co.breakingBlocksDropsItems = config.getBoolean(global + "BreakingBlocksDropsItems", globalConfigOptions.breakingBlocksDropsItems);
        co.dropWoolOnDeath = config.getBoolean(global + "DropWoolOnDeath", globalConfigOptions.dropWoolOnDeath);
        co.exactTeamMemberCount = config.getBoolean(global + "ExactTeamMemberCount", globalConfigOptions.exactTeamMemberCount);
        co.economyMoneyCostForJoiningArena = config.getInt(global + "EconomyMoneyCostForJoiningArena", globalConfigOptions.economyMoneyCostForJoiningArena);
        co.balanceTeamsWhenPlayerLeaves = config.getInt(global + "BalanceTeamsWhenPlayerLeaves", globalConfigOptions.balanceTeamsWhenPlayerLeaves);
        co.giveNewRoleItemsOnRespawn = config.getBoolean(global + "GiveNewRoleItemsOnRespawn", globalConfigOptions.giveNewRoleItemsOnRespawn);
        co.givenWoolNumber = config.getInt(global + "GivenWoolNumber", 64) <= 0
                ? -1
                : config.getInt(global + "GivenWoolNumber", globalConfigOptions.givenWoolNumber);
        co.lobbyKickTime = globalConfigOptions.lobbyKickTime;
        co.maxPlayerHealth = config.getInt(global + "MaxPlayerHealth", globalConfigOptions.maxPlayerHealth);
        co.moneyAtTheLobby = config.getInt(global + "MoneyAtTheLobby", globalConfigOptions.moneyAtTheLobby);
        co.moneyEvery30Sec = config.getInt(global + "MoneyEvery30sec", globalConfigOptions.moneyEvery30Sec);
        co.moneyForKill = config.getInt(global + "MoneyForKill", globalConfigOptions.moneyForKill);
        co.moneyForPointCapture = config.getInt(global + "MoneyForPointCapture", globalConfigOptions.moneyForPointCapture);
        co.protectionDistance = config.getInt(global + "DamageImmunityNearSpawnDistance", globalConfigOptions.protectionDistance);
        co.ringBlock = globalConfigOptions.ringBlock;
        co.useSelectedArenaOnly = globalConfigOptions.useSelectedArenaOnly;

        KillStreakMessages ksm = new KillStreakMessages();

        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        for (int i = 0; i < 50; i++) {
            if (config.getString("StreakMessage." + i) != null) {
                hm.put(i, config.getString("StreakMessage." + i));
            } else if (!ksm.getMessage(i).isEmpty()) {
                hm.put(i, ksm.getMessage(i));
                config.addDefault("StreakMessage." + i, ksm.getMessage(i));
            }
        }
        co.killStreakMessages = new KillStreakMessages(hm);

        try
        {
            config.options().copyDefaults(true);
            config.save(arenafile);
        }
        catch (IOException ex)
        {
            Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
        }

        return co;
    }

    public void setArenaConfigOptions (File arenafile)
    {
        FileConfiguration config = load(arenafile);
        
        String pointCapture = "GlobalSettings.GameMode.PointCapture.";
        String pointCaptureWithScore = "GlobalSettings.GameMode.PointCaptureWithScoreGeneration.";
        String global = "GlobalSettings.";

        //Game mode configuration
        if(!config.contains(pointCapture + "PointsToWin"))
            config.set(pointCapture + "PointsToWin", globalConfigOptions.pointsToWin);
        if(!config.contains(pointCapture + "PlayTime"))
            config.set(pointCapture + "PlayTime", globalConfigOptions.playTime);

        // Score mod
        if(!config.contains(pointCaptureWithScore + "UseScoreGeneration"))
            config.set(pointCaptureWithScore + "UseScoreGeneration", globalConfigOptions.useScoreGeneration);
        if(!config.contains(pointCaptureWithScore + "ScoreMultiplier"))
            config.set(pointCaptureWithScore + "ScoreMultiplier", globalConfigOptions.scoreMyltiplier);
        if(!config.contains(pointCaptureWithScore + "ScoreToWin"))
            config.set(pointCaptureWithScore + "ScoreToWin", globalConfigOptions.scoreToWin);
        if(!config.contains(pointCaptureWithScore + "OnePointGeneratedScoreEvery30sec"))
            config.set(pointCaptureWithScore + "OnePointGeneratedScoreEvery30sec", globalConfigOptions.onePointGeneratedScoreEvery30sec);
        if(!config.contains(pointCaptureWithScore + "ScoreAnnounceTime"))
            config.set(pointCaptureWithScore + "ScoreAnnounceTime", globalConfigOptions.scoreAnnounceTime);


        // Global configuration
        if(!config.contains(global + "AllowBlockBreak"))
            config.set(global + "AllowBlockBreak", globalConfigOptions.allowBlockBreak);
        if(!config.contains(global + "AllowBlockPlacement"))
            config.set(global + "AllowBlockPlacement", globalConfigOptions.allowBlockPlacement);
        if(!config.contains(global + "AllowDropItems"))
            config.set(global + "AllowDropItems", globalConfigOptions.allowDropItems);
        if(!config.contains(global + "BreakingBlocksDropsItems"))
            config.set(global + "BreakingBlocksDropsItems", globalConfigOptions.breakingBlocksDropsItems);
        if(!config.contains(global + "DropWoolOnDeath"))
            config.set(global + "DropWoolOnDeath", globalConfigOptions.dropWoolOnDeath);
        if(!config.contains(global + "ExactTeamMemberCount"))
            config.set(global + "ExactTeamMemberCount", globalConfigOptions.exactTeamMemberCount);
        if(!config.contains(global + "EconomyMoneyCostForJoiningArena"))
            config.set(global + "EconomyMoneyCostForJoiningArena", globalConfigOptions.economyMoneyCostForJoiningArena);
        if(!config.contains(global + "BalanceTeamsWhenPlayerLeaves"))
            config.set(global + "BalanceTeamsWhenPlayerLeaves", globalConfigOptions.balanceTeamsWhenPlayerLeaves);
        if(!config.contains(global + "GiveNewRoleItemsOnRespawn"))
            config.set(global + "GiveNewRoleItemsOnRespawn", globalConfigOptions.giveNewRoleItemsOnRespawn);
        if(!config.contains(global + "GivenWoolNumber"))
            config.set(global + "GivenWoolNumber", globalConfigOptions.givenWoolNumber);
        if(!config.contains(global + "MaxPlayerHealth"))
            config.set(global + "MaxPlayerHealth", globalConfigOptions.maxPlayerHealth);
        if(!config.contains(global + "MoneyAtTheLobby"))
            config.set(global + "MoneyAtTheLobby", globalConfigOptions.moneyAtTheLobby);
        if(!config.contains(global + "MoneyEvery30sec"))
            config.set(global + "MoneyEvery30sec", globalConfigOptions.moneyEvery30Sec);
        if(!config.contains(global + "MoneyForKill"))
            config.set(global + "MoneyForKill", globalConfigOptions.moneyForKill);
        if(!config.contains(global + "MoneyForPointCapture"))
            config.set(global + "MoneyForPointCapture", globalConfigOptions.moneyForPointCapture);
        if(!config.contains(global + "DamageImmunityNearSpawnDistance"))
            config.set(global + "DamageImmunityNearSpawnDistance", globalConfigOptions.protectionDistance);

        try
        {
            config.options().copyDefaults(true);
            config.save(arenafile);
        }
        catch (IOException ex)
        {
            Logger.getLogger(BuildCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** This method finds if a suitable arena exists.
     * If useSelectedArenaOnly is true, this method will only search the main arena.
     * @param numberofplayers The number of players that want to play.
     * @return If a suitable arena exists, else false. */
    public boolean hasSuitableArena (int numberofplayers) {
        // No arenas built
        if (arena_list == null || arena_list.isEmpty()) {
            return false;
        }
        
        // Is the config set to allow the random choosing of arenas?
        if (!mainArena.co.useSelectedArenaOnly) {
            int size = arena_list.size();
            if (size > 1) {
                // If there is more than 1 arena to choose from
                for (String arena : arena_list) {
                    ArenaData loadArena = loadArena(arena);
                    if (loadArena.maximumPlayers >= numberofplayers && loadArena.minimumPlayers <= numberofplayers) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            if (mainArena.maximumPlayers >= numberofplayers && mainArena.minimumPlayers <= numberofplayers) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean isGameRunning () {
        return this.blockListener.capturegame;
    }

    public boolean isPreGame () {
        return this.blockListener.preGame;
    }

    public void leaveGame (Player player) {
        //On exit we get double sygnal
        if (playerData.get(player) == null) {
            return;
        }
        
        if (playerListener.waitingToMove != null && !playerListener.waitingToMove.isEmpty()) {
            if (player == playerListener.waitingToMove.get(0) && playerListener.waitingToMove.size() == 1)
            {
                playerListener.clearWaitingQueue(); // The player who left was someone in the lobby waiting to join. We need to remove them from the queue
            }
            else
            {
                playerListener.waitingToMove.remove(player);
            }
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
        Util.sendMessageToPlayers(this, player, ChatColor.GREEN + player.getName() + ChatColor.WHITE + " left the CTP game!"); // Won't send to "player".
        
        if (playerData.get(player).team != null) {
            for (int i = 0; i < mainArena.teams.size(); i++) {
                if (mainArena.teams.get(i) == (playerData.get(player).team)) {
                    mainArena.teams.get(i).memberCount--;
                    break;
                }
            }
        }
        this.mainArena.lobby.playersinlobby.remove(player);
        this.blockListener.restoreThings(player);
        this.previousLocation.remove(player);
        this.playerData.remove(player);

        // Check for player replacement if there is somone waiting to join the game
        boolean wasReplaced = false;
        if (mainArena.co.exactTeamMemberCount && isGameRunning())
        {
            for (Player play : playerData.keySet())
            {
                if (playerData.get(play).isInLobby && playerData.get(play).isReady)
                {
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

        //If there was no replacement we should move one member to lobby
        if (!wasReplaced && mainArena.co.exactTeamMemberCount && this.isGameRunning()) {
            if (mainArena.co.balanceTeamsWhenPlayerLeaves > 0) {
                balanceTeams(0, mainArena.co.balanceTeamsWhenPlayerLeaves);
            }
        }
    }

    public void loadArenas (File file)
    {
        if (file.isDirectory())
        {
            String[] internalNames = file.list();
            for (String name : internalNames)
            {
                if (!name.startsWith("."))
                loadArenas(new File(file.getAbsolutePath() + File.separator + name));
            }
        } 
        else
        {
            String fileName = file.getName().split("\\.")[0];
            if (!arena_list.contains(fileName))
            {
                arena_list.add(fileName);
            }
        }
    }

    public void loadConfigFiles ()
    {
        loadRoles();
        loadRewards();
        loadHealingItems();

        //Load existing arenas
        File file = new File(mainDir + File.separator + "Arenas");
        loadArenas(file);

        // Load arenas boundaries
        for(int i = 0; i < arena_list.size(); i++)
        {
            ArenaData tmp = loadArena(arena_list.get(i));
            ArenaBoundaries tmpBound = new ArenaBoundaries();
            //tmpBound.arenaName = tmp.name;
            tmpBound.world = tmp.world;
            tmpBound.x1 = tmp.x1;
            tmpBound.x2 = tmp.x2;
            tmpBound.y1 = tmp.y1;
            tmpBound.y2 = tmp.y2;
            tmpBound.z1 = tmp.z1;
            tmpBound.z2 = tmp.z2;

            arenasBoundaries.put(tmp.name, tmpBound);
        }

        globalConfigOptions = getConfigOptions(globalConfigFile);
        FileConfiguration globalConfig = load();

        String arenaName = globalConfig.getString("Arena");
        if (arenaName == null) {
            mainArena = null;
        } else {
            mainArena = loadArena(arenaName);
        }
        editingArena = mainArena;
        if (mainArena == null)
        {
            globalConfig.set("Arena", null);
            try
            {
                globalConfig.options().copyDefaults(true);
                globalConfig.save(globalConfigFile);
            }
            catch (IOException ex)
            {
                Logger.getLogger(CaptureThePoints.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        CTP_Scheduler.money_Score = 0;
        CTP_Scheduler.playTimer = 0;
        CTP_Scheduler.pointMessenger = 0;
        CTP_Scheduler.helmChecker = 0;
        CTP_Scheduler.lobbyActivity = 0;
        CTP_Scheduler.healingItemsCooldowns = 0;
    }

    /**Loads ArenaData data ready for assignment to mainArena */
    public ArenaData loadArena (String name) {
        ArenaData arena = new ArenaData();

        if (arena_list.contains(name))
        {
            File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + name + ".yml");
            FileConfiguration arenaConf = YamlConfiguration.loadConfiguration(arenaFile);
            
            String world = arenaConf.getString("World");
            
            // Kj -- check the world to see if it exists. 
            try {
                getServer().getWorld(world);
                arena.world = world;
            } catch (Exception ex) {
                logger.warning("[CTP] ### WARNING: " + name + " has an incorrect World. The World in the config, \"" + world + "\", could not be found. ###");
                List<String> worlds = new LinkedList<String>();
                for (World aWorld : getServer().getWorlds()) {
                    worlds.add(aWorld.getName());
                }
                if (worlds.size() == 1) {
                    arena.world = worlds.get(0);
                    logger.info("[CTP] Successfully resolved. \"" + arena.world + "\" will be used.");
                } else {
                    logger.info("[CTP] Could not resolve. Please fix this manually. Hint: Your installed worlds are: " + worlds);
                }
            }

            arena.name = name;
            if(!arenaConf.contains("MaximumPlayers"))
                arenaConf.set("MaximumPlayers", 9999);
            if(!arenaConf.contains("MinimumPlayers"))
                arenaConf.set("MinimumPlayers", 2);

            arena.maximumPlayers = arenaConf.getInt("MaximumPlayers", 9999);
            arena.minimumPlayers = arenaConf.getInt("MinimumPlayers", 2);
            if (arenaConf.contains("Points"))
            {
                for (String str : arenaConf.getConfigurationSection("Points").getKeys(false))
                {
                    CTPPoints tmps = new CTPPoints();
                    tmps.name = str;
                    str = "Points." + str;
                    tmps.x = arenaConf.getInt(str + ".X", 0);
                    tmps.y = arenaConf.getInt(str + ".Y", 0);
                    tmps.z = arenaConf.getInt(str + ".Z", 0);

                    // Load teams that are not allowed to capture
                    String teamColors = arenaConf.getString(str + ".NotAllowedToCaptureTeams");
                    if(teamColors == null)
                    {
                        tmps.notAllowedToCaptureTeams = null;
                    }
                    else
                    {
                        // Trim commas and whitespace, and split items by commas
                        teamColors = teamColors.toLowerCase();
                        teamColors = teamColors.trim();
                        teamColors = teamColors.replaceAll(" ", "");

                        if (teamColors.endsWith(","))
                        {
                            teamColors = teamColors.substring(0, teamColors.length() - 1);
                        }
                        String[] tc = teamColors.split(",");

                        tmps.notAllowedToCaptureTeams.addAll(Arrays.asList(tc));
                    }

                    if (arenaConf.contains(str + ".Dir"))
                    {
                        tmps.pointDirection = arenaConf.getString(str + ".Dir");
                    }
                    arena.capturePoints.add(tmps);
                }
            }
            if (arenaConf.contains("Team-Spawns"))
            {
                for (String str : arenaConf.getConfigurationSection("Team-Spawns").getKeys(false))
                {
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

                    // Check if this spawn is already in the list
                    boolean hasTeam = false;

                    for (Team aTeam : arena.teams) {
                        if (aTeam.color.equalsIgnoreCase(spawn.name))
                        {
                            hasTeam = true;
                            break;
                            //ctp.teams.remove(aTeam);
                        }
                    }

                    if (!hasTeam)
                    {
                        arena.teams.add(team);
                    }
                }
            }
            // Arena boundarys
            arena.x1 = arenaConf.getInt("Boundarys.X1", 0);
            arena.y1 = arenaConf.getInt("Boundarys.Y1", 0);
            arena.z1 = arenaConf.getInt("Boundarys.Z1", 0);
            arena.x2 = arenaConf.getInt("Boundarys.X2", 0);
            arena.y2 = arenaConf.getInt("Boundarys.Y2", 0);
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
                if (!playerListener.isInside((int) aSpawn.x, arena.x1, arena.x2) || !playerListener.isInside((int) aSpawn.z, arena.z1, arena.z2)) {
                    logger.warning("[CTP] ### WARNING: The spawn point \"" + aSpawn.name + "\" in the arena \"" + arena.name + "\" is out of the arena boundaries. ###");
                    continue;
                }
            }

            try
            {
                arenaConf.options().copyDefaults(true);
                arenaConf.save(arenaFile);
            }
            catch (IOException ex)
            {
                Logger.getLogger(CaptureThePoints.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            arena.co = getArenaConfigOptions(arenaFile);

            return arena;
        } else {
            logger.warning("[CTP] Could not load arena! Check your config file and existing arenas");
            return null;
        }
    }

    public void loadHealingItems ()
    {
        FileConfiguration config = load();
        // Healing items loading
        if (!config.contains("HealingItems"))
        {
            config.addDefault("HealingItems.BREAD.HOTHeal", 1);
            config.addDefault("HealingItems.BREAD.HOTInterval", 1);
            config.addDefault("HealingItems.BREAD.Duration", 5);
            config.addDefault("HealingItems.BREAD.Cooldown", 0);
            config.addDefault("HealingItems.BREAD.ResetCooldownOnDeath", false);
            config.addDefault("HealingItems.GOLDEN_APPLE.InstantHeal", 20);
            config.addDefault("HealingItems.GOLDEN_APPLE.Cooldown", 60);
            config.addDefault("HealingItems.GOLDEN_APPLE.ResetCooldownOnDeath", true);
            config.addDefault("HealingItems.GRILLED_PORK.HOTHeal", 1);
            config.addDefault("HealingItems.GRILLED_PORK.HOTInterval", 3);
            config.addDefault("HealingItems.GRILLED_PORK.Duration", 5);
            config.addDefault("HealingItems.GRILLED_PORK.Cooldown", 10);
            config.addDefault("HealingItems.GRILLED_PORK.InstantHeal", 5);
            config.addDefault("HealingItems.GRILLED_PORK.ResetCooldownOnDeath", true);
        }
        int itemNR = 0;
        for (String str : config.getConfigurationSection("HealingItems").getKeys(false))
        {
            itemNR++;
            HealingItems hItem = new HealingItems();
            try
            {
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
        try
        {
            config.options().copyDefaults(true);
            config.save(globalConfigFile);
        }
        catch (IOException ex)
        {
            Logger.getLogger(CaptureThePoints.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadRoles ()
    {
        FileConfiguration config = load();
        if (!config.contains("Roles"))
        {
            config.addDefault("Roles.Tank.Items", "268, 297:16, DIAMOND_CHESTPLATE, 308, 309, SHEARS, CAKE");
            config.addDefault("Roles.Fighter.Items", "272, 297:4, 261, 262:32, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS");
            config.addDefault("Roles.Ranger.Items", "268, 297:6, 261, 262:256, 299, 300, 301");
            config.addDefault("Roles.Berserker.Items", "267, GOLDEN_APPLE:2");
        }
        for (String str : config.getConfigurationSection("Roles").getKeys(false))
        {
            String text = config.getString("Roles." + str + ".Items");

            roles.put(str.toLowerCase(), Util.getItemListFromString(text));
        }
        try
        {
            config.options().copyDefaults(true);
            config.save(globalConfigFile);
        }
        catch (IOException ex)
        {
            Logger.getLogger(CaptureThePoints.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadRewards ()
    {
        FileConfiguration config = load();
        if (!config.contains("Rewards"))
        {
            config.addDefault("Rewards.WinnerTeam.ItemCount", "2");
            config.addDefault("Rewards.WinnerTeam.Items", "DIAMOND_LEGGINGS, DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_BOOTS, DIAMOND_AXE, DIAMOND_HOE, DIAMOND_PICKAXE, DIAMOND_SPADE, DIAMOND_SWORD");
            config.addDefault("Rewards.OtherTeams.ItemCount", "1");
            config.addDefault("Rewards.OtherTeams.Items", "CAKE, RAW_FISH:5, COAL:5, 56, GOLDEN_APPLE");
            config.addDefault("Rewards.ForKillingEnemy", "APPLE, BREAD, ARROW:10");
            config.addDefault("Rewards.ForCapturingThePoint", "CLAY_BRICK, SNOW_BALL:2, SLIME_BALL, IRON_INGOT");
            config.addDefault("Rewards.ExpRewardForKillingOneEnemy", "0");
        }
        rewards = new CTPRewards();
        rewards.expRewardForKillingEnemy = config.getInt("Rewards.ExpRewardForKillingOneEnemy", 0);
        rewards.winnerRewardCount = config.getInt("Rewards.WinnerTeam.ItemCount", 2);
        rewards.winnerRewards = Util.getItemListFromString(config.getString("Rewards.WinnerTeam.Items"));
        rewards.otherTeamRewardCount = config.getInt("Rewards.OtherTeams.ItemCount", 1);
        rewards.loozerRewards = Util.getItemListFromString(config.getString("Rewards.OtherTeams.Items"));
        rewards.rewardsForCapture = Util.getItemListFromString(config.getString("Rewards.ForCapturingThePoint"));
        rewards.rewardsForKill = Util.getItemListFromString(config.getString("Rewards.ForKillingEnemy"));
        try
        {
            config.options().copyDefaults(true);
            config.save(globalConfigFile);
        }
        catch (IOException ex)
        {
            Logger.getLogger(CaptureThePoints.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void moveToLobby (Player player) {
        String mainArenaCheckError = checkMainArena(player, mainArena); // Kj -- Check arena, if there is an error, an error message is returned.
        if (!mainArenaCheckError.isEmpty()) {
            player.sendMessage(mainArenaCheckError);
            return;
        }

        // Some more checks
        if (player.isInsideVehicle()) {
            try {
                player.leaveVehicle();
            } catch (Exception e) {
                // May sometimes reach this if player is riding an entity other than a Minecart
            }
        }
        if (player.isSleeping()) {
            player.kickPlayer("Banned for life... Nah, just don't join from a bed ;)");
            return;
        }

        if (playerData.isEmpty()) {
            mainArena.lobby.playersinlobby.clear();   //Reset if first to come
        }

        // Get lobby location and move player to it.
        Location loc = new Location(getServer().getWorld(mainArena.world), mainArena.lobby.x, mainArena.lobby.y + 1, mainArena.lobby.z);
        loc.setYaw((float) mainArena.lobby.dir);
        if(!loc.getWorld().isChunkLoaded(loc.getChunk()))
        {
            Packet packet = new Packet51MapChunk((int)mainArena.lobby.x - 5, (int)mainArena.lobby.y - 2, (int)mainArena.lobby.z - 5, (int)mainArena.lobby.x + 5, (int)mainArena.lobby.y + 2, (int)mainArena.lobby.z + 5, ((CraftWorld)loc.getWorld()).getHandle().worldProvider.a);
            ((CraftPlayer)player).getHandle().netServerHandler.sendPacket(packet);
        }
        //loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());

        if(economyHandler != null && this.mainArena.co.economyMoneyCostForJoiningArena != 0)
        {
            EconomyResponse r = economyHandler.bankWithdraw(player.getName(), mainArena.co.economyMoneyCostForJoiningArena);
            if(r.transactionSuccess())
            {
                player.sendMessage("[CTP] You were charged " + ChatColor.GREEN + r.amount + ChatColor.WHITE + " for entering " + ChatColor.GREEN + mainArena.name + ChatColor.WHITE + " arena.");
            }
            else
            {
                player.sendMessage("[CTP] You dont have enought money to join arena!");
                return;
            }
        }
        
        // Assign player's PlayerData
        PlayerData data = new PlayerData();
        data.deaths = 0;
        data.deathsInARow = 0;
        data.kills = 0;
        data.killsInARow = 0;
        data.money = mainArena.co.moneyAtTheLobby;
        data.pointCaptures = 0;
        data.isReady = false;
        data.isInArena = false;
        data.foodLevel = player.getFoodLevel();
        data.health = player.getHealth();
        data.lobbyJoinTime = System.currentTimeMillis();
        
        // Store and remove potion effects on player
        data.potionEffects = CTPPotionEffect.storePlayerPotionEffects(player);
        CTPPotionEffect.removeAllEffects(player);
        
        playerData.put(player, data);

        // Save player's previous state 
        player.setFoodLevel(20);
        if (player.getGameMode() == GameMode.CREATIVE) {
            data.isInCreativeMode = true;
            player.setGameMode(GameMode.SURVIVAL);
        }

        mainArena.lobby.playersinlobby.put(player, false); // Kj
        mainArena.lobby.playerswhowereinlobby.add(player); // Kj

        player.setHealth(mainArena.co.maxPlayerHealth);

        Double X = Double.valueOf(player.getLocation().getX());
        Double y = Double.valueOf(player.getLocation().getY());
        Double z = Double.valueOf(player.getLocation().getZ());

        Location previous = new Location(player.getWorld(), X.doubleValue(), y.doubleValue(), z.doubleValue());
        previousLocation.put(player, previous);

        Util.sendMessageToPlayers(this, ChatColor.GREEN + player.getName() + ChatColor.WHITE + " joined a CTP game.");

        // Get lobby location and move player to it.        
        player.teleport(loc); // Teleport player to lobby
        player.sendMessage(ChatColor.GREEN + "Joined CTP lobby " + ChatColor.GOLD + mainArena.name + ChatColor.GREEN + ".");
        playerData.get(player).isInLobby = true;
        saveInv(player);
    }

    /** Add the CTP commands to the master commands list */
    private void populateCommands () {
        commands.clear();
        commands.add(new AliasesCommand(this));
        commands.add(new AutoCommand(this));
        commands.add(new BuildCommand(this));
        commands.add(new ColorsCommand(this));
        commands.add(new DebugCommand(this));
        commands.add(new HelpCommand(this));
        commands.add(new JoinAllCommand(this));
        commands.add(new JoinCommand(this));
        commands.add(new KickCommand(this));
        commands.add(new LateJoinCommand(this));
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

    public void resetArenaList () {
        arena_list.clear();
        //Load existing arenas
        File file = new File(mainDir + File.separator + "Arenas");
        loadArenas(file);
    }

    public void restoreInv (Player player) {
        PlayerInventory PlayerInv = player.getInventory();

        // Just to be sure that inventory is saved
        if (Inventories.get(player) != null) {
            PlayerInv.setContents(this.Inventories.get(player));
            this.Inventories.remove(player);

            PlayerInv.setBoots(this.armor.get(player)[0].getTypeId() == 0 ? null
                    : this.armor.get(player)[0]); // Kj -- removed redundant casts
            PlayerInv.setLeggings(this.armor.get(player)[1].getTypeId() == 0
                    ? null : this.armor.get(player)[1]);
            PlayerInv.setChestplate(this.armor.get(player)[2].getTypeId() == 0
                    ? null : this.armor.get(player)[2]);
            PlayerInv.setHelmet(this.armor.get(player)[3].getTypeId() == 0
                    ? null : this.armor.get(player)[3]);
            this.armor.remove(player);
            player.updateInventory();
        }
    }

    public void saveInv (Player player) {
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

    private void setupPermissions ()
    {
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

    private boolean setupEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            logger.info("[CTP] Vault plugin not detected, disabling economy support.");
            return false;
        }

        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
        {
            economyHandler = economyProvider.getProvider();
        }

        if(economyHandler != null)
            logger.info("[CTP] Vault plugin found, economy support enabled.");

        return economyHandler != null;
    }

}