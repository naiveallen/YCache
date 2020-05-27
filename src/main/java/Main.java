import node.Node;

import java.io.IOException;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2 || !args[0].equals("-port")) {
            System.out.println("Invalid parameter.");
            System.exit(0);
        }
        int port = Integer.parseInt(args[1]);

        Properties props = new Properties();
        try {
            props.load(new java.io.FileInputStream("src/main/Config.properties"));
        } catch (IOException e) {
            System.out.println("Load property file error.");
        }

        String cluster = props.getProperty("cluster");
        String[] hosts = cluster.split(",");


        Node node = Node.getInstance();

        node.init(port, hosts);

        node.start();



    }
}

