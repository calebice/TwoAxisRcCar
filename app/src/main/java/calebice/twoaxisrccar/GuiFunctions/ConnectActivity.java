package calebice.twoaxisrccar.GuiFunctions;

/**
 * Authors Caleb Ice, Jesse Reyes, Justin Janker
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import calebice.twoaxisrccar.R;

/**
 * Class is designed to read in a users IP address and Port number in order to
 * set up an instance of StreamControllerActivity when connectButton is pressed.
 */
public class ConnectActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);

        Button button = (Button) findViewById(R.id.connectButton);
        final EditText ipText = (EditText) findViewById(R.id.ipTextID);
        final EditText portText = (EditText) findViewById(R.id.portTextID);
        final Context me = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(me, StreamControllerActivity.class);
                intent.putExtra("ip", ipText.getText().toString());
                intent.putExtra("port",portText.getText().toString());
                startActivity(intent);
            }
        });
    }
}
