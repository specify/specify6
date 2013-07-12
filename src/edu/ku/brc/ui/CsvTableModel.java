/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;
import java.util.Vector;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class CsvTableModel implements TableModel
{
	protected Vector<TableModelListener> listeners;
	
	protected File csvFile;
	
	protected Vector<String[]> rowData;
	protected Vector<String> methods;
	protected String[] header;
	
	protected Random rand;
	protected int randStart;
	
	public CsvTableModel( File csvFile ) throws Exception
	{
		listeners = new Vector<TableModelListener>();
		rand = new Random();
		randStart = rand.nextInt(2756);
		
		rowData = new Vector<String[]>();
		methods = new Vector<String>();
		
		this.csvFile = csvFile;
		FileReader fr = new FileReader(csvFile);
		BufferedReader br = new BufferedReader(fr);

		// first line is header
		String line = br.readLine();
		header = StringUtils.split(line);
		line = br.readLine();
		while( line != null )
		{
			String[] row = StringUtils.split(line);
			if( row.length > 0 )
			{
				rowData.add(row);
			}
			line = br.readLine();
		}
		
		for( int i = 0; i < rowData.size(); ++i )
		{
			//setup a method
			int j = rand.nextInt(15);
			if( j == 0 )
			{
				methods.add("dynamite");
			}
			else if( j == 1 )
			{
				methods.add("boat electro-shocker");
			}
			else
			{
				methods.add("seine");
			}
		}
	}

	public int getColumnCount()
	{
		return 8;
	}

	public String getColumnName(int column)
	{
		return header[column];
	}

	public int getRowCount()
	{
		return rowData.size();
	}

	public Object getValueAt(int row, int column)
	{
		if( column == 0 )
		{
			int x = row + randStart;
			return "ACB-2006-" + x;
		}
		else if( column == 3 )
		{
			return methods.elementAt(row);
		}
		else if( column == 1 || column == 2 )
		{
			String[] r = rowData.get(row);
			return r[column-1];
		}
		else
		{
			String[] r = rowData.get(row);
			return r[column-2];
		}
	}

	public boolean isCellEditable(int row, int column)
	{
		return false;
	}

	public Class<?> getColumnClass(int columnIndex)
	{
		return String.class;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		return;
	}

	public void addTableModelListener(TableModelListener l)
	{
		listeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l)
	{
		listeners.remove(l);
	}
}
