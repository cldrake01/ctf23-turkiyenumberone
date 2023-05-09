package teams.turkiyeNumberOne;

import java.awt.Color;

import ctf.Team;

import info.gridworld.grid.Location;
import teams.turkiyeNumberOne.BeelinePlayer;
import teams.turkiyeNumberOne.RandomPlayer;

public class TurkiyeNumberOne extends Team {

    public TurkiyeNumberOne(Color color) {
        super(color);

        super.addPlayer(new teams.turkiyeNumberOne.BeelinePlayer(new Location(5 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new teams.turkiyeNumberOne.RandomPlayer(new Location(10 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new teams.turkiyeNumberOne.BeelinePlayer(new Location(15 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new teams.turkiyeNumberOne.RandomPlayer(new Location(20 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new teams.turkiyeNumberOne.BeelinePlayer(new Location(30 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new teams.turkiyeNumberOne.RandomPlayer(new Location(35 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new teams.turkiyeNumberOne.BeelinePlayer(new Location(40 + (int)(Math.random()*3 - 1), 30)));
        super.addPlayer(new teams.turkiyeNumberOne.RandomPlayer(new Location(45 + (int)(Math.random()*3 - 1), 30)));
    }
}