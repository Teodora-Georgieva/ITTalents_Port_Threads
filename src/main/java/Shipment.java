public class Shipment {
    private static int id = 1;
    private int shipmentID;
    private String name;

    public Shipment(){
        this.shipmentID = id++;
        this.name = "Shipment_" + this.shipmentID;
    }

    public String getName() {
        return name;
    }

    public int getShipmentID() {
        return shipmentID;
    }
}