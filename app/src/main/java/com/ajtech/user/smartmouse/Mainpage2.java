package com.ajtech.user.smartmouse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by user on 28-02-2016.
 */
public class Mainpage2 extends Activity {

    Button b1,b2,b3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage2);


        b2 = (Button)findViewById(R.id.gy);
        b3 = (Button)findViewById(R.id.ac);
        b1 = (Button)findViewById(R.id.b4);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open = new Intent(Mainpage2.this, Racing.class);
                startActivity(open);

            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open = new Intent(Mainpage2.this, Maingyroscope.class);
                startActivity(open);

            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open = new Intent(Mainpage2.this, MainActivity.class);
                startActivity(open);

            }
        });


    }
}
