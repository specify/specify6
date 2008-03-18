package edu.ku.brc.ui.forms.formatters;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.List;

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
import javax.swing.text.JTextComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;

public class DataObjFieldFormatSinglePanelBuilder extends DataObjFieldFormatPanelBuilder {
	
	// ui controls
	protected JTextArea formatText;
	protected JList availableFieldsLst;
	protected JButton addFieldBtn;
	
	protected DocumentListener formatTextDL;
	protected CaretListener formatTextCL;
	protected ActionListener addFieldAL;
	protected MouseAdapter addFieldMA;
	
	// hash mapping fields or formatters that go inside the available field list
	protected Hashtable<String, Object> fieldHash = new Hashtable<String, Object>();

	DataObjFieldFormatSinglePanelBuilder(DBTableInfo tableInfo)
	{
		super(tableInfo);
	}
	
/*	Old version of buildUI() using list box
 
    protected void buildUI() {
	    CellConstraints cc = new CellConstraints();
	    PanelBuilder pb = new PanelBuilder(new FormLayout("f:100px:g,5px,p,5px,f:p:g",  
	    		"10px,"       + // empty space on top of panel 
	    		"p,2px,"      + // row + small separator 
	    		"p,2px,"      + // row + small separator 
	    		"p,2px,"      + // row + small separator 
	    		"p,2px,"      + // row + small separator 
	    		"p,2px,"      + // row + small separator 
	    		"p,0px:g,"    + // row + small separator
	    		"10px,21px,15px"  // empty space where the delete button goes for the multiple value panel
	    		
	    		), new FormDebugPanel());
	    
	    JLabel currentFieldsLbl = new JLabel("Fields:");
	    JLabel availableFieldsLbl = new JLabel("Available Fields:");
	
	    fieldsLst = new JList(new DefaultListModel());
	
	    DefaultListModel availableFieldListModel = new DefaultListModel();
	    availableFieldsLst = new JList(availableFieldListModel);
	
	    JButton includeBtn  = new JButton("<= Include");
	    JButton excludeBtn  = new JButton("=> Exclude");
	    JButton moveUpBtn   = new JButton("^ Move Up");
	    JButton moveDownBtn = new JButton("v Move Down");
	    JButton addSepBtn   = new JButton("Add Separator");
	    
	    // lay out components on main panel        
	    int y = 2; // leave first row blank 
	    pb.add(currentFieldsLbl,            cc.xy  (1, y)); y += 2;
	    pb.add(new JScrollPane(fieldsLst),  cc.xywh(1, y, 1, 10)); y += 2;
	
	    y = 4; // start from the top to fill 2nd column
	    pb.add(includeBtn,                  cc.xy(3, y)); y += 2;
	    pb.add(excludeBtn,                  cc.xy(3, y)); y += 2;
	    pb.add(moveUpBtn,                   cc.xy(3, y)); y += 2;
	    pb.add(moveDownBtn,                 cc.xy(3, y)); y += 2;
	    pb.add(addSepBtn,                   cc.xy(3, y)); y += 2;
	    
	    y = 2;
	    pb.add(availableFieldsLbl,                  cc.xy  (5, y)); y += 2;
	    pb.add(new JScrollPane(availableFieldsLst), cc.xywh(5, y, 1, 10)); y += 2;
	    
	    this.mainPanelBuilder = pb;
	}
*/
	
	/*
	 * 
	 */
	protected void buildUI() {
	    CellConstraints cc = new CellConstraints();
	    PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g",  
	    		"10px,"       + // empty space on top of panel 
	    		"p,p,"        + // Label & text box for format
	    		"10px,p,"     + // separator & label
	    		"f:150px:g,"  + // list box for available fields 
	    		"10px,21px,15px"  // empty space where the delete button goes for the multiple value panel
	    		
	    		)/*, new FormDebugPanel()*/);
	    
	    JLabel currentFieldsLbl = new JLabel("Display Format:");
	    formatText = new JTextArea(4, 50);
	    formatText.setLineWrap(true);

        PanelBuilder addFieldPB = new PanelBuilder(new FormLayout("l:m:g,r:m", "p"));  
	    JLabel availableFieldsLbl = new JLabel("Available Fields:");
        addFieldBtn = new JButton("Add Field ^"); 
        addFieldBtn.setEnabled(true);
	    addFieldPB.add(availableFieldsLbl, cc.xy(1,1));
	    addFieldPB.add(addFieldBtn, cc.xy(2,1));

	    DefaultListModel availableFieldListModel = new DefaultListModel();
	    availableFieldsLst = new JList(availableFieldListModel);
	
	    // lay out components on main panel        
	    int y = 2; // leave first row blank 
	    pb.add(currentFieldsLbl,   cc.xy(1, y)); y += 1;
	    pb.add(formatText,         cc.xy(1, y)); y += 2;
	
	    pb.add(addFieldPB.getPanel(), cc.xy(1, y)); y += 1;
	    pb.add(new JScrollPane(availableFieldsLst), cc.xy(1, y)); y += 2;
	    
	    this.mainPanelBuilder = pb;

	    hookupFormatTextListeners();
	    // must be called after list of available fields has been created
	    hookupAddFieldListeners();
	}

	public void hookupAddFieldListeners()
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

	public void hookupFormatTextListeners()
	{
		// action listener for add field button
		if (formatTextDL == null)
		{
			formatTextDL = new DocumentListener()
            {
                public void removeUpdate (DocumentEvent e) { changed(e); }
                public void insertUpdate (DocumentEvent e) { changed(e); }
                public void changedUpdate(DocumentEvent e) { changed(e); }

                private void changed(DocumentEvent e)
                {
                }            	
            };
		}
		formatText.getDocument().addDocumentListener(formatTextDL);

		if (1 == 1 || formatTextCL == null)
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
	 * Adds a field to the format being composed
	 */
	public void addField(Object selectedValue)
	{
		if (selectedValue instanceof String)
			// not really a field that can be added, just a string
			return;
		
		formatText.insert(selectedValue.toString(), formatText.getCaretPosition());
	}
	
	public void fillWithObjFormatter(DataObjSwitchFormatter switchFormatter)
	{
		if (switchFormatter == null)
		{
			formatText.setText("");
			return;
		}
		
		formatText.setText(switchFormatter.toString());
		
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

			//debugging code
			char[] sample = (text + " ").toCharArray();
			if (openBracketAfterPos   >= 0) sample[openBracketAfterPos]   = '[';
			if (openBracketBeforePos  >= 0) sample[openBracketBeforePos]  = '[';
			if (closeBracketAfterPos  >= 0) sample[closeBracketAfterPos]  = ']';
			if (closeBracketBeforePos >= 0) sample[closeBracketBeforePos] = ']';
			if (caretPos >= 0)              sample[caretPos]              = '|';
			System.out.println(new String(sample) + " Caret Pos: " + caretPos);
			

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
