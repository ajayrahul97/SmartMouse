package com.ajtech.user.smartmouse;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by user on 28-02-2016.
 */
public class Mainpage extends Activity {
    Context context;
    TextView ip, port;
    private Socket socket;
    private boolean isConnected=false;
    Button connect, proc;

    private PrintWriter out;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);
        ip= (TextView) findViewById(R.id.ip);
        port= (TextView) findViewById(R.id.port);
        connect = (Button) findViewById(R.id.connect);
        context = this;
        proc = (Button) findViewById(R.id.proceed);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Constants.SERVER_IP= ip.getText().toString();
                Constants.SERVER_PORT = Integer.parseInt(port.getText().toString());
                proc.setVisibility(View.VISIBLE);


            }

        });

        proc.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                Intent open = new Intent(Mainpage.this, Mainpage2.class);
                startActivity(open);

            }
        });

    }

    public class ConnectPhoneTask extends AsyncTask<String,Void,Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = true;
            try {
                InetAddress serverAddr = InetAddress.getByName(params[0]);
                socket = new Socket(serverAddr,Constants.SERVER_PORT );//Open socket on server IP and port
            } catch (IOException e) {
                Log.e("remotedroid", "Error while connecting:"+e.toString() ,e);
                result = false;
            }
            return result;
        }
        @Override
        protected void onPostExecute(Boolean result)
        {
            isConnected = result;
            Toast.makeText(context, isConnected ? "Connected to server!" : "Error while connecting", Toast.LENGTH_LONG).show();
            try {
                if(isConnected) {
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                            .getOutputStream())), true); //create output stream to send data to server
                }
            }catch (IOException e){
                Log.e("remotedroid", "Error while creating OutWriter", e);
                Toast.makeText(context,"Error while connecting",Toast.LENGTH_LONG).show();
            }
        }
    }

}
