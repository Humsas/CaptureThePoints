package me.dalton.capturethepoints.commands;
import java.util.ArrayList;
import java.util.List;
import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Kristian
 */
public abstract class CTPCommand
{
    public CaptureThePoints ctp;
    public List<String> aliases;

    public String[] requiredPermissions;
    public boolean notOpCommand;
    public boolean senderMustBePlayer;

    public String usageTemplate;

    public CommandSender sender;
    public Player player;

    public List<String> parameters;
    public int minParameters;
    public int maxParameters;
    public int actionIndex;

    public CTPCommand()
    {
        aliases = new ArrayList<String>();

        notOpCommand = false;
        senderMustBePlayer = true;

        usageTemplate = "";

        actionIndex = 1;
        minParameters = 0;
        maxParameters = 0;
    }

    public final void sendMessage(String message)
    {
        sender.sendMessage("[CTP] "+message);
    }

    protected boolean canAccess(CommandSender sender, boolean notOpCommand, String[] permissions)
    {
        if (sender instanceof ConsoleCommandSender)
        {
            return true;
        } 
        else if (!(sender instanceof Player))
        {
            return false;
        }
        else
        {
            return canAccess((Player)sender, notOpCommand, permissions);
        }
    }

    protected boolean canAccess(Player p, boolean notOpCommand, String[] permissions)
    {
        if (CaptureThePoints.UsePermissions)
        {
            for (String perm : permissions)
            {
                if (CaptureThePoints.Permissions.has(p, perm))
                {
                    return true;
                }
            }
        } 
        else
        {
            if (notOpCommand)
            {
                return true;
            }
            return p.isOp();
        }
        
//        if (permissions == null)
//        {
//            return true;
//        }

        return false;

    }

    public final void execute(CommandSender sender, List<String> parameters)
    {
        this.sender = sender;
        this.parameters = parameters;

        if (senderMustBePlayer && !(sender instanceof Player))
        {
            sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        } 
        else
        {
            this.player = (Player) sender;
        }

        if (requiredPermissions != null)
        {
            if (requiredPermissions.length != 0 && !canAccess(sender, notOpCommand, requiredPermissions))
            {
                sendMessage(ChatColor.RED + "You need permission to use the " + ChatColor.WHITE + parameters.get(actionIndex) + ChatColor.RED + " command.");
                return;
            }
        }

        if ((parameters.size() < minParameters && minParameters != 0) || (parameters.size() > maxParameters) && maxParameters != 0)
        {
            usageError();
            return;
        }

        perform();
    }

    public abstract void perform();

    public final void usageError()
    {
        sendMessage(ChatColor.AQUA + "Try: " + ChatColor.WHITE + usageTemplate);
    }
}
