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
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.AutoNumberIFace;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFormatterEditorDlg;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 18, 2008
 *
 */
public class FormatterPickerPanel extends BaseSetupPanel
{
    protected JCheckBox isNumericChk    = UIHelper.createCheckBox(getResourceString("IS_NUM_CHK"));
    protected JComboBox formatterCBX    = createComboBox(new DefaultComboBoxModel());
    protected JLabel    isNumericLbl    = createLabel(" ");
    protected JLabel    patternLbl      = createLabel(" ");
    protected JLabel    autoIncLbl      = createLabel(" ");
    protected JSpinner  lenSpin         = new JSpinner(new SpinnerNumberModel(9,9,20,1));
    protected JLabel	lenTitleLbl     = null;
    
    protected boolean                     doingCatNums;
    protected List<UIFieldFormatterIFace> fmtList;
    protected UIFieldFormatterIFace       newFormatter = null;
    protected int                         newFmtInx    = 0;
    protected boolean                     wasUsed      = false;
    protected String                      currFormatter;
    protected boolean                     doingDisciplineCollection = false;

    protected UIFieldFormatterMgr         uiFldFmtMgr;
    
    /**
     * @param nextBtn
     * @param doingCatNums
     */
    public FormatterPickerPanel(final String  panelName, 
                                final String  helpContext,
                                final JButton nextBtn, 
                                final JButton prevBtn, 
                                final boolean doingCatNums,
                                final String  currFormatter)
    {
        super(panelName, helpContext, nextBtn, prevBtn);
        
        //UIFieldFormatterMgr.setDoingLocal(true);
        //uiFldFmtMgr = new SpecifyUIFieldFormatterMgr();
        //UIFieldFormatterMgr.setDoingLocal(false);
        uiFldFmtMgr = UIFieldFormatterMgr.getInstance();
        
        this.doingCatNums  = doingCatNums;
        this.currFormatter = currFormatter;
        
        formatterCBX.addActionListener(createFrmCBXAL());
        
        loadFormatCbx(null);

        CellConstraints cc = new CellConstraints();
        //removing wiz changes for #9604
        //PanelBuilder    pb = new PanelBuilder(new FormLayout("p,4px,p,2px,p,f:p:g", "p,10px,p,4px,p,2px,p,2px,p,2px,p"), this);
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,4px,p,2px,p,f:p:g", "p,10px,p,4px,p,2px,p,2px,p"), this);
        
        int y = 1;
        String label = getResourceString(doingCatNums ? "CHOOSE_FMT_CAT" : "CHOOSE_FMT_ACC");
        pb.add(createLabel(label, SwingConstants.CENTER), cc.xywh(1, y, 6, 1)); 
        y +=2;
        
        JLabel lbl = createI18NFormLabel("FORMAT", SwingConstants.RIGHT);
        lbl.setFont(bold);
        pb.add(lbl, cc.xy(1, y));
        pb.add(formatterCBX, cc.xy(3, y));
        y +=2;

        if (doingCatNums)
        {
            pb.add(createI18NFormLabel("IS_NUM", SwingConstants.RIGHT), cc.xy(1, y));
            pb.add(isNumericLbl, cc.xy(3, y));
            y +=2;
            
            /* removing wiz changes for #9604...
            lenTitleLbl = createI18NFormLabel("NUM_LEN", SwingConstants.RIGHT);
            pb.add(lenTitleLbl, cc.xy(1, y));
            UIHelper.setControlSize(lenSpin);
            lenSpin.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
	                int index = formatterCBX.getSelectedIndex();
	                UIFieldFormatterIFace fmt = null;
	                if (index > (doingCatNums ? 0 : 1))
	                {
	                    fmt = fmtList.get(index - (doingCatNums ? 1 : 2));
	                    //condition should be the only possibility but...
	                    if (fmt != null && fmt.getName().equals("CatalogNumberNumeric"));
	                    {
	                    	fmt.setLength((Integer)lenSpin.getValue());
	                        lenSpin.setValue(fmt.getLength());
	                        patternLbl.setText(fmt.toPattern());
	                    }
	                }
	                lenSpin.setVisible(fmt != null && fmt.getName().equals("CatalogNumberNumeric"));
				}
            	
            });
            pb.add(lenSpin, cc.xy(3, y));
            lenSpin.setVisible(false);
            lenTitleLbl.setVisible(false);
            y +=2; */
        }
        
        pb.add(createI18NFormLabel("PATTERN", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(patternLbl, cc.xy(3, y));
        y +=2;

        pb.add(createI18NFormLabel("IS_AUTO_INC", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(autoIncLbl, cc.xy(3, y));
        y +=2;

        nextBtn.setEnabled(false);

    }
    
    /**
     * @param doingDisciplineColl
     */
    public void setDoingDisciplineCollection(final boolean doingDisciplineColl)
    {
        this.doingDisciplineCollection = doingDisciplineColl;
    }
    
    /**
     * Launches the Field Formatter editor.
     */
    protected void addFieldFormatter()
    {
        DBFieldInfo fieldInfo;
        if (doingCatNums)
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
            fieldInfo = ti.getFieldByColumnName("CatalogNumber");
        } else
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(Accession.getClassTableId());
            fieldInfo = ti.getFieldByColumnName("AccessionNumber");
        }
        
        newFormatter = new UIFieldFormatter();
        UIFormatterEditorDlg dlg = new UIFormatterEditorDlg(null, fieldInfo, newFormatter, true, false, uiFldFmtMgr);
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            if (newFormatter.isIncrementer())
            {
                boolean isSingleField = newFormatter.getFields().size() == 1;
                AutoNumberIFace autoNum = doingCatNums ? 
                    UIFieldFormatterMgr.getInstance().createAutoNumber("edu.ku.brc.specify.dbsupport.CollectionAutoNumber", "edu.ku.brc.specify.datamodel.CollectionObject", "catalogNumber", isSingleField) :
                    UIFieldFormatterMgr.getInstance().createAutoNumber("edu.ku.brc.af.core.db.AutoNumberGeneric", "edu.ku.brc.specify.datamodel.Accession", "accessionNumber", isSingleField);
                newFormatter.setAutoNumber(autoNum);
            }
        } else
        {
            newFormatter = null;
        }
        loadFormatCbx(newFormatter);
    }
    
    /**
     * @return
     */
    protected ActionListener createFrmCBXAL()
    {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int index = formatterCBX.getSelectedIndex();
                UIFieldFormatterIFace fmt = null;
                if (index > (doingCatNums ? 0 : 1))
                {
                    fmt = fmtList.get(index - (doingCatNums ? 1 : 2));
                    if (fmt != null)
                    {
                        isNumericLbl.setText(getResourceString(fmt.isNumeric() ? "YES" : "NO"));
                        
                        //removing wiz changes for #9604
                        //lenSpin.setValue(fmt.getLength());
                        
                        patternLbl.setText(fmt.toPattern());
                        autoIncLbl.setText(getResourceString(fmt.isIncrementer() ? "YES" : "NO"));
                    }
                }
                
                /* removing wiz changes for #9604
                lenSpin.setVisible(fmt != null && fmt.getName().equals("CatalogNumberNumeric"));
                lenTitleLbl.setVisible(lenSpin.isVisible()); */
                
                if (formatterCBX.getSelectedIndex() == newFmtInx)
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            addFieldFormatter();
                            updateBtnUI();
                        }
                    });
                }
                updateBtnUI();
            }
        };
    }
    
    /**
     * 
     */
    protected void loadFormatCbx(final UIFieldFormatterIFace selectedFmt)
    {
        ((DefaultComboBoxModel)formatterCBX.getModel()).removeAllElements();
        
        //Vector<ActionListener> alList = new Vector<ActionListener>();
        //alList.addAll(formatterCBX.getActionListeners());
        
        //Collections.addAll(alList, formatterCBX.getActionListeners());
        
        for (ActionListener al : formatterCBX.getActionListeners())
        {
            formatterCBX.removeActionListener(al);
        }
        
        fmtList = new Vector<UIFieldFormatterIFace>(uiFldFmtMgr.getFormatterList(doingCatNums ? CollectionObject.class : Accession.class));
        if (newFormatter != null)
        {
            fmtList.add(newFormatter);
        }
        Collections.sort(fmtList, new Comparator<UIFieldFormatterIFace>() {
            @Override
            public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        if (!doingCatNums)
        {
            ((DefaultComboBoxModel)formatterCBX.getModel()).addElement(getResourceString("NONE"));
            newFmtInx = 1;
        }
        ((DefaultComboBoxModel)formatterCBX.getModel()).addElement(getResourceString("CREATE"));

        for (UIFieldFormatterIFace fmt : fmtList)
        {
            ((DefaultComboBoxModel)formatterCBX.getModel()).addElement(fmt.getName());
        }

        if (currFormatter != null)
        {
            formatterCBX.setSelectedItem(currFormatter);
            nextBtn.setEnabled(true);
            
        } else if (selectedFmt != null)
        {
            formatterCBX.setSelectedItem(selectedFmt.getName());
            nextBtn.setEnabled(true);
            
        } else
        {
            formatterCBX.setSelectedIndex(-1);
            nextBtn.setEnabled(false);
        }
        
        formatterCBX.addActionListener(createFrmCBXAL());
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setVisible(boolean)
     */
    public void setVisible(final boolean vis)
    {
        super.setVisible(vis);
        wasUsed = true;
    }
    
    /**
     * @param f
     * @return
     */
    private boolean needToGetFormatterObject(UIFieldFormatterIFace f) {
    	boolean result = true;
    	if (f != newFormatter) {
    		result = false;
    		if (doingCatNums) {
    			result = f != null && f.getName().equals("CatalogNumberNumeric") && f.getLength() != 9;
    		}
    	}
    	return result;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    public void getValues(final Properties props)
    {
        if (wasUsed)
        {
            String selectedFormatName = (String)formatterCBX.getSelectedItem();
            UIFieldFormatterIFace selectedFormat = newFormatter;
            if (selectedFormatName != null) {
            	for (UIFieldFormatterIFace f : fmtList) {
            		if (selectedFormatName.equals(f.getName())) {
            			if (needToGetFormatterObject(f)) {
            				selectedFormat = f;
            			}             			
            			break;
            		}
            	}
            }
        	if (doingCatNums)
            {
                props.put("catnumfmt", selectedFormat != null ? selectedFormat : formatterCBX.getSelectedItem());
                
            } else if (formatterCBX.getSelectedIndex() > 1)
            {
                props.put("accnumfmt", selectedFormat != null ? selectedFormat : formatterCBX.getSelectedItem());
            } else
            {
                props.remove("accnumfmt");
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
        super.setValues(values);
        doingNext();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        return !doingCatNums || formatterCBX.getSelectedIndex() > 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#updateBtnUI()
     */
    @Override
    public void updateBtnUI()
    {
        int     inx     = formatterCBX.getSelectedIndex();
        boolean enable;
        if (doingCatNums)
        {
            enable = inx > 0;
        } else
        {
            enable = inx != 1;
        }
        nextBtn.setEnabled(enable);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingNext()
     */
    @Override
    public void doingNext()
    {
        updateBtnUI();
        
        Institution institution = null;
        Boolean     isAccGlobal = null;
        if (!doingCatNums)
        {
            if (AppContextMgr.getInstance() != null && AppContextMgr.getInstance().hasContext())
            {
                institution = AppContextMgr.getInstance().getClassObject(Institution.class);
                if (institution != null)
                {
                    isAccGlobal = institution.getIsAccessionsGlobal();
                }
            }
        
            if (isAccGlobal == null && properties != null)
            {
                isAccGlobal = (Boolean)properties.get("accglobal");
                isAccGlobal = isAccGlobal == null ? false : isAccGlobal;
                
            } else
            {
                isAccGlobal = false;
            }
            
            if (institution != null && !doingDisciplineCollection)
            {
                formatterCBX.setEnabled(!isAccGlobal);
                
            } else if (currFormatter != null || (isAccGlobal && institution != null) || doingDisciplineCollection)
            {
                formatterCBX.setEnabled(false);
            }
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingPrev()
     */
    @Override
    public void doingPrev()
    {
        updateBtnUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getSummary()
     */
    @Override
    public List<Pair<String, String>> getSummary()
    {
        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        String lbl   = getResourceString(doingCatNums ? "CollectionObject" : "Accession") + " " + getResourceString("Formatter");
        String value = formatterCBX.getSelectedIndex() == -1 ? "" : formatterCBX.getSelectedItem().toString(); 
        list.add(new Pair<String, String>(lbl, value));
        return list;
    }
    
    
}
