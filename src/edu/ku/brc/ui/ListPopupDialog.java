/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 *
 * @code_status Beta
 * 
 * @author jstewart
 */
@SuppressWarnings("serial")
public class ListPopupDialog extends JDialog
{
	protected ListPopupCallback callback;
	protected Vector<Object> options;
	
	protected JPanel    messagePanel;
	protected JPanel    cbPanel;
	protected JPanel    buttonPanel;
	protected JComboBox optionList;
	protected DefaultComboBoxModel model;
	
	protected JButton okButton;
	protected JButton cancelButton;
	
	public ListPopupDialog(Frame owner,String message,List<Object> options,ListPopupCallback popupCallback)
	{
		super(owner);
        
		this.setLayout(new BorderLayout());
		this.callback = popupCallback;
		this.options = new Vector<Object>(options);
		
		model      = new DefaultComboBoxModel(this.options);
		optionList = createComboBox(model);
		cbPanel    = new JPanel();
		cbPanel.add(optionList);
		add(cbPanel,BorderLayout.CENTER);
         
        JLabel lbl = createLabel(message);

		add(lbl,BorderLayout.NORTH);
		
		buttonPanel  = new JPanel(new FlowLayout());
		cancelButton = createButton(getResourceString("CANCEL"));

		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
				callback.cancelled();
			}
		});
		okButton = createButton(getResourceString("OK"));

		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
				callback.completed(optionList.getSelectedItem());
			}
		});
		
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		
		add(buttonPanel,BorderLayout.SOUTH);
		
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent we)
			{
				callback.cancelled();
			}
		});
		
        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }

	}
	
	public void setComboBoxCellRenderer(final ListCellRenderer renderer)
	{
		optionList.setRenderer(renderer);
	}
	
	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		okButton.requestFocusInWindow();
		okButton.requestFocus();
	}
	
	public void addOption(Object option)
	{
		options.add(option);
	}
	
	public void removeOption(Object option)
	{
		options.remove(option);
	}
	
	public interface ListPopupCallback
	{
		public void completed(Object userSelection);
		public void cancelled();
	}
	
//	@SuppressWarnings("unchecked")
//	public static void main(String[] args)
//	{
//		ListPopupCallback cb = new ListPopupCallback()
//		{
//			public void cancelled()
//			{
//				System.out.println("User cancelled");
//			}
//			public void completed(Object userSelection)
//			{
//				System.out.println("User selected " + userSelection);
//			}
//		};
//		Vector options = new Vector();
//		options.add("Hello");
//		options.add(new Object());
//		options.add(new WindowAdapter(){/* do nothing */});
//		ListPopupDialog d = new ListPopupDialog(null,"Make a selection",options,cb);
//		d.setSize(300,300);
//		UIHelper.centerAndShow(d);
//	}
}
