package ctf;

import info.gridworld.actor.Actor;
import info.gridworld.actor.ActorWorld;
import info.gridworld.actor.Rock;
import info.gridworld.grid.BoundedGrid;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import java.awt.Transparency;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;

/**
 * CTFWorld is a custom ActorWorld that consists of two Teams that play Capture the Flag until either
 * 1000 steps have been made or one team captures the opposing Team's Flag and returns it to their
 * own side.
 */
public final class CTFWorld extends ActorWorld {
    public static final int MAX_GAME_LENGTH = 1000;     // max steps before game ends
    public static final Location DEFAULT_FLAG_LOCATION = new Location(24, 10);
    public static final int LEFT_SIDE = 0;
    public static final int RIGHT_SIDE = 1;
    private static String extra = "";                   // extra text in message area
    private ArrayList<Player> players;
    // list of Players to receive act()
    private Team leftTeam, rightTeam;                   // the two teams playing
    private int steps;                                  // the number of steps so far

    public static void addExtraText(String text) {
        CTFWorld.extra += " " + text;
    }

    /**
     * Constructs a new Capture the Flag World with the two given Teams and 75 Rocks.
     *
     * @param leftTeam  the Team that will appear on the left side of the field
     * @param rightTeam the Team that will appear on the right side of the field (it will automatically be mirrored from the left side to the right side
     */
    public CTFWorld(Team leftTeam, Team rightTeam) {
        this(leftTeam, rightTeam, 75, 9);
    }

    /**
     * Constructs a new Capture the Flag World with the two given Teams and the specified number of Rocks.
     *
     * @param leftTeam
     * @param rightTeam
     * @param numClumps
     */
    public CTFWorld(Team leftTeam, Team rightTeam, int numClumps) {
        this(leftTeam, rightTeam, numClumps, 9);
    }

    public CTFWorld(Team leftTeam, Team rightTeam, int numClumps, int clumpsize) {
        // create World with 50x100 grid
        super(new BoundedGrid<>(50, 100));
        Grid grid = super.getGrid();
        this.setMessage("Welcome to Capture the Flag!");

        this.leftTeam = leftTeam;
        this.rightTeam = rightTeam;
        this.displayScores();

        // add players to primary list

        this.players = new ArrayList<Player>();

        // set up teams
        leftTeam.setSide(CTFWorld.LEFT_SIDE);
        leftTeam.setOpposingTeam(rightTeam);
        rightTeam.setSide(CTFWorld.RIGHT_SIDE);
        rightTeam.setOpposingTeam(leftTeam);

        // add flags and players to grid
        Flag leftFlag = leftTeam.getFlag();
        this.add(CTFWorld.DEFAULT_FLAG_LOCATION, leftTeam.getFlag());
        for (Player player : leftTeam.getPlayers()) {
            this.players.add(player);
            // add check that this is a valid location
            this.add(player.getStartLocation(), player);
        }
        Flag rightFlag = rightTeam.getFlag();
        this.add(mirror(CTFWorld.DEFAULT_FLAG_LOCATION, grid), rightTeam.getFlag());
        for (Player player : rightTeam.getPlayers()) {
            this.players.add(player);
            // add check that this is a valid location
            this.add(mirror(player.getStartLocation(), grid), player);
        }

        // add rocks
        for (int i = 0; i < numClumps; i++) {
            // add initial Rock of clump
            Location rockClumpLocation;
            do {
                rockClumpLocation = new Location((int) (Math.random() * (grid.getNumRows())), (int) (Math.random() * ((grid.getNumCols()) - 6)) + 3);
            } while (nearFlag(grid, rockClumpLocation) || nearPlayer(leftTeam, rockClumpLocation) || nearPlayer(rightTeam, rockClumpLocation) || grid.get(rockClumpLocation) != null);

            new Rock().putSelfInGrid(grid, rockClumpLocation);

            // add remaining Rocks of clump
            for (int j = 0; j < clumpsize - 1; j++) {
                int randomDirection = (int) (Math.random() * 8) * Location.HALF_RIGHT;
                Location possibleRockLocation = rockClumpLocation.getAdjacentLocation(randomDirection);
                if (grid.isValid(possibleRockLocation) && !nearFlag(grid, possibleRockLocation) && grid.get(possibleRockLocation) == null)
                    if (!nearPlayer(leftTeam, possibleRockLocation) && !nearPlayer(rightTeam, possibleRockLocation))
                        new Rock().putSelfInGrid(grid, possibleRockLocation);
            }
        }
    }

    public int getSteps() {
        return this.steps;
    }

    private Location mirror(Location loc, Grid<Actor> grid) {
        return new Location(loc.getRow(), (grid.getNumCols() - 1 - loc.getCol()));
    }

    private static boolean nearFlag(Grid<Actor> grid, Location loc) {
        for (int i = loc.getCol() - 5; i <= loc.getCol() + Team.RANGE + 1; i++) {
            for (int j = loc.getRow() - 5; j <= loc.getRow() + Team.RANGE + 1; j++) {
                Location newloc = new Location(j, i);
                if (grid.isValid(newloc) && (grid.get(newloc) instanceof Flag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean nearPlayer(Team t, Location loc) {
        for (Player p : t.getPlayers()) {
            if (distance(p.getLocation(), loc) < 3)
                return true;
        }
        return false;
    }

    private double distance(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow(loc1.getRow() - loc2.getRow(), 2) + Math.pow(loc1.getCol() - loc2.getCol(), 2));
    }

    /**
     * makes one step on the CTFWorld.  All active Players are sent an act() message in random order.
     */
    public void step() {
        // make sure this is only called from GUI
        String callingClass = Thread.currentThread().getStackTrace()[2].getClassName();
        if (callingClass.equals("info.gridworld.gui.GUIController")) {
            // game over due to number of steps
            if (this.steps >= MAX_GAME_LENGTH) {
                if (this.leftTeam.getScore() > this.rightTeam.getScore()) {
                    this.leftTeam.setHasWon();
                } else if (this.rightTeam.getScore() > this.leftTeam.getScore()) {
                    this.rightTeam.setHasWon();
                } else { // tie!
                    boolean coin = (Math.random() < 0.5);
                    if (coin) {
                        this.leftTeam.setHasWon();
                    } else {
                        this.rightTeam.setHasWon();
                    }
                }
                return;
            }
            // game active - make all Players act
            this.steps++;
            Collections.shuffle(this.players);
            System.gc();
            for (Player p : this.players) {
                p.act();
                if (p.hasFlag()) {
                    // check for win (has flag on own side)
                    if (p.getTeam().onSide(p.getLocation())) {
                        p.getTeam().setHasWon();
                        return;
                    }
                }
            }
        }

        // update displayed scores
        this.displayScores();

    }

    private final void displayScores() {
        String scoreText = "step: " + steps + "   \t";
        if (this.leftTeam.getSide() == 0) {
            scoreText += this.leftTeam.getName() + ": " + this.leftTeam.getScore();
            scoreText += "   \t" + this.rightTeam.getName() + ": " + this.rightTeam.getScore();
        } else {
            scoreText += this.rightTeam.getName() + ": " + this.rightTeam.getScore();
            scoreText += "   \t" + this.leftTeam.getName() + ": " + this.leftTeam.getScore();
        }
        scoreText += CTFWorld.extra;
        this.setMessage(scoreText);
        CTFWorld.extra = "";
    }

    public boolean locationClicked(Location loc) {
        return true;
    }

    /**
     * closes the GUI windows of a CTFWorld
     */
    public void close() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        System.gc();
    }
}