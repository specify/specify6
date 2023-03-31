/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.ViewBasedDialogFactoryIFace.FRAME_TYPE;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.IconViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 22, 2007
 *
 */
public class CollectorActionListener implements ActionListener
{
    protected static final Logger log = Logger.getLogger(CollectorActionListener.class);
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (!(e instanceof IconViewObj.IconViewActionEvent))
        {
            throw new RuntimeException("Event MUST be instance of IconViewObj.IconViewActionEvent");
        }
        
        Object source = e.getSource();
        if (!(source instanceof Collector))
        {
            throw new IllegalArgumentException("Passed object must be a Collector");
        }
        Collector  collector   = (Collector)source;
        IconViewObj iconViewObj = ((IconViewObj.IconViewActionEvent)e).getIconViewObj();
        
        Agent agent = collector.getAgent();

        // if we use agent.getClass().getName() it might fail if the agent object is a Hibernate-generated proxy, which is common
        String      classname   = Agent.class.getName();
        DBTableInfo setTI       = DBTableIdMgr.getInstance().getByClassName(classname);
        String      defFormName = setTI.getEditObjDialog();
        if (defFormName == null)
        {
            log.error("Cannot find default form for " + collector.getClass().getSimpleName() + " records");
            return;
        }

        int     options    = iconViewObj.getViewOptions();
        boolean isEditting = iconViewObj.getAltView().getMode() == AltViewIFace.CreationMode.EDIT;
        String  title      = (MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT) && isEditting) ? 
                                getResourceString("EDIT") : collector.getIdentityTitle();
                                
        if (!isEditting)
        {
            options ^= MultiView.VIEW_SWITCHER;
            options ^= MultiView.RESULTSET_CONTROLLER;
        }
        ViewBasedDisplayIFace dialog = UIRegistry.getViewbasedFactory().createDisplay(UIHelper.getWindow(iconViewObj.getUIComponent()),
                                                                    defFormName,
                                                                    title,
                                                                    isEditting ? getResourceString("OK") : getResourceString("CLOSE"),
                                                                    isEditting,
                                                                    options,
                                                                    null,
                                                                    FRAME_TYPE.DIALOG);
        if (dialog != null)
        {
            dialog.setData(agent);
            dialog.createUI();
            dialog.getOkBtn().setEnabled(!isEditting);
            dialog.showDisplay(true);
            dialog.dispose();
        }
    }

}
