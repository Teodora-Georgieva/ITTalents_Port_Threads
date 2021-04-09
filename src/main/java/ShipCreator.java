public class ShipCreator extends Thread{
    private int crrShipNum = 1;
    private Port port;

    public ShipCreator(Port port){
        this.port = port;
    }

    @Override
    public void run() {
        while (true){
            //this.port.createShip();
            this.createShip();
            this.port.notifyAllThatThereAreShipsOnDocks();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void createShip(){ //synchronized
        (new Ship("Ship" + (crrShipNum++))).queueOnDock();
        //notifyAll();
    }
}