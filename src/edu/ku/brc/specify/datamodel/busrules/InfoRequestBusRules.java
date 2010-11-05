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
package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 18, 2010
 *
 */
public class InfoRequestBusRules extends BaseBusRules
{
    public static final String CMDTYPE     = "Interactions";
    protected static final String INFO_REQ_MESSAGE     = "Specify Info Request";

    /**
     * @param dataClasses
     */
    public InfoRequestBusRules()
    {
        super(InfoRequest.class);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        /*if (formViewObj != null)
        {
            if (formViewObj.getMVParent().isTopLevel())
            {
                ResultSetController rsc = formViewObj.getRsController();
                if (rsc != null)
                {
                    rsc.getPanel().setVisible(false);
                    if (rsc.getNewRecBtn() != null) rsc.getNewRecBtn().setVisible(false);
                    if (rsc.getDelRecBtn() != null) rsc.getDelRecBtn().setVisible(false);
                }
            }
        }*/
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#canCreateNewDataObject()
     */
    @Override
    public boolean canCreateNewDataObject()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#createNewObj(boolean, java.lang.Object)
     */
    @Override
    public void createNewObj(boolean doSetIntoAndValidateArg, Object oldDataObj)
    {
        CommandAction cmdAction = new CommandAction(CMDTYPE, INFO_REQ_MESSAGE, viewable);
        CommandDispatcher.dispatch(cmdAction);
    }
}
