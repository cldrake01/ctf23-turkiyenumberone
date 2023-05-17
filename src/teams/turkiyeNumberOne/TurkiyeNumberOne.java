package teams.turkiyeNumberOne;

import java.awt.Color;

import ctf.Team;

import info.gridworld.grid.Location;

public class TurkiyeNumberOne extends Team {

    public TurkiyeNumberOne(Color color) {
        super(color);

        super.addPlayer(new CPlayer(new Location(5 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new QPlayer(new Location(10 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new CPlayer(new Location(15 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new QPlayer(new Location(20 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new CPlayer(new Location(30 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new QPlayer(new Location(35 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new CPlayer(new Location(40 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new QPlayer(new Location(45 + (int)(Math.random()*3 - 1), 30)));
    }
}