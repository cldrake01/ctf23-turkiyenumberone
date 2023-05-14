package teams.turkiyeNumberOne;

import ctf.Player;
import info.gridworld.grid.Location;

public class BeelinePlayer extends Player {

    public BeelinePlayer(Location startLocation) {
        super(startLocation);
    }

    public Location getMoveLocation() {
        if (getOtherTeam().getFlag().beingCarried() || (!getMyTeam().onSide(getLocation()) && hasFlag()))
            return evade() != null && !getMyTeam().onSide(getLocation()) ? evade() : getMyTeam().getFlag().getLocation();
        else
            return searchSurroundings() != null ? searchSurroundings() : getOtherTeam().getFlag().getLocation();
    }
}
