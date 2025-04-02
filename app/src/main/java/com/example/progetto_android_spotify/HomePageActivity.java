package com.example.progetto_android_spotify;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class HomePageActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d("Home Page", "Sono passato alla nuova pagina");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);


    }
}
