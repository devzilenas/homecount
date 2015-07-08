import java.util.Observable;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/**
 * This class must be extended to provide readRow();
 *
 * @author Marius Žilėnas
 * @version 1.0
 * @since 2015-06-29
 */
public class TableSelector 
	extends Observable
{
	JTable           table;
	RowSetTableModel rstm ;

	//Stores currently selected object.
	Object current ;

	public TableSelector(JTable table, RowSetTableModel rstm)
	{
		this.table = table;
		this.rstm  = rstm ;
		addListener();
	}

	public AbstractTableModel getAbstractTableModel()
	{
		return (AbstractTableModel) getRowSetTableModel().getTableModel();
	}

	public void fireTableDataChanged()
	{
		getAbstractTableModel().fireTableDataChanged();
	}

	public void fireTableRowsInserted(int firstRow, int lastRow)
	{ 
		getAbstractTableModel().fireTableRowsInserted(firstRow, lastRow);
	}

	public void fireTableRowsUpdated(int firstRow, int lastRow)
	{
		getAbstractTableModel().fireTableRowsUpdated(firstRow, lastRow);
	}

	public JTable getTable()
	{
		return table;
	}

	public void setRowSetTableModel(RowSetTableModel rstm)
	{
		this.rstm = rstm;
	}

	public RowSetTableModel getRowSetTableModel()
	{
		return rstm;
	}

	public void setCurrent(Object current)
	{
		this.current = current;
		this.setChanged();
	}

	public Object getCurrent()
	{ 
		return current;
	}

	private void addListener()
	{
		getTable().getSelectionModel().addListSelectionListener(
			new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent e)
				{
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();
					if (!lsm.getValueIsAdjusting()) 
					{
						int selectedIndex = lsm.getMinSelectionIndex();
						int selected = getTable().convertRowIndexToModel(selectedIndex);
						if (-1 != selectedIndex)
						{
							System.out.format("Index: %d, selected: %d%n", selectedIndex, selected);
							setCurrent(readRow(selected));
							notifyObservers();
						}
					}
				}
			});
	}

	public Object readRow(int selected)
	{
		return null;
	}
}
