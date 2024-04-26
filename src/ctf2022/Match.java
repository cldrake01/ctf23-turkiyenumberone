package ctf2022;

import info.gridworld.actor.Actor;
import info.gridworld.actor.Rock;
import info.gridworld.grid.BoundedGrid;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import javax.swing.*;

public class Match {
  private static int numRocks;

  private final Team teamA;
  private final Team teamB;
  private final CtfWorld world;
  private Team winner;

  /**
   * Creates a new match with two teams and default number of rocks.
   *
   * @param a the first team
   * @param b the second team
   */
  public Match(Team a, Team b) {
    teamA = a;
    teamB = b;
    world = new CtfWorld(a, b);
    numRocks = 75;
  }

  /**
   * Creates a new match with two teams and a specified number of rocks.
   *
   * @param a the first team
   * @param b the second team
   * @param rocks the number of rocks to place in the grid
   */
  public Match(Team a, Team b, int rocks) {
    teamA = a;
    teamB = b;
    world = new CtfWorld(a, b);
    numRocks = rocks;
  }

  /**
   * Determines if a flag is near the given location in the grid.
   *
   * @param grid the grid to check
   * @param loc the location to check around
   * @return true if a flag is within 5 units of the location, false otherwise
   */
  private static boolean nearFlag(Grid<Actor> grid, Location loc) {
    for (int i = loc.getCol() - 5; i <= loc.getCol() + 5; i++) {
      for (int j = loc.getRow() - 5; j <= loc.getRow() + 5; j++) {
        Location newloc = new Location(j, i);
        if (grid.isValid(newloc) && (grid.get(newloc) instanceof Flag)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Starts the match. Generates the grid and places teams and rocks in it. Shows the grid and keeps
   * the game running until a team wins. Displays the winner at the end.
   */
  public void start() {
    // create a new grid
    BoundedGrid<Actor> grid = new BoundedGrid<>(50, 100);

    // add teams to the grid randomly
    double randomNumber = Math.random();
    teamA.addTeamToGrid(grid, randomNumber < .5 ? 0 : 1);
    teamB.addTeamToGrid(grid, randomNumber < .5 ? 1 : 0);

    // set opposing teams for each team
    teamA.setOpposingTeam(teamB);
    teamB.setOpposingTeam(teamA);

    // place rocks in the grid randomly while ensuring they are not close to flags or players
    for (int i = 0; i < numRocks; i++) {
      Location rockClumpLocation;
      do {
        rockClumpLocation =
            new Location(
                (int) (Math.random() * (grid.getNumRows())),
                (int) (Math.random() * ((grid.getNumCols()) - 6)) + 3);
      } while (nearFlag(grid, rockClumpLocation) || grid.get(rockClumpLocation) != null);

      if (!nearPlayer(teamA, rockClumpLocation) && !nearPlayer(teamB, rockClumpLocation))
        new Rock().putSelfInGrid(grid, rockClumpLocation);

      for (int j = 0; j < 8; j++) {
        int randomDirection = (int) (Math.random() * 8) * Location.HALF_RIGHT;
        Location possibleRockLocation = rockClumpLocation.getAdjacentLocation(randomDirection);
        if (grid.isValid(possibleRockLocation)
            && !nearFlag(grid, possibleRockLocation)
            && grid.get(possibleRockLocation) == null)
          if (!nearPlayer(teamA, possibleRockLocation) && !nearPlayer(teamB, possibleRockLocation))
            new Rock().putSelfInGrid(grid, possibleRockLocation);
      }
    }

    // set the grid for the world
    world.setGrid(grid);

    // display a dialog box before the game starts
    String msg = "";
    if (teamA.getSide() == 0) {
      msg = teamA.getName() + " vs. " + teamB.getName();
    } else {
      msg = teamB.getName() + " vs. " + teamA.getName();
    }
    final JOptionPane pane = new JOptionPane(msg);
    final JDialog d = pane.createDialog(null, "Next Match");
    System.out.println();
    d.setLocation((int) d.getLocation().getX(), Math.max(0, (int) d.getLocation().getY() - 300));
    d.setVisible(true);
    System.out.println("Starting Match: " + teamA.getName() + " vs. " + teamB.getName());

    // display the grid
    world.show();

    // keep the game running until a team wins
    while (!teamA.hasWon() && !teamB.hasWon())
      ;

    // determine the winner and display a message
    winner = teamA.hasWon() ? teamA : teamB;
    System.out.println(
        teamA.getName()
            + ": "
            + teamA.getScore()
            + " "
            + teamB.getName()
            + ": "
            + teamB.getScore()
            + " Winner: "
            + winner.getName()
            + "\n");
    JOptionPane.showMessageDialog(null, winner.getName() + " has won!");
  }

  /**
   * Returns the winning team.
   *
   * @return the winning team
   */
  public Team getWinner() {
    return winner;
  }

  /**
   * Determines if a player from the given team is near the given location.
   *
   * @param t the team to check
   * @param loc the location to check around
   * @return true if a player from the team is within 3 units of the location, false otherwise
   */
  private boolean nearPlayer(Team t, Location loc) {
    for (Player p : t.getPlayers()) {
      if (distance(p.getLocation(), loc) < 3) return true;
    }
    return false;
  }

  /**
   * Calculates the distance between two locations.
   *
   * @param loc1 the first location
   * @param loc2 the second location
   * @return the distance between the two locations
   */
  private double distance(Location loc1, Location loc2) {
    return Math.sqrt(
        Math.pow(loc1.getRow() - loc2.getRow(), 2) + Math.pow(loc1.getCol() - loc2.getCol(), 2));
  }
}
