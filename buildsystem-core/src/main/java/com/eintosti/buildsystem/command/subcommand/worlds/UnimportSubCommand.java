/*
 * Copyright (c) 2022, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.eintosti.buildsystem.command.subcommand.worlds;

import com.eintosti.buildsystem.BuildSystem;
import com.eintosti.buildsystem.command.subcommand.SubCommand;
import com.eintosti.buildsystem.manager.WorldManager;
import com.eintosti.buildsystem.object.world.BuildWorld;
import com.eintosti.buildsystem.tabcomplete.WorldsTabComplete;
import org.bukkit.entity.Player;

/**
 * @author einTosti
 */
public class UnimportSubCommand extends SubCommand {

    private final BuildSystem plugin;
    private final String worldName;

    public UnimportSubCommand(BuildSystem plugin, String worldName) {
        super(WorldsTabComplete.WorldsArgument.UNIMPORT);

        this.plugin = plugin;
        this.worldName = worldName;
    }

    @Override
    public void execute(Player player, String[] args) {
        WorldManager worldManager = plugin.getWorldManager();
        if (!worldManager.isPermitted(player, getArgument().getPermission(), worldName)) {
            plugin.sendPermissionMessage(player);
            return;
        }

        if (args.length > 2) {
            player.sendMessage(plugin.getString("worlds_unimport_usage"));
            return;
        }

        BuildWorld buildWorld = worldManager.getBuildWorld(worldName);
        if (buildWorld == null) {
            player.sendMessage(plugin.getString("worlds_unimport_unknown_world"));
            return;
        }

        worldManager.unimportWorld(buildWorld, true);
        player.sendMessage(plugin.getString("worlds_unimport_finished").replace("%world%", buildWorld.getName()));
    }
}