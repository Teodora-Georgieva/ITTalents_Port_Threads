import java.util.ArrayList;
import java.util.Random;

public class Ship {
    private ArrayList<Shipment> shipments;
    private static Port port;
    private String name;
    private String dockName;
    private int dockID;
    private int totalNumberOfUnloadShipments;

    public Ship(String name){
        this.name = name;
        this.shipments = new ArrayList<>();
        Random r = new Random();
        int countOfShipments = r.nextInt(4) + 1;
        for (int i = 0; i < countOfShipments; i++) {
            this.shipments.add(new Shipment());
        }
    }

    public void queueOnDock(){
        Dock dock = port.getRandomDock();
        dock.queueAShip(this);
        this.dockName = dock.getName();
        this.dockID = dock.getDockID();
        System.out.println(this.name + " is queued on " + dock.getName());
    }

    public static void setPort(Port port) {
        Ship.port = port;
    }

    public ArrayList<Shipment> emptyShip(){
        ArrayList<Shipment> shipments = new ArrayList<>();
        shipments.addAll(this.shipments);
        this.totalNumberOfUnloadShipments = this.shipments.size();
        this.shipments = null;
        this.shipments = new ArrayList<>();
        port.addShipToList(this);
        return shipments;
    }

    public String getName() {
        return this.name;
    }

    public String getDockName() {
        return this.dockName;
    }

    public int getTotalNumberOfUnloadShipments() {
        return this.totalNumberOfUnloadShipments;
    }

    public int getDockID() {
        return dockID;
    }
}