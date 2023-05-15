package ctf;

import info.gridworld.actor.Actor;
import info.gridworld.actor.Rock;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * This is the basic superclass Object in the Capture the Flag World.  All Team Players must extend
 * Player at some level.  Only descendants of Player can be added to a Team.
 */
public abstract class Player extends Actor {

    private static final int MOVE = 1;
    private static final int MOVE_ON_OPPONENT_SIDE = 2;
    private static final int CAPTURE = 50;
    private static final int TAG = 20;
    private static final int CARRY = 5;

    private static final int TURN_MAX_TIME = 100; // The time in milliseconds a player has to calculate a move Loc

    private Team team;
    private boolean hasFlag;
    private final Location startLocation;
    private int tagCoolDown;
    private int tagCount;

    /**
     * Constructs a new Player with its desired starting Location
     *
     * @param startLocation the desired starting Location
     */
    public Player(Location startLocation) {
        this.startLocation = startLocation;
        this.tagCount = 0;
    }

    /**
     * This is called each time a Player is called on to act.
     * The Player follows this sequence:
     * 1) Check to see if either team has already won
     * 2) If Player has recently been tagged out, it must wait until the cool-down period has expired
     * 3) If the Player can act, it processes its immediate neighbors.  It can:
     * - pick up an adjacent Flag
     * - tag an opponent.  If the opponenet has the Flag, it will always tag out the opponent.
     * if the opponenet does not have the Flag, it will only tag out the opponent with a probability
     * based on how many neighbors are on the other team (to decrease the liklihood of multiple tags)
     * 4) Calculates the Location to move to by calling getMoveLocation.  If it takes too much time to
     * get the move Location, it may not move at all
     * 5) Moves to the new Location.  The new Location must be:
     * - valid
     * - adjacent to its current Location
     * - not too close to its own Flag
     * <p>
     * This method can only be called by CTFWorld
     */
    private volatile Location moveToLoc;

    public final void act() {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.equals("ctf.CTFWorld")) {
            try {
                if (team.hasWon() || team.getOpposingTeam().hasWon()) {
                    if (team.hasWon()) {
                        if (hasFlag) setColor(Color.MAGENTA);
                        else setColor(Color.YELLOW);
                    }
                    return;
                }

                if (tagCoolDown > 0) {
                    setColor(Color.BLACK);
                    tagCoolDown--;
                    if (tagCoolDown == 0) {
                        setColor(team.getColor());
                    }
                } else {
                    // process immediate neighbors
                    processNeighbors();

                    // set up Thread to call getMoveLocation (see private CallMove class below)
                    CallMove callThread = new CallMove();
                    moveToLoc = null;
                    callThread.start();

                    // wait for either the moveToLoc to be set by getMoveLocation or timeout
                    long timeLimit = TURN_MAX_TIME;
                    long startTime = System.currentTimeMillis();
                    while (moveToLoc == null && System.currentTimeMillis() - startTime < timeLimit) {
                        try {
                            Thread.sleep(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // if timeout - log it to err
                    if (callThread.isAlive()) {
                        callThread.interrupt();  // deprecated, but ok in this case
                        System.err.println("Player ran out of time: " + this);
                        CTFWorld.addExtraText("Timeout");
                    }

                    // move (or if timeout, stay put)
                    makeMove(moveToLoc); // null = don't move
                }
            } catch (Exception e) {
                CTFWorld.addExtraText("RuntimeError");
                System.err.println("Player " + this + " has generated a runtime exception");
                e.printStackTrace();
            }
        } else {
            CTFWorld.addExtraText("Cheat");
            System.err.println(callingClass + " has cheated and tried to make a Player act directly");
        }

    }

    // private class to encapsulate the getMoveLocation call into its own Thread (for timing)
    private class CallMove extends Thread {
        @Override
        public void run() {
            moveToLoc = getMoveLocation();
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
        if (loc == null || !getGrid().isValid(loc)) {
            loc = getLocation();
        }

        // limit to one step towards desired location
        if (!loc.equals(getLocation())) {
            loc = getLocation().getAdjacentLocation(getLocation().getDirectionToward(loc));
            this.setDirection(getLocation().getDirectionToward(loc));
        }
        // Player is too close to own flag and not moving away from it, it must bounce
        if (team.onSide(getLocation()) && getGrid().get(team.getFlag().getLocation()) instanceof Flag && team.nearFlag(getLocation()) && team.nearFlag(loc)) {
            loc = bounce();
            CTFWorld.addExtraText("Bounce");
            System.out.println("Player was relocated because it was too close to the flag: " + this);
        }

        // if Player is on own side and flag isn't being carried, it can't move too close to own flag
        if (team.onSide(getLocation()) && getGrid().get(team.getFlag().getLocation()) instanceof Flag && team.nearFlag(loc)) {
            loc = bounce();
            CTFWorld.addExtraText("Close to flag");
            System.out.println("Player prohibited from moving too close to the flag: " + this);
            loc = bounce();
        }

        // move to loc and score appropriate points
        if (!loc.equals(getLocation()) && getGrid().isValid(loc) && getGrid().get(loc) == null) {
            moveTo(loc);
            if (team.onSide(getLocation())) {
                team.addScore(MOVE);
                team.addDefensiveMove();
            } else {
                team.addScore(MOVE_ON_OPPONENT_SIDE);
                team.addOffensiveMove();
            }
            if (this.hasFlag) {
                team.addScore(CARRY);
                team.addCarry();
            }
        }
    }

    /**
     * Bounces the player in the opposite direction of the flag if the player is within a certain distance of the flag.
     *
     * @return a location in the opposite direction of the flag.
     */
    private Location bounce() {
        return getMyTeam().getFlag().getLocation().getRow() > getLocation().getRow()
                ? getLocation().getAdjacentLocation(Location.NORTH) == null ? getLocation().getAdjacentLocation(getLocation().getDirectionToward(getOtherTeam().getFlag().getLocation())) : getLocation().getAdjacentLocation(Location.SOUTH)
                : getLocation().getAdjacentLocation(Location.SOUTH) == null ? getLocation().getAdjacentLocation(getLocation().getDirectionToward(getOtherTeam().getFlag().getLocation())) : getLocation().getAdjacentLocation(Location.SOUTH);
    }

    /**
     * Attempts to evade other players by moving in the opposite direction of the enemy flag.
     *
     * @return The location of the player after the evasion, or null if there are no adjacent enemy players or if the player does not have the flag.
     */
    public Location evade() {
        for (Location loc : getGrid().getOccupiedAdjacentLocations(getLocation()))
            if (!getMyTeam().onSide(getLocation()) && getGrid().get(loc) instanceof Player && ((Player) getGrid().get(loc)).getTeam().equals(getOtherTeam()))
                return loc.getRow() >= getLocation().getRow()
                        ? getLocation().getAdjacentLocation(Location.NORTH) == null ? getLocation().getAdjacentLocation(getLocation().getDirectionToward(getMyTeam().getFlag().getLocation())) : getLocation().getAdjacentLocation(Location.SOUTH)
                        : getLocation().getAdjacentLocation(Location.SOUTH) == null ? getLocation().getAdjacentLocation(getLocation().getDirectionToward(getMyTeam().getFlag().getLocation())) : getLocation().getAdjacentLocation(Location.SOUTH);
            else if (getGrid().get(getLocation().getAdjacentLocation(getLocation().getDirectionToward(getMyTeam().getFlag().getLocation()))) instanceof Rock)
                return getGrid().getEmptyAdjacentLocations(getLocation().getAdjacentLocation(getLocation().getDirectionToward(new Location(getLocation().getRow(), getMyTeam().getFlag().getLocation().getCol())))).size() > 0
                        ? getGrid().getEmptyAdjacentLocations(getLocation().getAdjacentLocation(getLocation().getDirectionToward(new Location(getLocation().getRow(), getMyTeam().getFlag().getLocation().getCol())))).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation().getAdjacentLocation(getLocation().getDirectionToward(new Location(getLocation().getRow(), getMyTeam().getFlag().getLocation().getCol())))).size()))
                        : getGrid().getEmptyAdjacentLocations(getLocation()).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation()).size()));
        return null;
    }

    /**
     * Searches for enemy players on the grid and returns the location of the first enemy player found.
     *
     * @return The location of the first enemy player found, or null if there are no enemy players on the grid.
     */
    public Location intruderSearch() {
        for (Location loc : getGrid().getOccupiedLocations())
            if (getGrid().get(loc) instanceof Player && ((Player) getGrid().get(loc)).getTeam().equals(getOtherTeam()) && !getOtherTeam().onSide(loc))
                return getGrid().get(getLocation().getAdjacentLocation(getLocation().getDirectionToward(loc))) instanceof Rock
                        ? getGrid().getEmptyAdjacentLocations(getLocation()).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation()).size()))
                        : loc;
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
            return getOtherTeam().onSide(loc) ? getGrid().getEmptyAdjacentLocations(getLocation()).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation()).size())) : loc;
        else if (getGrid().get(getLocation().getAdjacentLocation(getLocation().getDirectionToward(getOtherTeam().getFlag().getLocation()))) instanceof Rock && getGrid().getEmptyAdjacentLocations(getLocation()).size() > 0)
            return getGrid().getEmptyAdjacentLocations(getLocation()).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation()).size()));
        else if (getGrid().get(loc) instanceof Flag && ((Flag) getGrid().get(loc)).getTeam() != this.getTeam())
            return loc;
        else if (!getMyTeam().onSide(getLocation()) && getGrid().get(loc) instanceof Player && ((Player) getGrid().get(loc)).getTeam().equals(getOtherTeam()))
            return loc.getRow() >= getLocation().getRow() ? getLocation().getAdjacentLocation(Location.NORTH) : getLocation().getAdjacentLocation(Location.SOUTH);
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
     * Returns the desired move Location.  This MUST be implemented in a subclass of Player
     *
     * @return the Location to move to
     */
    public abstract Location getMoveLocation();

    private final void tag() {
        tagCount++;
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.equals("ctf.Player")) {
            Location oldLoc = getLocation();
            Location nextLoc;
            do {
                nextLoc = team.adjustForSide(new Location((int) (Math.random() * getGrid().getNumRows()), 0), getGrid());
            } while (getGrid().get(nextLoc) != null);
            moveTo(nextLoc);
            tagCoolDown = 10;
            if (hasFlag) {
                team.getOpposingTeam().getFlag().putSelfInGrid(getGrid(), oldLoc);
                hasFlag = false;
            }
            setColor(Color.BLACK);

        } else {
            CTFWorld.addExtraText("Cheat");
            System.err.println(callingClass + " has cheated and tried to override the tag method");
        }
    }

    /**
     * Puts a Player into the specified Grid.
     * <p>
     * This may ONLY be called automatically by CTFWorld - it can not be called by other Player classes
     *
     * @param grid the grind into which the Player should be placed
     * @param loc  the location into which the Player should be placed
     */
    public final void putSelfInGrid(Grid<Actor> grid, Location loc) {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.equals("info.gridworld.actor.ActorWorld")) {
            if (getGrid() != null) super.removeSelfFromGrid();
            hasFlag = false;
            tagCoolDown = 0;
            setColor(team.getColor());
            super.putSelfInGrid(grid, loc);
        } else {
            CTFWorld.addExtraText("Cheat");
            System.err.println(callingClass + " has cheated and tried to add a player to the grid");
        }
    }

    /**
     * Removes a Player from its Grid
     * This may ONLY be called automatically by CTFWorld - it can not be called by other Player classes
     */
    public final void removeSelfFromGrid() {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.equals("ctf.CtfWorld")) super.removeSelfFromGrid();
        else {
            CTFWorld.addExtraText("Cheat");
            System.err.println(callingClass + " has cheated and tried to remove a player from the grid");
        }
    }

    protected final void setTeam(Team team) {
        this.team = team;
        setColor(team.getColor());
    }

    /**
     * determines whether a Player is carrying the Flag
     *
     * @return whether this Player is carrying the Flag
     */
    public final boolean hasFlag() {
        return hasFlag;
    }

    protected final Location getStartLocation() {
        return startLocation;
    }

    /**
     * Returns the Team that this Player belongs to
     *
     * @return the Team that this Player belongs to
     */
    public final Team getTeam() {
        return getMyTeam();
    }

    /**
     * Returns the Team that this Player belongs to
     *
     * @return the Team that this Player belongs to
     */
    public final Team getMyTeam() {
        return team;
    }

    /**
     * Returns the Team that this Player is playing against
     *
     * @return the Team that this Player is playing against
     */
    public final Team getOtherTeam() {
        return team.getOpposingTeam();
    }

    /**
     * Returns the Location of this Player
     *
     * @return the Location of this Player
     */
    public final Location getLocation() {
        return new Location(super.getLocation().getRow(), super.getLocation().getCol());
    }

    /**
     * Moves this Player to the specified Location.
     * This may ONLY be called indirectly by the Player superclass and not directly by any subclasses
     *
     * @param loc the Location to move to
     */
    public final void moveTo(Location loc) {
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.equals("ctf.Player")) super.moveTo(loc);
        else {
            CTFWorld.addExtraText("Cheat");
            System.err.println(callingClass + " has attempted an unauthorized moveTo");
        }
    }

    /**
     * Returns the number of times this Player has been tagged
     *
     * @return tag count
     */
    public int getTagCount() {
        return this.tagCount;
    }

    public String toString() {
        return team.getName() + ": " + this.getClass() + " at Loc: " + getLocation();
    }
}
