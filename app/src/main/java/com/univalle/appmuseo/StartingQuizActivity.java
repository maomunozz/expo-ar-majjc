package com.univalle.appmuseo;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class StartingQuizActivity extends AppCompatActivity {

    private static final String USER_MAIL = "majcc.univalle.ar@gmail.com";
    private static final String USER_PASSWORD = "M40z#1219";

    private static final int REQUEST_CODE_QUIZ = 1;

    private TextView textViewHighscore;
    private Button buttonStart;
    private EditText editTextMail;
    private Session session;

    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_quiz);

        buttonStart = findViewById(R.id.button_start_quiz);

        textViewHighscore = findViewById(R.id.text_view_highscore);
        editTextMail = findViewById(R.id.editMail);
        loadHighscore();

        Button buttonStart_Quiz = findViewById(R.id.button_start_quiz);
        buttonStart_Quiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuiz();
            }
        });
    }

    private void startQuiz() {
        Intent intent = new Intent(StartingQuizActivity.this, QuizActivity.class);
        startActivityForResult(intent, REQUEST_CODE_QUIZ);
        Toast.makeText(StartingQuizActivity.this, "Buena suerte", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_QUIZ) {
            if (resultCode == RESULT_OK) {
                int score = data.getIntExtra(QuizActivity.EXTRA_SCORE, 0);
                //if (score > highscore) {
                    updateHighscore(score);
                //}
            }
        }
    }

    public void loadHighscore() {
        editTextMail.setVisibility(View.GONE);
    }

    public void updateHighscore(int scoreNew) {
        score = scoreNew;
        textViewHighscore.setText("Respuestas correctas: " + score);

        if (score < 5){
            buttonStart.setText("Volver a intentar");
        } else {
            editTextMail.setVisibility(View.VISIBLE);
            buttonStart.setText("Terminar");
            Button buttonStart_Quiz = findViewById(R.id.button_start_quiz);
            buttonStart_Quiz.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mensaje = "Te dejaremos un obsequio por tu visita";
                    String recipiente = editTextMail.getText().toString().trim();
                    Boolean validate = validateEmail(recipiente);
                    if (validate) {
                        sendEmail(recipiente, mensaje);
                        finish();
                    }
                }
            });
        }
    }

    public boolean validateEmail(String email){
        if (email.isEmpty()){
            editTextMail.setError("Debes ingresar un correo");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextMail.setError("Por favor ingresar un correo valido");
            return false;
        } else {
            editTextMail.setError(null);
            return true;
        }
    }

    protected void sendEmail(String recipiente, String mensaje) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Properties properties = new Properties();
        properties.put("mail.smtp.host","smtp.googlemail.com");
        properties.put("mail.smtp.socketFactory.port","465");
        properties.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth","true");
        properties.put("mail.smtp.port","465");

        try {
            session = javax.mail.Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USER_MAIL, USER_PASSWORD);
                }
            });

            if (session != null){
                javax.mail.Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(USER_MAIL));
                message.setSubject("Gracias por visitar el museo");
                message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(recipiente));
                message.setContent(mensaje,"text/html; charset=utf-8");

                Transport.send(message);
                Toast.makeText(StartingQuizActivity.this, "Enviamos tu recuerdo, gracias por tu visita", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(StartingQuizActivity.this, "Problemas al enviar, inténtalo mas tarde", Toast.LENGTH_SHORT).show();
        }
    }
}
