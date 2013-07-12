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
package edu.ku.brc.specify.ui.db;

import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextArea;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.DocumentAdaptor;

/**
 * This class is used to enable users to edit a comma separated list of numbers 
 * and that were in error. Then move them to the 'main control' once they are 
 * presumed to be correct.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Nov 2, 2010
 *
 */
public class NumberEditorPanel extends JPanel
{
    protected ChangeListener  changeListner;
    protected JTextArea       mainTextArea;
    protected JTextArea       editTextArea;
    protected JButton         moveBtn;
    protected JButton         clearBtn;
    
    protected boolean         isOK = true;

    /**
     * @param mainTextArea
     * @param changeListner
     * @param titleKey
     */
    public NumberEditorPanel(final JTextArea mainTextArea,
                             final ChangeListener changeListner,
                             final String titleKey)
    {
        super();
        this.mainTextArea  = mainTextArea;
        this.changeListner = changeListner;
        
        createUI(titleKey);
    }
    
    /**
     * @param titleKey
     */
    private void createUI(final String titleKey)
    {
        editTextArea = createTextArea(5, 40);
        moveBtn      = createI18NButton("AFN_MOVE_UP");
        clearBtn     = createI18NButton("AFN_CLEAR");
        
        editTextArea.setLineWrap(true);
        editTextArea.setWrapStyleWord(true);
        
        CellConstraints cc  = new CellConstraints();
        PanelBuilder    vpb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,p,f:p:g,p,f:p:g"));
        vpb.add(moveBtn,  cc.xy(1, 2));
        vpb.add(clearBtn, cc.xy(1, 4));

        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,4px,p", "p,2px,f:p:g"), this);
        
        pb.addSeparator(getResourceString(titleKey), cc.xyw(1, 1, 3));
        pb.add(createScrollPane(editTextArea),       cc.xy(1, 3));
        pb.add(vpb.getPanel(),                       cc.xy(3, 3));
        
        moveBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                moveNumbersUp();
            }
        });
        
        clearBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editTextArea.setText("");
                isOK = true;
                changeListner.stateChanged(null);
            }
        });
        
        editTextArea.getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                updateBtnUI(true);
            }
        });
    }
    
    /**
     * @return the isOK
     */
    public boolean isOK()
    {
        return isOK;
    }

    /**
     * @param text
     */
    public void setNumbers(final List<String> numbers)
    {
        AskForNumbersDlg.buildNumberList(numbers, editTextArea);
        updateBtnUI(false);
        editTextArea.setEnabled(true);
        editTextArea.setEditable(true);
        isOK = numbers == null;
    }
    
    /**
     * 
     */
    private void moveNumbersUp()
    {
        String        txt = mainTextArea.getText().trim();
        StringBuilder sb  = new StringBuilder(txt);
        if (!txt.endsWith(",")) sb.append(", ");
        sb.append(editTextArea.getText().trim());
        mainTextArea.setText(sb.toString());
        editTextArea.setText("");
        updateBtnUI(false);
        isOK = true;
        changeListner.stateChanged(null);
    }
    
    /**
     * 
     */
    private void updateBtnUI(final boolean enable)
    {
        moveBtn.setEnabled(enable);
        clearBtn.setEnabled(editTextArea.getText().length() > 0);
    }
}
