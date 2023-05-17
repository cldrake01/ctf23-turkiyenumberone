package teams.turkiyeNumberOne;

import info.gridworld.grid.Location;

public class QPlayer extends BasePlayer {

    public QPlayer(Location startLocation) {
        super(startLocation);
    }

    @Override
    public Location getMoveLocation() {
        if (getMyTeam().getFlag().beingCarried())
            return getTeam().getFlag().getLocation();
        else
            return intruderSearch() != null ? intruderSearch() : getGrid().getEmptyAdjacentLocations(getLocation()).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation()).size()));
    }
}
