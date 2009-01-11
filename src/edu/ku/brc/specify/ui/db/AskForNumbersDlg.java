/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.ui.db;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

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
import edu.ku.brc.ui.CustomDialog;
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
public class AskForNumbersDlg extends CustomDialog
{
    private static final Logger log = Logger.getLogger(AskForNumbersDlg.class);
    
    protected Vector<Integer> numbersList = new Vector<Integer>();
    protected Class<? extends FormDataObjIFace> dataClass;
    protected String          labelKey;
    protected String          fieldName;
    protected JTextArea       textArea;
    protected JTextArea       status;
    protected JScrollPane     statusSP;
    protected StringBuilder   errorList = new StringBuilder();
    
    
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
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,f:p:g"));
        CellConstraints cc = new CellConstraints();
        
        textArea = UIHelper.createTextArea(5, 30);
        status   = UIHelper.createTextArea(3, 30);
        status.setEditable(false);
        
        pb.add(UIHelper.createI18NLabel(labelKey, SwingConstants.RIGHT), cc.xy(1,1));
        pb.add(UIHelper.createScrollPane(textArea), cc.xy(3,1));
        
        statusSP = new JScrollPane(status, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pb.add(statusSP, cc.xy(3,3));
        statusSP.setVisible(false);
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pack();
    }
    
    /**
     * @return
     */
    protected boolean processNumbers()
    {
        status.setText("");
        numbersList.clear();
        errorList.setLength(0);
        
        DBTableInfo           ti        = DBTableIdMgr.getInstance().getByClassName(dataClass.getName());
        DBFieldInfo           fi        = ti.getFieldByName(fieldName);
        UIFieldFormatterIFace formatter = fi.getFormatter();
        
        boolean isOK = true;
        
        String catNumbersStr = textArea.getText().trim();
        if (StringUtils.isNotEmpty(catNumbersStr))
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                for (String catNumStr : StringUtils.split(catNumbersStr, ','))
                {
                    String catNum      = catNumStr.trim();
                    String catNumForDB = catNum;
                    if (formatter != null)
                    {
                        try
                        {
                            catNumForDB = (String)formatter.formatFromUI(catNum);
                            
                        } catch (java.lang.NumberFormatException ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AskForNumbersDlg.class, ex);
                            errorList.append(getLocalizedMessage("AFN_NUMFMT_ERROR", catNum));
                            errorList.append("\n");
                            isOK = false;
                        }
                    }
                    
                    if (StringUtils.isNotEmpty(catNumForDB))
                    {
                        String sql = QueryAdjusterForDomain.getInstance().adjustSQL("SELECT id FROM "+ti.getClassName()+" WHERE "+fieldName+" = '"+catNumForDB+"' AND CollectionmemberID = COLLID");
                        Integer colObjId = (Integer)session.getData(sql);
                        
                        if (colObjId != null)
                        {
                            numbersList.add(colObjId);
                        } else
                        {
                            statusSP.setVisible(true);
                            pack();
                            errorList.append(getLocalizedMessage("AFN_NOTFND_ERROR", fi.getTitle(), catNum));
                            errorList.append("\n");
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
        return isOK;
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
        } else
        {
            status.setText(errorList.toString());
        }
    }

    /**
     * @return the numbersList a valid list of numbers.
     */
    public Vector<Integer> getNumbersList()
    {
        return numbersList;
    }
    
    /**
     * @return
     */
    public RecordSetIFace getRecordSet()
    {
        if (numbersList.size() > 0)
        {
            RecordSet rs = new RecordSet();
            rs.initialize();
            rs.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
            rs.setDbTableId(DBTableIdMgr.getInstance().getByClassName(dataClass.getName()).getTableId());
            for (Integer id : numbersList)
            {
                rs.addItem(id);
            }
            return rs;
        }
        return null;
    }
}
