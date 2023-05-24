package teams.turkiyeNumberOne;

import ctf.CTFWorld;
import ctf.Flag;
import ctf.Player;
import info.gridworld.actor.Rock;
import info.gridworld.grid.Location;

import java.util.ArrayList;

public class BasePlayer extends ctf.Player {

    Location lastLocation = null;

    /**
     * Constructs a new Player with its desired starting Location
     *
     * @param startLocation the desired starting Location
     */
    public BasePlayer(Location startLocation) {
        super(startLocation);
    }

    /**
     * Attempts to evade other players by moving in the opposite direction of the enemy flag.
     *
     * @return The location of the player after the evasion, or null if there are no adjacent enemy players or
     * if the player does not have the flag.
     */
    public Location evade() {
        Location adjacentLocation = getLocation().getAdjacentLocation(getLocation().getDirectionToward(getMyTeam().getFlag().getLocation()));

        int size = getGrid().getEmptyAdjacentLocations(adjacentLocation).size();

        if (getGrid().get(adjacentLocation) instanceof Rock && size > 0)
            return getGrid().getEmptyAdjacentLocations(adjacentLocation).get((int) (Math.random() * size));

        for (Location loc : getGrid().getOccupiedAdjacentLocations(adjacentLocation))
            if (getGrid().get(loc) instanceof Player && ((Player) getGrid().get(loc)).getTeam().equals(getOtherTeam())) {
                boolean isHigherOnGrid = loc.getRow() <= getLocation().getRow();

                System.out.println("loc: " + loc.getRow() + " getLoc: " + getLocation().getRow());

                System.out.println(getGrid().get(loc) + " isHigherOnGrid: " + isHigherOnGrid);

                return isHigherOnGrid
                        ? getLocation().getAdjacentLocation(Location.NORTH)
                        : getLocation().getAdjacentLocation(Location.SOUTH);
            }

        return null;
    }

    /**
     * Searches for enemy players on the grid and returns the location of the first enemy player found.
     *
     * @return The location of the first enemy player found, or null if there are no enemy players on the grid.
     */
    public Location intruderSearch() {
        for (Location loc : getGrid().getOccupiedLocations())
            if (getGrid().get(loc) instanceof Player && ((Player) getGrid().get(loc)).getTeam().equals(getOtherTeam()) && !getOtherTeam().onSide(loc)) {

                Location adjacentLocation = getLocation().getAdjacentLocation(getLocation().getDirectionToward(loc));

                int size = getGrid().getEmptyAdjacentLocations(getLocation()).size();

                return getGrid().get(adjacentLocation) instanceof Rock && size > 0
                        ? getGrid().getEmptyAdjacentLocations(getLocation()).get((int) (Math.random() * size))
                        : loc;
            }

        return null;
    }


    /**
     * Returns the objective location of the player based on the given location.
     *
     * @param loc The location of the player.
     * @return The objective location of the player, or null if the location is not a valid objective location.
     */
    public Location getImmediateObjectiveLocation(Location loc) {
        if (getGrid().get(loc) instanceof Player && ((Player) getGrid().get(loc)).getTeam() != this.getTeam() && !getOtherTeam().onSide(loc))
            return loc;
        else if (getGrid().get(getLocation().getAdjacentLocation(getLocation().getDirectionToward(getOtherTeam().getFlag().getLocation()))) instanceof Rock && getGrid().getEmptyAdjacentLocations(getLocation()).size() > 0)
            return getGrid().getEmptyAdjacentLocations(getLocation()).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation()).size()));
        else if (getGrid().get(loc) instanceof Flag && ((Flag) getGrid().get(loc)).getTeam() != this.getTeam())
            return loc;
        else
            return null; // the reason we return null is to allow for several iterations of this method to be called in a row.
    }

    /**
     * Searches for the immediate objective location in the surrounding locations of the current location.
     *
     * @return the immediate objective location if found, otherwise null.
     * The reason for returning null is to allow for class-specific behavior, which may follow a call to this method.
     */
    public Location searchSurroundings() {
        for (Location loc : getGrid().getOccupiedAdjacentLocations(getLocation()))
            if (getImmediateObjectiveLocation(loc) != null) return getImmediateObjectiveLocation(loc);
        return null;
    }

    /**
     * Bounces the player in the opposite direction of the flag if the player is within a certain distance of the flag.
     *
     * @return a location in the opposite direction of the flag.
     */
    public Location newBounce() {

        Location northAdjacentLocation = getLocation().getAdjacentLocation(Location.NORTH);
        Location southAdjacentLocation = getLocation().getAdjacentLocation(Location.SOUTH);

        if (getMyTeam().nearFlag(getLocation().getAdjacentLocation(getLocation().getDirectionToward(getMyTeam().getFlag().getLocation()))))
            return (getMyTeam().getFlag().getLocation().getRow() >= getLocation().getRow())
                    ?
                    (
                            getGrid().get(northAdjacentLocation) instanceof Rock
                                    ? getGrid().getEmptyAdjacentLocations(northAdjacentLocation).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(northAdjacentLocation).size()))
                                    : northAdjacentLocation
                    )
                    :
                    (
                            getGrid().get(southAdjacentLocation) instanceof Rock
                                    ? getGrid().getEmptyAdjacentLocations(southAdjacentLocation).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(southAdjacentLocation).size()))
                                    : southAdjacentLocation
                    );
        else
            return null;
    }


    /**
     * This method is never called.
     *
     * @return
     */
    @Override
    public Location getMoveLocation() {
        return null;
    }
}
