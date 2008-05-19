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

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createList;
import static edu.ku.brc.ui.UIHelper.createTextArea;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;

public class DataObjFieldFormatSinglePanelBuilder extends DataObjFieldFormatPanelBuilder {
	
	protected DataObjDataFieldFormatIFace selectedFormat;
	
	// ui controls
	protected JTextArea formatText;
	protected JList availableFieldsLst;
	protected JButton addFieldBtn;
	
	protected DocumentListener formatTextDL;
	protected CaretListener formatTextCL;
	protected ActionListener addFieldAL;
	protected MouseAdapter addFieldMA;
	
	// hash mapping fields or formatters that go inside the available field list
	protected Hashtable<String, Object> fieldHash;

	DataObjFieldFormatSinglePanelBuilder(DBTableInfo tableInfo,
		      							 JList formatList,	
		      							 ListSelectionListener formatListSL,
					                     JButton               okButton,
		      							 DataObjDataFieldFormatIFace selectedFormat)
	{
		super(tableInfo, formatList, formatListSL, okButton);
		this.selectedFormat = selectedFormat;
	    fillWithObjFormatter(selectedFormat);
	}
	
	public DataObjDataFieldFormatIFace getSelectedFormat() 
	{
		return selectedFormat;
	}

	public void setSelectedFormat(DataObjDataFieldFormatIFace selectedFormat) 
	{
		this.selectedFormat = selectedFormat;
	}

	/*
	 * 
	 */
	protected void buildUI() {
	    CellConstraints cc = new CellConstraints();
	    PanelBuilder pb = new PanelBuilder(new FormLayout("f:d:g",  
	    		"10px,"       + // empty space on top of panel 
	    		"p,p,"        + // Label & text box for format
	    		"10px,p,"     + // separator & label
	    		"f:150px:g,"  + // list box for available fields 
	    		"10px,21px,15px"  // empty space where the delete button goes for the multiple value panel
	    		
	    		)/*, new FormDebugPanel()*/);
	    
	    fieldHash = new Hashtable<String, Object>();

	    JLabel currentFieldsLbl = createLabel(getResourceString("DOF_DISPLAY_FORMAT")+":");
	    formatText = createTextArea(4, 50);
	    formatText.setLineWrap(true);
	    // to make sure the component shrinks with the dialog
	    formatText.setMinimumSize(new Dimension(50, 5));
	    formatText.setPreferredSize(new Dimension(100, 10));
	    //formatText.setMaximumSize(new Dimension(5, 1));

        PanelBuilder addFieldPB = new PanelBuilder(new FormLayout("l:m:g,r:m", "p"));  
	    JLabel availableFieldsLbl = createLabel(getResourceString("DOF_AVAILABLE_FIELDS")+":");
        addFieldBtn = createButton(getResourceString("DOF_ADD_FIELD")); 
        addFieldBtn.setEnabled(true);
	    addFieldPB.add(availableFieldsLbl, cc.xy(1,1));
	    addFieldPB.add(addFieldBtn, cc.xy(2,1));

	    DefaultListModel availableFieldListModel = new DefaultListModel();
	    availableFieldsLst = createList(availableFieldListModel);
	    
	    // lay out components on main panel        
	    int y = 2; // leave first row blank 
	    pb.add(currentFieldsLbl,   cc.xy(1, y)); y += 1;
	    pb.add(formatText,         cc.xy(1, y)); y += 2;
	
	    pb.add(addFieldPB.getPanel(), cc.xy(1, y)); y += 1;
	    pb.add(new JScrollPane(availableFieldsLst), cc.xy(1, y)); y += 2;
	    
	    this.mainPanelBuilder = pb;

	    addFormatTextListeners();
	    // must be called after list of available fields has been created
	    addFieldListeners();
	}

	public void addFieldListeners()
	{
		// action listener for add field button
		if (addFieldAL == null)
		{
			addFieldAL = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					addField(availableFieldsLst.getSelectedValue());
				}
			};
		}
		addFieldBtn.addActionListener(addFieldAL);
		
		// mouse adapter to detect double-click on list of available fields 
		if (addFieldMA == null)
		{
			addFieldMA = new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
				{
					if(e.getClickCount() == 2)
					{
						int index = availableFieldsLst.locationToIndex(e.getPoint());
						availableFieldsLst.ensureIndexIsVisible(index);
						addField(availableFieldsLst.getSelectedValue());
					}
				}
			};
		}
		
		availableFieldsLst.addMouseListener(addFieldMA);

	}

	public void addFormatTextListeners()
	{
		// action listener for add field button
		if (formatTextDL == null)
		{
			formatTextDL = new DocumentListener()
            {
                public void removeUpdate (DocumentEvent e) { changed(e); }
                public void insertUpdate (DocumentEvent e) { changed(e); }
                public void changedUpdate(DocumentEvent e) { changed(e); }

                private void changed(DocumentEvent e)      { formatChanged(); }
            };
		}
		formatText.getDocument().addDocumentListener(formatTextDL);

		if (formatTextCL == null)
		{
			formatTextCL = new CaretListener()
			{
				public void caretUpdate(CaretEvent e)
				{
					// try to detect whether insert position on format text area is inside a valid field marker
					// if so, select the whole field to prevent user from changing the field name
					FieldDefinitionLocation loc = new FieldDefinitionLocation();
					if (loc.findFieldDefinitionLocation(e))
					{
						// caret is inside a field definition, so change selection to encompass field def
						// be careful to remove caret listener to avoid infinite recursion
						formatText.removeCaretListener(formatTextCL);
						formatText.setSelectionStart(loc.getBegin());
						formatText.setSelectionEnd(loc.getEnd());
						formatText.addCaretListener(formatTextCL);
					}
				}
			};
		}
		formatText.addCaretListener(formatTextCL);
	}
	
	/*
	 * Check whether newly typed format already exists on format list or not
	 * Change formatList selection accordingly
	 */
	protected void formatChanged()
	{
		if (formatList == null)
			return;
		
		// if new value cannot be found among those listed, then it's a new one
		DefaultListModel listModel = (DefaultListModel) formatList.getModel();
		Enumeration<?> elements = listModel.elements();
		int i = 0;
		int index = -1;
		while (elements.hasMoreElements())
		{
			Object obj = elements.nextElement();
			if (obj instanceof DataObjSwitchFormatter)
			{
				DataObjSwitchFormatter fmt = (DataObjSwitchFormatter) obj;
				if (formatText.getText().equals(fmt.toString()))
				{
					index = i;
					break;
				}
			}
			++i;
		}
		
	
		if (index == -1) {
			// it's a new format: change index to last value (i.e. "New")
			index = formatList.getModel().getSize() - 1;
		}
		
		removeEditorListeners();
		formatList.removeListSelectionListener(formatListSL);
		formatList.setSelectedIndex(index);
		formatList.addListSelectionListener(formatListSL);
		addEditorListeners();
	}

	/*
	 * Removes all editors listeners (so that format list selection can be updated without side-effects
	 */
	public void removeEditorListeners()
	{
		formatText.removeCaretListener(formatTextCL);
		formatText.getDocument().removeDocumentListener(formatTextDL);
	}
	
	/*
	 * Adds all editors listeners (after calling removeEditorListeners)
	 */
	public void addEditorListeners()
	{
		formatText.addCaretListener(formatTextCL);
		formatText.getDocument().addDocumentListener(formatTextDL);
	}

	/*
	 * Adds a field to the format being composed
	 */
	public void addField(Object selectedValue)
	{
		if (selectedValue == null || selectedValue instanceof String)
			// not really a field that can be added, just empty or a string
			return;
		
		formatText.insert(selectedValue.toString(), formatText.getCaretPosition());
	}
	
	public void fillWithObjFormatter(DataObjSwitchFormatter switchFormatter)
	{
		if (switchFormatter != null)
		{
			fillWithObjFormatter(switchFormatter.getSingle());
		}
		else
		{
			fillWithObjFormatter((DataObjDataFieldFormatIFace) null);
		}
	}
	
	protected void fillWithObjFormatter(DataObjDataFieldFormatIFace singleFormatter)
	{
		removeEditorListeners();
		if (singleFormatter != null)
		{
			formatText.setText(singleFormatter.toString());
			setSelectedFormat(singleFormatter);
		}
		else
		{
			formatText.setText("");
			setSelectedFormat(null);
		}
		addEditorListeners();
		
		// set 2nd list box with all available fields
		DefaultListModel listModel = (DefaultListModel) availableFieldsLst.getModel();
		listModel.removeAllElements();
		fieldHash.clear();
		
		// fields from this table
		listModel.addElement("---- " + tableInfo.getTitle() + " ------");
		for (DBFieldInfo field : tableInfo.getFields()) 
		{
			DBFieldInfoWrapper fieldWrapper = new DBFieldInfoWrapper(null, tableInfo, field);
			listModel.addElement(fieldWrapper);
			fieldHash.put(fieldWrapper.toString(), fieldWrapper);
		}
		
		// get fields from relationship tables and their formatters (if any)
		for (DBRelationshipInfo rel : tableInfo.getRelationships())
		{
			// separator for this relationship
			listModel.addElement("---- " + rel.getTitle() + " ------");

			// get formatters
			List<DataObjSwitchFormatter> formatters;
			formatters = DataObjFieldFormatMgr.getFormatterList(rel.getDataClass());
			for (DataObjSwitchFormatter formatter : formatters)
			{
				FormatWrapper formatWrapper = new FormatWrapper(formatter);
				listModel.addElement(formatWrapper);
				fieldHash.put(formatWrapper.toString(), formatWrapper);
			}
			
			// get relationship table fields
			DBTableInfo relTable = DBTableIdMgr.getInstance().getByClassName(rel.getClassName());
			for (DBFieldInfo relField : relTable.getFields()) 
			{
				DBFieldInfoWrapper fieldWrapper = new DBFieldInfoWrapper(rel, relTable, relField);
				listModel.addElement(fieldWrapper);
				fieldHash.put(fieldWrapper.toString(), fieldWrapper);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#getSwitchFormatter()
	 */
	public DataObjSwitchFormatter getSwitchFormatter()
	{
		// check whether format selected is an existing one
		if (formatList != null && formatList.getSelectedIndex() < formatList.getModel().getSize() - 1)
		{
			// selected entry is not the last one, so it's an existing one
			// just return it
			newFormat = false;
			Object value = formatList.getSelectedValue(); 
			if (value instanceof DataObjSwitchFormatter)
				return (DataObjSwitchFormatter) value;
			
			// should never get here... if we do, then I'm not sure what we have here :(
			return null;
		}
		
		// last entry is selected, so return the new formatter that has been composed
		newFormat = true;
		return createFormatterFromFormatText();
	}
	
	protected DataObjSwitchFormatter createFormatterFromFormatText()
	{
		String text = formatText.getText();
		
    	Pattern splitPattern = Pattern.compile("([^\\[]*\\[[^\\]]+\\])");
    	Matcher matcher = splitPattern.matcher(text);

		Vector<DataObjDataField> fields = new Vector<DataObjDataField>();
    	while (matcher.find()) 
    	{
    		int mStart   = matcher.start();
    		int mEnd     = matcher.end();
    		String group = matcher.group();
    		
    		int openBracketPos = text.indexOf('[', matcher.start());
    		String sepStr   = text.substring(matcher.start(), openBracketPos);
    		String fieldStr = text.substring(openBracketPos, matcher.end());
    		
    		Object value = fieldHash.get(fieldStr);
    		if (value != null)
    		{
    			String fieldName = "";
    			String formatStr = "";
    			String formatterName = "";    // if another formatter has been used, use it's name here
    			String uifieldformatter = ""; // not implemented yet

    			if (value instanceof DBFieldInfoWrapper)
    			{
    				DBFieldInfoWrapper fieldWrapper = (DBFieldInfoWrapper) value;
        			fieldName     = fieldWrapper.getFieldInfo().getName();
    				formatterName = "";
    			}
    			else if (value instanceof DataObjSwitchFormatter)
    			{
    				DataObjSwitchFormatter fmt = (DataObjSwitchFormatter) value;
        			fieldName     = fmt.getFieldName();
    				formatterName = fmt.getName();
    			}
    				
    			Class<?> classObj = null;
    			fields.add(new DataObjDataField(fieldName, classObj, formatStr, sepStr, "", uifieldformatter));
    			
    			// separator has been used up, so clean it for next matches 
    			sepStr = "";
    		}
    		else
    		{
    			// what's enclosed in brackets is not a field definition, so copy it verbatim as separator
    			sepStr = sepStr + fieldStr;
    		}
    		
    		// move begin pointer to end of current match
    		//begin = matcher.end();
       	}

    	// XXX: what do we do with the remaining of the text? right now we ignore it
    	DataObjDataField[] fieldsArray = new DataObjDataField[fields.size()];
    	for (int i = 0; i < fields.size(); ++i)
    	{
    		fieldsArray[i] = (DataObjDataField) fields.elementAt(i); 
    	}
    	
    	DataObjDataFieldFormat newFormatter = new DataObjDataFieldFormat("", tableInfo.getClassObj(), false, "", "", fieldsArray);
    	DataObjSwitchFormatter newSwitchFmt = new DataObjSwitchFormatter("", true, false, tableInfo.getClassObj(), "");
    	newFormatter.setTableAndFieldInfo();
    	newSwitchFmt.add(newFormatter);
		return newSwitchFmt;
	}
	
	/*
	 * Wrapper for formatters.
	 * Created to modify toString() method and display item nicely on JList. 
	 */
	public class FormatWrapper
	{
		protected DataObjSwitchFormatter formatter;
		
		FormatWrapper(DataObjSwitchFormatter formatter)
		{
			this.formatter = formatter; 
		}
		
		public DataObjSwitchFormatter getFormatter()
		{
			return formatter;
		}
		
		public String toString()
		{
			return formatter.toString();
		}
	}
	
	/*
	 * Wrapper for db fields. 
	 * Ceated to modify toString() method and display item nicely on JList 
	 */
	public class DBFieldInfoWrapper
	{
		protected DBRelationshipInfo relationshipInfo;
		protected DBTableInfo        tableInfo;
		protected DBFieldInfo		 fieldInfo;
		
		DBFieldInfoWrapper(DBRelationshipInfo relationshipInfo,
				           DBTableInfo        tableInfo,
				           DBFieldInfo		  fieldInfo)
		{
			this.relationshipInfo = relationshipInfo;
			this.tableInfo        = tableInfo;
			this.fieldInfo        = fieldInfo;
		}

		public DBRelationshipInfo getRelationshipInfo() 
		{
			return relationshipInfo;
		}

		public DBTableInfo getTableInfo() 
		{
			return tableInfo;
		}

		public DBFieldInfo getFieldInfo() 
		{
			return fieldInfo;
		}
		
		public String toString()
		{
			String result = (relationshipInfo != null)? relationshipInfo.getTitle() : tableInfo.getTitle();    
			return "[" + result + "." + fieldInfo.getTitle() + "]";
		}
	}
	
	/*
	 * Wrapper class for return value of method that indicates whether text field 
	 * caret is inside a field definition or not  
	 */
	public class FieldDefinitionLocation
	{
		protected String fieldName;
		protected int begin = -1;
		protected int end   = -1;
		
		public FieldDefinitionLocation()
		{
		}
		
		public String getFieldName() {
			return fieldName;
		}

		public int getBegin() {
			return begin;
		}

		public int getEnd() {
			return end;
		}
		
		public boolean findFieldDefinitionLocation(CaretEvent e)
		{
			if (!(e.getSource() instanceof JTextComponent))
				return false;

			JTextComponent comp = (JTextComponent) e.getSource();
			
			if (e.getDot() == e.getMark())
			{
				// dot and mark are the same, i.e., selection is empty
				return findFieldDefinitionLocationInternal(comp, e.getDot());
			}
			
			// else
			int beginPos = (e.getDot() < e.getMark())? e.getDot() : e.getMark(); 
			int endPos   = (e.getDot() > e.getMark())? e.getDot() : e.getMark();
			
			FieldDefinitionLocation locBegin = new FieldDefinitionLocation();
			FieldDefinitionLocation locEnd   = new FieldDefinitionLocation();

			boolean resBegin = locBegin.findFieldDefinitionLocationInternal(comp, beginPos);
			boolean resEnd   = locEnd.findFieldDefinitionLocationInternal(comp, endPos);

			if (resBegin || resEnd)
			{
				this.begin = resBegin ? locBegin.getBegin() : beginPos;
				this.end   = resEnd   ? locEnd.getEnd()     : endPos;
				return true;
			}

			return false;
		}
		
		protected boolean findFieldDefinitionLocationInternal(JTextComponent source, int caretPos)
		{
			// try to detect whether insert position on format text area is inside a valid field marker
			// if so, select the whole field to prevent user from changing the field name
			String text = source.getText();

			int openBracketAfterPos   = text.indexOf    ('[', caretPos); // first [ after caret
			int openBracketBeforePos  = text.lastIndexOf('[', caretPos - 1); // first [ before caret
			int closeBracketAfterPos  = text.indexOf    (']', caretPos); // first ] after caret
			int closeBracketBeforePos = text.lastIndexOf(']', caretPos - 1); // first ] before caret

			/*
			//debugging code
			char[] sample = (text + " ").toCharArray();
			if (openBracketAfterPos   >= 0) sample[openBracketAfterPos]   = '[';
			if (openBracketBeforePos  >= 0) sample[openBracketBeforePos]  = '[';
			if (closeBracketAfterPos  >= 0) sample[closeBracketAfterPos]  = ']';
			if (closeBracketBeforePos >= 0) sample[closeBracketBeforePos] = ']';
			if (caretPos >= 0)              sample[caretPos]              = '|';
			System.out.println(new String(sample) + " Caret Pos: " + caretPos);
*/			

			// adjust position of brackets after caret if that's not found to point to after the end of the string
			openBracketAfterPos   = (openBracketAfterPos   >= 0)? openBracketAfterPos   : text.length();
			
			// check if caret is inside or outside a field definition: 
			// Inside:  [...][  |  ][...]
			// Outside: [...]   |   [...]
			if (openBracketBeforePos == -1 || closeBracketAfterPos == -1 ||
				closeBracketAfterPos > openBracketAfterPos ||
				openBracketBeforePos < closeBracketBeforePos)
			{
				// caret is outside a field definition, so bail out
				return false;
			} 
			
			String fieldDefStr = (String) text.subSequence(openBracketBeforePos, closeBracketAfterPos + 1);
			if (fieldHash.containsKey(fieldDefStr))
			{
				fieldName = fieldDefStr;
				begin = openBracketBeforePos;
				end   = closeBracketAfterPos + 1;
				return true;
			}
			
			return false;
		}
	}
}
