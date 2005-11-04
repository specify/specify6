package edu.ku.brc.specify.helpers;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.io.*;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Date;

import javax.mail.*;
import javax.mail.internet.*;

import com.sun.mail.smtp.*;

/**
 * Sends an email with optional attachment
 *
 * @author Rod Spears <rods@ku.edu>
 */

public class EMailHelper
{

    /**
     * Default Constructor
     *
     */
    public EMailHelper()
    {
        super();
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Send an email
     * @param aHost host of SMTP server
     * @param aUserName username of email account
     * @param aPassword password of email account
     * @param aFrom the email address of who the email is coming from typically this is the same as the user's email
     * @param aTo the email addr of who this is going to
     * @param aSubject the Textual subject line of the email
     * @param aBodyText the body text of the email (plain text???)
     * @param aFileAttachment and optional file to be attached to the email
     * @return true if the msg was sent, false if not
     */
    public static boolean sendMsg(String aHost,
                                  String aUserName, 
                                  String aPassword, 
                                  String aFrom, 
                                  String aTo, 
                                  String aSubject, 
                                  String aBodyText,
                                  File   aFileAttachment)
    {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", aHost);
        props.put( "mail.smtp.auth", "true");
        
        Session session = Session.getInstance(props, null);
        
        session.setDebug(true);
        
        try 
        {
            // create a message
            MimeMessage msg = new MimeMessage(session);
            
            msg.setFrom(new InternetAddress(aFrom));
            if (aTo.indexOf(",") > -1)
            {
                StringTokenizer st = new StringTokenizer(aTo, ",");
                InternetAddress[] address = new InternetAddress[st.countTokens()];
                int i = 0;
                while (st.hasMoreTokens())
                {
                    String toStr = st.nextToken().trim();
                    address[i++] = new InternetAddress(toStr);
                }
                msg.setRecipients(Message.RecipientType.TO, address);                
            } else
            {
                InternetAddress[] address = {new InternetAddress(aTo)};
                msg.setRecipients(Message.RecipientType.TO, address);                
            }
            msg.setSubject(aSubject);

            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(aBodyText);

            // create the second message part
            MimeBodyPart mbp2 = new MimeBodyPart();

                // attach the file to the message
            FileDataSource fds = new FileDataSource(aFileAttachment);
            mbp2.setDataHandler(new DataHandler(fds));
            mbp2.setFileName(fds.getName());

            // create the Multipart and add its parts to it
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);

            // add the Multipart to the message
            msg.setContent(mp);

            // set the Date: header
            msg.setSentDate(new Date());
            
            // send the message
            //Transport.send(msg);
            
            SMTPTransport t = (SMTPTransport)session.getTransport("smtp");
            try {
                t.connect(aHost, aUserName, aPassword);
                
                t.sendMessage(msg, msg.getAllRecipients());
                
            } finally {
            //if (verbose)
                System.out.println("Response: " +
                            t.getLastServerResponse());
              t.close();
            }

            
        } catch (MessagingException mex) {
            mex.printStackTrace();
            Exception ex = null;
            if ((ex = mex.getNextException()) != null) {
              ex.printStackTrace();
            }
            return false;
        }
        return true;
        
    }

}
