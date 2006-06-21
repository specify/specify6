/* Filename:    $RCSfile: JComboBoxFromQuery.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/03/30 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */package edu.ku.brc.specify.ui.db;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Formatter;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.dbsupport.DBConnection;

/**
 * Auto Complete ComboxBox filled from a query, the setValue and getValue should use Hibernate Objects from that table
 * 
 * @author rods
 *
 */
public class JComboBoxFromQuery extends JComboBox
{
    protected static final Logger log = Logger.getLogger(JComboBoxFromQuery.class);
    
    protected int                  caretPos         = 0;
    protected boolean              caseInsensitve   = true;

    protected JTextField           tf               = null;
    protected boolean              foundMatch       = false;
    protected boolean              ignoreFocus      = false;
    protected DefaultComboBoxModel model;
    protected Vector<Integer>      idList           = new Vector<Integer>();
    protected Vector<String>       list             = new Vector<String>();
    protected String               entryStr         = "";
    protected int                  searchStopLength = Integer.MAX_VALUE;
    protected int                  oldLength        = 0;
    
    protected String               sql;
    protected String               tableName;
    protected String               displayColumns;
    protected String               idColumn;
    protected String               format;
    protected String               keyColumn;
    protected int                  numColumns      = -1; 
    protected Connection           dbConnection    = null;
    protected Object[]             values;

    /**
     * Constructor 
     */
    public JComboBoxFromQuery(final String sql, 
                              final String format)
    {
        this(null, null, null, null, format);
        this.sql = sql;
    }
    
    /**
     * Constructor 
     */
    public JComboBoxFromQuery(final String tableName, 
                              final String idColumn, 
                              final String keyColumn, 
                              final String format)
    {
        this(tableName, idColumn, keyColumn, null, format);
    }
    
    /**
     * Constructor 
     */
    public JComboBoxFromQuery(final String tableName, 
                              final String idColumn, 
                              final String keyColumn, 
                              final String displayColumns, 
                              final String format)
    {
        super();
        this.tableName      = tableName;
        this.keyColumn      = keyColumn;
        this.idColumn       = idColumn;
        this.displayColumns = displayColumns != null ? displayColumns : keyColumn;
        this.format         = format;
        
        init();
    }
    
    /**
     * Returns combobox's text field
     * @return combobox's text field
     */
    public JTextField getTextField()
    {
        return tf;
    }
    
    /**
     * Initializes the combobox to enable the typing of values 
     */
    protected void init()
    {
        model = new DefaultComboBoxModel(list);
        this.setModel(model);
        this.setEditor(new BasicComboBoxEditor());
        this.setEditable(true);
        setSelectedItem("");     
    }
    
    /**
     * Sets wehether the searches for the items are case insensitive or not
     * @param caseInsensitve
     */
    public void setCaseInsensitive(final boolean caseInsensitve)
    {
        this.caseInsensitve = caseInsensitve;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComboBox#setSelectedIndex(int)
     */
    public void setSelectedIndex(int index)
    {
        super.setSelectedIndex(index);
        if (index > -1)
        {
	        tf.setText((String)getItemAt(index));
	        tf.setSelectionEnd(caretPos + tf.getText().length());
	        tf.moveCaretPosition(caretPos);
        }
    }
    
    /**
     * @return the list
     */
    public List<String> getList()
    {
        return list;
    }
    
    /**
     * @return the id of the selected item
     */
    public Integer getSelectedId()
    {
        if (idList.size() > 0 && getSelectedIndex() > -1)
        {
            return idList.get(getSelectedIndex());
        } else
        {
            return null;
        }
    }
    
    /**
     * Returns the list of IDs for the combobox
     * @return the list of IDs for the combobox
     */
    public List<Integer> getIDList()
    {
        return idList;
    }
    
    /**
     * 
     */
    protected void lookForMatch()
    {
        String s   = tf.getText();
        int    len = s.length();
        if (len == 0)
        {
            setSelectedIndex(-1);
            foundMatch = false;
            return;
        }
        
        //System.out.println(s);
        caretPos = tf.getCaretPosition();
        String text = "";
        try
        {
            text = tf.getText(0, caretPos);
            
        } catch (BadLocationException ex)
        {
            ex.printStackTrace();
        }
        
        String textLowerCase = text.toLowerCase();
        
        foundMatch = true;
        int n = getItemCount();
        for (int i = 0; i < n; i++)
        {
            int ind;
            if (caseInsensitve) 
            {
                String item = ((String)getItemAt(i)).toLowerCase();
                ind = item.indexOf(textLowerCase);
            } else
            {
                ind = ((String)getItemAt(i)).indexOf(text);
            }
            
            if (ind == 0)
            {
                setSelectedIndex(i);
                return;
            }
        }
        
        // When not doing "additions" ...
        // At this point there was no match so "if" there had been one before there isn't now
        // so remove the last character typed and check to see if there is a match again.
        if (len > 0)
        {
            tf.setText(s.substring(0, len-1));
            lookForMatch();
            return;
        }
        foundMatch = false;        
    }

    /**
     * Fill the the drop down with the list from the query
     */
    protected void fillBox(final String newEntryStr)
    {
        try
        {
            searchStopLength = Integer.MAX_VALUE;
            if (newEntryStr.equals(entryStr))
            {
                //System.out.println("Skipping.");
                return;
            }
            list.clear();
            idList.clear();
            if (newEntryStr.length() == 0)
            {
                searchStopLength = Integer.MAX_VALUE;
                return;
            }
            entryStr = newEntryStr;
            
            String queryString;
            if (sql == null)
            {
                queryString= "select distinct " + displayColumns + "," + idColumn  + " from " + tableName + " where lower(" + keyColumn + 
                                 ") like '"+ entryStr.toLowerCase() +"%' order by " + keyColumn + " asc";
            } else
            {
                queryString = sql.replace("%s", entryStr.toLowerCase());
            }
            log.debug(queryString);
            
            Statement  dbStatement = dbConnection.createStatement();
            ResultSet rs           = dbStatement.executeQuery(queryString);
            if (rs.first())
            {
                if (numColumns == -1)
                {
                    numColumns = rs.getMetaData().getColumnCount()-1;
                    values = new Object[numColumns];
                }
                
                do
                {
                    if (numColumns == 1)
                    {
                        idList.addElement(rs.getInt(2));
                        list.addElement(rs.getString(1));
                        
                    } else
                    {
                        try
                        {
                            idList.addElement(rs.getInt(numColumns+1));
                            for (int i=0;i<numColumns;i++)
                            {
                                Object val = rs.getObject(i+1);
                                values[i] = val != null ? val : "";
                            }
                            Formatter formatter = new Formatter(); 
                            formatter.format(format, (Object[])values);
                            list.addElement(formatter.toString());
                            
                        } catch (java.util.IllegalFormatConversionException ex)
                        {
                            list.addElement(values[0] != null ? values[0].toString() : "(No Value)");
                        } 
                    }
                    
                } while(rs.next());
            } else
            {
                searchStopLength = Integer.MAX_VALUE;
            }
            
            if (list.size() > 0 && list.size() < 11)
            {
                this.searchStopLength = entryStr.length();
            }
            rs.close();
            dbStatement.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComboBox#setEditor(javax.swing.ComboBoxEditor)
     */
    public void setEditor(ComboBoxEditor anEditor)
    {
        super.setEditor(anEditor);
        
        if (anEditor.getEditorComponent() instanceof JTextField)
        {
            tf = (JTextField) anEditor.getEditorComponent();
            tf.addFocusListener(new FocusAdapter() 
            {
                public void focusGained(FocusEvent e)
                {
                    searchStopLength = Integer.MAX_VALUE;
                    
                    if (dbConnection != null) // shouldn't happen
                    {
                        try
                        {
                            dbConnection.close();
                        } catch (SQLException ex) {};
                    }
                    dbConnection = DBConnection.getConnection();
                }
                
                public void focusLost(FocusEvent e)
                {
                    tf.setSelectionStart(0);
                    tf.setSelectionEnd(0);
                    tf.moveCaretPosition(0);
                    try
                    {
                        dbConnection.close();
                    } catch (SQLException ex) {};
                    dbConnection = null;
                    if (tf.getText().length() == 0)
                    {
                        setSelectedIndex(-1);
                    }
                }
            });
            
            //System.out.println(tf.getKeyListeners());
            tf.addKeyListener(new KeyAdapter()
            {
                protected int prevCaretPos = -1;
                
                public void keyPressed(KeyEvent ev)
                {
                    prevCaretPos = tf.getCaretPosition();
                }
                
                public void keyReleased(KeyEvent ev)
                {
                    String textStr = tf.getText();
                    //System.out.println("Len: "+textStr.length()+" "+searchStopLength+"  Old: "+oldLength);
                    
                    if (Math.abs(textStr.length() - oldLength) > 1)
                    {
                        searchStopLength = Integer.MAX_VALUE;
                        //System.out.println("searchStopLength reset");
                    }
                    
                    char key = ev.getKeyChar();
                    if (ev.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                    {
                        if (textStr.length() < searchStopLength)
                        {
                            fillBox(textStr);
                        }
                        int len = textStr.length();
                        if (len == 0)
                        {
                            foundMatch = false;
                            setSelectedIndex(-1);
                            oldLength = tf.getText().length();
                            return;
                            
                        } else
                        {
                            if (foundMatch)
                            {
                                tf.setText(textStr.substring(0, len-1));
                                
                            } else if (len > 0)
                            {
                                tf.setText(textStr.substring(0, len-1));
                                lookForMatch();
                                oldLength = tf.getText().length();
                                return;
                            }
                        }
                        
                    } else if ((!(Character.isLetterOrDigit(key) || Character.isSpaceChar(key))) && 
                                 ev.getKeyCode() != KeyEvent.VK_DELETE)
                    {
                        if (ev.getKeyCode() == KeyEvent.VK_ENTER) 
                        {
                            tf.setSelectionStart(0);
                            tf.setSelectionEnd(0);
                            tf.moveCaretPosition(0);

                            
                        } else if (ev.getKeyCode() == KeyEvent.VK_END)
                        {
                            tf.setSelectionStart(prevCaretPos);
                            tf.setSelectionEnd(tf.getText().length());
                        }
                        oldLength = tf.getText().length();
                        return;
                        
                    } else if (ev.getKeyCode() != KeyEvent.VK_DELETE)
                    {
                        if (textStr.length() < searchStopLength)
                        {
                            fillBox(textStr);
                        }
                        
                    } else if (textStr.length() < searchStopLength)
                    {
                        fillBox(textStr);
                    }
                    lookForMatch();
                    oldLength = tf.getText().length();
                }
            });
        }
    }

}
