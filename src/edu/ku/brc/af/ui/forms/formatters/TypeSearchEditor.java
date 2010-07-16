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
package edu.ku.brc.af.ui.forms.formatters;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.HeadlessException;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jun 1, 2010
 *
 */
public class TypeSearchEditor extends CustomDialog
{

    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public TypeSearchEditor(Dialog dialog, String title, boolean isModal, int whichBtns,
            Component contentPanel) throws HeadlessException
    {
        super(dialog, title, isModal, whichBtns, contentPanel);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        PanelBuilder pb = new PanelBuilder(new FormLayout("", ""));
        
        
        
        contentPanel = pb.getPanel();
        
        super.createUI();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        super.okButtonPressed();
    }

    
}
