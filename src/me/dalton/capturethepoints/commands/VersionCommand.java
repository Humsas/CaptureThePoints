package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class VersionCommand extends CTPCommand {
   
    /** Get the version of CTP you are running. */
    public VersionCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("version");
        super.aliases.add("about");
        super.aliases.add("authors");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin.version", "ctp.admin"};
        super.senderMustBePlayer = false;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp version";
    }

    @Override
    public void perform() {
        sendMessage("Catpure the Points " + ChatColor.GREEN + ctp.getDescription().getVersion());
        sendMessage("By " + ChatColor.GREEN + ctp.getDescription().getAuthors());
    }
}