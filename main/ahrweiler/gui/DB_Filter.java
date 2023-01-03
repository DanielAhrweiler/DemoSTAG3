package ahrweiler.gui;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrDate;
import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrGen;
import ahrweiler.support.FCI;
import ahrweiler.support.StockFilter;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DB_Filter extends JFrame {

	public DB_Filter(){
		drawGUI();
	}
	
	//GUI for Database -> Stock Filter
	public void drawGUI(){
		//lists and structs
		String[] industryList = {""};
		String[] indicatorList = {"S/M 20", "S/M 10", "S/M 5", "S/M 2", "S/I 20", "S/I 10", "S/I 5", "S/I 2", "SMA 20",
								"SMA 10", "SMA 5", "SMA 2", "RSI", "MACD", "MACD Histogram", "CMF", "Bollinger Bandwidth",
								"%B", "ROC", "MFI", "CCI", "Mass Index", "TSI", "Ult Osc"};
		ArrayList<ArrayList<String>> rules = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> fdAL = new ArrayList<ArrayList<String>>();

		StockFilter sf = new StockFilter();

		//layout components
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setTitle("STAG 3");
		this.setSize(1030, 600);
		this.setLayout(null);	
		JPanel pInputs = new JPanel();
		pInputs.setLayout(null);
		pInputs.setBorder(BorderFactory.createTitledBorder("Inputs"));
		JPanel pOutputs = new JPanel();
		pOutputs.setLayout(null);
		pOutputs.setBorder(BorderFactory.createTitledBorder("Outputs"));

		//components
		JLabel lbMC = new JLabel("Market Cap:");
		JTextField tfStartMC = new JTextField("300");
		JLabel lbMil1 = new JLabel("mil  to");
		JTextField tfEndMC = new JTextField("10000");
		JLabel lbMil2 = new JLabel("mil"); 
		JLabel lbSector = new JLabel("Sector:");
		JTextField tfSector = new JTextField("01,02,03,04,05,06,07,08,09,10,11,12");
		Button bSectorList = new Button("List");
		Button bSectorAll = new Button("All");
		JLabel lbIndustry = new JLabel("Industry:");
		JTextArea taIndustry = new JTextArea(2, 30);
		Button bIndustryAll = new Button("All");
		Button bIndustryList = new Button("List");
		JLabel lbIndicator = new JLabel("Indicator:");
		JComboBox cbIndicator = new JComboBox();
		JLabel lbIndRangeStart = new JLabel("Start:");
		JTextField tfIndRangeStart = new JTextField();
		JLabel lbIndRangeEnd = new JLabel("End:");
		JTextField tfIndRangeEnd = new JTextField();
		Button bIndAdd = new Button("Add");
		JLabel lbFilterDetails = new JLabel("Filter Details:");
		JTextArea taFilterDetails = new JTextArea();
		JScrollPane spFilterDetails = new JScrollPane(taFilterDetails, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		Button bApply = new Button("Apply Inputs");
		Button bReset = new Button("Reset Inputs");
		JLabel lbStockList = new JLabel("Stock List:");
		JLabel lbStockSort = new JLabel("Sort By:");
		JComboBox cbStockSort = new JComboBox();
		ButtonGroup bgSort = new ButtonGroup();
		JRadioButton rbAsc = new JRadioButton("Asc");
		JRadioButton rbDes = new JRadioButton("Des");
		bgSort.add(rbAsc);
		bgSort.add(rbDes);
		Button bStockSort = new Button("Sort");
		JTextArea taStockList = new JTextArea();
		JScrollPane spStockList = new JScrollPane(taStockList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		Button bToFile = new Button("Save To File");

		
		//component bounds
		pInputs.setBounds(10, 10, 515, 550);
		lbMC.setBounds(10, 20, 110, 25);
		tfStartMC.setBounds(110, 20, 100, 25);
		lbMil1.setBounds(220, 20, 60, 25);
		tfEndMC.setBounds(280, 20, 100, 25);
		lbMil2.setBounds(390, 20, 40, 25);
		lbSector.setBounds(10, 55, 110, 25);
		tfSector.setBounds(110, 55, 270, 25);
		bSectorList.setBounds(390, 60, 50, 25);
		bSectorAll.setBounds(450, 60, 50, 25);
		lbIndustry.setBounds(10, 90, 110, 25);
		taIndustry.setBounds(110, 90, 270, 25);
		bIndustryList.setBounds(390, 90, 50, 25);
		bIndustryAll.setBounds(450, 90, 50, 25);
		lbIndicator.setBounds(10, 140, 110, 25);
		cbIndicator.setBounds(110, 140, 270, 25);
		lbIndRangeStart.setBounds(50, 175, 55, 25);
		tfIndRangeStart.setBounds(110, 175, 90, 25);
		lbIndRangeEnd.setBounds(240, 175, 55, 25);
		tfIndRangeEnd.setBounds(290, 175, 90, 25);
		bIndAdd.setBounds(390, 160, 50, 25);
		lbFilterDetails.setBounds(10, 220, 120, 25);
		taFilterDetails.setBounds(50, 255, 450, 230);
		spFilterDetails.setBounds(50, 255, 450, 230);
		bApply.setBounds(100, 500, 150, 35);
		bReset.setBounds(300, 500, 150, 35);
		pOutputs.setBounds(540, 10, 480, 550);
		lbStockList.setBounds(10, 10, 300, 25);
		lbStockSort.setBounds(10, 40, 90, 25);
		cbStockSort.setBounds(90, 40, 150, 25);
		rbAsc.setBounds(260, 40, 55, 25);
		rbDes.setBounds(325, 40, 55, 25);
		bStockSort.setBounds(390, 40, 50, 25);
		taStockList.setBounds(10, 90, 460, 400);
		spStockList.setBounds(10, 90, 460, 400);
		bToFile.setBounds(10, 500, 150, 35);

		//basic functionality
		taIndustry.setLineWrap(true);
		taIndustry.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		for(int i = 0; i < indicatorList.length; i++){
			cbIndicator.addItem(indicatorList[i]);
		}
		taFilterDetails.setLineWrap(true);
		taFilterDetails.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		taFilterDetails.setText(sf.getText());
		System.out.println("--> Filter Text : " + sf.getText());
		rbDes.setSelected(true);
		taStockList.setLineWrap(true);
		taStockList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		//init starting filter lines
		sf.setMarketCap(Integer.parseInt(tfStartMC.getText()), Integer.parseInt(tfEndMC.getText()));
		sf.setSectors(tfSector.getText());
		sf.setIndustries(taIndustry.getText());

		//button functionality
		bSectorList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ArrayList<ArrayList<String>> scFC = AhrIO.scanFile("./../in/sector_codes.txt", "~");
				FCI fciSC = new FCI(false, "./../in/sector_codes.txt");
				ArrayList<String> uniqSectors = new ArrayList<String>();
				for(int i = 0; i < scFC.size(); i++){
					String itrSector = scFC.get(i).get(fciSC.getIdx("sector"));
					if(!uniqSectors.contains(itrSector)){
						uniqSectors.add(itrSector);
					}
				}
				//print out
				System.out.println("******* Sector List *******");
				for(int i = 0; i < uniqSectors.size(); i++){
					System.out.println("   "+(i+1)+ ") " + uniqSectors.get(i));
				}
			}
		});
		bSectorAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//TODO add dialog box in case button is pressed while tf is already at full len
				String scPath = "./../in/sector_codes.txt";
				FCI fciSC = new FCI(false, scPath);
				ArrayList<String> scSectors = AhrIO.scanCol(scPath, "~", fciSC.getIdx("sector"));
				HashSet<String> uniqSec = new HashSet<String>();
				for(int i = 0; i < scSectors.size(); i++){
					uniqSec.add(scSectors.get(i));
				}
				String secStr = "";
				for(int i = 0; i < uniqSec.size(); i++){
					if(i == uniqSec.size()-1){
						secStr += String.format("%02d", (i+1));
					}else{
						secStr += String.format("%02d", (i+1)) + ",";
					}
				}
				tfSector.setText(secStr);
			}
		});
		bIndustryList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				boolean is_good_text = false;
				String[] sectorCodes = {""};
				if(!tfSector.getText().equals("")){
					sectorCodes = tfSector.getText().split(",");
					if(sectorCodes.length == 1){
						is_good_text = true;
					}
				}
				if(is_good_text){
					String secName = "";
					String secStr = tfSector.getText().replaceAll("\\s+", "");
					secStr = secStr.replaceAll(",", "");
					int secInt = Integer.parseInt(secStr);
					System.out.println("--> secInt = " + secInt);
					ArrayList<String> uniqSectors = new ArrayList<String>();
					ArrayList<String> uniqInds = new ArrayList<String>();
					FCI fciSC = new FCI(false, "./../in/sector_codes.txt");
					ArrayList<ArrayList<String>> scFC = AhrIO.scanFile("./../in/sector_codes.txt", "~");
					for(int i = 0; i < scFC.size(); i++){
						String itrSector = scFC.get(i).get(fciSC.getIdx("sector"));
						if(!uniqSectors.contains(itrSector)){
							uniqSectors.add(itrSector);
						}
						if(uniqSectors.size() == secInt){
							secName = scFC.get(i).get(fciSC.getIdx("sector"));
							String itrInd = scFC.get(i).get(fciSC.getIdx("industry"));
							uniqInds.add(itrInd);
						}
					}
					System.out.println("******* Industries within "+secName+" *******");
					for(int i = 0; i < uniqInds.size(); i++){
						System.out.println("   "+(i+1)+") "+uniqInds.get(i));
					}
				}else{
					System.out.println("ERR: only 1 industry must be selected for this filter to work.");
				}
			}	
		});
		bIndustryAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				boolean is_good_text = false;
				String[] sectorCodes = {""};
				if(!tfSector.getText().equals("")){
					sectorCodes = tfSector.getText().split(",");
					if(sectorCodes.length == 1){
						is_good_text = true;
					}
				}
				if(is_good_text){
					int nearest100th = Integer.parseInt(tfSector.getText()) * 100;
					ArrayList<ArrayList<String>> scFC = AhrIO.scanFile("./../in/sector_codes.txt", "~");
					FCI fciSC = new FCI(false, "./../in/sector_codes.txt");				
					ArrayList<String> subcodes = new ArrayList<String>();
					for(int i = 0; i < scFC.size(); i++){
						int itrCode = Integer.parseInt(scFC.get(i).get(fciSC.getIdx("code")));
						if((itrCode-nearest100th) > 0 && (itrCode-nearest100th) < 100){
							String itrSubcode = String.valueOf(itrCode-nearest100th);
							if(itrSubcode.length() == 1){
								itrSubcode = "0" + itrSubcode;
							}
							subcodes.add(itrSubcode);
						}
					}
					//set taSector to subcodes
					String strSubcodes = "";
					for(int i = 0; i < subcodes.size(); i++){
						strSubcodes += subcodes.get(i);
						if(i != (subcodes.size()-1)){
							strSubcodes += ",";
						}
					}
					taIndustry.setText(strSubcodes);
				}else{
					System.out.println("ERR: only 1 industry must be selected for this filter to work.");
				}
			}
		});
		bIndAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//add to rules 
				String indNum = "ind";
				indNum += String.valueOf(cbIndicator.getSelectedIndex());
				String rngStart = tfIndRangeStart.getText();
				String rngEnd = tfIndRangeEnd.getText();
				if(rngStart.matches("[0-9]+") && rngEnd.matches("[0-9]+")){
					sf.addIndicatorFilter(AhrAL.toAL(new String[]{indNum, rngStart, rngEnd}));
					taFilterDetails.setText(sf.getText());
				}else{
					System.out.println("ERR: indicator values can only be numbers.");
				}
			}
		});
		bApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//print out all non-indicator filters to taFilterDetails
				String mcStart = tfStartMC.getText();
				String mcEnd = tfEndMC.getText();
				String rawSecStr = tfSector.getText();
				String rawIndStr = taIndustry.getText();
				boolean good_vals = true;
				if(!AhrGen.isInt(mcStart) || !AhrGen.isInt(mcEnd)){
					System.out.println("ERR: market cap values must be integers.");
					good_vals = false;
				}
				if(!rawSecStr.replace(",","").matches("[0-9]+")){
					System.out.println("ERR: industry values must be comma seperated integers");
					good_vals = false;
				} 
				if(!rawIndStr.replace(",","").matches("[0-9]+") && !rawIndStr.equals("")){
					System.out.println("ERR: sector values must be comma seperated integers.");
					good_vals = false;
				}
				if(good_vals){
					sf.clearSectorCodes();
					sf.clearResults();
					sf.setMarketCap(Integer.parseInt(mcStart), Integer.parseInt(mcEnd));
					sf.setSectors(rawSecStr);
					sf.setIndustries(rawIndStr);
					taFilterDetails.setText(sf.getText());
					setVisible(true);
					String mrDate = AhrDate.mostRecentDate(AhrIO.getNamesInPath("./../../DB_Intrinio/Clean/ByDate/"));
					sf.applyFilter(mrDate);
					ArrayList<ArrayList<String>> res = formatStockList(sf.getResults());
					lbStockList.setText("Stock List: ("+res.size()+" results)");
					//reset and print stocks to taStockList
					taStockList.setText("");
					for(int i = 0; i < res.size(); i++){
						if(i==0){
							taStockList.append(res.get(i).get(0));
						}else{
							taStockList.append("\n"+res.get(i).get(0));
						}
					}
					//add to cbStockSort
					cbStockSort.removeAllItems();
					cbStockSort.addItem("ticker");
					cbStockSort.addItem("market cap");
					cbStockSort.addItem("sector code");
					ArrayList<String> inds = sf.getIndicators();
					for(int i = 0; i < inds.size(); i++){
						if(!inds.get(i).equals("industry")){
							cbStockSort.addItem(inds.get(i));
						}
					}
				}
				
			}
		});
		bReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				sf.resetFilter();
				taFilterDetails.setText(sf.getText());
				taStockList.setText("");
				lbStockList.setText("Stock List:");
				cbStockSort.removeAllItems();
			}
		});
		bStockSort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				int colIdx = cbStockSort.getSelectedIndex();
				boolean is_asc = rbAsc.isSelected();
				ArrayList<ArrayList<String>> sorted = sortStockList(sf.getResults(), colIdx, is_asc);
				sorted = formatStockList(sorted);
				taStockList.setText("");
				for(int i = 0; i < sorted.size(); i++){
					taStockList.append(sorted.get(i)+"\n");
				}
			}
		});	
		bToFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String tfPath = "./../data/filters/sfilter_";
				int maxFileNum = -1;
				ArrayList<String> sfFiles = AhrIO.getNamesInPath("./../data/filters/");
				for(int i = 0; i < sfFiles.size(); i++){
					int itrFileNum = Integer.parseInt(sfFiles.get(i).split("_")[1]);
					if(itrFileNum > maxFileNum){
						maxFileNum = itrFileNum;
					}
				}
				tfPath += String.valueOf(maxFileNum+1) + ".txt";
				AhrIO.writeToFile(tfPath, sf.getData(), "~");
				System.out.println("==> File \""+tfPath+"\' written to file.");
			}
		});

		pInputs.add(lbMC);
		pInputs.add(tfStartMC);
		pInputs.add(lbMil1);
		pInputs.add(tfEndMC);
		pInputs.add(lbMil2);
		pInputs.add(lbSector);
		pInputs.add(tfSector);
		pInputs.add(bSectorList);
		pInputs.add(bSectorAll);
		pInputs.add(lbIndustry);
		pInputs.add(taIndustry);
		pInputs.add(bIndustryList);
		pInputs.add(bIndustryAll);
		pInputs.add(lbIndicator);
		pInputs.add(cbIndicator);
		pInputs.add(lbIndRangeStart);
		pInputs.add(tfIndRangeStart);
		pInputs.add(lbIndRangeEnd);
		pInputs.add(tfIndRangeEnd);
		pInputs.add(bIndAdd);
		pInputs.add(lbFilterDetails);
		pInputs.add(spFilterDetails);
		pInputs.add(bApply);
		pInputs.add(bReset);
		pOutputs.add(lbStockList);
		pOutputs.add(lbStockSort);
		pOutputs.add(cbStockSort);
		pOutputs.add(rbAsc);
		pOutputs.add(rbDes);
		pOutputs.add(bStockSort);
		pOutputs.add(spStockList);
		pOutputs.add(bToFile);
		this.add(pInputs);
		this.add(pOutputs);
		this.setVisible(true);
	}

	//update the text in taFilterDetails according to state of a StockFilter obj
	public String updateFilterDetailsText(ArrayList<ArrayList<String>> al){
		System.out.println("--> In updateFilter ...");
		AhrIO.printSAL(al);
		System.out.println("=======================");
		String fdText = "";
		String[] fdInit = {"========== General ==========", "--> Market Cap: ", "--> Sectors: ", "--> Industries: ",
							"========== Indicators =========="};
		boolean init_state_changed = false;
		for(int i = 0; i < al.size(); i++){
			if(al.get(i).get(0).equals("mc")){
				init_state_changed = true;
			}
		}
		if(init_state_changed){
			fdText += fdInit[0]+"\n";
			fdText += fdInit[1]+"["+al.get(0).get(1)+", "+al.get(0).get(2)+"]\n";		//market cap
			fdText += fdInit[2]+al.get(1).get(1)+"\n";									//sectors
			if(al.get(2).size() > 1){													//industries
				fdText += fdInit[3]+al.get(2).get(1)+"\n";
			}else{
				fdText += fdInit[3]+"\n";
			}
			fdText += fdInit[4]+"\n";
			//add all additional added indicators
			if(al.size() > 3){
				for(int i = 3; i < al.size(); i++){
					fdText += "--> "+al.get(i).get(0)+" : ["+al.get(i).get(1)+", "+al.get(i).get(2)+"]\n";
				}
			}
		}else{
			for(int i = 0; i < fdInit.length; i++){
				fdText += fdInit[i]+"\n";
			}
			for(int i = 0; i < al.size(); i++){
				fdText += "--> "+al.get(i).get(0)+" : ["+al.get(i).get(1)+", "+al.get(i).get(2)+"]\n";
			}
		}

		return fdText;
	}

	//sort 2D that is in taStockList by colIdx param
	public ArrayList<ArrayList<String>> sortStockList(ArrayList<ArrayList<String>> al, int colIdx, boolean is_asc){
		ArrayList<ArrayList<String>> sorted = new ArrayList<ArrayList<String>>(al);
		Collections.sort(sorted, new Comparator<ArrayList<String>>(){
			@Override
			public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
				int mult = -1;
				if(is_asc){
					mult = 1;
				}
				String tick1 = obj1.get(0);
				String tick2 = obj2.get(0);
				double mc1 = Double.parseDouble(obj1.get(1).substring(0, obj1.get(1).length()));
				double mc2 = Double.parseDouble(obj2.get(1).substring(0, obj2.get(1).length()));
				if(colIdx == 0){
					return (tick1.compareTo(tick2) * mult);
				}else if(colIdx == 1){
					return (Double.compare(mc1, mc2) * mult);
				}else{
					double dcomp1 = Double.parseDouble(obj1.get(colIdx));
					double dcomp2 = Double.parseDouble(obj2.get(colIdx));
					return (Double.compare(dcomp1, dcomp2) * mult);
				}
			}
		});
		return sorted;
	}

	//format stock list
	public ArrayList<ArrayList<String>> formatStockList(ArrayList<ArrayList<String>> al){
		ArrayList<ArrayList<String>> fmt = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < al.size(); i++){
			String fline = "";
			//format ticker
			String ticker = al.get(i).get(0);
			while(ticker.length() < 5){
				ticker = ticker + " ";
			}
			fline += ticker;
			//format market cap
			String mcStr = "| "+al.get(i).get(1);
			String mcLetter = "n/a";
			double mcVal = Double.parseDouble(al.get(i).get(1));
			if(mcVal > 1000000.0){
				mcLetter = "T";
				mcVal = mcVal / 1000000.0;
			}else if(mcVal > 1000.0){
				mcLetter = "B";
				mcVal = mcVal / 1000.0;
			}else{
				mcLetter = "M";
			}
			if(mcVal < 10.0){
				mcStr = " | " + String.format("%.3f", mcVal) + mcLetter;
			}else if(mcVal < 100.0){
				mcStr = " | " + String.format("%.2f", mcVal) + mcLetter;
			}else{
				mcStr = " | " + String.format("%.1f", mcVal) + mcLetter;
			}
			while(mcStr.length() < 9){
				mcStr += " ";
			}
			fline += mcStr;
			//format sector code
			String scode = " | "+al.get(i).get(2);
			while(scode.length() < 7){
				scode += " ";
			}
			fline += scode;
			//format indicators
			if(al.get(i).size() > 3){
				for(int j = 3; j < al.get(i).size(); j++){
					String indVal = al.get(i).get(j);
					while(indVal.length() < 5){
						indVal = "0" + indVal;
					}
					indVal = " | " + indVal;
					fline += indVal;
				}
			}
			ArrayList<String> fmtLine = new ArrayList<String>();		
			fmtLine.add(fline);
			fmt.add(fmtLine);
		}
		return fmt;
	}
}
