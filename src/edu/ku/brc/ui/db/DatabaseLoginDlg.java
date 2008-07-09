/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.ui.db;

import java.awt.Frame;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.UIRegistry;

/**
 * This dialog simply wraps the DatabaseLoginPanel
 *
 *  NOTE: This dialog can only be closed for two reasons: 1) A valid login, 2) It was cancelled by the user.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class DatabaseLoginDlg extends JDialog implements DatabaseLoginListener
{
    private static final Logger          log            = Logger.getLogger(DatabaseLoginDlg.class);
     protected DatabaseLoginPanel    dbPanel;
     protected DatabaseLoginListener listener;
     protected boolean               doAutoLogin = false;
     protected boolean               doAutoClose = true;
     protected JPanel                glassPane   = new JPanel();

    /**
     * Constructor that has the form created from the view system.
     * @param frame the parent frame
     * @param listener the listener usually the parent like the Dialog
     * @param iconName name of icon to use
     */
    public DatabaseLoginDlg(final Frame frame, 
                            final DatabaseLoginListener listener,
                            final String iconName)
    {
        super(frame);
        
        this.listener = listener;

        //setTitle(getResourceString("LOGINTITLE")); //$NON-NLS-1$

        dbPanel = new DatabaseLoginPanel(this, true, iconName);
        setContentPane(dbPanel);

        setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setModal(true);

        getRootPane().setDefaultButton(dbPanel.getLoginBtn());

        pack();
    }
    
//    /**
//     * Constructor that has the form created from the view system.
//     * @param frame the parent frame
//     * @param listener the listener usually the parent like the Dialog
//     */
//    public DatabaseLoginDlg(final Frame frame, final DatabaseLoginListener listener)
//    {
//        super(frame);
//        
//        this.listener = listener;
//
//        setTitle(getResourceString("logintitle"));
//
//        dbPanel = new DatabaseLoginPanel(this, true);
//        setContentPane(dbPanel);
//
//        setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
//        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//        this.setModal(true);
//
//        getRootPane().setDefaultButton(dbPanel.getLoginBtn());
//
//        pack();
//    }

    /**
     * Return whether dialog was cancelled
     * @return whether dialog was cancelled
     */
    public boolean isCancelled()
    {
        return dbPanel.isCancelled();
    }

    /**
     * Returns true if doing auto login
     * @return true if doing auto login
     */
    public boolean doingAutoLogin()
    {
        return dbPanel.doingAutoLogin();
    }

    /**
     * Sets to auto login
     * @param doAutoLogin true - do auto login
     */
    public void setDoAutoLogin(final boolean doAutoLogin)
    {
        this.doAutoLogin = doAutoLogin;
    }

    /**
     * Sets whether the dialog should automatically close when it logs in successfully.
     * @param doAutoClose the value
     */
    public void setDoAutoClose(boolean doAutoClose)
    {
        this.doAutoClose = doAutoClose;
        if (dbPanel != null)
        {
            dbPanel.setAutoClose(doAutoClose);
        }
    }
    
    /**
     * Returns the DatabaseLoginPanel.
     * @return the DatabaseLoginPanel
     */
    public DatabaseLoginPanel getDatabaseLoginPanel()
    {
        return dbPanel;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setVisible(boolean)
     */
    @Override
    public void setVisible(final boolean show)
    {
        if (show && doAutoLogin)
        {
            dbPanel.doLogin();
        }
        super.setVisible(show);
    }

    //---------------------------------------------------------
    // DatabaseLoginListener Interface
    //---------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#aboutToLoginIn()
     */
    public void aboutToLoginIn()
    {
        glassPane.setSize(this.getSize());
        this.setGlassPane(glassPane);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#loggedIn(java.awt.Window, java.lang.String, java.lang.String)
     */
    public void loggedIn(final Window window, final String databaseName, final String userName)
    {
        log.debug("loggedIn"); //$NON-NLS-1$
        setVisible(false);
        dispose();
        if (listener != null)
        {
            listener.loggedIn(window, databaseName, userName);
        }
        //this.setGlassPane(null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#cancelled()
     */
    public void cancelled()
    {
        setVisible(false);
        dispose();
        if (listener != null)
        {
            listener.cancelled();
        }
        //this.setGlassPane(null);
    }
//
//    /**
//     * @return the jaasContext
//     */
//    public JaasContext getJaasContext()
//    {
//        return jaasContext;
//    }
//
//    /**
//     * @param jaasContext the jaasContext to set
//     */
//    public void setJaasContext(JaasContext jaasEmbedder)
//    {
//        this.jaasContext = jaasEmbedder;
//    }



}
