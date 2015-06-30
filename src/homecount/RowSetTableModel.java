import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import javax.sql.RowSet;
import java.sql.SQLException;

/**
 * RowSetTableModel provides a 
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

	TableModel tableModel = new AbstractTableModel()
	{
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

		public Object getValueAt(int row, int column)
		{ 
			System.out.format("getting value %d, %d %n", row, column);
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
