package com.lexiqb.awakening.data;

import com.lexiqb.awakening.world.Portal;
import com.lexiqb.awakening.world.World;
import com.rubynaxela.kyanite.game.GameContext;
import com.rubynaxela.kyanite.graphics.Color;

import java.util.List;

public class WorldData {

    public String background;
    public int width, height;

    public List<PortalData> portals;

    public World build() {
        final var world = new World(width, height, GameContext.getInstance().getAssetsBundle().get(background));
        for (final var portalData : portals) {
            final var portal = portalData.build();
            world.add(portal);
        }
        return world;
    }

    public static class PortalData {

        public int x, y;
        public String color, facing, destination;

        Portal build() {
            final boolean altTexture = facing.equalsIgnoreCase("right");
            final var portal = new Portal(Color.parse(color), altTexture);
            portal.setPosition(x, y);
            return portal;
        }
    }
}
