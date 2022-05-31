package it.school.mailapp;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;


public class MailHandler implements Parcelable {
    final String username, password, host, port;
    protected Store store;

    public MailHandler(String username, String password, String host, String port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    public javax.mail.Store connect() throws javax.mail.MessagingException, NoSuchProviderException {
        if (store == null) {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            javax.mail.Session session = javax.mail.Session.getInstance(props);
            store = session.getStore();
            store.connect(host, username, password);
            return store;
        } return store;
    }

    public javax.mail.Folder getInbox() throws MessagingException, NoSuchProviderException {
        return connect().getFolder("INBOX");
    }

    public static String encodeBase64(String s) {
        int start_ind = s.toLowerCase().indexOf("=?utf-8?"),
                end_ind = s.lastIndexOf("?=");
        if (start_ind >= 0 && end_ind >= 0) {
            String decoding = "";
            try {
                decoding = MimeUtility.decodeText(s.substring(start_ind, end_ind + 2));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            s = s.substring(0, start_ind)
                    + decoding
                    + s.substring(end_ind + 2);
        }
        return s;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String[] getUTFAddress(Address[] addresses) {
        if (addresses == null) return new String[]{};

        ArrayList<String> result = new ArrayList<>();
        for (Address ad : addresses) {
            String res = ad.toString();
            int start_ind = res.toLowerCase().indexOf("=?utf-8?b?"),
                    end_ind = res.lastIndexOf("?=");

            if (start_ind >= 0 && end_ind >= 0) {
                String decoding = "";
                try {
                    decoding = MimeUtility.decodeText(res.substring(start_ind, end_ind + 2));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                res = res.substring(0, start_ind)
                        + decoding
                        + res.substring(end_ind + 2);
            }
            result.add(res);
        }

        return result.toArray(new String[0]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] { username, password, host, port });
    }

    public static final Parcelable.Creator<MailHandler> CREATOR = new Parcelable.Creator<>() {

        @Override
        public MailHandler createFromParcel(Parcel source) {
            String[] data = new String[4];
            source.readStringArray(data);
            return new MailHandler(data[0], data[1], data[2], data[3]);
        }

        @Override
        public MailHandler[] newArray(int size) {
            return new MailHandler[size];
        }
    };
}