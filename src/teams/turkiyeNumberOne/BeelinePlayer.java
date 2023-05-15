package teams.turkiyeNumberOne;

import ctf.Player;
import info.gridworld.grid.Location;

import java.util.Map;

public class BeelinePlayer extends Player {

    public BeelinePlayer(Location startLocation) {
        super(startLocation);
    }

    public Location getMoveLocation() {
        if (getOtherTeam().getFlag().beingCarried())
            return evade() != null ? evade() : getMyTeam().getFlag().getLocation();
        else
            return searchSurroundings() != null
                    ? searchSurroundings()
                    : (getGrid().get(getLocation().getAdjacentLocation(getLocation().getDirectionToward(getOtherTeam().getFlag().getLocation()))) instanceof Player ? getGrid().getEmptyAdjacentLocations(getLocation()).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation()).size())) : getOtherTeam().getFlag().getLocation());
    }
}
