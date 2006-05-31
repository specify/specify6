package edu.ku.brc.specify.ui.forms;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.ku.brc.specify.ui.forms.persist.TableViewDef;
import edu.ku.brc.specify.ui.forms.persist.View;
import edu.ku.brc.specify.ui.forms.persist.ViewDef;
import edu.ku.brc.specify.ui.validation.FormValidator;

public class TableViewObj implements Viewable
{
    //private static Log log = LogFactory.getLog(TableViewObj.class);

    protected boolean                       isEditting     = false;
    protected MultiView                     multiView      = null;
    protected FormViewObj                   parent;
    protected TableViewDef                  tableViewDef;

    // UI
    protected JTable                        table;
    protected JScrollPane                   tableScroller;
    protected JPanel                        mainComp;

    /**
     * Constructor with FormView definition
     * @param tableViewDef the definition of the form
     */
    public TableViewObj(final FormViewObj  parent,
                        final TableViewDef tableViewDef,
                        final MultiView    multiView)
    {

        this.parent       = parent;
        this.tableViewDef = tableViewDef;
        this.multiView    = multiView;

        //if (scrDateFormat == null)
        //{
        //    scrDateFormat = PrefsCache.getSimpleDateFormat("ui", "formatting", "scrdateformat");
        //}

        //Preferences appsNode = UICacheManager.getAppPrefs();
        //Preferences prefNode = appsNode.node("ui/formatting");
        //prefNode.addPreferenceChangeListener(this);

        mainComp = new JPanel(new BorderLayout());
        table = new JTable();
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);

        //for (int i=0;i<_model.getColumnCount();i++) {
        //    TableColumn column = _table.getColumn(_model.getColumnName(i));
        //    column.setCellRenderer(renderer);
        //}

        tableScroller = new JScrollPane(table);
        mainComp.add(tableScroller, BorderLayout.CENTER);

    }

    //-------------------------------------------------
    // Viewable
    //-------------------------------------------------

    public String getName()
    {
        return "XXX";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getId()
     */
    public int getId()
    {
        return -1;//tableViewDef.getId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getType()
     */
    public ViewDef.ViewType getType()
    {
        return tableViewDef.getType();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getUIComponent()
     */
    public Component getUIComponent()
    {
        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#isSubform()
     */
    public boolean isSubform()
    {
        return false;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getComp(java.lang.String)
     */
    public Component getComp(final String name)
    {
        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getControlMapping()
     */
    public Map<String, Component> getControlMapping()
    {
        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getValidator()
     */
    public FormValidator getValidator()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#setDataObj(java.lang.Object)
     */
    public void setDataObj(final Object dataObj)
    {

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getDataObj()
     */
    public Object getDataObj()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#setParentDataObj(java.lang.Object)
     */
    public void setParentDataObj(Object parentDataObj)
    {

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getParentDataObj()
     */
    public Object getParentDataObj()
    {
        return null;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#setDataIntoUI()
     */
    public void setDataIntoUI()
    {

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getDataFromUI()
     */
    public void getDataFromUI()
    {

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getDataFromUIComp(java.lang.String)
     */
    public Object getDataFromUIComp(final String name)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#setDataIntoUIComp(java.lang.String, java.lang.Object)
     */
    public void setDataIntoUIComp(final String name, Object data)
    {

    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getSubView(java.lang.String)
     */
    public MultiView getSubView(final String name)
    {
        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getFieldNames(java.util.List)
     */
    public void getFieldNames(final List<String> fieldNames)
    {

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#aboutToShow(boolean)
     */
    public void aboutToShow(boolean show)
    {

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getView()
     */
    public View getView()
    {
        return null;
    }
    /* (non
     * -Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#hideMultiViewSwitch(boolean)
     */
    public void hideMultiViewSwitch(boolean hide)
    {
        /*
        if (altViewUI != null)
        {
            altViewUI.setVisible(!hide);
        }
        */
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#dataHasChanged()
     */
    public void validationWasOK(boolean wasOK)
    {

    }
}
