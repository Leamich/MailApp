package it.school.mailapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.text.CaseMap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

public class MainActivity extends AppCompatActivity {
    MailHandler mh;
    ListView lw;
    SimpleAdapter ada;
    ArrayList<HashMap<String, String>> messages = new ArrayList<>();
    ArrayList<Integer> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lw = findViewById(R.id.listView);
        ada = new SimpleAdapter(this, messages, android.R.layout.simple_list_item_2,
                new String[]{"s", "f"},
                new int[]{android.R.id.text1, android.R.id.text2});
        lw.setAdapter(ada);
        lw.setOnItemClickListener(new ItemClickListener());

        startActivityForResult(new Intent(this, LoginActivity.class), 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            mh = data.getParcelableExtra("mh");
            fillListView();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void fillListView() {
        Runnable r = () -> {
            try {
                Folder inbox = mh.getInbox();
                inbox.open(Folder.READ_ONLY);
                Message[] msgs = inbox.getMessages();

                for (int i = msgs.length - 1; i >= 0; i--) {
                    Message m = msgs[i];
                    HashMap<String, String> res = new HashMap<>();
                    res.put("s", m.getSubject());
                    res.put("f", Arrays.toString(MailHandler.getUTFAddress(m.getFrom())));
                    ids.add(m.getMessageNumber());
                    runOnUiThread(() -> {
                        messages.add(res);
                        ada.notifyDataSetChanged();
                    });
                }
            } catch (MessagingException | NoSuchProviderException e) {
                e.printStackTrace();
            }
        };
        new Thread(r).start();
    }

    class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ada.notifyDataSetChanged();

            Intent i = new Intent(MainActivity.this, MessageView.class);
            i.putExtra("mh", mh);
            i.putExtra("pos", ids.get(position));
            i.putExtra("subj", messages.get(position).get("s"));
            i.putExtra("from", messages.get(position).get("f"));
            startActivityForResult(i, 1);
        }
    }
}