package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;

public class ReloadCommand extends CTPCommand {
   
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
        ctp.loadConfigFiles();
        sender.sendMessage("[CTP] successfully reloaded!");
        return;
    }
}