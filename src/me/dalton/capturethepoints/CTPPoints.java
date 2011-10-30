 package me.dalton.capturethepoints;
 
 /** A Capture Point or Spawn Point in a CTP arena */
 public class CTPPoints {
     
   /** X co-ordinate of this Point */
   public double x;
   
   /** Y co-ordinate of this Point */
   public double y;
   
   /** Z co-ordinate of this Point */
   public double z;
   
   /** Direction players spawn in this Point */
   public double dir = 0.0D;
   
   /** The name of this Capture Point */
   public String name;
   
   /** Which Block in a Capture Point this is [NORTH, EAST, SOUTH, WEST] */
   public String pointDirection = null;
   
   /** Which Team controls this Point 
    * @see Team */
   public String controledByTeam = null;
 }

