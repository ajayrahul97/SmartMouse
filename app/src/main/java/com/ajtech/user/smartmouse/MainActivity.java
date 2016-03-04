package com.ajtech.user.smartmouse;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,SensorEventListener {
    Context context;
    Button playPauseButton;
    Button nextButton;
    Button previousButton;
    TextView mousePad,x,y,z;
    SeekBar s1;

    TextView sensorData;
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private boolean isConnected=false;
    private float lastX,lastY,lastZ;
    private boolean mouseMoved=false;
    private boolean setValueX=true;
    private boolean setValueY=true;
    private Socket socket;
    private PrintWriter out;
    private double deltaX=0;
    private double deltaY=0;
    private float deltaZ=0;
    private float initX =0;
    private float initY =0;
    private double disX =0;
    private double disY =0;
    private double arX[]={0,0,0,0,0};
    private double arY[]={0,0,0,0,0};
    private int n =4;
    double avg[]={0,0};
    double sum[]={0,0};
    double ratio;
    float x1= ((float) ratio);
    int x2= Math.round(x1*100000);

    BigDecimal disx,disy;

    private float threshold=5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, accelerometer, 30000);
        }



        context = this; //save the context to show Toast messages
        //Get references of all buttons
        playPauseButton = (Button)findViewById(R.id.playPauseButton);

        //this activity extends View.OnClickListener, set this as onClickListener
        //for all buttons
        playPauseButton.setOnClickListener(this);
        s1=(SeekBar)findViewById(R.id.seekBar);

        sensorData = (TextView)findViewById(R.id.sensorData);
        x = (TextView)findViewById(R.id.x);
        y = (TextView)findViewById(R.id.y);
        z = (TextView)findViewById(R.id.z);
        //Get reference to the TextView acting as mousepad
        mousePad = (TextView)findViewById(R.id.mousePad);
        //capture finger taps and movement on the textview
        mousePad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isConnected && out!=null){
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            //save X and Y positions when user touches the TextView
                            initX =event.getX();
                            initY =event.getY();
                            mouseMoved=false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            disX = event.getX()- initX; //Mouse movement in x direction
                            disY = event.getY()- initY; //Mouse movement in y direction
                            /*set init to new position so that continuous mouse movement
                            is captured*/
                            initX = event.getX();
                            initY = event.getY();
                            if(disX !=0|| disY !=0){
                                out.println(disX +","+ disY); //send mouse movement to server
                            }
                            mouseMoved=true;
                            break;
                        case MotionEvent.ACTION_UP:
                            //consider a tap only if usr did not move mouse after ACTION_DOWN
                            if(!mouseMoved){
                                out.println("left_click");
                            }
                    }
                }
                return true;
            }
        });


        s1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            double pro;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pro = progress;
                ratio = pro/s1.getMax();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ratio = pro/s1.getMax();
                Toast.makeText(MainActivity.this,"Changing sensitivity",Toast.LENGTH_LONG).show();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ratio = pro/s1.getMax();

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
        //mSensorManager.registerListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

    }
    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // get the change of the x,y,z values of the accelerometer

        deltaX = lastX - event.values[0];
        deltaY = lastY - event.values[1];
        deltaZ = lastZ - event.values[2];

        if(deltaX<=0.3 &&deltaX>=-0.3)
        {
            deltaX=0;
        }
        if(deltaY<=0.2 &&deltaY>=-0.2)
        {
            deltaY=0;
        }

        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];

        if(isConnected && out!=null){
            disX=deltaX;
            disX=disX*(150+(ratio*100));
            //disX=Math.round(disX);
            disY=deltaY;
            disY=disY*(100+(ratio*100));
            //disY=Math.round(disY);
            avesens(disX,disY);
          // average(disX,disY);

        }

        /*if(isConnected && out!=null)
        {
            if (lastX>4.0 )
            {
                disX=20;
            }
            else if (lastX<-4.0 )
            {
                disX=-20;
            }
            if (lastY>3.0 )
            {
                disY=20;
            }
            else if (lastY<-3.0 )
            {
                disY=-20;
            }
            out.println(disX+","+disY);

        }*/

    }

    public void lowpass(float x,float y)
    {
        double a[]={0,0};
        a[0]=a[0]* 0.3 + 0.7*x;
        a[1]=a[1]*0.3+ y*0.7;
        out.println( a[0]+ "," + a[1]);
    }


    public void average(float x,float y)
    {

        if (n!=-1){
            arX[n]=x;
            arY[n]=y;
            n--;
        }
        if(n==-1)
        {
            for(int i=0;i<5;i++)
            {
                sum[0]+=arX[i];
                sum[1]+=arY[i];

            }
            avg[0]=sum[0]/5;
            avg[1]=sum[1]/5;
            out.println( avg[0]+ "," + avg[1]);
            // Now to reset the values for a new set of values;
            avg[0]=0;
            avg[1]=0;
            sum[0]=0;
            sum[1]=0;

            for(int i=0;i<5;i++)
            {
                arX[i]=0;
                arY[i]=0;

            }
            n=4;

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

    public void displayCleanValues(){
        x.setText("0.0");
        y.setText("0.0");
        z.setText("0.0");
    }

    public void displayCurrentValues(){
        x.setText(Double.toString(deltaX));
        y.setText(Double.toString(deltaY));
        z.setText(Double.toString(deltaZ));
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
        if(id == R.id.action_connect) {
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
    public void onDestroy()
    {
        super.onDestroy();
        if(isConnected && out!=null) {
            try {
                out.println("exit"); //tell server to exit
                socket.close(); //close socket
            } catch (IOException e) {
                Log.e("remotedroid", "Error in closing socket", e);
            }
        }
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
            Toast.makeText(context,isConnected?"Connected to server!":"Error while connecting",Toast.LENGTH_LONG).show();
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

