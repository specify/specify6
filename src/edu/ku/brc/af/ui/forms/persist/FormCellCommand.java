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
package edu.ku.brc.af.ui.forms.persist;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class FormCellCommand extends FormCellSeparator implements Cloneable, FormCellCommandIFace
{
    protected String commandType;
    protected String action;
    
    public FormCellCommand(final String id, final String name, final String label, final String commandType, final String action)
    {
        super(id, name, label, 1);
        
        this.type        = FormCellIFace.CellType.command;
        this.commandType = commandType;
        this.action      = action;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellCommandIFace#getAction()
     */
    public String getAction()
    {
        return action;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellCommandIFace#setAction(java.lang.String)
     */
    public void setAction(String action)
    {
        this.action = action;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellCommandIFace#getCommandType()
     */
    public String getCommandType()
    {
        return commandType;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellCommandIFace#setCommandType(java.lang.String)
     */
    public void setCommandType(String commandType)
    {
        this.commandType = commandType;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSeparator#clone()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellCommandIFace#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        FormCellCommand fcc = (FormCellCommand)super.clone();
        fcc.commandType = commandType;
        fcc.action      = action;
        return fcc;      
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#toXMLAttrs(java.lang.StringBuilder)
     */
    public void toXMLAttrs(StringBuilder sb)
    {
        xmlAttr(sb, "commandtype", commandType);
        xmlAttr(sb, "action", action);
    }
}
