package me.dalton.capturethepoints;

/** Class for the config options ctp accepts. */
public class ConfigOptions {   
    /** Allow placement of blocks during a ctp game /||/ Disallow placement of blocks during a ctp game (except for capturing points) [Default: true] */
    public boolean allowBlockPlacement; // Kjhf
    
    /** Allow use of commands during a ctp game /||/ Disallow use of commands during a ctp game (excludes /ctp) [Default: false] */
    public boolean allowCommands;
    
    /** Allow joining a ctp game if one has already started /||/ Disallow joining a ctp game if one has already started [Default: true] */
    public boolean allowLateJoin; // Kjhf
    
    /** Allow the game to start automatically when there are enough players /||/ Don't start game until an admin does /ctp start [Default: true] */
    public boolean autoStart; // Kjhf
    
    /** Breaking blocks in game drops items /||/ Cancel any item drops resulting from breaking blocks in game [Default: false] */
    public boolean breakingBlocksDropsItems; // Kjhf
    
    /** Drop wool if a player dies /||/ Don't drop wool if a player dies [Default: true] */
    public boolean dropWoolOnDeath; // Kjhf
       
    /** Teams must be even on start (may result in one person being left at lobby) /||/ Teams can be uneven on start (everyone can play) [Default: false] */
    public boolean exactTeamMemberCount;
    
    /** Replenish items on respawn /||/ Limited ammo and durability! [Default: true] */
    public boolean giveNewRoleItemsOnRespawn;
    
    /** Use scoring system /||/ Use points system [Default: false] */
    public boolean useScoreGeneration;
    
    /** Only choose the arena that an admin has picked (/ctp select) or one specified as main. /||/ Allow suitable arena pick. [Default: true] */
    public boolean useSelectedArenaOnly; // Kjhf
    
    /** The starting wool players are given. [Default: 64] */
    public int givenWoolNumber;
    
    /** The time, in seconds, players are given to ready up else they are kicked. [Default: 60] */
    public int lobbyKickTime; // Kjhf
    
    /** The maximum player health in game. [Default: 20] */
    public int maxPlayerHealth;
    
    /** The starting money players are given. [Default: 0] */
    public int moneyAtTheLobby;
    
    /** The money players are awarded every 30s. [Default: 100] */
    public int moneyEvery30Sec;
    
    /** The money players are awarded per kill [Default: 100] */
    public int moneyForKill;
    
    /** The money players are awarded for a point capture [Default: 100] */
    public int moneyForPointCapture;
    
    /** How much score a team gets per point per 30s [Default: 1] */
    public int onePointGeneratedScoreEvery30sec;
    
    /** Maximum play time in minutes [Default: 10] */
    public int playTime;
    
    /** Points a team much reach to win [Default: 1] */
    public int pointsToWin;
    
    /** Spawn camping protection radius [Default: 10] */
    public int protectionDistance;
    
    /** The block ID of a point [Default: 7 (Bedrock)] */
    public int ringBlock;
    
    /** How often scores are announced in seconds [Default: 30] */
    public int scoreAnnounceTime;
    
    /** The score a team much reach to win [Default: 15] */
    public int scoreToWin;
    
    /*
    public boolean enableHardArenaRestore; // Write to MYSQL
    public int mysqlPort;
    public String mysqlAddress;
    public String mysqlDatabase;
    public String mysqlPass;
    public String mysqlUser;
     */
}