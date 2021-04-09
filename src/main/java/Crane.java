public class Crane extends Thread{
    private int totalNumberOfShipments;
    private Port port;
    //private static int id = 1;
    //private int craneID;

    public Crane(String name, Port port){
        super(name);
        this.port = port;
        //this.craneID = id++;
    }

    @Override
    public void run() {
        while (true){
            int shipments = this.port.serveDocks();
            this.totalNumberOfShipments+=shipments;
        }
    }

    public int getTotalNumberOfShipments() {
        return this.totalNumberOfShipments;
    }

    /*
    public int getCraneID() {
        return craneID;
    }

     */
}