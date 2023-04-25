package ctf2022.sampleTeam;

import ctf2022.Player;

import info.gridworld.grid.Location;

public class BeelinePlayer extends Player {

    public BeelinePlayer(Location startLocation) {
        super(startLocation);
    }

    public Location getMoveLocation() {
        int dir;
        if (this.hasFlag()) {
            return this.getMyTeam().getFlag().getLocation();
        } else {
            return getOtherTeam().getFlag().getLocation();
        }
    }
}
