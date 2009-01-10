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

package edu.ku.brc.af.ui.forms.formatters;

import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.ui.CustomDialog;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 27, 2008
 *
 */
public class UIFormatterEditorDlg extends CustomDialog
{
	protected DBFieldInfo               fieldInfo      = null;
	protected UIFieldFormatterIFace     selectedFormat = null;
    
    // used to hold changes to formatters before committing them to DB
    protected DataObjFieldFormatMgr 	dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr		uiFieldFormatterMgrCache;
    
    protected UIFieldFormatterFactory   formatFactory;
    protected UIFieldFormatterSampler 	fieldFormatterSampler; 

    protected JLabel	                sampleLabel;
    protected JTextField                nameTF;
    protected JTextField                titleTF;
    protected JTextField                formatTF;
    protected JCheckBox                 byYearCB;
    protected JButton                   deleteBtn;
    
    protected ListSelectionListener     formatListSelectionListener = null;
    protected DocumentListener          formatChangedDL             = null;
    protected boolean                   hasChanged                  = false;
    protected boolean                   isInError                   = false;
    protected boolean                   isNew;
    protected String                    fmtErrMsg                   = null;
    
    /**
     * @param frame
     * @param fieldInfo
     * @param selectedFormat
     * @param uiFieldFormatterMgrCache
     * @throws HeadlessException
     */
    public UIFormatterEditorDlg(final CustomDialog          parentDlg, 
                                final DBFieldInfo           fieldInfo,
                                final UIFieldFormatterIFace selectedFormat,
                                final boolean               isNew,
                                final UIFieldFormatterMgr	uiFieldFormatterMgrCache) throws HeadlessException
    {
        super(parentDlg, getResourceString("FFE_DLG_TITLE"), true, OKCANCELHELP, null);
        
        this.fieldInfo                   = fieldInfo;
        this.selectedFormat              = selectedFormat;
        this.uiFieldFormatterMgrCache    = uiFieldFormatterMgrCache;
        this.isNew                       = isNew;
        this.fieldFormatterSampler       = new UIFieldFormatterSampler(fieldInfo);
        this.formatFactory               = UIFieldFormatterMgr.getFormatFactory(fieldInfo);
        this.helpContext                 = "UIF_EDITOR";
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

        JLabel tableTitleLbl      = createI18NFormLabel("FFE_TABLE");
        JLabel tableTitleValueLbl = createLabel(fieldInfo.getTableInfo().getTitle());
        tableTitleValueLbl.setBackground(Color.WHITE);
        tableTitleValueLbl.setOpaque(true);

        JLabel fieldTitleLbl      = createI18NFormLabel("FFE_FIELD");
        JLabel fieldTitleValueLbl = createLabel(fieldInfo.getTitle());
        fieldTitleValueLbl.setBackground(Color.WHITE);
        fieldTitleValueLbl.setOpaque(true);

        //JLabel fieldTypeLbl = createI18NFormLabel("FFE_TYPE");
        //JLabel fieldTypeValueLbl = createLabel(typeStr);
        //fieldTypeValueLbl.setBackground(Color.WHITE);
        //fieldTypeValueLbl.setOpaque(true);
        
        JLabel fieldLengthLbl = createI18NFormLabel("FFE_LENGTH");
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

        // sample panel
        sampleLabel = createLabel("", SwingConstants.LEFT); 
        JPanel samplePanel = new JPanel();
        samplePanel.setBorder(BorderFactory.createTitledBorder(getResourceString("FFE_SAMPLE"))); 
        samplePanel.add(sampleLabel);

        // name text field
        nameTF = createTextField();
        nameTF.setColumns(20);
        
        // title text field
        titleTF = createTextField();
        titleTF.setColumns(20);
        
        // format text field
        formatTF = createTextField();
        formatTF.setColumns(20);
        
        byYearCB = createCheckBox(getResourceString("FFE_BY_YEAR_CHECKBOX")); 
        hookByYearCheckBoxListener();
        
        // formatting key panel
        JPanel keyPanel = new JPanel();
        keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.Y_AXIS));
        keyPanel.setBorder(BorderFactory.createTitledBorder(getResourceString("FFE_HELP")));
        // left help body text in a single string resource until we build infrastructure to 
        // localize long texts (in files maybe)
        keyPanel.add(createLabel(formatFactory.getHelpHtml()));
        
        y = 1;
        PanelBuilder subPB = new PanelBuilder(new FormLayout("r:p,2px,p", "p,4px, p,4px, p,4px, p,4px"));
        
        subPB.add(createI18NFormLabel("FFE_NAME"), cc.xy(1,y));
        subPB.add(nameTF, cc.xy(3, y)); y += 2;
        
        subPB.add(createI18NFormLabel("FFE_TITLE"), cc.xy(1,y));
        subPB.add(titleTF, cc.xy(3, y)); y += 2;
        
        subPB.add(createI18NFormLabel("FFE_PATTERN"), cc.xy(1,y));
        subPB.add(formatTF, cc.xy(3, y)); y += 2;
        
        subPB.add(byYearCB, cc.xyw(1,y,3)); y += 2;
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p:g,10px,p",  "p,10px,p,4px,p,4px,p"));

        y = 1;
        pb.add(tblInfoPB.getPanel(), cc.xyw(1, y, 1));     y += 2;
        pb.add(subPB.getPanel(),     cc.xy(1, y));         y += 2;
        pb.add(keyPanel,             cc.xywh(3, 1, 1, 5)); y += 2;
        pb.add(samplePanel,          cc.xywh(1, y, 3, 1)); y += 2;  
        
        setByYearSelected(selectedFormat);
        
        nameTF.setEditable(isNew);
        nameTF.setText(selectedFormat.getTitle());
        titleTF.setText(selectedFormat.getName());
        formatTF.setText(selectedFormat.toPattern());
        updateSample(); 
        
        hookTextChangeListener(nameTF,  "FFE_NO_NAME");
        hookTextChangeListener(titleTF, "FFE_NO_TITLE");
        hookFormatTextChangeListener(formatTF);
        
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        updateUIEnabled();
        
        pack();
    }
    
    /**
     * 
     */
    protected void getDataFromUI()
    {
        selectedFormat.setTitle(titleTF.getText());
        selectedFormat.setName(nameTF.getText());
        selectedFormat.setByYear(byYearCB.isSelected());
        selectedFormat.setDefault(false);
        selectedFormat.setDataClass(fieldInfo.getTableInfo().getClassObj());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        super.okButtonPressed();
        getDataFromUI();
    }


    /**
     * 
     */
    private void updateSample() 
    {
        sampleLabel.setText(selectedFormat != null ? selectedFormat.getSample() : " "); 
    	resetError();
    }
    
    /**
     * 
     */
    private void resetError() 
    {
        isInError = false;
        sampleLabel.setForeground(Color.black);
    	updateUIEnabled();
    	fmtErrMsg = null;
    }

    /**
     * @param message
     */
    private void setError(final String message, final boolean doClearFmt) 
    {
        selectedFormat = doClearFmt ? null : selectedFormat;
        isInError      = true;
        sampleLabel.setForeground(Color.red);
        sampleLabel.setText(message);
    	updateUIEnabled();
    }

    /**
     * 
     */
    private void hookByYearCheckBoxListener()
    {
    	ItemListener il = new ItemListener()
    	{
    		public void itemStateChanged(ItemEvent e)
    		{
    			if (e.getItem() == byYearCB)
    			{
    				// do nothing if format is new because it will be destroyed when the next key is pressed
    				// the correct byYear state of newly created formatters is corrected when the ok button is pressed
    				if (selectedFormat != null)
    				{
    					selectedFormat.setByYear(e.getStateChange() == ItemEvent.SELECTED);
    				}
    			}
    		}
    	};
    	byYearCB.addItemListener(il);
    }
    
    /**
     * 
     */
    private void hookFormatTextChangeListener(final JTextField txtFld)
    {
    	if (formatChangedDL == null)
    	{
            formatChangedDL = new DocumentListener()
            {
                public void removeUpdate(DocumentEvent e) 
                {
                	changed(e);
                }
                public void insertUpdate(DocumentEvent e)
                {
                	changed(e);
                }
                public void changedUpdate(DocumentEvent e)
                {
                	changed(e);
                }

                private void changed(@SuppressWarnings("unused")DocumentEvent e)
                {
                	updateSample(); 
                	findOrCreateFormat(formatTF.getText());
                }            	
            };

            txtFld.getDocument().addDocumentListener(formatChangedDL);
    	}
    }
    
    /**
     * Tries to find an existing format with the same pattern
     * If one doesn't exist, creates a new one
     */
    private void findOrCreateFormat(final String pattern)
    {
        if (pattern.length() > fieldInfo.getLength())
        {
            fmtErrMsg = getResourceString("FFE_TOO_LONG");
            setError(fmtErrMsg, true); 
            return;
        }
        
        try 
        {
            selectedFormat = formatFactory.createFormat(pattern, fieldFormatterSampler);
            selectedFormat.setTitle(titleTF.getText());
            selectedFormat.setName(nameTF.getText());
            selectedFormat.setByYear(byYearCB.isSelected());
            
            hasChanged = true;
            
            updateSample();
        }
        catch (UIFieldFormatterInvalidatesExistingValueException e1)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIFormatterEditorDlg.class, e1);
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIFormatterEditorDlg.class, e1);
            fmtErrMsg = getResourceString("FFE_INVALID_FORMAT") + " (*)";
            setError(fmtErrMsg, true); 
        }
        catch (UIFieldFormatterParsingException e2) 
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIFormatterEditorDlg.class, e2);
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIFormatterEditorDlg.class, e2);
            fmtErrMsg = getResourceString("FFE_INVALID_FORMAT");
            setError(fmtErrMsg, true); 
        }
    }
    
    /**
     * @param txtFld
     */
    private void hookTextChangeListener(final JTextField txtFld, final String errMsgKey)
    {
        txtFld.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate(DocumentEvent e) 
            {
                changed(e);
            }
            public void insertUpdate(DocumentEvent e)
            {
                changed(e);
            }
            public void changedUpdate(DocumentEvent e)
            {
                changed(e);
            }

            private void changed(@SuppressWarnings("unused")DocumentEvent e)
            {
                if (StringUtils.isEmpty(txtFld.getText()))
                {
                    setError(getResourceString(errMsgKey), false); 
                    
                } else if (selectedFormat == null)
                {
                    setError(fmtErrMsg, true); 
                    
                } else
                {
                    updateSample();
                }
                hasChanged = true;
            }               
        });
    }

	/**
	 * 
	 */
	private void setByYearSelected(Object obj)
	{
		if (obj instanceof UIFieldFormatterIFace)
		{
			UIFieldFormatterIFace fmt = (UIFieldFormatterIFace) obj;
			byYearCB.setSelected(fmt.getByYear());
		}
	}
	
    /**
     * 
     */
    /*private void setFormatText()
    {
    	String text = (selectedFormat == null)? "" : selectedFormat.toString(); 
    	formatTF.getDocument().removeDocumentListener(formatChangedDL);
    	formatTF.setText(text);
    	formatTF.getDocument().addDocumentListener(formatChangedDL);
    	updateSample();
    }*/


    /**
     * 
     */
    protected void updateUIEnabled()
    {
    	// enable ok button only if currently selected format is valid 
        // by year checkbox is enabled if there's one YEAR and one auto-number (###) in the format
        boolean byYearEnabled = (selectedFormat != null) && (selectedFormat.byYearApplies());
        byYearCB.setEnabled(byYearEnabled);
        
        okBtn.setEnabled(hasChanged && 
                         !isInError && 
                         nameTF.getText().length() > 0 && 
                         titleTF.getText().length() > 0 && 
                         formatTF.getText().length() > 0);
    }


    /**
     * @return the selectedFormat
     */
    public UIFieldFormatterIFace getSelectedFormat()
    {
        return selectedFormat;
    }
}
