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

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.mail.smtp.SMTPTransport;

import edu.ku.brc.specify.ui.UICacheManager;

/**
 * Sends an email with optional attachment
 *
 * @author Rod Spears <rods@ku.edu>
 */

public class EMailHelper
{
    private static Log log  = LogFactory.getLog(EMailHelper.class);
    
    public enum AccountType {Unknown, POP3, IMAP};
    
    public static final String POP3       = "POP3";
    public static final String IMAP       = "IMAP";
    public static final String PLAIN_TEXT = "text/plain";
    public static final String HTML_TEXT  = "text/html";
    
    protected static final EMailHelper instance     = new EMailHelper();
    
    // Data Members
    protected String            lastErrorMsg = "";
    protected boolean           isDebugging  = true;
    protected List<MailBoxInfo> mailBoxCache = new ArrayList<MailBoxInfo>();
    
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
                                  String mimeType,
                                  File   aFileAttachment)
    {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", aHost);
        props.put( "mail.smtp.auth", "true");
        
        Session session = Session.getInstance(props, null);
        
        session.setDebug(instance.isDebugging);
        if (instance.isDebugging)
        {
            log.debug("Host:     " + aHost);
            log.debug("UserName: " + aUserName);
            log.debug("Password: " + aPassword);
            log.debug("From:     " + aFrom);
            log.debug("To:       " + aTo);
            log.debug("Subject:  " + aSubject);
        }
        
        
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

            // create the second message part
            if (aFileAttachment != null)
            {
                // create and fill the first message part
                MimeBodyPart mbp1 = new MimeBodyPart();
                mbp1.setText(aBodyText);
                
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
                msg.setContent(mp, mimeType);
    
            } else
            {
                // add the Multipart to the message
                msg.setContent(aBodyText, mimeType);
            }
            
 
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

            
        } catch (MessagingException mex) 
        {
            instance.lastErrorMsg = mex.toString();
            
            //mex.printStackTrace();
            Exception ex = null;
            if ((ex = mex.getNextException()) != null) {
              ex.printStackTrace();
              instance.lastErrorMsg = instance.lastErrorMsg + ", " + ex.toString();
            }
            return false;
        }
        return true;
    }
    
    /**
     * Returns true if the account type string is "POP3"
     * @param acctType the account type string
     * @return Returns true if the account type string is "POP3"
     */
    public static AccountType getAccountType(final String acctType)
    {
        if (acctType != null)
        {
            try
            {
                return AccountType.valueOf(acctType);
            } catch (Exception ex)
            {
                return AccountType.Unknown;
            }
        }
        return AccountType.Unknown;
    }
    
    /**
     * Returns true if the account type string is "POP3"
     * @param acctType the account type string
     * @return Returns true if the account type string is "POP3"
     */
    public static boolean isIMAP(final String acctType)
    {
        return acctType != null && acctType.equals(IMAP);
    }
    
    /**
     * Returns whether the string has at least one character
     * @param str string to be checked
     * @return Returns whether the string has at least one character
     */
    protected static boolean isStrValid(final String str)
    {
        return str != null && str.length() > 0;
    }
    
    /**
     * Returns whether the email settings have been filled in (but this doesn't indicate whether they will actually work)
     * @param usernameStr username
     * @param passwordStr password
     * @param emailStr email
     * @param smtpStr smtp
     * @param serverNameStr server name
     * @param acctTypeStr account type (POP3, IMAP)
     * @param localMailBoxStr URL location of mailbox (can be a file URL)
     * @return Returns whether the email settings have been filled in (but this doesn't indicate whether they will actually work)
     */
    public static boolean hasEMailSettings(final String usernameStr,
                                           final String passwordStr,
                                           final String emailStr,
                                           final String smtpStr,
                                           final String serverNameStr,
                                           final String acctTypeStr,
                                           final String localMailBoxStr)
    {
        EMailHelper.AccountType acctType = EMailHelper.getAccountType(acctTypeStr);
        
        return acctType != AccountType.Unknown &&
               isStrValid(usernameStr) && isStrValid(passwordStr) && isStrValid(emailStr) && 
               isStrValid(smtpStr) && isStrValid(serverNameStr) && 
               (acctType == AccountType.IMAP || isStrValid(localMailBoxStr));
    }
    
    /**
     * Returns whether the email settings have been filled in (but this doesn't indicate whether they will actually work)
     * @return Returns whether the email settings have been filled in (but this doesn't indicate whether they will actually work)
     */
    public static boolean hasEMailSettings()
    {
        Preferences prefNode = UICacheManager.getAppPrefs().node("settings/email");
        if (prefNode == null)
        {
            throw new RuntimeException("Could find pref for email!");
        }        
        
        String usernameStr     = prefNode.get("username", null);
        String passwordStr     = Encryption.decrypt(prefNode.get("password", null));
        String emailStr        = prefNode.get("email", null);
        String smtpStr         = prefNode.get("smtp", null);
        String serverNameStr   = prefNode.get("servername", null);
        String acctTypeStr     = prefNode.get("accounttype", null);
        String localMailBoxStr = prefNode.get("localmailbox", null);
        
        return hasEMailSettings(usernameStr, passwordStr, emailStr, smtpStr, serverNameStr, acctTypeStr, localMailBoxStr);
    }
    
    /**
     * Retrieves all the message from the INBOX
     * @param store the store to retrieve them from
     * @param msgList the list to add them to
     * @return true if successful, false if their was an exception
     */
    public static boolean getMessagesFromInbox(final Store store, 
                                               final java.util.List<javax.mail.Message> msgList)
    {
        try
        {
            Folder inbox = store.getDefaultFolder().getFolder("Inbox");
            inbox.open(Folder.READ_ONLY);
    
            javax.mail.Message[] messages = inbox.getMessages();
            
            Collections.addAll(msgList, messages);
            
            MailBoxInfo mbx = instance.new MailBoxInfo(store, inbox);
            
            instance.mailBoxCache.add(mbx);
            
            return true;
            
        } catch (MessagingException mex)
        {
            instance.lastErrorMsg = mex.toString();
        }
        return false;
    }
    
    /**
     * Retrieves a list of all the available messages. For POP3 it checks the local mailbox and downloads any on the server.
     *  For IMAP it returns any in the INBOX
     * @param msgList the list to be filled
     * @return true if not erros, false if an error occurred.
     */
    public static boolean getAvailableMsgs(java.util.List<javax.mail.Message> msgList)
    {
        boolean status = false; // assume it will fail
        
        msgList.clear();
        
        Preferences userPrefNode = Preferences.userRoot();
        Preferences prefNode     = userPrefNode.node("settings/email");
        if (prefNode == null)
        {
            throw new RuntimeException("Could find pref for email!");
        }        
        
        try
        {
            String usernameStr     = prefNode.get("username", null);
            String passwordStr     = Encryption.decrypt(prefNode.get("password", null));
            String emailStr        = prefNode.get("email", null);
            String smtpStr         = prefNode.get("smtp", null);
            String serverNameStr   = prefNode.get("servername", null);
            String acctTypeStr     = prefNode.get("accounttype", null);
            String localMailBoxStr = prefNode.get("localmailbox", null);
            
            EMailHelper.AccountType acctType = EMailHelper.getAccountType(acctTypeStr);
            
            if (!hasEMailSettings(usernameStr, passwordStr, emailStr, smtpStr, serverNameStr, acctTypeStr, localMailBoxStr))
            {
                JOptionPane.showMessageDialog(UICacheManager.getMostRecentFrame(), getResourceString("emailsetnotvalid"));
            }
            
            // Open Local Box if POP  
            if (acctTypeStr.equals(getResourceString("pop3")))
            {
                try
                {
                    Properties props = new Properties();
                    Session session = Session.getDefaultInstance(props);
                    
                    Store store = session.getStore(new URLName("mstor:"+localMailBoxStr));
                    store.connect();
                    status = getMessagesFromInbox(store, msgList); // closes everything
                    
                } catch (Exception ex)
                {
                   
                    instance.lastErrorMsg = ex.toString();
                    ex.printStackTrace();
                    status = false;
                }
            } else
            {
                throw new RuntimeException("Unknown Account Type ["+acctTypeStr+"] must be POP3 or IMAP"); // XXX FIXME
            }
            
            // Try to download message from pop account
            try
            {
                Properties props   = System.getProperties();
                Session    session = Session.getInstance(props, null);
                
                if (acctType == AccountType.POP3)
                {
                    Store store = session.getStore("pop3");
                    store.connect(serverNameStr, usernameStr, passwordStr);
                    status = getMessagesFromInbox(store, msgList); // closes everything
                    
                } else if (acctType == AccountType.IMAP)
                {
                    Store store = session.getStore("imap");
                    store.connect(serverNameStr, usernameStr, passwordStr);
                    status = getMessagesFromInbox(store, msgList); // closes everything
                    
                } else
                {
                    String msgStr = "Unknown Account Type ["+acctTypeStr+"] must be POP3 or IMAP"; // XXX I18N
                    instance.lastErrorMsg  = msgStr;
                    throw new RuntimeException(msgStr); // XXX FIXME
                }               
            } catch (Exception ex)
            {
                instance.lastErrorMsg  = ex.toString();
                status = false;
            }
            
        } catch (Exception ex)
        {
            instance.lastErrorMsg  = ex.toString();
            status = false;
        }
        return status;
    }
    
    /**
     * Returns a the last error message string it typ[ically comes from an exception that we catch
     * @return Returns a the last error message string it typ[ically comes from an exception that we catch
     */
    public static String getLastErrorMsg()
    {
        return instance.lastErrorMsg;
    }

    public static boolean isDebugging()
    {
        return instance.isDebugging;
    }

    public static void setDebugging(boolean isDebugging)
    {
        instance.isDebugging = isDebugging;
    }
    
    /**
     * Close all Message Store and their folders
     */
    public static void closeAllMailBoxes()
    {
        for (MailBoxInfo mbx : instance.mailBoxCache)
        {
            mbx.close();
        }
        instance.mailBoxCache.clear();
    }
    
    //---------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------
    class MailBoxInfo 
    {
        protected Store  store;
        protected Folder folder;
        
        public MailBoxInfo(final Store store, final Folder folder)
        {
            this.store  = store;
            this.folder = folder;
        }

        public Folder getFolder()
        {
            return folder;
        }

        public Store getStore()
        {
            return store;
        }
        
        public void close()
        {
            try
            {
                folder.close(false);
                store.close();
            } catch (MessagingException mex)
            {
                instance.lastErrorMsg = mex.toString();
            }
        }
    }

}
