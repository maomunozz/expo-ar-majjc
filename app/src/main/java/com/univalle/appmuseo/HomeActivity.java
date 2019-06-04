package com.univalle.appmuseo;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    Dialog popupQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        popupQuiz = new Dialog(this);
    }

    public void goAumentedRealityOcarinas(View view){
        Intent i = new Intent(this, AugmentedOcarinasActivity.class);
        startActivity(i);
        //Toast toast = Toast.makeText(HomeActivity.this, "Busca una superficie plana",
                //Toast.LENGTH_LONG);
        //toast.show();
     }

    public void goAumentedRealityPlatos(View view){
        Intent i = new Intent(this, AugmentedPlatosActivity.class);
        startActivity(i);
        //Toast toast = Toast.makeText(HomeActivity.this, "Busca una superficie plana",
                //Toast.LENGTH_LONG);
        //toast.show();
    }

    public void goQuizStarting(View view){
        popupQuiz.setContentView(R.layout.style_alert_dialog);
        ConstraintLayout constraintLayout = popupQuiz.findViewById(R.id.layout_starting_quiz);
        Intent i = new Intent(this, StartingQuizActivity.class);
        Button si = popupQuiz.findViewById(R.id.button_yes);
        si.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(i);
                popupQuiz.dismiss();
            }
        });

        Button no = popupQuiz.findViewById(R.id.button_no);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupQuiz.dismiss();
                Toast.makeText(HomeActivity.this, "Puedes visitar las secciones de realidad aumentada y volver por el quiz", Toast.LENGTH_LONG).show();
            }
        });
        popupQuiz.show();
    }

}
