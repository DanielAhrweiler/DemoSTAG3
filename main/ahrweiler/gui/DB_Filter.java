package ahrweiler.gui;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrDate;
import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrGen;
import ahrweiler.support.FCI;
import ahrweiler.support.StockFilter;
import ahrweiler.gui.TableSortPanel;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DB_Filter extends JFrame {

	private StockFilter sf;

	public DB_Filter(){
		sf = new StockFilter();
		drawGUI();
	}
	
	//GUI for Database -> Stock Filter
	public void drawGUI(){
		//lists and structs
		String[] industryList = {""};
		String[] indicatorList = {"S/M 20", "S/M 10", "S/M 5", "S/M 2", "S/I 20", "S/I 10", "S/I 5", "S/I 2", "SMA 20",
								"SMA 10", "SMA 5", "SMA 2", "RSI", "MACD", "MACD Histogram", "CMF", "Bollinger Bandwidth",
								"%B", "ROC", "MFI", "CCI", "Mass Index", "TSI", "Ult Osc"};
		String[] startHeader = {"ticker", "market_cap", "sector"};
		String[][] startData = {{"","",""},{"","",""},{"","",""},{"","",""},{"","",""},
								{"","",""},{"","",""},{"","",""},{"","",""},{"","",""}};
		ArrayList<ArrayList<String>> rules = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> fdAL = new ArrayList<ArrayList<String>>();

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
		JTextField tfStartMC = new JTextField("100");
		JLabel lbMil1 = new JLabel("mil  to");
		JTextField tfEndMC = new JTextField("10000000");//$10 trillion
		JLabel lbMil2 = new JLabel("mil"); 
		JLabel lbSector = new JLabel("Sector:");
		JTextField tfSector = new JTextField("01,02,03,04,05,06,07,08,09,10,11,12");
		JButton bSectorList = new JButton("List");
		JButton bSectorAll = new JButton("All");
		JLabel lbIndustry = new JLabel("Industry:");
		JTextArea taIndustry = new JTextArea(2, 30);
		JButton bIndustryAll = new JButton("All");
		JButton bIndustryList = new JButton("List");
		JLabel lbIndicator = new JLabel("Indicator:");
		JComboBox cbIndicator = new JComboBox();
		JLabel lbIndRangeStart = new JLabel("Start:");
		JTextField tfIndRangeStart = new JTextField();
		JLabel lbIndRangeEnd = new JLabel("End:");
		JTextField tfIndRangeEnd = new JTextField();
		JButton bIndAdd = new JButton("Add");
		JLabel lbFilterDetails = new JLabel("Filter Details:");
		JTextArea taFilterDetails = new JTextArea();
		JScrollPane spFilterDetails = new JScrollPane(taFilterDetails, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JButton bApply = new JButton("Apply Inputs");
		JButton bReset = new JButton("Reset Inputs");
		JLabel lbStockNum = new JLabel("Number of Stocks: 0");
		TableSortPanel pTableSort = new TableSortPanel(startData, startHeader);
		JButton bToFile = new JButton("Save To File");
		
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
		lbStockNum.setBounds(10, 10, 330, 25);
		pTableSort.setBounds(10, 40, 460, 450);
		bToFile.setBounds(10, 500, 150, 35);

		//basic functionality
		setButtonStyle(bSectorList);
		setButtonStyle(bSectorAll);
		setButtonStyle(bIndustryList);
		setButtonStyle(bIndustryAll);
		setButtonStyle(bIndAdd);
		setButtonStyle(bApply);
		setButtonStyle(bReset);
		//setButtonStyle(bStockSort);
		setButtonStyle(bToFile);
		taIndustry.setLineWrap(true);
		taIndustry.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		for(int i = 0; i < indicatorList.length; i++){
			cbIndicator.addItem(indicatorList[i]);
		}
		taFilterDetails.setLineWrap(true);
		taFilterDetails.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		taFilterDetails.setText(setBasicFilter(tfStartMC.getText(), tfEndMC.getText(),
											 tfSector.getText(), taIndustry.getText()));
		System.out.println("--> Filter Text : " + sf.getText());

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
					//apply filter from StockFilter
					sf.clearSectorCodes();
					sf.clearResults();
					sf.setMarketCap(Integer.parseInt(mcStart), Integer.parseInt(mcEnd));
					sf.setSectors(rawSecStr);
					sf.setIndustries(rawIndStr);
					taFilterDetails.setText(sf.getText());
					setVisible(true);
					String mrDate = AhrDate.mostRecentDate(AhrIO.getNamesInPath("./../../DB_Intrinio/Clean/ByDate/"));
					sf.applyFilter(mrDate);
					ArrayList<ArrayList<String>> res = sf.getResults();
					lbStockNum.setText("Number of Stocks: "+res.size()+" results");
					//update table in table sort
					ArrayList<String> inds = sf.getIndicators();
					String[] header = new String[3+inds.size()];
					header[0] = "ticker";
					header[1] = "market_cap";
					header[2] = "sector";
					for(int i = 0; i < inds.size(); i++){
						header[3+i] = inds.get(i);
					}
					String[][] data = AhrAL.toArr2D(res);
					pTableSort.updateModel(data, header);
				}
				
			}
		});
		bReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//reset textboxes
				tfStartMC.setText("100");
				tfEndMC.setText("10000000");
				tfSector.setText("01,02,03,04,05,06,07,08,09,10,11,12");
				taIndustry.setText("");
				sf.resetFilter();
				String basicDetails = setBasicFilter(tfStartMC.getText(), tfEndMC.getText(),
													 tfSector.getText(), taIndustry.getText());
				taFilterDetails.setText(basicDetails);
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
		pOutputs.add(lbStockNum);
		/*
		pOutputs.add(lbStockSort);
		pOutputs.add(cbStockSort);
		pOutputs.add(rbAsc);
		pOutputs.add(rbDes);
		pOutputs.add(bStockSort);
		pOutputs.add(spStockList);
		*/
		pOutputs.add(pTableSort);
		pOutputs.add(bToFile);
		this.add(pInputs);
		this.add(pOutputs);
		this.setVisible(true);
	}

	//set starting vals of a StockFilter and show in taFilterDetails
	public String setBasicFilter(String startMC, String endMC, String sectors, String industries){
		boolean good_vals = true;
		if(!AhrGen.isInt(startMC) || !AhrGen.isInt(endMC)){
			System.out.println("ERR: market cap values must be integers.");
			good_vals = false;
		}
		if(!sectors.replace(",","").matches("[0-9]+")){
			System.out.println("ERR: industry values must be comma seperated integers");
			good_vals = false;
		} 
		if(!industries.replace(",","").matches("[0-9]+") && !industries.equals("")){
			System.out.println("ERR: sector values must be comma seperated integers.");
			good_vals = false;
		}
		if(good_vals){
			//apply filter from StockFilter
			sf.clearSectorCodes();
			sf.clearResults();
			sf.setMarketCap(Integer.parseInt(startMC), Integer.parseInt(endMC));
			sf.setSectors(sectors);
			sf.setIndustries(industries);
			return sf.getText();
		}else{
			return "";
		}
	}

	//GUI related, sets style to a JButton
	public void setButtonStyle(JButton btn){
		Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}
}
