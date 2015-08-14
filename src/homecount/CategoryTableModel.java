import java.util.*;
import javax.swing.table.AbstractTableModel;

public class CategoryTableModel
	extends AbstractTableModel
{
	CategoryDAOImpl cdao = null;
	List<Category>  data = new LinkedList<>();

	public CategoryTableModel(CategoryDAOImpl cdao)
	{
		this.cdao = cdao;
		makeData();
	}

	CategoryDAOImpl getCDAO()
	{
		return cdao;
	}

	public List<Category> getData()
	{
		return data;
	}

	private void makeData()
	{
		data = getCDAO().getAll();
	}

	public Class<?> getColumnClass(int columnIndex)
	{
		return String.class;
	}

	public int getColumnCount()
	{
		return 3;
	}

	public int getRowCount()
	{
		return data.size();
	}

	public Object getValueAt(int row, int column)
	{
		Object value = null;
		Category cat = data.get(row);
		switch (column)
		{
			case 0:
				value = cat.getId();
				break;
			case 1:
				value = cat.getName();
				break;
			case 2:
				value = cat.getParent();
				break;
		}

		return value;
	}

	public String getColumnName(int column)
	{
		String name = "";
		switch (column)
		{
			case 0:
				name = "Category ID";
				break;
			case 1:
				name = "Category name";
				break;
			case 2:
				name = "Parent category";
				break;
		}
		return name;
	}

	public void refreshData()
	{
		makeData();
	}

	public void update(Category cat)
	{
		getCDAO().update(cat);
	}

	public void insertRow(Category cat)
	{
		getCDAO().save(cat);
	}
}
