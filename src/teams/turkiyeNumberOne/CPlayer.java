package teams.turkiyeNumberOne;

import ctf.Player;
import info.gridworld.actor.Rock;
import info.gridworld.grid.Location;
import java.awt.Transparency;
public class CPlayer extends BasePlayer {

    public CPlayer(Location startLocation) {
        super(startLocation);
    }

    @Override
    public Location getMoveLocation() {

        Location home = new Location(getLocation().getRow(), getMyTeam().getFlag().getLocation().getCol());

        if (this.hasFlag())
            return evade() != null ? evade() : (getGrid().get(getLocation().getAdjacentLocation(getLocation().getDirectionToward(home))) instanceof Rock
                    ? getGrid().getEmptyAdjacentLocations(getLocation().getAdjacentLocation(getLocation().getDirectionToward(home))).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation().getAdjacentLocation(getLocation().getDirectionToward(home))).size()))
                    : home
            );
        else if (searchSurroundings() != null)
            return locBounce(searchSurroundings());
        else if (getGrid().get(getLocation().getAdjacentLocation(getLocation().getDirectionToward(getOtherTeam().getFlag().getLocation()))) instanceof Player)
            return locBounce(getGrid().getEmptyAdjacentLocations(getLocation()).get((int) (Math.random() * getGrid().getEmptyAdjacentLocations(getLocation()).size())));
        else
            return locBounce(getOtherTeam().getFlag().getLocation());
    }
}
