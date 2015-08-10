public class Category 
{
	Integer  id    ;
	String   name  ;
	Category parent;
	Integer  parid ;

	public Category()
	{
	}

	public Category(Integer id)
	{
		this.id = id;
	}

	public Category(String name)
	{
		this.name = name;
	}

	public Category(String name, Category parent)
	{
		this.name   = name;
		this.parent = parent;
	}

	public Category(Integer id, String name)
	{
		this.id     = id;
		this.name   = name;
		this.parent = parent;
	}

	public Category(Integer id, String name, Integer parid)
	{
		this.id     = id;
		this.name   = name;
		this.parid  = parid; 
	}

	public Category(Integer id, String name, Category parent)
	{
		this.id     = id;
		this.name   = name;
		this.parent = parent;
	}

	public Integer getParId()
	{
		return parid;
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

	public Integer getId()
	{
		return id;
	}
}
