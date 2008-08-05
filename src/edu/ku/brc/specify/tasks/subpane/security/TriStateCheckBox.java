/*
 * 
 * Copyright 1994-2007 Sun Microsystems, Inc. All Rights Reserved.
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * - Redistribution of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS 
 * OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED 
 * WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, 
 * ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT 
 * BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR 
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS 
 * BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, 
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE 
 * THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended for use 
 * in the design, construction, operation or maintenance of any nuclear facility. 
 * 
 * Source code extracted from:
 * 
 *  	http://forum.java.sun.com/thread.jspa?threadID=593755&messageID=3116647
 * 
 * This license is also available at the following web address:
 * 
 * 		http://developers.sun.com/license/berkeley_license.html
 * 
 */

package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JCheckBox;
import javax.swing.JToggleButton;


public class TriStateCheckBox extends JCheckBox {

	public static enum State {
		CHECKED, UNCHECKED, PARTIAL 
	};

	/**
	 * Creates an initially unselected check box button with no text, no icon.
	 */
	public TriStateCheckBox() {
		this(null, State.UNCHECKED);
	}

	/**
	 * Creates a check box with text and icon,
	 * and specifies whether or not it is initially selected.
	 *
	 * @param text the text of the check box.
	 * @param icon  the Icon image to display
	 * @param selected a boolean value indicating the initial selection
	 *        state. If <code>true</code> the check box is selected
	 */
	public TriStateCheckBox (String text, State initial) {      
		super.setText(text);
		setModel(new TriStateModel(initial));

		//some UI settings
		setRolloverEnabled( false );

		/*
	    List<Object> gradient = new LinkedList<Object>();
	    gradient.add( 0 );
	    gradient.add( 0 );
	    gradient.add( new ColorUIResource(Color.white) );
	    gradient.add( new ColorUIResource(Color.white) );
	    gradient.add( new ColorUIResource(Color.white) );    
	    UIManager.put("CheckBox.gradient", gradient); //get rid of gradient
		*/
	}


	/**
	 * Set the new state to either CHECKED, PARTIAL or UNCHECKED.
	 */
	public void setState(State state) 
	{
		((TriStateModel) model).setState(state);
	}

	/**
	 * Return the current state, which is determined by the selection status of
	 * the model.
	 */
	public State getState() 
	{
		return ((TriStateModel) model).getState();
	} 

	public void setSelected(boolean selected) 
	{
		((TriStateModel) model).setSelected(selected);    
	} 


	public void paintComponent( Graphics g ) 
	{
		super.paintComponent( g );

		if(((TriStateModel) model).getState() == State.PARTIAL) 
		{      
			Graphics2D g2 = (Graphics2D) g;
			
			int w = getWidth();
			int h = getHeight();
			
			g2.setColor( Color.darkGray );      
			g2.fillRect( w/2 - 5, h/2 - 4 , 8 , 8 );  
		}    
	}

	/** The model for the button */
	private static class TriStateModel extends JToggleButton.ToggleButtonModel
	{      
		protected State state;  

		public TriStateModel(State state)
		{
			this.state = state;
		}

		public boolean isSelected()
		{      
			return state == State.CHECKED;
		} 

		public State getState() 
		{
			return state;
		}

		public void setState(State state) 
		{
			this.state = state;
			fireStateChanged();
		}

		public void setPressed(boolean pressed)
		{      
			if (pressed)
			{
				switch(state)
				{
				case UNCHECKED: 
					state = State.PARTIAL;
					break;
				case PARTIAL: 
					state = State.CHECKED;
					break;
				case CHECKED: 
					state = State.UNCHECKED;
					break;
				}        
			}

			//System.err.println("Changed state to : " + state);
		}

		public void setSelected(boolean selected)
		{       
			if (selected) 
			{
				this.state = State.CHECKED;
			} 
			else 
			{
				this.state = State.UNCHECKED;
			}      
		}
	} 

}