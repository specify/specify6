/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionMember;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Provides a simple interface for viewing and setting defaults for local fields and foreign keys not present in upload dataset.
 */
public class MissingDataResolver implements ActionListener
{
    protected static final Logger log = Logger.getLogger(MissingDataResolver.class);
    
    /**
     * Foreign keys not provided by the upload dataset.
     */
    protected Vector<RelatedClassSetter> missingClasses;

    /**
     * Local fields not provided by the upload dataset.
     */
    protected Vector<DefaultFieldEntry> missingFlds;

    /**
     * Lists of available values for each missingClass and missingFld.
     */
    protected Vector<ComboBoxModel> lists;

    /**
     * huh?
     */
    protected ActionListener myself; 
    /**
     * The objects corresponding to the rows of uiTbl.
     */
    protected Vector<Object> tblObjects;
    protected JTable uiTbl;
    
    /**
     * @author timbo
     *
     * @code_status Alpha
     *
     * @param <F>
     * @param <S>
     * 
     * Pairs a string label with an Object
     */
    @SuppressWarnings("serial")
    private class LabelledObject<F, S> extends Pair<F, S>
    {
        @Override
        public String toString()
        {
            return first.toString();
        }
        public LabelledObject(F first, S second)
        {
            super(first, second);
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent a)
    {
        if (a.getActionCommand().equals("DEFAULTS"))
        {
            try
            {
            	setDefaults();
            }
            catch (UploaderException ex)
            {
            	UIRegistry.showError(ex.getLocalizedMessage());
            }
            uiTbl.setModel(bldModel(false));
        }
        else if (a.getSource().getClass().equals(JComboBox.class))
        {
            JComboBox jbox = (JComboBox)a.getSource();
            setFldOrClass(jbox.getSelectedItem());
        }
    }
    
    /**
     * @param lblObj
     * 
     * Sets default value based on UI combobox selection.
     */
    protected void setFldOrClass(Object lblObj)
    {
        Object obj = tblObjects.get(uiTbl.getSelectedRow());
        LabelledObject<?, ?> valToSet = (LabelledObject<?,?>)lblObj;
        if (obj.getClass().equals(DefaultFieldEntry.class))
        {
            ((DefaultFieldEntry)obj).setDefaultValue(valToSet.getSecond());
        }
        else if (obj.getClass().equals(RelatedClassSetter.class))
        {
            ((RelatedClassSetter)obj).setDefaultId(valToSet.getSecond());
        }
    }
    
    /**
     * Sets defaults for all foreign keys and local fields.
     */
    protected void setDefaults() throws UploaderException
    {
        setDefaultFlds();
        setDefaultClasses();
    }
    
    /**
     * Sets defaults for all local fields.
     */
    protected void setDefaultFlds()
    {
        for (DefaultFieldEntry dfe : missingFlds)
        {
            meetFldRequirement(dfe);
        }
    }
    
    /**
     * Sets defaults for all foreign keys.
     */
    protected void setDefaultClasses() throws UploaderException
    {
        for (RelatedClassSetter rce : missingClasses)
        {
            if (!rce.isDefined())
            //filter out already assigned defaults (TreeDefs defaults are determined by UploadTableTree)
            {
                rce.defaultSetting();
            }
        }
    }

    
    /**
     * @param missingClasses
     * @param missingFlds
     */
    public MissingDataResolver(Vector<RelatedClassSetter> missingClasses, Vector<DefaultFieldEntry> missingFlds) throws UploaderException
    {
        super();
        this.missingClasses = missingClasses;
        this.missingFlds = missingFlds;
        this.tblObjects = new Vector<Object>(this.missingClasses.size() + this.missingFlds.size());
        setDefaults();
    }
    
    /**
     * @return true if all requirements are met
     */
    public boolean resolve(boolean readOnly)
    {
        showUI(readOnly);
        return isResolved();
    }

    /**
     * @return true if everything is ok.
     */
    public boolean isResolved()
    {
        return missingClassesResolved() && missingFldsResolved();
    }
    
    /**
     * @param rce
     * @return values available for foreign key represented by rce.
     */
    protected Vector<Pair<String, Object>> getReqClassChcs(final RelatedClassSetter rce)
    {
        Vector<Pair<String, Object>> result = new Vector<Pair<String, Object>>();
        StringBuilder hql = new StringBuilder("from ");
        hql.append(rce.getRelatedClass().getSimpleName());     
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();        
        try
        {
            Iterator<?> it = session.createQuery(hql.toString(), false).list().iterator();
            while (it.hasNext())
            {
                DataModelObjBase nextObj = (DataModelObjBase)it.next();
                result.add(new LabelledObject<String, Object>(nextObj.toString(), nextObj.getId()));
            }
        }
        finally
        {
            session.close();
        }
        return result;
    }
    
    /**
     * (Not ready to deal with new re-internationalized resource stuff)
     * @param fldName name of field.
     * @param code byte value 
     * @return string rep for code.
     */
    protected String getTextForTypeCode(final String fldName, final Byte code)
    {
        //i18n
        if (fldName.equalsIgnoreCase("agenttype"))
        {
            switch (code)
            {
                case Agent.GROUP: return "Group"; //i18n
                case Agent.ORG: return "Organization"; //i18n;
                case Agent.OTHER: return "Other"; //i18n;
                case Agent.PERSON: return "Person";  //i18n
            }
        }
        else if (fldName.equalsIgnoreCase("referenceworktype"))
        {
            switch (code)
            {
                case 0: return "Book"; //i18n
                case 1: return "Electronic Media"; //i18n
                case 2: return "Paper"; //i18n
                case 3: return "Technical Report"; //i18n
                case 4: return "Thesis"; //i18n
                case 5: return "Section in Book"; //i18n
            }
        }
        return code.toString();
    }
    
    /**
     * @param fldName
     * @return numeric vals for 'record typing' field.
     */
    protected Vector<Byte> getValsForTypeFld(final String fldName)
    {
        Vector<Byte> result = new Vector<Byte>();
        if (fldName.equalsIgnoreCase("agenttype"))
        {
            result.add(new Byte(Agent.GROUP));
            result.add(new Byte(Agent.ORG));
            result.add(new Byte(Agent.OTHER));
            result.add(new Byte(Agent.PERSON));
        }
        else if (fldName.equalsIgnoreCase("referenceworktype"))
        {
            result.add(/*Book*/ Byte.valueOf("0")); 
            result.add(/*Electronic Media*/ Byte.valueOf("1"));
            result.add(/*Paper*/ Byte.valueOf("2")); 
            result.add(/*Technical Report*/ Byte.valueOf("3")); 
            result.add(/*Thesis*/ Byte.valueOf("4")); 
            result.add(/*Section in Book*/ Byte.valueOf("5")); 
        }
        return result;
    }
    
    /**
     * @param fldName
     * @return true if field with fldName is a record type determining field.
     */
    protected boolean isTypeFld(final String fldName)
    {
        return fldName.equalsIgnoreCase("ReferenceWorkType") || fldName.equalsIgnoreCase("AgentType");
    }
    
    /**
     * @param fldName
     * @return list of values for 'record typing' field.
     */
    protected Vector<LabelledObject<String, Object>> getTypeFldChcs(final String fldName)
    {
        Vector<LabelledObject<String, Object>> result = new Vector<LabelledObject<String, Object>>();
        for (Byte val : getValsForTypeFld(fldName))
        {
            result.add(new LabelledObject<String, Object>(getTextForTypeCode(fldName, val), val));
        }
        return result;
    }
    
    /**
     * @param dfe
     * @return valid values for local field represented by dfe.
     */
     protected Vector<LabelledObject<String, Object>> getReqFldChcs(final DefaultFieldEntry dfe)
    {
        if (isTypeFld(dfe.getFldName()))
        {
            return getTypeFldChcs(dfe.getFldName());
        }
        
        Vector<LabelledObject<String, Object>> result = new Vector<LabelledObject<String, Object>>();
        if (dfe.getUploadTbl().getTblClass() == PrepType.class)
        {
            if (dfe.getFldName().equalsIgnoreCase("IsLoanable"))
            {
                result.add(new LabelledObject<String, Object>(String.valueOf(Boolean.FALSE), new Boolean(false)));
                result.add(new LabelledObject<String, Object>(String.valueOf(Boolean.TRUE), new Boolean(true)));
            }
        }
        else if (dfe.getUploadTbl().getTblClass() == AccessionAgent.class)
        {
            //its a picklist. not sure of generic way to lookup values???
            if (dfe.getFldName().equalsIgnoreCase("Role"))
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try
                {
                    Iterator<?> it = session.createQuery("from picklistitem where picklistid = 15", false).list().iterator();
                    while (it.hasNext())
                    {
                        Object nextOne = it.next();
                        result.add(new LabelledObject<String, Object>(nextOne.toString(), nextOne.toString()));
                    }
                }
                finally
                {
                    session.close();
                }
            }
        }
        else
        {
            log.error("unable to find default valid values for "
                    + dfe.getUploadTbl().getTblClass().getSimpleName() + "." + dfe.getFldName());
        }
        return result;
    }
    
    /**
     * @param dfe
     * @return true if default value can be determined for local field represented by dfe.
     */
    protected boolean meetFldRequirement(DefaultFieldEntry dfe)
    {
        if (dfe.getUploadTbl().getTblClass() == edu.ku.brc.specify.datamodel.Agent.class)
        {
            if (dfe.getFldName().equalsIgnoreCase("agenttype"))
            {
                log.debug("setting Agent.AgentType to 1");
                Object[] options = new Object[4];
                options[0] = new Byte(Agent.PERSON); //the default value for new records
                options[1] = new Byte(Agent.ORG);
                options[2] = new Byte(Agent.GROUP);
                options[3] = new Byte(Agent.OTHER);
                dfe.setValues(options);
                return true;
            }
        }
        if (dfe.getUploadTbl().getTblClass() == edu.ku.brc.specify.datamodel.PrepType.class)
        {
            if (dfe.getFldName().equalsIgnoreCase("IsLoanable"))
            {
                log.debug("setting PrepType.IsLoanable to true");
                dfe.setDefaultValue(true);
                return true;
            }
        }
        if (dfe.getUploadTbl().getTblClass() == edu.ku.brc.specify.datamodel.AccessionAgent.class)
        {
            if (dfe.getFldName().equalsIgnoreCase("Role"))
            {
                log.debug("setting AccessionAgent.Role to \"Receiver\"");
                dfe.setDefaultValue("Receiver");
                return true;
            }
        }
        if (dfe.getUploadTbl().getTblClass() == edu.ku.brc.specify.datamodel.ReferenceWork.class)
        {
            if (dfe.getFldName().equalsIgnoreCase("ReferenceWorkType"))
            {
                log.debug("setting ReferenceWork.ReferenceWorkType to 1");
                dfe.setDefaultValue(new Byte("1"));
                return true;
            }
        }
        if (dfe.getUploadTbl().getTblClass() == edu.ku.brc.specify.datamodel.Determination.class)
        {
            if (dfe.getFldName().equalsIgnoreCase("IsCurrent"))
            {
                log.debug("setting IsCurrent to true");
                dfe.setDefaultValue(true);
                return true;
            }
        }
        if (dfe.getUploadTbl().getTblClass() == edu.ku.brc.specify.datamodel.Locality.class)
        {
            if (dfe.getFldName().equalsIgnoreCase("SrcLatLongUnit"))
            {
                log.debug("setting SrcLatLongUnit to 0");
                dfe.setDefaultValue(new Byte("0"));
                return true;
            }
        }
        if (CollectionMember.class.isAssignableFrom(dfe.getUploadTbl().getTblClass()))
        {
            if (AppContextMgr.getInstance().getClassObject(Collection.class) != null)
            {
                dfe.setDefaultValue(AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionId());
                return true;
            } 
            log.error("No default Collection has been set!");
        }
        log.error("unable to meet requirement: " + dfe.getUploadTbl().getTblClass().getSimpleName() + "." + dfe.getFldName());
        return false;
    }

    /**
     * @param items
     * @return comma-separated list of items.
     */
    protected String commaList(Vector<String> items)
    {
        StringBuilder result = new StringBuilder();
        for (String item : items)
        {
            if (result.length() > 0)
            {
                result.append(", "); 
            }
            result.append(item);
        }
        return result.toString();
    }
    
    /**
     * @param readOnly
     * @return TableModel for UI.
     */
    protected TableModel bldModel(boolean readOnly)
    {
        Vector<String> headers = new Vector<String>();
        headers.add("Columns"); //i18n
        headers.add("Data Type"); //i18n
        headers.add("Data"); //i18n
        headers.add("Value"); //i18n
        
        lists = new Vector<ComboBoxModel>();
        
        Vector<Vector<String>> rows = new Vector<Vector<String>>();
        for (RelatedClassSetter rce : missingClasses)
        {
            Vector<String> row = new Vector<String>(4);
            row.add(commaList(rce.getUploadTbl().getWbFldNames()));
            row.add(rce.getUploadTbl().getTable().getName());
            row.add(rce.getFieldName());
            if (rce.getDefaultId() == null)
            {
                row.add("<undefined>"); //i18n;
            }
            else
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                DataModelObjBase rec = (DataModelObjBase)session.get(rce.getRelatedClass(), (Integer)rce.getDefaultId()); 
                if (rec != null)
                {
                    row.add(rec.toString());
                }
                else
                {
                    row.add(rce.getDefaultId().toString());
                }
            }
            rows.add(row);
            tblObjects.add(rce);
            DefaultComboBoxModel list = new DefaultComboBoxModel();
            //list.addElement(row.get(3));
            Vector<Pair<String, Object>> chcs = this.getReqClassChcs(rce);
            Pair<String, Object> currChc = null;
            for (Pair<String, Object> chc : chcs)
            {
                list.addElement(chc);
                if (rce.getDefaultId() != null && rce.getDefaultId().equals(chc.getSecond()))
                {
                    currChc = chc;
                }
            }
            if (currChc != null)
            {
                list.setSelectedItem(currChc);
            }
            lists.add(list);
        }
        for (DefaultFieldEntry dfe : missingFlds)
        {
            Vector<String> row = new Vector<String>(4);
            row.add(commaList(dfe.getUploadTbl().getWbFldNames()));
            row.add(dfe.getUploadTbl().getTable().getName());
            row.add(dfe.getFldName());
            if (dfe.getDefaultValue() == null || dfe.getDefaultValue() == null)
            {
                row.add("<undefined>"); //i18n;
            }
            else
            {
                if (isTypeFld(dfe.getFldName()))
                {
                    row.add(getTextForTypeCode(dfe.getFldName(), (Byte)dfe.getDefaultValue()));
                }
                else
                {
                    row.add(dfe.getDefaultValue().toString());
                }
            }
            rows.add(row);
            tblObjects.add(dfe);
            DefaultComboBoxModel list = new DefaultComboBoxModel();
            Vector<LabelledObject<String, Object>> chcs = this.getReqFldChcs(dfe);
            LabelledObject<String, Object> currChc = null;
            for (LabelledObject<String, Object> chc : chcs)
            {
                list.addElement(chc);
                if (dfe.getDefaultValue() != null && dfe.getDefaultValue().equals(chc.getSecond()))
                {
                    currChc = chc;
                }
            }
            if (currChc != null)
            {
                list.setSelectedItem(currChc);
            }
            lists.add(list);
        }
        
        myself = this;
		if (readOnly)
		{
			return new DefaultTableModel(rows, headers) {
				@Override
				public boolean isCellEditable(int row, int col)
				{
					return false;
				}
			};
		}
		return new DefaultTableModel(rows, headers) {
			@Override
			public boolean isCellEditable(int row, int col)
			{
				JComboBox jBox = createComboBox(lists.get(row));
				jBox.addActionListener(myself);

				uiTbl.setDefaultEditor(uiTbl.getColumnClass(col),
						new DefaultCellEditor(jBox));
				return col == 3;
			}
		};
    }
    
    public JPanel getUI(boolean readOnly)
    {
        JPanel mainPane = new JPanel(new BorderLayout());
        //JLabel msg = createLabel(getResourceString("WB_UPLOAD_MISSING_DATA")); 
        //msg.setFont(msg.getFont().deriveFont(Font.BOLD));
        //mainPane.add(msg, BorderLayout.NORTH);
        if (!readOnly)
        {
            CellConstraints cc = new CellConstraints();
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p", "p"));
            JButton defBtn = createButton(getResourceString("WB_UPLOAD_DEFAULTS_BTN")); 
            defBtn.setActionCommand("DEFAULTS");
            defBtn.addActionListener(this);
            pb.add(defBtn, cc.xy(2,1));
            mainPane.add(pb.getPanel(), BorderLayout.SOUTH);
        }

        uiTbl = new JTable(bldModel(readOnly));
        uiTbl.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        uiTbl.setDefaultEditor(uiTbl.getColumnClass(3), new DefaultCellEditor(createComboBox()));
        uiTbl.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent me)
            {
                if (me.getClickCount() == 2)
                {
                    JTable tbl = (JTable) me.getSource();
                    int rowNum = tbl.getSelectedRow();
                    log.debug(tbl.getModel().getValueAt(rowNum, 0) + "."
                            + tbl.getModel().getValueAt(rowNum, 1));
                }
            }

            public void mouseEntered(MouseEvent me)
            {
                // who cares?
            }

            public void mouseExited(MouseEvent me)
            {
                // who cares?
            }

            public void mousePressed(MouseEvent me)
            {
                // who cares?
            }

            public void mouseReleased(MouseEvent me)
            {
                // who cares?
            }
        });
        mainPane.add(new JScrollPane(uiTbl), BorderLayout.CENTER);
        return mainPane;
    }
    
    /**
     * Show missing data resolver form.
     * 
     * @return true if user doesn't cancel out of the form.
     */
    protected boolean showUI(boolean readOnly)
    {
        if (missingClasses.size() == 0 && missingFlds.size() == 0)
        {
            return true;
        }
        
        JPanel mainPane = getUI(readOnly);
        CustomDialog cwin;
        if (!readOnly)
        {
            cwin = new CustomDialog(null, "Missing Data", true, mainPane); // i18n
        }
        else
        {
            cwin = new CustomDialog(null, "Missing Data", true, CustomDialog.OK_BTN, mainPane, CustomDialog.OK_BTN); // i18n
        }
            
        cwin.setModal(true);
        UIHelper.centerAndShow(cwin);
        boolean result = !cwin.isCancelled();
        cwin.dispose();
        return result;
    }
    /**
     * @return true if all missing classes are defined.
     */
    protected boolean missingClassesResolved()
    {
        for (RelatedClassSetter rce : missingClasses)
        {
            if (!rce.isDefined())
            {
                return false;
            }
        }
        return true;
    }
    /**
     * @return true if all missing flds are defined.
     */
    protected boolean missingFldsResolved()
    {
        for (DefaultFieldEntry dfe : missingFlds)
        {
            if (!dfe.isDefined())
            {
                return false;
            }
        }
        return true;
    }
}
