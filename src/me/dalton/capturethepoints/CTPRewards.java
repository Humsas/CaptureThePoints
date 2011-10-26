 package me.dalton.capturethepoints;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class CTPRewards {
   public int winnerRewardCount;
   public int otherTeamRewardCount;
   public List<Items> winnerRewards = new LinkedList<Items>();
   public List<Items> loozerRewards = new LinkedList<Items>();
   public List<Items> rewardsForKill = new LinkedList<Items>();
   public List<Items> rewardsForCapture = new LinkedList<Items>();
 }

