package ahrweiler.gui;
import ahrweiler.Globals;
import ahrweiler.util.*;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DB_DataIntegrity {

	public DB_DataIntegrity(){
			drawGUI();
	}

	public void drawGUI(){
		//lists and overarching structs
		int fxDim = 500;
		int fyDim = 440;

		//layout components
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("Data Integrity of DB_Intrinio");
		frame.setSize(fxDim, fyDim);
		frame.setLayout(null);
		JTabbedPane tpDI = new JTabbedPane();
		tpDI.setBounds(0, 0, fxDim, fyDim-37);
		JPanel pMC = new JPanel();
		pMC.setLayout(null);
		JPanel pTRI = new JPanel();
		pTRI.setLayout(null);
		
		/*---------------------------------------------------------------
			Market Cap Panel
		----------------------------------------------------------------*/
		//layout
		JPanel pSOT = new JPanel();
		pSOT.setBorder(BorderFactory.createTitledBorder("Std Out MC Tests"));
		pSOT.setBounds(10, 15, fxDim-20, 100);
		pSOT.setLayout(null);
		JPanel pCDS = new JPanel();
		pCDS.setBorder(BorderFactory.createTitledBorder("Compare SBase MC Datastreams"));
		pCDS.setBounds(10, 130, fxDim-20, 120);
		pCDS.setLayout(null);

		//init components
		JLabel lbAbnormalMC = new JLabel("Check Abnormal MCs :");
		Button bAbnormalMC = new Button("Test");
		JLabel lbDateMC = new JLabel("Check MCs on Date :");
		JTextField tfDateMC = new JTextField(AhrDate.getTodaysDate());
		Button bDateMC = new Button("Test");
		JLabel lbTickerSB = new JLabel("Ticker:");
		JTextField tfTickerSB = new JTextField("GME");
		JLabel lbSDateSB = new JLabel("Start Date:");
		JTextField tfSDateSB = new JTextField("2020-01-01");
		JLabel lbEDateSB = new JLabel("End Date:");
		JTextField tfEDateSB = new JTextField(AhrDate.getTodaysDate());
		Button bPlotSB = new Button("Plot");

		//components bounds
		lbAbnormalMC.setBounds(10, 20, 180, 25);
		bAbnormalMC.setBounds(280, 20, 60, 25);
		lbDateMC.setBounds(10, 50, 180, 25);
		tfDateMC.setBounds(190, 50, 80, 25);
		bDateMC.setBounds(280, 50, 60, 25);
		lbTickerSB.setBounds(10, 20, 80, 25);
		tfTickerSB.setBounds(110, 20, 80, 25);
		lbSDateSB.setBounds(10, 50, 80, 25);
		tfSDateSB.setBounds(110, 50, 80, 25);
		lbEDateSB.setBounds(210, 50, 80, 25);
		tfEDateSB.setBounds(310, 50, 80, 25);
		bPlotSB.setBounds(10, 80, 80, 30);

		//basic functionality

		//listener functionality
		bAbnormalMC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//TODO put function somewhere in demo java file
				//DB_Manager dbm = new DB_Manager();
				//dbm.checkAbnormalMC(false);
			}
		});
		bDateMC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String date = tfDateMC.getText().replaceAll("\\s+", "");
				ArrayList<ArrayList<String>> mc = new ArrayList<ArrayList<String>>();
				//TODO put function somewhere in demo java file
				//DB_Manager dbm = new DB_Manager();
				//ArrayList<ArrayList<String>> mc = dbm.singleDayMC(date);
				//AhrAL.print(mc);
			}
		});
		bPlotSB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
			}
		});

		//add
		pSOT.add(lbAbnormalMC);
		pSOT.add(bAbnormalMC);
		pSOT.add(lbDateMC);
		pSOT.add(tfDateMC);
		pSOT.add(bDateMC);
		pCDS.add(lbTickerSB);
		pCDS.add(tfTickerSB);
		pCDS.add(lbSDateSB);
		pCDS.add(tfSDateSB);
		pCDS.add(lbEDateSB);
		pCDS.add(tfEDateSB);
		pCDS.add(bPlotSB);
		pMC.add(pSOT);
		pMC.add(pCDS);		
		
		/*---------------------------------------------------------------
			Test Random Ind Panel
		----------------------------------------------------------------*/		
		//lists and over-arching structs
		Font monoFont = new Font(Font.MONOSPACED, Font.BOLD, 11);
		String[] indGroups = {"Any", "All SMAs", "All Non-SMAs", "SMA20 / M lvl SMA20", "SMA10 / M lvl SMA10",
							"SMA5 / M lvl SMA5", "SMA2 / M lvl SMA2", "SMA20 / I lvl SMA20", "SMA10 / I lvl SMA10",
							"SMA5 / I lvl SMA5", "SMA2 / I lvl SMA2", "SMA20", "SMA10", "SMA5", "SMA2", "RSI",
							"MACD", "MACD Histo", "CMF", "BBW", "%B", "ROC", "MFI", "CCI", "Mass Index", "TSI",
							"Ultimate Osc"};
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		//layout
		JPanel pIn = new JPanel();
		pIn.setBorder(BorderFactory.createTitledBorder("Input"));
		pIn.setBounds(10, 15, 270, 150);
		pIn.setLayout(null);
		JPanel pComp = new JPanel();
		pComp.setBorder(BorderFactory.createTitledBorder("Compare"));
		pComp.setBounds(10, 170, 270, 195);
		pComp.setLayout(null);
		JPanel pErrPlot = new JPanel();
		pErrPlot.setBorder(BorderFactory.createTitledBorder("Error Plot"));
		pErrPlot.setBounds(290, 10, 200, 360);
		pErrPlot.setLayout(null);
		
		//init components
		JLabel lbFocusInd = new JLabel("Focus Ind:");
		JComboBox cbFocusInd = new JComboBox();
		JLabel lbSDate = new JLabel("Start Date:");
		JTextField tfSDate = new JTextField("2012-01-01");
		JLabel lbEDate = new JLabel("End Date:");
		JTextField tfEDate = new JTextField(AhrDate.getTodaysDate());
		Button bGetVal = new Button("Get DB Value");
		JLabel lbInd = new JLabel("Ind   : ");
		JLabel lbStock = new JLabel("Stock : ");
		JLabel lbDate = new JLabel("Date  : ");
		JLabel lbValDB = new JLabel("DB Value     =  ");
		JLabel lbValAct = new JLabel("Actual Value =");
		JTextField tfValAct = new JTextField("");
		JLabel lbLastDiff = new JLabel("Last Diff    =  ");
		Button bSubmitVal = new Button("Submit Value");
		DefaultTableModel dtmErr = new DefaultTableModel();
		dtmErr.addColumn("ind");
		dtmErr.addColumn("error");
		JTable tErr = new JTable(dtmErr);
		JScrollPane spErr = new JScrollPane(tErr, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JLabel lbAvgErr = new JLabel("Avg Error = 0.00 %");
		Button bClearTable = new Button("Clear Table");


		//components bounds
		lbFocusInd.setBounds(10, 20, 95, 25);
		cbFocusInd.setBounds(100, 20, 160, 25);
		lbSDate.setBounds(10, 50, 95, 25);
		tfSDate.setBounds(100, 50, 80, 25);
		lbEDate.setBounds(10, 80, 95, 25);
		tfEDate.setBounds(100, 80, 80, 25);
		bGetVal.setBounds(10, 110, 140, 30);
		lbInd.setBounds(10, 20, 200, 17);
		lbStock.setBounds(10, 42, 200, 17);
		lbDate.setBounds(10, 64, 200, 17);
		lbValDB.setBounds(10, 88, 200, 17);
		lbValAct.setBounds(10, 110, 110, 17);
		tfValAct.setBounds(120, 110, 100, 17);
		lbLastDiff.setBounds(10, 134, 200, 17);
		bSubmitVal.setBounds(10, 156, 140, 30);
		tErr.setBounds(10, 20, 180, 270);
		spErr.setBounds(10, 20, 180, 270);
		lbAvgErr.setBounds(10, 290, 180, 25);
		bClearTable.setBounds(10, 320, 180, 25);
		

		//basic functionality
		for(int i = 0; i < indGroups.length; i++){
			cbFocusInd.addItem(indGroups[i]);
		}
		lbInd.setFont(monoFont);
		lbStock.setFont(monoFont);
		lbDate.setFont(monoFont);
		lbValDB.setFont(monoFont);
		lbValAct.setFont(monoFont);
		tfValAct.setFont(monoFont);
		lbLastDiff.setFont(monoFont);
		tErr.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		tErr.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		tErr.setAutoCreateRowSorter(true);
		lbAvgErr.setFont(monoFont);
		

		//listener functionality
		bGetVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//init vars and get vars from input panel
				String srPath = "./../../DB_Intrinio/Main/S_Raw/";
				FCI fciSR = new FCI(false, srPath);
				Random rnd = new Random();
				String indType = cbFocusInd.getSelectedItem().toString();
				String sdate = tfSDate.getText().replaceAll("\\s+", "");
				String edate = tfEDate.getText().replaceAll("\\s+", "");
				String indName = "";
				int totInds = Globals.sraw_num;
				int totSMAs = 12;
				//get col from S_Raw according to input
				int srcIdx = -1; 	//S_Raw col index
				HashMap<String, String> indMap = new HashMap<String, String>();
				for(int i = 3; i < indGroups.length; i++){
					String colName = "ind"+String.valueOf(i-3);
					indMap.put(indGroups[i], colName);
				}
				if(indType.equals("Any")){
					indName = indGroups[rnd.nextInt(indGroups.length-3)+3];
					srcIdx = fciSR.getIdx(indMap.get(indName));
				}else if(indType.equals("All SMAs")){
					indName = indGroups[rnd.nextInt(totSMAs)+3];
					srcIdx = fciSR.getIdx(indMap.get(indName));
				}else if(indType.equals("All Non-SMAs")){
					indName = indGroups[rnd.nextInt(indGroups.length-3-totSMAs)+3+totSMAs];
					srcIdx = fciSR.getIdx(indMap.get(indName));
				}else{
					indName = indType;
					srcIdx = fciSR.getIdx(indMap.get(indType));
				}
				//System.out.println("--> srcIdx = "+srcIdx);
				//retrieve val from DB
				String rndStock = "";
				String rndDate = "";
				String dbValStr = "NA";
				boolean good_db_val = false;
				boolean date_in_range = false;
				while(!good_db_val || !date_in_range){
					ArrayList<String> files = AhrIO.getFilesInPath(srPath);
					int fileIdx = rnd.nextInt(files.size());
					ArrayList<ArrayList<String>> fc = AhrIO.scanFile(srPath+files.get(fileIdx), "~");
					int rrIdx = rnd.nextInt(fc.size());	//rnd row index
					ArrayList<String> srRow = fc.get(rrIdx);
					dbValStr = srRow.get(srcIdx);
					rndStock = files.get(fileIdx).substring(0, files.get(fileIdx).length()-4);
					rndDate = srRow.get(fciSR.getIdx("date"));
					//update while loop checks
					if(!dbValStr.equals("NA") && !dbValStr.equals("NaN")){
						good_db_val = true;
					}else{
						good_db_val = false;
					}
					date_in_range = AhrDate.isDateInPeriod(rndDate, sdate, edate);
				}
				double dbVal = Double.parseDouble(dbValStr);
				lbValDB.setText("DB Value     =  " + String.format("%.5f", dbVal));
				lbInd.setText("Ind   : " + indName);
				lbStock.setText("Stock : " + rndStock);
				lbDate.setText("Date  : " + rndDate);
			}
		});
		bSubmitVal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String indName = lbInd.getText().replaceAll("\\s+","").split(":")[1];
				int indIdx = -1;
				for(int i = 0; i < indGroups.length; i++){
					String indGroupSpaceless = indGroups[i].replaceAll("\\s+","");
					if(indGroupSpaceless.equals(indName)){
						indIdx = i - 3;
					}
				}
				double actVal = Double.parseDouble(tfValAct.getText().replaceAll("\\s+",""));
				double dbVal = Double.parseDouble(lbValDB.getText().replaceAll("\\s+","").split("=")[1]);
				double diff = 0.0;
				if(dbVal != 0.0){
					diff = ((actVal - dbVal) / dbVal) * 100.0;
				}
				lbLastDiff.setText("Last Diff    =  " + String.format("%.3f", diff) + " %");
				//update table
				DefaultTableModel dtm = (DefaultTableModel)tErr.getModel();
				String[] row = new String[2];
				row[0] = String.valueOf(indIdx);
				row[1] = String.format("%.2f", diff);
				dtm.addRow(row);
				//update avg err label
				String avgErrStr = lbAvgErr.getText().replaceAll("\\s+","").split("=")[1];
				avgErrStr = avgErrStr.replace('%',Character.MIN_VALUE);
				double avgErr = Double.parseDouble(avgErrStr);
				avgErr = (avgErr+Math.abs(diff)) / (double)dtm.getRowCount();
				lbAvgErr.setText("Avg Error = "+String.format("%.2f",avgErr)+" %");
				//update GUI
				frame.revalidate();
				frame.repaint();
				frame.setVisible(true);
			}
		});
		bClearTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//clear table
				DefaultTableModel dtm = (DefaultTableModel)tErr.getModel();	
				while(dtm.getRowCount() > 0){
					dtm.removeRow(0);
				}
				lbAvgErr.setText("Avg Error = 0.00 %");
				//update GUI
				frame.revalidate();
				frame.repaint();
				frame.setVisible(true);
			}
		});


		//add everything
		pIn.add(lbFocusInd);
		pIn.add(cbFocusInd);
		pIn.add(lbSDate);
		pIn.add(tfSDate);
		pIn.add(lbEDate);
		pIn.add(tfEDate);
		pIn.add(bGetVal);
		pComp.add(lbInd);
		pComp.add(lbStock);
		pComp.add(lbDate);
		pComp.add(lbValDB);
		pComp.add(lbValAct);
		pComp.add(tfValAct);
		pComp.add(lbLastDiff);
		pComp.add(bSubmitVal);
		pErrPlot.add(spErr);
		pErrPlot.add(lbAvgErr);
		pErrPlot.add(bClearTable);
		pTRI.add(pIn);
		pTRI.add(pComp);
		pTRI.add(pErrPlot);
		tpDI.add("Test Market Cap", pMC);
		tpDI.add("Test Rnd Indicators", pTRI);
		frame.add(tpDI);
		frame.setVisible(true);
	}

}
