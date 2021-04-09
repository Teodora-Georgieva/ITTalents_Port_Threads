import java.util.LinkedList;
import java.util.Queue;

public class Dock {
    private static int id = 1;
    private int dockID;
    private String name;
    private Queue<Ship> queueWithShips;
    private int totalNumberOfShips;

    public Dock(String name){
        this.dockID = id++;
        this.name = name;
        this.queueWithShips = new LinkedList<>();
        this.totalNumberOfShips = 0;
    }

    public void queueAShip(Ship ship){
        this.queueWithShips.offer(ship);
    }

    public int getNumberOfShipsOnQueue(){
        return this.queueWithShips.size();
    }

    public Ship getNextShip(){
        Ship ship = this.queueWithShips.poll();
        this.totalNumberOfShips++; //incrementing here, because it counts the number of ships that have been unloaded
                                  //when I get the ship here, the next step is the ship to be unloaded
        return ship;
    }

    public String getName() {
        return name;
    }

    public int getTotalNumberOfShips() {
        return this.totalNumberOfShips;
    }

    public int getDockID() {
        return dockID;
    }
}