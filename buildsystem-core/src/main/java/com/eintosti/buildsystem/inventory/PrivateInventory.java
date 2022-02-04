/*
 * Copyright (c) 2022, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.eintosti.buildsystem.inventory;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.eintosti.buildsystem.BuildSystem;
import com.eintosti.buildsystem.manager.InventoryManager;
import com.eintosti.buildsystem.manager.WorldManager;
import com.eintosti.buildsystem.object.world.BuildWorld;
import com.eintosti.buildsystem.object.world.WorldStatus;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author einTosti
 */
public class PrivateInventory extends PaginatedInventory implements Listener {

    private static final int MAX_WORLDS = 36;

    private final BuildSystem plugin;
    private final InventoryManager inventoryManager;
    private final WorldManager worldManager;

    public PrivateInventory(BuildSystem plugin) {
        this.plugin = plugin;
        this.inventoryManager = plugin.getInventoryManager();
        this.worldManager = plugin.getWorldManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private Inventory createInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, plugin.getString("private_title"));

        int numOfWorlds = (numOfWorlds(player) / MAX_WORLDS) + (numOfWorlds(player) % MAX_WORLDS == 0 ? 0 : 1);
        inventoryManager.fillMultiInvWithGlass(plugin, inventory, player, invIndex, numOfWorlds);
        addWorldCreateItem(inventory, player);

        return inventory;
    }

    private Inventory getInventory(Player player) {
        if (getInvIndex(player) == null) {
            setInvIndex(player, 0);
        }
        addWorlds(player);
        return inventories[getInvIndex(player)];
    }

    public void openInventory(Player player) {
        player.openInventory(getInventory(player));
    }

    private int numOfWorlds(Player player) {
        int numOfWorlds = 0;
        for (BuildWorld buildWorld : worldManager.getBuildWorlds()) {
            if (isValid(player, buildWorld)) {
                numOfWorlds++;
            }
        }
        return numOfWorlds;
    }

    private void addWorlds(Player player) {
        int columnWorld = 9, maxColumnWorld = 44;
        int numWorlds = numOfWorlds(player);
        int numInventories = (numWorlds % MAX_WORLDS == 0 ? numWorlds : numWorlds + 1) != 0 ? (numWorlds % MAX_WORLDS == 0 ? numWorlds : numWorlds + 1) : 1;

        inventories = new Inventory[numInventories];
        Inventory inventory = createInventory(player);

        int index = 0;
        inventories[index] = inventory;
        if (numWorlds == 0) {
            inventoryManager.addUrlSkull(inventory, 22, plugin.getString("private_no_worlds"), "https://textures.minecraft.net/texture/2e3f50ba62cbda3ecf5479b62fedebd61d76589771cc19286bf2745cd71e47c6");
            return;
        }

        List<BuildWorld> buildWorlds = inventoryManager.sortWorlds(player, worldManager, plugin);
        for (BuildWorld buildWorld : buildWorlds) {
            if (isValid(player, buildWorld)) {
                inventoryManager.addWorldItem(player, inventory, columnWorld++, buildWorld);
            }

            if (columnWorld > maxColumnWorld) {
                columnWorld = 9;
                inventory = createInventory(player);
                inventories[++index] = inventory;
            }
        }
    }

    private boolean isValid(Player player, BuildWorld buildWorld) {
        if (!buildWorld.isPrivate() || buildWorld.getStatus() == WorldStatus.HIDDEN) {
            return false;
        }

        String worldPermission = buildWorld.getPermission();
        if (worldPermission.equalsIgnoreCase("-") || player.hasPermission(worldPermission)) {
            World world = Bukkit.getWorld(buildWorld.getName());
            return world != null || !buildWorld.isLoaded();
        }

        return false;
    }

    private void addWorldCreateItem(Inventory inventory, Player player) {
        BuildWorld buildWorld = worldManager.getBuildWorld(player.getName());
        if (buildWorld != null || !player.hasPermission("buildsystem.createprivate")) {
            inventoryManager.addGlassPane(plugin, player, inventory, 49);
            return;
        }
        inventoryManager.addUrlSkull(inventory, 49, plugin.getString("private_create_world"), "https://textures.minecraft.net/texture/3edd20be93520949e6ce789dc4f43efaeb28c717ee6bfcbbe02780142f716");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!inventoryManager.checkIfValidClick(event, "private_title")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack itemStack = event.getCurrentItem();
        Material itemType = itemStack.getType();

        if (itemType == XMaterial.PLAYER_HEAD.parseMaterial()) {
            switch (event.getSlot()) {
                case 45:
                    decrementInv(player);
                    XSound.ENTITY_CHICKEN_EGG.play(player);
                    openInventory(player);
                    break;
                case 49:
                    player.closeInventory();
                    XSound.BLOCK_CHEST_OPEN.play(player);
                    plugin.getCreateInventory().openInventory(player, CreateInventory.Page.PREDEFINED);
                    worldManager.createPrivateWorldPlayers.add(player);
                    break;
                case 53:
                    incrementInv(player);
                    XSound.ENTITY_CHICKEN_EGG.play(player);
                    openInventory(player);
                    break;
            }
        }

        inventoryManager.manageInventoryClick(event, player, itemStack);
    }
}