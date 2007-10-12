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
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.DatabaseLoginPanel;

/**
 * This dialog simply wraps the CustomDBConverterPanel
 *
 *  NOTE: This dialog can only be closed for two reasons: 1) A valid login, 2) It was cancelled by the user.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class CustomDBConverterDlg extends JDialog implements CustomDBConverterListener
{
    private static final Logger          log            = Logger.getLogger(CustomDBConverterDlg.class);
     protected CustomDBConverterPanel    dbConverterPanel;
     protected CustomDBConverterListener dbConverterListener;
     protected boolean               doAutoLogin = false;
     protected boolean               doAutoClose = true;
     protected JPanel                glassPane   = new JPanel();

    /**
     * Constructor that has the form created from the view system.
     * @param frame the parent frame
     * @param dbConverterListener the dbConverterListener usually the parent like the Dialog
     */
    public CustomDBConverterDlg(final Frame frame, final CustomDBConverterListener listener)
    {
        super(frame);
        
        this.dbConverterListener = listener;

        setTitle(getResourceString("logintitle"));

        dbConverterPanel = new CustomDBConverterPanel(this, this, true);
        setContentPane(dbConverterPanel);

        setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setModal(true);

        getRootPane().setDefaultButton(dbConverterPanel.getLoginBtn());

        pack();
    }

    /**
     * Return whether dialog was cancelled
     * @return whether dialog was cancelled
     */
    public boolean isCancelled()
    {
        return dbConverterPanel.isCancelled();
    }

    /**
     * Returns true if doing auto login
     * @return true if doing auto login
     */
    public boolean doingAutoLogin()
    {
        return dbConverterPanel.doingAutoLogin();
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
        if (dbConverterPanel != null)
        {
            dbConverterPanel.setAutoClose(doAutoClose);
        }
    }
    
    /**
     * Returns the CustomDBConverterPanel.
     * @return the CustomDBConverterPanel
     */
    public CustomDBConverterPanel getCustomDBConverterPanel()
    {
        return dbConverterPanel;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setVisible(boolean)
     */
    @Override
    public void setVisible(final boolean show)
    {
        if (show && doAutoLogin)
        {
            dbConverterPanel.doLogin();
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
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#loggedIn(java.lang.String)
     */
    public void loggedIn(final String databaseName, final String userName)
    {
        log.debug("loggedIn");
        setVisible(false);
        dispose();
        if (dbConverterListener != null)
        {
            dbConverterListener.loggedIn(databaseName, userName);
        }
        //this.setGlassPane(null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#cancelled()
     */
    public void cancelled()
    {
        log.debug("cancelled");
        setVisible(false);
        dispose();
        if (dbConverterListener != null)
        {
            dbConverterListener.cancelled();
        }
        //this.setGlassPane(null);
    }



}
