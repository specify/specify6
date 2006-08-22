/**
 * 
 */
package edu.ku.brc.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class MemoryDropDownButton extends DropDownButton
{
	protected JMenuItem lastChosen;
	
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
            lastChosen = (JMenuItem)source;
        }
        else
        {
        	System.out.println("Dispatching event to the last item chosen");
        	for( ActionListener listener: lastChosen.getListeners(ActionListener.class))
        	{
        		if(listener != this)
        		{
            		listener.actionPerformed(ae);
        		}
        	}
        }
	}
}
