/* Filename:    $RCSfile: EMailHelper.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.helpers;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.mail.smtp.SMTPTransport;

/**
 * Sends an email with optional attachment
 *
 * @author Rod Spears <rods@ku.edu>
 */

public class EMailHelper
{
    private static Log log  = LogFactory.getLog(EMailHelper.class);
    
    /**
     * Default Constructor
     *
     */
    public EMailHelper()
    {
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
                
            } finally 
            {
                  
                 log.info("Response: " + t.getLastServerResponse());
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
