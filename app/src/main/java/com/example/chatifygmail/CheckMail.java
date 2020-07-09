package com.example.chatifygmail;

import android.util.Log;

import com.example.chatifygmail.data.Email;
import com.sun.mail.pop3.POP3Store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;

public class CheckMail{

    private static ArrayList<Email> emails;

    public static ArrayList<Email> getEmails() {
        return emails;
    }

    public static void setEmails(ArrayList<Email> emails) {
        CheckMail.emails = emails;
    }

    public static void checkEmail(String pop3Host, String storeType,
                                  String user, String password) {
        try {
            //1) get the session object
            Properties properties = new Properties();
            properties.put("mail.pop3.host", pop3Host);
            Session emailSession = Session.getDefaultInstance(properties);

            //2) create the POP3 store object and connect with the pop server
            POP3Store emailStore = (POP3Store) emailSession.getStore(storeType);
            emailStore.connect(user, password);

            //3) create the folder object and open it
            Folder emailFolder = emailStore.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            //4) retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                System.out.println("---------------------------------");
                System.out.println("Email Number " + (i + 1));
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                System.out.println("Text: " + message.getContent().toString());
            }

            //5) close the store and folder objects
            emailFolder.close(false);
            emailStore.close();

        } catch (NoSuchProviderException e) {e.printStackTrace();}
        catch (MessagingException e) {e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}
    }
    public static ArrayList<Email> checkUnreadEmailBySender(String pop3Host, String storeType,
                                                       final String user, final String password, String fromAddress) {

        try {
            //1) get the session object
            //Properties properties = new Properties();
            //properties.put("mail.pop3.host", pop3Host);
            //Session emailSession = Session.getDefaultInstance(properties);

            //2) create the POP3 store object and connect with the pop server
            //POP3Store emailStore = (POP3Store) emailSession.getStore(storeType);
            //emailStore.connect(user, password);

            //3) create the folder object and open it
            //Folder emailFolder = emailStore.getFolder("INBOX");
            //emailFolder.open(Folder.READ_ONLY);

            Properties properties = new Properties();
            //properties.setProperty("mail.imap.ssl.enable", "true");
            //Session emailSession = Session.getDefaultInstance(properties);
            //Store emailStore =emailSession.getStore(storeType);
            //emailStore.connect(pop3Host, user, password);

            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.imaps.port", "993");
            properties.put("mail.imaps.starttls.enable", "true");

            //properties.put("mail.smtp.host", "smtp.gmail.com");
            //properties.put("mail.smtp.port", "587");
            //properties.put("mail.smtp.auth", "true");
            //properties.put("mail.smtp.starttls.enable", "true"); //TLS

            Session session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user, password);
                        }
                    });
            Session emailSession = Session.getDefaultInstance(properties);
            Store emailStore =emailSession.getStore("imaps");
            emailStore.connect(pop3Host, user, password);
            Folder emailFolder = emailStore.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            //TODO: Change receiver email
            FromTerm fromTerm = new FromTerm(new InternetAddress(fromAddress));
            SearchTerm searchTerm = new AndTerm(unseenFlagTerm, fromTerm);
            //4) retrieve the messages from the folder in an array and print it
            //Message[] messages = emailFolder.getMessages();
            Message[] messages = emailFolder.search(searchTerm);
            Log.i("Message Count", messages.length+"");
            int msgUnread = emailFolder.getUnreadMessageCount();
            Log.i("Messages Length",messages.length+"");
            //Log.i("Email Status",emails+"");
            //Email emails[] = new Email[messages.length];
            //Log.i("Email Status After",emails+"");
            //Log.i("Email Length",emails.length+"");
            //Log.i("Message Count", msgUnread+"");
            //Log.i("Email Global Status",getEmails()+"");
            //Log.i("Email Local Status",emails+"");
            //Email email = new Email();
            emails = new ArrayList<>();
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                message.getMessageNumber();
                System.out.println("---------------------------------");
                System.out.println("Email Number " + (i + 1));
                System.out.println("Subject: " + message.getSubject());
                Log.i("Email Length",i+"");
                Email email = new Email();
                //emails[i].setSubject(message.getSubject());
                email.setSubject(message.getSubject());
                //email.setSubject(message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                System.out.println("Text: " + message.getContent().toString());
                //emails[i].setContents(message.getContent().toString());
                email.setContents(message.getContent().toString());
                email.setMessageNumber(message.getMessageNumber());
                emails.add(email);
            }


            //5) close the store and folder objects
            emailFolder.close(false);
            emailStore.close();

        } catch (NoSuchProviderException e) {e.printStackTrace();}
        catch (MessagingException e) {e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}
        //TODO: Change email
        return emails;
    }

    public static void main(String[] args) {

        /*String host = "mail.javatpoint.com";//change accordingly
        String mailStoreType = "pop3";
        String username= "sonoojaiswal@javatpoint.com";
        String password= "xxxxx";//change accordingly*/

        //checkEmail(host, mailStoreType, username, password);

        String host = "imap.gmail.com";//change accordingly
        String mailStoreType = "imap";
        String username= "mightythor.707@gmail.com";
        String password= "Mightythor@1";//change accordingly

        checkUnreadEmailBySender(host, mailStoreType, username, password, "raghuchandan1@gmail.com");
    }
}
