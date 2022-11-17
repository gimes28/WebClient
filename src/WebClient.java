import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class WebClient {

    private Socket controlSocket = null;
    private BufferedReader controlReader = null;
    static BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
    private DataOutputStream controlWriter = null;
    private ClientListener listener;
    private String CurrentResponse;
    static String clientOptions = "%connect <IP address> <port number(int)>: Attempts connection to bulletin server.\n"
            +
            "%join: Joins global message board.\n" +
            "%post <subject> <content>: Post a message with subject and content on message board.\n" +
            "%users: Retrieve a list of users in current group.\n" +
            "%message <messageId>: View content of message with given ID.\n" +
            "%exit: Disconnect from the server, exit client program.\n";

    public WebClient() {
    }

    public void listen() throws IOException {
        listener = new ClientListener(controlReader); // Creates Listener to print out messages from server
        Thread thread = new Thread(listener); // Create fresh thread to run listener on
        // Start the thread.
        thread.start();
    }

    // this will be the command line command that connects the client to the server
    public void connect(String ipAddress, int portNumber) throws UnknownHostException {
        try {
            controlSocket = new Socket(ipAddress, portNumber);
            InputStream is = controlSocket.getInputStream();
            controlWriter = new DataOutputStream(controlSocket.getOutputStream());
            controlReader = new BufferedReader(new InputStreamReader(is));
            CurrentResponse = controlReader.readLine();
            System.out.println(CurrentResponse);

            // Prompts user for the username; append \n so that the reader will know when to
            // stop
            String userName = userInputReader.readLine() + "\n";
            controlWriter.writeBytes(userName);
            listen();
            sendCommand();
        } catch (IOException ex) {
            System.out.println("IOException: " + ex);
        }
    }

    // Method made to send information to the webserver via user input commands
    // Will never do error checking; leave that to server. Only will check for
    // connect/disconnect as that requires
    // Client-side work. Will cut up and send in pieces to make reading easier for
    // server
    public void sendCommand() {
        while (true) {
            try {
                System.out.println("You are free to write to the server!");
                String command = userInputReader.readLine();
                String[] commandArgs = command.split("\\s+");
                if (commandArgs[0].equals("%disconnect") && commandArgs.length == 1) {
                    disconnect();
                    break; // Will exit the loop and end sendCommand, allowing program to restart
                } else if (commandArgs[0].equals("%exit") && commandArgs.length == 1) {
                    exit();
                    break; // Will exit the loop and end sendCommand, allowing program to restart
                }
                controlWriter.writeBytes(command + "\n"); // leave it to server to split, determine validity
            } catch (IOException ex) {
                System.out.println("IOException: " + ex);
            }
        }
    }

    // self-explanatory, command line command to disconnect
    public void disconnect() {
        // needs to send a message notifying server of disconnection
        try {
            controlWriter.writeBytes("%disconnect\n");
            controlReader.close();
            controlWriter.close();
            controlSocket.close();
        } catch (IOException ex) {
            System.out.println("Client: IOException: " + ex);
        }
    }

    public void exit() {
        // needs to send a message notifying server of disconnection
        try {
            disconnect();
            controlWriter.writeBytes("%exit\n");
            return;

        } catch (IOException ex) {
            System.out.println("Client: IOException: " + ex);
        }
    }

    // Needs a loop to keep asking for commands
    public static void main(String argv[]) {
        while (true) {
            WebClient webClient = new WebClient();
            System.out.println("Welcome to the bulletin-board Web Client! Go ahead and use the following commands:\n"
                    + clientOptions);
            boolean connectionFound = false;
            while (!connectionFound)
                connectionFound = ShowConnectionMenu(webClient);
            webClient.sendCommand();
        }
    }

    private static Boolean ShowConnectionMenu(WebClient webClient) {
        int port = 0;
        String userInput;
        String ip = "empty";
        Boolean invalidCommand = true;
        while (invalidCommand) {
            System.out
                    .println("You are not yet connected to a server; please choose the correct way to write %connect");
            try {
                userInput = userInputReader.readLine();
            } catch (Exception ex) {
                userInput = "";
            }
            String[] arguments = userInput.split("\\s+"); // split into arguments
            if (userInput.startsWith("%connect") && arguments.length == 3) // If not in this format, can't continue
            {
                try {
                    port = Integer.parseInt(arguments[2]); // sets variables from arguments
                    ip = arguments[1];
                    invalidCommand = false;
                } catch (Exception ex) {
                    userInput = "";
                    invalidCommand = true; // sets while loop to true so it will ask again
                }
            } else if (userInput.startsWith("%exit") && arguments.length == 1) {

            }
        }
        try {
            webClient.connect(ip, port);
        } catch (UnknownHostException ex) {
            System.out.println("Couldn't connect to server: " + ex + "\n Please try again");
            return false;
        }
        return true;
    }
}
