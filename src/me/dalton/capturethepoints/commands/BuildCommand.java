package me.dalton.capturethepoints.commands;

import java.io.File;
import me.dalton.capturethepoints.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.config.Configuration;

public class BuildCommand extends CTPCommand {
    
    // Kj -- This could be broken down further, perhaps into a new package compeltely.
    public BuildCommand(CaptureThePoints instance) {
        super.ctp = instance;
        super.aliases.add("build");
        super.aliases.add("create");
        super.aliases.add("make");
        super.notOpCommand = false;
        super.requiredPermissions = new String[]{"ctp.*", "ctp.admin",
            "ctp.admin.setpoint", "ctp.admin.removepoint", "ctp.admin.create", "ctp.admin.delete", "ctp.admin.selectarena",
            "ctp.admin.setarena", "ctp.admin.setlobby", "ctp.admin.arenalist", "ctp.admin.pointlist", "ctp.admin.setboundary",
            "ctp.admin.maximumplayers", "ctp.admin.minimumplayers"};
        super.senderMustBePlayer = true;
        super.minParameters = 2;
        super.maxParameters = 5;
        super.usageTemplate = "/ctp build help";
    }

    @Override
    public void perform() {
        int size = parameters.size();
        String arg = size > 2 ? parameters.get(2) : "help"; // Kj -- grab the arguments with null -> empty checking. If only /ctp build, assume help.
        String arg2 = size > 3 ? parameters.get(3) : "";
        String arg3 = size > 4 ? parameters.get(4) : "";
        if (arg.equalsIgnoreCase("help")) {
            player.sendMessage(ChatColor.RED + "Build commands:");
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin.setspawn", "ctp.admin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build setspawn <Team color> " + ChatColor.WHITE + "- sets the place people are teleported to when they die or when they join the game");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin.removespawn", "ctp.admin"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build removespawn <Team color> " + ChatColor.WHITE + "- removes spawn point of selected color");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setpoint"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build setpoint <Point name> <vert | hor> " + ChatColor.WHITE + "- creates new capture point");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.removepoint"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build removepoint <Point name> " + ChatColor.WHITE + "- removes an existing capture point");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.create"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build create <Arena name> " + ChatColor.WHITE + "- creates an arena");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.delete"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build delete <Arena name> " + ChatColor.WHITE + "- deletes an existing arena");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.selectarena"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build selectarena <Arena name> " + ChatColor.WHITE + "- selects arena for editing");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setarena"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build setarena <Arena name> " + ChatColor.WHITE + "- sets main arena for playing");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setlobby"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build setlobby " + ChatColor.WHITE + "- sets arena lobby");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.arenalist"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build arenalist " + ChatColor.WHITE + "- shows existing arenas list");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.pointlist"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build pointlist " + ChatColor.WHITE + "- shows selected arena capture points list");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setboundary"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build setboundary <1 | 2> " + ChatColor.WHITE + "- sets boundary (1 or 2) of the arena");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.maximumplayers"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build maximumplayers <number> " + ChatColor.WHITE + "- sets maximum players of the arena");
            }
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.minimumplayers"})) {
                player.sendMessage(ChatColor.GREEN + "/ctp build minimumplayers <number> " + ChatColor.WHITE + "- sets minimum players of the arena");
            }
            return;
        }

        if (arg.equalsIgnoreCase("setspawn")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin.setspawn", "ctp.admin"})) {
                if (parameters.size() < 4) {
                    player.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setspawn <Team color> ");
                    return;
                }
                if (ctp.editingArenaName.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }
                Location loc = player.getLocation();

                File arenaFile = new File(CaptureThePoints.mainDir + File.separator + "Arenas" + File.separator + ctp.editingArenaName + ".yml");
                Configuration arenaConf = new Configuration(arenaFile);
                arenaConf.load();

                if ((arenaConf.getString("World") != null) && (!arenaConf.getString("World").equals(player.getWorld().getName()))) {
                    player.sendMessage(ChatColor.RED + "Please build all arena team spawns in the same world ---->" + ChatColor.GREEN + arenaConf.getString("World"));
                    return;
                }

                if ((arg2.equals("white"))
                        || (arg2.equals("lightgray"))
                        || (arg2.equals("gray"))
                        || (arg2.equals("black"))
                        || (arg2.equals("red"))
                        || (arg2.equals("orange"))
                        || (arg2.equals("yellow"))
                        || (arg2.equals("lime"))
                        || (arg2.equals("green"))
                        || (arg2.equals("blue"))
                        || (arg2.equals("cyan"))
                        || (arg2.equals("lightblue"))
                        || (arg2.equals("purple"))
                        || (arg2.equals("pink"))
                        || (arg2.equals("magenta"))
                        || (arg2.equals("brown"))) {
                    CTPPoints tmps = new CTPPoints();
                    tmps.name = arg2;
                    tmps.x = Double.valueOf(loc.getX()).doubleValue();
                    tmps.y = Double.valueOf(loc.getY()).doubleValue();
                    tmps.z = Double.valueOf(loc.getZ()).doubleValue();
                    tmps.dir = loc.getYaw();

                    String aWorld = arenaConf.getString("World");
                    if (aWorld == null) {
                        arenaConf.setProperty("World", player.getWorld().getName());
                    } else if (!aWorld.equals(player.getWorld().getName())) {
                        player.sendMessage(ChatColor.RED + "Please build arena lobby in same world as its spawns and capture points!");
                        return;
                    }
                    arenaConf.setProperty("Team-Spawns." + arg2 + ".X", Double.valueOf(loc.getX()));
                    arenaConf.setProperty("Team-Spawns." + arg2 + ".Y", Double.valueOf(loc.getY()));
                    arenaConf.setProperty("Team-Spawns." + arg2 + ".Z", Double.valueOf(loc.getZ()));
                    arenaConf.setProperty("Team-Spawns." + arg2 + ".Dir", Double.valueOf(tmps.dir));
                    arenaConf.save();

                    if (ctp.mainArena.world == null) {
                        //ctp.mainArena = new ArenaData();
                        ctp.mainArena.world = player.getWorld().getName();
                        ctp.mainArena.name = ctp.editingArenaName;
                    }
                    if (ctp.mainArena.world.equals(player.getWorld().getName())) {
                        ctp.mainArena.teamSpawns.put(arg2, tmps);
                        Team team = new Team();
                        team.color = arg2;
                        team.memberCount = 0;
                        try {
                            team.chatcolor = ChatColor.valueOf(tmps.name.toUpperCase()); // Kj -- init teamchat colour
                        } catch (Exception ex) {
                            team.chatcolor = ChatColor.GREEN;
                        }
                        // Check if this spawn is already in the list
                        boolean hasTeam = false;

                        for (Team aTeam : ctp.teams) {
                            if (aTeam.color.equalsIgnoreCase(arg2)) {
                                hasTeam = true;
                                //ctp.teams.remove(aTeam);
                            }
                        }

                        if (!hasTeam) {
                            ctp.teams.add(team);
                        }
                    }
                    player.sendMessage("You set the " + ChatColor.GREEN + arg2 + ChatColor.WHITE + " team spawn point.");
                    return;
                }

                player.sendMessage(ChatColor.RED + "There is no such color!");
                return;
            }
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("removespawn")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin.removespawn", "ctp.admin"})) {
                if (parameters.size() < 4) {
                    player.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build removespawn <Team color> ");
                    return;
                }
                arg2 = arg2.toLowerCase();

                if (ctp.editingArenaName.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }

                File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + ctp.editingArenaName + ".yml");
                Configuration arenaConf = new Configuration(arenaFile);
                arenaConf.load();
                if (arenaConf.getString("Team-Spawns." + arg2 + ".X") == null) {
                    player.sendMessage(ChatColor.RED + "This arena spawn does not exist! -----> " + ChatColor.GREEN + arg2);
                    return;
                }
                arenaConf.removeProperty("Team-Spawns." + arg2);
                arenaConf.save();
                if (ctp.editingArenaName.equalsIgnoreCase(ctp.mainArena.name)) {
                    ctp.mainArena.teamSpawns.remove(arg2);
                }
                for (int i = 0; i < ctp.teams.size(); i++) {
                    if (!ctp.teams.get(i).color.equals(arg2)) {
                        continue;
                    }
                    ctp.teams.remove(i);
                    break;
                }

                player.sendMessage(ChatColor.GREEN + arg2 + " " + ChatColor.WHITE + "spawn was removed.");
                return;
            }
            player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("setpoint")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setpoint"})) {
                if (parameters.size() < 5) {
                    player.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setpoint <Point name> <vert | hor>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                arg3 = arg3.toLowerCase();

                if ((!arg3.equals("vert")) && (!arg3.equals("hor"))) {
                    player.sendMessage(ChatColor.RED + "Points can be vertical or horizontal: " + ChatColor.GREEN + "vert | hor");
                    return;
                }

                if (ctp.editingArenaName.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }
                CTPPoints tmps = new CTPPoints();
                tmps.name = arg2;
                Location loc = player.getLocation();
                int start_x;
                tmps.x = (start_x = loc.getBlockX());
                int start_y;
                tmps.y = (start_y = loc.getBlockY());
                int start_z;
                tmps.z = (start_z = loc.getBlockZ());

                ArenaData arena = ctp.loadArena(ctp.editingArenaName);
                File arenaFile = new File(ctp.mainDir + File.separator + "Arenas" + File.separator + ctp.editingArenaName + ".yml");
                Configuration arenaConf = new Configuration(arenaFile);
                arenaConf.load();
                if ((arenaConf.getString("World") != null) && (!arenaConf.getString("World").equals(player.getWorld().getName()))) {
                    player.sendMessage(ChatColor.RED + "Please build all arena points in same world ----> " + ChatColor.GREEN + arenaConf.getString("World"));
                    return;
                }

                for (CTPPoints point : arena.capturePoints) {
                    Location protectionPoint = new Location(player.getWorld(), point.x, point.y, point.z);
                    double distance = player.getLocation().distance(protectionPoint);
                    if (distance < 5.0D) {
                        player.sendMessage(ChatColor.RED + "You are trying to build too close to another point!"); // Kj to -> too
                        return;
                    }
                }

                if (arg3.equals("vert")) {
                    double yaw = loc.getYaw();

                    while (yaw < 0.0D) {
                        yaw += 360.0D;
                    }
                    BlockFace direction;
                    if ((yaw > 315.0D) || (yaw <= 45.0D)) {
                        direction = BlockFace.WEST;
                    } else {
                        if ((yaw > 45.0D) && (yaw <= 135.0D)) {
                            direction = BlockFace.NORTH;
                        } else {
                            if ((yaw > 135.0D) && (yaw <= 225.0D)) {
                                direction = BlockFace.EAST;
                            } else {
                                direction = BlockFace.SOUTH;
                            }
                        }
                    }
                    switch (direction) {
                        case NORTH:
                            Util.buildVert(player, start_x, start_y - 1, start_z - 1, 2, 4, 4, ctp.configOptions.ringBlock);
                            player.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y, start_z + 1).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z + 1).setTypeId(0);
                            arenaConf.setProperty("Points." + arg2 + ".Dir", "NORTH");
                            tmps.pointDirection = "NORTH";
                            break;
                        case EAST:
                            Util.buildVert(player, start_x - 1, start_y - 1, start_z, 4, 4, 2, ctp.configOptions.ringBlock);
                            player.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x + 1, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x + 1, start_y + 1, start_z).setTypeId(0);
                            arenaConf.setProperty("Points." + arg2 + ".Dir", "EAST");
                            tmps.pointDirection = "EAST";
                            break;
                        case SOUTH:
                            Util.buildVert(player, start_x - 1, start_y - 1, start_z - 1, 2, 4, 4, ctp.configOptions.ringBlock);
                            player.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y, start_z + 1).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z + 1).setTypeId(0);
                            arenaConf.setProperty("Points." + arg2 + ".Dir", "SOUTH");
                            tmps.pointDirection = "SOUTH";
                            break;
                        case WEST:
                            Util.buildVert(player, start_x - 1, start_y - 1, start_z - 1, 4, 4, 2, ctp.configOptions.ringBlock);
                            player.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x, start_y + 1, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x + 1, start_y, start_z).setTypeId(0);
                            player.getWorld().getBlockAt(start_x + 1, start_y + 1, start_z).setTypeId(0);
                            arenaConf.setProperty("Points." + arg2 + ".Dir", "WEST");
                            tmps.pointDirection = "WEST";
                    }
                }

                if (arg3.equals("hor")) {
                    for (int x = start_x + 2; x >= start_x - 1; x--) {
                        for (int y = start_y - 1; y <= start_y; y++) {
                            for (int z = start_z - 1; z <= start_z + 2; z++) {
                                player.getWorld().getBlockAt(x, y, z).setTypeId(ctp.configOptions.ringBlock);
                            }
                        }
                    }

                    player.getWorld().getBlockAt(start_x, start_y, start_z).setTypeId(0);
                    player.getWorld().getBlockAt(start_x + 1, start_y, start_z).setTypeId(0);
                    player.getWorld().getBlockAt(start_x + 1, start_y, start_z + 1).setTypeId(0);
                    player.getWorld().getBlockAt(start_x, start_y, start_z + 1).setTypeId(0);
                }

                String aWorld = arenaConf.getString("World");
                if (aWorld == null) {
                    arenaConf.setProperty("World", player.getWorld().getName());
                } else if (!aWorld.equals(player.getWorld().getName())) {
                    player.sendMessage(ChatColor.RED + "Please build arena lobby in same world as its spawns and capture points!");
                    return;
                } // it should never get here
                arenaConf.setProperty("Points." + arg2 + ".X", Double.valueOf(tmps.x));
                arenaConf.setProperty("Points." + arg2 + ".Y", Double.valueOf(tmps.y));
                arenaConf.setProperty("Points." + arg2 + ".Z", Double.valueOf(tmps.z));
                arenaConf.save();

                if (ctp.mainArena.world == null) {
                    ctp.mainArena.world = player.getWorld().getName();
                    ctp.mainArena.name = ctp.editingArenaName;
                }

                if (ctp.mainArena.world.equals(player.getWorld().getName())) {
                    ctp.mainArena.capturePoints.add(tmps);
                }
                player.sendMessage(ChatColor.WHITE + "You created capture point -----> " + ChatColor.GREEN + arg2);
                return;
            }
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("removepoint")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.removepoint"})) {
                if (parameters.size() < 4) {
                    player.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build removepoint <Point name>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                if (ctp.editingArenaName.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }

                File arenaFile = new File(ctp.mainDir + File.separator + "Arenas" + File.separator + ctp.editingArenaName + ".yml");
                Configuration arenaConf = new Configuration(arenaFile);
                arenaConf.load();
                if (arenaConf.getString("Points." + arg2 + ".X") == null) {
                    player.sendMessage(ChatColor.RED + "This arena point does not exist! -----> " + ChatColor.GREEN + arg2);
                    return;
                }
                if ((arenaConf.getKeys("Points").size() == 1) && (arenaConf.getKeys("Team-Spawns") == null)) {
                    arenaConf.removeProperty("World");
                }
                int start_x = arenaConf.getInt("Points." + arg2 + ".X", 0);
                int start_y = arenaConf.getInt("Points." + arg2 + ".Y", 0);
                int start_z = arenaConf.getInt("Points." + arg2 + ".Z", 0);

                // Kj -- s -> aPoint
                if (ctp.mainArena.name.equals(player.getWorld().getName())) {
                    for (CTPPoints aPoint : ctp.mainArena.capturePoints) {
                        if (aPoint.name.equalsIgnoreCase(arg2)) {
                            ctp.mainArena.capturePoints.remove(aPoint);
                            break;
                        }
                    }
                }
                //Remove blocks
                if (arenaConf.getString("Points." + arg2 + ".Dir") == null) {
                    for (int x = start_x + 2; x >= start_x - 1; x--) {
                        for (int y = start_y - 1; y <= start_y; y++) {
                            for (int z = start_z - 1; z <= start_z + 2; z++) {
                                if (player.getWorld().getBlockAt(x, y, z).getTypeId() == ctp.configOptions.ringBlock) {
                                    player.getWorld().getBlockAt(x, y, z).setTypeId(0);
                                }
                            }
                        }
                    }
                } else {
                    String direction = arenaConf.getString("Points." + arg2 + ".Dir");
                    Util.removeVertPoint(player, direction, start_x, start_y, start_z, ctp.configOptions.ringBlock);
                }

                arenaConf.removeProperty("Points." + arg2);
                arenaConf.save();
                player.sendMessage(ChatColor.WHITE + "You removed capture point -----> " + ChatColor.GREEN + arg2);

                return;
            }
            player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("create")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.create"})) {
                if (parameters.size() < 4) {
                    player.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build create <Arena name>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                if (ctp.arena_list.contains(arg2)) {
                    player.sendMessage(ChatColor.RED + "This arena already exists! -----> " + ChatColor.GREEN + arg2); // Kj -- typo
                    return;
                }
                File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + arg2 + ".yml");
                Configuration arenaConf = new Configuration(arenaFile);
                arenaConf.save();
                ctp.editingArenaName = arg2;
                Configuration config = ctp.load();
                //Seting main arena if this is first arena

                String arena = (String) config.getProperty("Arena");
                if (arena == null) {
                    config.setProperty("Arena", arg2);
                    config.save();

                    ctp.mainArena = new ArenaData();
                    ctp.mainArena.name = arg2;
                    ctp.mainArena.world = null;
                }

                ctp.arena_list.add(arg2);
                player.sendMessage("You created arena: " + ChatColor.GREEN + arg2);

                return;
            }
            player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("delete")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.delete"})) {
                if (parameters.size() < 4) {
                    player.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build delete <Arena name>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                if (!ctp.arena_list.contains(arg2)) {
                    player.sendMessage(ChatColor.RED + "This arena does not exist! -----> " + ChatColor.GREEN + arg2);
                    return;
                }
                if ((ctp.isGameRunning()) && (ctp.mainArena.name.equals(arg2))) {
                    player.sendMessage(ChatColor.RED + "Cannot delete arena while game is running in it!");
                    return;
                }
                File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + arg2 + ".yml");

                //Remove blocks
                Configuration arenaConf = new Configuration(arenaFile);
                arenaConf.load();
                Configuration config = ctp.load();

                if (arenaConf.getString("Points") != null) {
                    for (String str : arenaConf.getKeys("Points")) {
                        str = "Points." + str;
                        int start_x = arenaConf.getInt(str + ".X", 0);
                        int start_y = arenaConf.getInt(str + ".Y", 0);
                        int start_z = arenaConf.getInt(str + ".Z", 0);

                        if (arenaConf.getString(str + ".Dir") == null) {
                            for (int x = start_x + 2; x >= start_x - 1; x--) {
                                for (int y = start_y - 1; y <= start_y; y++) {
                                    for (int z = start_z - 1; z <= start_z + 2; z++) {
                                        if (player.getWorld().getBlockAt(x, y, z).getTypeId() == ctp.configOptions.ringBlock) {
                                            player.getWorld().getBlockAt(x, y, z).setTypeId(0);
                                        }
                                    }
                                }
                            }
                        } else {
                            String direction = arenaConf.getString(str + ".Dir");
                            Util.removeVertPoint(player, direction, start_x, start_y, start_z, ctp.configOptions.ringBlock);
                        }
                    }
                }
                arenaFile.delete();
                ctp.arena_list.remove(arg2);
                if (arg2.equals(ctp.mainArena.name)) {
                    ctp.mainArena = null;
                    ctp.teams.clear();
                    config.removeProperty("Arena");
                    config.save();
                }
                if (arg2.equals(ctp.editingArenaName)) {
                    ctp.editingArenaName = "";
                }
                player.sendMessage("You deleted arena: " + ChatColor.GREEN + arg2);

                return;
            }
            player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }
        //  sets arena for editing/creating
        if (arg.equalsIgnoreCase("selectarena")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.selectarena"})) {
                if (parameters.size() < 4) {
                    player.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build selectarena <Arena name>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                if (!ctp.arena_list.contains(arg2)) {
                    player.sendMessage(ChatColor.RED + "This arena does not exist! -----> " + ChatColor.GREEN + arg2);
                    return;
                }
                ctp.editingArenaName = arg2;
                player.sendMessage(ChatColor.WHITE + "Arena selected for editing: " + ChatColor.GREEN + arg2);

                return;
            }
            player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("setarena")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setarena"})) {
                if (parameters.size() < 4) {
                    player.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setarena <Arena name>");
                    return;
                }
                arg2 = arg2.toLowerCase();
                if (!ctp.arena_list.contains(arg2)) {
                    player.sendMessage(ChatColor.RED + "This arena does not exist! -----> " + ChatColor.GREEN + arg2);
                    return;
                }

                ArenaData arena = ctp.loadArena(arg2);
                boolean canLoad = true;
                if (arena.capturePoints.size() < 1) {
                    player.sendMessage(ChatColor.RED + "Please add at least one capture point");
                    canLoad = false;
                }
                if (arena.teamSpawns.size() < 2) {
                    player.sendMessage(ChatColor.RED + "Please add at least two ctp.teams spawn points");
                    canLoad = false;
                }
                if (arena.lobby == null) {
                    player.sendMessage(ChatColor.RED + "Please create arena lobby");
                    canLoad = false;
                }
                if ((arena.x1 == 0) && (arena.x2 == 0) && (arena.z1 == 0) && (arena.z2 == 0)) {
                    player.sendMessage(ChatColor.RED + "Please set arena boundaries");
                    canLoad = false;
                }

                if (canLoad) {
                    Configuration config = ctp.load();
                    config.setProperty("Arena", arg2);
                    config.save();
                    ctp.mainArena = null;
                    ctp.mainArena = arena;
                    // And to be sure that everything is fine rectp.load all config

                    ctp.clearConfig();
                    ctp.loadConfigFiles();

                    player.sendMessage(ChatColor.WHITE + "Arena selected for playing: " + ChatColor.GREEN + arg2);
                }

                return;
            }
            player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("setlobby")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setlobby"})) {
                if (ctp.editingArenaName.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }
                File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + ctp.editingArenaName + ".yml");

                Configuration arenaConf = new Configuration(arenaFile);
                arenaConf.load();
                String aWorld = arenaConf.getString("World");
                if (aWorld == null) {
                    arenaConf.setProperty("World", player.getWorld().getName());
                } else if (!aWorld.equals(player.getWorld().getName())) {
                    player.sendMessage(ChatColor.RED + "Please build arena lobby in same world as its spawns and capture points!");
                    return;
                }
                // Kj -- changed from CTPoints
                Lobby lobby = new Lobby(
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ(),
                        player.getLocation().getYaw());
                if ((ctp.mainArena.name.equalsIgnoreCase(ctp.editingArenaName)) || (ctp.mainArena.name == null)) {
                    ctp.mainArena.lobby = lobby;
                }
                arenaConf.setProperty("Lobby.X", Double.valueOf(lobby.x));
                arenaConf.setProperty("Lobby.Y", Double.valueOf(lobby.y));
                arenaConf.setProperty("Lobby.Z", Double.valueOf(lobby.z));
                arenaConf.setProperty("Lobby.Dir", Double.valueOf(lobby.dir));
                arenaConf.save();
                player.sendMessage(ChatColor.GREEN + ctp.editingArenaName + ChatColor.WHITE + " arena lobby created");
                return;
            }
            player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("arenalist")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.arenalist"})) {
                String arenas = "";
                boolean firstTime = true;
                for (String arena : ctp.arena_list) {
                    if (firstTime) {
                        arenas = arena;
                        firstTime = false;
                    } else {
                        arenas = arena + ", " + arenas;
                    }
                }
                player.sendMessage("Arena list:");
                player.sendMessage(arenas);
                return;
            }
            player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("pointlist")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.pointlist"})) {
                if (ctp.editingArenaName.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }

                ArenaData arena = ctp.loadArena(ctp.editingArenaName);
                String points = "";
                boolean firstTime = true;

                //Kj -- s -> aPoint
                for (CTPPoints aPoint : arena.capturePoints) {
                    if (firstTime) {
                        points = aPoint.name;
                        firstTime = false;
                    } else {
                        points = aPoint.name + ", " + points;
                    }
                }
                player.sendMessage(ChatColor.GREEN + ctp.editingArenaName + ChatColor.WHITE + " point list:");
                player.sendMessage(points);
                return;
            }
            player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        if (arg.equalsIgnoreCase("setboundary")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.setboundary"})) {
                if (parameters.size() < 4) {
                    player.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build setboundary <1 | 2>");
                    return;
                }
                if (ctp.editingArenaName.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }

                Location loc = player.getLocation();
                if (arg2.equalsIgnoreCase("1")) {
                    if (ctp.editingArenaName.equalsIgnoreCase(ctp.mainArena.name)) {
                        ctp.mainArena.x1 = loc.getBlockX();
                        ctp.mainArena.z1 = loc.getBlockZ();
                    }

                    File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + ctp.editingArenaName + ".yml");
                    Configuration arenaConf = new Configuration(arenaFile);
                    arenaConf.load();
                    arenaConf.setProperty("Boundarys.X1", Integer.valueOf(loc.getBlockX()));
                    arenaConf.setProperty("Boundarys.Z1", Integer.valueOf(loc.getBlockZ()));
                    arenaConf.save();

                    player.sendMessage("First boundary point set.");
                } else if (arg2.equalsIgnoreCase("2")) {
                    if (ctp.editingArenaName.equalsIgnoreCase(ctp.mainArena.name)) {
                        ctp.mainArena.x2 = loc.getBlockX();
                        ctp.mainArena.z2 = loc.getBlockZ();
                    }

                    File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + ctp.editingArenaName + ".yml");
                    Configuration arenaConf = new Configuration(arenaFile);
                    arenaConf.load();
                    arenaConf.setProperty("Boundarys.X2", Integer.valueOf(loc.getBlockX()));
                    arenaConf.setProperty("Boundarys.Z2", Integer.valueOf(loc.getBlockZ()));
                    arenaConf.save();

                    player.sendMessage("Second boundary point set.");
                }

                return;
            }
        }
        
        if (arg.equalsIgnoreCase("setpoint")) {
                Location loc = player.getLocation();
                if (arg.equalsIgnoreCase("1")) {
                    ctp.x1 = loc.getBlockX();
                    ctp.y1 = loc.getBlockY();
                    ctp.z1 = loc.getBlockZ();
                } else if (arg.equalsIgnoreCase("2")) {
                    ctp.x2 = loc.getBlockX();
                    ctp.y2 = loc.getBlockY();
                    ctp.z2 = loc.getBlockZ();
                }

                return;
            }
        
        // Kj
        if (arg.equalsIgnoreCase("maximumplayers") || arg.equalsIgnoreCase("maxplayers") || arg.equalsIgnoreCase("max")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.maximumplayers"})) {
                if (parameters.size() < 4) {
                    player.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build maximumplayers <number>");
                    return;
                }
                if (ctp.editingArenaName.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }
                ArenaData arena = ctp.loadArena(ctp.editingArenaName);
                
                int amount = 0;
                try {
                    amount = Integer.parseInt(arg2);
                } catch (Exception ex) {
                    player.sendMessage(ChatColor.WHITE + arg2 + " is not a number.");
                    return;
                }
                
                File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + ctp.editingArenaName + ".yml");
                Configuration arenaConf = new Configuration(arenaFile);
                arenaConf.load();
                arenaConf.setProperty("MaximumPlayers", amount);
                arenaConf.save();
                
                arena.maximumPlayers = amount;
                player.sendMessage(ChatColor.GREEN + "Set maximum players of " + arena.name + " to " + amount + ".");
                return;
            }
            player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }
        // Kj
        if (arg.equalsIgnoreCase("minimumplayers") || arg.equalsIgnoreCase("minplayers") || arg.equalsIgnoreCase("min")) {
            if (canAccess(player, false, new String[]{"ctp.*", "ctp.admin", "ctp.admin.minimumplayers"})) {
                if (parameters.size() < 4) {
                    player.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GREEN + "/ctp build minimumplayers <number>");
                    return;
                }
                if (ctp.editingArenaName.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No arena selected!");
                    return;
                }
                ArenaData arena = ctp.loadArena(ctp.editingArenaName);
                
                int amount = 0;
                try {
                    amount = Integer.parseInt(arg2);
                } catch (Exception ex) {
                    player.sendMessage(ChatColor.WHITE + arg2 + " is not a number.");
                    return;
                }
                
                File arenaFile = new File("plugins/CaptureThePoints" + File.separator + "Arenas" + File.separator + ctp.editingArenaName + ".yml");
                Configuration arenaConf = new Configuration(arenaFile);
                arenaConf.load();
                arenaConf.setProperty("MinimumPlayers", amount);
                arenaConf.save();
                
                arena.minimumPlayers = amount;
                player.sendMessage(ChatColor.GREEN + "Set minimum players of " + arena.name + " to " + amount + ".");
                return;
            }
            player.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }
/*
        if (arg.equalsIgnoreCase("save")) {
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
                    }
                }
            }
            return;
        }
*/

        if (arg.equalsIgnoreCase("restore")) {
            return;
        }
           
    }
}
