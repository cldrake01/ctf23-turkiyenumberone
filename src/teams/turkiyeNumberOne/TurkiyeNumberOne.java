package teams.turkiyeNumberOne;

import ctf.Team;
import info.gridworld.grid.Location;
// Quentin made QPlayer with their associated methods, icons, and formatting
// Collin made CPlayer with their associated methods, strategy and structure
import java.awt.Color;

public class TurkiyeNumberOne extends Team {
  public TurkiyeNumberOne() {
    super(Color.RED);
    super.addPlayer(new CPlayer(new Location(5 + (int) (Math.random() * 3 - 1), 49)));
    super.addPlayer(new QPlayer(new Location(10 + (int) (Math.random() * 3 - 1), 30)));
    super.addPlayer(new CPlayer(new Location(15 + (int) (Math.random() * 3 - 1), 49)));
    super.addPlayer(new QPlayer(new Location(20 + (int) (Math.random() * 3 - 1), 30)));
    super.addPlayer(new CPlayer(new Location(24, 0)));
    super.addPlayer(new QPlayer(new Location(35 + (int) (Math.random() * 3 - 1), 30)));
    super.addPlayer(new CPlayer(new Location(40 + (int) (Math.random() * 3 - 1), 49)));
    super.addPlayer(new QPlayer(new Location(45 + (int) (Math.random() * 3 - 1), 30)));
  }
}
