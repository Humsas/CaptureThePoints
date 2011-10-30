package me.dalton.capturethepoints;

/** The schedulers used by CTP */
public class CTPScheduler {
    
  /** The timer used to give players money every x seconds */
  public int money_Score;
  
  /** The timer used to track game time. */
  public int playTimer;
  
  /** The timer used to announce points/scores of teams */
  public int pointMessenger;
  
  /** The timer used to periodically check the helmets of everyone so they haven't removed it */
  public int helmChecker;
  
  /** The timer used to check time players have been in the lobby (and then to see if they need kicking) */
  public int lobbyActivity; // Kjhf
  
  /** The timer used to check healing items' cooldowns. */
  public int healingItemsCooldowns;
}
