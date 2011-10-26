package me.dalton.capturethepoints.commands;

import me.dalton.capturethepoints.CaptureThePoints;

public class SaveCommand extends CTPCommand {

    public SaveCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("save");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin.save", "ctp.admin"};
        super.senderMustBePlayer = false;
        super.minParameters = 2;
        super.maxParameters = 2;
        super.usageTemplate = "/ctp save";
    }

    @Override
    public void perform() {
        int xlow = ctp.x1;
        int xhigh = ctp.x2;
        if (ctp.x2 < ctp.x1) {
            xlow = ctp.x2;
            xhigh = ctp.x1;
        }
        int ylow = ctp.y1;
        int yhigh = ctp.y2;
        if (ctp.y2 < ctp.y1) {
            ylow = ctp.y2;
            yhigh = ctp.y1;
        }
        int zlow = ctp.z1;
        int zhigh = ctp.z2;
        if (ctp.z2 < ctp.z1) {
            zlow = ctp.z2;
            zhigh = ctp.z1;
        }
        for (int x = xlow; x <= xhigh; x++) {
            for (int y = ylow; y <= yhigh; y++) {
                for (int z = zlow; z <= zhigh; z++) {
                    /*
                    Location loc = new Location(ctp.getServer().getWorld(ctp.pasaulis), x, y, z);
                    int typeID = loc.getBlock().getTypeId();
                    int data = loc.getBlock().getData();
                    Material blockType = loc.getBlock().getType();

                    switch (typeID) {
                    case BlockID.WALL_SIGN:
                    case BlockID.SIGN_POST: {
                    SignBlock block = new SignBlock(type, data);
                    world.copyFromWorld(pt, block);
                    return block;
                    }

                    case BlockID.CHEST: {
                    ChestBlock block = new ChestBlock(data);
                    world.copyFromWorld(pt, block);
                    return block;
                    }

                    case BlockID.FURNACE:
                    case BlockID.BURNING_FURNACE: {
                    FurnaceBlock block = new FurnaceBlock(type, data);
                    return block;
                    }

                    case BlockID.DISPENSER: {
                    DispenserBlock block = new DispenserBlock(data);
                    world.copyFromWorld(pt, block);
                    return block;
                    }

                    case BlockID.MOB_SPAWNER: {
                    MobSpawnerBlock block = new MobSpawnerBlock(data);
                    world.copyFromWorld(pt, block);
                    return block;
                    }

                    case BlockID.NOTE_BLOCK: {
                    NoteBlock block = new NoteBlock(data);
                    world.copyFromWorld(pt, block);
                    return block;
                    }

                    default:
                    return new BaseBlock(type, data);
                    }
                    }
                    return true;
                     */
                }
            }
        }
    }
}