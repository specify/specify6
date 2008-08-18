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

package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createList;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.ui.CustomDialog;

/**
 * (This needs to be converted over to use FmtListEditorDlgBase)
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 27, 2008
 *
 */
public class UIFormatterListEdtDlg extends CustomDialog
{
	protected DBFieldInfo               fieldInfo = null;
    
    // used to hold changes to formatters before committing them to DB
    protected DataObjFieldFormatMgr 	dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr		uiFieldFormatterMgrCache;
    
    protected UIFieldFormatterFactory   formatFactory;

    protected JList                     formatList;
    protected DefEditDeleteAddPanel     dedaPanel;
    protected ListSelectionListener     formatListSelectionListener = null;
    protected boolean                   hasChanged = false;
    
    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public UIFormatterListEdtDlg(final Frame               frame, 
                                 final DBFieldInfo         fieldInfo, 
                                 final UIFieldFormatterMgr uiFieldFormatterMgrCache) throws HeadlessException
    {
        super(frame, getResourceString("FFE_DLG_TITLE"), true, OKCANCELHELP, null);
        
        this.fieldInfo                   = fieldInfo;
        this.uiFieldFormatterMgrCache    = uiFieldFormatterMgrCache;
        this.formatFactory               = UIFieldFormatterMgr.getFormatFactory(fieldInfo);
        this.helpContext                 = "UIF_LIST_EDITOR";

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        CellConstraints cc = new CellConstraints();

        // get formatters for field
        List<UIFieldFormatterIFace> fmtrs = new Vector<UIFieldFormatterIFace>(
        		uiFieldFormatterMgrCache.getFormatterList(fieldInfo.getTableInfo().getClassObj(), fieldInfo.getName()));
        Collections.sort(fmtrs, new Comparator<UIFieldFormatterIFace>() {
            public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
            {
                return o1.toPattern().compareTo(o2.toPattern());
            }
        });

        // table and field titles
        PanelBuilder tblInfoPB = new PanelBuilder(new FormLayout("r:p,2px,f:p:g", "p,2px,p,2px,p,10px")/*, new FormDebugPanel()*/);

        String typeStr = fieldInfo.getType();
        typeStr = typeStr.indexOf('.') > -1 ? StringUtils.substringAfterLast(fieldInfo.getType(), ".") : typeStr;

        JLabel tableTitleLbl      = createLabel(getResourceString("FFE_TABLE") + ":");
        JLabel tableTitleValueLbl = createLabel(fieldInfo.getTableInfo().getTitle());
        tableTitleValueLbl.setBackground(Color.WHITE);
        tableTitleValueLbl.setOpaque(true);

        JLabel fieldTitleLbl      = createLabel(getResourceString("FFE_FIELD") + ":");
        JLabel fieldTitleValueLbl = createLabel(fieldInfo.getTitle());
        fieldTitleValueLbl.setBackground(Color.WHITE);
        fieldTitleValueLbl.setOpaque(true);

        //JLabel fieldTypeLbl = createLabel(getResourceString("FFE_TYPE") + ":");
        //JLabel fieldTypeValueLbl = createLabel(typeStr);
        //fieldTypeValueLbl.setBackground(Color.WHITE);
        //fieldTypeValueLbl.setOpaque(true);
        
        JLabel fieldLengthLbl = createLabel(getResourceString("FFE_LENGTH") + ":");
        JLabel fieldLengthValueLbl = createLabel(Integer.toString(fieldInfo.getLength()));
        fieldLengthValueLbl.setBackground(Color.WHITE);
        fieldLengthValueLbl.setOpaque(true);
        
        int y = 1;
        tblInfoPB.add(tableTitleLbl,       cc.xy(1, y));
        tblInfoPB.add(tableTitleValueLbl,  cc.xy(3, y)); y += 2;
        tblInfoPB.add(fieldTitleLbl,       cc.xy(1, y));
        tblInfoPB.add(fieldTitleValueLbl,  cc.xy(3, y)); y += 2;
        //tblInfoPB.add(fieldTypeLbl,        cc.xy(1, y));
        //tblInfoPB.add(fieldTypeValueLbl,   cc.xy(3, y)); y += 2;
        tblInfoPB.add(fieldLengthLbl,      cc.xy(1, y));
        tblInfoPB.add(fieldLengthValueLbl, cc.xy(3, y)); y += 2;

        DefaultListModel listModel = new DefaultListModel();

        // add available formatters
        for (UIFieldFormatterIFace format : fmtrs)
        {
        	listModel.addElement(format);
        }
        
        formatList = createList(listModel);
        formatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hookFormatListSelectionListener();
        hookFormatListMouseListener();
        
        ActionListener deleteListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {   
                deleteFormatter();
            }
        };
        
        ActionListener editListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {   
                editFormatter((UIFieldFormatter)formatList.getSelectedValue(), false);
            }
        };
        
        ActionListener addListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {   
                addFormatter();
            }
        };
        
        ActionListener defListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {   
                setAsDefFormatter();
            }
        };
        
        dedaPanel = new DefEditDeleteAddPanel(addListener, deleteListener, editListener, defListener,
                                              "FFE_ADD", "FFE_DEL", "FFE_EDT", "FFE_DEF");
        dedaPanel.getAddBtn().setEnabled(true);
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:max(250px;p):g", "p,4px,p,2px,f:p:g,4px,p"));
 
        y = 1;
        pb.add(tblInfoPB.getPanel(), cc.xy(1, y)); y += 2;
        pb.add(createI18NLabel("FFE_AVAILABLE_FORMATS", SwingConstants.LEFT), cc.xy(1, y)); y += 2; 
        pb.add(createScrollPane(formatList), cc.xy(1, y)); y += 2;
        pb.add(dedaPanel, cc.xy(1, y)); y += 2;
        
        pb.setDefaultDialogBorder();
        
        //pb.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        pack();
        
    }

    /**
     * 
     */
    private void hookFormatListMouseListener()
    {
        MouseAdapter mAdp = new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    int index = formatList.locationToIndex(e.getPoint());
                    formatList.ensureIndexIsVisible(index);
                    editFormatter((UIFieldFormatter)formatList.getSelectedValue(), false);
                }
            }
        };
        
        formatList.addMouseListener(mAdp);
    }
    
    
    /**
     * 
     */
    private void hookFormatListSelectionListener() 
    {
        if (formatListSelectionListener == null)
        {
            formatListSelectionListener = new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    if (e.getValueIsAdjusting()) 
                    {
                        return;
                    }
                    updateUIEnabled();
                }
            };
        }
        
        formatList.addListSelectionListener(formatListSelectionListener);
    }


    /**
     * 
     */
    protected void updateUIEnabled()
    {
    	
    	UIFieldFormatterIFace uif = (UIFieldFormatterIFace)formatList.getSelectedValue();
    	boolean selected = uif != null && !uif.isSystem();
        dedaPanel.getDelBtn().setEnabled(selected);
        dedaPanel.getEditBtn().setEnabled(selected);
        dedaPanel.getDefBtn().setEnabled(selected);
    }
    
    /**
     * Sets the selected formatter as the default.
     */
    protected void setAsDefFormatter()
    {
        UIFieldFormatter selected = (UIFieldFormatter)formatList.getSelectedValue();
        DefaultListModel model    = (DefaultListModel)formatList.getModel();
        for (int i=0;i<model.getSize();i++)
        {
            UIFieldFormatter uif = (UIFieldFormatter)model.get(i);
            uif.setDefault(uif == selected);
        }
        setHasChanged(true);
        formatList.repaint();
    }
    
    /**
     * @param uif
     * @param isNew
     */
    protected void editFormatter(final UIFieldFormatterIFace uif, final boolean isNew)
    {
        try
        {
            UIFieldFormatterIFace tempCopy = isNew ? uif : (UIFieldFormatterIFace)uif.clone();
            
            UIFormatterEditorDlg dlg = new UIFormatterEditorDlg(this, fieldInfo, tempCopy, uiFieldFormatterMgrCache);
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                if (isNew)
                {
                    DefaultListModel model = (DefaultListModel) formatList.getModel();
                    model.addElement(uif);
                    uiFieldFormatterMgrCache.addFormatter(uif);
                    
                } else
                {
                    uiFieldFormatterMgrCache.addFormatter(uif);
                }
                setHasChanged(true);
            }
            
        } catch (CloneNotSupportedException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    protected void deleteFormatter()
    {
        UIFieldFormatterIFace uif  = (UIFieldFormatterIFace)formatList.getSelectedValue();
        if (uif != null)
        {
            DefaultListModel model = (DefaultListModel) formatList.getModel();
            model.removeElement(uif);
            uiFieldFormatterMgrCache.removeFormatter(uif);
            setHasChanged(true);
        }
    }
    
    /**
     * 
     */
    protected void addFormatter()
    {
        UIFieldFormatter uif = new UIFieldFormatter();
        editFormatter(uif, true);
    }
    
    /**
     * @return the hasChanged
     */
    public boolean hasChanged()
    {
        return hasChanged;
    }

    /**
     * @param hasChanged the hasChanged to set
     */
    public void setHasChanged(boolean hasChanged)
    {
        if (this.hasChanged != hasChanged)
        {
            setWindowModified(hasChanged);
        }
        this.hasChanged = hasChanged;
        updateUIEnabled();
    }
    
}
