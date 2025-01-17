/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package de.eintosti.buildsystem.command.subcommand.worlds;

import de.eintosti.buildsystem.BuildSystem;
import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.command.subcommand.Argument;
import de.eintosti.buildsystem.command.subcommand.SubCommand;
import de.eintosti.buildsystem.tabcomplete.WorldsTabComplete;
import de.eintosti.buildsystem.util.ArgumentParser;
import de.eintosti.buildsystem.util.UUIDFetcher;
import de.eintosti.buildsystem.world.Builder;
import de.eintosti.buildsystem.world.WorldManager;
import de.eintosti.buildsystem.world.generator.Generator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

public class ImportAllSubCommand implements SubCommand {

    private final BuildSystem plugin;

    public ImportAllSubCommand(BuildSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!hasPermission(player)) {
            plugin.sendPermissionMessage(player);
            return;
        }

        if (args.length != 1) {
            Messages.sendMessage(player, "worlds_importall_usage");
            return;
        }

        WorldManager worldManager = plugin.getWorldManager();
        if (worldManager.isImportingAllWorlds()) {
            Messages.sendMessage(player, "worlds_importall_already_started");
            return;
        }

        File worldContainer = Bukkit.getWorldContainer();
        String[] directories = worldContainer.list((dir, name) -> {
            File worldFolder = new File(dir, name);
            if (!worldFolder.isDirectory()) {
                return false;
            }

            if (!new File(worldFolder, "level.dat").exists()) {
                return false;
            }

            return worldManager.getBuildWorld(name) == null;
        });

        if (directories == null || directories.length == 0) {
            Messages.sendMessage(player, "worlds_importall_no_worlds");
            return;
        }

        ArgumentParser parser = new ArgumentParser(args);
        Generator generator = Generator.VOID;
        Builder builder = new Builder(null, "-");

        if (parser.isArgument("g")) {
            String generatorArg = parser.getValue("g");
            if (generatorArg == null) {
                Messages.sendMessage(player, "worlds_importall_usage");
                return;
            }
            try {
                generator = Generator.valueOf(generatorArg.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (parser.isArgument("c")) {
            String creatorArg = parser.getValue("c");
            if (creatorArg == null) {
                Messages.sendMessage(player, "worlds_importall_usage");
                return;
            }
            UUID creatorId = UUIDFetcher.getUUID(creatorArg);
            if (creatorId == null) {
                Messages.sendMessage(player, "worlds_importall_player_not_found");
                return;
            }
            builder = new Builder(creatorId, creatorArg);
        }

        worldManager.importWorlds(player, directories, generator, builder);
    }

    @Override
    public Argument getArgument() {
        return WorldsTabComplete.WorldsArgument.IMPORT_ALL;
    }
}