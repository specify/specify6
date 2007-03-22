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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.validation.ValSpinner;

/**
 * Creates a Dialog used to edit form control attributes.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Mar 22, 2007
 *
 */
public class EditFormControl extends CustomDialog
{
    protected InputPanel inputPanel;
    protected JPanel     canvasPanel;
    
    protected ValSpinner xCoord;
    protected ValSpinner yCoord;
    protected ValSpinner fieldWidth;
    protected JTextField label;
    
    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param isModal whether or not it is model
     * @param contentPanel the contentpane
     * @throws HeadlessException
     */
    public EditFormControl(final Frame     frame, 
                           final String    title, 
                           final InputPanel inputPanel,
                           final JPanel     canvasPanel) throws HeadlessException
    {
        super(frame, title, true, OKCANCELAPPLYHELP, null);
        
        this.inputPanel  = inputPanel;
        this.canvasPanel = canvasPanel;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    protected void createUI()
    {
        super.createUI();
        
        int y    = 1;
        CellConstraints cc         = new CellConstraints();
        PanelBuilder    panelBlder = new PanelBuilder(new FormLayout("p,2px,p,p:g", UIHelper.createDuplicateJGoodiesDef("p", "2px", 10)));
        JPanel          panel      = panelBlder.getPanel();
        
        Dimension canvasSize  = canvasPanel.getSize();
        Dimension controlSize = inputPanel.getSize();
        
        panelBlder.add(new JLabel("X:", JLabel.RIGHT), cc.xy(1, y));
        panelBlder.add(xCoord = new ValSpinner(0, canvasSize.width-controlSize.width, false, false), cc.xy(3, y));
        y += 2;
        
        panelBlder.add(new JLabel("Y:", JLabel.RIGHT), cc.xy(1, y));
        panelBlder.add(yCoord = new ValSpinner(0, canvasSize.height-controlSize.height, false, false), cc.xy(3, y));
        y += 2;
        
        panelBlder.add(new JLabel("Label:", JLabel.RIGHT), cc.xy(1, y));
        panelBlder.add(label = new JTextField(25), cc.xywh(3, y, 2, 1));
        y += 2;
        
        if (inputPanel.getComp() instanceof JTextField)
        {
            panelBlder.add(new JLabel("Field Columns:", JLabel.RIGHT), cc.xy(1, y));
            panelBlder.add(fieldWidth = new ValSpinner(0, 100, false, false), cc.xy(3, y));
            y += 2;
        }
        
        Point location = inputPanel.getLocation();
        xCoord.setValue(((Double)location.getX()).intValue());
        yCoord.setValue(((Double)location.getY()).intValue());
        fieldWidth.setValue(((JTextField)inputPanel.getComp()).getColumns());
        label.setText(inputPanel.getLabel().getText());
        
        applyBtn.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e)
           {
               //Point location = inputPanel.getLocation();
               //location
               inputPanel.setLocation(((Integer)xCoord.getValue()).intValue(), ((Integer)yCoord.getValue()).intValue());
               inputPanel.getWbtmi().setCaption(label.getText());
               
               JLabel      lbl      = inputPanel.getLabel();
               FontMetrics fm       = lbl.getFontMetrics(lbl.getFont());
               int         oldWidth = fm.stringWidth(lbl.getText());
               
               //System.out.println(lbl.getPreferredSize());
               
               lbl.setText(label.getText());
               int newWidth = fm.stringWidth(lbl.getText());
               int diff = newWidth - oldWidth;
               
               //lbl.validate();
               //inputPanel.doLayout();
               
               Dimension newSize = lbl.getPreferredSize();
               newSize.width += diff;
               newSize.width = newWidth;
               
               System.out.println(newSize+" diff "+diff);
               lbl.setSize(newSize);
               lbl.setPreferredSize(newSize);

               System.out.println("OLD:      "+inputPanel.getPreferredSize());
               System.out.println("OLD Comp: "+inputPanel.getComp().getPreferredSize());

               if (inputPanel.getComp() instanceof JTextField)
               {
                   ((JTextField)inputPanel.getComp()).setColumns(((Integer)fieldWidth.getValue()).intValue());
               }
               /*
               inputPanel.validate();
               inputPanel.invalidate();
               inputPanel.doLayout();
               inputPanel.repaint();
               */
               newSize = inputPanel.getSize();
               System.out.println("New: "+newSize);
               System.out.println("New: "+inputPanel.getComp().getPreferredSize());
                             //newSize.width += diff;
               
               Rectangle bounds = inputPanel.getBounds();
               //inputPanel.setSize(newSize);
               //inputPanel.setPreferredSize(newSize);
               bounds.width = newSize.width;
               inputPanel.setBounds(bounds);
               
               inputPanel.validate();
               inputPanel.invalidate();
               inputPanel.doLayout();
               inputPanel.repaint();
           }
        });
        

        mainPanel.add(panel, BorderLayout.CENTER);
        
        pack();
    }
    
    
    
}
