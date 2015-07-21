public class Category 
{
	String   name  ;
	Category parent;

	public Category()
	{

	}

	public Category(String name, Category parent)
	{
		this.name   = name;
		this.parent = parent;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setParent(Category parent)
	{
		this.parent = parent;
	}

	public String getName()
	{
		return name;
	}

	public Category getParent()
	{
		return parent;
	}
}
