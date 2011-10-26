package me.dalton.capturethepoints;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
import java.util.ArrayList;
import java.util.Arrays;
import me.dalton.capturethepoints.commands.*;
import me.dalton.capturethepoints.listeners.CaptureThePointsBlockListener;
import me.dalton.capturethepoints.listeners.CaptureThePointsEntityListener;
import me.dalton.capturethepoints.listeners.CaptureThePointsPlayerListener;

public class CaptureThePoints extends JavaPlugin
{
    public static PermissionHandler Permissions;
    public static boolean UsePermissions;
    public static final String mainDir = "plugins/CaptureThePoints";
    public static final File myfile = new File(mainDir + File.separator + "CaptureSettings.yml");

    public final Logger logger = Logger.getLogger("Minecraft");
    public final CaptureThePointsBlockListener blockListener = new CaptureThePointsBlockListener(this);
    protected final CaptureThePointsEntityListener entityListener = new CaptureThePointsEntityListener(this);
    protected final CaptureThePointsPlayerListener playerListener = new CaptureThePointsPlayerListener(this);
    public ArenaRestore arenaRestore = new ArenaRestore(this);

    private static List<CTPCommand> commands = new ArrayList<CTPCommand>();             // Kj

    public PluginDescriptionFile info = null;
    public PluginManager pluginManager = null;

    public final HashMap<Player, ItemStack[]> Inventories = new HashMap<Player, ItemStack[]>();
    private HashMap<Player, ItemStack[]> armor = new HashMap<Player, ItemStack[]>();

    public final HashMap<Player, Integer> health = new HashMap<Player, Integer>();

    public HashMap<Player, PlayerData> playerData = new HashMap<Player, PlayerData>();

    public List<Team> teams = new LinkedList<Team>();

    public final HashMap<Player, Location> previousLocation = new HashMap<Player, Location>();
    public List<Lobby> lobbies = new LinkedList<Lobby>();               // List of all lobbies
    public ConfigOptions configOptions = new ConfigOptions();

    // arena
    public String selectedArena = "";
    public List<String> arena_list = new LinkedList<String>();
    public ArenaData mainArena = new ArenaData();
    public HashMap<String, List<Items>> roles = new HashMap<String, List<Items>>();
    public CTPRewards rewards = new CTPRewards();

    //timer
    public CTPScheduler CTP_Scheduler = new CTPScheduler();

    // Block destroy - teleport protection
    public String playerNameForTeleport = "";

    // Arenos issaugojimui
    public int x1, y1, z1, x2, y2, z2;
    String pasaulis = "";

  public Configuration load()//YamlConfiguration
  {
        try
        {
          Configuration PluginPropConfig = new Configuration(myfile);
          PluginPropConfig.load();
          return PluginPropConfig;
        }
        catch (Exception localException) {}
    return null;
  }

    @Override
    public void onDisable()
    {
        if (CTP_Scheduler.lobbyActivity != 0)
        {
            getServer().getScheduler().cancelTask(CTP_Scheduler.lobbyActivity);
            CTP_Scheduler.lobbyActivity = 0;
        }
        clearConfig();
        this.logger.info("[" + this.info.getName() + "] Disabled");
        this.info = null;
        this.pluginManager = null;
    }


    public void clearConfig()
    {
        if (this.blockListener.capturegame)
            this.blockListener.endGame(true);
        if(!this.playerData.isEmpty())
            for (Player players : playerData.keySet())
            {
                blockListener.restoreThings(players);
                players.sendMessage(ChatColor.RED + "[CTP] Server shutting down or reloading. The CTP game has been terminated.");  // Kj
            }

        arena_list.clear();
        playerData.clear();
        rewards = new CTPRewards();
        mainArena = null;
        selectedArena = "";
        teams.clear();
        roles.clear();
    }

  private void setupPermissions()
  {
    Plugin test = getServer().getPluginManager().getPlugin("Permissions");
    this.info = getDescription();
    if (Permissions == null)
      if (test != null)
      {
          UsePermissions = true;
          Permissions = ((Permissions)test).getHandler();
          System.out.println("[" + this.info.getName() + "] Permissions was found and enabled.");
      }
      else
      {
          System.out.println("[" + this.info.getName() + "] Permission system not detected, defaulting to OP");
          UsePermissions = false;
      }
  }

    @Override
  public void onEnable()
  {
    setupPermissions();
    PluginManager pm = getServer().getPluginManager();

    // REGISTER EVENTS-----------------------------------------------------------------------------------
    pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Event.Priority.Normal, this);
    pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Normal, this);
    pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Highest, this); // Because when game starts you must deal damage to enemy
    pm.registerEvent(Event.Type.PLAYER_MOVE, this.playerListener, Event.Priority.Normal, this);
    pm.registerEvent(Event.Type.PLAYER_TELEPORT, this.playerListener, Event.Priority.Normal, this);
    pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
    pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
    pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Event.Priority.Normal, this);

    PluginDescriptionFile pdfFile = getDescription();
    //this.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");

    //this.info = getDescription();
    this.pluginManager = getServer().getPluginManager();

    loadConfigFiles();

    commands.add(new AliasesCommand(this));
    commands.add(new BuildCommand(this));
    commands.add(new ColorsCommand(this));
    commands.add(new HelpCommand(this));
    commands.add(new JoinAllCommand(this));
    commands.add(new JoinCommand(this));
    commands.add(new KickCommand(this));
    commands.add(new LeaveCommand(this));
    commands.add(new PJoinCommand(this));
    commands.add(new ReloadCommand(this));
//    commands.add(new SaveCommand(this));
//    commands.add(new SetpointCommand(this));
    commands.add(new SetpointsCommand(this));
    commands.add(new StopCommand(this));
    commands.add(new TeamCommand(this));
    commands.add(new VersionCommand(this));

    //Kj: LobbyActivity timer.
    CTP_Scheduler.lobbyActivity = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
    {
        @Override
        public void run()
        {
            if(configOptions.lobbyKickTime <= 0)
                return;
            
            for (Player player : playerData.keySet())
            {
                PlayerData data = playerData.get(player);
                if (data.isInLobby && !data.isReady)
                {
                    // Kj -- Time inactivity warning.
                    if (((System.currentTimeMillis() - data.lobbyJoinTime) >= ((configOptions.lobbyKickTime * 1000) / 2)) && !data.warnedAboutActivity)
                    {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "[CTP] Please choose your class and ready up, else you will be kicked from the lobby!");
                        data.warnedAboutActivity = true;
                    }

                    // Kj -- Time inactive in the lobby is greater than the lobbykicktime specified in config (in ms)
                    if ((System.currentTimeMillis() - data.lobbyJoinTime >= (configOptions.lobbyKickTime * 1000)) && data.warnedAboutActivity)
                    {
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

    System.out.println("[" + this.info.getName() + "]  " + pdfFile.getVersion() + " version is enabled.");
  }

    private void loadArenas(File file)
    {
        if (file.isDirectory())
        {
            String internalNames[] = file.list();
            for (String name : internalNames)
                loadArenas(new File(file.getAbsolutePath() + File.separator + name));
        }
        else
        {
            String fileName = file.getName().split("\\.")[0];
            if (!arena_list.contains(fileName))
                arena_list.add(fileName);
        }
    }

    public void loadConfigFiles()
    {
        loadRoles();
        loadRewards();
        
        //Load existing arenas
        File file = new File(mainDir + File.separator + "Arenas");
        loadArenas(file);
        Configuration config = load();
        String arenaName = config.getString("Arena");
        if(arenaName == null)
            mainArena = null;
        else
            mainArena = loadArena(arenaName);
        selectedArena = "";
        if(mainArena == null)
            config.removeProperty("Arena");

        configOptions.breakingBlocksDropsItems = config.getBoolean("BreakingBlocksDropsItems", false); // Kj
        configOptions.dropWoolOnDeath = config.getBoolean("DropWoolOnDeath", true); // Kj
//        configOptions.enableHardArenaRestore = config.getBoolean("EnableHardArenaRestore", false);
        configOptions.exactTeamMemberCount = config.getBoolean("ExactTeamMemberCount", false);
        configOptions.giveNewRoleItemsOnRespawn = config.getBoolean("GiveNewRoleItemsOnRespawn", true);
        configOptions.givenWoolNumber = config.getInt("GivenWoolNumber", 64) <= 0 ? -1 : config.getInt("GivenWoolNumber", 64); // Kj
        configOptions.lobbyKickTime = config.getInt("LobbyKickTime", 60); // Kj
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

        config.save();

        CTP_Scheduler.money_Score = 0;
        CTP_Scheduler.playTimer = 0;
        CTP_Scheduler.pointMessenger = 0;
        CTP_Scheduler.helmChecker = 0;
        CTP_Scheduler.lobbyActivity = 0;
    }

    //Loads mainArena data
    public ArenaData loadArena(String name)
    {
        ArenaData arena = new ArenaData();

        if(arena_list.contains(name))
        {
            File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + name + ".yml");
            Configuration arenaConf = new Configuration(arenaFile);
            arenaConf.load();
            arena.world = arenaConf.getString("World");
            arena.name = name;
            if(arenaConf.getString("Points") != null)
                for (String str : arenaConf.getKeys("Points"))
                {
                    CTPPoints tmps = new CTPPoints();
                    tmps.name = str;
                    str = "Points." + str;
                    tmps.x = arenaConf.getInt(str+".X", 0);
                    tmps.y = arenaConf.getInt(str+".Y", 0);
                    tmps.z = arenaConf.getInt(str+".Z", 0);
                    if(arenaConf.getString(str+".Dir") != null)
                        tmps.pointDirection = arenaConf.getString(str+".Dir");
                    arena.capturePoints.add(tmps);
                }
            if(arenaConf.getString("Team-Spawns") != null)
                for (String str : arenaConf.getKeys("Team-Spawns"))
                {
                    CTPPoints tmps = new CTPPoints();
                    tmps.name = str;
                    str = "Team-Spawns." + str;
                    tmps.x = arenaConf.getDouble(str+".X", 0);
                    tmps.y = arenaConf.getDouble(str+".Y", 0);
                    tmps.z = arenaConf.getDouble(str+".Z", 0);
                    tmps.dir = arenaConf.getDouble(str+".Dir", 0);
                    arena.teamSpawns.put(tmps.name, tmps);

                    Team team = new Team();
                    team.color = tmps.name;
                    team.memberCount = 0;
                    try
                    {
                        team.chatcolor = ChatColor.valueOf(tmps.name.toUpperCase()); 
                    }
                    catch (Exception ex)
                    {
                        team.chatcolor = ChatColor.GREEN;
                    }
                    teams.add(team);
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
            if ((lobby.x == 0.0D) && (lobby.y == 0.0D) && (lobby.z == 0.0D) && (lobby.dir == 0.0D))
                arena.lobby = null;

            return arena;
        }
        else
        {
            System.out.println("[" + this.info.getName() + "] Could not load arena! Check your config file and existing arenas");
            return null;
        }
    }

    public void loadRoles()
    {
        Configuration config = load();
        if (config.getKeys("Roles") == null)
        {
            config.setProperty("Roles.Tank.Items", "268, 297:16, DIAMOND_CHESTPLATE, 308, 309, SHEARS, CAKE");
            config.setProperty("Roles.Fighter.Items", "272, 297:4, 261, 262:32, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS");
            config.setProperty("Roles.Ranger.Items", "268, 297:6, 261, 262:256, 299, 300, 301");
            config.setProperty("Roles.Berserker.Items", "267, GOLDEN_APPLE:2");
        }
        for (String str : config.getKeys("Roles"))
        {
            String text = config.getString("Roles." + str + ".Items");

            roles.put(str.toLowerCase(), Util.getItemListFromString(text));
        }
        config.save();
    }

    public void loadRewards()
    {
        Configuration config = load();
        if (config.getKeys("Rewards") == null)
        {
            config.setProperty("Rewards.WinnerTeam.ItemCount", "2");
            config.setProperty("Rewards.WinnerTeam.Items", "DIAMOND_LEGGINGS, DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_BOOTS, DIAMOND_AXE, DIAMOND_HOE, DIAMOND_PICKAXE, DIAMOND_SPADE, DIAMOND_SWORD");
            config.setProperty("Rewards.OtherTeams.ItemCount", "1");
            config.setProperty("Rewards.OtherTeams.Items", "CAKE, RAW_FISH:5, COAL:5, 56, GOLDEN_APPLE");
            config.setProperty("Rewards.ForKillingEnemy", "APPLE, BREAD, ARROW:10");
            config.setProperty("Rewards.ForCapturingThePoint", "CLAY_BRICK, SNOW_BALL:2, SLIME_BALL, IRON_INGOT");
        }
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

    public void checkForGameEndThenPlayerLeft()
    {
        if(this.playerData.size() < 2 && !isPreGame())
        {
            //maybe dc or something. it should be moved to cheking to see players who left the game
            boolean zeroPlayers = true;
            for(int i = 0; i < teams.size(); i++)
            {
                if(teams.get(i).memberCount == 1)
                {
                    zeroPlayers = false;
                    Util.sendMessageToPlayers(this, "The game has stopped because there are too few players. "
                            + teams.get(i).chatcolor + teams.get(i).color.toUpperCase() + ChatColor.WHITE + " wins! (With a final score of "
                            + teams.get(i).score + ")");
                    blockListener.endGame(true);
                    break;
                }
            }
            if(zeroPlayers == true)
            {
                //getServer().broadcastMessage("[CTP] No players left, game closes!");
                Util.sendMessageToPlayers(this, "No players left. Resetting game.");
                blockListener.endGame(true);
            }
        }
    }

    public void checkForKillMSG(Player player, boolean died)
    {
        PlayerData data = playerData.get(player);
        if(died)
        {
            data.deaths++;
            data.deathsInARow++;
            data.killsInARow = 0;
        }
        else
        {
            data.kills++;
            data.killsInARow++;
            data.deathsInARow = 0;
            if (data.killsInARow == 2)
            {
                Util.sendMessageToPlayers(this, playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE + " strikes again!");
            }
            if (data.killsInARow == 3)
            {
                Util.sendMessageToPlayers(this, playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE + " is on a killing spree!");
            }
            if (data.killsInARow == 4)
            {
                Util.sendMessageToPlayers(this, playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE + " is on a rampage!");
            }
            if (data.killsInARow == 5)
            {
                Util.sendMessageToPlayers(this, playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE + " is unstoppable!");
            }
            if (data.killsInARow >= 6)
            {
                Util.sendMessageToPlayers(this, playerData.get(player).team.chatcolor + player.getName() + ChatColor.WHITE + " is GOD-LIKE!");
            }
        }

        playerData.put(player, data);
    }

    private boolean canAccess(Player player, boolean notOpCommand, String...permissions)
    {
        if(UsePermissions)
        {
            for (String perm : permissions )
            {
                if(Permissions.has(player, perm))
                    return true;
            }
        }
        else
        {
            if(notOpCommand)
                return true;
            else
                return player.isOp();
        }

        return false;
    }



    public void saveInv(Player player)
    {
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

  public void restoreInv(Player player)
  {
    PlayerInventory PlayerInv = player.getInventory();

    // Just to be sure that inventory is saved
    if((ItemStack[])this.Inventories.get(player) != null)
    {
        PlayerInv.setContents((ItemStack[])this.Inventories.get(player));
        this.Inventories.remove(player);

        PlayerInv.setBoots(((ItemStack[])this.armor.get(player))[0].getTypeId() == 0 ? null : ((ItemStack[])this.armor.get(player))[0]);
        PlayerInv.setLeggings(((ItemStack[])this.armor.get(player))[1].getTypeId() == 0 ? null : ((ItemStack[])this.armor.get(player))[1]);
        PlayerInv.setChestplate(((ItemStack[])this.armor.get(player))[2].getTypeId() == 0 ? null : ((ItemStack[])this.armor.get(player))[2]);
        PlayerInv.setHelmet(((ItemStack[])this.armor.get(player))[3].getTypeId() == 0 ? null : ((ItemStack[])this.armor.get(player))[3]);
        this.armor.remove(player);
        player.updateInventory();
    }
  }

  public boolean isPreGame()
  {
    return this.blockListener.preGame;
  }

    public boolean isGameRunning()
    {
        return this.blockListener.capturegame;
    }

    public void leaveGame(Player player)
    {
        //I6einant pareina dvigubas signalas, tai jo nepriimam
        if(playerData.get(player) == null)
            return;

        for (Player play : playerData.keySet())
        {
            if (play == player)
            {
                continue; //Kj -- don't want a message being sent to an offline player!
            }
            play.sendMessage("[CTP] " + ChatColor.GREEN + player.getName() + ChatColor.WHITE + " left the CTP game!");
        }
        
        if(playerData.get(player).color != null)
            for(int i = 0; i < teams.size(); i++)
            {
                if(teams.get(i).color.equalsIgnoreCase(playerData.get(player).color))
                {
                    teams.get(i).memberCount--;
                    break;
                }
            }

        this.blockListener.restoreThings(player);
        this.previousLocation.remove(player);
        this.health.remove(player);
        this.playerData.remove(player);

        // Check for player replacement if there is somone waiting to join the game
        if(configOptions.exactTeamMemberCount && isGameRunning())
        {
            for(Player play : playerData.keySet())
            {
                if(playerData.get(play).isInLobby && playerData.get(play).isReady)
                {
                    this.playerListener.moveToSpawns(play);
                    break;
                }
            }
        }

        //check for player count
        checkForGameEndThenPlayerLeft();
    }


    public void moveToLobby(Player player)
    {
        //some more checks
        if (player.isInsideVehicle())
            player.leaveVehicle();
        if (player.isSleeping())
        {
            player.kickPlayer("Banned for life... Nah, just don't join from a bed ;)");
            return;
        }
        Util.sendMessageToPlayers(this, ChatColor.GREEN + player.getName() + ChatColor.WHITE + " joined a CTP game.");
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
        player.setFoodLevel(20);
        if(player.getGameMode() == GameMode.CREATIVE)
        {
            data.isInCreativeMode = true;
            player.setGameMode(GameMode.SURVIVAL);
        }

        playerData.put(player, data);
        health.put(player, Integer.valueOf(player.getHealth()));
        player.setHealth(20);

        mainArena.lobby.playersinlobby.put(player, false); // Kj

        Double X = Double.valueOf(player.getLocation().getX());
        Double y = Double.valueOf(player.getLocation().getY());
        Double z = Double.valueOf(player.getLocation().getZ());

        Location previous = new Location(player.getWorld(), X.doubleValue(), y.doubleValue(), z.doubleValue());
        previousLocation.put(player, previous);
        Location loc = new Location(getServer().getWorld(mainArena.world), mainArena.lobby.x, mainArena.lobby.y + 1, mainArena.lobby.z);
        loc.setYaw((float)mainArena.lobby.dir);
        loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
        player.teleport(loc);
        saveInv(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!command.getName().equalsIgnoreCase("ctp"))
        {
            return true;
        }

        List<String> parameters = new ArrayList<String>();
        parameters.add(command.getName());
        parameters.addAll(Arrays.asList(args));

        if (parameters.size() == 1)
        {
            HelpCommand helpCommand = new HelpCommand(this);
            helpCommand.execute(sender, parameters);
            return true;
        }

        for (CTPCommand each : commands)
        {
            if (each.aliases.contains(parameters.get(1)))  // Search the command aliases for the first argument given. If found, execute command.
            {
                each.execute(sender, parameters);
                return true;
            }
        }
        return true;
    }
}

//    @Override
//  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
//  {
//    Player playa = (Player)sender;
//    if (cmd.getName().equalsIgnoreCase("ctp"))
//    {
//        // if no arguments
//      String subc = "";
//      try{
//             subc = args[0];
//      }
//      catch (java.lang.ArrayIndexOutOfBoundsException e){
//            playa.sendMessage(ChatColor.WHITE + "Type " + ChatColor.GREEN + "/ctp help " + ChatColor.WHITE + "to see commands and their usage");
//            return true;
//      }
//
//        subc = subc.toLowerCase();
//        String arg = null;
//        try
//        {
//            arg = args[1];
//        }
//        catch (Exception e) {
//            arg = null;
//        }
//
//        if (subc.equals("help"))
//        {
//            playa.sendMessage(ChatColor.RED + "Commands:");
//            if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.stop"))
//                playa.sendMessage(ChatColor.GREEN + "/ctp stop  " + ChatColor.WHITE + "- stops allready running game");
//            if (canAccess(playa, true, "ctp.*", "ctp.play", "ctp.admin"))
//                playa.sendMessage(ChatColor.GREEN + "/ctp join" + ChatColor.WHITE + "- join the game");
//            if (canAccess(playa, true, "ctp.*", "ctp.play", "ctp.admin"))
//                playa.sendMessage(ChatColor.GREEN + "/ctp leave " + ChatColor.WHITE + "- leave the game");
//            if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.setpoints"))
//                playa.sendMessage(ChatColor.GREEN + "/ctp setpoints <Team color> <number> " + ChatColor.WHITE + "- Sets the chosen teams points/score to this number");
//            if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.pjoin"))
//                playa.sendMessage(ChatColor.GREEN + "/ctp pjoin <player>" + ChatColor.WHITE + "- makes this player join the game");
//            if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.kick"))
//                playa.sendMessage(ChatColor.GREEN + "/ctp kick <player>" + ChatColor.WHITE + "- kicks player from the game");
//            if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.joinall"))
//                playa.sendMessage(ChatColor.GREEN + "/ctp joinall" + ChatColor.WHITE + "- makes all players join the game");
//            if (canAccess(playa, true, "ctp.*", "ctp.play", "ctp.admin"))
//                playa.sendMessage(ChatColor.GREEN + "/ctp colors " + ChatColor.WHITE + "- awailable colors list");
//            if (canAccess(playa, true, "ctp.*", "ctp.play", "ctp.admin"))
//                playa.sendMessage(ChatColor.GREEN + "/ctp build help " + ChatColor.WHITE + "- arena editing commands list");
//            if (canAccess(playa, true, "ctp.*", "ctp.admin", "ctp.admin.reload"))
//                playa.sendMessage(ChatColor.GREEN + "/ctp reload " + ChatColor.WHITE + "- reload CTP config files");
//
//            return true;
//        }
//
//        if (subc.equals("stop"))
//        {
//            if (canAccess(playa, false, "ctp.*", "ctp.admin.stop", "ctp.admin"))
//            {
//                getServer().broadcastMessage("The Capture The Points game has ended.");
//                this.blockListener.endGame(true);
//                return true;
//            }
//            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//            return true;
//        }
//
//        if (subc.equals("version"))
//        {
//            if (playa.getName().equalsIgnoreCase("Humsas"))
//            {
//                playa.sendMessage("CTP version: " + ChatColor.GREEN + getDescription().getVersion());
//                return true;
//            }
//            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//            return true;
//        }
//
//        if (subc.equals("setpoints"))
//        {
//            if (canAccess(playa, false, "ctp.*", "ctp.admin.setpoints", "ctp.admin"))
//            {
//                //bahhhh it can be simpler
//                String arg2 = null;
//                try{
//                    arg2 = args[2];
//                }
//                catch (Exception e) {
//                    arg2 = null;
//                }
//
//                if (arg == null || arg2 == null)
//                {
//                    playa.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp setpoints <Team color> <number>");
//                    return true;
//                }
//
//                int points = 0;
//                try{points = Integer.parseInt(arg2);}
//                catch (Exception NumberFormatException)
//                {
//                    playa.sendMessage(ChatColor.RED + "Incorect number format. Usage: " + ChatColor.GREEN + "/ctp setpoints <Team color> <number>");
//                    return true;
//                }
//
//                if(configOptions.useScoreGeneration)
//                {
//                    for(Team team: team_member_count)
//                        if(team.color.equalsIgnoreCase(arg))
//                            team.score = points;
//                    blockListener.didSomeoneWin();
//                }
//                else
//                {
//                    for(Team team: team_member_count)
//                        if(team.color.equalsIgnoreCase(arg))
//                            team.controledPoints = points;
//                    blockListener.didSomeoneWin();
//                }
//
//                //No such color
//                playa.sendMessage(ChatColor.RED + "There is no such color!");
//                return true;
//            }
//            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//            return true;
//        }
//
//        if (subc.equals("join"))
//            if (canAccess(playa, true, "ctp.*", "ctp.play", "ctp.admin"))
//            {
//                if (!this.blockListener.isAlreadyInGame(playa))
//                {
//                    if(mainArena == null)
//                    {
//                        playa.sendMessage(ChatColor.RED + "Please create an arena first");
//                        return true;
//                    }
//                    if(mainArena.lobby == null)
//                    {
//                        playa.sendMessage(ChatColor.RED + "Please create arena lobby");
//                        return true;
//                    }
//
//                    moveToLobby(playa);
//                    return true;
//                }
//                playa.sendMessage(ChatColor.RED + "You are already playing game!");
//                return true;
//            }
//            else
//            {
//                sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//        if(subc.equals("joinall"))
//        {
//            if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.joinall"))
//            {
//                for(Player player:getServer().getOnlinePlayers())
//                {
//                    if (!this.blockListener.isAlreadyInGame(player))
//                    {
//                        if(mainArena == null)
//                        {
//                            playa.sendMessage(ChatColor.RED + "Please create an arena first");
//                            return true;
//                        }
//                        if(mainArena.lobby == null)
//                        {
//                            playa.sendMessage(ChatColor.RED + "Please create arena lobby");
//                            return true;
//                        }
//
//                        moveToLobby(player);
//                    }
//                }
//                return true;
//            }
//            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//            return true;
//        }
//
//        if (subc.equals("pjoin"))
//        {
//            if (canAccess(playa, false, "ctp.*", "ctp.admin.pjoin", "ctp.admin"))
//            {
//                if(mainArena == null)
//                {
//                    playa.sendMessage(ChatColor.RED + "Please create an arena first");
//                    return true;
//                }
//                if(mainArena.lobby == null)
//                {
//                    playa.sendMessage(ChatColor.RED + "Please create arena lobby");
//                    return true;
//                }
//                Player bob = getServer().getPlayer(arg);
//                if(bob == null)
//                {
//                    playa.sendMessage("Player " + ChatColor.RED + arg + ChatColor.WHITE + " is currently offline!");
//                    return true;
//                }
//                if (!this.blockListener.isAlreadyInGame(bob))
//                {
//                    bob.sendMessage(ChatColor.GREEN + playa.getName() + ChatColor.WHITE + " forced you to join CTP!");
//                    moveToLobby(bob);
//                }
//                else
//                {
//                    playa.sendMessage(ChatColor.RED + "This player is already playing CTP!");
//                }
//                return true;
//            }
//            playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//            return true;
//        }
//
//        if (subc.equals("kick"))
//        {
//            if (canAccess(playa, false, "ctp.*", "ctp.admin.kick", "ctp.admin"))
//            {
//                if(mainArena == null)
//                {
//                    playa.sendMessage(ChatColor.RED + "Please create an arena first");
//                    return true;
//                }
//                if(mainArena.lobby == null)
//                {
//                    playa.sendMessage(ChatColor.RED + "Please create arena lobby");
//                    return true;
//                }
//                Player bob = getServer().getPlayer(arg);
//                if(bob == null)
//                {
//                    playa.sendMessage("Player " + ChatColor.RED + arg + ChatColor.WHITE + " is currently offline!");
//                    return true;
//                }
//                if(this.blockListener.isAlreadyInGame(bob))
//                {
//                    bob.sendMessage(ChatColor.GREEN + playa.getName() + ChatColor.WHITE + " kicked you from CTP game!");
//                    leaveGame(bob);
//                }
//                else
//                {
//                    playa.sendMessage(ChatColor.RED + "This player is not playing CTP!");
//                }
//                return true;
//            }
//            playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//            return true;
//        }
//
//        if (subc.equals("leave"))
//        {
//            if (canAccess(playa, true, "ctp.*", "ctp.play", "ctp.admin"))
//            {
//                // check if player is in CTP game
//                if (!this.blockListener.isAlreadyInGame(playa))
//                {
//                    playa.sendMessage(ChatColor.RED + "You are not in the game!");
//                    return true;
//                }
//
//                leaveGame(playa);
//
//                return true;
//            }
//            playa.sendMessage("You do not have permission to do that.");
//            return true;
//        }
//
//        if (subc.equals("reload"))
//        {
//            if (canAccess(playa, true, "ctp.*", "ctp.admin", "ctp.admin.reload"))
//            {
//                clearConfig();
//                loadConfigFiles();
//                playa.sendMessage("[CTP] successfully reloaded!");
//
//                return true;
//            }
//            playa.sendMessage("You do not have permission to do that.");
//            return true;
//        }
//
//        if (subc.equals("colors"))
//        {
//            if (canAccess(playa, true, "ctp.*", "ctp.admin"))
//            {
//                playa.sendMessage(ChatColor.BLUE + "Awailable team colors:");
//                playa.sendMessage(ChatColor.GREEN + "WHITE, LIGHTGRAY, GRAY, BLACK, RED, ORANGE, YELLOW, LIME, LIGHTBLUE, GREEN, CYAN, BLUE, PURPLE, MAGENTA, PINK, BROWN");
//            }
//            if (canAccess(playa, true, "ctp.play"))
//            {
//                if(team_member_count.size() > 0)
//                {
//                    String teams = "";
//                    for(int i = 0; i < team_member_count.size(); i++)
//                        teams = teams + team_member_count.get(i).color + " ";
//
//                    playa.sendMessage("Existing team colors to join: " + ChatColor.GREEN + teams.toLowerCase());
//                    return true;
//                }
//                else
//                {
//                    playa.sendMessage(ChatColor.BLUE + "There are no existing teams to join.");
//                    return true;
//                }
//            }
//            playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//            return true;
//        }
//
//        if (subc.equals("build"))
//        {
//            if(arg == null)
//            {
//                playa.sendMessage(ChatColor.GREEN + "/ctp build help " + ChatColor.WHITE + "- arena editing commands list");
//                return true;
//            }
//            arg = arg.toLowerCase();
//
//            if (arg.equals("help"))
//            {
//                playa.sendMessage(ChatColor.RED + "Build commands:");
//                if (canAccess(playa, false, "ctp.*", "ctp.admin.setspawn", "ctp.admin"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build setspawn <Team color> " + ChatColor.WHITE + "- sets the place people are teleported to when they die or when they join the game");
//                if (canAccess(playa, false, "ctp.*", "ctp.admin.removespawn", "ctp.admin"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build removespawn <Team color> " + ChatColor.WHITE + "- removes spawn point of selected color");
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.setpoint"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build setpoint <Point name> <vert | hor> " + ChatColor.WHITE + "- creates new capture point");
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.removepoint"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build removepoint <Point name> " + ChatColor.WHITE + "- removes an existing capture point");
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.create"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build create <Arena name> " + ChatColor.WHITE + "- creates an arena");
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.delete"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build delete <Arena name> " + ChatColor.WHITE + "- deletes an existing arena");
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.selectarena"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build selectarena <Arena name> " + ChatColor.WHITE + "- selects arena for editing");
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.setarena"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build setarena <Arena name> " + ChatColor.WHITE + "- sets main arena for playing");
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.setlobby"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build setlobby " + ChatColor.WHITE + "- sets arena lobby");
//                if(canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.arenalist"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build arenalist " + ChatColor.WHITE + "- shows existing arenas list");
//                if(canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.pointlist"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build pointlist " + ChatColor.WHITE + "- shows selected arena capture points list");
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.setboundary"))
//                    playa.sendMessage(ChatColor.GREEN + "/ctp build setboundary <1 | 2> " + ChatColor.WHITE + "- sets boundary (1 or 2) of the arena");
//                return true;
//            }
//
//
//            if (arg.equals("setspawn"))
//            {
//                if (canAccess(playa, false, "ctp.*", "ctp.admin.setspawn", "ctp.admin"))
//                {
//                    if (args.length < 3)
//                    {
//                        playa.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setspawn <Team color> ");
//                        return true;
//                    }
//                    args[2] = args[2].toLowerCase();
//                    if(selectedArena.equals(""))
//                    {
//                        playa.sendMessage(ChatColor.RED + "No arena selected!");
//                        return true;
//                    }
//
//                    Location loc = playa.getLocation();
//
//                    File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + selectedArena + ".yml");
//                    Configuration arenaConf = new Configuration(arenaFile);
//                    arenaConf.load();
//
//                    if(!(arenaConf.getString("World") == null) )
//                        if(!arenaConf.getString("World").equals(playa.getWorld().getName()))
//                        {
//                            playa.sendMessage(ChatColor.RED + "Please build all arena team spawns in the same world ---->"+ ChatColor.GREEN +arenaConf.getString("World"));
//                            return true;
//                        }
//
//                    if ((args[2].equals("white")) ||
//                        (args[2].equals("lightgray")) ||
//                        (args[2].equals("gray")) ||
//                        (args[2].equals("black")) ||
//                        (args[2].equals("red")) ||
//                        (args[2].equals("orange")) ||
//                        (args[2].equals("yellow")) ||
//                        (args[2].equals("lime")) ||
//                        (args[2].equals("green")) ||
//                        (args[2].equals("blue")) ||
//                        (args[2].equals("cyan")) ||
//                        (args[2].equals("lightblue")) ||
//                        (args[2].equals("purple")) ||
//                        (args[2].equals("pink")) ||
//                        (args[2].equals("magenta")) ||
//                        (args[2].equals("brown")))
//                    {
//                        CTPPoints tmps = new CTPPoints();
//                        tmps.name = args[2];
//                        tmps.x = Double.valueOf(loc.getX());
//                        tmps.y = Double.valueOf(loc.getY());
//                        tmps.z = Double.valueOf(loc.getZ());
//                        tmps.dir = loc.getYaw();
//
//                        String aWorld = arenaConf.getString("World");
//                        if(aWorld == null)
//                            arenaConf.setProperty("World", playa.getWorld().getName());
//                        else if(!aWorld.equals(playa.getWorld().getName()))
//                        {
//                            playa.sendMessage(ChatColor.RED + "Please build arena lobby in same world as its spawns and capture points!");
//                            return true;
//                        }
//                        arenaConf.setProperty("Team-Spawns." + args[2] + ".X", Double.valueOf(loc.getX()));
//                        arenaConf.setProperty("Team-Spawns." + args[2] + ".Y", Double.valueOf(loc.getY()));
//                        arenaConf.setProperty("Team-Spawns." + args[2] + ".Z", Double.valueOf(loc.getZ()));
//                        arenaConf.setProperty("Team-Spawns." + args[2] + ".Dir", tmps.dir);
//                        arenaConf.save();
//
//                        if(mainArena.world == null)
//                        {
//                            //mainArena = new ArenaData();
//                            mainArena.world = playa.getWorld().getName();
//                            mainArena.name = selectedArena;
//                        }
//                        if(mainArena.world.equals(playa.getWorld().getName()))
//                        {
//                            mainArena.teamSpawns.put(args[2], tmps);
//                            Team tmp = new Team();
//                            tmp.color = args[2];
//                            tmp.memberCount = 0;
//
//                            boolean hasTeam = false;
//                            // Check if this spawn is already in the list
//                            for(Team tm: team_member_count)
//                            {
//                                if(tm.color.equalsIgnoreCase(args[2]))
//                                {
//                                    hasTeam = true;
//                                    //team_member_count.remove(tm);
//                                }
//                            }
//                            if(!hasTeam)
//                                team_member_count.add(tmp);
//                        }
//
//                        playa.sendMessage("You set the " + ChatColor.GREEN +  args[2] + ChatColor.WHITE +  " team spawn point.");
//                        return true;
//                    }
//                    // If there is no such color
//                    playa.sendMessage(ChatColor.RED + "There is no such color!");
//                    return true;
//                }
//                sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//
//            if (arg.equals("removespawn"))
//            {
//                if (canAccess(playa, false, "ctp.*", "ctp.admin.removespawn", "ctp.admin"))
//                {
//                    if (args.length < 3)
//                    {
//                        playa.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build removespawn <Team color> ");
//                        return true;
//                    }
//                    args[2] = args[2].toLowerCase();
//
//                    if(selectedArena.equals(""))
//                    {
//                        playa.sendMessage(ChatColor.RED + "No arena selected!");
//                        return true;
//                    }
//
//                    File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + selectedArena + ".yml");
//                    Configuration arenaConf = new Configuration(arenaFile);
//                    arenaConf.load();
//                    if(arenaConf.getString("Team-Spawns." + args[2] + ".X") == null)
//                    {
//                        playa.sendMessage(ChatColor.RED + "This arena spawn does not exist! -----> " + ChatColor.GREEN + args[2]);
//                        return true;
//                    }
//                    arenaConf.removeProperty("Team-Spawns." + args[2]);
//                    arenaConf.save();
//                    if(selectedArena.equalsIgnoreCase(mainArena.name))
//                        mainArena.teamSpawns.remove(args[2]);
//
//                    for(int i = 0; i < team_member_count.size(); i++)
//                        if(team_member_count.get(i).color.equals(args[2]))
//                        {
//                            team_member_count.remove(i);
//                            break;
//                        }
//
//                    playa.sendMessage(ChatColor.GREEN + args[2] + " " +  ChatColor.WHITE + "spawn was removed.");
//                    return true;
//                }
//                playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//            if(arg.equals("setpoint"))
//            {
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.setpoint"))
//                {
//                    if(args.length < 4)
//                    {
//                        playa.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setpoint <Point name> <vert | hor>");
//                        return true;
//                    }
//                    args[2] = args[2].toLowerCase();
//                    args[3] = args[3].toLowerCase();
//
//                    if(!(args[3].equals("vert") || args[3].equals("hor")))
//                    {
//                        playa.sendMessage(ChatColor.RED + "Points can be vertical or horizontal: " + ChatColor.GREEN + "vert | hor");
//                        return true;
//                    }
//
//                    if(selectedArena.equals(""))
//                    {
//                        playa.sendMessage(ChatColor.RED + "No arena selected!");
//                        return true;
//                    }
//                    CTPPoints tmps = new CTPPoints();
//                    tmps.name = args[2];
//                    Location loc = playa.getLocation();
//                    int start_x, start_y, start_z;
//                    tmps.x = start_x = loc.getBlockX();
//                    tmps.y = start_y = loc.getBlockY();
//                    tmps.z = start_z = loc.getBlockZ();
//
//                    ArenaData arena = loadArena(selectedArena);
//                    File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + selectedArena + ".yml");
//                    Configuration arenaConf = new Configuration(arenaFile);
//                    arenaConf.load();
//                    if(!(arenaConf.getString("World") == null) )
//                        if(!arenaConf.getString("World").equals(playa.getWorld().getName()))
//                        {
//                            playa.sendMessage(ChatColor.RED + "Please build all arena points in same world ----> "+ ChatColor.GREEN +arenaConf.getString("World"));
//                            return true;
//                        }
//
//                    for(CTPPoints point: arena.capturePoints)
//                    {
//                        Location protectionPoint = new Location(playa.getWorld(), point.x, point.y, point.z);
//                        double distance = playa.getLocation().distance(protectionPoint);
//                        if(distance < 5)
//                        {
//                            playa.sendMessage(ChatColor.RED + "You are trying to build to close to another point!");
//                            return true;
//                        }
//                    }
//
//                    if(args[3].equals("vert"))
//                    {
//                        BlockFace direction;
//                        double yaw = loc.getYaw();
//
//                        while (yaw < 0) yaw += 360;
//                        if ((yaw > 315) || (yaw <= 45)) direction = BlockFace.WEST;
//                        else if ((yaw > 45) && (yaw <= 135)) direction = BlockFace.NORTH;
//                        else if ((yaw > 135) && (yaw <= 225)) direction = BlockFace.EAST;
//                        else direction = BlockFace.SOUTH;
//
//                        switch (direction)
//                        {
//                            case NORTH:
//                                Util.buildVert(playa, start_x, start_y-1, start_z-1, 2, 4, 4, configOptions.ringBlock);
//                                playa.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x, start_y+1, start_z).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x, start_y, start_z+1).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x, start_y+1 ,start_z+1).setTypeId(0);
//                                arenaConf.setProperty("Points." + args[2] + ".Dir", "NORTH");
//                                tmps.pointDirection = "NORTH";
//                                break;
//                            case EAST:
//                                Util.buildVert(playa, start_x-1, start_y-1, start_z, 4, 4, 2, configOptions.ringBlock);
//                                playa.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x, start_y+1, start_z).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x+1, start_y, start_z).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x+1, start_y+1 ,start_z).setTypeId(0);
//                                arenaConf.setProperty("Points." + args[2] + ".Dir", "EAST");
//                                tmps.pointDirection = "EAST";
//                                break;
//                            case SOUTH:
//                                Util.buildVert(playa, start_x-1, start_y-1, start_z-1, 2, 4, 4, configOptions.ringBlock);
//                                playa.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x, start_y+1, start_z).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x, start_y, start_z+1).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x, start_y+1 ,start_z+1).setTypeId(0);
//                                arenaConf.setProperty("Points." + args[2] + ".Dir", "SOUTH");
//                                tmps.pointDirection = "SOUTH";
//                                break;
//                            case WEST:
//                                Util.buildVert(playa, start_x-1, start_y-1, start_z-1, 4, 4, 2, configOptions.ringBlock);
//                                playa.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x, start_y+1, start_z).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x+1, start_y, start_z).setTypeId(0);
//                                playa.getWorld().getBlockAt(start_x+1, start_y+1 ,start_z).setTypeId(0);
//                                arenaConf.setProperty("Points." + args[2] + ".Dir", "WEST");
//                                tmps.pointDirection = "WEST";
//                                break;
//                        }
//
//                    }
//
//                    if(args[3].equals("hor"))
//                    {
//                        for(int x = start_x+2; x >= start_x-1; x--)
//                            for(int y = start_y-1; y <= start_y; y++)
//                                for(int z = start_z-1; z <= start_z+2; z++)
//                                    playa.getWorld().getBlockAt(x,y,z).setTypeId(configOptions.ringBlock);
//                        playa.getWorld().getBlockAt(start_x,start_y,start_z).setTypeId(0);
//                        playa.getWorld().getBlockAt(start_x+1,start_y,start_z).setTypeId(0);
//                        playa.getWorld().getBlockAt(start_x+1,start_y,start_z+1).setTypeId(0);
//                        playa.getWorld().getBlockAt(start_x,start_y,start_z+1).setTypeId(0);
//                    }
//
//                    String aWorld = arenaConf.getString("World");
//                    if(aWorld == null)
//                        arenaConf.setProperty("World", playa.getWorld().getName());
//                    else if(!aWorld.equals(playa.getWorld().getName()))
//                    {// it should never get here
//                        playa.sendMessage(ChatColor.RED + "Please build arena lobby in same world as its spawns and capture points!");
//                        return true;
//                    }
//                    arenaConf.setProperty("Points." + args[2] + ".X", tmps.x);
//                    arenaConf.setProperty("Points." + args[2] + ".Y", tmps.y);
//                    arenaConf.setProperty("Points." + args[2] + ".Z", tmps.z);
//                    arenaConf.save();
//
//                    if(mainArena.world == null)
//                    {
//                        //mainArena = new ArenaData();
//                        mainArena.world = playa.getWorld().getName();
//                        mainArena.name = selectedArena;
//                    }
//
//                    if(mainArena.world.equals(playa.getWorld().getName()))
//                        mainArena.capturePoints.add(tmps);
//
//                    playa.sendMessage(ChatColor.WHITE + "You created capture point -----> " + ChatColor.GREEN + args[2]);
//                    return true;
//                }
//                sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//            if(arg.equals("removepoint"))
//            {
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.removepoint"))
//                {
//                    if(args.length < 3)
//                    {
//                        playa.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build removepoint <Point name>");
//                        return true;
//                    }
//                    args[2] = args[2].toLowerCase();
//                    if(selectedArena.equals(""))
//                    {
//                        playa.sendMessage(ChatColor.RED + "No arena selected!");
//                        return true;
//                    }
//
//                    File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + selectedArena + ".yml");
//                    Configuration arenaConf = new Configuration(arenaFile);
//                    arenaConf.load();
//                    if(arenaConf.getString("Points." + args[2] + ".X") == null)
//                    {
//                        playa.sendMessage(ChatColor.RED + "This arena point does not exist! -----> " + ChatColor.GREEN + args[2]);
//                        return true;
//                    }
//                    if(arenaConf.getKeys("Points").size() == 1 && arenaConf.getKeys("Team-Spawns") == null)
//                        arenaConf.removeProperty("World");
//
//                    int start_x = arenaConf.getInt("Points." + args[2] + ".X", 0);
//                    int start_y = arenaConf.getInt("Points." + args[2] + ".Y", 0);
//                    int start_z = arenaConf.getInt("Points." + args[2] + ".Z", 0);
//
//                    if(mainArena.name.equals(playa.getWorld().getName()))
//                        for(CTPPoints s: mainArena.capturePoints)
//                            if(s.name.equalsIgnoreCase(args[2]))
//                            {
//                                mainArena.capturePoints.remove(s);
//                                break;
//                            }
//
//                    //Remove blocks
//                    if(arenaConf.getString("Points." + args[2] + ".Dir") == null)
//                    {
//                        for(int x = start_x+2; x >= start_x-1; x--)
//                            for(int y = start_y-1; y <= start_y; y++)
//                                for(int z = start_z-1; z <= start_z+2; z++)
//                                    if(playa.getWorld().getBlockAt(x,y,z).getTypeId() == configOptions.ringBlock)
//                                        playa.getWorld().getBlockAt(x,y,z).setTypeId(0);
//                    }
//                    else
//                    {
//                        String direction = arenaConf.getString("Points." + args[2] + ".Dir");
//                        Util.removeVertPoint(playa, direction, start_x, start_y, start_z, configOptions.ringBlock);
//                    }
//
//                    arenaConf.removeProperty("Points." + args[2]);
//                    arenaConf.save();
//                    playa.sendMessage(ChatColor.WHITE + "You removed capture point -----> " + ChatColor.GREEN + args[2]);
//
//                    return true;
//                }
//                playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//            if(arg.equals("create"))
//            {
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.create"))
//                {
//                    if(args.length < 3)
//                    {
//                        playa.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build create <Arena name>");
//                        return true;
//                    }
//                    args[2] = args[2].toLowerCase();
//                    if(arena_list.contains(args[2]))
//                    {
//                        playa.sendMessage(ChatColor.RED + "This arena alredy exists! -----> " + ChatColor.GREEN + args[2]);
//                        return true;
//                    }
//                    File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + args[2] + ".yml");
//                    Configuration arenaConf = new Configuration(arenaFile);
//                    arenaConf.save();
//                    selectedArena = args[2];
//                    Configuration config = load();
//                    //Seting main arena if this is first arena
//                    String arena = (String)(config.getProperty("Arena"));
//                    if(arena == null)
//                    {
//                        config.setProperty("Arena", args[2]);
//                        config.save();
//
//                        mainArena = new ArenaData();
//                        mainArena.name = args[2];
//                        mainArena.world = null;
//                    }
//
//                    arena_list.add(args[2]);
//                    playa.sendMessage("You created arena: " + ChatColor.GREEN + args[2]);
//
//                    return true;
//                }
//                playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//            if(arg.equals("delete"))
//            {
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.delete"))
//                {
//                    if(args.length < 3)
//                    {
//                        playa.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build delete <Arena name>");
//                        return true;
//                    }
//                    args[2] = args[2].toLowerCase();
//                    if(!arena_list.contains(args[2]))
//                    {
//                        playa.sendMessage(ChatColor.RED + "This arena does not exist! -----> " + ChatColor.GREEN + args[2]);
//                        return true;
//                    }
//                    if(isGameRunning() && mainArena.name.equals(args[2]))
//                    {
//                        playa.sendMessage(ChatColor.RED + "Cannot delete arena while game is running in it!");
//                        return true;
//                    }
//                    File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + args[2] + ".yml");
//
//                    //Remove blocks
//                    Configuration arenaConf = new Configuration(arenaFile);
//                    arenaConf.load();
//                    Configuration config = load();
//
//                    if(arenaConf.getString("Points") != null)
//                        for (String str : arenaConf.getKeys("Points"))
//                        {
//                            str = "Points." + str;
//                            int start_x = arenaConf.getInt(str+".X", 0);
//                            int start_y = arenaConf.getInt(str+".Y", 0);
//                            int start_z = arenaConf.getInt(str+".Z", 0);
//
//                            if(arenaConf.getString(str + ".Dir") == null)
//                            {
//                                for(int x = start_x+2; x >= start_x-1; x--)
//                                    for(int y = start_y-1; y <= start_y; y++)
//                                        for(int z = start_z-1; z <= start_z+2; z++)
//                                            if(playa.getWorld().getBlockAt(x,y,z).getTypeId() == configOptions.ringBlock)
//                                                playa.getWorld().getBlockAt(x,y,z).setTypeId(0);
//                            }
//                            else
//                            {
//                                String direction = arenaConf.getString(str + ".Dir");
//                                Util.removeVertPoint(playa, direction, start_x, start_y, start_z, configOptions.ringBlock);
//                            }
//                        }
//
//                    arenaFile.delete();
//                    arena_list.remove(args[2]);
//                    if(args[2].equals(mainArena.name))
//                    {
//                        mainArena = null;
//                        team_member_count.clear();
//                        config.removeProperty("Arena");
//                        config.save();
//                    }
//                    if(args[2].equals(selectedArena))
//                        selectedArena = "";
//
//                    playa.sendMessage("You deleted arena: " + ChatColor.GREEN + args[2]);
//
//                    return true;
//                }
//                playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//            //  sets arena for editing/creating
//            if(arg.equals("selectarena"))
//            {
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.selectarena"))
//                {
//                    if(args.length < 3)
//                    {
//                        playa.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build selectarena <Arena name>");
//                        return true;
//                    }
//                    args[2] = args[2].toLowerCase();
//                    if(!arena_list.contains(args[2]))
//                    {
//                        playa.sendMessage(ChatColor.RED + "This arena does not exist! -----> " + ChatColor.GREEN + args[2]);
//                        return true;
//                    }
//                    selectedArena = args[2];
//                    playa.sendMessage(ChatColor.WHITE + "Arena selected for editing: " + ChatColor.GREEN + args[2]);
//
//                    return true;
//                }
//                playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//            if(arg.equals("setarena"))
//            {
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.setarena"))
//                {
//                    if(args.length < 3)
//                    {
//                        playa.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setarena <Arena name>");
//                        return true;
//                    }
//                    args[2] = args[2].toLowerCase();
//                    if(!arena_list.contains(args[2]))
//                    {
//                        playa.sendMessage(ChatColor.RED + "This arena does not exist! -----> " + ChatColor.GREEN + args[2]);
//                        return true;
//                    }
//
//                    ArenaData arena = loadArena(args[2]);
//                    boolean canLoad = true;
//                    if(arena.capturePoints.size() < 1)
//                    {
//                        playa.sendMessage(ChatColor.RED + "Please add at least one capture point");
//                        canLoad = false;
//                    }
//                    if(arena.teamSpawns.size() < 2)
//                    {
//                        playa.sendMessage(ChatColor.RED + "Please add at least two teams spawn points");
//                        canLoad = false;
//                    }
//                    if(arena.lobby == null)
//                    {
//                        playa.sendMessage(ChatColor.RED + "Please create arena lobby");
//                        canLoad = false;
//                    }
//                    if((arena.x1 == 0) && (arena.x2 == 0) && (arena.z1 == 0) && (arena.z2 == 0))
//                    {
//                        playa.sendMessage(ChatColor.RED + "Please set arena boundaries");
//                        canLoad = false;
//                    }
//
//                    if(canLoad)
//                    {
//                        Configuration config = load();
//                        config.setProperty("Arena", args[2]);
//                        config.save();
//                        mainArena = null;
//                        mainArena = arena;
//                        // And to be sure that everything is fine reload all config
//                        clearConfig();
//                        loadConfigFiles();
//
//                        playa.sendMessage(ChatColor.WHITE + "Arena selected for playing: " + ChatColor.GREEN + args[2]);
//                    }
//
//                    return true;
//                }
//                playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//
//            if(arg.equals("setlobby"))
//            {
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.setlobby"))
//                {
//                    if(selectedArena.equals(""))
//                    {
//                        playa.sendMessage(ChatColor.RED + "No arena selected!");
//                        return true;
//                    }
//                    File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + selectedArena + ".yml");
//
//                    Configuration arenaConf = new Configuration(arenaFile);
//                    arenaConf.load();
//                    String aWorld = arenaConf.getString("World");
//                    if(aWorld == null)
//                        arenaConf.setProperty("World", playa.getWorld().getName());
//                    else if(!aWorld.equals(playa.getWorld().getName()))
//                    {
//                        playa.sendMessage(ChatColor.RED + "Please build arena lobby in same world as its spawns and capture points!");
//                        return true;
//                    }
//                    CTPPoints tmps = new CTPPoints();
//                    tmps.x = playa.getLocation().getX();
//                    tmps.y = playa.getLocation().getY();
//                    tmps.z = playa.getLocation().getZ();
//                    tmps.dir = playa.getLocation().getYaw();
//                    if(mainArena.name.equalsIgnoreCase(selectedArena) || mainArena.name == null)
//                        mainArena.lobby = tmps;
//                    arenaConf.setProperty("Lobby.X", tmps.x);
//                    arenaConf.setProperty("Lobby.Y", tmps.y);
//                    arenaConf.setProperty("Lobby.Z", tmps.z);
//                    arenaConf.setProperty("Lobby.Dir", tmps.dir);
//                    arenaConf.save();
//                    playa.sendMessage(ChatColor.GREEN + selectedArena + ChatColor.WHITE + " arena lobby created");
//                    return true;
//                }
//                playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//            if(arg.equals("arenalist"))
//            {
//                if(canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.arenalist"))
//                {
//                    String arenas = "";
//                    boolean firstTime = true;
//                    for(String arena: arena_list)
//                    {
//                        if(firstTime)
//                        {
//                            arenas = arena;
//                            firstTime = false;
//                        }
//                        else
//                            arenas = arena + ", " + arenas;
//                    }
//                    playa.sendMessage("Arena list:");
//                    playa.sendMessage(arenas);
//                    return true;
//                }
//                playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//            if(arg.equals("pointlist"))
//            {
//                if(canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.pointlist"))
//                {
//                    if(selectedArena.equals(""))
//                    {
//                        playa.sendMessage(ChatColor.RED + "No arena selected!");
//                        return true;
//                    }
//
//                    ArenaData arena = loadArena(selectedArena);
//                    String points = "";
//                    boolean firstTime = true;
//                    for(CTPPoints s: arena.capturePoints)
//                    {
//                        if(firstTime)
//                        {
//                            points = s.name;
//                            firstTime = false;
//                        }
//                        else
//                            points = s.name + ", " + points;
//                    }
//                    playa.sendMessage(ChatColor.GREEN + selectedArena + ChatColor.WHITE + " point list:");
//                    playa.sendMessage(points);
//                    return true;
//                }
//                playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//
//            //  sets arena for editing/creating
//            if(arg.equals("setboundary"))
//            {
//                if (canAccess(playa, false, "ctp.*", "ctp.admin", "ctp.admin.setboundary"))
//                {
//                    if(args.length < 3)
//                    {
//                        playa.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setboundary <1 | 2>");
//                        return true;
//                    }
//                    if(selectedArena.equals(""))
//                    {
//                        playa.sendMessage(ChatColor.RED + "No arena selected!");
//                        return true;
//                    }
//
//
//                    Location loc = playa.getLocation();
//                    if(args[2].equalsIgnoreCase("1"))
//                    {
//                        if(selectedArena.equalsIgnoreCase(mainArena.name))
//                        {
//                            mainArena.x1 = loc.getBlockX();
//                            mainArena.z1 = loc.getBlockZ();
//                        }
//
//                        File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + selectedArena + ".yml");
//                        Configuration arenaConf = new Configuration(arenaFile);
//                        arenaConf.load();
//                        arenaConf.setProperty("Boundarys.X1", loc.getBlockX());
//                        arenaConf.setProperty("Boundarys.Z1", loc.getBlockZ());
//                        arenaConf.save();
//
//                        playa.sendMessage("First boundary point set.");
//                    }
//                    else if(args[2].equalsIgnoreCase("2"))
//                    {
//                        if(selectedArena.equalsIgnoreCase(mainArena.name))
//                        {
//                            mainArena.x2 = loc.getBlockX();
//                            mainArena.z2 = loc.getBlockZ();
//                        }
//
//                        File arenaFile = new File(mainDir + File.separator + "Arenas" + File.separator + selectedArena + ".yml");
//                        Configuration arenaConf = new Configuration(arenaFile);
//                        arenaConf.load();
//                        arenaConf.setProperty("Boundarys.X2", loc.getBlockX());
//                        arenaConf.setProperty("Boundarys.Z2", loc.getBlockZ());
//                        arenaConf.save();
//
//                        playa.sendMessage("Second boundary point set.");
//                    }
//
//                    return true;
//                }
//                playa.sendMessage(ChatColor.RED + "You do not have permission to do that.");
//                return true;
//            }
//        }
//
////        // arenos išsaugojimui
////        if(subc.equals("setpoint"))
////        {
////            Location loc = playa.getLocation();
////            if(arg.equals("1"))
////            {
////                 pasaulis = loc.getWorld().getName();
////                x1 = loc.getBlockX();
////                y1 = loc.getBlockY();
////                z1 = loc.getBlockZ();
////            }
////            else if(arg.equals("2"))
////            {
////                x2 = loc.getBlockX();
////                y2 = loc.getBlockY();
////                z2 = loc.getBlockZ();
////            }
////
////            return true;
////        }
////        // arenos išsaugojimui
////        if(subc.equals("save"))
////        {
////            int xlow = x1;
////            int xhigh = x2;
////            if(x2 < x1)
////            {
////                xlow = x2;
////                xhigh = x1;
////            }
////            int ylow = y1;
////            int yhigh = y2;
////            if(y2 < y1)
////            {
////                ylow = y2;
////                yhigh = y1;
////            }
////            int zlow = z1;
////            int zhigh = z2;
////            if(z2 < z1)
////            {
////                zlow = z2;
////                zhigh = z1;
////            }
////            if(configOptions.enableHardArenaRestore)
////            {
////                Statement stmt = null;
////                ResultSet rs = null;
////                try
////                {
////                    Class.forName("com.mysql.jdbc.Driver");
////                }
////                catch (Exception e)
////                {
////                   e.printStackTrace();
////                }
////                try
////                {
////                    Connection con = DriverManager.getConnection("jdbc:mysql://" + configOptions.mysqlAddress + ":" + configOptions.mysqlPort + "/"
////                            + configOptions.mysqlDatabase + "?user=" + configOptions.mysqlUser + "&password=" + configOptions.mysqlPass);
////
////                    stmt = con.createStatement();
////                }
////                catch (Exception e)
////                {
////                    e.printStackTrace();
////                }
////            }
////
////            for(int x = xlow; x <= xhigh; x++)
////                for(int y = ylow; y <= yhigh; y++)
////                    for(int z = zlow; z <= zhigh; z++)
////                    {
////                        Location loc = new Location(getServer().getWorld(pasaulis), x, y, z);
////                        int typeID = loc.getBlock().getTypeId();
////                        int data = loc.getBlock().getData();
////                        Material blockType = loc.getBlock().getType();
////
////                        switch (typeID)
////                        {
////                            case BlockID.WALL_SIGN:
////                            case BlockID.SIGN_POST:
////                            {
////                                SignBlock block = new SignBlock(type, data);
////                                world.copyFromWorld(pt, block);
////                                return block;
////                            }
////
////                            case BlockID.CHEST:
////                            {
////                                ChestBlock block = new ChestBlock(data);
////                                world.copyFromWorld(pt, block);
////                                return block;
////                            }
////
////                            case BlockID.FURNACE:
////                            case BlockID.BURNING_FURNACE:
////                            {
////                                FurnaceBlock block = new FurnaceBlock(type, data);
////                                world.copyFromWorld(pt, block);
////                                return block;
////                            }
////
////                            case BlockID.DISPENSER:
////                            {
////                                DispenserBlock block = new DispenserBlock(data);
////                                world.copyFromWorld(pt, block);
////                                return block;
////                            }
////
////                            case BlockID.MOB_SPAWNER:
////                            {
////                                MobSpawnerBlock block = new MobSpawnerBlock(data);
////                                world.copyFromWorld(pt, block);
////                                return block;
////                            }
////
////                            case BlockID.NOTE_BLOCK:
////                            {
////                                NoteBlock block = new NoteBlock(data);
////                                world.copyFromWorld(pt, block);
////                                return block;
////                            }
////
////                            default:
////                                return new BaseBlock(type, data);
////                        }
////                    }
////
////            return true;
////        }
////
////        if(subc.equals("restore"))
////        {
////
////
////            return true;
////        }
//      }
//    // if there is no such command
//    playa.sendMessage(ChatColor.WHITE + "Type " + ChatColor.GREEN + "/ctp help " + ChatColor.WHITE + "to see commands and their usage");
//    return true;
//  }
//}