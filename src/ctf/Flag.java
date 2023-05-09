package ctf;

import info.gridworld.actor.Actor;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;

/**
 * Special Actor that acts as the Flag in a CTF Game.  If placed in the Grid, it is available
 * to be picked up.  If it exists, but is not in a Grid that means a Player has picked it up and
 * is actively carrying it.
 */
public class Flag extends Actor {

    private Team team;
    private Player carrier;

    /**
     * Constructs a new Flag.  This is only done when a Team is created and is only allowed by the CTF Team superclass.
     * @param team
     */
    public Flag(Team team) {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.equals("ctf.Team")) {
            this.team = team;
            setColor(team.getColor());
        }
        else {
            CTFWorld.addExtraText("Cheat");
            System.err.println(callingClass + " has cheated and tried to construct a Flag");
        }
    }

    /**
     * Adds the Flag to a Grid, meaning that it is available to be picked up.
     * This can only be done by the ActorWorld class at Construction time or by the Player
     * superclass when a Flag is dropped and becomes available to pick up again.
     *
     * @param grid the grid into which to place the Flag
     * @param loc the location into which the Flag should be placed
     */
    public final void putSelfInGrid(Grid<Actor> grid, Location loc) {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.equals("info.gridworld.actor.ActorWorld") || callingClass.equals("ctf.Player")) {
            super.putSelfInGrid(grid, loc);
        }
        else {
            CTFWorld.addExtraText("Cheat");
            System.err.println(callingClass + " has cheated and tried to add a Flag to the grid");
        }
    }

    /**
     * Flags do not act
      */
    public final void act() {
    }

    protected void pickUp(Player player) {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.equals("ctf.Player")) {
            super.removeSelfFromGrid();
            this.carrier = player;
        }
        else {
            CTFWorld.addExtraText("Cheat");
            System.err.println(callingClass + " has cheated and tried to pic up the Flag");
        }
    }

    /**
     * This method is prohibited to be called by any Players
     */
    public final void removeSelfFromGrid() {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
//        System.out.println(callingClass);
//        if (callingClass.endsWith("CtfWorld"))
//            super.removeSelfFromGrid();
//        else {
            CTFWorld.addExtraText("Cheat");
            System.err.println(callingClass + "has cheated and tried to remove the Flag");
//        }
    }

    /**
     * Gets the team that owns  this Flag
     * @return the team that owns thi Flag
     */
    public Team getTeam() {
        return team;
    }

    /**
     * Returns the location of the current Flag.  If it is being carried, it will return the Location
     * of the Player that is currently carrying it.  If it is not being carried, it will return the
     * Location of the Flag instance in the Grid.
     *
     * @return the Location of the Flag whether or not it is being carried
     */
    public Location getLocation() {
        if (getGrid() == null && carrier != null)
            return carrier.getLocation();
        return new Location(super.getLocation().getRow(), super.getLocation().getCol());
    }

    /**
     * Returns an indication of whether or not the Flag is currently being carried
     * @return whether or not the Flag is currently being carried
     */
    public boolean beingCarried() {
        return getGrid() == null && carrier != null;
    }
}
