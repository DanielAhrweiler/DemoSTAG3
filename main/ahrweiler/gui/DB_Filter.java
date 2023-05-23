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

public class DB_Filter {

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
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("STAG 3");
		frame.setSize(1030, 580);
		frame.setLayout(null);	
		JPanel pBasics = new JPanel();
		pBasics.setLayout(null);
		pBasics.setBorder(BorderFactory.createTitledBorder("Basic Filter Params"));
		JPanel pNormInds = new JPanel();
		pNormInds.setLayout(null);
		pNormInds.setBorder(BorderFactory.createTitledBorder("Normalized Indicators"));		
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
		JButton bUpdateBasics = new JButton("Update Basic Params");
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
		pBasics.setBounds(10, 10, 515, 165);				//basic filter params panel
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
		taIndustry.setBounds(110, 90, 270, 35);
		bIndustryList.setBounds(390, 90, 50, 25);
		bIndustryAll.setBounds(450, 90, 50, 25);
		bUpdateBasics.setBounds(10, 130, 200, 25);
		pNormInds.setBounds(10, 185, 515, 85);				//normalized inds panel
		lbIndicator.setBounds(10, 20, 110, 25);
		cbIndicator.setBounds(110, 20, 270, 25);
		lbIndRangeStart.setBounds(50, 50, 55, 25);
		tfIndRangeStart.setBounds(110, 50, 90, 25);
		lbIndRangeEnd.setBounds(240, 50, 55, 25);
		tfIndRangeEnd.setBounds(290, 50, 90, 25);
		bIndAdd.setBounds(390, 35, 50, 25);
		lbFilterDetails.setBounds(20, 270, 120, 25);		//filter details
		taFilterDetails.setBounds(60, 300, 450, 170);
		spFilterDetails.setBounds(60, 300, 450, 170);
		bApply.setBounds(60, 480, 130, 30);
		bReset.setBounds(220, 480, 130, 30);
		bToFile.setBounds(380, 480, 130, 30);
		pOutputs.setBounds(540, 10, 480, 520);				//output panel
		lbStockNum.setBounds(10, 10, 330, 25);
		pTableSort.setBounds(10, 40, 460, 470);

		//basic functionality
		setButtonStyle(bUpdateBasics);
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
		//System.out.println("--> Filter Text : " + sf.getText());

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
				//std out print
				//System.out.println("******* Sector List *******");
				//for(int i = 0; i < uniqSectors.size(); i++){
				//	System.out.println("   "+(i+1)+ ") " + uniqSectors.get(i));
				//}
				//JOptionPane print
				String message = "";
				for(int i = 0; i < uniqSectors.size(); i++){
					message += "  "+(i+1)+") "+uniqSectors.get(i);
					if(i != (uniqSectors.size()-1)){
						message += "\n";
					}
				}
				JOptionPane.showMessageDialog(frame, message, "All Sectors", JOptionPane.PLAIN_MESSAGE);
			}
		});
		bSectorAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String oldStr = tfSector.getText();
				String newStr = "";
				String scPath = "./../in/sector_codes.txt";
				FCI fciSC = new FCI(false, scPath);
				ArrayList<String> scSectors = AhrIO.scanCol(scPath, "~", fciSC.getIdx("sector"));
				HashSet<String> uniqSec = new HashSet<String>();
				for(int i = 0; i < scSectors.size(); i++){
					uniqSec.add(scSectors.get(i));
				}
				for(int i = 0; i < uniqSec.size(); i++){
					if(i == uniqSec.size()-1){
						newStr += String.format("%02d", (i+1));
					}else{
						newStr += String.format("%02d", (i+1)) + ",";
					}
				}
				if(newStr.equals(oldStr)){
					JOptionPane.showMessageDialog(frame, "All sectors already selected.");
				}else{
					tfSector.setText(newStr);
				}
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
					//System.out.println("--> secInt = " + secInt);
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
					//print std out
					//System.out.println("******* Industries within "+secName+" *******");
					//for(int i = 0; i < uniqInds.size(); i++){
					//	System.out.println("   "+(i+1)+") "+uniqInds.get(i));
					//}
					//JOptionPane print
					String message = "";
					for(int i = 0; i < uniqInds.size(); i++){
						message += "  "+(i+1)+") "+uniqInds.get(i);
						if(i != (uniqInds.size()-1)){
							message += "\n";
						}
					}
					JOptionPane.showMessageDialog(frame, message, "All Industries within "+secName, JOptionPane.PLAIN_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(frame, "Only one sector must be selected for this filter"+
												" to work.", "Error", JOptionPane.ERROR_MESSAGE);
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
					String oldStr = taIndustry.getText();
					String newStr = "";
					String strSubcodes = "";
					for(int i = 0; i < subcodes.size(); i++){
						newStr += subcodes.get(i);
						if(i != (subcodes.size()-1)){
							newStr += ",";
						}
					}
					if(newStr.equals(oldStr)){
						JOptionPane.showMessageDialog(frame, "All industries already selected.");
					}else{
						taIndustry.setText(newStr);
					}
				}else{
					JOptionPane.showMessageDialog(frame, "Only one sector must be selected for this filter"+
												" to work.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		bUpdateBasics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//print out all non-indicator filters to taFilterDetails
				String mcStart = tfStartMC.getText();
				String mcEnd = tfEndMC.getText();
				String rawSecStr = tfSector.getText();
				String rawIndStr = taIndustry.getText();
				ArrayList<String> errors = new ArrayList<String>();
				if(!AhrGen.isInt(mcStart) || !AhrGen.isInt(mcEnd)){
					errors.add("Market cap values must be integers.");
				}
				if(!rawSecStr.replace(",","").matches("[0-9]+")){
					errors.add("Industry values must be comma seperated integers.");
				} 
				if(!rawIndStr.replace(",","").matches("[0-9]+") && !rawIndStr.equals("")){
					errors.add("Sector values must be comma seperated integers.");
				}
				if(!rawIndStr.equals("") && rawSecStr.split(",").length > 1){
					errors.add("If more than one sector is selected, industry text must be blank.");
				}
				if(errors.size() != 0){
					String errStr = "";
					if(errors.size() == 1){
						errStr = errors.get(0);
					}else if(errors.size() > 1){
						errStr = "1) "+errors.get(0);
						for(int i = 0; i < errors.size(); i++){
							errStr += "\n"+(i+1)+") "+errors.get(i);
						}
					}
					JOptionPane.showMessageDialog(frame, errStr, "Error", JOptionPane.ERROR_MESSAGE);
				}else{
					//apply filter from StockFilter
					sf.clearSectorCodes();
					sf.clearResults();
					sf.setMarketCap(Integer.parseInt(mcStart), Integer.parseInt(mcEnd));
					sf.setSectors(rawSecStr);
					sf.setIndustries(rawIndStr);
					taFilterDetails.setText(sf.getText());
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
					JOptionPane.showMessageDialog(frame, "Indicator values can only be integers in range [0-65535]",
												"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		bApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
				frame.setCursor(null);
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
				//System.out.println("==> File \""+tfPath+"\' written to file.");
			}
		});

		pBasics.add(lbMC);
		pBasics.add(tfStartMC);
		pBasics.add(lbMil1);
		pBasics.add(tfEndMC);
		pBasics.add(lbMil2);
		pBasics.add(lbSector);
		pBasics.add(tfSector);
		pBasics.add(bSectorList);
		pBasics.add(bSectorAll);
		pBasics.add(lbIndustry);
		pBasics.add(taIndustry);
		pBasics.add(bIndustryList);
		pBasics.add(bIndustryAll);
		pBasics.add(bUpdateBasics);
		frame.add(pBasics);
		pNormInds.add(lbIndicator);
		pNormInds.add(cbIndicator);
		pNormInds.add(lbIndRangeStart);
		pNormInds.add(tfIndRangeStart);
		pNormInds.add(lbIndRangeEnd);
		pNormInds.add(tfIndRangeEnd);
		pNormInds.add(bIndAdd);
		frame.add(pNormInds);
		frame.add(lbFilterDetails);
		frame.add(spFilterDetails);
		frame.add(bApply);
		frame.add(bReset);
		frame.add(bToFile);
		pOutputs.add(lbStockNum);
		pOutputs.add(pTableSort);
		frame.add(pOutputs);
		frame.setVisible(true);
	}

	//set starting vals of a StockFilter and show in taFilterDetails
	public String setBasicFilter(String startMC, String endMC, String sectors, String industries){
		//apply filter from StockFilter
		sf.clearSectorCodes();
		sf.clearResults();
		sf.setMarketCap(Integer.parseInt(startMC), Integer.parseInt(endMC));
		sf.setSectors(sectors);
		sf.setIndustries(industries);
		return sf.getText();
	}

	//GUI related, sets style to a JButton
	public void setButtonStyle(JButton btn){
		Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}
}
