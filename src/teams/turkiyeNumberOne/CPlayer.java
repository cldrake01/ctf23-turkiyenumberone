package teams.turkiyeNumberOne;

import ctf.Flag;
import info.gridworld.grid.Location;
import java.util.Optional;

public class CPlayer extends BasePlayer {
  public CPlayer(Location startLocation) {
    super(startLocation);
  }

  @Override
  public Location getMoveLocation() {
    return priority();
  }

  Location priority() {
    Location location = getLocation();
    Location home = new Location(location.getRow(), getMyTeam().getFlag().getLocation().getCol());
    Optional<Location> intruders = intruderSearch();
    Optional<Location> evade = evade();
    Optional<Location> surroundings = searchSurroundings();
    Optional<Location> juked =
        juke(
            location.getAdjacentLocation(
                location.getDirectionToward(getOtherTeam().getFlag().getLocation())));
    Optional<Location> newBounce = newBounce();
    Flag otherFlag = getOtherTeam().getFlag();

    if (newBounce.isPresent()) return newBounce.get();
    else if (intruders.isPresent()) return intruders.get();
    else if (otherFlag.beingCarried()) return evade.orElse(home);
    else return surroundings.orElse(juked.orElse(otherFlag.getLocation()));
  }
}
