package teams.turkiyeNumberOne;

import java.util.List;

import ctf.Flag;
import ctf.Player;

import info.gridworld.actor.Rock;
import info.gridworld.grid.Location;

public class RandomPlayer extends Player {

    public RandomPlayer(Location startLocation) {
        super(startLocation);
    }

    public Location getMoveLocation() {
        if (getMyTeam().getFlag().beingCarried())
            return getTeam().getFlag().getLocation();
        else
            return intruderSearch() != null ? intruderSearch() : getGrid().getEmptyAdjacentLocations(getLocation()).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation()).size()));
    }
}
