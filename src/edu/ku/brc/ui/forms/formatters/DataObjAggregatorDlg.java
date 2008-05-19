package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createList;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.ComparatorByStringRepresentation;

public class DataObjAggregatorDlg extends CustomDialog {
	protected Frame 					aggDlgFrame; 
	protected DBTableInfo 				tableInfo;
	protected DataObjAggregator 		selectedAggregator;
	protected boolean 					newAggregator;
    protected Vector<DataObjAggregator> deletedFormats = new Vector<DataObjAggregator>(); 
	
	// UI controls
	protected JList aggragatorList;
	protected JComboBox displayCbo;
	protected JComboBox fieldOrderCbo;
	protected JTextField sepText;
	protected JTextField endingText;
	protected JTextField countText; 
	protected JCheckBox defaultCheck;
	
	// listeners
	protected ListSelectionListener aggregatorListSL = null;
	protected DocumentListener formatChangedDL = null;
	protected ItemListener checkBoxListener = null;
	protected ActionListener cboAL = null;


    /**
     * @throws HeadlessException
     */
    public DataObjAggregatorDlg(Frame                 frame, 
    					  		DBTableInfo           tableInfo, 
    					  		int                   initialFormatSelectionIndex) 
    	throws HeadlessException
    {
        super(frame, getResourceString("DOA_DLG_TITLE"), true, OKCANCELHELP, null); //I18N 
        this.tableInfo = tableInfo;
        this.aggDlgFrame = frame;  // save it for when another dialog is created
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("10px,250px,10px,f:p:g,10px",  
        		"10px,"       + // empty space on top of panel 
        		"p,"          + // table name 
        		"10px,"       + // --- separator 
        		"p,"          + // help 
        		"10px,"       + // --- separator 
        		"p,"          + // available aggregators label,
        		"f:150px:g,"  + // available aggregators list 
        		"10px,"       + // --- separator 
        		"p,"          + // delete button 
        		"15px"          // --- separator 
        		)/*, new FormDebugPanel()*/);
        
        // table info
        JLabel tableTitleLbl = createLabel(getResourceString("FFE_TABLE") + ": " + 
        		tableInfo.getTitle(), SwingConstants.LEFT); 

        JLabel helpLbl = createLabel(getResourceString("DOA_HELP"), SwingConstants.LEFT);

        // list of existing formats
        DefaultListModel listModel = new DefaultListModel();

        // add available data object formatters
        List<DataObjAggregator> aggs;
        aggs = DataObjFieldFormatMgr.getAggregatorList(tableInfo.getClassObj());
        Collections.sort(aggs, new ComparatorByStringRepresentation<DataObjAggregator>()); 
        for (DataObjAggregator agg : aggs)
        {
        	listModel.addElement(agg);
        }
        // add "New" string as last entry
        listModel.addElement(getResourceString("DOA_NEW"));
        
        aggragatorList = createList(listModel);
        aggragatorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addAggregatorListSelectionListener();
        addAggregatorListMouseListener();
        
        ActionListener deleteListener = new ActionListener()
        {
        	public void actionPerformed(ActionEvent e) 
        	{ 	
        		int index = aggragatorList.getSelectedIndex();
        		DefaultListModel model = (DefaultListModel) aggragatorList.getModel();
        		model.removeElement(selectedAggregator);
        		index = (index >= model.getSize())? index - 1: index;
        		aggragatorList.setSelectedIndex(index);
        		deletedFormats.add(selectedAggregator);
        	}
        };
        
        // delete button
        PanelBuilder deletePB = new PanelBuilder(new FormLayout("l:p", "p"));  
        JButton deleteBtn = createButton(getResourceString("FFE_DELETE")); 
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(deleteListener);
        deletePB.add(deleteBtn);
        
        // panel for aggregator editing panel
        PanelBuilder rightPB = new PanelBuilder(new FormLayout("l:p,2px,f:p:g", 
        		"20px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", 6))/*, new FormDebugPanel()*/);

        // display combo box with available data obj formatters
        PanelBuilder displayPB = new PanelBuilder(new FormLayout("f:p:g,min", "p")/*, new FormDebugPanel()*/);

        displayCbo = createComboBox();
        JButton displayDlgBtn = createButton("...");
        displayPB.add(displayCbo,    cc.xy(1, 1));
        displayPB.add(displayDlgBtn, cc.xy(2, 1));
        
        fieldOrderCbo = createComboBox();
        
        ActionListener displayDlgBtnAL = new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		// subtract 1 from selected index to account for empty entry at the beginning
        		// if selected index is zero, then select "new" entry in the dialog, which is the last one
        		int correctIndex = (displayCbo.getSelectedIndex() == 0)? displayCbo.getModel().getSize() - 1 : displayCbo.getSelectedIndex() - 1; 
        		DataObjFieldFormatDlg dlg = new DataObjFieldFormatDlg(aggDlgFrame, tableInfo, correctIndex);
        		dlg.setVisible(true);
        		
        		// set combo selection to formatter selected in dialog
        		if (dlg.getBtnPressed() == OK_BTN)
        		{
	        		DataObjSwitchFormatter format = dlg.getSelectedFormatter();
	        		selectedAggregator.setFormatName(format.getName());
	        		updateDisplayCombo();
        		}
        	}
        };
        displayDlgBtn.addActionListener(displayDlgBtnAL);
        
        // text fields
        sepText    = createTextField(10);
        endingText = createTextField(10);
        countText  = createTextField(10); 

        // checkbox
        defaultCheck = createCheckBox(getResourceString("DOA_DEFAULT"));
        addEditorListeners();
        
        int yr = 2; // leave blank on top
        rightPB.add(createLabel(getResourceString("DOA_DISPLAY")+":"), cc.xy(1, yr)); 
        rightPB.add(displayPB.getPanel(), cc.xy(3, yr)); 
        yr += 2;
        
        rightPB.add(createLabel(getResourceString("DOA_SEP")+":"), cc.xy(1, yr)); 
        rightPB.add(sepText, cc.xy(3, yr)); 
        yr += 2;

        rightPB.add(createLabel(getResourceString("DOA_ENDING")+":"), cc.xy(1, yr));
        rightPB.add(endingText, cc.xy(3, yr)); 
        yr += 2;

        rightPB.add(createLabel(getResourceString("DOA_SORT_BY")+":"), cc.xy(1, yr)); 
        rightPB.add(fieldOrderCbo, cc.xy(3, yr)); 
        yr += 2;

        rightPB.add(createLabel(getResourceString("DOA_COUNT")+":"), cc.xy(1, yr)); 
        rightPB.add(countText, cc.xy(3, yr)); 
        yr += 2;
        
        rightPB.add(defaultCheck, cc.xy(3, yr)); yr += 2;
        
        // lay out components on main panel        
        int y = 2; // leave first row blank 
        pb.add(tableTitleLbl,  cc.xyw(2, y, 3)); y += 2;
        pb.add(helpLbl,        cc.xyw(2, y, 3)); y += 2;
        
        pb.add(createLabel(getResourceString("DOA_DISPLAY_FORMATS")+":", SwingConstants.LEFT), cc.xy(2, y)); y += 1; 
        int y2 = y; // align 3rd column with this row 
        pb.add(new JScrollPane(aggragatorList), cc.xy(2,y)); y += 2;

        pb.add(deletePB.getPanel(), cc.xy(2,y)); y += 2;
        
        // 2nd column (4th if you count the spaces between them)
        pb.add(rightPB.getPanel(), cc.xywh(4, y2, 1, 4));

        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // after all is created, set initial selection on format list 
        //formatList.setSelectedIndex(initialFormatSelectionIndex);
        if (aggs.size() > 0)
        {
            selectedAggregator = aggs.get(0);
            fillWithObjAggregator(selectedAggregator);
        }
        else 
        {
        	fillWithObjAggregator(null);
        }

    	updateUIEnabled();

        pack();
    }

    private void addEditorListeners()
    {
    	addComboBoxActionListeners();
        addTextFieldChangeListeners();
        addCheckBoxListener();
    }

    private void removeEditorListeners()
    {
        displayCbo   .removeActionListener(cboAL);
        fieldOrderCbo.removeActionListener(cboAL);
    	
        sepText.getDocument()   .removeDocumentListener(formatChangedDL);
    	endingText.getDocument().removeDocumentListener(formatChangedDL);
    	countText.getDocument() .removeDocumentListener(formatChangedDL);
    	
    	defaultCheck.removeItemListener(checkBoxListener);
    }

    private void addComboBoxActionListeners()
    {
    	if (cboAL == null)
    	{
    		cboAL = new ActionListener()
    		{
    			public void actionPerformed(ActionEvent e)
    			{
    				aggregatorChanged();
    			}
    		};
    	}
    	
        displayCbo.addActionListener(cboAL);
        fieldOrderCbo.addActionListener(cboAL);
    }
    
    private void addCheckBoxListener()
    {
    	if (checkBoxListener == null)
    	{
    		checkBoxListener = new ItemListener()
	    	{
	    		public void itemStateChanged(ItemEvent e)
	    		{
	    			aggregatorChanged();
	    		}
	    	};
    	}
    	defaultCheck.addItemListener(checkBoxListener);
    }
    
    private void addTextFieldChangeListeners()
    {
    	if (formatChangedDL == null)
    	{
            formatChangedDL = new DocumentListener() 
            {
                public void removeUpdate(DocumentEvent e)  { changed(e); }
                public void insertUpdate(DocumentEvent e)  { changed(e); }
                public void changedUpdate(DocumentEvent e) { changed(e); }
                
                private void changed(DocumentEvent e)
                {
                	aggregatorChanged();
                }            	
            };

    	}
    	sepText.getDocument().addDocumentListener(formatChangedDL);
    	endingText.getDocument().addDocumentListener(formatChangedDL);
    	countText.getDocument().addDocumentListener(formatChangedDL);
    }
    
    /*
     * Populates the display format combo with available formatters for this table 
     */
    protected void updateDisplayCombo()
    {
    	// save selected aggregator because that one will be reset when elements are removed from combo box model
    	DataObjAggregator tempAgg = selectedAggregator;
    	
    	// clear combo box list
    	DefaultComboBoxModel cboModel = (DefaultComboBoxModel) displayCbo.getModel();
    	cboModel.removeAllElements();
    	
    	// add formatters to display combo box
    	List<DataObjSwitchFormatter> fmts = DataObjFieldFormatMgr.getFormatterList(tableInfo.getClassObj());

    	// add an empty entry at the beginning so the user can clear the selection if he wants to
    	cboModel.addElement("");

    	if (fmts.size() == 0)
    		return;

    	int selectedFieldIndex = 0;
    	for (int i = 0; i < fmts.size(); ++i)
    	{
    		DataObjSwitchFormatter currentFormat = fmts.get(i);
    		cboModel.addElement(currentFormat);
    		if (tempAgg != null && 
    			currentFormat.getName().equals(tempAgg.getFormatName()))
    		{
    			// found the selected field
    			// current combo index is (i+1) because of empty entry at the beginning
    			selectedFieldIndex = i + 1; 
    		}
    	}
    	
    	// set selected field
    	displayCbo.setSelectedIndex(selectedFieldIndex);
    }
    
    /*
     * Populates the field value combo with fields and leaves the right one selected
     */
    protected void updateFieldValueCombo()
    {
    	// clear combo box list
    	DefaultComboBoxModel cboModel = (DefaultComboBoxModel) fieldOrderCbo.getModel();
    	cboModel.removeAllElements();
    	
    	// add an empty entry at the beginning so the user can clear the selection if he wants to
    	cboModel.addElement("");
    	
    	// add fields to combo box
    	List<DBFieldInfo> fields = tableInfo.getFields();
    	int selectedFieldIndex = 0;
    	for (int i = 0; i < fields.size(); ++i)
    	{
    		DBFieldInfo currentField = fields.get(i);
    		cboModel.addElement(currentField);
    		if (selectedAggregator != null && 
    			currentField.getName().equals(selectedAggregator.getOrderFieldName()))
    		{
    			// found the selected field
    			// current combo index is (i+1) because of empty entry at the beginning
    			selectedFieldIndex = i + 1;
    		}
    	}
    	
    	// set selected field
    	fieldOrderCbo.setSelectedIndex(selectedFieldIndex);
    }

    /*
     * Populates the dialog controls with data from a given formatter
     */
    protected void fillWithObjAggregator(final DataObjAggregator aggArg)
    {
        DataObjAggregator agg = aggArg;
        
    	newAggregator = false;
    	if (agg == null)
    	{
    		agg = new DataObjAggregator();
    		agg.setDataClass(tableInfo.getClassObj());
    		newAggregator = true;
    	}
    	selectedAggregator = agg;
    	
    	defaultCheck.setSelected(agg.isDefault());
    	sepText.setText(agg.getSeparator());
    	endingText.setText(agg.getEnding());
    	if (agg.getCount() != null)
    	{
    		countText.setText(agg.getCount().toString());
    	}
    	else 
    	{
    		countText.setText("");
    	}
    	
    	updateDisplayCombo();
    	updateFieldValueCombo();
    	updateUIEnabled();
    }
    
    /**
     * 
     */
    private void addAggregatorListMouseListener()
    {
    	MouseAdapter mAdp = new MouseAdapter()
    	{
    		public void mouseClicked(MouseEvent e)
    		{
    			if(e.getClickCount() == 2)
    			{
    				int index = aggragatorList.locationToIndex(e.getPoint());
    				aggragatorList.ensureIndexIsVisible(index);
    				okButtonPressed();
    			}
    		}
    	};
    	
    	aggragatorList.addMouseListener(mAdp);
    }
    
	/**
	 * 
	 */
	private void addAggregatorListSelectionListener() 
	{
		if (aggregatorListSL == null)
		{
	        aggregatorListSL = new ListSelectionListener()
	        {
	        	public void valueChanged(ListSelectionEvent e)
	        	{
	        	    if (e.getValueIsAdjusting()) 
	        	    	return;
	
	        	    JList theList = (JList) e.getSource();
	        	    if (theList.isSelectionEmpty())
	        	    {
	        	    	return;
	        	    }
	
        	    	Object selValue = theList.getSelectedValue();
	        	    if (selValue instanceof DataObjAggregator)
	        	    {
	        	    	setSelectedFormat((DataObjAggregator) selValue);
	        	    }
	        	    else
	        	    {
	        	    	// set selected formatter to null
	        			// but detach selection listeners from formatList before that
	        			// and attach listeners again once selection is changed
	        	    	setSelectedFormat(null);
	        	    }
	        	    
	        	    updateUIEnabled();
	        	}
	        };
		}
		
        aggragatorList.addListSelectionListener(aggregatorListSL);
	}

	/**
	 * @param agg the aggregator
	 */
	protected void setSelectedFormat(final DataObjAggregator agg)
	{
		// remove listeners from editor fields so that we don't end up in a cycle
		removeEditorListeners();
		fillWithObjAggregator(agg);
		addEditorListeners();
	}
	
	protected void aggregatorChanged()
	{
		DataObjAggregator tempAgg = new DataObjAggregator();
		
		String formatName = "";
		Object formatObj = displayCbo.getSelectedItem();
		if (formatObj instanceof DataObjSwitchFormatter)
		{
			formatName = ((DataObjSwitchFormatter) formatObj).getName();
		}
		
		String orderFieldName = "";
		Object orderFieldObj = fieldOrderCbo.getSelectedItem();
		if (orderFieldObj instanceof DBFieldInfo)
		{
			orderFieldName = ((DBFieldInfo) orderFieldObj).getName();
		}

		tempAgg.setFormatName(formatName);
		tempAgg.setDataClass(tableInfo.getClassObj());
		tempAgg.setOrderFieldName(orderFieldName);
		tempAgg.setSeparator(sepText.getText());
		tempAgg.setEnding(endingText.getText());
		tempAgg.setDefault(defaultCheck.isSelected());

		if (StringUtils.isNotEmpty(countText.getText()))
		{
			tempAgg.setCount(Integer.valueOf(countText.getText()));
		}

		// if new value cannot be found among those listed, then it's a new one
		DefaultListModel listModel = (DefaultListModel) aggragatorList.getModel();
		Enumeration<?> elements = listModel.elements();
		int i = 0;
		int index = -1;
		while (elements.hasMoreElements())
		{
			Object obj = elements.nextElement();
			if (obj instanceof DataObjAggregator)
			{
				DataObjAggregator agg = (DataObjAggregator) obj;
				if (tempAgg.toString().equals(agg.toString()))
				{
					index = i;
					selectedAggregator = agg;
					break;
				}
			}
			++i;
		}

		if (index == -1) {
			// it's a new format: change index to last value (i.e. "New")
			index = aggragatorList.getModel().getSize() - 1;
			selectedAggregator = tempAgg;
		} 

		// detach selection listeners from formatList, change value to New (last on the list)
		// and attach listeners again
    	aggragatorList.removeListSelectionListener(aggregatorListSL);
    	aggragatorList.setSelectedIndex(index);
    	aggragatorList.addListSelectionListener(aggregatorListSL);
	}

    public DataObjAggregator getSelectedAggregator()
    {
    	return selectedAggregator;
    }

    public int getSelectedAggregatorIndex()
    {
    	return aggragatorList.getSelectedIndex();
    }

    public boolean isNewAggregator()
    {
    	return newAggregator;
    }

    /**
     * 
     */
    protected void updateUIEnabled()
    {
    	// TODO: determine when the delete button is to be enabled 
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
    	DataObjFieldFormatMgr instance = DataObjFieldFormatMgr.getInstance();

    	// XXX delete button not implemented yet
    	// not sure what can be deleted
    	// maybe we should add a "system" flag to the formats just like UIFieldFormatters

    	/*
    	// remove non-system formatters marked for deletion
    	Iterator<UIFieldFormatterIFace> it = deletedFormats.iterator();
    	while (it.hasNext()) 
    	{
    		instance.removeFormatter(it.next());
    	}
    	*/
    	
    	// save formatter if new
    	// save formatter anyway if it is a multiple switch formatter as its contents may have changed
    	// without affecting the format list selection
    	// that's because a multiple switch formatter is only considered new if its switch field is changed
    	// and not if just the internal formatters have changed
		if (aggragatorList.getSelectedIndex() == aggragatorList.getModel().getSize() - 1)
    	{
    		// add formatter to list of existing ones and save it
			selectedAggregator = getSelectedAggregator();
    		instance.addAggregator(selectedAggregator);
    		instance.save();
    	}

        super.okButtonPressed();
    }
}
