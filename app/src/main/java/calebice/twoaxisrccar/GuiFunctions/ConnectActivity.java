package calebice.twoaxisrccar.GuiFunctions;

/**
 * Authors Caleb Ice, Jesse Reyes, Justin Janker
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

import calebice.twoaxisrccar.R;

/**
 * Class is designed to read in a users IP address and Port number in order to
 * set up an instance of StreamControllerActivity when connectButton is pressed.
 */
public class ConnectActivity extends Activity {

        /**
         * Sets up the page to be viewed and interated with
         * @param savedInstanceState stores information about the current state
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.connect);

            final EditText ipaddressTEXT = (EditText)findViewById(R.id.ipaddressID);
            final EditText portnumberTEXT = (EditText)findViewById(R.id.portnumberID);

        /*Button used to connect to Raspberry Pi 3*/
            final Button connectButton = (Button) findViewById(R.id.connectID);
            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Create activity to switch over to other page.
                    if (ipaddressTEXT.length() == 0 || portnumberTEXT.length() == 0) {
                        //create an error message to inform user to input ip and port number
                        if(ipaddressTEXT.length() == 0 && portnumberTEXT.length() > 0 ) {
                            showToast("Please input an IP Address.", v);
                        }
                        else if(portnumberTEXT.length() == 0 && ipaddressTEXT.length() > 0) {
                            showToast("Please input a Port Number.", v);
                        }
                        else {
                            showToast("Please input an IP Address \n& Port Number.", v);
                        }
                    }
                    else {
                        String ip = ipaddressTEXT.getText().toString();
                        InetAddress addr = null;
                        try{
                            addr = InetAddress.getByName(ip);
                            Intent startVideo = new Intent(ConnectActivity.this, StreamControllerActivity.class);
                            startVideo.putExtra("ip", ipaddressTEXT.getText().toString());
                            startVideo.putExtra("port", portnumberTEXT.getText().toString());
                            startActivity(startVideo);
                        }
                        catch(UnknownHostException e){}
                        catch(NetworkOnMainThreadException e){
                            Toast.makeText(ConnectActivity.this,ip+" is not a valid Ip address",Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            final Button quitButton = (Button)findViewById(R.id.quitmenuID);
            quitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.exit(0);
                }
            });

            ipaddressTEXT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            portnumberTEXT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }

        /**
         * Creates an error message informing the user the condition of their connection
         * @param ErrorMessage Message to inform user
         * @param v the view that is being referenced
         */
        public void showToast(String ErrorMessage, View v) {
            Toast.makeText(this, ErrorMessage, Toast.LENGTH_LONG).show();
        }
    }
