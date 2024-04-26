package teams.blueSampleTeam;

import ctf.Player;
import info.gridworld.grid.Location;

public class BeelinePlayer extends Player {

  public BeelinePlayer(Location startLocation) {
    super(startLocation);
  }

  public Location getMoveLocation() {
    // if I have the flag, return to my side by going towards MY flag
    if (hasFlag()) {
      return this.getMyTeam().getFlag().getLocation();
    }
    // if I don't have the flag, move towards THEIR flag
    else {
      return this.getOtherTeam().getFlag().getLocation();
    }
  }
}
