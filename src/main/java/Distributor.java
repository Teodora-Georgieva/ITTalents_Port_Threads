public class Distributor extends Thread{
    private Warehouse warehouse;
    private Port port;

    public Distributor(String name, Warehouse warehouse, Port port){
        super(name);
        this.warehouse = warehouse;
        this.port = port;
    }

    @Override
    public void run() {
        while (true){
            this.port.exportShipment(this);
        }
    }

    public Warehouse getWarehouse() {
        return this.warehouse;
    }
}