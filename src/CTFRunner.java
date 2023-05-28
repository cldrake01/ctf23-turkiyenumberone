
import ctf.CTFWorld;
import ctf.Team;
import teams.blueSampleTeam.BlueSampleTeam;
import teams.turkiyeNumberOne.TurkiyeNumberOne;
import javax.swing.*;
public class CTFRunner {
    public static void main(String[] args) throws InterruptedException {
//        Uncomment this to prevent any output from being printed to the console (speeds up performance)
//        System.setOut(new PrintStream(new OutputStream() {
//            public void write(int b) {
//                //DO NOTHING
//            }
//        }));
        // create two teams
        Team a = new TurkiyeNumberOne();
        Team b = new TurkiyeNumberOne();
        // build and display a CTFWorld
        // use numClumps = 10 for testing
        // competition will set num clumps to 75
        CTFWorld world = new CTFWorld(a, b, 75, 9);  // for development and testing
//        CTFWorld world = new CTFWorld(a, b, 75, 9);
        world.show();
        // wait for a winner
        while (!a.hasWon() && !b.hasWon()) {
            Thread.sleep(100);
        }
        // display popup alert with resuults
        String msg = "Game over! ";
        if (a.hasWon()) msg += a.getName() + " wins!\n\n";
        if (b.hasWon()) msg += b.getName() + " wins!\n\n";
        JOptionPane.showMessageDialog(null, msg + a.getStats() + "\n" + b.getStats());
    }
}