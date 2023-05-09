package teams.turkiyeNumberOne;

import ctf.Player;

import info.gridworld.actor.Rock;
import info.gridworld.grid.Location;

public class BeelinePlayer extends Player {

    public BeelinePlayer(Location startLocation) {
        super(startLocation);
    }

    public Location getMoveLocation() {
        if (this.hasFlag())
            return evade() != null ? evade() : this.getMyTeam().getFlag().getLocation();
        else
            return searchSurroundings() != null ? searchSurroundings() : this.getOtherTeam().getFlag().getLocation();
    }
}
