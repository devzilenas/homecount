import javax.swing.JFrame;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.Color;
import javax.swing.text.PlainDocument;
import javax.swing.text.Document;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import javax.sql.RowSetListener;
import javax.sql.rowset.CachedRowSet;
import javax.sql.RowSetEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.JTable;
import com.sun.rowset.CachedRowSetImpl;

public class HomeCountApp
	implements RowSetListener
{
	/**
	 * Main window.
	 */
	JFrame frame = null;
	Connection connection = null;
	IncomeExpenseTableModel IETableModel = null;
	//Income/expense table
	JTable table = null;

	public void setFrame(JFrame frame)
	{
		this.frame = frame;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public Connection getConnection()
	{
		return connection;
	}

	public JTable getTable()
	{
		return table;
	}

	public void setTable(JTable table)
	{
		this.table = table;
	}

	public void setIETableModel(IncomeExpenseTableModel IETableModel)
	{
		this.IETableModel = IETableModel;
	}

	public IncomeExpenseTableModel getIETableModel()
	{
		return IETableModel;
	}

	public JFrame getFrame()
	{
		return frame;
	}

	public static void main(String[] args)
	{
		System.out.println("It's run!");
		HomeCountApp hca = new HomeCountApp(); 
		hca.connectDB();
		hca.prepareFrame();
	}

	public void connectDB()
	{
		try
		{ 
			Class.forName("org.h2.Driver");
			setConnection(
					DriverManager.getConnection("jdbc:h2:~/homecount", "sa", ""));
		}
		catch (ClassNotFoundException | SQLException e)
		{
			System.out.println("DB connection error occured." + e);
		}
	}

	/**
	 * Show frame.
	 */
	public void prepareFrame()
	{ 
		JFrame frame = new JFrame("Main");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(
				new Dimension(600, 800));
		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);

		final Document logDocument = new PlainDocument();
		JTextArea      txtArea     = new JTextArea(logDocument);
		txtArea.setLineWrap(true);
		JScrollPane    scrollPane  = new JScrollPane(
				txtArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(scrollPane, BorderLayout.CENTER);

		JButton button1 = new JButton("Connect to db.");
		button1.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						try 
						{
							logDocument.insertString(0, "Key 1 pressed", null);
						}
						catch (BadLocationException bex)
						{
							System.exit(1);
						}
					}
				});
		JButton button2 = new JButton("Exit");
		button2.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							logDocument.insertString(0, "Key 2 pressed", null);
						}
						catch (BadLocationException bex)
						{
							System.exit(1);
						}
					}
				});
		Box buttonBox = new Box(BoxLayout.PAGE_AXIS);
		buttonBox.add(button1);
		buttonBox.add(button2);
		panel.add(buttonBox, BorderLayout.EAST);

		setIETableModel(
			new IncomeExpenseTableModel(
				getIncomeExpenseTableContents()));
		getIETableModel().addEventsHandlersToRowSet(this);

		setTable(new JTable());
		getTable().setModel(getIETableModel());

		panel.add(new JScrollPane(getTable()), BorderLayout.NORTH);

		frame.setVisible(true);
		frame.pack();
		setFrame(frame);
	}

	public CachedRowSet getIncomeExpenseTableContents()
	{
		CachedRowSet crs = null;
		try
		{ 
			crs = new CachedRowSetImpl();
			crs.setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
			crs.setConcurrency(ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = getConnection().createStatement().executeQuery(
					"SELECT amount, name, ondate FROM income_expense"
					);
			crs.populate(rs);
		}
		catch (SQLException e)
		{
			System.out.println("Error getting income/expense table data."+e);
		}

		return crs;
	}

	/**
	 * Setup JDBC.
	 */
	public class IncomeExpenseTableModel
		implements TableModel
	{
		CachedRowSet      rowSet;
		ResultSetMetaData metadata;
		int cols, rows;

		public CachedRowSet getRowSet()
		{
			return rowSet;
		}

		public void setCols(int cols)
		{
			this.cols = cols;
		}

		public void setRows(int rows)
		{
			this.rows = rows;
		} 

		public int getRows()
		{
			return rows;
		}

		public void setRowSet(CachedRowSet rowSet)
		{
			this.rowSet = rowSet;
		}

		public ResultSetMetaData getMetaData()
		{
			return metadata;
		}

		public void setMetadata(ResultSetMetaData metadata)
		{
			this.metadata = metadata;
		}

		public IncomeExpenseTableModel(CachedRowSet rowSet)
		{
			try
			{
				setRowSet(rowSet);
				setMetadata(getRowSet().getMetaData());
				setCols(getMetaData().getColumnCount());

				//Calculate rows
				getRowSet().beforeFirst();
				while (getRowSet().next())
				{ 
					setRows(getRows()+1);
				}
				getRowSet().beforeFirst(); 
			}
			catch (SQLException e)
			{
				System.out.println("Error creating income/expense table model."+e);
			}
		}

		protected void finalize()
		{
			close();
		}

		public void close()
		{
			try
			{
				getRowSet().getStatement().close();
			}
			catch (SQLException e)
			{
				System.out.println("Could not close statement."+e);
			}
		}

		public void addEventsHandlersToRowSet(RowSetListener listener)
		{
			getRowSet().addRowSetListener(listener);
		}

		public void addTableModelListener(TableModelListener l)
		{
			//Empty intentionaly
		}

		public Class getColumnClass(int column)
		{
			return String.class;
		}

		public int getColumnCount()
		{
			return cols;
		}

		public String getColumnName(int columnIndex)
		{
			String ret = String.format("Column%d", columnIndex + 1);
			try
			{
				ret = getMetaData().getColumnLabel(columnIndex+1);
			}
			catch (SQLException e)
			{
				ret = "error";
			}
			return ret;
		}

		public int getRowCount()
		{
			return rows;
		}

		public void insertRow(String amount, String name, String ondate)
		{
			try
			{
				getRowSet().moveToInsertRow();
				getRowSet().updateString("amount", amount);
				getRowSet().updateString("name"  , name  );
				getRowSet().updateString("ondate", ondate);
				getRowSet().insertRow();
				getRowSet().moveToCurrentRow();
			}
			catch (SQLException e)
			{
				System.out.println("Insert row error."+e);
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			Object ret = "";
			try
			{
				getRowSet().absolute(rowIndex+1);
				Object o = getRowSet().getObject(columnIndex+1);
				if (null != o)
				{
					ret = o.toString();
				}
			} 
			catch (SQLException e)
			{
				System.out.println("Get data error."+e);
			}
			return ret;
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return false;
		}

		public void removeTableModelListener(TableModelListener l)
		{
			//Empty intentionaly
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			System.out.format("Setting value for row %d column %d", rowIndex, columnIndex);
		}
	}

	public void rowChanged(RowSetEvent evt)
	{
		CachedRowSet currentRowSet = getIETableModel().getRowSet();
		try
		{
			currentRowSet.moveToCurrentRow();
			setIETableModel(
					new IncomeExpenseTableModel(
						getIETableModel().getRowSet()));
			getTable().setModel(getIETableModel());
		}
		catch (SQLException e)
		{
			System.out.println("Error constructing table model."+e);
		}
	}

	public void cursorMoved(RowSetEvent event)
	{
		// intentionally empty
	}
	public void rowSetChanged(RowSetEvent event)
	{
		// intentionally empty
	}
}
