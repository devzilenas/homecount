import javax.swing.DefaultComboBoxModel;

public class CategoryComboBoxModel<E>
	extends DefaultComboBoxModel<E>
{
	java.util.List<E> categories = null;
	{
		refreshData();
	}

	/**
	 * For override
	 */
	public void refreshData()
	{ 
	}

	public E getElementAt(int index)
	{
		return categories.get(index);
	}

	public int getSize()
	{
		return categories.size();
	}
}
