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
package edu.ku.brc.ui.forms.formatters;

import java.awt.Frame;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.IconManager.IconSize;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 9, 2007
 *
 */
public class UIFieldFormatterEditor extends CustomDialog
{
    protected JList                    formattersList;
    protected Vector<UIFieldFormatter> uffList;
    protected JButton                  addUFFBtn;
    protected JButton                  delUFFBtn;
    
    public UIFieldFormatterEditor(final Frame frame, final String title)
    {
        super(frame, title, true, null);
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        CellConstraints cc = new CellConstraints();
        
        uffList = new Vector<UIFieldFormatter>();
        // Left Panel
        formattersList = new JList(uffList);
        addUFFBtn = UIHelper.createButton("PlusSign",  "", IconSize.Std16, true);
        delUFFBtn = UIHelper.createButton("MinusSign", "", IconSize.Std16, true);

        PanelBuilder    lhBldr    = new PanelBuilder(new FormLayout("p", "p, 2px, p, 2px, p"));
        lhBldr.add(new JLabel("Formatters"), cc.xy(1,1));
        lhBldr.add(formattersList,           cc.xy(1,3));
        
        PanelBuilder    lhBBBldr = new PanelBuilder(new FormLayout("f:p:g,p,2px,p", "p"));
        lhBBBldr.add(addUFFBtn,         cc.xy(2, 1));
        lhBBBldr.add(delUFFBtn,         cc.xy(4, 1));
        
        lhBldr.add(lhBBBldr.getPanel(), cc.xy(1, 5));
        
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p", "p"));

       builder.add( lhBldr.getPanel(), cc.xy(1,1));
        
        contentPanel = builder.getPanel();
        
        // TODO Auto-generated method stub
        super.createUI();
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        UIFieldFormatterEditor dlg = new UIFieldFormatterEditor(null, "Editor");
        dlg.setVisible(true);

    }

}
