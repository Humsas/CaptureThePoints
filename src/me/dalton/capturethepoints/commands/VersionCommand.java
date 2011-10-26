package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class VersionCommand extends CTPCommand {
   
    public VersionCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("version");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin.version", "ctp.admin"};
        super.senderMustBePlayer = false;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp version";
    }

    @Override
    public void perform() {
        player.sendMessage("CTP version: " + ChatColor.GREEN + ctp.getDescription().getVersion());
    }
}