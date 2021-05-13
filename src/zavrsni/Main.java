package zavrsni;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.postgresql.util.PSQLException;

import postgres.database.tools.DatabaseConnection;

public class Main extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static JTextArea search;
	private static JComboBox<String> comboBox;
	private static List<String> userData = null;
	private static String[] info;
	private static Map<String, String> data;
	private boolean hint;


	static class DbRunnable implements Runnable{

		public final static int SELECT = 1;
		public final static int INSERT = 2;
		
		private String element;
		private int mode;

		DbRunnable(String element, int mode) {
			this.element = element;
			this.mode = mode;
		}

		DbRunnable() {
			this(null, 0);
		}

		@Override
		public void run() {
			if (mode == 0)
				dbSearch();
			else if (mode == 1)
				dbSelect();
			else 
				dbInsert();
		}

		private void dbInsert() {
			if (userData == null)
				return;
			try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + userData.get(0), userData.get(1), userData.get(2))){
				try {
					PreparedStatement pstmt = connection.prepareStatement("INSERT INTO reference_genomes VALUES(?,?) ON CONFLICT(tax_id) DO NOTHING;");
					pstmt.setInt(1, Integer.parseInt(data.get("tax_id")));
					pstmt.setString(2, element);
					pstmt.execute();

				} catch (SQLException ex) {
					ex.printStackTrace();
				}

			} catch (SQLException e) {
				System.out.println("Connection failure.");
				e.printStackTrace();

				try {
					DatabaseConnection.WrongData();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}			
		}

		private void dbSelect() {
			if (userData == null)
				return;
			try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + userData.get(0), userData.get(1), userData.get(2))){

				try {
					PreparedStatement pstmt = connection.prepareStatement("select * from names, nodes left outer join reference_genomes \n"
							+ "on nodes.tax_id = reference_genomes.tax_id, gencode, division \n"
							+ "where nodes.genetic_code_id = gencode.genetic_code_id and nodes.division_id = division.division_id and nodes.tax_id = names.tax_id\n"
							+ "and lower(name_txt) = trim(lower(?));");
					pstmt.setString(1, element);
					ResultSet rs = pstmt.executeQuery();
					if (rs.next()) {
						data = new HashMap<>();
						for (int i = 0; i < info.length; i++) {
							data.put(info[i], rs.getString(info[i]));
						}
					}
					if (data == null)
						return;
					if (data.get("file_location") == null) {
						pstmt = connection.prepareStatement("WITH RECURSIVE sub_tree AS (\n"
								+ "select nodes.tax_id, names.name_txt, nodes.parent_tax_id, file_location\n"
								+ "from names, nodes left outer join reference_genomes \n"
								+ "on nodes.tax_id = reference_genomes.tax_id, gencode, division \n"
								+ "where nodes.genetic_code_id = gencode.genetic_code_id and nodes.division_id = division.division_id and nodes.tax_id = names.tax_id\n"
								+ "and name_txt = ? and name_class = 'scientific name'\n"
								+ "	\n"
								+ "union all\n"
								+ "	\n"
								+ "select nod.tax_id, nam.name_txt, nod.parent_tax_id, gen.file_location \n"
								+ "from sub_tree st, names nam, nodes nod left outer join reference_genomes gen\n"
								+ "on nod.tax_id = gen.tax_id, gencode gc, division div\n"
								+ "where nod.genetic_code_id = gc.genetic_code_id and nod.division_id = div.division_id and nod.tax_id = nam.tax_id\n"
								+ "and st.parent_tax_id = nod.tax_id and nam.name_class = 'scientific name'\n"
								+ ")\n"
								+ "select * from sub_tree\n"
								+ "limit 10");
						pstmt.setString(1, element);
						try {
							rs = pstmt.executeQuery();
							while(rs.next()) {
								if (rs.getString("file_location") != null) {
									data.put("file_location", rs.getString("file_location"));
									data.put("parent_name", rs.getString("name_txt"));
									break;
								}
							}
						} catch (PSQLException ex) {
							System.out.println(ex);
						}
					} 

				} catch (SQLException ex) {
					ex.printStackTrace();
				}


			} catch (SQLException e) {
				System.out.println("Connection failure.");
				e.printStackTrace();

				try {
					DatabaseConnection.WrongData();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		}
		private void dbSearch() {
			if (userData == null)
				return;
			try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + userData.get(0), userData.get(1), userData.get(2))){

				try {
					PreparedStatement pstmt = connection.prepareStatement("select * from names where name_txt ilike ?;");
					pstmt.setString(1, search.getText() + "%");
					ResultSet rs = pstmt.executeQuery();
					List<String> names = new ArrayList<>();
					int counter = 0;
					while (rs.next()) {
						if (counter++ > 20)
							break;
						names.add(rs.getString("name_txt"));
					}
					if (comboBox.getItemCount() != 0)
						comboBox.removeAllItems();
					for (String s : names) 
						comboBox.addItem(s);	
				} catch (SQLException ex) {
					ex.printStackTrace();
				}


			} catch (SQLException e) {
				System.out.println("Connection failure.");
				e.printStackTrace();

				try {
					DatabaseConnection.WrongData();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public Main() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		initGUI();
		setSize(650, 200);
		setLocationRelativeTo(null);
		setTitle("Aplikacija za pohranu bioinformatičkih podataka u sustavu PostgreSQL");

		try {
			userData = DatabaseConnection.Connect();
			DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + userData.get(0), userData.get(1), userData.get(2));
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		info = new String[] {"tax_id", "name_txt", "unique_name", "name_class", "parent_tax_id", "rank", "embl_code",
				"division_id", "inherited_div_flag", "genetic_code_id", "inherited_GC_flag", "mitochondrial_genetic_code_id",
				"inheritedMGC_flag", "genbank_hidden_flag", "hidden_subtree_root_flag", "division_cde", "division_name", 
				"abbrevation", "name", "cde", "starts", "file_location"};

	}


	private void initGUI() {
		
		Container cp = this.getContentPane();
			
		cp.setLayout(new LayoutProzora());
		cp.setBackground(Color.CYAN);
		
		search = new JTextArea();
		search.setFont(search.getFont().deriveFont(20f)); 
		search.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), 
				BorderFactory.createLoweredBevelBorder()));
		checkHint();

		cp.add(search, LayoutProzora.FIRST_ELEMENT);
		search.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				checkHint();
				Thread t = new Thread(new DbRunnable());
				t.start();
				
			}
		});

		comboBox = new JComboBox<String>();
		
		cp.add(comboBox, LayoutProzora.SECOND_ELEMENT);

		comboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getModifiers() > 1) {
					search.setText(((JComboBox<?>)e.getSource()).getSelectedItem().toString());	
					Thread t = new Thread(new DbRunnable());
					t.start();		
					
					
				}
			}
		});

		JButton next = new JButton("Traži");
		next.setFont(next.getFont().deriveFont(15f));
		next.addActionListener(e -> {
			Thread t = new Thread(new DbRunnable(search.getText(), DbRunnable.SELECT));
			t.start();
			while (true) {
				try {
					t.join();
				} catch (InterruptedException ex) {
					continue;
				}
				break;
	 		}
			if (data != null) {
				@SuppressWarnings("unused")
				NewWindow newWindow = new NewWindow(data.get("name_txt"), data);
			}
		});
		cp.add(next, LayoutProzora.THIRD_ELEMENT);
	}

	protected void checkHint() {
		if (search.getText().length() == 0)
			hint = true;
		if (hint) {
			search.setForeground(Color.LIGHT_GRAY);
			search.setText("Upišite naziv organizma:");
			hint = false;
		} else {
			if (search.getText().equals("Upišite naziv organizma:"))
				search.setText("");
			search.setForeground(Color.BLACK);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new Main().setVisible(true);
		});
	}



}
