import javax.swing.JComboBox;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.sql.*;
import javax.sql.*;
import javax.sql.rowset.JdbcRowSet  ;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.JTable;
import com.sun.rowset.JdbcRowSetImpl;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;

import javax.swing.JFormattedTextField;
import java.text.DateFormat;
import javax.swing.text.DateFormatter;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

import javax.swing.JLabel;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.SyncResolver;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import org.h2.tools.Server;

import java.util.Observer;
import java.util.Observable;

import java.util.Set;
import java.util.HashSet;

public class HomeCountApp
{
	/**
	 * Main window.
	 */
	JFrame     frame = null;
	Connection connection = null;

	TableSelector ts = null;

	//IERecordUI fields
	JTextField          nameTf;
	JFormattedTextField amountFtf, ondateFtf;

	//Filter fields
	JTextField yearTf, nameFTf; 
	JComboBox<Integer> monthCb = new JComboBox<Integer>(new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12});

	Server dbServer = null;

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

	public JFrame getFrame()
	{
		return frame;
	}

	void setDBServer(Server dbServer)
	{
		this.dbServer = dbServer;
	}

	Server getDBServer()
	{
		return dbServer;
	}

	public static void main(String[] args)
	{
		HomeCountApp hca = new HomeCountApp(); 
		hca.startTCPServer();
		hca.connectDB();
		//Create DB with a table if not found
		hca.setupDB(); 
		hca.prepareFrame();
	}

	void setupDB()
	{
		try 
		{
			PreparedStatement create = getConnection().prepareStatement(
					"CREATE TABLE IF NOT EXISTS income_expense ( id     INTEGER       NOT NULL AUTO_INCREMENT PRIMARY KEY, amount DECIMAL(10,2) NOT NULL, name   VARCHAR(255)  NOT NULL, ondate DATE          NOT NULL)");
			create.execute();
		}
		catch (SQLException e)
		{ 
			System.out.println("Error creating table " + e);
		}
	}

	void startTCPServer()
	{
		try
		{
			setDBServer(Server.createTcpServer("-tcpAllowOthers").start());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	void connectDB()
	{
		try
		{
			Class.forName("org.h2.Driver");
			setConnection(
					DriverManager.getConnection("jdbc:h2:tcp://localhost/~/homecount;AUTO_SERVER=TRUE", "sa", "")); 
		}
		catch (ClassNotFoundException | SQLException e)
		{
			System.out.println("DB connection error occured." + e);
		}
	}

	Statement newStatement()
	{
		Statement stmt = null;
		try
		{
			stmt = getConnection().createStatement(
					ResultSet.TYPE_SCROLL_SENSITIVE, 
					ResultSet.CONCUR_UPDATABLE); 
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return stmt;
	}

	RowSet makeRowSet(Statement stmt, String query)
	{
		RowSet rowSet = null;
		try
		{
			rowSet = new JdbcRowSetImpl(stmt.executeQuery(query));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return rowSet;
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

		final RowSetTableModel tm =
			new RowSetTableModel(
				new RowSetProvider()
				{
					Statement stmt  = newStatement();
					String    query = "SELECT * FROM income_expense WHERE YEAR(CONVERT(ondate, TIMESTAMP))=%d AND MONTH(CONVERT(ondate, TIMESTAMP))=%d AND name LIKE '%%%s%%'";
					Integer   year  = 2015;
					Integer   month = 6; 
					String    name  = "";
					RowSet    rs    = makeRowSet(); 
					
					public Set<Observer> observers = new HashSet<>();

					public void addObserver(Observer obs)
					{
						this.observers.add(obs);
					}
			        
			        public Set<Observer> getObservers()
					{
						return observers;
					} 

					public Integer getYear()
					{
						return year;
					}

					public void setYear(Integer year)
					{
						this.year = year;
					}

					public void setName(String name)
					{
						this.name = name;
					}

					public String getName()
					{
						return name;
					}

					public void setMonth(Integer month)
					{
						this.month = month;
					}

					public Integer getMonth()
					{
						return month;
					}

			        public void notifyObservers()
					{
						for (Observer observer : getObservers())
						{
							observer.update(new Observable(){},null);
						}
					}

			        private String getQuery()
					{
						return String.format(query, getYear(), getMonth(), getName());
					}

					private RowSet makeRowSet()
					{
						RowSet rowSet = null;
						try
						{
							rowSet = new JdbcRowSetImpl(
								stmt.executeQuery(getQuery()));
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
						return rowSet;
					}

					public void refreshRowSet()
					{ 
						setRowSet(makeRowSet());
					}

					private void setRowSet(RowSet rs)
					{
						this.rs = rs;
						notifyObservers();
					}

					public RowSet getRowSet()
					{
						return rs;
					}
				})
				{ 
					public void insertRow(Object o)
					{
						IERecord r = (IERecord) o;
						RowSet rs = getRowSet();
						try
						{
							rs.last();
							rs.moveToInsertRow();
							rs.updateBigDecimal("amount", r.getAmount());
							rs.updateString    ("name"  , r.getName()  );
							rs.updateDate      ("ondate", new java.sql.Date(
										r.getOndate().getTime()));
							rs.insertRow();
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
					}

					public void updateRow(Object o)
					{ 
						RowSet rs = getRowSet();
						IERecord r = (IERecord) o;
						try
						{
							rs.updateBigDecimal("amount", r.getAmount());
							rs.updateString(    "name"  , r.getName()  );
							rs.updateDate(      "ondate", new java.sql.Date(
										r.getOndate().getTime()));
							rs.updateRow();
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
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
				};

		JTable tableView = new JTable(tm.getTableModel()); 
		tableView.setDefaultRenderer(
				BigDecimal.class, 
				new DefaultTableCellRenderer()
				{
					public Component getTableCellRendererComponent(
						JTable table, Object value, boolean isSelected, 
						boolean hasFocus, int row, int column)
					{
						DecimalFormat formatter = new DecimalFormat("0.00");
						value = formatter.format((Number) value);

						return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					}
				});

		yearTf  = new JTextField(""+Calendar.getInstance().get(Calendar.YEAR));
		monthCb.setSelectedItem(Calendar.getInstance().get(Calendar.MONTH)+1);
		nameFTf = new JTextField();

		Box tl  = new Box(BoxLayout.Y_AXIS);
		Box tlf = new Box(BoxLayout.X_AXIS);

		yearTf.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					RowSetTableModel tm = ts.getRowSetTableModel();
					RowSetProvider rsp  = tm.getRowSetProvider();
					Integer year = Integer.valueOf(yearTf.getText());
					rsp.setYear(year);
					tm.refreshRowSet();  
					ts.fireTableDataChanged();
				}
			});

		monthCb.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					RowSetTableModel tm = ts.getRowSetTableModel();
					RowSetProvider rsp  = tm.getRowSetProvider();
					Integer month = (Integer) monthCb.getSelectedItem();
					rsp.setMonth(month);
					tm.refreshRowSet();  
					ts.fireTableDataChanged();
				}
			});

		nameFTf.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					RowSetTableModel tm = ts.getRowSetTableModel();
					RowSetProvider rsp  = tm.getRowSetProvider();
					String name         = nameFTf.getText();
					rsp.setName(name);
					tm.refreshRowSet();  
					ts.fireTableDataChanged();
				}
			});

		JPanel fp = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 0;
		c.gridwidth = 1;
		fp.add(new JLabel("Year"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 0;
		c.gridwidth = 1;
		fp.add(yearTf, c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 1;
		c.gridwidth = 1;
		fp.add(new JLabel("Month"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 1;
		c.gridwidth = 1;
		fp.add(monthCb, c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 2;
		c.gridwidth = 1;
		fp.add(new JLabel("Name"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 2;
		c.gridwidth = 1;
		fp.add(nameFTf, c);

		tm.getRowSetProvider().addObserver(
			new Observer()
			{
				public void update(Observable o, Object arg)
				{
				}
			});

		tlf.add(fp);
		tl.add(tlf);
		tl.add(new JScrollPane(tableView));
		panel.add(tl, BorderLayout.WEST);

		ts = new TableSelector(tableView, tm)
		{
			public Object readRow(int selected)
			{
				try
				{
					RowSet rs = getRowSetTableModel().getRowSet();
					rs.absolute(selected + 1);
					return new IERecord(
						rs.getBigDecimal("amount" ),
						rs.getString    ("name"   ),
						rs.getDate      ("ondate" ));
				} 
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				return null;
			}
		};
		ts.addObserver(
			new Observer()
			{
				public void update(Observable o, Object arg)
				{
					System.out.println("Observable updated");
					TableSelector ts = (TableSelector) o;
					System.out.println("IERecord: " + (IERecord)ts.getCurrent());
					setTextFields((IERecord) ts.getCurrent());
				}
			});

		JButton button1 = new JButton("Filter");
		tlf.add(button1);
		button1.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ 
						RowSetTableModel tm = ts.getRowSetTableModel(); 
						RowSetProvider rsp  = tm.getRowSetProvider();
						rsp.setName(nameFTf.getText());
						rsp.setYear(parseInt(yearTf.getText(), 2015));
						rsp.setMonth((Integer) monthCb.getSelectedItem());
						tm.refreshRowSet();  
						ts.fireTableDataChanged();
					}
				});

		JButton button3 = new JButton("Reset");
		tlf.add(button3);
		button3.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{ 
						Calendar cal = Calendar.getInstance();
						nameFTf.setText("");
						yearTf.setText(""+cal.get(Calendar.YEAR));
						monthCb.setSelectedItem(cal.get(Calendar.MONTH)+1);

						RowSetTableModel tm = ts.getRowSetTableModel(); 
						RowSetProvider rsp  = tm.getRowSetProvider();
						rsp.setName(nameFTf.getText());
						rsp.setYear(Integer.valueOf(yearTf.getText()));
						rsp.setMonth((Integer) monthCb.getSelectedItem());
						tm.refreshRowSet();  
						ts.fireTableDataChanged();
					}
				});
		JButton button2 = new JButton("Action 2");
		button2.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
					}
				});
		Box buttonBox = new Box(BoxLayout.PAGE_AXIS);
		buttonBox.add(button2);
	
		panel.add(buttonBox, BorderLayout.EAST);

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

		c = new GridBagConstraints();
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
		JButton newRow    = new JButton("New row");
		JButton insertRow = new JButton("Insert row");
		JButton updateRow = new JButton("Update row");
		JButton deleteRow = new JButton("Delete row");
		rpBBx.add(newRow);
		rpBBx.add(insertRow);
		rpBBx.add(updateRow);
		rpBBx.add(deleteRow);

		newRow.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					Calendar cal = Calendar.getInstance();
					IERecord r = new IERecord(
						BigDecimal.ZERO, 
						""             ,
						new java.sql.Date(System.currentTimeMillis()));
					setTextFields(r);
				}
			}
		);
		insertRow.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
				    {
						System.out.println("Inserting row");
						IERecord ier = readTextFields(); 
						RowSetTableModel tm = ts.getRowSetTableModel();
						tm.insertRow(
							readTextFields());
						tm.refreshRowSet();  
						ts.fireTableRowsInserted(
							tm.getTableModel().getRowCount()+1, 
							tm.getTableModel().getRowCount()+1);
					}
				});

		updateRow.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						//if has current selected row
						if (null != ts.getCurrent())
						{
							AbstractTableModel atm = (AbstractTableModel) ts.getRowSetTableModel().getTableModel();
							ts.getRowSetTableModel().updateRow(
								readTextFields());
							RowSet rs = ts.getRowSetTableModel().getRowSet();
							try
							{
								ts.fireTableRowsUpdated(rs.getRow()-1, rs.getRow()-1);
							}
							catch (SQLException e)
							{
								e.printStackTrace();
							}
						}
					}
				});

		deleteRow.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						System.out.println("Deleting row");
						if (-1 != -1)
						{
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

	public static class IERecord
	{
		Integer        id;
		BigDecimal     amount = BigDecimal.ZERO;
		String         name   = "";
		java.util.Date ondate = new java.util.Date();

		public IERecord()
		{
		}

		public IERecord(Integer id, BigDecimal amount, String name, java.util.Date ondate)
		{
			this.id = id;
			this.amount = amount;
			this.name   = name  ;
			this.ondate = ondate;
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
		
		public String toString()
		{
			DecimalFormat formatter = new DecimalFormat("0.00");
			return String.format(
					"%tF;%s;%s", 
					getOndate(),
					formatter.format((Number) getAmount()), 
					getName()
					);
		}
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
		ondateFtf.setValue(ieRecord.getOndate());
	}

	public Integer parseInt(String value, Integer defVal)
	{
		int retVal;
		try
		{
			retVal = Integer.valueOf(value);
		}
		catch (NumberFormatException e)
		{
			retVal = defVal;
		} 
		return retVal;
	}
}
