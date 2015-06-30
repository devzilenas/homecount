import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import javax.sql.RowSet;
import java.sql.SQLException;

/**
 * RowSetTableModel provides an TableModel from RowSet.
 *
 * @author Marius Žilėnas
 * @version 1.0
 * @since 2015-06-29
 */
public class RowSetTableModel
{ 
	RowSet rowSet;

	public RowSetTableModel(RowSet rowSet)
	{
		this.rowSet = rowSet;
	}

	public RowSet getRowSet()
	{
		return rowSet;
	}

	public TableModel getTableModel()
	{
		return tableModel;
	}

	/**
	 * @override
	 */
	public Class getColumnClass(int column)
	{
		return String.class;
	}

	/**
	 * @override
	 */
	public void updateRow(Object o)
	{
	}

	TableModel tableModel = new AbstractTableModel()
	{
		public Class getColumnClass(int column)
		{
			return RowSetTableModel.this.getColumnClass(column);
		}

		public int getRowCount()
		{
			RowSet rowSet = getRowSet();
			try
			{
				int currentRow = rowSet.getRow();
				rowSet.last();
				int lastRow    = rowSet.getRow();

				if (0 < currentRow)
				{
					rowSet.absolute(currentRow);
				}
				return lastRow;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				return 0;
			}
		}

		public int getColumnCount()
		{
			int count = 0;
			try 
			{
				count = getRowSet().getMetaData().getColumnCount();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			return count;
		}

		public String getColumnName(int column)
		{
			int col = column + 1; //in JDBC columns start with 1
			String name = "";
			try 
			{
				name = getRowSet().getMetaData().getColumnName(col);
			}
			catch (SQLException e)
			{
				name = super.getColumnName(column);
				e.printStackTrace();
			}
			return name;
		}

		public Object getValueAt(int row, int column)
		{
			RowSet rowSet = getRowSet();
			Object value  = null;
			try 
			{
				rowSet.absolute(row + 1);
				value = rowSet.getObject(column + 1);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			return value;
		}
	};
}
