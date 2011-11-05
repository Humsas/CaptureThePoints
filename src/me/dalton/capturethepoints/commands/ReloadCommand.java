package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.ChatColor;

public class ReloadCommand extends CTPCommand {
   
    /** Reload the config files and get version */
    public ReloadCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("reload");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin", "ctp.admin.reload"};
        super.senderMustBePlayer = false;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp reload";
    }

    @Override
    public void perform() {
        ctp.clearConfig();
        ctp.enableCTP(true);
        sender.sendMessage("[CTP] successfully reloaded!");
        return;
    }
}