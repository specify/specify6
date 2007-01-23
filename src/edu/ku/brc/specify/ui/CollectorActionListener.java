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
/**
 * 
 */
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableIdMgr.TableInfo;
import edu.ku.brc.specify.datamodel.Collectors;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.ViewBasedDialogFactoryIFace.FRAME_TYPE;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.forms.IconViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.persist.AltView;

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
        if (!(source instanceof Collectors))
        {
            throw new IllegalArgumentException("Passed object must be a Collectors");
        }
        Collectors  collector   = (Collectors)source;
        IconViewObj iconViewObj = ((IconViewObj.IconViewActionEvent)e).getIconViewObj();
        
        TableInfo setTI = DBTableIdMgr.lookupByClassName(collector.getClass().getName());
        String defFormName = setTI.getEditObjDialog();

        int     options    = iconViewObj.getViewOptions();
        boolean isEditting = iconViewObj.getAltView().getMode() == AltView.CreationMode.Edit;
        String  title      = (MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT) && isEditting) ? 
                                getResourceString("Edit") : collector.getIdentityTitle();
                                
        ViewBasedDisplayIFace dialog = UICacheManager.getViewbasedFactory().createDisplay(UIHelper.getFrame(iconViewObj.getUIComponent()),
                                                                    defFormName,
                                                                    title,
                                                                    getResourceString("OK"),
                                                                    isEditting,
                                                                    options,
                                                                    FRAME_TYPE.DIALOG);
        dialog.setData(collector.getAgent());
        dialog.showDisplay(true);
        
    }

}