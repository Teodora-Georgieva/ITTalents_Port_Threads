import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class Port {
    private ArrayList<Dock> docks;
    private Warehouse warehouse1;
    private Warehouse warehouse2;
    private Distributor distributor1;
    private Distributor distributor2;
    private Crane crane1;
    private Crane crane2;
    private TreeMap<String, TreeMap<LocalDateTime, String>> portDiary;
    private ArrayList<Ship> allShips;

    public Port(){
        this.docks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            this.addDock(new Dock("Dock" + (i+1)));
        }

        this.portDiary = new TreeMap<>();
        this.allShips = new ArrayList<>();
        this.warehouse1 = new Warehouse("Warehouse1");
        this.warehouse2 = new Warehouse("Warehouse2");

        this.distributor1 = new Distributor("Distributor1", warehouse1, this);
        this.warehouse1.setDistributor(distributor1);
        this.distributor2 = new Distributor("Distributor2", warehouse2, this);
        this.warehouse2.setDistributor(distributor2);

        this.crane1 = new Crane("Crane1", this);
        this.crane2 = new Crane("Crane2", this);
    }

    private void addDock(Dock dock){
        this.docks.add(dock);
    }

    public void addShipToList(Ship ship){
        this.allShips.add(ship);
    }

    public Dock getRandomDock(){
        Random r = new Random();
        int numOfDocks = this.docks.size();
        int dockIdx = r.nextInt(numOfDocks);
        return this.docks.get(dockIdx);
    }

    public void startWork(){
        ShipCreator shipThread = new ShipCreator(this);
        //this.notifyAllThatThereAreShipsOnDocks();
        shipThread.start();
        this.crane1.start();
        this.crane2.start();
        this.distributor1.start();
        this.distributor2.start();
    }

    public synchronized void notifyAllThatThereAreShipsOnDocks(){
        System.out.println("Notifying all that there are ships on docks!!!");
        notifyAll();
    }

    public synchronized void exportShipment(Distributor distributor) {
        Warehouse warehouse = distributor.getWarehouse();
        while (warehouse.isEmpty()){
            try {
                System.out.println(Thread.currentThread().getName() + " waits because there are no shipments in " +
                                                                           warehouse.getName());
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Shipment crrShipment = warehouse.exportNextShipment();
        System.out.println(Thread.currentThread().getName() + " exported " + crrShipment.getName() + " from " +
                                                                                       warehouse.getName());

        notifyAll();
    }

    public synchronized int serveDocks(){
        String craneName = Thread.currentThread().getName();
        long craneID = Thread.currentThread().getId(); //this is the id of the thread, not of the crane
                                                      //how to fix it?

        while(!thereAreShipsOnAnyDock()) {
            try {
                System.out.println(craneName + " waits because there are no ships on the docks");
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Ship ship = this.getNextShipFromAnyDock();
        String dockName = ship.getDockName();
        ArrayList<Shipment> shipments = ship.emptyShip();
        for (int i = 0; i < shipments.size(); i++) {
            Warehouse warehouse = this.getRandomWarehouse();
            Shipment crrShipment = shipments.get(i);
            int shipmentID = crrShipment.getShipmentID();
            warehouse.receiveShipment(crrShipment);

            if (!this.portDiary.containsKey(dockName)) {
                this.portDiary.put(dockName, new TreeMap<>());
            }
            TreeMap<LocalDateTime, String> info = this.portDiary.get(dockName);
            info.put(LocalDateTime.now(), crrShipment.getName() + ", ship " + ship.getName() + ", crane " + craneName);
            this.insertIntoDatabase(ship.getName(), ship.getDockID(), craneID, LocalDateTime.now(), shipmentID);

            System.out.println(craneName + " - " +
                    ship.getName() + " - " + crrShipment.getName() + " is put in " + warehouse.getName());
            try {
                Thread.currentThread().sleep(2000); //sleeps for 2 seconds for each shipment
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        notifyAll();
        return shipments.size();
    }

    private void insertIntoDatabase(String name, long dockID, long craneID, LocalDateTime unloadingTime, long shipmentID){
        Connection connection = DBConnector.getInstance().getConnection();
        String insertQuery = "INSERT INTO port_shipments (boat_name, dock_id, crane_id, unloading_time, package_id) " +
                              "VALUES (?, ?, ?, ?, ?);";
        try(PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);){
            ps.setString(1, name);
            ps.setLong(2, dockID);
            ps.setLong(3, craneID);
            ps.setTimestamp(4, Timestamp.valueOf(unloadingTime));
            ps.setLong(5, shipmentID);
            ps.executeUpdate();
            System.out.println("Inserted into DB");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private boolean thereAreShipsOnAnyDock(){
        for (int i = 0; i < this.docks.size(); i++) {
            if(this.docks.get(i).getNumberOfShipsOnQueue() > 0){
                return true;
            }
        }

        return false;
    }

    private Ship getNextShipFromAnyDock(){
        for (int i = 0; i < this.docks.size(); i++) {
            Dock crrDock = this.docks.get(i);
            if(crrDock.getNumberOfShipsOnQueue() > 0){
                return crrDock.getNextShip();
            }
        }

        return null;
    }

    private Warehouse getRandomWarehouse(){
        Random r = new Random();
        return r.nextBoolean() ? this.warehouse1 : this.warehouse2;
    }

    public void generateDailyReport(int i){
        File directory = new File("Directory_" + i);
        directory.mkdir();

        this.generateReportForAllShipments(directory);
        this.generateReportForDocksAndShips(directory);
        this.generateReportForCranesAndShipments(directory);
        this.generateReportForShipWithMostShipments(directory);
        System.out.println("Files with reports generated!");
    }

    private void generateReportForAllShipments(File dir){
        File file = new File(dir, "allShipments.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try(PrintStream ps = new PrintStream(file);) {
            for(String dock : this.portDiary.keySet()){
                ps.println(dock);
                TreeMap<LocalDateTime, String> info = this.portDiary.get(dock);
                for(Map.Entry<LocalDateTime, String> entry : info.entrySet()){
                    ps.println("    " + entry.getKey() + " - " + entry.getValue());
                }
                ps.println();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void generateReportForDocksAndShips(File dir){
        TreeMap<String, Integer> docksAndShips = new TreeMap<>();
        for(Dock dock : this.docks){
            docksAndShips.put(dock.getName(), dock.getTotalNumberOfShips());
        }

        File file = new File(dir, "docksWithNumberOfShips.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try(PrintStream ps = new PrintStream(file);) {
            for (Map.Entry<String, Integer> entry : docksAndShips.entrySet()) {
                ps.println(entry.getKey() + " - " + entry.getValue() + " ships");
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void generateReportForCranesAndShipments(File dir){
        TreeMap<String, Integer> craneAndNumberOfShipments = new TreeMap<>();
        craneAndNumberOfShipments.put(crane1.getName(), crane1.getTotalNumberOfShipments());
        craneAndNumberOfShipments.put(crane2.getName(), crane2.getTotalNumberOfShipments());

        File file = new File(dir,"cranesWithNumberOfShipments.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try(PrintStream ps = new PrintStream(file);) {
            for (Map.Entry<String, Integer> entry : craneAndNumberOfShipments.entrySet()) {
                ps.println(entry.getKey() + " - " + entry.getValue() + " shipments");
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void generateReportForShipWithMostShipments(File dir){
        TreeMap<String, Integer> shipsWithShipments = new TreeMap<>();
        for(Ship ship : this.allShips){
            shipsWithShipments.put(ship.getName(), ship.getTotalNumberOfUnloadShipments());
        }

        ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.addAll(shipsWithShipments.entrySet());
        entries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        File file = new File(dir,"shipWithMostShipments.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try(PrintStream ps = new PrintStream(file);){
            for(Map.Entry<String, Integer> entry : entries){
                ps.println(entry.getKey() + " - " + entry.getValue() + " shipments");
            }

            ps.println();
            if(entries.size() > 0) {
                ps.println("Ship with most shipments: " + entries.get(0).getKey());
            }
            else{
                ps.println("Still no ships");
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}