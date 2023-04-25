package ctf2022;

import info.gridworld.actor.Actor;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;

import java.awt.*;
import java.util.ArrayList;

public abstract class Team {
    public static final int MAX_SIZE = 8;
    public static final Location DEFAULT_FLAG_LOCATION = new Location(24, 10);

    private ArrayList<Player> players;
    private Grid<Actor> grid;
    private Flag flag;
    private Team opposingTeam;
    private Color color;
    private String name;
    private volatile boolean hasWon;
    private int score;
    private int pickUps;
    private int tags;
    private int offensiveMoves;

    private int side;

    public Team(String name, Color color) {
        players = new ArrayList<>();
        this.name = name;
        this.color = color;
    }

    public abstract void generateTeam();

    private final void resetTeam() {
        players.clear();
        generateTeam();
    }

    public final void addPlayer(Player player) {
        if (players.size() < MAX_SIZE) {
            players.add(player);
            player.setTeam(this);
        } else {
            throw new RuntimeException("Team is full - MAX_SIZE = " + MAX_SIZE);
        }
    }

    protected final void addTeamToGrid(Grid<Actor> grid, int side) {
        this.grid = grid;
        this.side = side;
        score = 0;
        pickUps = 0;
        tags = 0;
        offensiveMoves = 0;
        hasWon = false;
        flag = new Flag(this);
        flag.putSelfInGrid(grid, adjustForSide(DEFAULT_FLAG_LOCATION, grid));
        resetTeam();
        for (Player player : players) {
            double dist = Math.sqrt(Math.pow(player.getStartLocation().getRow() - DEFAULT_FLAG_LOCATION.getRow(), 2)
                    + Math.pow(player.getStartLocation().getCol() - DEFAULT_FLAG_LOCATION.getCol(), 2));
            if (player.getStartLocation().getCol() >= grid.getNumCols() / 2 || player.getStartLocation().getCol() < 0 || dist < 10.0) {
                System.err.println("Someone has cheated and given their players an invalid start location");
                Location nextLoc;
                do {
                    nextLoc = new Location((int) (Math.random() * grid.getNumRows() - 1), 0);
                } while (grid.get(nextLoc) != null);
                player.setStartLocation(nextLoc);
            }
            player.putSelfInGridProtected(grid, adjustForSide(player.getStartLocation(), grid));
        }
    }

    protected final void addPickUp() {
        pickUps++;
    }

    protected final void addTag() {
        tags++;
    }

    protected final void addOffensiveMove() {
        offensiveMoves++;
    }

    public void displayStats(int steps) {
        System.out.println(name + " got " + pickUps + " pick ups and " + tags + " tags. They made " + offensiveMoves
                + " steps on the offencive side, and were on the other side " + offensiveMoves / (steps + players.size()) + "% of the game");
    }

    protected final void addScore(int s) {
        score += s;
    }

    public final Location adjustForSide(Location loc, Grid<Actor> grid) {
        return new Location(loc.getRow(), (side == 0 ? loc.getCol() : grid.getNumCols() - 1 - loc.getCol()));
    }

    protected final void setOpposingTeam(Team opposingTeam) {
        this.opposingTeam = opposingTeam;
    }

    protected final void setHasWon() {
        hasWon = true;
        // if (hasWon)
        // System.err.println("Someone has cheated and pretended to win without actually winning");
        // I had to remove the team checking if it actually won
    }

    public final boolean onSide(Location loc) {
        return side == 0 && loc.getCol() < grid.getNumCols() / 2 || side == 1 && loc.getCol() >= grid.getNumCols() / 2;
    }

    final static int RANGE = 4;
    public final boolean nearFlag(Location loc) {
        if (flag == null || flag.getLocation() == null) return false;
        Location fLoc = flag.getLocation();

        return Math.abs(loc.getRow()-fLoc.getRow()) <= RANGE && Math.abs(loc.getCol()-fLoc.getCol()) <= RANGE;
        //return Math.sqrt(Math.pow(loc.getRow() - flag.getLocation().getRow(), 2) + Math.pow(loc.getCol() - flag.getLocation().getCol(), 2)) <= 4;
    }

    public final ArrayList<Player> getPlayers() {
        return (ArrayList<Player>) players.clone();
    }

    public final Flag getFlag() {
        return flag;
    }

    public final Team getOpposingTeam() {
        return opposingTeam;
    }

    public final Color getColor() {
        return color;
    }

    public final String getName() {
        return name;
    }

    public final int getScore() {
        return score;
    }

    public final int getSide() {
        return side;
    }

    public final boolean hasWon() {
        return hasWon;
    }

    public final boolean equals(Team team) {
        return team.getSide() == side && team.getColor().equals(color) && team.getClass() == getClass();
    }
}