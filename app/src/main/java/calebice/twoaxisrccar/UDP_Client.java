//http://stackoverflow.com/questions/34799166/android-udp-cant-send-or-recive

package calebice.twoaxisrccar;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * Created by reyes on 3/27/2017.
 *
 *
 * Creates a UDP connection in order to connect to the Raspberry Pi server
 */
class UDP_Client extends AsyncTask<HashMap, Integer, String> {
    //Initialize Variables
    InetAddress IPAddress = null;
    int Port = 5432;
    DatagramSocket clientSocket;
    DatagramPacket sendPacket;
    byte[] sendData, receiveData;
    //BufferedReader inFromUser;

    //Constructors
    public UDP_Client() throws IOException {    }
    public UDP_Client(String ipaddress, int port) throws IOException {
        IPAddress = InetAddress.getByName(ipaddress);
        Port = port;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        //clientSocket = new DatagramSocket();
        receiveData = new byte[1024];

    }

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
