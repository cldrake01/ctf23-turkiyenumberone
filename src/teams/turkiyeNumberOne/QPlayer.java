package teams.turkiyeNumberOne;

import ctf.Flag;
import info.gridworld.actor.Actor;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import java.util.ArrayList;
import java.util.Optional;

public class QPlayer extends BasePlayer {
  /**
   * Constructs a new QPlayer with the specified starting location.
   *
   * @param startLocation the starting location of the QPlayer
   */
  public QPlayer(Location startLocation) {
    super(startLocation);
  }

  /**
   * Determines the next move location for the QPlayer.
   *
   * @return the next move location
   */
  @Override
  public Location getMoveLocation() {
    return priority();
  }

  Location priority() {
    Grid<Actor> grid = getGrid();
    Location location = getLocation();
    Location flagLocation = getMyTeam().getFlag().getLocation();
    Location towardsFlag = getAdjacentLocation(flagLocation);
    ArrayList<Location> emptyAdjacentLocations = grid.getEmptyAdjacentLocations(towardsFlag);
    Optional<Location> intruder = intruderSearch();
    Flag myFlag = getMyTeam().getFlag();

    if (myFlag.beingCarried()) {
      if (isRock(towardsFlag) && !emptyAdjacentLocations.isEmpty())
        getRandomEmptyAdjacentLocation(towardsFlag);
      else return myFlag.getLocation();
    }

    return intruder.orElse(getRandomEmptyAdjacentLocation(location).orElse(null));
  }
}
