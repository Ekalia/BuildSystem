/*
 * Copyright (c) 2023, Thomas Meaney
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package de.eintosti.buildsystem.world.data;

import de.eintosti.buildsystem.Messages;
import de.eintosti.buildsystem.world.BuildWorld;
import org.bukkit.entity.Player;

import java.util.Locale;

public enum WorldStatus {

    /**
     * Represent a world that has not been modified.
     */
    NOT_STARTED("status_not_started", 1),

    /**
     * Represents a world that is currently being built.
     * <p>
     * The status is automatically switched to this when a block is placed/broken.
     */
    IN_PROGRESS("status_in_progress", 2),

    /**
     * Represents a world that has almost been completed.
     */
    ALMOST_FINISHED("status_almost_finished", 3),

    /**
     * Represents a world that has completed its building phase.
     */
    FINISHED("status_finished", 4),

    /**
     * Represents an old world that has been finished for a while. Blocks cannot be placed/broken in archived worlds.
     */
    ARCHIVE("status_archive", 5),

    /**
     * Represents a world that is not shown in the navigator.
     */
    HIDDEN("status_hidden", 6);

    private final String typeNameKey;
    private final int stage;

    WorldStatus(String typeNameKey, int stage) {
        this.typeNameKey = typeNameKey;
        this.stage = stage;
    }

    /**
     * Gets the display name of the status.
     *
     * @param player The player to parse the placeholders against
     * @return The type's display name
     */
    public String getName(Player player) {
        return Messages.getString(typeNameKey, player);
    }

    /**
     * Gets the permission needed to change the status.
     *
     * @return The permission needed to change the status
     */
    public String getPermission() {
        return "buildsystem.setstatus." + name().toLowerCase(Locale.ROOT).replace("_", "");
    }

    /**
     * Gets the stage in which the {@link BuildWorld} is currently in.
     * A higher value means the world is further in development.
     *
     * @return the stage in which the world is currently in.
     */
    public int getStage() {
        return stage;
    }
}