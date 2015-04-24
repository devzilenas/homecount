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
import java.sql.ResultSetMetaData;
import javax.sql.RowSetListener;
import javax.sql.rowset.CachedRowSet;
import javax.sql.RowSetEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.JTable;
import com.sun.rowset.CachedRowSetImpl;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ListSelectionModel;

import javax.swing.JFormattedTextField;
import java.text.DateFormat;
import javax.swing.text.DateFormatter;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.SyncResolver;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.DecimalFormat;

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

	//IERecordUI fields
	JTextField nameTf;
	JFormattedTextField amountFtf, ondateFtf;

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

	public void createIETable()
	{
		setIETableModel(
			new IncomeExpenseTableModel(
				getIncomeExpenseTableContents())); 
		getIETableModel().addEventsHandlersToRowSet(this);
		getTable().setModel(getIETableModel());
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

		JTable tt = new JTable();
		tt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tt.getSelectionModel().addListSelectionListener(
				new ListSelectionListener()
				{
					public void valueChanged(ListSelectionEvent e)
					{
						ListSelectionModel lsm = (ListSelectionModel) e.getSource();
						if (!lsm.getValueIsAdjusting())
						{
							int selectedIndex = lsm.getMinSelectionIndex();
							int selected      = getTable().convertRowIndexToModel(selectedIndex);
							setTextFields(readRow(selected));
						}
					}
				});
		setTable(tt);
		createIETable();

		panel.add(new JScrollPane(getTable()), BorderLayout.NORTH);

		//Record pane
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		amountFtf = new JFormattedTextField((DecimalFormat)nf);

		nameTf   = new JTextField();
		//For reference see p. 568 in "Java Swing the definitive guide".
		DateFormat    displayFormat    = new SimpleDateFormat("yyyy-MM-dd");
		DateFormatter displayFormatter = new DateFormatter(displayFormat);
		ondateFtf = new JFormattedTextField(displayFormatter);
		//clear all fields: get them filled with default values.
		clearTextFields();

		JPanel rp = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 0;
		c.gridwidth = 1;
		rp.add(new JLabel("Amount"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 0;
		c.gridwidth = 1;
		rp.add(amountFtf, c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 1;
		c.gridwidth = 1;
		rp.add(new JLabel("Name"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 1;
		c.gridwidth = 1;
		rp.add(nameTf, c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 2;
		c.gridwidth = 1;
		rp.add(new JLabel("On date"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 2;
		c.gridwidth = 1;
		rp.add(ondateFtf, c);

		Box rpBBx = new Box(BoxLayout.X_AXIS);
		JButton insertRow = new JButton("Insert row");
		JButton updateRow = new JButton("Update row");
		JButton updateDb  = new JButton("Update database");
		JButton discard   = new JButton("Discard");
		JButton deleteRow = new JButton("Delete row");
		rpBBx.add(insertRow);
		rpBBx.add(updateRow);
		rpBBx.add(updateDb);
		rpBBx.add(discard);
		rpBBx.add(deleteRow);

		insertRow.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
				    {
						System.out.println("Inserting row");
						IERecord ier = readTextFields();

						getIETableModel().insertRow(
							ier.getAmount(), 
							ier.getName()  ,
							ier.getOndate());
						syncWithTable();

					}
				});
		updateRow.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						System.out.println("Updating row"); 
						int selectedRow = getTable().getSelectedRow();
						if (-1 != selectedRow)
						{
							getIETableModel().updateRow(
								selectedRow, readTextFields());
							syncWithTable();
						}
					}
				});
		updateDb.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
				    {
						//updates database with data from JTable
						syncWithTable();
						System.out.println("Updating database"); 
					}
				});
		discard.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
				    {
						System.out.println("Clearing fields");
						clearTextFields();
						createIETable();
					}
				});

		deleteRow.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						System.out.println("Deleting row");
						int selectedRow = getTable().getSelectedRow();
						if (-1 != selectedRow)
						{
							getIETableModel().removeRow(selectedRow);
							syncWithTable();
						}
					}
				});

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridy     = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		rp.add(rpBBx, c);

		panel.add(rp, BorderLayout.SOUTH); 

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
			crs.setType(CachedRowSet.TYPE_SCROLL_INSENSITIVE);
			crs.setConcurrency(CachedRowSet.CONCUR_UPDATABLE);
			crs.setCommand(
					  "SELECT id, amount, name, ondate FROM income_expense"); 

			crs.setKeyColumns(new int[]{1});
			crs.execute(getConnection());
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

		public void removeRow(int row)
		{
			try
			{
				getRowSet().absolute(row+1);
				getRowSet().deleteRow();
			}
			catch (SQLException e)
			{
				System.out.println("Error deleting row."+e);
			}
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
			Class<?> cl = String.class;
			switch (column)
			{
				case 0:
					cl = Integer.class;
					break;
				case 1:
					cl = BigDecimal.class;
					break;
				case 2:
					cl = String.class;
					break;
				case 3:
					cl = String.class;
					break;
			}
			return cl;
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

		public void insertRow(BigDecimal amount, String name, java.util.Date ondate)
		{
			try
			{
				getRowSet().moveToInsertRow();
				getRowSet().updateNull("id");
				getRowSet().updateBigDecimal("amount", amount);
				getRowSet().updateString("name"  , name  );
				getRowSet().updateDate(  "ondate", 
						new java.sql.Date(ondate.getTime()));
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

		public void updateRow(int rowIndex, IERecord r)
		{
			try
			{
				getRowSet().absolute(rowIndex+1);
				getRowSet().updateBigDecimal("amount", r.getAmount());
				getRowSet().updateString("name"      , r.getName()  );
				getRowSet().updateDate("ondate"      , new java.sql.Date(r.getOndate().getTime()));
				getRowSet().updateRow();
			}
			catch (SQLException e)
			{
				System.out.println("Error updating row."+e);
			}
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
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

	public static class IERecord
	{
		BigDecimal     amount = BigDecimal.ZERO;
		String         name   = "";
		java.util.Date ondate = new java.util.Date();

		public IERecord()
		{
		}

		public IERecord(BigDecimal amount, String name, java.util.Date ondate)
		{
			this.amount = amount;
			this.name   = name  ;
			this.ondate = ondate;
		}

		public BigDecimal getAmount()
		{
			return amount;
		}

		public String getName()
		{
			return name;
		}

		public java.util.Date getOndate()
		{
			return ondate;
		}
	}

	public IERecord readRow(int rowIndex)
	{
		CachedRowSet crs = getIETableModel().getRowSet();
		IERecord r = new IERecord();
		try
		{
			crs.absolute(rowIndex+1);
			r = new IERecord(
					crs.getBigDecimal("amount"),
					crs.getString("name"),
					crs.getDate("ondate"));
		}
		catch (SQLException e)
		{
			System.out.println("Error reading row."+e);
		}
		return r;
	}

	//Fills text fields with default data
	public void clearTextFields()
	{
		setTextFields(new IERecord());
	}

	public IERecord readTextFields()
	{
		return new IERecord(
				new BigDecimal(((Number)amountFtf.getValue()).doubleValue()), 
				nameTf.getText(),
				(java.util.Date) ondateFtf.getValue());
	}

	public void setTextFields(IERecord ieRecord)
	{
		amountFtf.setValue(ieRecord.getAmount());
		nameTf.setText(ieRecord.getName());
		//***
		ondateFtf.setValue(ieRecord.getOndate());
	}

	public void syncWithTable()
	{
		try
		{
			// Must provide connection. Otherwise "Unable to get connection" occurs.
			getIETableModel().getRowSet().acceptChanges(getConnection());
		} 
		catch (SyncProviderException e)
		{
			System.out.println("Updating failed."+e);
			SyncResolver resolver = e.getSyncResolver();

			Object crsValue;  // value in the RowSet object
			Object resolverValue;  // value in the SyncResolver object
			Object resolvedValue;  // value to be persisted 
			try
			{
				while(resolver.nextConflict())
				{
					if(resolver.getStatus() == SyncResolver.UPDATE_ROW_CONFLICT)
					{
						int row = resolver.getRow();
						CachedRowSet crs = getIETableModel().getRowSet();
						crs.absolute(row);

						int colCount = crs.getMetaData().getColumnCount();
						for(int j = 1; j <= colCount; j++) 
						{
							if (resolver.getConflictValue(j) != null) 
							{
								crsValue = crs.getObject(j);
								resolverValue = resolver.getConflictValue(j);
								System.out.format("Crsvalue:%s , resolver:%s", crsValue, resolverValue);
								// compare crsValue and resolverValue to determine
								// which should be the resolved value (the value to persist)
								//resolvedValue = crsValue;
								//resolver.setResolvedValue(j, resolvedValue);
							}
						}
					}
				}
			}
			catch (SQLException ex)
			{
				System.out.println("Exception occured in resolver."+e);
			}
		}
	}
}
