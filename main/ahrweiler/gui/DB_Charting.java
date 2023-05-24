package ahrweiler.gui;
import ahrweiler.Globals;
import ahrweiler.util.*;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import ahrweiler.support.SQLCode;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DB_Charting {

	Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	RCode rcode = new RCode();

	public DB_Charting(){
		drawGUI();
	}
	
	//GUI for Database -> Stock Ind Charting
	public void drawGUI(){
		//lists and overarching structs
		String[] cblExchanges = {"ALL", "AMEX", "NASDAQ", "NYSE", "OTHER"};
		String[] cblSectors = {"Financial [2xx]", "Healthcare [3xx]", "Technology [4xx]",
							"Industrials [5xx]", "Cons Cyc [6xx]", "Energy [7xx]", "Real Estate [8xx]",
							"Communication [9xx]", "Basic Mats [10xx]", "Cons Def [11xx]", "Utilities [12xx]"}; 
		String[] cblIndustries;
		String[] cblStockInds = {"Price", "Volume"};
		String[] cblIndexes = {"QQQ - Top NASDAQ", "SPY - Top S&P500", "DIA - Top Dow Jones"};
		String[] cblSpecial = {"Inflows by Sector", "Gold To Dow Ratio", "PM vs Intra"};
		File fr_data = new File(AhrIO.uniPath("./../data/r/rdata/db_chart.csv"));
		if(fr_data.exists()){
			fr_data.delete();
		}
		File fr_plot = new File(AhrIO.uniPath("./../resources/db_chart.png"));
		if(fr_plot.exists()){
			fr_plot.delete();
		}


		//layout components
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("Stock Charting");
		frame.setSize(1400, 800);
		frame.setLayout(null);
		JPanel pMainGraph = new JPanel();
		pMainGraph.setBounds(400, 10, 990, 700);
		pMainGraph.setBorder(BorderFactory.createTitledBorder("Graph"));
		pMainGraph.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel pGenFields = new JPanel();
		pGenFields.setBounds(10, 10, 380, 125);
		pGenFields.setBorder(BorderFactory.createTitledBorder("General Parameters"));
		pGenFields.setLayout(null);
		JPanel pAddComp = new JPanel();
		pAddComp.setBounds(10, 140, 380, 360);
		pAddComp.setBorder(BorderFactory.createTitledBorder("Add Component"));
		pAddComp.setLayout(null);
		JPanel pPopout = new JPanel();
		pPopout.setBounds(10, 670, 380, 90);
		pPopout.setBorder(BorderFactory.createTitledBorder("Popout Graphs"));
		pPopout.setLayout(null);

	
		//init comps
		JLabel lbPlot = new JLabel();
		JLabel lbSDate = new JLabel("Start Date:");				//General Params Panel
		JTextField tfSDate = new JTextField("2020-01-01");
		JLabel lbEDate = new JLabel("End Date:");
		JTextField tfEDate = new JTextField("2022-05-30");
		JLabel lbSMA = new JLabel("SMA Value:");
		JTextField tfSMA = new JTextField("1");
		ButtonGroup bgAddComp = new ButtonGroup();				//Add Comp Panel
		JRadioButton rbStock = new JRadioButton("Stock");
		JLabel lbQuant = new JLabel("Quantmod?");				
		ButtonGroup bgQuant = new ButtonGroup();
		JRadioButton rbQuantYes = new JRadioButton("Yes");
		JRadioButton rbQuantNo = new JRadioButton("No");
		bgQuant.add(rbQuantYes);
		bgQuant.add(rbQuantNo);
		JLabel lbStockTicker = new JLabel("Ticker:");
		JTextField tfStockTicker = new JTextField("");
		JLabel lbStockInds = new JLabel("Indicator:");
		JComboBox cbStockInds = new JComboBox();
		JRadioButton rbSecInd = new JRadioButton("Sector/Industry");
		JLabel lbSector = new JLabel("Sector:");	
		JComboBox cbSector = new JComboBox();
		JLabel lbIndustry = new JLabel("Industry:");
		JComboBox cbIndustry = new JComboBox();
		JRadioButton rbExchange = new JRadioButton("Exchange");
		JComboBox cbExchange = new JComboBox();	
		JRadioButton rbIndex = new JRadioButton("Index");
		JComboBox cbIndex = new JComboBox();
		bgAddComp.add(rbStock);
		bgAddComp.add(rbSecInd);
		bgAddComp.add(rbExchange);
		bgAddComp.add(rbIndex);
		JButton bAddComp = new JButton("Add To Chart");
		JLabel lbNormDate = new JLabel("Normalization Date:");	//Popout Panel
		JTextField tfNormDate = new JTextField("");
		JButton bNormGraph = new JButton("Graph");
		JLabel lbSpecial = new JLabel("Specials:");
		JComboBox cbSpecial = new JComboBox();
		JButton bSpecial = new JButton("Graph");
		JButton bClear = new JButton("Clear");					//bottom toolbar
		JLabel lbSeries = new JLabel("Series:");
		JComboBox cbListSeries = new JComboBox();	
		JButton bRemoveSeries = new JButton("Remove");
		JButton bDistnSeries = new JButton("Distn");

		//components bounds
		lbSDate.setBounds(10, 20, 100, 25);					//General Params Panel
		tfSDate.setBounds(120, 20, 100, 25);
		lbEDate.setBounds(10, 55, 100, 25);
		tfEDate.setBounds(120, 55, 100, 25);
		lbSMA.setBounds(10, 90, 100, 25);
		tfSMA.setBounds(120, 90, 100, 25);
		rbStock.setBounds(10, 20, 120, 20);					//Add Comp Panel
		lbQuant.setBounds(40, 50, 80, 25);
		rbQuantYes.setBounds(150, 50, 50, 25);
		rbQuantNo.setBounds(210, 50, 50, 25);
		lbStockTicker.setBounds(40, 80, 100, 25);
		tfStockTicker.setBounds(150, 80, 100, 25);
		lbStockInds.setBounds(40, 110, 100, 25);
		cbStockInds.setBounds(150, 110, 200, 25);
		rbSecInd.setBounds(10, 140, 180, 20);
		lbSector.setBounds(40, 170, 100, 20);
		cbSector.setBounds(150, 170, 200, 25);
		lbIndustry.setBounds(40, 200, 100, 25);
		cbIndustry.setBounds(150, 200, 200, 25);
		rbExchange.setBounds(10, 230, 120, 25);
		cbExchange.setBounds(150, 230, 200, 25);
		rbIndex.setBounds(10, 260, 120, 25);
		cbIndex.setBounds(150, 260, 200, 25);
		bAddComp.setBounds(10, 305, 360, 40);
		lbNormDate.setBounds(10, 20, 150, 25);				//Popout Panel
		tfNormDate.setBounds(170, 20, 100, 25);
		bNormGraph.setBounds(285, 20, 80, 25);
		lbSpecial.setBounds(10, 55, 80, 25);
		cbSpecial.setBounds(90, 55, 180, 25);
		bSpecial.setBounds(285, 55, 80, 25);
		bClear.setBounds(405, 720, 90, 30);					//bottom toolbar
		lbSeries.setBounds(590, 720, 70, 30);
		cbListSeries.setBounds(660, 720, 150, 30);
		bRemoveSeries.setBounds(820, 720, 100, 30);
		bDistnSeries.setBounds(940, 720, 100, 30);

		//basic functionality
		rbStock.setSelected(true);							//Add Comp Panel
		lbSector.setEnabled(false);
		cbSector.setEnabled(false);
		lbIndustry.setEnabled(false);
		cbIndustry.setEnabled(false);
		cbExchange.setEnabled(false);
		cbIndex.setEnabled(false);
		bSpecial.setEnabled(false);
		rbQuantNo.setSelected(true);
		setButtonStyle(bAddComp);
		setButtonStyle(bNormGraph);
		setButtonStyle(bSpecial);
		setButtonStyle(bClear);
		setButtonStyle(bRemoveSeries);
		for(int i = 0; i < cblStockInds.length; i++){
			cbStockInds.addItem(cblStockInds[i]);
		}
		for(int i = 0; i < cblSectors.length; i++){
			cbSector.addItem(cblSectors[i]);
		}
		cblIndustries = getIndustryList(cbSector.getSelectedIndex());
		for(int i = 0; i < cblIndustries.length; i++){
			cbIndustry.addItem(cblIndustries[i]);
		}
		for(int i = 0; i < cblExchanges.length; i++){
			cbExchange.addItem(cblExchanges[i]);
		}
		cbExchange.setSelectedIndex(1);
		for(int i = 0; i < cblIndexes.length; i++){
			cbIndex.addItem(cblIndexes[i]);
		}
		for(int i = 0; i < cblSpecial.length; i++){			//Popout Panel
			cbSpecial.addItem(cblSpecial[i]);
		}

		//listener functionality
		ActionListener alChangeComp = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(rbStock.isSelected()){
					lbQuant.setEnabled(true);
					rbQuantYes.setEnabled(true);
					rbQuantNo.setEnabled(true);
					lbStockTicker.setEnabled(true);
					tfStockTicker.setEnabled(true);
					lbStockInds.setEnabled(true);
					cbStockInds.setEnabled(true);
					lbSector.setEnabled(false);
					cbSector.setEnabled(false);
					lbIndustry.setEnabled(false);
					cbIndustry.setEnabled(false);
					cbExchange.setEnabled(false);
					cbIndex.setEnabled(false);
				}else if(rbSecInd.isSelected()){
					lbQuant.setEnabled(false);
					rbQuantYes.setEnabled(false);
					rbQuantNo.setEnabled(false);
					lbStockTicker.setEnabled(false);
					tfStockTicker.setEnabled(false);
					lbStockInds.setEnabled(false);
					cbStockInds.setEnabled(false);
					lbSector.setEnabled(true);
					cbSector.setEnabled(true);
					lbIndustry.setEnabled(true);
					cbIndustry.setEnabled(true);
					cbExchange.setEnabled(false);
					cbIndex.setEnabled(false);
				}else if(rbExchange.isSelected()){
					lbQuant.setEnabled(false);
					rbQuantYes.setEnabled(false);
					rbQuantNo.setEnabled(false);
					lbStockTicker.setEnabled(false);
					tfStockTicker.setEnabled(false);
					lbStockInds.setEnabled(false);
					cbStockInds.setEnabled(false);
					lbSector.setEnabled(false);
					cbSector.setEnabled(false);
					lbIndustry.setEnabled(false);
					cbIndustry.setEnabled(false);
					cbExchange.setEnabled(true);
					cbIndex.setEnabled(false);
				}else if(rbIndex.isSelected()){
					lbQuant.setEnabled(false);
					rbQuantYes.setEnabled(false);
					rbQuantNo.setEnabled(false);
					lbStockTicker.setEnabled(false);
					tfStockTicker.setEnabled(false);
					lbStockInds.setEnabled(false);
					cbStockInds.setEnabled(false);
					lbSector.setEnabled(false);
					cbSector.setEnabled(false);
					lbIndustry.setEnabled(false);
					cbIndustry.setEnabled(false);
					cbExchange.setEnabled(false);
					cbIndex.setEnabled(true);				
				}else{
					String message = "No RadioButton in Add Index is selected.";
					JOptionPane.showMessageDialog(frame, message, "Input Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		rbStock.addActionListener(alChangeComp);
		rbSecInd.addActionListener(alChangeComp);
		rbExchange.addActionListener(alChangeComp);
		rbIndex.addActionListener(alChangeComp);
		cbSector.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				String[] cbl = getIndustryList(cbSector.getSelectedIndex());
				cbIndustry.removeAllItems();
				for(int i = 0; i < cbl.length; i++){
					cbIndustry.addItem(cbl[i]);
				}
			}
		});
		bAddComp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//setup basic vars
				String[] indList = {"S/M_20", "S/M_10", "S/M_5", "S/M_2", "S/I_20", "S/I_10", "S/I_5",
							 "S/I_2", "SMA20", "SMA10", "SMA5", "SMA2", "RSI", "MACD", "MACDH", "CMF", "BBW",
							 "%B", "ROC", "MFI", "CCI", "Mass", "TSI", "Ult Osc"};
				int xdim = 980;
				int ydim = 680;
				String dataPath = AhrIO.uniPath("./../data/r/rdata/db_chart.csv");
				String plotPath = AhrIO.uniPath("./../resources/db_chart.png");
				String scriptPath = AhrIO.uniPath("./../data/r/rscripts/db_chart.R");
				tfNormDate.setText(tfSDate.getText());
				int sma = Integer.parseInt(tfSMA.getText());
				//get inputs from GUI
				String sdate = tfSDate.getText();
				String edate = tfEDate.getText();				
				//setup empty vars needed to calc for chart
				boolean redraw_chart = true;
				boolean quantmod_used = false;
				String csvColName = "";
				ArrayList<ArrayList<String>> newData = new ArrayList<ArrayList<String>>();				
				//calc the vars according to chosen option
				if(rbStock.isSelected()){
					//set column names
					String ticker = tfStockTicker.getText().toUpperCase();
					String dbPath = AhrIO.uniPath(Globals.intrinio_path+ticker+".txt");
					int indIdx = cbStockInds.getSelectedIndex();
					String dbColName = "adj_close";
					csvColName = ticker;
					File tickFile = new File(dbPath);
					if(tickFile.exists()){
						if(indIdx > 2){
							dbPath = AhrIO.uniPath(Globals.snorm_path+ticker+".txt");
						}
						if(indIdx == 1){
							dbColName = "adj_vol";
							csvColName += ".vol";
						}else if(indIdx == 2){
							dbColName = "adj_ratio";
							csvColName += ".adjr";
						}else if(indIdx > 1){
							dbColName = "ind" + String.valueOf(indIdx-2);
							csvColName += "." + indList[indIdx-2];
						}
						if(rbQuantYes.isSelected()){
							quantmod_used = true;
							RCode rcodeQM = new RCode();
							rcodeQM.addPackage("quantmod");
							rcodeQM.addCode("df <- read.delim.zoo(\""+dbPath+"\", "+
										"format=\"%Y-%m-%d\", sep=\"~\", header=FALSE)");
							rcodeQM.addCode("df <- df[,c(1,2,3,4,5,9)]");
							rcodeQM.addCode("colnames(df) <- c(\""+ticker+".Open\",\""+ticker+".High\",\""+ticker+
										".Low\",\""+ticker+".Close\",\""+ticker+".Volume\",\""+ticker+".Adjusted\")");
							rcodeQM.startPlot(plotPath, xdim, ydim);
							rcodeQM.addCode("chartSeries(df, type=\"bar\", subset=\'"+sdate+"::"+edate+
											"\', theme=chartTheme(\'black\'), name=\""+ticker+" Stock Price (daily)\")");
							rcodeQM.endPlot();
							//rcodeQM.printCode();
							rcodeQM.writeCode(scriptPath);
							rcodeQM.runScript(scriptPath);
							//show R plot
							ImageIcon ii = new ImageIcon(plotPath);
							lbPlot.setIcon(ii);
							frame.setVisible(true);
							ii.getImage().flush();	
						}else{
							FCI fciDB = new FCI(false, dbPath);
							int colIdx = fciDB.getIdx(dbColName);
							if(Globals.uses_mysql_source){
								String mysqlCol = "close";
								if(indIdx == 1){
									mysqlCol = "vol";
								}
								ArrayList<String> colNames = AhrAL.toAL(new String[]{"date", mysqlCol});
								newData = getWebSBaseDataForPlot(ticker, colNames, sdate, edate, sma);
							}else{
								newData = getLocalDataForPlot(dbPath, sdate, edate, colIdx, sma);
							}
							if(newData.size() < 1){
								redraw_chart = false;
								JOptionPane.showMessageDialog(frame, "Stock ticker not found in given date range.",
														"Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}else{
						redraw_chart = false;
						JOptionPane.showMessageDialog(frame, "Stock ticker not found.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}else if(rbSecInd.isSelected()){
						int secIdx = cbSector.getSelectedIndex();
						int indIdx = cbIndustry.getSelectedIndex();
						String siCode = String.valueOf(secIdx+2);
						String tname = "sector"+siCode;
						String cname = "dmc_close_";
						if(indIdx > 0){//is certain 1 industry
							String indPart = String.valueOf(indIdx);
							if(indPart.length() == 1){
								indPart = "0" + indPart;
							}
							siCode += indPart;
							cname += indPart;
						}else{//all industries in this sector
							siCode += "xx";
							cname += "xx";
						}
						csvColName = siCode;
						String dbPath = Globals.ibase_path;
						FCI fciIB = new FCI(false, dbPath);
						int colIdx = fciIB.getIdx("close");
						if(Globals.uses_mysql_source){
							ArrayList<String> colNames = AhrAL.toAL(new String[]{"date", cname});
							newData = getWebIBaseDataForPlot(tname, colNames, sdate, edate, sma);
						}else{
							String fname = siCode + ".txt";
							newData = getLocalDataForPlot(dbPath+fname, sdate, edate, colIdx, sma);				
						}
				}else if(rbExchange.isSelected()){
						String exchange = cbExchange.getSelectedItem().toString();
						String tname = "exchanges";
						String cname = "dmc_close_"+exchange.toLowerCase();
						csvColName = exchange.toUpperCase();
						String dbPath = Globals.mbase_path;
						FCI fciMB = new FCI(false, dbPath);
						int colIdx = fciMB.getIdx("close");
						if(Globals.uses_mysql_source){
							ArrayList<String> colNames = AhrAL.toAL(new String[]{"date", cname});
							newData = getWebMBaseDataForPlot("exchanges", colNames, sdate, edate, sma);
						}else{
							String fname = exchange+".txt";
							newData = getLocalDataForPlot(dbPath+fname, sdate, edate, colIdx, sma);				
						}
				}else if(rbIndex.isSelected()){
						String ticker = cbIndex.getSelectedItem().toString();
						ticker = ticker.replaceAll("\\s+", "").split("-")[0];
						csvColName = ticker;
						//String dbPath = "./../../DB_Intrinio/Main/S_Base/";
						String dbPath = Globals.intrinio_path;
						//FCI fciSB = new FCI(false, dbPath);
						FCI fciIT = new FCI(false, dbPath);
						//int colIdx = fciSB.getIdx("close");
						int colIdx = fciIT.getIdx("adj_close");
						if(Globals.uses_mysql_source){
							ArrayList<String> colNames = AhrAL.toAL(new String[]{"date", "close"});
							newData = getWebSBaseDataForPlot(ticker, colNames, sdate, edate, sma);
						}else{
							String fname = ticker + ".txt";
							newData = getLocalDataForPlot(dbPath+fname, sdate, edate, colIdx, sma);					
						}
				}else{
					String message = "No component selected.";
					JOptionPane.showMessageDialog(frame, message, "Input Error", JOptionPane.ERROR_MESSAGE);
				}
				if(sma > 1){
					csvColName += ".sma"+String.valueOf(sma);
				}
				//create rdata and rscript, run rscript to get plot
				//update chart with new data, only if not add stock w/ quantmod
				if(!quantmod_used){
					ArrayList<String> header = new ArrayList<String>();
					header.add("date");
					header.add(csvColName);
					newData.add(0, header);
					//if db_chart.csv exists , append new info upon the old
					if(fr_data.exists()){//add only newest data to existing file
						ArrayList<ArrayList<String>> oldData = AhrIO.scanFile(dataPath, ",");
						newData = AhrDTF.addToMelt(oldData, newData);
						AhrIO.writeToFile(dataPath, newData, ",");
					}else{				//rewrite whole file
						AhrIO.writeToFile(dataPath, AhrDTF.melt(newData, "date"), ",");	
					}
					//gen R code
					rcode.resetCode();
					rcode.setTitle("Stock Market Charting (daily)");
					rcode.setXLabel("Date");
					rcode.setYLabel("Value");
					rcode.createTimeSeries(dataPath, plotPath, xdim, ydim);
					//rcode.printCode();
					rcode.writeCode(scriptPath);
					rcode.runScript(scriptPath);
					//update GUI
					cbListSeries.addItem(csvColName);
					ImageIcon ii = new ImageIcon(plotPath);
					lbPlot.setIcon(ii);
					frame.setVisible(true);
					ii.getImage().flush();				
				}			
			}
		});
		bNormGraph.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String normDate = tfNormDate.getText().replaceAll("\\s+", "");
				int xdim = 900;
				int ydim = 650;
				String dataPath = AhrIO.uniPath("./../data/r/rdata/db_chart_norm.csv");
				String plotPath = AhrIO.uniPath("./../resources/db_chart_norm.png");
				String scriptPath = AhrIO.uniPath("./../data/r/rscripts/db_chart_norm.R");
				//get data for main chart, find the vars and first acceptable date
				String dbcPath = AhrIO.uniPath("./../data/r/rdata/db_chart.csv");
				ArrayList<ArrayList<String>> oldData = AhrIO.scanFile(dbcPath, ",");//melted format!
				ArrayList<String> header = oldData.get(0);			
				oldData.remove(0);
				String faDate = "1980-01-01";
				ArrayList<String> uniqVars = new ArrayList<String>();
				for(int i = oldData.size()-1; i > 0; i--){
					if(!uniqVars.contains(oldData.get(i).get(1))){
						uniqVars.add(oldData.get(i).get(1));
					}
					if(oldData.get(i).get(2).equals("NA") && i < (oldData.size()-1)){
						faDate = AhrDate.maxDate(faDate, oldData.get(i+1).get(0));
						//System.out.println("--> New faDate : " + faDate);
					}
				}
				ArrayList<ArrayList<String>> oldDataTmp = new ArrayList<ArrayList<String>>();
				for(int i = 0; i < oldData.size(); i++){
					String itrDate = oldData.get(i).get(0);
					if(AhrDate.compareDates(itrDate, faDate) != 1){
						oldDataTmp.add(oldData.get(i));
					}
				}
				oldData = oldDataTmp;
				//AhrAL.print(oldData);
				//go thru col names to find (native) SMA vals
				Collections.reverse(uniqVars);
				//System.out.println("--> UniqVars = " + uniqVars);
				int[] cprtIdx = new int[uniqVars.size()];
				int[] smaVals = new int[uniqVars.size()];
				for(int i = 0; i < uniqVars.size(); i++){
					int itrIdx = -1;
					int smaVal = -1;
					int periodCount = 0;
					for(int x = 0; x < uniqVars.get(i).length(); x++){
						if(uniqVars.get(i).charAt(x) == '.'){
							periodCount++;
						}
					}
					//System.out.println("--> Col Name : "+uniqVars.get(i)+"  |  periodCount = "+periodCount);
					if(periodCount > 0){
						String[] parts = uniqVars.get(i).split("\\.");
						if(parts[parts.length-1].contains("sma")){
							String firstPartOfName = parts[0];
							if(parts.length == 3){
								firstPartOfName += "."+parts[1];
							}
							for(int j = 0; j < uniqVars.size(); j++){
								if(uniqVars.get(j).equals(firstPartOfName)){
									itrIdx = j;
									break;
								}
							}
							smaVal = Integer.parseInt(parts[parts.length-1].replace("sma", ""));
						}
					}
					cprtIdx[i] = itrIdx;
					smaVals[i] = smaVal;
				}
				/*
				System.out.print("--> Counterpart Indexes : [");
				for(int i = 0; i < cprtIdx.length; i++){
					System.out.print(cprtIdx[i] + " ");
				}
				System.out.println("]");
				System.out.print("--> SMA Values : [");
				for(int i = 0; i < smaVals.length; i++){
					System.out.print(smaVals[i] + " ");
				}
				System.out.println("]");
				*/
				ArrayList<ArrayList<String>> normData = new ArrayList<ArrayList<String>>();
				//itr thru vars, calcing all non SMA vals first
				for(int i = 0; i < uniqVars.size(); i++){
					ArrayList<ArrayList<String>> srowsAll = AhrAL.getSelectRows(oldData, uniqVars.get(i), 1);
					ArrayList<ArrayList<String>> srows = new ArrayList<ArrayList<String>>();
					for(int j = 0; j < srowsAll.size(); j++){
						String itrDate = srowsAll.get(j).get(0);
						if(AhrDate.compareDates(itrDate, normDate) <= 0){
							srows.add(srowsAll.get(j));
						}					
					}
					if(cprtIdx[i] == -1){	//is normal val
						//setup var data struct, header, first line, and itrNormVal
						ArrayList<ArrayList<String>> varData = new ArrayList<ArrayList<String>>();
						varData.add(AhrAL.toAL(new String[]{"date", uniqVars.get(i)}));
						double itrNormVal = 100.0;
						ArrayList<String> firstLine = new ArrayList<String>();
						firstLine.add(srows.get(0).get(0));
						firstLine.add(String.format("%.2f", itrNormVal));
						varData.add(firstLine);
						//go thru all rows of variable, calc norm for each and add to varData
						for(int j = 1; j < srows.size(); j++){
							String itrDate = srows.get(j).get(0);
							double curVal = Double.parseDouble(srows.get(j).get(2));
							double prevVal = Double.parseDouble(srows.get(j-1).get(2));
							double multiplier = ((curVal - prevVal) / prevVal) + 1.0;
							itrNormVal = itrNormVal * multiplier;
							ArrayList<String> line = new ArrayList<String>();
							line.add(srows.get(j).get(0));
							line.add(String.format("%.2f", itrNormVal));
							varData.add(line);
						}
						if(normData.size() > 0){
							normData = AhrDTF.addToMelt(normData, varData);
						}else{
							normData = AhrDTF.melt(varData, "date");
						}
					}
				}
				//itr thru vars, calcing SMA vals
				for(int i = 0; i < uniqVars.size(); i++){
					if(cprtIdx[i] != -1){
						int sma = smaVals[i];
						//get rows from unnormalized data
						ArrayList<ArrayList<String>> srows = AhrAL.getSelectRows(normData, uniqVars.get(cprtIdx[i]), 1);
						//init var data struct and add header
						ArrayList<ArrayList<String>> varData = new ArrayList<ArrayList<String>>();
						varData.add(AhrAL.toAL(new String[]{"date", uniqVars.get(i)}));
						//itr thru selected rows 
						for(int j = sma-1; j < srows.size(); j++){
							double smaVal = 0.0;
							for(int k = 0; k < sma; k++){
								smaVal += Double.parseDouble(srows.get(j-k).get(2));
							}
							smaVal = smaVal / (double)sma;
							ArrayList<String> line = new ArrayList<String>();
							line.add(srows.get(j).get(0));
							line.add(String.format("%.2f", smaVal));
							varData.add(line);
						}
						normData = AhrDTF.addToMelt(normData, varData);
					}
				}

				AhrIO.writeToFile(dataPath, normData, ",");
				//create plot
				RCode rcodeNM = rcode;
				rcodeNM.setTitle("Normalized starting at "+normDate);
				rcodeNM.setXLabel("Date");
				rcodeNM.setYLabel("Normalized Values");
				rcodeNM.createTimeSeries(dataPath, plotPath, xdim, ydim);
				rcodeNM.writeCode(scriptPath);
				rcodeNM.runScript(scriptPath);
				//show norm chart in new popout frame
				JFrame rframe = new JFrame();
				rframe.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				rframe.setTitle("Normalized Data");
				JLabel lbPlot = new JLabel();
				lbPlot.setPreferredSize(new Dimension(xdim, ydim));
				ImageIcon ii = new ImageIcon(plotPath);
				lbPlot.setIcon(ii);
				rframe.getContentPane().add(lbPlot, BorderLayout.CENTER);
				rframe.pack();
				rframe.setVisible(true);
				ii.getImage().flush();
				//System.out.println("--> Popped out Normalization Chart.");
			}
		});
		bSpecial.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				
			}
		});
		bClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				rcode.resetColors();
				if(fr_data.exists()){
					fr_data.delete();
				}
				if(fr_plot.exists()){
					fr_plot.delete();
				}
				cbListSeries.removeAllItems();
				lbPlot.setIcon(new ImageIcon(""));
				frame.setVisible(true); 
			}
		});
		bRemoveSeries.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){	
				int xdim = 980;
				int ydim = 680;
				String dataPath = AhrIO.uniPath("./../data/r/rdata/db_chart.csv");
				String plotPath = AhrIO.uniPath("./../resources/db_chart.png");
				String scriptPath = AhrIO.uniPath("./../data/r/rscripts/db_chart.R");
				//remove line from combobox
				int selectedIdx = cbListSeries.getSelectedIndex();
				String selectedLine = cbListSeries.getSelectedItem().toString();
				ArrayList<ArrayList<String>> data = AhrIO.scanFile(dataPath, ",");
				data = AhrDTF.removeFromMelt(data, selectedLine);
				AhrIO.writeToFile(dataPath, data, ",");
				//gen R code
				rcode.setTitle("Stock Market Charting");
				rcode.setXLabel("Date");
				rcode.setYLabel("Value");
				rcode.removeColor(selectedIdx);
				rcode.createTimeSeries(dataPath, plotPath, xdim, ydim);
				//rcode.printCode();
				rcode.writeCode(scriptPath);
				rcode.runScript(scriptPath);
				//update GUI
				ImageIcon ii = new ImageIcon(plotPath);
				lbPlot.setIcon(ii);
				cbListSeries.removeItem(selectedLine);
				frame.setVisible(true);
				ii.getImage().flush();
			}
		});
		bDistnSeries.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){	
				//create pop-up graph showing histogram of distribution of selected indicator
			}
		});

		//add everything
		pMainGraph.add(lbPlot);
		pGenFields.add(lbSDate);
		pGenFields.add(tfSDate);
		pGenFields.add(lbEDate);
		pGenFields.add(tfEDate);
		pGenFields.add(lbSMA);
		pGenFields.add(tfSMA);
		pAddComp.add(rbStock);
		pAddComp.add(lbQuant);
		pAddComp.add(rbQuantYes);
		pAddComp.add(rbQuantNo);
		pAddComp.add(lbStockTicker);
		pAddComp.add(tfStockTicker);
		pAddComp.add(lbStockInds);
		pAddComp.add(cbStockInds);
		pAddComp.add(rbSecInd);
		pAddComp.add(lbSector);
		pAddComp.add(cbSector);
		pAddComp.add(lbIndustry);
		pAddComp.add(cbIndustry);
		pAddComp.add(rbExchange);
		pAddComp.add(cbExchange);
		pAddComp.add(rbIndex);
		pAddComp.add(cbIndex);
		pAddComp.add(bAddComp);


		pPopout.add(lbNormDate);
		pPopout.add(tfNormDate);
		pPopout.add(bNormGraph);
		pPopout.add(lbSpecial);
		pPopout.add(cbSpecial);
		pPopout.add(bSpecial);
		frame.add(pMainGraph);
		frame.add(pGenFields);
		frame.add(pAddComp);
		frame.add(pPopout);
		frame.add(bClear);
		frame.add(lbSeries);
		frame.add(cbListSeries);
		frame.add(bRemoveSeries);
		//frame.add(bDistnSeries);
		frame.setVisible(true);
	}
	//GUI related, sets specific style to a JButton
	public void setButtonStyle(JButton btn){
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}

	//getdata from local disk to chart
	public ArrayList<ArrayList<String>> getLocalDataForPlot(String dbPath, String sdate, String edate, int colIdx, int sma){
		dbPath = AhrIO.uniPath(dbPath);
		FCI fciDB = new FCI(false, dbPath);
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		data = AhrIO.scanColWithIndex(dbPath, "~", colIdx);
		ArrayList<ArrayList<String>> inPeriodData = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < data.size(); i++){
			String itrDate = data.get(i).get(fciDB.getIdx("date"));
			if(AhrDate.isDateInPeriod(itrDate, sdate, edate)){
				inPeriodData.add(data.get(i));
			}
		}
		data = inPeriodData;
		//order data AL in least recent to most recent date order
		Collections.sort(data, new Comparator<ArrayList<String>>(){
			@Override
			public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
				String str1 = obj1.get(0);
				String str2 = obj2.get(0);
				return str1.compareTo(str2);
			}
		});
		//System.out.println("***** In retreiveNewDataForPlot() *****");
		//System.out.println("--> 1st Date : " + data.get(0).get(0) + "\n--> Last Date : " + data.get(data.size()-1).get(0));
		if(sma > 1){
			ArrayList<ArrayList<String>> smaData = new ArrayList<ArrayList<String>>();
			for(int i = sma; i < data.size(); i++){
				double smaVal = 0.0;
				for(int j = 0; j < sma; j++){
					smaVal += Double.parseDouble(data.get(i-j).get(1));
				}
				smaVal = smaVal / (double)sma;
				ArrayList<String> line = new ArrayList<String>();
				line.add(data.get(i).get(0));
				line.add(String.format("%.3f", smaVal));
				smaData.add(line);		
			}
			data = smaData;
		}
		return data;
	}
	//gets data from MySQL (SBase db) web server to chart
	public ArrayList<ArrayList<String>> getWebSBaseDataForPlot(String tname, ArrayList<String> colNames,
																String sdate, String edate, int sma){
		SQLCode sqlc = new SQLCode("aws");
		sqlc.setDB("sbase");
		sqlc.setDateRange(sdate, edate);
		ArrayList<ArrayList<String>> data = sqlc.selectCols(tname, colNames);
		Collections.reverse(data);
		//AhrAL.print(data);
		if(sma > 1 && colNames.size() == 2 && data.size() >= sma){
			ArrayList<ArrayList<String>> smaData = calcSMA(data, sma);
			return smaData;
		}else{
			return data;
		}
	}
	//gets data from MySQL (SNorm db) web server to chart
	public ArrayList<ArrayList<String>> getWebSNormDataForPlot(String tname, ArrayList<String> colNames,
														String sdate, String edate, String narMask, int sma){
		SQLCode sqlc = new SQLCode("aws");
		sqlc.setDB("snorm");
		sqlc.setDateRange(sdate, edate);
		ArrayList<ArrayList<String>> data = sqlc.selectCols(tname, colNames);
		Collections.reverse(data);
		//AhrAL.print(data);
		if(sma > 1 && colNames.size() == 2 && data.size() >= sma){
			ArrayList<ArrayList<String>> smaData = calcSMA(data, sma);
			return smaData;
		}else{
			return data;
		}
	}
	//gets data from MySQL (IMBase) web server to chart
	public ArrayList<ArrayList<String>> getWebIBaseDataForPlot(String tname, ArrayList<String> colNames, String sdate,
																String edate, int sma){
		SQLCode sqlc = new SQLCode("aws");
		sqlc.setDB("ibase");
		sqlc.setDateRange(sdate, edate);
		ArrayList<ArrayList<String>> data = sqlc.selectCols(tname, colNames);
		Collections.reverse(data);
		//AhrAL.print(data);
		if(sma > 1 && colNames.size() == 2 && data.size() >= sma){
			ArrayList<ArrayList<String>> smaData = calcSMA(data, sma);
			return smaData;
		}else{
			return data;
		}
	}
	//gets data from MySQL (IMBase) web server to chart
	public ArrayList<ArrayList<String>> getWebMBaseDataForPlot(String tname, ArrayList<String> colNames, String sdate,
																						String edate, int sma){
		SQLCode sqlc = new SQLCode("aws");
		sqlc.setDB("mbase");
		sqlc.setDateRange(sdate, edate);
		ArrayList<ArrayList<String>> data = sqlc.selectCols(tname, colNames);
		Collections.reverse(data);
		//AhrAL.print(data);
		if(sma > 1 && colNames.size() == 2 && data.size() >= sma){
			ArrayList<ArrayList<String>> smaData = calcSMA(data, sma);
			return smaData;
		}else{
			return data;
		}
	}
	//calc SMA of a value col (assuming just a date col & value col), used in above 4 functs
	public ArrayList<ArrayList<String>> calcSMA(ArrayList<ArrayList<String>> data, int sma){
		ArrayList<ArrayList<String>> smaData = new ArrayList<ArrayList<String>>();
		int llIdx = data.size()-1;
		double sum = 0.0;
		for(int i = 0; i < sma; i++){
			sum += Double.parseDouble(data.get(llIdx-i).get(1));
		}
		ArrayList<String> firstLine = new ArrayList<String>();
		firstLine.add(data.get(llIdx-(sma-1)).get(0));
		firstLine.add(String.format("%.3f", sum/(double)sma));
		smaData.add(firstLine);
		for(int i = (llIdx-sma); i >= 0; i--){
			sum -= Double.parseDouble(data.get(i+sma).get(1));
			sum += Double.parseDouble(data.get(i).get(1));
			ArrayList<String> line = new ArrayList<String>();
			line.add(data.get(i).get(0));
			line.add(String.format("%.3f", sum/(double)sma));
			smaData.add(line);
		}
		return smaData;
	}


	//get list of industries given index from cbSector
	public String[] getIndustryList(int idx){
		String sector = "errNT";
		if(idx == 0){
			sector = "Financial Services";
		}else if(idx == 1){
			sector = "Healthcare";
		}else if(idx == 2){
			sector = "Technology";
		}else if(idx == 3){
			sector = "Industrials";
		}else if(idx == 4){
			sector = "Consumer Cyclical";
		}else if(idx == 5){
			sector = "Energy";
		}else if(idx == 6){
			sector = "Real Estate";
		}else if(idx == 7){
			sector = "Communication Services";
		}else if(idx == 8){
			sector = "Basic Materials";
		}else if(idx == 9){
			sector = "Consumer Defensive";
		}else if(idx == 10){
			sector = "Utilities";
		}else{
		}
		//itr thru ./../in/sector_codes.txt to get all industries for this sector
		String scPath = AhrIO.uniPath("./../in/sector_codes.txt");
		FCI fciSC = new FCI(false, scPath);
		ArrayList<ArrayList<String>> scodes = AhrIO.scanSelectRows(scPath, "~", sector, fciSC.getIdx("sector"));
		ArrayList<String> uniqInds = new ArrayList<String>();
		for(int i = 0; i < scodes.size(); i++){
			String itrInd = scodes.get(i).get(fciSC.getIdx("industry"));
			if(!uniqInds.contains(itrInd)){
				uniqInds.add(itrInd);
			}
		}
		//covert AL to string[] and return
		String[] indsInSec = new String[uniqInds.size()+1];
		indsInSec[0] = "All";
		for(int i = 0; i < uniqInds.size(); i++){
			indsInSec[i+1] = uniqInds.get(i);
		}
		return indsInSec;
	}

}
