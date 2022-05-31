package it.school.mailapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.security.NoSuchProviderException;
import java.util.function.Consumer;


import javax.mail.MessagingException;

public class LoginActivity extends Activity {
    class ChangeLoginEditTextListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            LoginActivity.this.resetButton();
        }
    }

    public Button sendBtn;
    public EditText edServer;
    public EditText edPort;
    public EditText edMail;
    public EditText edPassword;
    public MailHandler mh;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sendBtn = findViewById(R.id.sendBtn);
        edServer = findViewById(R.id.editTextServer);
        edPort = findViewById(R.id.editTextPort);
        edMail = findViewById(R.id.editTextMail);
        edPassword = findViewById(R.id.editTextPassword);

        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        String host_p = sPref.getString("login_host", "imap.mail.ru"),
                port_p = sPref.getString("login_port", "993"),
                name_p = sPref.getString("login_name", "mishak2005med@mail.ru"),
                pass_p = sPref.getString("login_pass", "");
        edServer.setText(host_p);
        edPort.setText(port_p);
        edMail.setText(name_p);
        edPassword.setText(pass_p);

        setForAllEditText((ed -> ed.addTextChangedListener(new ChangeLoginEditTextListener())));

        sendBtn.setOnClickListener(btn -> {
            Runnable r = () -> {
                try {
                    String port = edPort.getText().toString();
                    String mail = edMail.getText().toString();
                    String pass = edPassword.getText().toString();
                    String serv = edServer.getText().toString();

                    mh = new MailHandler(
                            mail, pass, serv, port
                    );
                    mh.connect();

                    SharedPreferences ePref = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = ePref.edit();
                    ed.putString("login_host", serv);
                    ed.putString("login_port", port);
                    ed.putString("login_name", mail);
                    ed.putString("login_pass", pass);
                    ed.apply();

                    Intent i = new Intent();
                    i.putExtra("mh", mh);
                    LoginActivity.this.setResult(RESULT_OK, i);
                    LoginActivity.this.finish();
                } catch (MessagingException | NoSuchProviderException e) {
                    e.printStackTrace();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            setDangerForButton();
                        }
                    }).start();
                }
            };

            new Thread(r).start();
        });

        resetButton();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setForAllEditText(Consumer<EditText> consumer) {
        consumer.accept(edPort);
        consumer.accept(edPassword);
        consumer.accept(edMail);
        consumer.accept(edServer);
    }

    public void resetButton() {
        sendBtn.setBackgroundColor(Color.CYAN);
    }

    public void setDangerForButton() {
        sendBtn.setBackgroundColor(Color.RED);
    }
}