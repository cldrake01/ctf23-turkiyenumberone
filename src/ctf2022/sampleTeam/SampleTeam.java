package ctf2022.sampleTeam;

import ctf2022.Team;
import info.gridworld.grid.Location;
import java.awt.*;

public class SampleTeam extends Team {

  public SampleTeam(String name, Color color) {
    super(name, color);
  }

  public void generateTeam() {
    addPlayer(new BeelinePlayer(new Location(5 + (int) (Math.random() * 3 - 1), 30)));
    addPlayer(new RandomPlayer(new Location(10 + (int) (Math.random() * 3 - 1), 30)));
    addPlayer(new RandomPlayer(new Location(15 + (int) (Math.random() * 3 - 1), 30)));
    addPlayer(new BeelinePlayer(new Location(20 + (int) (Math.random() * 3 - 1), 30)));
    addPlayer(new RandomPlayer(new Location(30 + (int) (Math.random() * 3 - 1), 30)));
    addPlayer(new RandomPlayer(new Location(35 + (int) (Math.random() * 3 - 1), 30)));
    addPlayer(new RandomPlayer(new Location(40 + (int) (Math.random() * 3 - 1), 30)));
    addPlayer(new BeelinePlayer(new Location(45 + (int) (Math.random() * 3 - 1), 30)));
  }
}
