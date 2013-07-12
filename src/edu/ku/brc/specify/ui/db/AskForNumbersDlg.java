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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.ui.CatalogNumberFormatter;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 10, 2008
 *
 */
public class AskForNumbersDlg extends CustomDialog implements ChangeListener
{
    private static final Logger log = Logger.getLogger(AskForNumbersDlg.class);
    private static final String AND_COLLID = " AND CollectionMemberID = COLLID";
    
    protected Class<? extends FormDataObjIFace> dataClass;
    protected Vector<String>      numbersList   = new Vector<String>();
    protected Vector<Integer>     dataObjsIds   = new Vector<Integer>();
    protected String              labelKey;
    protected String              fieldName;
    protected JTextArea           textArea;
    
    protected NumberEditorPanel   errorPanel;
    protected NumberEditorPanel   missingPanel;
    
    protected ArrayList<String>   numErrorList   = new ArrayList<String>();
    protected ArrayList<String>   numMissingList = new ArrayList<String>();
    
    protected PanelBuilder        pb;
    
    
    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public AskForNumbersDlg(final String  titleKey,
                            final String  labelKey,
                            Class<? extends FormDataObjIFace> dataClass,
                            final String  fieldName) throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), getResourceString(titleKey), true, OKCANCELHELP, null);
        this.labelKey  = labelKey;
        this.dataClass = dataClass;
        this.fieldName = fieldName;
        
        this.helpContext = "AskForCatNumbers";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        textArea     = UIHelper.createTextArea(5, 30);
        errorPanel   = new NumberEditorPanel(textArea, this, "AFN_NUMFMT_ERROR");
        missingPanel = new NumberEditorPanel(textArea, this, "AFN_NOTFND_ERROR");
        
        CellConstraints cc = new CellConstraints();
        
        pb = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p,4px,f:p:g,4px,f:p:g"));
        pb.addSeparator(UIRegistry.getResourceString(labelKey), cc.xy(1,1));
        pb.add(UIHelper.createScrollPane(textArea),             cc.xy(1,3));
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        textArea.getDocument().addDocumentListener(new DocumentAdaptor()
        {
            @Override
            protected void changed(DocumentEvent e)
            {
                checkStatus();
            }
        });
        
        getOkBtn().setEnabled(false);
        
        pack();
    }
    
    /**
     * @return
     */
    protected boolean processNumbers()
    {
        dataObjsIds.clear();
        numbersList.clear();
        
        numErrorList.clear();
        numMissingList.clear();
        errorPanel.setNumbers(null);
        missingPanel.setNumbers(null);
        
        DBTableInfo           ti          = DBTableIdMgr.getInstance().getByClassName(dataClass.getName());
        DBFieldInfo           fi          = ti.getFieldByName(fieldName);
        boolean               hasColMemID = ti.getFieldByColumnName("CollectionMemberID", true) != null;
        UIFieldFormatterIFace formatter   = fi.getFormatter();
        
        // Check for a dash in the format
        char rangeSeparator = formatter != null ? formatter.hasDash() ? '/' : '-' : ' ';
        
        boolean isOK = true;
        
        String fieldStr = textArea.getText().trim();
        if (formatter != null && formatter.isNumeric() && ti.getTableId() == 1 && fieldName.equals("catalogNumber"))
        {
        	fieldStr = CatalogNumberFormatter.preParseNumericCatalogNumbers(fieldStr, formatter);
        }
        
        if (StringUtils.isNotEmpty(fieldStr))
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                String[] toks = StringUtils.split(fieldStr, ',');
                for (String fldStr : toks)
                {
                    String numToken = fldStr.trim();
                    if (formatter != null && StringUtils.contains(numToken, rangeSeparator))
                    {
                        String   fldNum    = null;
                        String   endFldNum = null;
                        String[] tokens    =  StringUtils.split(numToken, rangeSeparator);
                        if (tokens.length == 2)
                        {
                            try
                            {
                                if (formatter.isNumeric())
                                {
                                    if (!StringUtils.isNumeric(fldNum) || !StringUtils.isNumeric(endFldNum))
                                    {
                                        numErrorList.add(fldStr.trim());
                                        isOK = false;
                                        continue;
                                    }
                                }
                                fldNum    = (String)formatter.formatFromUI(tokens[0].trim());
                                endFldNum = (String)formatter.formatFromUI(tokens[1].trim());
                                
                            } catch (java.lang.NumberFormatException ex)
                            {
                                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AskForNumbersDlg.class, ex);
                                numErrorList.add(numToken);
                                isOK = false;
                            }
                            
                            String sql = String.format("SELECT id FROM %s WHERE %s >= '%s' AND %s <= '%s' %s", 
                                                        ti.getClassName(), fieldName, fldNum, fieldName, endFldNum, (hasColMemID ? AND_COLLID : ""));
                            sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
                            List<?> list = session.getDataList(sql);
                            for (Object obj : list)
                            {
                                dataObjsIds.add((Integer)obj);
                            }
                            numbersList.add(numToken);
                            
                        } else
                        {
                            numErrorList.add(numToken);
                            isOK = false;
                        }
                        continue;
                    }
                    
                    String fldValForDB = numToken;
                    try
                    {
                        if (formatter != null)
                        {
                            if (formatter.isNumeric())
                            {
                                if (!StringUtils.isNumeric(numToken))
                                {
                                    numErrorList.add(numToken);
                                    isOK = false;
                                    continue;
                                }
                            }
                            fldValForDB = (String)formatter.formatFromUI(numToken);
                        } else
                        {
                            fldValForDB = numToken;
                        }
                        
                    } catch (java.lang.NumberFormatException ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AskForNumbersDlg.class, ex);
                        numErrorList.add(numToken);
                        isOK = false;
                    }
                
                    if (StringUtils.isNotEmpty(fldValForDB))
                    {
                        String sql = String.format("SELECT id FROM %s WHERE %s = '%s' %s", ti.getClassName(), fieldName, fldValForDB, (hasColMemID ? AND_COLLID : ""));
                        sql        = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
                        //log.debug(sql);
                        Integer recordId = (Integer)session.getData(sql);
                        
                        if (recordId != null)
                        {
                            dataObjsIds.add(recordId);
                            numbersList.add(numToken);
                        } else
                        {
                            numMissingList.add(numToken);
                            isOK = false;
                        }
                    }
                }
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AskForNumbersDlg.class, ex);
                log.error(ex);
                ex.printStackTrace();
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
        
        buildNumberList(numbersList, textArea);
        
        pb.getPanel().removeAll();
        
        CellConstraints cc = new CellConstraints();
        pb.addSeparator(UIRegistry.getResourceString(labelKey), cc.xy(1,1));
        pb.add(UIHelper.createScrollPane(textArea),             cc.xy(1,3));

        int y = 5;
        if (numErrorList.size() > 0)
        {
            errorPanel.setNumbers(numErrorList);
            pb.add(UIHelper.createScrollPane(errorPanel), cc.xy(1,y));
            y += 2;
        }
        
        if (numMissingList.size() > 0)
        {
            missingPanel.setNumbers(numMissingList);
            pb.add(UIHelper.createScrollPane(missingPanel), cc.xy(1,y));
            y += 2;
        }
        
        if (numErrorList.isEmpty() && 
            numMissingList.isEmpty() && 
            dataObjsIds.isEmpty())
        {
            UIRegistry.showLocalizedError("BT_NO_NUMS_ERROR");
            return false;
        }
        
        
        if (!isOK)
        {
            pack();
        }
        return isOK;
    }
    
    /**
     * @param list
     * @param ta
     */
    protected static void buildNumberList(final List<String> list, final JTextArea ta)
    {
        StringBuilder sb = new StringBuilder();
        if (list != null && list.size() > 0)
        {
            for (String num : list)
            {
                if (sb.length() > 0) sb.append(", ");
                sb.append(num);
            }
        }
        ta.setText(sb.toString());
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        if (processNumbers())
        {
            super.okButtonPressed();
        }
        checkStatus();
    }

    /**
     * @return the numbersList a valid list of numbers.
     */
    public Vector<Integer> getNumbersList()
    {
        return dataObjsIds;
    }
    
    /**
     * @return
     */
    public RecordSetIFace getRecordSet()
    {
        if (dataObjsIds.size() > 0)
        {
            RecordSet rs = new RecordSet();
            rs.initialize();
            rs.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
            rs.setDbTableId(DBTableIdMgr.getInstance().getByClassName(dataClass.getName()).getTableId());
            for (Integer id : dataObjsIds)
            {
                rs.addItem(id);
            }
            return rs;
        }
        return null;
    }
    
    /**
     * 
     */
    private void checkStatus()
    {
        getOkBtn().setEnabled(textArea.getText().length() > 0 && errorPanel.isOK() && missingPanel.isOK());
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(ChangeEvent e)
    {
       checkStatus();
    }
}
