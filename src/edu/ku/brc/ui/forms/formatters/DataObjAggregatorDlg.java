package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createList;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import edu.ku.brc.ui.AddRemoveEditPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.ComparatorByStringRepresentation;

public class DataObjAggregatorDlg extends CustomDialog {
	protected Frame 					aggDlgFrame; 
	protected DBTableInfo 				tableInfo;
	protected DataObjAggregator 		selectedAggregator;
	protected int 						initialFormatSelectionIndex;
	protected boolean 					newAggregator;
    protected Vector<DataObjAggregator> deletedAggregators = new Vector<DataObjAggregator>();
    protected Set<String>				uniqueTitles = new HashSet<String>();
    protected DataObjFieldFormatMgr 	dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr		uiFieldFormatterMgrCache;
	
	// UI controls
	protected JList aggregatorList;
	protected JComboBox displayCbo;
	protected JComboBox fieldOrderCbo;
	protected JTextField titleText;
	protected JTextField sepText;
	protected JTextField countText; 
	protected JTextField endingText;
	protected JCheckBox defaultCheck;
	protected AddRemoveEditPanel controlPanel;

	
	// listeners
	protected DocumentListener[] textChangedDL = new DocumentListener[5];
	protected ListSelectionListener aggregatorListSL = null;
	protected ItemListener checkBoxListener = null;
	protected ActionListener cboAL = null;


    /**
     * @throws HeadlessException
     */
    public DataObjAggregatorDlg(Frame                 frame, 
    					  		DBTableInfo           tableInfo, 
    					  		int                   initialFormatSelectionIndex,
    					  		DataObjFieldFormatMgr dataObjFieldFormatMgrCache,
    					  		UIFieldFormatterMgr   uiFieldFormatterMgrCache) 
    	throws HeadlessException
    {
        super(frame, getResourceString("DOA_DLG_TITLE"), true, OKCANCELHELP, null); //I18N 
        this.tableInfo = tableInfo;
        this.aggDlgFrame = frame;  // save it for when another dialog is created
        this.initialFormatSelectionIndex = initialFormatSelectionIndex;
        this.dataObjFieldFormatMgrCache = dataObjFieldFormatMgrCache;
        this.uiFieldFormatterMgrCache = uiFieldFormatterMgrCache;
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
        PanelBuilder tblInfoPB = new PanelBuilder(new FormLayout("p,p", "p")/*, new FormDebugPanel()*/);
        JLabel tableTitleLbl      = createLabel(getResourceString("FFE_TABLE") + ": ");
        JLabel tableTitleValueLbl = createLabel(getResourceString(tableInfo.getTitle()));
        tableTitleValueLbl.setBackground(Color.WHITE);
        tableTitleValueLbl.setOpaque(true);
        
        tblInfoPB.add(tableTitleLbl,      cc.xy(1, 1));
        tblInfoPB.add(tableTitleValueLbl, cc.xy(2, 1));
        
        JLabel helpLbl = createLabel(getResourceString("DOA_HELP"), SwingConstants.LEFT);

        // add available data object formatters
        List<DataObjAggregator> aggs = populateAggregatorList();
        
        ActionListener addAL = new ActionListener()
        {
        	public void actionPerformed(ActionEvent e) 
        	{ 	
        		addNewAggregator();
        	}
        };
        
        ActionListener delAL = new ActionListener()
        {
        	public void actionPerformed(ActionEvent e) 
        	{
        		deleteSelectedAggregator();
        	}
        };
        
        // delete button
        controlPanel = new AddRemoveEditPanel(addAL, delAL, null);
        controlPanel.getAddBtn().setEnabled(true);
        
        // panel for aggregator editing controls
        PanelBuilder rightPB = new PanelBuilder(new FormLayout("10px,r:p,2px,f:p:g", 
        		"20px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", 7))/*, new FormDebugPanel()*/);

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
        		DataObjFieldFormatDlg dlg = new DataObjFieldFormatDlg(aggDlgFrame, tableInfo, correctIndex, 
        				dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache);
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
        titleText  = createTextField(10);
        sepText    = createTextField(10);
        endingText = createTextField(10);
        countText  = createTextField(10); 

        // checkbox
        defaultCheck = createCheckBox(getResourceString("DOA_DEFAULT"));
        addEditorListeners();
        
        int yr = 2; // leave blank on top
        rightPB.add(createLabel(getResourceString("DOA_TITLE")+":"), cc.xy(2, yr)); 
        rightPB.add(titleText, cc.xy(4, yr)); 
        yr += 2;

        rightPB.add(createLabel(getResourceString("DOA_DISPLAY")+":"), cc.xy(2, yr)); 
        rightPB.add(displayPB.getPanel(), cc.xy(4, yr)); 
        yr += 2;

        rightPB.add(createLabel(getResourceString("DOA_SEP")+":"), cc.xy(2, yr)); 
        rightPB.add(sepText, cc.xy(4, yr)); 
        yr += 2;

        rightPB.add(createLabel(getResourceString("DOA_COUNT")+":"), cc.xy(2, yr)); 
        rightPB.add(countText, cc.xy(4, yr)); 
        yr += 2;

        rightPB.add(createLabel(getResourceString("DOA_ENDING")+":"), cc.xy(2, yr));
        rightPB.add(endingText, cc.xy(4, yr)); 
        yr += 2;

        rightPB.add(createLabel(getResourceString("DOA_SORT_BY")+":"), cc.xy(2, yr)); 
        rightPB.add(fieldOrderCbo, cc.xy(4, yr)); 
        yr += 2;

        rightPB.add(defaultCheck, cc.xy(4, yr)); yr += 2;
        
        /*
        // add separator line between left and right columns
        Color vsFGColor = new Color(224, 224, 224);
        Color vsBGColor = new Color(124, 124, 124);
        VerticalSeparator vs = new VerticalSeparator(vsFGColor, vsBGColor);
        rightPB.add(vs, cc.xywh(1, 1, 1, 12));
        */
        
        // lay out components on main panel        
        int y = 2; // leave first row blank 
        pb.add(tblInfoPB.getPanel(), cc.xyw(2, y, 3)); y += 2;
        pb.add(helpLbl,              cc.xyw(2, y, 3)); y += 2;
        
        pb.add(createLabel(getResourceString("DOA_DISPLAY_FORMATS")+":", SwingConstants.LEFT), cc.xy(2, y)); y += 1; 
        int y2 = y; // align 3rd column with this row 
        pb.add(new JScrollPane(aggregatorList), cc.xy(2,y)); y += 2;

        pb.add(controlPanel, cc.xy(2,y)); y += 2;
        
        // 2nd column (4th if you count the spaces between them)
        pb.add(rightPB.getPanel(), cc.xywh(3, y2, 2, 4));

        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // after all is created, set initial selection on format list 
        //formatList.setSelectedIndex(initialFormatSelectionIndex);
        if (initialFormatSelectionIndex >= 0 && 
        	initialFormatSelectionIndex < aggs.size())
        {
            fillWithObjAggregator(aggs.get(initialFormatSelectionIndex));
            aggregatorList.setSelectedIndex(initialFormatSelectionIndex);
        }
        else 
        {
        	addNewAggregator();
        }

    	updateUIEnabled();

        pack();
    }

    protected List<DataObjAggregator> populateAggregatorList() 
    {
        DefaultListModel listModel = new DefaultListModel();
    	List<DataObjAggregator> aggs;
    	aggs = dataObjFieldFormatMgrCache.getAggregatorList(tableInfo.getClassObj());
    	Collections.sort(aggs, new ComparatorByStringRepresentation<DataObjAggregator>()); 
    	for (DataObjAggregator agg : aggs)
    	{
    		listModel.addElement(agg);
    		uniqueTitles.add(agg.getTitle());
    	}

    	aggregatorList = createList(listModel);
    	aggregatorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	addAggregatorListSelectionListener();
    	addAggregatorListMouseListener();
    	
    	return aggs;
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
    	
        titleText .getDocument().removeDocumentListener(textChangedDL[4]);
        titleText .getDocument().removeDocumentListener(textChangedDL[0]);
        sepText   .getDocument().removeDocumentListener(textChangedDL[1]);
    	countText .getDocument().removeDocumentListener(textChangedDL[2]);
    	endingText.getDocument().removeDocumentListener(textChangedDL[3]);
    	
    	defaultCheck.removeItemListener(checkBoxListener);
    }

    private void addTextFieldChangeListeners()
    {
    	if (textChangedDL[4] == null)
    	{
    		try
    		{
	            textChangedDL[0] = new DataObjAggregatorDlgDocumentListener(titleText,   
	            		DataObjAggregator.class.getMethod("setTitle",   String.class));
	            textChangedDL[1] = new DataObjAggregatorDlgDocumentListener(sepText,    
	            		DataObjAggregator.class.getMethod("setSeparator",    String.class));
	            textChangedDL[2] = new DataObjAggregatorDlgDocumentListener(countText,  
	            		DataObjAggregator.class.getMethod("setCount",  String.class));
	            textChangedDL[3] = new DataObjAggregatorDlgDocumentListener(endingText, 
	            		DataObjAggregator.class.getMethod("setEnding", String.class));
	            
	            textChangedDL[4] = new DocumentListener()
	            {
	                public void removeUpdate(DocumentEvent e)  { changed(e); }
	                public void insertUpdate(DocumentEvent e)  { changed(e); }
	                public void changedUpdate(DocumentEvent e) { changed(e); }
	                
	                protected void changed(DocumentEvent e)
	                {
	                	// FIXME: tried to invalidate the list at every change to the aggregator so that it would show changes to title right away, but didn't work
	                	//aggregatorList.invalidate();
	                }
	            };
    		}
    		catch (NoSuchMethodException nsce)
    		{
        		throw new RuntimeException("No Such Method Exception: " + nsce.getMessage());
    		}
    	}
    	
        titleText .getDocument().addDocumentListener(textChangedDL[4]);
    	titleText .getDocument().addDocumentListener(textChangedDL[0]);
    	sepText   .getDocument().addDocumentListener(textChangedDL[1]);
    	countText .getDocument().addDocumentListener(textChangedDL[2]);
    	endingText.getDocument().addDocumentListener(textChangedDL[3]);
    }
    
    private void addComboBoxActionListeners()
    {
    	if (cboAL == null)
    	{
    		cboAL = new ActionListener()
    		{
    			public void actionPerformed(ActionEvent e)
    			{
    				if (e.getSource() == displayCbo)
    				{
        				Object item = displayCbo.getSelectedItem();
        				if (item instanceof DataObjSwitchFormatter)
        				{
	        				// display format changed
        					DataObjSwitchFormatter fmt = (DataObjSwitchFormatter) item;
	    					selectedAggregator.setFormatName(fmt.getName());
        				}
    				}
    				else if (e.getSource() == fieldOrderCbo)
    				{
        				Object item = fieldOrderCbo.getSelectedItem();
        				if (item instanceof DBFieldInfo)
        				{
        					// order by field changed
        					DBFieldInfo fi = (DBFieldInfo) item;
        					selectedAggregator.setOrderFieldName(fi.getName());
        				}
    				}
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
	    			// only source should be the default checkbox
	    			selectedAggregator.setDefault(defaultCheck.isSelected());
	    		}
	    	};
    	}
    	defaultCheck.addItemListener(checkBoxListener);
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
    	List<DataObjSwitchFormatter> fmts = dataObjFieldFormatMgrCache.getFormatterList(tableInfo.getClassObj());

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
    protected void fillWithObjAggregator(final DataObjAggregator agg)
    {
    	selectedAggregator = agg;

    	titleText.setText(agg.getTitle());
    	sepText.setText(agg.getSeparator());
    	endingText.setText(agg.getEnding());
    	defaultCheck.setSelected(agg.isDefault());
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
    
    protected void deleteSelectedAggregator()
    {
    	if (selectedAggregator == null)
    		return;
    		
		deletedAggregators.add(selectedAggregator);

		int index = aggregatorList.getSelectedIndex();
		DefaultListModel model = (DefaultListModel) aggregatorList.getModel();
		model.removeElement(selectedAggregator);
		
		if (model.getSize() == 0)
		{
			// aggregator list is now empty
			setSelectedFormat(null);
			updateUIEnabled();
			return;
		}
		
		//else
		index = (index >= model.getSize())? index - 1: index;
		aggregatorList.setSelectedIndex(index);
    }
    
    protected void addNewAggregator()
    {
		newAggregator = true;
		DefaultListModel listModel = (DefaultListModel) aggregatorList.getModel();
    	DataObjAggregator agg = new DataObjAggregator();
		agg.setDataClass(tableInfo.getClassObj());
    	String newTitle = DataObjFieldFormatMgr.getUniqueName(tableInfo.getTitle(), " ", uniqueTitles);
    	agg.setTitle(newTitle);
    	// aggregator's name will be set when inserted into hash of aggregators by agg manager
    	listModel.addElement(agg);
    	uniqueTitles.add(newTitle);
    	aggregatorList.setSelectedIndex(listModel.getSize() - 1);
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
    				int index = aggregatorList.locationToIndex(e.getPoint());
    				aggregatorList.ensureIndexIsVisible(index);
    				okButtonPressed();
    			}
    		}
    	};
    	
    	aggregatorList.addMouseListener(mAdp);
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
	        	    	setSelectedFormat(null);
	        	    	updateUIEnabled();
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
		
        aggregatorList.addListSelectionListener(aggregatorListSL);
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

    public DataObjAggregator getSelectedAggregator()
    {
    	return selectedAggregator;
    }

    public int getSelectedAggregatorIndex()
    {
    	return aggregatorList.getSelectedIndex();
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
    	int count;
		try
		{
			count = Integer.valueOf(countText.getText());
		}
		catch(Exception e)
		{
			count = 0;
		}
    	endingText.setEnabled( count > 0 );
    	
    	controlPanel.getDelBtn().setEnabled(selectedAggregator != null);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
    	// delete aggregators marked for deletion
    	Iterator<DataObjAggregator> it = deletedAggregators.iterator();
    	while (it.hasNext()) 
    	{
    		dataObjFieldFormatMgrCache.removeAggregator(it.next());
    	}

    	// add new aggregators back to manager
    	// new aggregators have empty names (as they will be named after inserted)
    	DefaultListModel listModel = (DefaultListModel) aggregatorList.getModel();
    	Enumeration<?> elements = listModel.elements();
    	while (elements.hasMoreElements())
    	{
    		Object obj = elements.nextElement();
    		if (obj instanceof DataObjAggregator)
    		{
           		// add formatter to list of existing ones and save it
    			DataObjAggregator agg = (DataObjAggregator) obj;
    			dataObjFieldFormatMgrCache.addAggregator(agg);
    		}
    	}

        super.okButtonPressed();
    }
    
    protected class DataObjAggregatorDlgDocumentListener implements DocumentListener
    {
    	protected JTextField source;
    	protected Method     setter;
    	
    	public DataObjAggregatorDlgDocumentListener(JTextField source, Method setter)
    	{
    		this.source = source;
    		this.setter = setter;
    	}
    	
        public void removeUpdate(DocumentEvent e)  { changed(e); }
        public void insertUpdate(DocumentEvent e)  { changed(e); }
        public void changedUpdate(DocumentEvent e) { changed(e); }
        
        protected void changed(DocumentEvent e)
        {
        	try
        	{
        		// equivalent to calling selectedAggregator.setter(source.getText())
        		setter.invoke(selectedAggregator, source.getText());
        		updateUIEnabled();
        	}
        	catch (IllegalAccessException iae)
        	{
        		throw new RuntimeException("Illegal Access Exception: " + iae.getMessage());
        	}
        	catch (InvocationTargetException ite)
        	{
        		throw new RuntimeException("Invocation Target Exception: " + ite.getMessage());
        	}
        }            	
    }
}
