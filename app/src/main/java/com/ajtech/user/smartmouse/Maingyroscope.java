package com.ajtech.user.smartmouse;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;


public class Maingyroscope extends AppCompatActivity implements View.OnClickListener,SensorEventListener {
    Context context;
    Button playPauseButton;
    TextView mousePad;
    TextView sensorData;

    private SensorManager mSensorManager;
    private boolean isConnected = false;
    private boolean mouseMoved = false;
    private boolean setValueX = true;
    private boolean setValueY = true;
    private Socket socket;
    private PrintWriter out;
    private double initX = 0;
    private double initY = 0;
    private double disX = 0;
    private double disY = 0;
    private double arX[]={0,0,0,0,0};
    private double arY[]={0,0,0,0,0};
    private double threshold =10;
    private double threshold2 =-10;
    BigDecimal disx,disy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        context = this; //save the context to show Toast messages
        //Get references of all buttons
        playPauseButton = (Button) findViewById(R.id.playPauseButton);

        //this activity extends View.OnClickListener, set this as onClickListener
        //for all buttons
        playPauseButton.setOnClickListener(this);

        sensorData = (TextView) findViewById(R.id.sensorData);
        //Get reference to the TextView acting as mousepad
        mousePad = (TextView) findViewById(R.id.mousePad);
        //capture finger taps and movement on the textview
        mousePad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isConnected && out != null) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //save X and Y positions when user touches the TextView
                            initX = event.getX();
                            initY = event.getY();
                            mouseMoved = false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            disX = event.getX() - initX; //Mouse movement in x direction
                            disY = event.getY() - initY; //Mouse movement in y direction
                            /*set init to new position so that continuous mouse movement
                            is captured*/
                            initX = event.getX();
                            initY = event.getY();
                            if (disX != 0 || disY != 0) {
                                out.println(disX + "," + disY); //send mouse movement to server
                            }
                            mouseMoved = true;
                            break;
                        case MotionEvent.ACTION_UP:
                            //consider a tap only if usr did not move mouse after ACTION_DOWN
                            if (!mouseMoved) {
                                out.println("left_click");
                            }
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                    out.println("left_click");
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                    out.println("right_click");
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), 30000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorData.setText("Heading: " + Float.toString(Math.round(event.values[0])) + ", " + Float.toString(Math.round(event.values[1])) + " degrees");
        // get the angle around the z-axis rotated
        //float degree = Math.round(event.values[0]);
        if (setValueX ) {
            initX = (event.values[0]);
            setValueX = false;
        }
        if (setValueY) {
            initY = (event.values[1]);
            setValueY = false;
        }
        //initY =event.getY();
        if (isConnected && out != null) {
            {
                disX = (event.values[0]) - initX; //Mouse movement in x direction
                disX *= 100;
                //disX = Math.round(disX);
                disY = (event.values[1]) - initY; //Mouse movement in x direction
                disY *= 100;
                //disY = Math.round(disY);
                //disY = event.getY() - initY; //Mouse movement in y direction
                            /*set init to new position so that continuous mouse movement
                            is captured*/
                //initX = Math.round(event.values[0]);
                //initY = event.getY();
                if ((disX > threshold || disY > threshold)) {
                    //avesens(disX, disY);
                   out.println(disX + "," + disY); //send mouse movement to server

                    if (disX != 0) {
                        setValueX = true;
                    }
                    if (disY != 0) {
                        setValueY = true;
                    }
                }

            }
        }
    }


    public void avesens(double x, double y)
    {
        double avg[]={0,0};
        double sum[]={0,0};
        arX[0]= x;
        arY[0]=y;

        for(int i=4;i>0;i--)
        {
            arX[i]=arX[i-1];
            arY[i]=arY[i-1];

        }

        for(int i=0;i<5;i++)
        {
            sum[0]+=arX[i];
            sum[1]+=arY[i];

        }
        avg[0]=sum[0]/5;
        avg[1]=sum[1]/5;
        disx = new BigDecimal(avg[0]);
        disy = new BigDecimal(avg[1]);
        out.println( disx+ "," + disy);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect) {
            ConnectPhoneTask connectPhoneTask = new ConnectPhoneTask();
            connectPhoneTask.execute(Constants.SERVER_IP); //try to connect to server in another thread
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //OnClick method is called when any of the buttons are pressed
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playPauseButton:
                ConnectPhoneTask connectPhoneTask = new ConnectPhoneTask();
                connectPhoneTask.execute(Constants.SERVER_IP); //try to connect to server in another thread
                break;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isConnected && out != null) {
            try {
                out.println("exit"); //tell server to exit
                socket.close(); //close socket
            } catch (IOException e) {
                Log.e("remotedroid", "Error in closing socket", e);
            }
        }
    }

    public class ConnectPhoneTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = true;
            try {
                InetAddress serverAddr = InetAddress.getByName(params[0]);
                socket = new Socket(serverAddr, Constants.SERVER_PORT);//Open socket on server IP and port
            } catch (IOException e) {
                Log.e("remotedroid", "Error while connecting:" + e.toString(), e);
                result = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            isConnected = result;
            Toast.makeText(context, isConnected ? "Connected to server!" : "Error while connecting", Toast.LENGTH_LONG).show();
            try {
                if (isConnected) {
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                            .getOutputStream())), true); //create output stream to send data to server
                }
            } catch (IOException e) {
                Log.e("remotedroid", "Error while creating OutWriter", e);
                Toast.makeText(context, "Error while connecting", Toast.LENGTH_LONG).show();
            }
        }
    }
}