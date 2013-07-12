/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Apr 13, 2010
 *
 */
public class PromptDlg extends CustomDialog
{
    private JTextField textField;
    private String     msg;

    /**
     * @param dialog
     * @param title
     * @param msg
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public PromptDlg(Dialog dialog, String title, String msg, boolean isModal, int whichBtns) throws HeadlessException
    {
        super(dialog, title, isModal, whichBtns, null);
        this.msg = msg;
    }

    /**
     * @param frame
     * @param title
     * @param msg
     * @param isModal
     * @param contentPanel
     * @throws HeadlessException
     */
    public PromptDlg(Frame frame, String title, String msg, boolean isModal)
            throws HeadlessException
    {
        super(frame, title, isModal, null);
        this.msg = msg;
    }

    /**
     * @param frame
     * @param title
     * @param msg
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @param defaultBtn
     * @throws HeadlessException
     */
    public PromptDlg(Frame frame, String title, String msg, boolean isModal, int whichBtns, int defaultBtn) throws HeadlessException
    {
        super(frame, title, isModal, whichBtns, null, defaultBtn);
        this.msg = msg;
    }

    /**
     * @param frame
     * @param title
     * @param msg
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public PromptDlg(Frame frame, String title, String msg, boolean isModal, int whichBtns) throws HeadlessException
    {
        super(frame, title, isModal, whichBtns, null);
        this.msg = msg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        textField = UIHelper.createTextField();

        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p,4px,p"));
        pb.add(UIHelper.createLabel(msg), cc.xy(1, 1));
        pb.add(textField, cc.xy(1, 3));
        
        pb.setDefaultDialogBorder();
        
        mainPanel.add(pb.getPanel(), BorderLayout.CENTER);
    }

    /**
     * @return the textField
     */
    public JTextField getTextField()
    {
        return textField;
    }


}
