package com.example.progetto_android_spotify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        setContentView(R.layout.login_page);

        SharedData.tcp_conn = new TCPConnection(8001, "10.0.2.2");
        Log.d("MainActivity", "Starting TCPConnection thread");
        SharedData.tcp_conn.start();

        TextInputLayout username_input_layout = (TextInputLayout) findViewById(R.id.Username_Input_Field);
        TextInputEditText username_edit_text = (TextInputEditText) username_input_layout.getEditText();

        TextInputLayout password_input_layout = (TextInputLayout) findViewById(R.id.Password_Input_Field);
        TextInputEditText password_edit_text = (TextInputEditText) password_input_layout.getEditText();

        MaterialButton sign_in = (MaterialButton) findViewById(R.id.SignInButton);
        MaterialButton sign_up = (MaterialButton) findViewById(R.id.SignUpButton);

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if( username_edit_text == null || password_edit_text == null)
                    return;

                SharedData.USERNAME = username_edit_text.getText().toString();
                SharedData.PASSWORD = password_edit_text.getText().toString();

                Log.i("Data Extracted", "Username = " + SharedData.USERNAME);
                Log.i("Data Extracted", "Hashed Password = " + SharedData.PASSWORD);

                SharedData.tcp_conn.set_job_for( SharedData.SIGN_IN_REQUEST);

                while( SharedData.tcp_conn.getAnswer() == 0 ){}

                int answer_code = SharedData.tcp_conn.getAnswer();

                switch( answer_code ){
                    case 200:
                        Intent intent = new Intent(view.getContext(), HomePageActivity.class);
                        view.getContext().startActivity(intent);
                        ((Activity)view.getContext()).finish();
                        break;

                    case 404:
                        Toast.makeText(view.getContext(), "Credenziali errare. Riprova o crea un account se non lo hai", Toast.LENGTH_LONG).show();
                        break;

                    case 500:
                        Toast.makeText(view.getContext(), "Errore del server, riprova più tardi", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        });

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( username_edit_text == null || password_edit_text == null)
                    return;

                SharedData.USERNAME = username_edit_text.getText().toString();
                SharedData.PASSWORD = password_edit_text.getText().toString();

                Log.i("Data Extracted", "Username = " + SharedData.USERNAME);
                Log.i("Data Extracted", "Hashed Password = " + SharedData.PASSWORD);

                SharedData.tcp_conn.set_job_for( SharedData.SIGN_UP_REQUEST);

                while( SharedData.tcp_conn.getAnswer() == 0 ){}

                int answer_code = SharedData.tcp_conn.getAnswer();

                switch( answer_code ){
                    case 200:
                        Toast.makeText(view.getContext(), "Account creato correttamente, procedi al login", Toast.LENGTH_LONG).show();
                        break;

                    case 404:
                        Toast.makeText(view.getContext(), "Nome utente già in uso.", Toast.LENGTH_LONG).show();
                        break;

                    case 500:
                        Toast.makeText(view.getContext(), "Errore del server, riprova più tardi", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        });

    }
}
