public class Demo {
    public static void main(String[] args) {
        Port port = new Port();
        Ship.setPort(port);

        Thread deamon = new Thread(){
            @Override
            public void run() {
                int i = 1;
                while (true){
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    port.generateDailyReport(i++);
                }
            }
        };

        deamon.setDaemon(true);
        port.startWork();
        deamon.start();
    }
}