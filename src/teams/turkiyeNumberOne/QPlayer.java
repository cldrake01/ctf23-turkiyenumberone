package teams.turkiyeNumberOne;
import info.gridworld.actor.Rock;
import info.gridworld.grid.Location;
import java.util.ArrayList;
import java.util.Random;
public class QPlayer extends BasePlayer {
    public QPlayer(Location startLocation) {
        super(startLocation);
    }
    @Override
    public Location getMoveLocation() {
        if (getMyTeam().getFlag().beingCarried()) {
            Location flagLocation = getMyTeam().getFlag().getLocation();
            Location towardsFlag = getLocation().getAdjacentLocation(getLocation().getDirectionToward(flagLocation));

            ArrayList<Location> emptyAdjacentLocations = getGrid().getEmptyAdjacentLocations(towardsFlag);
            int numEmptyLocations = emptyAdjacentLocations.size();

            if (getGrid().get(towardsFlag) instanceof Rock && numEmptyLocations > 0) {
                Random random = new Random();
                int randomIndex = random.nextInt(numEmptyLocations);
                return emptyAdjacentLocations.get(randomIndex);
            }
        }
        Location intruderLocation = intruderSearch();
        if (intruderLocation != null) {
            return intruderLocation;
        } else {
            ArrayList<Location> emptyAdjacentLocations = getGrid().getEmptyAdjacentLocations(getLocation());
            int numEmptyLocations = emptyAdjacentLocations.size();
            Random random = new Random();
            int randomIndex = random.nextInt(numEmptyLocations);
            return emptyAdjacentLocations.get(randomIndex);
        }
    }
}
