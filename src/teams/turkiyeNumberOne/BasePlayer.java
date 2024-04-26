package teams.turkiyeNumberOne;

import ctf.Flag;
import ctf.Player;
import ctf.Team;
import info.gridworld.actor.Actor;
import info.gridworld.actor.Rock;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class BasePlayer extends ctf.Player {

  /**
   * Constructs a new Player with its desired starting Location
   *
   * @param startLocation the desired starting Location
   */
  public BasePlayer(Location startLocation) {
    super(startLocation);
  }

  abstract Location priority();

  /**
   * Attempts to evade other players by moving in the opposite direction of the enemy flag.
   *
   * @return The location of the player after the evasion, or null if there are no adjacent enemy
   *     players or if the player does not have the flag.
   */
  Optional<Location> evade() {
    Grid<Actor> grid = getGrid();
    Location location = getLocation();
    Location adjacent = getAdjacentLocation(myFlag());
    List<Location> empty = grid.getEmptyAdjacentLocations(adjacent);

    if (isRock(adjacent) && !empty.isEmpty()) {
      Optional<Location> emptyLocation = getRandomEmptyAdjacentLocation(adjacent);
      if (empty.contains(emptyLocation.orElse(null))) return emptyLocation;
      return getRandomEmptyAdjacentLocation(location);
    }
    List<Location> enemies =
        grid.getOccupiedAdjacentLocations(adjacent).stream().filter(this::isEnemy).toList();
    for (Location enemy : enemies) juke(enemy);
    return Optional.empty();
  }

  Location myFlag() {
    return getMyTeam().getFlag().getLocation();
  }

  Location otherFlag() {
    return getOtherTeam().getFlag().getLocation();
  }

  boolean isPlayer(Location loc) {
    return getGrid().get(loc) instanceof Player;
  }

  boolean isEnemy(Location loc) {
    Grid<Actor> grid = getGrid();
    return (isPlayer(loc) && (((Player) grid.get(loc)).getTeam().equals(getOtherTeam())));
  }

  boolean isRock(Location loc) {
    return getGrid().get(loc) instanceof Rock;
  }

  boolean isFlag(Location loc) {
    return getGrid().get(loc) instanceof Flag;
  }

  boolean isOffsides(Location loc) {
    return !isEnemy(loc) && !getMyTeam().onSide(loc);
  }

  boolean enemyIsOffsides(Location loc) {
    return isEnemy(loc) && !getOtherTeam().onSide(loc);
  }

  Optional<Location> juke(Location loc) {
    Location location = getLocation();
    Location north = location.getAdjacentLocation(Location.NORTH);
    Location south = location.getAdjacentLocation(Location.SOUTH);

    if (isPlayer(loc))
      return loc.getRow() >= location.getRow() ? Optional.of(north) : Optional.of(south);
    else return Optional.empty();
  }

  /**
   * Searches for enemy players on the grid and returns the location of the first enemy player
   * found.
   *
   * @return The location of the first enemy player found, or null if there are no enemy players on
   *     the grid.
   */
  Optional<Location> intruderSearch() {
    List<Location> occupiedLocations =
        getGrid().getOccupiedLocations().stream()
            .filter(this::isEnemy)
            .filter(this::enemyIsOffsides)
            .toList();
    if (occupiedLocations.isEmpty()) return Optional.empty();
    Location closestEnemy = minDistance(getLocation(), occupiedLocations);

    if (!isOffsides(getLocation())) return aroundRock(closestEnemy);
    return Optional.empty();
  }

  //  /**
  //   * Determines the objective location based on the enemy player's location.
  //   *
  //   * @param loc The location of the enemy player
  //   * @return The objective location, which could be an empty adjacent location or the enemy
  // player's
  //   *     location itself.
  //   */
  //  private Optional<Location> getNearestEnemy(Location loc) {
  //    Location location = getLocation();
  //    Location adjacentLocation = getAdjacentLocation(loc);
  //    ArrayList<Location> emptyAdjacentLocations = getGrid().getEmptyAdjacentLocations(location);
  //
  //    if (isRock(adjacentLocation) && !emptyAdjacentLocations.isEmpty())
  //      return getRandomEmptyAdjacentLocation(location);
  //    return Optional.of(minDistance(loc));
  //  }

  Location minDistance(Location target, List<Location> locations) {
    double minDistance = Double.MAX_VALUE;
    Location minLocation = null;

    for (Location loc : locations) {
      double distance =
          Math.sqrt(
              Math.pow(loc.getRow() - target.getRow(), 2)
                  + Math.pow(loc.getCol() - target.getCol(), 2));
      if (distance < minDistance) {
        minDistance = distance;
        minLocation = loc;
      }
    }
    return minLocation;
  }

  /**
   * Returns the objective location of the player based on the given location.
   *
   * @param loc The location of the player.
   * @return The objective location of the player, or null if the location is not a valid objective
   *     location.
   */
  Optional<Location> getImmediateObjectiveLocation(Location loc) {
    Grid<Actor> grid = getGrid();
    Location location = getLocation();

    if (isEnemy(loc) && enemyIsOffsides(loc)) return Optional.of(loc);
    else if (isRock(getAdjacentLocation(otherFlag()))
        && !grid.getEmptyAdjacentLocations(location).isEmpty())
      return getRandomEmptyAdjacentLocation(location);
    else if (isFlag(loc) && ((Flag) grid.get(loc)).getTeam() != this.getTeam())
      return Optional.of(loc);
    else return Optional.empty();
  }

  /**
   * Searches for the immediate objective location in the surrounding locations of the current
   * location.
   *
   * @return the immediate objective location if found, otherwise null. The reason for returning
   *     null is to allow for class-specific behavior, which may follow a call to this method.
   */
  Optional<Location> searchSurroundings() {
    return getGrid().getOccupiedAdjacentLocations(getLocation()).stream()
        .map(this::getImmediateObjectiveLocation)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  Optional<Location> aroundRock(Location target) {
    Location adjacentLocation = getAdjacentLocation(target);

    return !isRock(adjacentLocation)
        ? Optional.of(adjacentLocation)
        : getRandomEmptyAdjacentLocation(getLocation());
  }

  /**
   * Bounces the player in the opposite direction of the flag if the player is within a certain
   * distance of the flag.
   *
   * @return a location in the opposite direction of the flag.
   */
  Optional<Location> newBounce() {
    Location location = getLocation();
    Location northAdjacentLocation = location.getAdjacentLocation(Location.NORTH);
    Location southAdjacentLocation = location.getAdjacentLocation(Location.SOUTH);
    Team myTeam = getMyTeam();

    if (!myTeam.nearFlag(getAdjacentLocation(myTeam.getFlag().getLocation())))
      return Optional.empty();

    return switch (myTeam.getFlag().getLocation().getRow() >= location.getRow() ? 1 : 0) {
      case 1 ->
          (isRock(northAdjacentLocation))
              ? getRandomEmptyAdjacentLocation(northAdjacentLocation)
              : Optional.of(northAdjacentLocation);
      case 0 ->
          (isRock(southAdjacentLocation))
              ? getRandomEmptyAdjacentLocation(southAdjacentLocation)
              : Optional.of(southAdjacentLocation);
      default -> Optional.empty();
    };
  }

  Location getAdjacentLocation(Location location) {
    return getLocation().getAdjacentLocation(getLocation().getDirectionToward(location));
  }

  Optional<Location> getRandomEmptyAdjacentLocation(Location location) {
    ArrayList<Location> emptyAdjacentLocations = getGrid().getEmptyAdjacentLocations(location);
    int numEmptyLocations = emptyAdjacentLocations.size();
    Random random = new Random();
    int randomIndex = random.nextInt(numEmptyLocations);
    return Optional.of(emptyAdjacentLocations.get(randomIndex));
  }

  /**
   * This method is never called.
   *
   * @return null
   */
  @Override
  public Location getMoveLocation() {
    System.err.println("This method should never be called.");
    return null;
  }
}
