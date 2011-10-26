package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;
import org.bukkit.Location;

public class SetpointCommand extends CTPCommand {

    public SetpointCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("setpoint");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin.setpoints", "ctp.admin"};
        super.senderMustBePlayer = false;
        super.minParameters = 3;
        super.maxParameters = 3;
        super.usageTemplate = "/ctp setpoint <number>";
    }

    @Override
    public void perform() {
        Location loc = player.getLocation();
        if (parameters.get(2).equalsIgnoreCase("1")) {
            ctp.x1 = loc.getBlockX();
            ctp.y1 = loc.getBlockY();
            ctp.z1 = loc.getBlockZ();
        } else if (parameters.get(2).equalsIgnoreCase("2")) {
            ctp.x2 = loc.getBlockX();
            ctp.y2 = loc.getBlockY();
            ctp.z2 = loc.getBlockZ();
        }
    }
}