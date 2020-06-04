import com.alipay.remoting.rpc.RpcClient;
import rpc.ClientRequest;
import rpc.ClientResponse;
import rpc.RPCClient;

import java.util.Scanner;

public class Client {

    private final static RpcClient client = new RpcClient();

    static {
        client.init();
    }

    public static void main(String[] args) {
        if (args.length != 2 || !args[0].equals("-host")) {
            System.out.println("Invalid parameter.");
            System.exit(0);
        }
        String host = args[1];

        Scanner scan = new Scanner(System.in);
        while (true) {
            String input = scan.nextLine();
            if (input.equals("quit")) {
                break;
            }

            String[] words = input.split(" ");
            String command = words[0].toUpperCase();
            String key = null;
            String value = null;

            if (!(command.equals("GET") || command.equals("PUT"))) {
                System.out.println("Invalid command.");
                continue;
            }

            if (command.equals("GET")) {
                if (words.length != 2) {
                    System.out.println("Invalid command.");
                    continue;
                }
                key = words[1];
            } else {
                if (words.length != 3) {
                    System.out.println("Invalid command.");
                    continue;
                }
                key = words[1];
                value = words[2];
            }


            ClientRequest request =
                    new ClientRequest(command.equals("GET") ? 0 : 1, key, value);

            ClientResponse response = null;
            try{
                response = (ClientResponse) client.invokeSync(host, request, 20000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (response == null) {
                System.out.println("Something wrong...");
                continue;
            }

            if (command.equals("GET")) {
                System.out.println(response.getResult());
            } else {
                System.out.println(response.getStatus());
            }


        }


    }
}
