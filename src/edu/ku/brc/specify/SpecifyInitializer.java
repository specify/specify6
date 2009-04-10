/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify;


/**
 * This class checks the local disk and the user's home directory to see if Specify has been used. 
 * If it could find the "Specify" or ".Specify" directory then is asks the user if they want to 
 * create a new empty database.
 * 
 * XXX I18N - This entire class needs to be Localized!
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Mar 1, 2007
 *
 */
public class SpecifyInitializer
{
    protected boolean           doLoginOnly        = false;
    protected boolean           assumeDerby        = false;

    /**
     * Constructor.
     */
    public SpecifyInitializer(final boolean doLoginOnly, 
                              final boolean assumeDerby)
    {
        this.doLoginOnly = doLoginOnly;
        this.assumeDerby = assumeDerby;

    }

    /**
     * Looks for a Specify directory and if not, then gives the user an opportunity
     * to create a new database. If it finds a directory then it display the Specify login window.
     * 
     * @param specify a Specify application object
     * @return
     */
    public boolean setup(final Specify specify)
    {
        /*AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        
        if (true)//StringUtils.isEmpty(localPrefs.get("login.dbdriver_selected", null)))
        {
            final SpecifyDBSetupWizard specifyInitFrame = new SpecifyDBSetupWizard(SpecifyDBSetupWizard.WizardType.Full);
            specifyInitFrame.setTitle("SpecifyDBInit - "); //$NON-NLS-1$
            // I can't believe I have to do the following....
            UIHelper.centerWindow(specifyInitFrame);
            specifyInitFrame.pack();
            Dimension size = specifyInitFrame.getSize();
            size.height += 10;
            specifyInitFrame.setSize(size);
            UIHelper.centerAndShow(specifyInitFrame);
            
        } else
        {
            HibernateUtil.shutdown();
            specify.startUp();
        }*/

        return true;
    }
}
