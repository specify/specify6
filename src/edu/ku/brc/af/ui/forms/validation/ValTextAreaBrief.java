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

package edu.ku.brc.af.ui.forms.validation;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.IconButton;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * A JTextArea that implements UIValidatable for participating in validation
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValTextAreaBrief extends ValTextArea
{
    protected JPanel              panel;
    protected JScrollPane         scrollPane;
    protected IconButton          iconButton;
    protected boolean             isEditting;
    protected String              title;

    /**
     * 
     */
    public ValTextAreaBrief()
    {
        super();
    }

    /**
     * @param doc
     * @param text
     * @param rows
     * @param cols
     */
    public ValTextAreaBrief(Document doc, String text, int rows, int cols)
    {
        super(doc, text, rows, cols);
    }

    /**
     * @param doc
     */
    public ValTextAreaBrief(Document doc)
    {
        super(doc);
    }

    /**
     * @param rows
     * @param cols
     */
    public ValTextAreaBrief(int rows, int cols)
    {
        super(rows, cols);
    }

    /**
     * @param text
     * @param rows
     * @param cols
     */
    public ValTextAreaBrief(String text, int rows, int cols)
    {
        super(text, rows, cols);
    }

    /**
     * @param text
     */
    public ValTextAreaBrief(String text)
    {
        super(text);
    }

    /**
     * @param isEditMode
     */
    public void initialize(final boolean isEditMode)
    {
        
        isEditting = isEditMode;
        
        iconButton = new IconButton(IconManager.getIcon(isEditting ? "FormEdit" : "InfoIcon", IconManager.IconSize.Std16), false);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p:g,2px,p", "t:p:g"));
        pb.add(new JScrollPane(this, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), cc.xy(1,1));
        pb.add(iconButton, cc.xy(3,1));
        
        panel = pb.getPanel();
        
        iconButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showPopup();
            }
        });
        
        // Enable being able to TAB out of TextArea
        getInputMap().put(KeyStroke.getKeyStroke("TAB"), "none");
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_TAB )
                {
                    if (event.isShiftDown())
                    {
                        transferFocusBackward();
                    } else
                    {
                        transferFocus();
                    }
                }
            }
        });
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.ValTextArea#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        
        if (iconButton != null)
        {
            iconButton.setEnabled(enabled);
        }
        if (scrollPane != null)
        {
            scrollPane.setEnabled(enabled);
        }
    }
    
    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * 
     */
    protected void showPopup()
    {
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        
        final JTextArea ta = UIHelper.createTextArea(10, 60);
        if (getDocument() instanceof ValPlainTextDocument)
        {
            ta.setDocument(new ValPlainTextDocument(((ValPlainTextDocument)getDocument()).getLimit()));
        }
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(isEditting);
        
        
        JScrollPane sp;
        final CustomDialog dlg = new CustomDialog((Frame)null, "", true, isEditting ? CustomDialog.OKCANCEL : CustomDialog.OK_BTN, pb.getPanel());
        if (isEditting)
        {
            sp = UIHelper.createScrollPane(ta);
        } else
        {
            dlg.setOkLabel(UIRegistry.getResourceString("CLOSE"));
            sp = ViewFactory.changeTextAreaForDisplay(ta);
        }
        pb.add(sp,  cc.xy(1,1));
        
        dlg.createUI();
        dlg.setTitle(title);
        dlg.getOkBtn().setEnabled(!isEditting);
        
        ta.setText(getText());
        ta.setCaretPosition(0);
        ta.getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                dlg.getOkBtn().setEnabled(true);
            }
        });
        
        pb.setDefaultDialogBorder();
        
        dlg.pack();
        
        dlg.setVisible(true);
        if (!dlg.isCancelled() && isEditting)
        {
            setText(ta.getText());
        }
    }

    /**
     * @return
     */
    public JComponent getUIComponent()
    {
        return panel;
    }
}
