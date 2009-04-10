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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/*
 * This work is hereby released into the Public Domain. To view a copy of the public domain
 * dedication, visit http://creativecommons.org/licenses/publicdomain/
 */
public class AutoCompletion extends PlainDocument
{
    protected JComboBox      comboBox;
    protected ComboBoxModel  model;
    protected AutoCompComboBoxModelIFace autoCompModel = null;
    
    protected JTextComponent editor;
    // flag to indicate if setSelectedItem has been called
    // subsequent calls to remove/insertString should be ignored
    protected boolean        selecting    = false;
    protected boolean        doShowPopUp  = false;
    protected boolean        hidePopupOnFocusLoss;
    protected boolean        hitBackspace = false;
    protected boolean        hitBackspaceOnSelection;
    protected boolean        canAdd       = false;

    protected KeyListener    editorKeyListener;
    protected FocusListener  editorFocusListener;
    
    int cnt = 0;

    public AutoCompletion(final JComboBox comboBoxArg)
    {
        this.comboBox = comboBoxArg;
        this.model    = comboBox.getModel();
        checkModel();
        
        comboBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //System.out.println("actionPerformed " + selecting);
                if (!selecting && comboBox.getSelectedIndex() != -1)
                {
                    highlightCompletedText(0);
                }
            }
        });
        comboBox.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                //System.out.println("propertyChange");
                if (e.getPropertyName().equals("editor"))
                {
                    configureEditor((ComboBoxEditor) e.getNewValue());
                }
                
                if (e.getPropertyName().equals("model"))
                {
                    model = (ComboBoxModel) e.getNewValue();
                    checkModel();
                }
            }
        });
        editorKeyListener = new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                //System.out.println("\nkeyPressed");

                if (!comboBox.isPopupVisible() && comboBox.isDisplayable() && editor.getText().length() == 0)
                {
                    //comboBox.setPopupVisible(true);
                }
                hitBackspace = false;
                switch (e.getKeyCode())
                {
                    // determine if the pressed key is backspace (needed by the remove method)
                    case KeyEvent.VK_BACK_SPACE:
                        hitBackspace = true;
                        hitBackspaceOnSelection = editor.getSelectionStart() != editor.getSelectionEnd();
                        break;

                    // ignore delete key
                    case KeyEvent.VK_ENTER:
                        //System.out.println("VK_ENTER " + comboBox.getSelectedIndex());
                        if (canAdd && comboBox.getSelectedIndex() == -1)
                        {
                            if (autoCompModel != null)
                            {
                                autoCompModel.add(editor.getText());
                                
                            } else if (model instanceof MutableComboBoxModel)
                            {
                                ((MutableComboBoxModel)model).addElement(editor.getText());
                            }
                        }
                        break;

                    case KeyEvent.VK_DELETE:
                        e.consume();
                        comboBox.getToolkit().beep();
                        break;

                    case KeyEvent.VK_DOWN:
                        if (!comboBox.isPopupVisible() && comboBox.isDisplayable())
                        {
                            comboBox.setPopupVisible(true);
                            e.consume();
                        } else
                        {
                            cnt++;
                            //System.out.println(cnt + " DOWN - Current Index: "+comboBox.getSelectedIndex());
                            if (comboBox.getSelectedIndex() < comboBox.getModel().getSize() - 1)
                            {
                                //System.out.println("  Setting Index: "+(comboBox.getSelectedIndex()+1));
                                doShowPopUp = true;
                                final int newIndex = comboBox.getSelectedIndex() + 1;
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run()
                                    {
                                        comboBox.setSelectedIndex(newIndex);
                                        //System.out.println("  Current Index: "+(comboBox.getSelectedIndex())+"  "+comboBox.getSelectedItem());
                                        final Object newText = comboBox.getModel().getElementAt(newIndex);
                                        //System.out.println(newText.hashCode() +" "+newText.toString().hashCode());
                                        if (editor != null)
                                        {
                                            //System.out.println(newText);
                                            //editor.setText(newText.toString());
                                            setText(new String(newText.toString()));
                                        }
                                        //System.out.println("  Current Index: "+(comboBox.getSelectedIndex()));
                                    }
                                    
                                });
                                
                                e.consume();
                            }
                        }
                        break;

                    case KeyEvent.VK_UP:
                        if (!comboBox.isPopupVisible() && comboBox.isDisplayable())
                        {
                            //System.out.println("XXXXXX");
                            comboBox.setPopupVisible(true);
                            e.consume();
                        } else
                        {
                            cnt++;
                            //System.out.println(cnt + " UP - Current Index: "+comboBox.getSelectedIndex());
                            if (comboBox.getSelectedIndex() > 0)
                            {
                                //System.out.println("  Setting Index: "+(comboBox.getSelectedIndex()-1));
                                doShowPopUp = true;
                                final int newIndex = comboBox.getSelectedIndex() - 1;
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run()
                                    {
                                        comboBox.setSelectedIndex(newIndex);
                                        //System.out.println("  Current Index: "+(comboBox.getSelectedIndex())+"  "+comboBox.getSelectedItem());
                                        final Object newText = comboBox.getModel().getElementAt(newIndex);
                                        //System.out.println(newText.hashCode() +" "+newText.toString().hashCode());
                                        if (editor != null)
                                        {
                                            //System.out.println(newText);
                                            //editor.setText(newText.toString());
                                            setText(new String(newText.toString()));
                                        }
                                        //System.out.println("  Current Index: "+(comboBox.getSelectedIndex()));
                                    }
                                    
                                });
                                e.consume();
                            }
                        }
                        break;
                }
            }
        };
        // Bug 5100422 on Java 1.5: Editable JComboBox won't hide popup when tabbing out
        hidePopupOnFocusLoss = System.getProperty("java.version").startsWith("1.5");
        // Highlight whole text when gaining focus
        editorFocusListener = new FocusAdapter()
        {
            public void focusGained(FocusEvent e)
            {
                highlightCompletedText(0);
            }

            public void focusLost(FocusEvent e)
            {
                // Workaround for Bug 5100422 - Hide Popup on focus loss
                if (hidePopupOnFocusLoss)
                {
                    comboBox.setPopupVisible(false);
                }
            }
        };
        configureEditor(comboBox.getEditor());
        // Handle initially selected object
        Object selected = comboBox.getSelectedItem();
        if (selected != null)
            setText(selected.toString());
        highlightCompletedText(0);
    }
    
    /**
     * Checks the model to see if it can have items added or removed.
     */
    protected void checkModel()
    {
        if (model instanceof AutoCompComboBoxModelIFace)
        {
            autoCompModel = (AutoCompComboBoxModelIFace)model;
            canAdd        = autoCompModel.isMutable(); 
            
        } else if (model instanceof MutableComboBoxModel)
        {
            canAdd = true;
        }
    }

    /**
     * @param comboBox
     */
    public static void enable(JComboBox comboBox)
    {
        // has to be editable
        comboBox.setEditable(true);
        // change the editor's document
        new AutoCompletion(comboBox);
    }

    /**
     * @param newEditor
     */
    void configureEditor(ComboBoxEditor newEditor)
    {
        if (editor != null)
        {
            editor.removeKeyListener(editorKeyListener);
            editor.removeFocusListener(editorFocusListener);
        }

        if (newEditor != null)
        {
            editor = (JTextComponent) newEditor.getEditorComponent();
            editor.addKeyListener(editorKeyListener);
            editor.addFocusListener(editorFocusListener);
            editor.setDocument(this);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.AbstractDocument#remove(int, int)
     */
    public void remove(final int offset, final int len) throws BadLocationException
    {
        int offs = offset;
        // return immediately when selecting an item
        if (selecting)
        {
            return;
        }
        
        if (hitBackspace)
        {
            // user hit backspace => move the selection backwards
            // old item keeps being selected
            if (offs > 0)
            {
                if (hitBackspaceOnSelection)
                    offs--;
            } else
            {
                // User hit backspace with the cursor positioned on the start => beep
                comboBox.getToolkit().beep(); // when available use:
                                                // UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
            }
            highlightCompletedText(offs);
        } else
        {
            super.remove(offs, len);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.PlainDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
     */
    public void insertString(final int offset, final String str, final AttributeSet a) throws BadLocationException
    {
        int offs = offset;
        
        //System.out.println("insertString");
        
        // return immediately when selecting an item
        if (selecting)
        {
            return;
        }
        
        //System.out.println("offs["+offs+"]["+comboBox.getSelectedIndex()+"]["+str+"][ canAdd["+canAdd+"]");
        /*if ((!canAdd || (offs > 0 && comboBox.getSelectedIndex() > -1)) && !comboBox.isPopupVisible() && comboBox.isDisplayable())
        {
            //System.out.println(offs+" ******** setPopupVisible(true)");
            comboBox.setPopupVisible(true);
        }*/
        
        // insert the string into the document
        super.insertString(offs, str, a);
        
        Object item = null;
        if (!canAdd || offs == 0)
        {
            // lookup and select a matching item
            item = lookupItem(getText(0, getLength()));
            if (item != null)
            {
                //System.out.println("insertString - Selecting item["+item+"]");
                setSelectedItem(item);
                setText(item.toString());
                
                // select the completed part
                highlightCompletedText(offs + str.length());
                
                /*if (doShowPopUp && !comboBox.isPopupVisible() && comboBox.isDisplayable())
                {
                    //System.out.println("***********************");
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            comboBox.setPopupVisible(true);
    
                        }
                    });
                }*/
                
            } else
            {
                setSelectedIndex(-1);
                //System.out.println("Selecting item[-1]");
                if (editor != null && !canAdd)
                {
                    editor.setText("");
                }
                if (comboBox.isPopupVisible())
                {
                    comboBox.setPopupVisible(false);
                }
                return;
            }
        }

        if (!canAdd && item == null)
        {
            // keep old item selected if there is no match
            item = comboBox.getSelectedItem();
            if (item != null)
            {
                // imitate no insert (later on offs will be incremented by str.length(): selection won't
                // move forward)
                offs = offs - str.length();
                // provide feedback to the user that his input has been received but can not be accepted
                comboBox.getToolkit().beep(); // when available use:
                                                // UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
                setText(item.toString());
                
                // select the completed part
                highlightCompletedText(offs + str.length());
            }
        }
        
        if (canAdd && item == null)
        {
            setSelectedIndex(-1);
        }

    }

    /**
     * @param text
     */
    private void setText(final String text)
    {
        try
        {
            // remove all text and insert the completed string
            super.remove(0, getLength());
            super.insertString(0, text, null);
            
        } catch (BadLocationException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AutoCompletion.class, e);
            throw new RuntimeException(e.toString());
        }
    }

    private void highlightCompletedText(int start)
    {
        editor.setCaretPosition(getLength());
        editor.moveCaretPosition(start);
    }

    private void setSelectedItem(Object item)
    {
        //System.out.println("****** setSelectedItem "+item);
        selecting = true;
        model.setSelectedItem(item);
        selecting = false;
    }

    private void setSelectedIndex(final int index)
    {
        //System.out.println("****** setSelectedIndex "+index);
        selecting = true;
        comboBox.setSelectedIndex(index);
        selecting = false;
    }

    private Object lookupItem(String pattern)
    {
        //System.out.println("------------ lookupItem ["+pattern+"]----------------");
        Object selectedItem = model.getSelectedItem();
        // only search for a different item if the currently selected does not match
        if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern))
        {
            return selectedItem;
        }
        
        // iterate over all items
        for (int i = 0, n = model.getSize(); i < n; i++)
        {
            Object currentItem = model.getElementAt(i);
            // current item starts with the pattern?
            if (currentItem != null && startsWithIgnoreCase(currentItem.toString(), pattern)) { return currentItem; }
        }
        // no item starts with the pattern => return null
        return null;
    }

    // checks if str1 starts with str2 - ignores case
    private boolean startsWithIgnoreCase(String str1, String str2)
    {
        return str1.toUpperCase().startsWith(str2.toUpperCase());
    }

    /*
    private static void createAndShowGUI()
    {
        String[] items = new String[] { "Ester", "Jordi", "Jordina",
                "Jorge", "Sergi" };// "TEster", "UJordi", "VJordina", "WJorge", "XSergi"};
        AutoCompCBXModel model = new AutoCompCBXModel(items);
        // the combo box (add/modify items if you like to)
        final JComboBox comboBox = createComboBox(model);
        enable(comboBox);

        // create and show a window containing the combo box
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(3);
        frame.getContentPane().add(comboBox);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args)
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                createAndShowGUI();
            }
        });
    }*/
}
