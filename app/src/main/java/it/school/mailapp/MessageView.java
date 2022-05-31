package it.school.mailapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import java.io.IOException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

public class MessageView extends AppCompatActivity {
    int position;
    MailHandler mh;
    Message msg;
    TextView fromText, subjectText, contentText, attachText;
    String subject, from;
    MyMessageView content;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);

        Intent i = getIntent();
        position = i.getIntExtra("pos", 0);
        mh = i.getParcelableExtra("mh");
        from = i.getStringExtra("from");
        subject = i.getStringExtra("subj");

        System.out.println();

        fromText = findViewById(R.id.fromText);
        subjectText = findViewById(R.id.subjectText);
        contentText = findViewById(R.id.contentText);
        attachText = findViewById(R.id.attachText);

        fromText.setText(from);
        subjectText.setText(subject);

        loadMsg();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void loadMsg() {
        Runnable r = () -> {
            try {
                Folder inbox = mh.getInbox();
                inbox.open(Folder.READ_ONLY);
                msg = inbox.getMessage(position);
                content = new MyMessageView(msg);
                runOnUiThread(this::showContent);
            } catch (MessagingException | NoSuchProviderException e) {
                e.printStackTrace();
            }
        };
        new Thread(r).start();
    }

    public void showContent() {
        String c = content.getContent();
        contentText.setText(Html.fromHtml(c));
        attachText.setText(content.getAttachments());
    }
}