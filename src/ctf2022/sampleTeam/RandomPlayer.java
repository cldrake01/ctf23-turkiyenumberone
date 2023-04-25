package ctf2022.sampleTeam;

import java.util.List;

import ctf2022.Player;

import info.gridworld.grid.Location;

public class RandomPlayer extends Player {

    public RandomPlayer(Location startLocation) {
        super(startLocation);
    }

    public Location getMoveLocation() {
        List<Location> possibleMoveLocations = getGrid().getEmptyAdjacentLocations(getLocation());
        if (possibleMoveLocations.size() == 0) return null;
        return possibleMoveLocations.get((int) (Math.random() * possibleMoveLocations.size()));
    }
}
