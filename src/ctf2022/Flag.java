package ctf2022;

import info.gridworld.actor.Actor;
import info.gridworld.grid.Location;

public class Flag extends Actor {

    /**
     * A flag object that belongs to a specific team.
     */

    private Team team;
    private Player carrier;

    /**
     * Creates a new Flag object.
     *
     * @param team The team that the flag belongs to.
     */

    public Flag(Team team) {
        this.team = team;
        setColor(team.getColor());
    }

    /**
     * Overrides the act method to do nothing because flags should not act on their own.
     */

    public void act() {
    }

    /**
     * Allows a player to pick up the flag.
     *
     * @param player The player that is picking up the flag.
     */

    protected void pickUp(Player player) {
        super.removeSelfFromGrid();
        this.carrier = player;
    }

    /**
     * Overrides the removeSelfFromGrid method to prevent cheating.
     */

    public final void removeSelfFromGrid() {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.endsWith("CtfWorld")) super.removeSelfFromGrid();
        else {
            System.err.println("Someone has cheated and tried to remove a player from the grid");
            CtfWorld.extra += " Cheat";
        }
    }

    /**
     * Returns the team that the flag belongs to.
     *
     * @return The team that the flag belongs to.
     */

    public Team getTeam() {
        return team;
    }

    /**
     * Returns the location of the flag or its carrier.
     *
     * @return The location of the flag or its carrier.
     */

    public Location getLocation() {
        if (getGrid() == null && carrier != null) return carrier.getLocation();
        return new Location(super.getLocation().getRow(), super.getLocation().getCol());
    }

    /**
     * Returns whether the flag is being carried by a player.
     *
     * @return Whether the flag is being carried by a player.
     */

    public boolean beingCarried() {
        return getGrid() == null && carrier != null;
    }
}
