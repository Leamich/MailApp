package it.school.mailapp;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

public class MyMessageView {
    protected StringBuilder content;
    protected ArrayList<String> attach;

    public MyMessageView(String content, String... attach) {
        initialize();
        addContent(content);
        this.attach = new ArrayList<>(Arrays.asList(attach));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public MyMessageView(Message m) {
        initialize();
        addContent(m);
    }

    public MyMessageView() {
        initialize();
    }

    protected void initialize() {
        content = new StringBuilder();
        attach = new ArrayList<>();
    }

    public void addContent(String c) {
        content.append(c.replace("\n", "<br>\n")).append("<br>").append("\n");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addContent(Part c) {
        try {
            String ct = c.getContentType();
            if (ct.startsWith("text"))
                addContent((String) c.getContent());
            else if (ct.startsWith("multipart/alternative")) {
                Multipart mp = (Multipart) c.getContent();
                addContent(mp.getBodyPart(0));
            }
            else if (ct.startsWith("multipart")) {
                Multipart mp = (Multipart) c.getContent();
                for (int j = 0; j < mp.getCount(); j++) {
                    BodyPart bp = mp.getBodyPart(j);
                    String ctbp = bp.getContentType();
//                    addContent(String.format(Locale.US, "%d) %s", j + 1, ctbp));
                    addContent((Part) bp);
                }
            } else if (ct.startsWith("application") || ct.startsWith("image")) {
                addAttachment(MailHandler.encodeBase64(c.getFileName()));
            }
            else addContent("Тип не поддерживается: " + ct);
        } catch (MessagingException | IOException e) {
            addContent("Произошла ошибка при загрузке");
        }
    }

    public void addAttachment(String at) {
        attach.add(at);
    }

    public String getContent() {
        return content.toString();
    }

    public String getAttachments() {
        if (attach.isEmpty()) return "Нет вложений";

        StringBuilder sb = new StringBuilder("Вложения:\n");
        for (int i = 0; i < attach.size(); i++) {
            sb.append(String.format(Locale.US, "%d) %s\n", i + 1, attach.get(i)));
        }
        return sb.toString();
    }
}
