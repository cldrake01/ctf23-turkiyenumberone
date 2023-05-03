package ctf2022.sampleTeam;

import ctf2022.Player;

import info.gridworld.actor.Rock;
import info.gridworld.grid.Location;

public class BeelinePlayer extends Player {

    public BeelinePlayer(Location startLocation) {
        super(startLocation);
    }

//    @Override
//    public Location getImediateObjectiveLocation(Location loc) {
//        return super.getImediateObjectiveLocation(loc);
//    }
//
//    @Override
//    public Location searchSurroundings() {
//        return super.searchSurroundings();
//    }

    public Location getMoveLocation() {
        if (this.hasFlag()) {
            return evade() != null ? evade() : this.getMyTeam().getFlag().getLocation();
        } else {
            return searchSurroundings() != null ? searchSurroundings() : this.getOtherTeam().getFlag().getLocation();
        }
    }
}
