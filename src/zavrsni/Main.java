package zavrsni;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import postgres.database.tools.DatabaseConnection;
import postgres.database.tools.DownloadFastaFile;
import postgres.database.tools.GroupFastaFiles;
import postgresSeed.FileReader;

public class Main extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static JTextArea search;
	private static JComboBox<String> comboBox;
	private static List<String> userData = null;
	private static String[] info;
	private static JButton newSeq;
	private static JButton seq;
	private static Map<String, String> data;


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
					//PreparedStatement pstmt = connection.prepareStatement("select * from names, nodes, gencode, division where nodes.tax_id = names.tax_id "
					//			+ "and nodes.genetic_code_id = gencode.genetic_code_id and nodes.division_id = division.division_id and name_txt = ?;");
					PreparedStatement pstmt = connection.prepareStatement("select * from names, nodes left outer join reference_genomes \n"
							+ "on nodes.tax_id = reference_genomes.tax_id, gencode, division \n"
							+ "where nodes.genetic_code_id = gencode.genetic_code_id and nodes.division_id = division.division_id and nodes.tax_id = names.tax_id\n"
							+ "and lower(name_txt) = trim(lower(?));");
					pstmt.setString(1, element);
					ResultSet rs = pstmt.executeQuery();
					data = new HashMap<>();
					if (rs.next()) {
						for (int i = 0; i < info.length; i++) {
							data.put(info[i], rs.getString(info[i]));
						}
					}
					for (Map.Entry<String,String> entry : data.entrySet()) {
						System.out.println(entry.getKey() + ": " + entry.getValue());
					}
					if (data.get("file_location") == null) {
						newSeq.setVisible(true);
						seq.setVisible(false);
					} else {
						newSeq.setVisible(false);
						seq.setVisible(true);
					}
					/*
					pstmt = connection.prepareStatement("select distinct * from names, nodes as nodes1, nodes as nodes2, reference_genomes\n"
							+ "where names.tax_id = nodes1.tax_id and names.tax_id = nodes2.parent_tax_id and reference_genomes.tax_id = names.tax_id\n"
							+ "and lower(name_txt) = trim(lower(?))");
					pstmt.setString(1, element);
			        rs = pstmt.executeQuery();
			        if (rs.next()) {
			        	seq.setVisible(true);
			        } else {
			        	newSeq.setVisible(true);
			        }*/

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
					PreparedStatement pstmt = connection.prepareStatement("select * from names where lower(name_txt) like trim(lower(?));");
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
		//pack();
		setSize(800, 200);
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
			
		cp.setLayout(new GridLayout(6,0));

		JTextField text = new JTextField("Tražilica:");
		text.setEditable(false);
		cp.add(text);

		search = new JTextArea();

		cp.add(search);


		search.addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e) {
				Thread t = new Thread(new DbRunnable());
				t.start();
			}
		});

		comboBox = new JComboBox<String>();
		comboBox.setSize(800, 80);
		cp.add(comboBox);

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
		next.addActionListener(e -> {
			Thread t = new Thread(new DbRunnable(search.getText(), DbRunnable.SELECT));
			t.start();
		});
		cp.add(next);

		seq = new JButton("Preuzmi sekvencu genoma.");
		seq.setVisible(false);
		seq.addActionListener(saveFastaAction);
		cp.add(seq);
		newSeq = new JButton("Dodaj sekvencu genoma.");
		newSeq.setVisible(false);
		newSeq.addActionListener(addFastaAction);
		cp.add(newSeq);
	}

	private Action addFastaAction = new AbstractAction() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Odaberite .fasta datoteku!");
			fc.setMultiSelectionEnabled(true);
			if(fc.showOpenDialog(Main.this)!=JFileChooser.APPROVE_OPTION) 
				return;
			
			try {
				String name = Arrays.stream(data.get("name_txt").split(" ")).collect(Collectors.joining("_"));
				File[] files = fc.getSelectedFiles();
				if (files == null || files.length == 0) {
					return;
				} else if (files.length == 1) {
					DownloadFastaFile.download(name, 
							Files.readAllLines(files[0].toPath()), new File("src/resources/reference_genomes/"));
				} else {
					List<String> l = GroupFastaFiles.groupFiles(files);
					if (l == null || l.isEmpty())
						return;
					DownloadFastaFile.download(name, 
							l, new File("src/resources/reference_genomes/"));
				}
				
				Thread t = new Thread(new DbRunnable(name + ".fasta", DbRunnable.INSERT));
				t.start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
	};

	private Action saveFastaAction = new AbstractAction() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {

			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setDialogTitle("Odaberite direktorij u koji želite spremiti sekvencu!");
			if(jfc.showSaveDialog(Main.this)!=JFileChooser.APPROVE_OPTION) {
				JOptionPane.showMessageDialog(
						Main.this, 
						"Ništa nije spremljeno!", 
						"Upozorenje!", 
						JOptionPane.WARNING_MESSAGE);	
			}
			System.out.println(jfc.getSelectedFile());
			DownloadFastaFile.download(Arrays.stream(data.get("name_txt").split(" ")).collect(Collectors.joining("_")), 
					FileReader.readFile("reference_genomes/" + data.get("file_location")), jfc.getSelectedFile());
		}
	};


	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new Main().setVisible(true);
		});
	}



}
