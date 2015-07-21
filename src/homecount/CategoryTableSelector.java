import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class CategoryTableSelector
	extends Observable
{
	Object current;
	JTable   table;
	CategoryTableModel ctm;

	public CategoryTableSelector(JTable table, CategoryTableModel ctm)
	{
		this.table = table;
		this.ctm   = ctm;
		addListener();
	}

	public JTable getTable()
	{
		return table;
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

	public CategoryTableModel getTableModel()
	{
		return ctm;
	}

	/**
	 * For @override
	 */
	public Object readRow(int selected)
	{
		return null;
	}
}
