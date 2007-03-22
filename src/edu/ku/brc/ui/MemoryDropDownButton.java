/**
 * 
 */
package edu.ku.brc.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;

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
    public MemoryDropDownButton(final String label, final ImageIcon icon, final int textPosition, final List<JComponent> menus)
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
         if(overRideButtonBorder)mainBtn.setBorder(hoverBorder);
        lastChosen.addPropertyChangeListener(changeListener);
    }

//    public void setOverrideBorder(Border border){
//        overRideButtonBorder = border;
//        if(overRideButtonBorder)mainBtn.setBorder(overRideButtonBorder);
//    }
	@Override
	public void actionPerformed(ActionEvent ae)
	{
        //if(overRideButtonBorder)mainBtn.setBorder(hoverBorder);
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
            fixMainButtonState();
        }
        else
        {
        	lastChosen.doClick();
        }
        //if(overRideButtonBorder!=null)mainBtn.setBorder(overRideButtonBorder);
	}
	
	protected void fixMainButtonState()
	{
		System.out.println("Fixing button state");
		mainBtn.setEnabled(lastChosen.isEnabled());
        mainBtn.setText(lastChosen.getText());
        //if(overRideButtonBorder)mainBtn.setBorder(hoverBorder);
	}
}
