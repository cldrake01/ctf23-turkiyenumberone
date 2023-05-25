package teams.turkiyeNumberOne;
import ctf.Player;
import info.gridworld.grid.Location;
public class CPlayer extends BasePlayer {
    public CPlayer(Location startLocation) {
        super(startLocation);
    }
    @Override
    public Location getMoveLocation() {
        Location home = new Location(getLocation().getRow(), getMyTeam().getFlag().getLocation().getCol());
        if (intruderSearch() != null)
            return intruderSearch();
        else if (getOtherTeam().getFlag().beingCarried())
            return evade() != null ? evade() : home;
        else if (searchSurroundings() != null)
            return searchSurroundings();
        else if (juke(getLocation().getAdjacentLocation(getLocation().getDirectionToward(getOtherTeam().getFlag().getLocation()))) != null)
            return juke(getLocation().getAdjacentLocation(getLocation().getDirectionToward(getOtherTeam().getFlag().getLocation())));
        else {
            return newBounce() != null ? newBounce() : getOtherTeam().getFlag().getLocation();
        }
    }
}
