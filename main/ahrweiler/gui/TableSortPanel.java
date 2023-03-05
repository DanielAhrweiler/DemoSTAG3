package ahrweiler.gui;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrDate;
import ahrweiler.support.FCI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;

//panel with table and built in column sorter
public class TableSortPanel extends JPanel {

	private Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	private String[] header;
	private String[][] data;
	private DefaultTableModel dtm;
	private JTable table;
	private int tableWidth;

	//----------------- CONSTRUCTORS --------------------
	//everything is already converted
	public TableSortPanel(String[][] data, String[] header){
		this.data = data;
		this.header = header;
		if(this.data.length == 0){
			String[][] emptyData = new String[1][this.header.length];
			for(int i = 0; i < this.header.length; i++){
				emptyData[0][i] = "";
			}
			this.data = emptyData;
		}
		dtm = new DefaultTableModel(this.data, this.header);
		table = new JTable(dtm);
		setColWidths();
		//run GUI stuff
		runGUI();
	}
	//file but no header given, header assumed to be 1st line in file
	public TableSortPanel(String inPath){
		ArrayList<ArrayList<String>> al = AhrIO.scanFile(inPath, ",");
		//convert table header
		this.header = new String[al.get(0).size()];
		if(al.get(0).get(0).contains("//")){
			for(int i = 0; i < al.get(0).size(); i++){
				String[] parts = al.get(0).get(i).split(" ");
				this.header[i] = parts[parts.length-1];
			}
		}else{
			System.out.println("WARNING: Actual Header? => " + al.get(0));
		}
		al.remove(0);
		//convert table data
		this.data = new String[al.size()][al.get(0).size()];
		for(int i = 0; i < al.size(); i++){
			for(int j = 0; j < al.get(i).size(); j++){
				this.data[i][j] = al.get(i).get(j);
			}
		}
		if(this.data.length == 0){
			String[][] emptyData = new String[1][this.header.length];
			for(int i = 0; i < this.header.length; i++){
				emptyData[0][i] = "";
			}
			this.data = emptyData;
		}
		dtm = new DefaultTableModel(data, header);
		table = new JTable(dtm);
		setColWidths();
		//run GUI stuff
		runGUI();	
	}
	//file and header is given
	public TableSortPanel(String inPath, String[] header){
		ArrayList<ArrayList<String>> al = AhrIO.scanFile(inPath, ",");
		this.header = header;
		//convert table data
		this.data = new String[al.size()][al.get(0).size()];
		for(int i = 0; i < al.size(); i++){
			for(int j = 0; j < al.get(i).size(); j++){
				this.data[i][j] = al.get(i).get(j);
			}
		}
		if(this.data.length == 0){
			String[][] emptyData = new String[1][this.header.length];
			for(int i = 0; i < this.header.length; i++){
				emptyData[0][i] = "";
			}
			this.data = emptyData;
		}
		dtm = new DefaultTableModel(this.data, this.header);
		table = new JTable(dtm);
		setColWidths();
		//run GUI stuff
		runGUI();
	}
	
	//---------------- Data Mangement -----------------
	//update model
	public void updateModel(String[][] data, String[] header){
		this.header = header;
		this.data = data;
		dtm = new DefaultTableModel(data, header);
		System.out.println("DTM   :\n  -> Cols = "+dtm.getColumnCount()+"\n  -> Rows = "+dtm.getRowCount());	
		table.setModel(dtm);
		System.out.println("Table :\n  -> Cols = "+table.getColumnCount()+"\n  -> Rows = "+table.getRowCount());	
		setColWidths();

	}
	//get width of table
	public int getTableWidth(){
		return this.tableWidth;
	}
	//add single row to table
	public void addRow(String[] row){
		dtm.addRow(row);
	}
	//update the header of the table
	public void setHeader(String[] newHeader){
		//this.
	}
	//update the data of the table
	public void setData(String[][] newData){
		//this.data = newData;
	}
	//set column widths according to data
	public void setColWidths(){
		int totWidth = 0;
		for(int i = 0; i < table.getColumnCount(); i++){
			int colLen = 0;
			int minLen = 3;
			for(int j = 0; j < table.getRowCount(); j++){
				String itrVal = table.getValueAt(j, i).toString();
				if(itrVal.length() > colLen){
					colLen = itrVal.length();
				}
			}
			if(colLen < minLen){
				colLen = minLen;
			}
			int itrWidth = colLen * 10;
			table.getColumnModel().getColumn(i).setMinWidth(itrWidth);
			table.getColumnModel().getColumn(i).setPreferredWidth(itrWidth);
			totWidth += itrWidth;
		}
		this.tableWidth = totWidth;
		System.out.println("--> In setColWidths() : totWidth = " + totWidth);
		table.setPreferredScrollableViewportSize(new Dimension(totWidth, 700));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	//--------------------- GUI -----------------------
	public void runGUI(){
		this.setLayout(new BorderLayout());
		//---------- Sort Panel ----------
		JPanel pSort = new JPanel();
		pSort.setLayout(new FlowLayout());
		pSort.setMaximumSize(new Dimension(2000, 50));
		//init components
		JLabel lbSortCol = new JLabel("Sort Col:");
		JComboBox cbSortCol = new JComboBox();
		JRadioButton rbAsc = new JRadioButton("Asc");
		JRadioButton rbDesc = new JRadioButton("Desc");
		ButtonGroup bgSort = new ButtonGroup();
		bgSort.add(rbAsc);
		bgSort.add(rbDesc);
		JButton bSort = new JButton("Sort");
		//component bounds
		lbSortCol.setPreferredSize(new Dimension(65, 25));
		cbSortCol.setPreferredSize(new Dimension(180, 25));
		rbAsc.setPreferredSize(new Dimension(55, 25));
		rbDesc.setPreferredSize(new Dimension(60, 25));
		bSort.setPreferredSize(new Dimension(60, 25));
		//look and feel, other basics
		rbAsc.setSelected(true); 
		rbAsc.setFont(plainFont);
		rbDesc.setFont(plainFont);
		setButtonStyle(bSort);
		for(int i = 0; i < this.header.length; i++){
			cbSortCol.addItem(this.header[i]);
		}

		//add everything to panel
		pSort.add(lbSortCol);
		pSort.add(cbSortCol);
		pSort.add(rbAsc);
		pSort.add(rbDesc);
		pSort.add(bSort);
		//----------- Table Data ----------
		JScrollPane spTable = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
									

		//listeners
		bSort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ArrayList<ArrayList<String>> dataAL = new ArrayList<ArrayList<String>>();
				int colIdx = cbSortCol.getSelectedIndex();
				//determine if col is date
				boolean is_date = false;
				//String colName = header[colIdx].toLowerCase();
				String colName = header[colIdx];
				colName = colName.replaceAll("\\s+", "");
				if(colName.equals("date") || colName.equals("dates")){
					is_date = true;
				}
				//determine if col is num
				boolean is_number = true;
				for(int i = 0; i < table.getRowCount(); i++){
					String itrVal = table.getValueAt(i, colIdx).toString();
					try{
						double dbVal = Double.parseDouble(itrVal);
					}catch(NumberFormatException ex){
						is_number = false;
						break;
					}
				}
				//sort based of data type of col
				if(is_date){
					boolean lr_first = true;
					if(rbDesc.isSelected()){
						lr_first = false;
					}
					dataAL = AhrAL.toAL2D(data);
					AhrDate.sortDates2D(dataAL, lr_first, colIdx);
				}else if(is_number){
					dataAL = AhrAL.toAL2D(data);
					Collections.sort(dataAL, new Comparator<ArrayList<String>>(){
						@Override
						public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
							double db1 = Double.parseDouble(obj1.get(colIdx));
							double db2 = Double.parseDouble(obj2.get(colIdx));
							if(rbAsc.isSelected()){
								return (Double.compare(db1, db2) * 1);
							}else{
								return (Double.compare(db1, db2) * -1);
							}
						}
					});
				}else{
					dataAL = AhrAL.toAL2D(data);
					Collections.sort(dataAL, new Comparator<ArrayList<String>>(){
						@Override
						public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
							String str1 = obj1.get(colIdx);
							String str2 = obj2.get(colIdx);
							if(rbAsc.isSelected()){
								return (str1.compareTo(str2) * -1);
							}else{
								return (str1.compareTo(str2) * 1);
							}
						}
					});
				}
				//set table to new value order
				//this.data = AhrAL.toArr2D(dataAL);
				for(int i = 0; i < table.getRowCount(); i++){
					for(int j = 0; j < table.getColumnCount(); j++){
						table.setValueAt(dataAL.get(i).get(j), i, j);
					}
				}
			}
		});
									
		this.add(pSort, BorderLayout.NORTH);
		this.add(spTable, BorderLayout.CENTER);
	}
	//GUI related, sets standard style to JButton
	private void setButtonStyle(JButton btn){
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}

}
