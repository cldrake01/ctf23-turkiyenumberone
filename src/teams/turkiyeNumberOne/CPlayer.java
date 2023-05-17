package teams.turkiyeNumberOne;

import ctf.Player;
import info.gridworld.grid.Location;

public class CPlayer extends BasePlayer {

    public CPlayer(Location startLocation) {
        super(startLocation);
    }

    @Override
    public Location getMoveLocation() {
        if (getOtherTeam().getFlag().beingCarried())
            return evade() != null ? evade() : getMyTeam().getFlag().getLocation();
        else if (searchSurroundings() != null)
            return bounce(searchSurroundings());
        else if (getGrid().get(getLocation().getAdjacentLocation(getLocation().getDirectionToward(getOtherTeam().getFlag().getLocation()))) instanceof Player)
            return bounce(getGrid().getEmptyAdjacentLocations(getLocation()).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation()).size())));
        else
            return bounce(getOtherTeam().getFlag().getLocation());
    }
}
