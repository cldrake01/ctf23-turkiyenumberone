package ctf2022;

import info.gridworld.actor.Actor;
import info.gridworld.actor.Rock;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public abstract class Player extends Actor {

    // point values for different actions
    private static final int MOVE = 1;
    private static final int MOVE_ON_OPPONENT_SIDE = 2;
    private static final int CAPTURE = 50;
    private static final int TAG = 20;
    private static final int CARRY = 5;

    // The time the whole team has, in milliseconds. Each player is individually capped on time, not the team
    private static final int TURNTIME = 500;

    private Team team;
    private boolean hasFlag;
    private Location startLocation;
    private int tagCoolDown;

    public Player(Location startLocation) {
        this.startLocation = startLocation;
    }

    public final void act() {
        try {
            if (team.hasWon() || team.getOpposingTeam().hasWon()) {
                if (team.hasWon()) if (hasFlag) setColor(Color.MAGENTA);
                else setColor(Color.YELLOW);
                return;
            }

            if (tagCoolDown > 0) {
                setColor(Color.BLACK);
                tagCoolDown--;
                if (tagCoolDown == 0) setColor(team.getColor());
            } else {
                processNeighbors();

                Location loc = new Location(-1, -1);
                Thread getMoveLocationThread = new Thread() {
                    @Override
                    public void run() {
                        Location l = getMoveLocation();
                        loc.setCol(l.getCol());
                        loc.setRow(l.getRow());
                    }
                };
                getMoveLocationThread.start();
                long timeLimit = TURNTIME / team.getPlayers().size();
                long startTime = System.currentTimeMillis();
                while (!this.getGrid().isValid(loc) && System.currentTimeMillis() - startTime < timeLimit) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (getMoveLocationThread.isAlive()) {
                    getMoveLocationThread.interrupt();
                    System.out.println("Player ran out of time: " + this);
                    CtfWorld.extra += " Time";
                }

                makeMove(!this.getGrid().isValid(loc) ? null : loc); // null = don't move
            }
        } catch (Exception e) {
            CtfWorld.extra += " Err";
            System.err.println("Player " + this + " has generated a runtime exception");
            e.printStackTrace();
        }
    }

    private void processNeighbors() {
        List<Location> neighborLocations = getGrid().getOccupiedAdjacentLocations(getLocation());
        for (int i = neighborLocations.size() - 1; i >= 0; i--) {
            Actor neighbor = getGrid().get(neighborLocations.get(i));
            if (!(neighbor instanceof Player) || ((Player) neighbor).team.equals(team)) {
                neighborLocations.remove(i);
                if (neighbor instanceof Flag && !((Flag) neighbor).getTeam().equals(team)) {
                    hasFlag = true;
                    setColor(Color.YELLOW);
                    team.getOpposingTeam().getFlag().pickUp(this);
                    team.addScore(CAPTURE);
                    team.addPickUp();
                }
            }
        }
        if (team.onSide(getLocation())) {
            Collections.shuffle(neighborLocations);
            for (Location neighborLocation : neighborLocations) {
                if (team.onSide(neighborLocation)) {
                    Actor neighbor = getGrid().get(neighborLocation);
                    if (((Player) neighbor).hasFlag() || Math.random() < (1. / neighborLocations.size())) {
                        ((Player) neighbor).tag();
                        team.addScore(TAG);
                        team.addTag();
                    }
                }
            }
        }
    }

    private void makeMove(Location loc) {
        // if null, treat as if you are staying in same location
        if (loc == null) loc = getLocation();

        // limit to one step towards desired location
        if (!loc.equals(getLocation())) loc = getLocation().getAdjacentLocation(getLocation().getDirectionToward(loc));

        // Player is too close to own flag and not moving away from it, it must bounce
        if (team.onSide(getLocation()) && getGrid().get(team.getFlag().getLocation()) instanceof Flag && team.nearFlag(getLocation()) && team.nearFlag(loc)) {
            loc = bounce();
            CtfWorld.extra += " Bounce";
        }
        // if Player is on own side and flag isn't being carried, it can't move too close to own flag
        if (team.onSide(getLocation()) && getGrid().get(team.getFlag().getLocation()) instanceof Flag && team.nearFlag(loc)) {
            CtfWorld.extra += " Close to flag";
            bounce();
        }

        // move to loc and score appropriate points
        if (!loc.equals(getLocation()) && getGrid().isValid(loc) && getGrid().get(loc) == null) {
            this.setDirection(getLocation().getDirectionToward(loc));
            moveTo(loc);
            if (team.onSide(getLocation())) team.addScore(MOVE);
            else team.addScore(MOVE_ON_OPPONENT_SIDE);
            team.addOffensiveMove();
            if (this.hasFlag) team.addScore(CARRY);
        }
    }

    /**
     * Bounces the player in the opposite direction of the flag if the player is within a certain distance of the flag.
     *
     * @return a location in the opposite direction of the flag.
     */
    private Location bounce() {
        return getMyTeam().getFlag().getLocation().getRow() >= getLocation().getRow() ? getLocation().getAdjacentLocation(Location.NORTH) : getLocation().getAdjacentLocation(Location.SOUTH);
    }

    /**
     * Attempts to evade other players by moving in the opposite direction of the enemy flag.
     *
     * @return The location of the player after the evasion, or null if there are no adjacent enemy players or if the player does not have the flag.
     */
    public Location evade() {
        for (Location loc : getGrid().getOccupiedAdjacentLocations(getLocation()))
            if (hasFlag() && getGrid().get(loc) instanceof Player && ((Player) getGrid().get(loc)).getTeam().equals(getOtherTeam()))
                return loc.getRow() >= getLocation().getRow() ? getLocation().getAdjacentLocation(Location.NORTH) : getLocation().getAdjacentLocation(Location.SOUTH);
        return null;
    }

    /**
     * Searches for enemy players on the grid and returns the location of the first enemy player found.
     *
     * @return The location of the first enemy player found, or null if there are no enemy players on the grid.
     */
    public Location intruderSearch() {
        for (Location loc : getGrid().getOccupiedLocations())
            if (getGrid().get(loc) instanceof Player && ((Player) getGrid().get(loc)).getTeam().equals(getOtherTeam()) && loc.getCol() < (getMyTeam().getSide() == 0 ? getGrid().getNumCols() / 2 : getGrid().getNumCols()) && loc.getCol() > (getMyTeam().getSide() == 0 ? 0 : getGrid().getNumCols() / 2))
                return loc;
        return null;
    }

    /**
     * Returns the objective location of the player based on the given location.
     *
     * @param loc The location of the player.
     * @return The objective location of the player, or null if the location is not a valid objective location.
     */
    public Location getImmediateObjectiveLocation(Location loc) {
        if (getGrid().get(loc) instanceof Player && ((Player) getGrid().get(loc)).getTeam() != this.getTeam() && ((Player) getGrid().get(loc)).getTeam().getSide() == getMyTeam().getSide())
            return loc;
        else if (getGrid().get(loc) instanceof Rock)
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
     *         The reason for returning null is to allow for class-specific behavior, which may follow a call to this method.
     */
    public Location searchSurroundings() {
        for (Location loc : getGrid().getOccupiedAdjacentLocations(getLocation()))
            if (getImmediateObjectiveLocation(loc) != null) return getImmediateObjectiveLocation(loc);
        return null;
    }

    public abstract Location getMoveLocation();

    private void tag() {
        Location oldLoc = getLocation();
        Location nextLoc;
        do nextLoc = team.adjustForSide(new Location((int) (Math.random() * getGrid().getNumRows()), 0), getGrid());
        while (getGrid().get(nextLoc) != null);
        moveTo(nextLoc);
        tagCoolDown = 10;
        if (hasFlag) {
            team.getOpposingTeam().getFlag().putSelfInGrid(getGrid(), oldLoc);
            hasFlag = false;
        }
        setColor(Color.BLACK);
    }

    protected final void putSelfInGridProtected(Grid<Actor> grid, Location loc) {
        if (getGrid() != null) super.removeSelfFromGrid();
        hasFlag = false;
        tagCoolDown = 0;
        setColor(team.getColor());
        super.putSelfInGrid(grid, loc);
    }

    public final void removeSelfFromGrid() {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.endsWith("CtfWorld")) super.removeSelfFromGrid();
        else {
            System.err.println("Someone has cheated and tried to remove a player from the grid");
            CtfWorld.extra += " Cheat";
        }
    }

    protected final void setTeam(Team team) {
        this.team = team;
        setColor(team.getColor());
    }

    protected final void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public final boolean hasFlag() {
        return hasFlag;
    }

    protected final Location getStartLocation() {
        return startLocation;
    }

    public final Team getTeam() {
        return team;
    }

    public final Team getMyTeam() {
        return team;
    }

    public final Team getOtherTeam() {
        return team.getOpposingTeam();
    }

    public final Location getLocation() {
        return new Location(super.getLocation().getRow(), super.getLocation().getCol());
    }

    public final void moveTo(Location loc) {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.endsWith("Player")) super.moveTo(loc);
        else {
            CtfWorld.extra += " Cheat";
            System.out.println("This Player has attempted an unauthorized moveTo");
        }
    }

    public String toString() {
        return "Player";
    }
}
