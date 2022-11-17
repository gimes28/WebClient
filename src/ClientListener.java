import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

// Very simply class that simply reads information from the server in another thread
// This allows for main thread to still write to server while getting updates from server
public class ClientListener implements Runnable{
    BufferedReader controlReader;

    public ClientListener(BufferedReader serverOutput)
    {
        controlReader = serverOutput;
    }
    public void run()
    {
        System.out.println("Now listening for server responses");
        try {
            while (true) {
                System.out.println(controlReader.readLine());
            }
        }catch(IOException ex)
        {System.out.println("Exception: " + ex);}
    }
}
