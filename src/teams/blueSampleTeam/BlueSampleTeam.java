package teams.blueSampleTeam;

import ctf.Team;
import info.gridworld.grid.Location;
import java.awt.Color;

public class BlueSampleTeam extends Team {

  public BlueSampleTeam() {
    // set up Team through superconstructor
    super(Color.BLUE);

    // add your Players: add to left side - can not be too near the Flag
    super.addPlayer(new RandomPlayer(new Location(5 + (int) (Math.random() * 3 - 1), 30)));
    super.addPlayer(new BeelinePlayer(new Location(10 + (int) (Math.random() * 3 - 1), 30)));
    super.addPlayer(new RandomPlayer(new Location(15 + (int) (Math.random() * 3 - 1), 30)));
    super.addPlayer(new BeelinePlayer(new Location(20 + (int) (Math.random() * 3 - 1), 30)));
    super.addPlayer(new RandomPlayer(new Location(30 + (int) (Math.random() * 3 - 1), 30)));
    super.addPlayer(new BeelinePlayer(new Location(35 + (int) (Math.random() * 3 - 1), 30)));
    super.addPlayer(new RandomPlayer(new Location(40 + (int) (Math.random() * 3 - 1), 30)));
    super.addPlayer(new BeelinePlayer(new Location(45 + (int) (Math.random() * 3 - 1), 30)));
  }
}
