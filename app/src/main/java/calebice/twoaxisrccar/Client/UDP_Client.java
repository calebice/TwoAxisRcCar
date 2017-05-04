//http://stackoverflow.com/questions/34799166/android-udp-cant-send-or-recive

package calebice.twoaxisrccar.Client;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * Created by reyes on 3/27/2017.
 *
 * Creates a UDP connection in order to connect to the Raspberry Pi server
 */
public class UDP_Client extends AsyncTask<HashMap, Integer, String> {

    /**
     * Creates an instance of UDP_Client that
     * @throws IOException if there is an invalid source
     */
    public UDP_Client() throws IOException {    }

    /**
     * Required method for AsyncTask, checks to see if there is a server available, and sends
     * data packets
     * @param params List of available information {ip address, udp message, port to send to}
     * @return null 
     */
    @Override
    protected String doInBackground(HashMap... params) {
        HashMap<String, Object> p = params[0];
        String testServer = (String) p.get("ip");
        String udpMsg = (String) p.get("msg");
        Integer port = (int) p.get("port");

        InetAddress serverAddr = null;
        DatagramSocket ds = null;
        DatagramPacket dp = null; try {
            serverAddr = InetAddress.getByName(testServer);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try{
            ds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, port);
        try {
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ds.close();
        return null;
    }
}
