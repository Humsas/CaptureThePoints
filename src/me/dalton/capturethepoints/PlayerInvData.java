package me.dalton.capturethepoints;

/**
 *
 * @author Humsas
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.bukkit.entity.Player;

public class PlayerInvData
{
    public final CaptureThePoints plugin;
    public ArrayList<String> existingPlayers = new ArrayList<String>();

    public PlayerInvData(CaptureThePoints instance)
    {
        this.plugin = instance;
        loadPlayers();
    }

    private void loadPlayers()
    {
        File file = new File(CaptureThePoints.mainDir + File.separator + "PlayersInv");
        searchFolders(file);
    }

    private void searchFolders(File file)
    {
        if (file.isDirectory())
        {
            String internalNames[] = file.list();
            for (String name : internalNames)
            {
                searchFolders(new File(file.getAbsolutePath() + File.separator + name));
            }
        } else
        {
            String fileName = file.getName().split("\\.")[0];
            if (!existingPlayers.contains(fileName))
            {
                existingPlayers.add(fileName);
            }
        }
    }

    // Saving
    public void storeInventory(Player player)
    {
        existingPlayers.add(player.getName());
        String inventoryName = "Inventory";
        
        MultiInvInventory inventory = new MultiInvInventory(player, inventoryName);
        
        String file = CaptureThePoints.mainDir + File.separator + "PlayersInv" + File.separator + player.getName() + ".data";
        String lala = inventory.toString();
        player.sendMessage(lala);
        saveToFile(file, inventoryName, lala);
    }

    public void setInventory(Player player)
    {
        if (!existingPlayers.contains(player.getName()))
            return;

        String inventoryName = "Inventory";
        File file = new File(CaptureThePoints.mainDir + File.separator + "PlayersInv" + File.separator + player.getName() + ".data");
        String tmpInventory = loadFromProperties(file, inventoryName);
        if (tmpInventory != null)
        {
            MultiInvInventory inventory = new MultiInvInventory();
            inventory.fromString(tmpInventory); // converts properties string to MultiInvInventory
            inventory.getInventory(player); //sets players inventory
            existingPlayers.remove(player.getName());

            // if inventory restored delete file
            file.delete();
            
            return;
        }
    }

    public void saveToFile(String file, String key, String value)
    {
        File FileP = new File(file);
        Properties prop = new Properties();
        File dir = new File(FileP.getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!FileP.exists())
        {
            try {
                FileP.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try
        {
            FileInputStream in = new FileInputStream(FileP);
            prop.load(in);
            prop.put(key, value);
            prop.store(new FileOutputStream(FileP), "Stored Inventory");
            in.close();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static String loadFromProperties(File file, String key)
    {
        Properties prop = new Properties();
        String value = null;
        File dir = new File(file.getParent());
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream in = new FileInputStream(file);
            prop.load(in);
            if (prop.containsKey(key))
            {
                value = prop.getProperty(key);
                in.close();
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return value;
    }

}
