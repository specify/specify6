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

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JFrame;

import edu.ku.brc.ui.UICacheManager;

/**
 * This dialog simply wraps the DatabaseLoginPanel
 * 
 *  NOTE: This dialog can only be closed for two reasons: 1) A valid login, 2) It was cancelled by the user.
 * 
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public class DatabaseLoginDlg extends JDialog implements DatabaseLoginListener
{
     protected DatabaseLoginPanel    dbPanel;
     protected DatabaseLoginListener listener;
     
    /**
     * Constructor that has the form created from the view system
     */
    public DatabaseLoginDlg(final DatabaseLoginListener listener)
    {
        this.listener = listener;
        
        setTitle(getResourceString("logintitle"));
        
        dbPanel = new DatabaseLoginPanel(this, true);
        setContentPane(dbPanel);
        
        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setModal(true);
        this.setAlwaysOnTop(true);
        
        getRootPane().setDefaultButton(dbPanel.getLoginBtn());
        
        pack();
    }
    
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
    
    //---------------------------------------------------------
    // DatabaseLoginListener Interface
    //---------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#loggedIn()
     */
    public void loggedIn()
    {
        setVisible(false);
        dispose();
        if (listener != null)
        {
            listener.loggedIn();
        }
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
    }
    

    
}
