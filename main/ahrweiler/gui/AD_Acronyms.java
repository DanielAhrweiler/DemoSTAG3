package ahrweiler.gui;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrAL;
import ahrweiler.support.FCI;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;


public class AD_Acronyms extends JFrame {

	private String[][] tableData;

	public AD_Acronyms(){
		tableData = AhrAL.toArr2D(AhrIO.scanFile("./../in/acronym_ref_table.txt", "~"));
		splitLines(tableData);
		drawGUI();
	}

	//split each cell text into multiple lines
	public void splitLines(String[][] data){
		for(int i = 0; i < data.length; i++){
			for(int j = 0; j < data[i].length; j++){
				if(j == 0){
					data[i][j] = "<html><p><br><b> "+data[i][j]+"</b></p></html>";
				}else{
					data[i][j] = "<html><p><br>"+data[i][j]+"</p></html>";
				}
			}
		}
	}
	
	//GUI for AutoDemo -> bAcronymRef
	public void drawGUI(){
		//lists and structs
		int[] rowHeights = new int[]{140, 140, 140, 140, 140, 140, 260, 140, 110, 110, 110, 110};

		//layout components
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setTitle("Acronym Reference Sheet");
		this.setSize(550, 600);	

		//column renderers
		DefaultTableCellRenderer topRenderer = new DefaultTableCellRenderer();
		topRenderer.setVerticalAlignment(JLabel.TOP);
		DefaultTableCellRenderer wrapRenderer = new DefaultTableCellRenderer();
		wrapRenderer.setBackground(Color.YELLOW);
		//TableCellRenderer altColRenderer = new AltColorCellRenderer();

		//components
		String[] refSheetHeader = new String[]{"Acronym", "Full Name", "Description"};
		DefaultTableModel dtmRefSheet = new DefaultTableModel(tableData, refSheetHeader);
		JTable tRefSheet = new JTable(dtmRefSheet);
		JScrollPane spRefSheet = new JScrollPane(tRefSheet, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		
		//basic functionality
		UIDefaults uiDefs = UIManager.getLookAndFeelDefaults();
		if(uiDefs.get("Table.alternateRowColor") == null){
			uiDefs.put("Table.alternateRowColor", new Color(235, 235, 235));
		}
		tRefSheet.getColumnModel().getColumn(0).setCellRenderer(topRenderer);
		tRefSheet.getColumnModel().getColumn(1).setCellRenderer(topRenderer);
		tRefSheet.getColumnModel().getColumn(2).setCellRenderer(topRenderer);
		tRefSheet.getColumnModel().getColumn(0).setPreferredWidth(100);
		tRefSheet.getColumnModel().getColumn(1).setPreferredWidth(170);
		tRefSheet.getColumnModel().getColumn(2).setPreferredWidth(280);
		for(int i = 0; i < tRefSheet.getRowCount(); i++){
			tRefSheet.setRowHeight(i, rowHeights[i]);
		}
		spRefSheet.getVerticalScrollBar().setUnitIncrement(16);


		//add everything
		this.add(spRefSheet);
		this.setVisible(true);
	}

}
