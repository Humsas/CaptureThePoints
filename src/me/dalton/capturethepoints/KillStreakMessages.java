package me.dalton.capturethepoints;

import java.util.HashMap;

/**
 * Kill Streak messages for arenas.
 * @author Kristian
 */
public class KillStreakMessages {
    public static final HashMap<Integer, String> defaultMessages = new HashMap<Integer, String>();
    private static boolean usingCustom = false;
    public HashMap<Integer, String> customMessages;

    /** Initialise KillStreakMessages by putting the default mappings in */
    public KillStreakMessages() {
        this(new HashMap<Integer, String>());
    }
    
    /** Initialise KillStreakMessages by putting the default mappings in and setting up customMessages. */
    public KillStreakMessages(HashMap<Integer, String> customMessages) {
        populateDefaultMessages();
        this.customMessages = customMessages;
        usingCustom = customMessages == null || customMessages.isEmpty() ? false : true;
    }    
    
    /** Populate the defaultMessages HM. */
    private void populateDefaultMessages() {
        defaultMessages.put(2, "%player strikes again!");
        defaultMessages.put(3, "%player is on a killing spree!");
        defaultMessages.put(4, "%player is on a rampage!");
        defaultMessages.put(5, "%player is unstoppable!");
        defaultMessages.put(6, "%player is GOD-LIKE!");
    }    
    
    /** Get the message associated with a kill amount. Returns an empty string if not found.*/
    public String getMessage(int killAmount) {
        if (this.customMessages.containsKey(killAmount)) {
            return this.customMessages.get(killAmount); // Check if the customMessages contains it
        } else if (defaultMessages.containsKey(killAmount)) {
            return defaultMessages.get(killAmount); // Nope, see if it's in default.
        } 
        return "";
    }    
    
    /** Add custom message to HashMap */
    public void addCustomMessage(int killAmount, String message) {
        if (this.customMessages != null) {
            this.customMessages.put(killAmount, message);
        } else {
            this.customMessages = new HashMap<Integer, String>();
            this.customMessages.put(killAmount, message);
        }
        usingCustom = true;
    }  
    
    /** Remove custom message from HashMap */
    public void removeCustomMessage(int killAmount) {
        if (this.customMessages != null) {
            this.customMessages.remove(killAmount);
        }
    }    
    
    /** Clear the customMessages HashMap */
    public void clearCustomMessages() {
        this.customMessages.clear();
    }    
    
    /** Return whether the customMessages HashMap is populated */
    public boolean useCustomMessages() {
        return usingCustom;
    }
}
