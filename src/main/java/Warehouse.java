import java.util.LinkedList;
import java.util.Queue;

public class Warehouse {
    private Distributor distributor;
    private Queue<Shipment> shipments;
    private String name;

    public Warehouse(String name){
        this.name = name;
        this.shipments = new LinkedList<>();
    }

    public void setDistributor(Distributor distributor) {
        this.distributor = distributor;
    }

    public void receiveShipment(Shipment shipment){
        this.shipments.add(shipment);
    }

    public String getName() {
        return this.name;
    }

    /*public Queue<Shipment> getShipments() {
        return this.shipments;
    }

     */

    public Shipment exportNextShipment() {
        return this.shipments.poll();
    }

    public boolean isEmpty() {
        return this.shipments.size() == 0;
    }
}