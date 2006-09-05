/**
 * 
 */
package edu.ku.brc.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @code_status Beta
 * @author jstewart
 */
public class MemoryDropDownButton extends DropDownButton
{
	protected JMenuItem lastChosen;
	protected PropertyChangeListener changeListener;
	
    /**
     *
     *
     * @param label
     * @param icon
     * @param textPosition
     * @param menus
     */
    public MemoryDropDownButton(final String label, final Icon icon, final int textPosition, final List<JComponent> menus)
    {
        super(label, icon, textPosition, menus);
        boolean valid = false;
        for(JComponent comp:menus)
        {
        	if(comp instanceof JMenuItem)
        	{
        		valid = true;
        		JMenuItem mi = (JMenuItem)comp;
        		mi.addActionListener(this);
        		if(lastChosen == null)
        		{
        			lastChosen = mi;
        		}
        	}
        }
        if(!valid)
        {
        	throw new IllegalArgumentException("Menus list must contain at least one JMenuItem");
        }
        
        changeListener = new PropertyChangeListener()
        {
    		public void propertyChange(PropertyChangeEvent evt)
    		{
    			fixMainButtonState();
    		}
        };
        
        lastChosen.addPropertyChangeListener(changeListener);
    }

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		Object source = ae.getSource();
        if(source == arrowBtn)
        {
            JPopupMenu popup = getPopupMenu();
            popup.addPopupMenuListener(this);
            popup.show(mainBtn, 0, mainBtn.getHeight());
            
        }
        else if( source instanceof JMenuItem )
        {
        	lastChosen.removePropertyChangeListener("enabled",changeListener);
            lastChosen = (JMenuItem)source;
            lastChosen.addPropertyChangeListener("enabled",changeListener);
        }
        else
        {
        	lastChosen.doClick();
        }
	}
	
	protected void fixMainButtonState()
	{
		System.out.println("Fixing button state");
		mainBtn.setEnabled(lastChosen.isEnabled());
	}
}
