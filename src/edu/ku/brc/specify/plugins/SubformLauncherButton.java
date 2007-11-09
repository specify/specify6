package edu.ku.brc.specify.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;
import edu.ku.brc.specify.tasks.DataEntryTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.forms.FormDataObjIFace;

public class SubformLauncherButton extends JButton implements GetSetValueIFace, UIPluginable, ActionListener
{
    private static Logger log = Logger.getLogger(SubformLauncherButton.class);
    
    protected Object data;
    protected int tableID;
    
    public SubformLauncherButton()
    {
        super();
        addActionListener(this);
    }

    public Object getValue()
    {
        return data;
    }

    public void setValue(Object value, String defaultValue)
    {
        data = (value != null) ? value : defaultValue;
        
        if (data instanceof Collection)
        {
            // force initialization of Hibernate collections if it is one
            Collection<?> dataCollection = (Collection)data;
            dataCollection.size();
        }
    }

    public JComponent getUIComponent()
    {
        return this;
    }

    public void initialize(Properties properties, boolean isViewMode)
    {
        String title = properties.getProperty("title");
        if (title != null)
        {
            setText(title);
        }
        String tID = properties.getProperty("tableID");
        if (tID != null)
        {
            tableID = Integer.parseInt(tID);
        }
        
    }

    public void setCellName(String cellName)
    {
        // do nothing
    }

    public void setChangeListener(ChangeListener listener)
    {
        // do nothing
    }

    public void actionPerformed(ActionEvent e)
    {
        Object dataToSend = null;
        
        if (data == null)
        {
            return;
        }
        
        if (data instanceof Collection)
        {
            // see if all elements implement FormDataObjIFace
            // if so, make a RecordSet to fire in the CommandAction
            Collection<?> dataItems = (Collection)data;
            ArrayList<Integer> ids = new ArrayList<Integer>();
            
            for (Object dataItem: dataItems)
            {
                if (dataItem instanceof FormDataObjIFace)
                {
                    FormDataObjIFace formObj = (FormDataObjIFace)dataItem;
                    ids.add(formObj.getId());
                }
                else
                {
                    // we encountered an object that didn't implement FormDataObjIFace
                    // we can't handle this data
                    // just quit
                    log.error("Non-FormDataObjIFace object found in data collection.  SubformLauncherButtons cannot handle this data.");
                    return;
                }
            }
            
            // make the RecordSet to send as the data of the CommandAction
            RecordSet recordSet = new RecordSet("tmpCmdRS", tableID);
            
            for (Integer id: ids)
            {
                recordSet.addItem(new RecordSetItem(id));
            }
            
            dataToSend = recordSet;
        }
        else if (data instanceof FormDataObjIFace)
        {
            FormDataObjIFace formObj = (FormDataObjIFace)data;
            
            RecordSet recordSet = new RecordSet("tmpCmdRS", tableID);
            recordSet.addItem(new RecordSetItem(formObj.getId()));
            
            dataToSend = recordSet;
        }
        else
        {
            // we don't understand this type of data
            return;
        }
        
        CommandAction command = new CommandAction(DataEntryTask.DATA_ENTRY, DataEntryTask.EDIT_DATA, dataToSend);
        CommandDispatcher.dispatch(command);
    }
}
