package ctf2022;

import info.gridworld.actor.Actor;
import info.gridworld.grid.Location;

public class Flag extends Actor {

    private Team team;
    private Player carrier;

    public Flag(Team team) {
        this.team = team;
        setColor(team.getColor());
    }

    // Overridden because default behavior for act is undesirable
    public void act() {
    } // I want only players to get called to act

    protected void pickUp(Player player) {
        super.removeSelfFromGrid();
        this.carrier = player;
    }

    public final void removeSelfFromGrid() {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.endsWith("CtfWorld"))
            super.removeSelfFromGrid();
        else {
            System.err.println("Someone has cheated and tried to remove a player from the grid");
            CtfWorld.extra += " Cheat";
        }
    }

    public Team getTeam() {
        return team;
    }

    public Location getLocation() {
        if (getGrid() == null && carrier != null)
            return carrier.getLocation();
        return new Location(super.getLocation().getRow(), super.getLocation().getCol());
    }

    public boolean beingCarried() {
        return getGrid() == null && carrier != null;
    }
}
