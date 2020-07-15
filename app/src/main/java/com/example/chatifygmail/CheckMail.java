package com.example.chatifygmail;

import android.util.Log;

import com.example.chatifygmail.data.Email;
import com.sun.mail.pop3.POP3Store;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;

public class CheckMail {

    private static ArrayList<Email> emails;
    private static String fromAddress;

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
            properties.put("mail_layout.pop3.host", pop3Host);
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

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Email> checkUnreadEmailBySender(String pop3Host, String storeType,
                                                            final String user, final String password, String fromAddress) {
        CheckMail.fromAddress = fromAddress;
        try {
            //1) get the session object
            //Properties properties = new Properties();
            //properties.put("mail_layout.pop3.host", pop3Host);
            //Session emailSession = Session.getDefaultInstance(properties);

            //2) create the POP3 store object and connect with the pop server
            //POP3Store emailStore = (POP3Store) emailSession.getStore(storeType);
            //emailStore.connect(user, password);

            //3) create the folder object and open it
            //Folder emailFolder = emailStore.getFolder("INBOX");
            //emailFolder.open(Folder.READ_ONLY);

            Properties properties = new Properties();
            //properties.setProperty("mail_layout.imap.ssl.enable", "true");
            //Session emailSession = Session.getDefaultInstance(properties);
            //Store emailStore =emailSession.getStore(storeType);
            //emailStore.connect(pop3Host, user, password);

            properties.put("mail_layout.store.protocol", "imaps");
            properties.put("mail_layout.imaps.port", "993");
            properties.put("mail_layout.imaps.starttls.enable", "true");

            //properties.put("mail_layout.smtp.host", "smtp.gmail.com");
            //properties.put("mail_layout.smtp.port", "587");
            //properties.put("mail_layout.smtp.auth", "true");
            //properties.put("mail_layout.smtp.starttls.enable", "true"); //TLS

            Session session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user, password);
                        }
                    });
            Session emailSession = Session.getDefaultInstance(properties);
            Store emailStore = emailSession.getStore("imaps");
            emailStore.connect(pop3Host, user, password);
            Folder emailFolder = emailStore.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);
            //emailFolder.open(Folder.READ_ONLY);
            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            // Change receiver email
            FromTerm fromTerm = new FromTerm(new InternetAddress(fromAddress));
            SearchTerm searchTerm = new AndTerm(unseenFlagTerm, fromTerm);
            //4) retrieve the messages from the folder in an array and print it
            //Message[] messages = emailFolder.getMessages();
            Message[] messages = emailFolder.search(searchTerm);
            Log.i("Message Count", messages.length + "");
            int msgUnread = emailFolder.getUnreadMessageCount();
            Log.i("Messages Length", messages.length + "");
            //Log.i("Email Status",emails+"");
            //Email emails[] = new Email[messages.length];
            //Log.i("Email Status After",emails+"");
            //Log.i("Email Length",emails.length+"");
            //Log.i("Message Count", msgUnread+"");
            //Log.i("Email Global Status",getEmails()+"");
            //Log.i("Email Local Status",emails+"");
            //Email email = new Email();
            /*emailFolder.close(false);
            emailStore.close();*/
            emails = new ArrayList<>();
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                //message.getMessageNumber();
                System.out.println("---------------------------------");
                System.out.println("Email Number " + (i + 1));
                System.out.println("Subject: " + message.getSubject());
                Log.i("Email Length", i + "");
                Email email = new Email();
                //emails[i].setSubject(message.getSubject());
                email.setSubject(message.getSubject());
                //email.setSubject(message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                System.out.println("Text: " + message.getContent().toString());
                //emails[i].setContents(message.getContent().toString());
                //email.setContents(message.getContent().toString());
                email.setContents(writePart(message));
                message.setFlag(Flags.Flag.SEEN, false);
                email.setMessageNumber(message.getMessageNumber());
                emails.add(email);
            }


            //5) close the store and folder objects
            emailFolder.close(false);
            emailStore.close();
            Log.i("Messages Length After", messages.length + "");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Change email
        return emails;
    }

    public static void main(String[] args) {

        /*String host = "mail_layout.javatpoint.com";//change accordingly
        String mailStoreType = "pop3";
        String username= "sonoojaiswal@javatpoint.com";
        String password= "xxxxx";//change accordingly*/

        //checkEmail(host, mailStoreType, username, password);

        String host = "imap.gmail.com";//change accordingly
        String mailStoreType = "imap";
        String username = "mightythor.707@gmail.com";
        String password = "Mightythor@1";//change accordingly

        checkUnreadEmailBySender(host, mailStoreType, username, password, "raghuchandan1@gmail.com");
    }

    public static String writePart(Part p) throws Exception {
        System.out.println("----------------------------");
        System.out.println("CONTENT-TYPE: " + p.getContentType());

        //check if the content is plain text
        if (p.isMimeType("text/plain")) {
            System.out.println("This is plain text");
            System.out.println("---------------------------");
            System.out.println((String) p.getContent());
            if (fromAddress.contains("gmail.com"))
                return "";
            else
                return ((String) p.getContent());
        }
        //check if the content has attachment
        else if (p.isMimeType("multipart/*")) {
            System.out.println("This is a Multipart");
            System.out.println("---------------------------");
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            Log.i("MIMECount", count + "");
            String content = "";
            for (int i = 0; i < count; i++)
                content += writePart(mp.getBodyPart(i));
            return content;
        }
        //check if the content is a nested message
        else if (p.isMimeType("message/rfc822")) {
            System.out.println("This is a Nested Message");
            System.out.println("---------------------------");
            String content = "";
            content += writePart((Part) p.getContent());
            return content;
        }
        //check if the content is an inline image
        else if (p.isMimeType("image/jpeg")) {
            System.out.println("--------> image/jpeg");
            Object o = p.getContent();

            /*InputStream x = (InputStream) o;
            // Construct the required byte array
            System.out.println("x.length = " + x.available());
            while ((i = (int) ((InputStream) x).available()) > 0) {
                int result = (int) (((InputStream) x).read(bArray));
                if (result == -1)
                    int i = 0;
                byte[] bArray = new byte[x.available()];

                break;
            }
            FileOutputStream f2 = new FileOutputStream("/tmp/image.jpg");
            f2.write(bArray);*/
            return "<--JPEG Image-->";
        } else if (p.getContentType().contains("image/")) {
            System.out.println("content type" + p.getContentType());
            /*File f = new File("image" + new Date().getTime() + ".jpg");
            DataOutputStream output = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(f)));
            com.sun.mail.util.BASE64DecoderStream test =
                    (com.sun.mail.util.BASE64DecoderStream) p
                            .getContent();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = test.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }*/
            return "<--Image-->";
        } else {
            Object o = p.getContent();
            if (o instanceof String) {
                System.out.println("This is a string");
                System.out.println("---------------------------");
                System.out.println((String) o);
                return ((String) o);
            } else if (o instanceof InputStream) {
                System.out.println("This is just an input stream");
                System.out.println("---------------------------");
                InputStream is = (InputStream) o;
                is = (InputStream) o;
                int c;
                String content = "";
                while ((c = is.read()) != -1) {
                    System.out.write(c);
                    content += c;
                }
                return content;
            } else {
                System.out.println("This is an unknown type");
                System.out.println("---------------------------");
                System.out.println(o.toString());
                return (o.toString());
            }
        }

    }

    public static int sendMail(final String user, final String password, String toAddress, String subject, String contents) {
        // Recipient's email ID needs to be mentioned.
        String to = toAddress;

        // Sender's email ID needs to be mentioned
        String from = user;

        // Get system properties
        // Properties properties = System.getProperties();

        // Setup mail server
        //properties.put("mail_layout.smtp.host", "smtp.gmail.com");
        //properties.put("mail_layout.smtp.port", "587");
        //properties.put("mail_layout.smtp.auth", "true");
        //properties.put("mail_layout.smtp.starttls.enable", "true");*/ //TLS
        /*Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");*/

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });


        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setText(contents);

            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        }
        catch(AuthenticationFailedException afe){
            return -1;
        }
        catch (MessagingException mex) {
            mex.printStackTrace();
        }
        return 0;
    }

    public static int validateMail(String user, String password) {
        try {
            Properties properties = new Properties();
            properties.put("mail_layout.store.protocol", "imaps");
            properties.put("mail_layout.imaps.port", "993");
            properties.put("mail_layout.imaps.starttls.enable", "true");
            Session session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user, password);
                        }
                    });
            Session emailSession = Session.getDefaultInstance(properties);
            Store emailStore = emailSession.getStore("imaps");
            emailStore.connect("imap.gmail.com", user, password);
            Folder emailFolder = emailStore.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);
            Message[] result = emailFolder.getMessages();
            emailFolder.close(false);
            emailStore.close();
            return 0;
        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
            return 1;
        } catch (MessagingException e) {
            e.printStackTrace();
            return 2;
        }
    }
}
