package ahrweiler.gui;
import ahrweiler.Globals;
import ahrweiler.util.*;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import ahrweiler.support.OrderSim;
import ahrweiler.support.StockFilter;
import ahrweiler.bgm.BGM_Manager;
import ahrweiler.bgm.AttributesSK;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.io.File;

public class PA_KeyPerf {

	final Font monoFont = new Font(Font.MONOSPACED, Font.BOLD, 11);
	final Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	public PA_KeyPerf(){
		drawGUI();
	}

	public void drawGUI(){
		//lists and overarching structs
		String[] bgmList = {"ANN"};
		ArrayList<String> sffList = AhrIO.getNamesInPath("./../data/filters/");
		sffList.add(0, "None");
		ArrayList<String> keyNumList = new ArrayList<String>();
		String[] plotList = {"Appr Distribution", "Trigger Codes", "Portfolio Growth ($)"};
		String[] baseOut = {"SPD    : " , "Pos %  : " , "APAPT  : " , "Annual % Yield   : ",
							"Total Section %  : "};

		//layout components
		int fxDim = 510;
		int fyDim = 475;
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("Key Performance");
		frame.setSize(fxDim, fyDim+37);
		frame.setLayout(null);
		JTabbedPane tpKeyPerf = new JTabbedPane();
		tpKeyPerf.setBounds(0, 0, fxDim, fyDim);
		JPanel pBGM = new JPanel();
		pBGM.setLayout(null);
		JPanel pRND = new JPanel();
		pRND.setLayout(null);

		/*--------------------------------------------
			BGM Panel
		---------------------------------------------*/
		//layout 
		JPanel pBgmInputs = new JPanel();
		pBgmInputs.setBounds(10, 10, fxDim-20, 235);
		pBgmInputs.setBorder(BorderFactory.createTitledBorder("Input Params"));
		pBgmInputs.setLayout(null);
		JPanel pBgmOutputs = new JPanel();
		pBgmOutputs.setBounds(10, 295, fxDim-20, 150);
		pBgmOutputs.setBorder(BorderFactory.createTitledBorder("Performance"));
		pBgmOutputs.setLayout(null);

		//init components
		JLabel lbKeyType = new JLabel("Key Type:");						//Input Panel
		JRadioButton rbSK = new JRadioButton("Single");
		JRadioButton rbAK = new JRadioButton("Aggregate");
		ButtonGroup bgKeyType = new ButtonGroup();
		bgKeyType.add(rbSK);
		bgKeyType.add(rbAK);
		JLabel lbMethod = new JLabel("Method:");
		JComboBox cbMethod = new JComboBox();
		JLabel lbKeyNum = new JLabel("Key Num:");
		JComboBox cbKeyNum = new JComboBox();
		JLabel lbDatasets = new JLabel("Datasets:");
		JCheckBox cbTrain = new JCheckBox("Train", true);
		JCheckBox cbTest = new JCheckBox("Test", true);
		JCheckBox cbVerify = new JCheckBox("Verify", true);
		JLabel lbBgmSDate = new JLabel("Start Date:");
		JTextField tfBgmSDate = new JTextField("2020-01-01");
		JLabel lbBgmEDate = new JLabel("End Date:");
		JTextField tfBgmEDate = new JTextField(AhrDate.getTodaysDate());
		JButton bBgmDatesAF = new JButton("Autofill");
		JLabel lbBgmBim = new JLabel("BIM:");
		JTextField tfBgmBim = new JTextField("0.95");
		JLabel lbBgmSom = new JLabel("SOM:");
		JTextField tfBgmSom = new JTextField("0.95");
		JButton bBgmBsoAF = new JButton("Autofill");
		JLabel lbBgmPrincipal = new JLabel("Principal ($):");
		JTextField tfBgmPrincipal = new JTextField("100000");
		JLabel lbBgmMop = new JLabel("MOP ($):");
		JTextField tfBgmMop = new JTextField("10000");
		JButton bBgmOrdersAF = new JButton("Autofill");
		JButton bBgmCalcPerf = new JButton("Calculate Performance");	//Calc Perf Button
		JButton bBgmRunRnd = new JButton("Run Params w/ RND Selection =>");
		JLabel lbBgmSPD = new JLabel(baseOut[0]);						//Output Panel
		JLabel lbBgmPosPer = new JLabel(baseOut[1]);
		JLabel lbBgmAPAPT = new JLabel(baseOut[2]);
		JLabel lbBgmYoyPer = new JLabel(baseOut[3]);	
		JLabel lbBgmSecPer = new JLabel(baseOut[4]);
		JLabel lbBgmPlots = new JLabel("Plot Results: ");
		JComboBox cbBgmPlots = new JComboBox();
		JButton bBgmPlots = new JButton("Plot");


		//bounds of components
		lbKeyType.setBounds(10, 20, 80, 25);							//Input Panel
		rbSK.setBounds(105, 20, 80, 25);
		rbAK.setBounds(195, 20, 120, 25);
		lbMethod.setBounds(10, 55, 80, 25);
		cbMethod.setBounds(105, 55, 80, 25);
		lbKeyNum.setBounds(215, 55, 80, 25);
		cbKeyNum.setBounds(295, 55, 80, 25);
		lbDatasets.setBounds(10, 90, 80, 25);
		cbTrain.setBounds(105, 90, 75, 25);
		cbTest.setBounds(180, 90, 75, 25);
		cbVerify.setBounds(255, 90, 75, 25);
		lbBgmSDate.setBounds(10, 125, 80, 25);
		tfBgmSDate.setBounds(105, 125, 80, 25);
		lbBgmEDate.setBounds(215, 125, 80, 25);
		tfBgmEDate.setBounds(295, 125, 80, 25);
		bBgmDatesAF.setBounds(385, 125, 80, 25);
		lbBgmBim.setBounds(10, 160, 80, 25);
		tfBgmBim.setBounds(105, 160, 80, 25);
		lbBgmSom.setBounds(215, 160, 80, 25);
		tfBgmSom.setBounds(295, 160, 80, 25);	
		bBgmBsoAF.setBounds(385, 160, 80, 25);
		lbBgmPrincipal.setBounds(10, 195, 100, 25);
		tfBgmPrincipal.setBounds(105, 195, 80, 25);
		lbBgmMop.setBounds(215, 195, 80, 25);
		tfBgmMop.setBounds(295, 195, 80, 25);
		bBgmOrdersAF.setBounds(385, 195, 80, 25);
		bBgmCalcPerf.setBounds(10, 250, 180, 35);						//Calc Perf Button
		bBgmRunRnd.setBounds(250, 250, 250, 35);
		lbBgmSPD.setBounds(10, 20, 200, 20);							//Outputs Panel
		lbBgmPosPer.setBounds(10, 45, 200, 20);
		lbBgmAPAPT.setBounds(10, 70, 200, 20);
		lbBgmYoyPer.setBounds(220, 20, 200, 20);
		lbBgmSecPer.setBounds(220, 70, 200, 20);
		lbBgmPlots.setBounds(10, 115, 100, 25);
		cbBgmPlots.setBounds(110, 115, 210, 25);
		bBgmPlots.setBounds(330, 115, 50, 25); 

		//basic functionality
		rbAK.setSelected(true);
		for(int i = 0; i < bgmList.length; i++){
			cbMethod.addItem(bgmList[i]);
		}
		cbMethod.setEnabled(false);
		keyNumList = getKeyNumList(rbSK.isSelected(), cbMethod.getSelectedItem().toString());
		for(int i = 0; i < keyNumList.size(); i++){
			cbKeyNum.addItem(keyNumList.get(i));
		}
		rbSK.setFont(plainFont);
		rbAK.setFont(plainFont);
		cbTrain.setFont(plainFont);
		cbTest.setFont(plainFont);
		cbVerify.setFont(plainFont);
		setButtonStyle(bBgmDatesAF);
		setButtonStyle(bBgmBsoAF);
		setButtonStyle(bBgmOrdersAF);
		setButtonStyle(bBgmCalcPerf);
		setButtonStyle(bBgmRunRnd);
		setButtonStyle(bBgmPlots);
		lbBgmSPD.setFont(monoFont);
		lbBgmPosPer.setFont(monoFont);
		lbBgmAPAPT.setFont(monoFont);
		lbBgmSecPer.setFont(monoFont);
		lbBgmYoyPer.setFont(monoFont);
		for(int i = 0; i < plotList.length; i++){
			cbBgmPlots.addItem(plotList[i]);
		}

		//listener functionality
		rbSK.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if(rbSK.isSelected()){
					cbMethod.setEnabled(true);
				}else{
					cbMethod.setEnabled(false);
				}
				String bgm = cbMethod.getSelectedItem().toString();
				ArrayList<String> keyNumList = getKeyNumList(rbSK.isSelected(), bgm);
				cbKeyNum.removeAllItems();
				for(int i = 0; i < keyNumList.size(); i++){
					cbKeyNum.addItem(keyNumList.get(i));
				}
			}
		});
		cbMethod.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				String bgm = cbMethod.getSelectedItem().toString();
				ArrayList<String> keyNumList = getKeyNumList(rbSK.isSelected(), bgm);
				cbKeyNum.removeAllItems();
				for(int i = 0; i < keyNumList.size(); i++){
					cbKeyNum.addItem(keyNumList.get(i));
				}
			}
		});
		bBgmDatesAF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String bgmUC = cbMethod.getSelectedItem().toString().toUpperCase();
				String bgmLC = cbMethod.getSelectedItem().toString().toLowerCase();
				String knum = cbKeyNum.getSelectedItem().toString();
				String bsPath = "";
				if(rbSK.isSelected()){
					bsPath = "./../out/sk/baseis/"+bgmLC+"/"+bgmUC+"_"+knum+".txt";
				}else{
					bsPath = "./../out/ak/baseis/"+bgmLC+"/"+bgmUC+"_"+knum+".txt";
				}
				FCI fciBS = new FCI(false, bsPath);
				ArrayList<ArrayList<String>> basis = AhrIO.scanFile(bsPath, ",");
				tfBgmSDate.setText(basis.get(0).get(fciBS.getIdx("date")));
				tfBgmEDate.setText(basis.get(basis.size()-1).get(fciBS.getIdx("date")));
			}
		});
		bBgmBsoAF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String bgmLC = cbMethod.getSelectedItem().toString().toLowerCase();
				String knum = cbKeyNum.getSelectedItem().toString();
				String bim = "ph";
				String som = "ph";
				String call = "0";
				if(rbSK.isSelected()){
					String kpPath = "./../out/sk/log/"+bgmLC+"/keys_perf.txt";
					FCI fciKP = new FCI(true, kpPath);
					ArrayList<String> kpLine = AhrIO.scanRow(kpPath, ",", String.valueOf(knum));
					call = kpLine.get(fciKP.getIdx("call"));
					bim = kpLine.get(fciKP.getIdx("bim"));
					som = kpLine.get(fciKP.getIdx("som"));
					if(bim.equals("ph")){
						String laPath = "./../out/ak/log/ak_log.txt";
						FCI fciLA = new FCI(true, laPath);
						ArrayList<ArrayList<String>> laFile = AhrIO.scanFile(laPath, ",");
						for(int i = 0; i < laFile.size(); i++){
							String bestKeysFull = laFile.get(i).get(fciLA.getIdx("best_keys"));
							String[] bestKeys = bestKeysFull.split("~");
							for(int j = 0; j < bestKeys.length; j++){
								if(bestKeys[j].equals(knum)){
									String akBimSom = laFile.get(i).get(fciLA.getIdx("ak_bso"));
									String[] bsoParts = akBimSom.split("\\|");
									bim = bsoParts[0];
									som = bsoParts[1];
									break;
								}
							}
						} 					
					}
				}else{
					String laPath = "./../out/ak/log/ak_log.txt";
					FCI fciLA = new FCI(true, laPath);
					ArrayList<String> laLine = AhrIO.scanRow(laPath, ",", String.valueOf(knum));
					call = laLine.get(fciLA.getIdx("call"));
					String akBimSom = laLine.get(fciLA.getIdx("ak_bso"));
					String[] bsoParts = akBimSom.split("\\|");
					bim = bsoParts[0];
					som = bsoParts[1];
				}
				//if no BSO is found in SK or AK file, set some def vals		
				if(bim.equals("ph")){
					if(call.equals("0")){
						bim = "0.92";
						som = "0.92";
					}else{
						bim = "1.15";
						som = "0.001";
					}
				}
				tfBgmBim.setText(bim);
				tfBgmSom.setText(som);
			}
		});
		bBgmOrdersAF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String keyNum = cbKeyNum.getSelectedItem().toString();
				int tvi = -1;
				if(rbSK.isSelected()){
					String ksPath = "./../out/sk/log/ann/keys_struct.txt";
					FCI fciKS = new FCI(true, ksPath);
					ArrayList<String> ksRow = AhrIO.scanRow(ksPath, ",", keyNum);
					tvi = Integer.parseInt(ksRow.get(fciKS.getIdx("tvi")));
				}else{
					String ksPath = "./../out/ak/log/ak_log.txt";
					FCI fciKS = new FCI(true, ksPath);
					ArrayList<String> ksRow = AhrIO.scanRow(ksPath, ",", keyNum);
					tvi = Integer.parseInt(ksRow.get(fciKS.getIdx("tvi")));
				}
				if(tvi == 0 || tvi == 1){//1-day
					tfBgmPrincipal.setText("100000");
					tfBgmMop.setText("10000");
				}else if(tvi == 2 || tvi == 3){//2-day
					tfBgmPrincipal.setText("200000");
					tfBgmMop.setText("10000");
				}else if(tvi == 4 || tvi == 5){//3-day
					tfBgmPrincipal.setText("300000");
					tfBgmMop.setText("10000");
				}else if(tvi == 6 || tvi == 7){//5-day
					tfBgmPrincipal.setText("500000");
					tfBgmMop.setText("10000");
				}else if(tvi == 8 || tvi == 9){//10-day
					tfBgmPrincipal.setText("1000000");
					tfBgmMop.setText("10000");
				}
			}
		});
		bBgmCalcPerf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){

				//get params from GUI
				String bgmUC = cbMethod.getSelectedItem().toString();
				String bgmLC = bgmUC.toLowerCase();
				int knum = Integer.parseInt(cbKeyNum.getSelectedItem().toString());
				String ttvMask = "";
				boolean has_no_datasets = true;
				if(cbTrain.isSelected()){
					ttvMask += "1";
					has_no_datasets = false;
				}else{
					ttvMask += "0";
				}
				if(cbTest.isSelected()){
					ttvMask += "1";
					has_no_datasets = false;
				}else{
					ttvMask += "0";
				}
				if(cbVerify.isSelected()){
					ttvMask += "1";
					has_no_datasets = false;
				}else{
					ttvMask += "0";
				}
				double principal = Double.parseDouble(tfBgmPrincipal.getText());
				double maxOrderPrice = Double.parseDouble(tfBgmMop.getText());
				//if > 1 datasets is selected, calc key perf
				if(has_no_datasets){
					JOptionPane.showMessageDialog(frame, "At least one dataset must be selected.", "Error",
												JOptionPane.ERROR_MESSAGE);
				}else{
					//suspend GUI while working
					bBgmCalcPerf.setEnabled(false);
					bBgmPlots.setEnabled(false);
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					//create OrderSim obj and calc the order list
					OrderSim osim;
					if(rbSK.isSelected()){
						osim = new OrderSim(bgmUC, knum);
					}else{
						osim = new OrderSim(knum);
					}
					osim.setDateRange(tfBgmSDate.getText(), tfBgmEDate.getText());
					osim.setBIM(Double.parseDouble(tfBgmBim.getText()));
					osim.setSOM(Double.parseDouble(tfBgmSom.getText()));
					osim.setTtvMask(ttvMask);
					osim.setPrincipal(principal);
					osim.setMaxOrderPrice(maxOrderPrice);
					osim.calcOrderList();
					lbBgmSPD.setText(baseOut[0] + String.valueOf(osim.getOrderListSPD()) + " (" + 
									String.valueOf(osim.getOrderListSize()) + " total)");
					lbBgmPosPer.setText(baseOut[1] + String.format("%.2f", (osim.getPosPer()*100.0)));
					lbBgmAPAPT.setText(baseOut[2] + String.format("%.4f", osim.getTrigAppr()));
					lbBgmYoyPer.setText(baseOut[3] + String.format("%.4f", osim.getYoyAppr()));
					lbBgmSecPer.setText(baseOut[4] + String.format("%.4f", osim.getSecAppr()));
					//Preserve data for graphing
					ArrayList<ArrayList<String>> growth = osim.calcGrowth(principal);
					ArrayList<String> growthHeader = new ArrayList<String>();
					growthHeader.add("date");
					growthHeader.add("growth");
					growth.add(0, growthHeader);
					AhrIO.writeToFile("./../data/r/rdata/pa_portgrowth.csv", AhrDTF.melt(growth, "date"), ",");
					//resume GUI
					bBgmCalcPerf.setEnabled(true);
					bBgmPlots.setEnabled(true);
					frame.setCursor(null);
				}

			}
		});
		bBgmPlots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//set up params for plots
				int xdim = 1050;
				int ydim = 650;
				String kmonik = "";
				if(rbSK.isSelected()){
					kmonik += "SK";
				}else{
					kmonik += "AK";
				}
				String bgmUC = cbMethod.getSelectedItem().toString();
				String bgmLC = bgmUC.toLowerCase();
				String knum = cbKeyNum.getSelectedItem().toString();
				kmonik += knum; 
				//get plot for selected option
				int idx = cbBgmPlots.getSelectedIndex();
				//System.out.println("==> Plot Idx = " + idx);
				if(idx == 0){//appr distn: b&w and cumlative
					//basic plot vars
					String plotPathBaw = "./../resources/pa_distn_baw.png";
					String plotPathCdf = "./../resources/pa_distn_cdf.png";
					String titleBaw = "All Method Appr %s for "+kmonik+" in B&W";
					String titleCdf = "All Method Appr %s for "+kmonik+" in CDF";
					xdim = 600;
					ydim = 300;
					//get all trig %s (while trimming) from orderlist, bounds from orderlist_byappr
					String olPath = "./../data/tmp/os_orderlist_byappr.txt";
					FCI fciOL = new FCI(false, olPath);
					ArrayList<String> methAppr = new ArrayList<String>();
					ArrayList<ArrayList<String>> fcOL = AhrIO.scanFile(olPath, ",");
					for(int i = 0; i < fcOL.size(); i++){
						String itrTrigCode = fcOL.get(i).get(fciOL.getIdx("trigger_code"));
						String itrMethAppr = fcOL.get(i).get(fciOL.getIdx("method_appr"));
						if(!itrTrigCode.equals("NO")){
							methAppr.add(itrMethAppr);
						}
					}
					//ArrayList<String> methAppr = AhrIO.scanCol(olPath, ",", fciOL.getIdx("method_appr"));
					ArrayList<Double> trimAppr = new ArrayList<Double>();
					double trimVal = 0.025;	//0-1, what % of vals you want trimmed on top & bot
					int startTrim = (int)((double)methAppr.size() * trimVal);
					int endTrim = (int)((double)methAppr.size() - ((double)methAppr.size() * trimVal));
					//System.out.println("StartTrim = "+startTrim+"  |  EndTrim = "+endTrim+"  |  "+
					//				"Count = "+methAppr.size());
					for(int i = startTrim; i < endTrim; i++){
						trimAppr.add(Double.parseDouble(methAppr.get(i)));
					}
					double loBound = trimAppr.get(0) - 1.0;
					double hiBound = trimAppr.get(trimAppr.size()-1) + 1.0;
					//create R Box & Whisker
					RCode rcBaw = new RCode();
					rcBaw.setTitle(titleBaw);
					rcBaw.setXLabel("");
					rcBaw.setYLabel("Appreciation (%)");
					rcBaw.hardLimY(loBound, hiBound);
					rcBaw.flipCoords();
					rcBaw.createBAW(trimAppr, plotPathBaw, xdim, ydim);
					//rcBaw.printCode();
					rcBaw.writeCode("./../data/r/rscripts/pa_distn_baw.R");
					rcBaw.runScript("./../data/r/rscripts/pa_distn_baw.R");
					//calc CDF plot
					RCode rcCDF = new RCode();
					rcCDF.setTitle(titleCdf);
					rcCDF.setXLabel("Appreciation (%)");
					rcCDF.setYLabel("Cumulative Probability");
					rcCDF.hardLimX(loBound, hiBound);
					rcCDF.createCDF(trimAppr, plotPathCdf, xdim, ydim);
					//rcCDF.printCode();
					rcCDF.writeCode("./../data/r/rscripts/pa_distn_cdf.R");
					rcCDF.runScript("./../data/r/rscripts/pa_distn_cdf.R");
					//show both plots on popout frame
					JFrame rframe = new JFrame();
					rframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					rframe.setTitle("Method Appreciation Distribution (BGM)");
					JLabel lbBawPlot = new JLabel();
					JLabel lbCdfPlot = new JLabel();
					lbBawPlot.setPreferredSize(new Dimension(xdim, ydim));
					lbCdfPlot.setPreferredSize(new Dimension(xdim, ydim));
					ImageIcon iiBaw = new ImageIcon(plotPathBaw);
					ImageIcon iiCdf = new ImageIcon(plotPathCdf);
					lbBawPlot.setIcon(iiBaw);
					lbCdfPlot.setIcon(iiCdf);
					rframe.getContentPane().add(lbBawPlot, BorderLayout.NORTH);
					rframe.getContentPane().add(lbCdfPlot, BorderLayout.SOUTH);
					rframe.pack();
					rframe.setVisible(true);
					iiBaw.getImage().flush();
					iiCdf.getImage().flush();
				}else if(idx == 1){//trigger code pie chart
					String plotPath = "./../resources/pa_trigger_codes.png";
					xdim = 330;
					ydim = 330;
					//get all trigger codes from orderlist, create pie chart
					String olPath = "./../data/tmp/os_orderlist.txt";
					FCI fciOL = new FCI(false, olPath);
					ArrayList<String> trigCodes = AhrIO.scanCol(olPath, ",", fciOL.getIdx("trigger_code"));
					ArrayList<ArrayList<String>> pieAL = AhrAL.countUniq(trigCodes);
					RCode rcPie = new RCode();
					rcPie.setTitle("Trigger Codes for "+kmonik);
					rcPie.setXLabel("");
					rcPie.setYLabel("");
					rcPie.createPie(pieAL, plotPath, xdim, ydim);
					//rcPie.printCode();
					rcPie.writeCode("./../data/r/rscripts/pa_trigger_codes.R");
					rcPie.runScript("./../data/r/rscripts/pa_trigger_codes.R");
					//show plot on new popout frame
					JFrame rframe = new JFrame();
					rframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					rframe.setTitle("Trigger Code Count");
					JLabel lbPlot = new JLabel();
					lbPlot.setPreferredSize(new Dimension(xdim, ydim));
					ImageIcon ii = new ImageIcon(plotPath);
					lbPlot.setIcon(ii);
					rframe.getContentPane().add(lbPlot, BorderLayout.CENTER);
					rframe.pack();
					rframe.setVisible(true);
					ii.getImage().flush();
				}else if(idx == 2){//Portfolio growth powered by OrderSim
					String dataPath = "./../data/r/rdata/pa_portgrowth.csv";
					String plotPath = "./../resources/pa_portgrowth.png";
					String scriptPath = "./../data/r/rscripts/pa_portgrowth.R";
					String bimStr = tfBgmBim.getText();
					String somStr = tfBgmSom.getText();
					String mopStr = tfBgmMop.getText();
					ArrayList<String> fdates = AhrIO.scanCol(dataPath, ",", 0);
					fdates.remove(0);
					String plotTitle = "Portfolio Growth "+kmonik+" ($)  [BIM = "+bimStr+", SOM = "+somStr+", MOP = "+mopStr+"]";
					String startTrainDate = "";
					String endTrainDate = "";
					if(rbSK.isSelected()){
						String ksPath =  "./../out/sk/log/"+bgmLC+"/keys_struct.txt";
						FCI fciKS = new FCI(true, ksPath);
						ArrayList<String> ksRow = AhrIO.scanRow(ksPath, ",", knum);
						startTrainDate = ksRow.get(fciKS.getIdx("start_date"));
						endTrainDate = ksRow.get(fciKS.getIdx("end_date"));
					}else{
						String alPath = "./../out/ak/log/ak_log.txt";
						FCI fciAL = new FCI(true, alPath);
						ArrayList<String> alRow = AhrIO.scanRow(alPath, ",", knum);
						startTrainDate = alRow.get(fciAL.getIdx("start_date"));
						endTrainDate = alRow.get(fciAL.getIdx("end_date"));
					}
					RCode rcode = new RCode();
					rcode.setXLabel("Date");
					rcode.setYLabel("Porfolio Value ($)");
					rcode.setTitle(plotTitle);
					if(AhrDate.isDateInPeriod(startTrainDate, fdates.get(0), fdates.get(fdates.size()-1))){
						rcode.addXIntercept(AhrDate.closestDateInAL(startTrainDate, fdates));
					}
					if(AhrDate.isDateInPeriod(endTrainDate, fdates.get(0), fdates.get(fdates.size()-1))){
						rcode.addXIntercept(AhrDate.closestDateInAL(endTrainDate, fdates));
					}
					rcode.createTimeSeries(dataPath, plotPath, xdim, ydim);
					//rcode.printCode();
					rcode.writeCode(scriptPath);
					rcode.runScript(scriptPath);
					//show plot on new popout frame
					JFrame rframe = new JFrame();
					rframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					rframe.setTitle("Portfolio Growth Plot (BGM)");
					JLabel lbPlot = new JLabel();
					lbPlot.setPreferredSize(new Dimension(xdim, ydim));
					ImageIcon ii = new ImageIcon(plotPath);
					lbPlot.setIcon(ii);
					rframe.getContentPane().add(lbPlot, BorderLayout.CENTER);
					rframe.pack();
					rframe.setVisible(true);
					ii.getImage().flush();
				}

			}
		});

		//add
		pBgmInputs.add(lbKeyType);
		pBgmInputs.add(rbSK);
		pBgmInputs.add(rbAK);
		pBgmInputs.add(lbMethod);
		pBgmInputs.add(cbMethod);
		pBgmInputs.add(lbKeyNum);
		pBgmInputs.add(cbKeyNum);
		pBgmInputs.add(lbDatasets);
		pBgmInputs.add(cbTrain);
		pBgmInputs.add(cbTest);
		pBgmInputs.add(cbVerify);
		pBgmInputs.add(lbBgmSDate);
		pBgmInputs.add(tfBgmSDate);
		pBgmInputs.add(lbBgmEDate);
		pBgmInputs.add(tfBgmEDate);
		pBgmInputs.add(bBgmDatesAF);
		pBgmInputs.add(lbBgmBim);
		pBgmInputs.add(tfBgmBim);
		pBgmInputs.add(lbBgmSom);
		pBgmInputs.add(tfBgmSom);
		pBgmInputs.add(bBgmBsoAF);
		pBgmInputs.add(lbBgmPrincipal);
		pBgmInputs.add(tfBgmPrincipal);
		pBgmInputs.add(lbBgmMop);
		pBgmInputs.add(tfBgmMop);
		pBgmInputs.add(bBgmOrdersAF);
		pBgmOutputs.add(lbBgmSPD);
		pBgmOutputs.add(lbBgmPosPer);
		pBgmOutputs.add(lbBgmAPAPT);
		pBgmOutputs.add(lbBgmYoyPer);
		pBgmOutputs.add(lbBgmSecPer);
		pBgmOutputs.add(lbBgmPlots);
		pBgmOutputs.add(cbBgmPlots);
		pBgmOutputs.add(bBgmPlots);
		
		/*--------------------------------------------
			RND Panel
		---------------------------------------------*/
		//layout 
		JPanel pRndInputs = new JPanel();
		pRndInputs.setBounds(10, 10, fxDim-20, 235);
		pRndInputs.setBorder(BorderFactory.createTitledBorder("Input Params"));
		pRndInputs.setLayout(null);
		JPanel pRndOutputs = new JPanel();
		pRndOutputs.setBounds(10, 295, fxDim-20, 140);
		pRndOutputs.setBorder(BorderFactory.createTitledBorder("Performance"));
		pRndOutputs.setLayout(null);

		//init components
		JLabel lbCall = new JLabel("Call Type:");
		JRadioButton rbLong = new JRadioButton("Long");
		JRadioButton rbShort = new JRadioButton("Short");
		ButtonGroup bgCall = new ButtonGroup();
		bgCall.add(rbLong);
		bgCall.add(rbShort);
		JLabel lbTargetVar = new JLabel("Target Var :");
		JComboBox cbTargetVar = new JComboBox();
		JLabel lbSampleSize = new JLabel("Sample Size:");
		JTextField tfSampleSize = new JTextField("30");
		JLabel lbRndSDate = new JLabel("Start Date:");
		JTextField tfRndSDate = new JTextField("2020-01-01");
		JLabel lbRndEDate = new JLabel("End Date:");
		JTextField tfRndEDate = new JTextField(AhrDate.getTodaysDate());
		JLabel lbRndBim = new JLabel("BIM:");
		JTextField tfRndBim = new JTextField("0.95");
		JLabel lbRndSom = new JLabel("SOM:");
		JTextField tfRndSom = new JTextField("0.95");
		JLabel lbRndPrincipal = new JLabel("Principal ($):");
		JTextField tfRndPrincipal = new JTextField("100000");
		JLabel lbRndMop = new JLabel("MOP ($):");
		JTextField tfRndMop = new JTextField("10000");
		JLabel lbFilter = new JLabel("Stock Filter:");
		JComboBox cbFilter = new JComboBox();
		JButton bPrintFilter = new JButton("Print Info");
		JButton bCreateNewFilter = new JButton("Create New");
		JButton bRndCalcPerf = new JButton("Calculate Performance");
		JLabel lbRndSPD = new JLabel(baseOut[0]);						//Output Panel
		JLabel lbRndPosPer = new JLabel(baseOut[1]);
		JLabel lbRndAPAPT = new JLabel(baseOut[2]);
		JLabel lbRndYoyPer = new JLabel(baseOut[3]);
		JLabel lbRndSecPer = new JLabel(baseOut[4]);
		JLabel lbRndPlots = new JLabel("Plot Results: ");
		JComboBox cbRndPlots = new JComboBox();
		JButton bRndPlots = new JButton("Plot");

		//bounds of components
		lbCall.setBounds(10, 20, 100, 25);
		rbLong.setBounds(95, 20, 60, 25);
		rbShort.setBounds(170, 20, 70, 25);
		lbTargetVar.setBounds(10, 55, 100, 25);
		cbTargetVar.setBounds(110, 55, 130, 25);
		lbSampleSize.setBounds(270, 55, 100, 25);
		tfSampleSize.setBounds(370, 55, 80, 25);
		lbRndSDate.setBounds(10, 90, 90, 25);
		tfRndSDate.setBounds(110, 90, 80, 25);
		lbRndEDate.setBounds(270, 90, 80, 25);
		tfRndEDate.setBounds(370, 90, 80, 25);
		lbRndBim.setBounds(10, 125, 80, 25);
		tfRndBim.setBounds(110, 125, 80, 25);
		lbRndSom.setBounds(270, 125, 80, 25);
		tfRndSom.setBounds(370, 125, 80, 25);
		lbRndPrincipal.setBounds(10, 160, 100, 25);
		tfRndPrincipal.setBounds(110, 160, 80, 25);
		lbRndMop.setBounds(270, 160, 80, 25);
		tfRndMop.setBounds(370, 160, 80, 25);
		lbFilter.setBounds(10, 195, 100, 25);
		cbFilter.setBounds(110, 195, 130, 25);
		bPrintFilter.setBounds(260, 195, 85, 25);
		bCreateNewFilter.setBounds(370, 195, 85, 25);
		bRndCalcPerf.setBounds(10, 250, 180, 35);
		lbRndSPD.setBounds(10, 20, 200, 20);							//Outputs Panel
		lbRndPosPer.setBounds(10, 45, 200, 20);
		lbRndAPAPT.setBounds(10, 70, 200, 20);
		lbRndYoyPer.setBounds(240, 20, 200, 20);
		lbRndSecPer.setBounds(240, 70, 200, 20);
		lbRndPlots.setBounds(10, 105, 100, 25);
		cbRndPlots.setBounds(110, 105, 210, 25);
		bRndPlots.setBounds(330, 105, 50, 25);

		//basic functionality
		rbLong.setSelected(true);
		for(int i = 0; i < Globals.target_var_num; i++){
			cbTargetVar.addItem(Globals.tvi_monikers[i]);
		}
		for(int i = 0; i < sffList.size(); i++){
			cbFilter.addItem(sffList.get(i));
		}
		rbLong.setFont(plainFont);
		rbShort.setFont(plainFont);
		setButtonStyle(bPrintFilter);
		setButtonStyle(bCreateNewFilter);
		setButtonStyle(bRndCalcPerf);
		setButtonStyle(bRndPlots);
		lbRndSPD.setFont(monoFont);
		lbRndPosPer.setFont(monoFont);
		lbRndAPAPT.setFont(monoFont);
		lbRndSecPer.setFont(monoFont);
		lbRndYoyPer.setFont(monoFont);
		for(int i = 0; i < plotList.length; i++){
			cbRndPlots.addItem(plotList[i]);
		}

		//button functionality
		bPrintFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String sfName = cbFilter.getSelectedItem().toString();
				if(sfName.equals("None")){
					String message = "No filter selected.";
					JOptionPane.showMessageDialog(frame, message, "Input Error", JOptionPane.ERROR_MESSAGE);
				}else{
					ArrayList<ArrayList<String>> fc = AhrIO.scanFile("./../data/filters/"+sfName+".txt", "~");
					System.out.println("===== Active Filters for "+sfName+" ====="); 
					AhrAL.print(fc);
				}
			}
		});
		bCreateNewFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				createNewFilter(cbFilter);
			}
		});
		bBgmRunRnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//move params to RND pane
				String keyNum = cbKeyNum.getSelectedItem().toString();
				int tvi = -1;
				String spd = "";
				String ksPath = "";
				if(rbSK.isSelected()){
					ksPath = "./../out/sk/log/keys_struct.txt";
				}else{
					ksPath = "./../out/ak/log/ak_log.txt";
				}
				FCI fciKS = new FCI(true, ksPath);
				ArrayList<String> ksRow = AhrIO.scanRow(ksPath, ",", keyNum);
				tvi = Integer.parseInt(ksRow.get(fciKS.getIdx("tvi")));
				spd = ksRow.get(fciKS.getIdx("spd"));
				cbTargetVar.setSelectedIndex(tvi);
				tfSampleSize.setText(spd);
				tfRndSDate.setText(tfBgmSDate.getText());
				tfRndEDate.setText(tfBgmEDate.getText());
				tfRndBim.setText(tfBgmBim.getText());
				tfRndSom.setText(tfBgmSom.getText());
				tfRndPrincipal.setText(tfBgmPrincipal.getText());
				tfRndMop.setText(tfBgmMop.getText());
				//run RND calc perf
				bRndCalcPerf.doClick();
				//move focus to RND pane
				tpKeyPerf.setSelectedIndex(1);
			}
		});
		bRndCalcPerf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//suspend GUI to while in progress
				bPrintFilter.setEnabled(false);
				bCreateNewFilter.setEnabled(false);
				bRndCalcPerf.setEnabled(false);
				bRndPlots.setEnabled(false);
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				//setup attrs from GUI
				AttributesSK kattr = new AttributesSK();
				String sdate = tfRndSDate.getText();
				String edate = tfRndEDate.getText();
				int sampleSize = 30;	//def val
				try{
					sampleSize = Integer.parseInt(tfSampleSize.getText());
				}catch(NumberFormatException ex){
				}
				int tvi = 6;			//def val, 6 = 5-Day Inter %
				try{
					tvi = cbTargetVar.getSelectedIndex();
				}catch(NumberFormatException ex){
				}
				double principal = 100000.0;
				try{
					principal = Double.parseDouble(tfRndPrincipal.getText());
				}catch(NumberFormatException ex){
				}
				double maxOrderPrice = 10000;
				try{
					maxOrderPrice = Double.parseDouble(tfRndMop.getText());
				}catch(NumberFormatException ex){
				}
				String msMask = "xxxxxxxx";
				String narMask = "1111";
				kattr.setSDate(sdate);
				kattr.setEDate(edate);
				kattr.setSPD(sampleSize);
				kattr.setTVI(tvi);
				kattr.setMsMask(msMask);
				kattr.setNarMask(narMask);
				//create rnd basis file from attrs
				BGM_Manager bgmm = new BGM_Manager(kattr);
				bgmm.genBasisRnd(1.0);
				//get rnd basis file
				String kpPath = "./../out/sk/log/rnd/keys_perf.txt";
				ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");
				FCI fciKP = new FCI(true, kpPath);
				int rndKeyNum = Integer.parseInt(kpFile.get(kpFile.size()-1).get(fciKP.getIdx("sk_num")));
				String bsPath = "./../out/sk/baseis/rnd/RND_"+String.valueOf(rndKeyNum)+".txt";
				File rndBasisFile = new File(bsPath);
				if(rndBasisFile.exists()){
					OrderSim osim = new OrderSim(bsPath);
					osim.setIsLong(rbLong.isSelected());
					osim.setDateRange(tfRndSDate.getText(), tfRndEDate.getText());
					osim.setTVI(tvi);
					osim.setBIM(Double.parseDouble(tfRndBim.getText()));
					osim.setSOM(Double.parseDouble(tfRndSom.getText()));
					osim.setPrincipal(principal);
					osim.setMaxOrderPrice(maxOrderPrice);
					osim.setTtvMask("111");
					osim.calcOrderList();
					//show results
					lbRndSPD.setText(baseOut[0] + String.valueOf(osim.getOrderListSPD()) + " (" + 
									String.valueOf(osim.getOrderListSize()) + " total)");
					lbRndPosPer.setText(baseOut[1] + String.format("%.2f", (osim.getPosPer()*100.0)));
					lbRndAPAPT.setText(baseOut[2] + String.format("%.4f", osim.getTrigAppr()));
					lbRndYoyPer.setText(baseOut[3] + String.format("%.4f", osim.getYoyAppr()));
					lbRndSecPer.setText(baseOut[4] + String.format("%.4f", osim.getSecAppr()));
					//preserve data for graphing
					ArrayList<ArrayList<String>> growth = osim.calcGrowth(principal);
					ArrayList<String> growthHeader = new ArrayList<String>();
					growthHeader.add("date");
					growthHeader.add("growth");
					growth.add(0, growthHeader);
					AhrIO.writeToFile("./../data/r/rdata/pa_portgrowth.csv", AhrDTF.melt(growth, "date"), ",");
				}else{
					String message = "The path : "+bsPath+" does not exist.";
					JOptionPane.showMessageDialog(frame, message, "Path Error", JOptionPane.ERROR_MESSAGE);
				}
				//resume GUI
				frame.setCursor(null);
				bPrintFilter.setEnabled(true);
				bCreateNewFilter.setEnabled(true);
				bRndCalcPerf.setEnabled(true);
				bRndPlots.setEnabled(true);
			}
		});
		bRndPlots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//set up params for plots
				int xdim = 1050;
				int ydim = 650;
				//get plot for selected option
				int idx = cbRndPlots.getSelectedIndex();
				//System.out.println("==> Plot Idx = " + idx);
				if(idx == 0){//appr distn: b&w and cumlative
					String plotPathBaw = "./../resources/pa_distn_baw.png";
					String plotPathCdf = "./../resources/pa_distn_cdf.png";
					String titleBaw = "B&W for All Appr %s for RND Method";
					String titleCdf = "CDF for All Appr %s for RND Method";
					xdim = 600;
					ydim = 300;

					//get all trig %s (while trimming) from orderlist, bounds from orderlist_byappr
					String olPath = "./../data/tmp/os_orderlist_byappr.txt";
					FCI fciOL = new FCI(false, olPath);
					ArrayList<String> methAppr = AhrIO.scanCol(olPath, ",", fciOL.getIdx("method_appr"));
					ArrayList<Double> trimAppr = new ArrayList<Double>();
					double trimVal = 0.025;	//0-1, what % of vals you want trimmed on top & bot
					int startTrim = (int)((double)methAppr.size() * trimVal);
					int endTrim = (int)((double)methAppr.size() - ((double)methAppr.size() * trimVal));
					//System.out.println("StartTrim = "+startTrim+"  |  EndTrim = "+endTrim+"  |  "+
					//				"Count = "+methAppr.size());
					for(int i = startTrim; i < endTrim; i++){
						trimAppr.add(Double.parseDouble(methAppr.get(i)));
					}
					double loBound = trimAppr.get(0) - 1.0;
					double hiBound = trimAppr.get(trimAppr.size()-1) + 1.0;
					//calc B&W plot
					RCode rcBaw = new RCode();
					rcBaw.setTitle(titleBaw);
					rcBaw.setXLabel("");
					rcBaw.setYLabel("Appreciation (%)");
					rcBaw.hardLimY(loBound, hiBound);
					rcBaw.flipCoords();
					rcBaw.createBAW(trimAppr, plotPathBaw, xdim, ydim);
					//rcBaw.printCode();
					rcBaw.writeCode("./../data/r/rscripts/pa_distn_baw.R");
					rcBaw.runScript("./../data/r/rscripts/pa_distn_baw.R");
					//calc CDF plot
					RCode rcCDF = new RCode();
					rcCDF.setTitle(titleCdf);
					rcCDF.setXLabel("Appreciation (%)");
					rcCDF.setYLabel("Cumulative Probability");
					rcCDF.hardLimX(loBound, hiBound);
					rcCDF.createCDF(trimAppr, plotPathCdf, xdim, ydim);
					//rcCDF.printCode();
					rcCDF.writeCode("./../data/r/rscripts/pa_distn_cdf.R");
					rcCDF.runScript("./../data/r/rscripts/pa_distn_cdf.R");
					//show both plots on popout frame
					JFrame rframe = new JFrame();
					rframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					rframe.setTitle("Method Appreciation Distribution (BGM)");
					JLabel lbBawPlot = new JLabel();
					JLabel lbCdfPlot = new JLabel();
					lbBawPlot.setPreferredSize(new Dimension(xdim, ydim));
					lbCdfPlot.setPreferredSize(new Dimension(xdim, ydim));
					ImageIcon iiBaw = new ImageIcon(plotPathBaw);
					ImageIcon iiCdf = new ImageIcon(plotPathCdf);
					lbBawPlot.setIcon(iiBaw);
					lbCdfPlot.setIcon(iiCdf);
					rframe.getContentPane().add(lbBawPlot, BorderLayout.NORTH);
					rframe.getContentPane().add(lbCdfPlot, BorderLayout.SOUTH);
					rframe.pack();
					rframe.setVisible(true);
					iiBaw.getImage().flush();
					iiCdf.getImage().flush();
				}else if(idx == 1){//trigger code pie chart
					String plotPath = "./../resources/pa_trigger_codes.png";
					xdim = 330;
					ydim = 330;
					//get all trigger codes from orderlist, create pie chart
					String olPath = "./../data/tmp/os_orderlist.txt";
					FCI fciOL = new FCI(false, olPath);
					ArrayList<String> trigCodes = AhrIO.scanCol(olPath, ",", fciOL.getIdx("trigger_code"));
					ArrayList<ArrayList<String>> pieAL = AhrAL.countUniq(trigCodes);
					RCode rcPie = new RCode();
					rcPie.setTitle("Trigger Codes for RND Method");
					rcPie.setXLabel("");
					rcPie.setYLabel("");
					rcPie.createPie(pieAL, plotPath, xdim, ydim);
					//rcPie.printCode();
					rcPie.writeCode("./../data/r/rscripts/pa_trigger_codes.R");
					rcPie.runScript("./../data/r/rscripts/pa_trigger_codes.R");
					//show plot on new popout frame
					JFrame rframe = new JFrame();
					rframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					rframe.setTitle("Trigger Code Count");
					JLabel lbPlot = new JLabel();
					lbPlot.setPreferredSize(new Dimension(xdim, ydim));
					ImageIcon ii = new ImageIcon(plotPath);
					lbPlot.setIcon(ii);
					rframe.getContentPane().add(lbPlot, BorderLayout.CENTER);
					rframe.pack();
					rframe.setVisible(true);
					ii.getImage().flush();
				}else if(idx == 2){//Portfolio growth powered by OrderSim
					//input params from GUI
					String dataPath = "./../data/r/rdata/pa_portgrowth.csv";
					String plotPath = "./../resources/pa_portgrowth.png";
					String scriptPath = "./../data/r/rscripts/pa_portgrowth.R";
					String bimStr = tfRndBim.getText();
					String somStr = tfRndSom.getText();
					String mopStr = tfRndMop.getText();
					//other plot attrs
					String plotTitle = "Portfolio Growth ($)  [BIM = "+bimStr+", SOM = "+somStr+", MOP = "+mopStr+"]";
					//create growth graph
					RCode rcode = new RCode();
					rcode.setXLabel("Date");
					rcode.setYLabel("Porfolio Value ($)");
					rcode.setTitle(plotTitle);
					rcode.createTimeSeries(dataPath, plotPath, xdim, ydim);
					//rcode.printCode();
					rcode.writeCode(scriptPath);
					rcode.runScript(scriptPath);
					//show plot on new popout frame
					JFrame rframe = new JFrame();
					rframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					rframe.setTitle("Portfolio Growth Plot (BGM)");
					JLabel lbPlot = new JLabel();
					lbPlot.setPreferredSize(new Dimension(xdim, ydim));
					ImageIcon ii = new ImageIcon(plotPath);
					lbPlot.setIcon(ii);
					rframe.getContentPane().add(lbPlot, BorderLayout.CENTER);
					rframe.pack();
					rframe.setVisible(true);
					ii.getImage().flush();
				}
			}
		});


		//add
		pRndInputs.add(lbCall);
		pRndInputs.add(rbLong);
		pRndInputs.add(rbShort);
		pRndInputs.add(lbTargetVar);
		pRndInputs.add(cbTargetVar);
		pRndInputs.add(lbSampleSize);
		pRndInputs.add(tfSampleSize);
		pRndInputs.add(lbRndSDate);
		pRndInputs.add(tfRndSDate);
		pRndInputs.add(lbRndEDate);
		pRndInputs.add(tfRndEDate);
		pRndInputs.add(lbRndBim);
		pRndInputs.add(tfRndBim);
		pRndInputs.add(lbRndSom);
		pRndInputs.add(tfRndSom);
		pRndInputs.add(lbRndPrincipal);
		pRndInputs.add(tfRndPrincipal);
		pRndInputs.add(lbRndMop);
		pRndInputs.add(tfRndMop);
		pRndInputs.add(lbFilter);
		pRndInputs.add(cbFilter);
		pRndInputs.add(bPrintFilter);
		pRndInputs.add(bCreateNewFilter);
		pRndOutputs.add(lbRndSPD);
		pRndOutputs.add(lbRndPosPer);
		pRndOutputs.add(lbRndAPAPT);
		pRndOutputs.add(lbRndSecPer);
		pRndOutputs.add(lbRndYoyPer);
		pRndOutputs.add(lbRndPlots);
		pRndOutputs.add(cbRndPlots);
		pRndOutputs.add(bRndPlots);

		//add everything together
		pBGM.add(pBgmInputs);
		pBGM.add(bBgmCalcPerf);
		pBGM.add(bBgmRunRnd);
		pBGM.add(pBgmOutputs);
		pRND.add(pRndInputs);
		pRND.add(bRndCalcPerf);
		pRND.add(pRndOutputs);
		tpKeyPerf.add("BGM Perf", pBGM);
		tpKeyPerf.add("RND Perf", pRND);
		frame.add(tpKeyPerf);
		frame.setVisible(true);
	}

	//GUI related, set specific style for a JButton
	public void setButtonStyle(JButton btn){
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}

	//gets list of all possible keys given BGM and SK or AK
	public ArrayList<String> getKeyNumList(boolean is_sk, String bgm){
		String bgmLC = bgm.toLowerCase();
		ArrayList<String> nums = new ArrayList<String>();
		String fpath = "";
		if(is_sk){
			fpath = "./../out/sk/log/"+bgmLC+"/keys_struct.txt";
			FCI fciKS = new FCI(true, fpath);
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile(fpath, ",");
			if(fc.size() > 1){
				for(int i = 1; i < fc.size(); i++){
					nums.add(fc.get(i).get(fciKS.getIdx("sk_num")));
				}
			}
		}else{
			fpath = "./../out/ak/log/ak_log.txt";
			FCI fciLA = new FCI(true, fpath);
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile(fpath, ",");
			if(fc.size() > 1){
				for(int i = 1; i < fc.size(); i++){
					if(!fc.get(i).get(fciLA.getIdx("bgm")).equals("RND")){
						nums.add(fc.get(i).get(fciLA.getIdx("ak_num")));
					}
				}
			}
		}
		return nums;
	}

	//creates GUI to create new filter to be used in RND panel
	public void createNewFilter(JComboBox cbFilters){
		//lists and overarching structs
		String[] indicatorList = {"S/M 20", "S/M 10", "S/M 5", "S/M 2", "S/I 20", "S/I 10", "S/I 5", "S/I 2", "SMA 20",
								"SMA 10", "SMA 5", "SMA 2", "RSI", "MACD", "MACD Histogram", "CMF", "Bollinger Bandwidth",
								"%B", "ROC", "MFI", "CCI", "Mass Index", "TSI", "Ult Osc"};
		ArrayList<String> filterFiles = AhrIO.getNamesInPath("./../data/filters/");
		int maxFileNum = 0;
		for(int i = 0; i < filterFiles.size(); i++){
			String[] ffParts = filterFiles.get(i).split("_");
			int fileNum = Integer.parseInt(ffParts[1]);
			if(fileNum > maxFileNum){
				maxFileNum = fileNum;
			}
		}
		StockFilter sf = new StockFilter();
		
		//layout
		JDialog dialog = new JDialog();
		dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.setTitle("Create Custom Filter");
		dialog.setSize(560, 580);
		dialog.setLayout(null);
		JPanel pBasics = new JPanel();
		pBasics.setLayout(null);
		pBasics.setBorder(BorderFactory.createTitledBorder("Basic Filter Params"));
		JPanel pNormInds = new JPanel();
		pNormInds.setLayout(null);
		pNormInds.setBorder(BorderFactory.createTitledBorder("Normalized Indicators"));
		
		//components
		JLabel lbMC = new JLabel("Market Cap:");
		JTextField tfStartMC = new JTextField("100");
		JLabel lbMil1 = new JLabel("mil  to");
		JTextField tfEndMC = new JTextField("10000000");
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
		JButton bReset = new JButton("Reset Inputs");
		JButton bToFile = new JButton("Save And Close");

		
		//component bounds
		pBasics.setBounds(10, 10, 515, 165);				//basic params
		lbMC.setBounds(10, 20, 110, 25);
		tfStartMC.setBounds(110, 20, 100, 25);
		lbMil1.setBounds(220, 20, 60, 25);
		tfEndMC.setBounds(280, 20, 100, 25);
		lbMil2.setBounds(390, 20, 40, 25);
		lbSector.setBounds(10, 55, 110, 25);
		tfSector.setBounds(110, 55, 270, 25);
		bSectorList.setBounds(390, 55, 50, 25);
		bSectorAll.setBounds(450, 55, 50, 25);
		lbIndustry.setBounds(10, 90, 110, 25);
		taIndustry.setBounds(110, 90, 270, 30);
		bIndustryList.setBounds(390, 90, 50, 25);
		bIndustryAll.setBounds(450, 90, 50, 25);
		bUpdateBasics.setBounds(10, 130, 200, 25);
		pNormInds.setBounds(10, 185, 515, 85);				//add indicator
		lbIndicator.setBounds(10, 20, 110, 25);
		cbIndicator.setBounds(110, 20, 270, 25);
		lbIndRangeStart.setBounds(50, 50, 55, 25);
		tfIndRangeStart.setBounds(110, 50, 90, 25);
		lbIndRangeEnd.setBounds(240, 50, 55, 25);
		tfIndRangeEnd.setBounds(290, 50, 90, 25);
		bIndAdd.setBounds(390, 35, 50, 25);
		lbFilterDetails.setBounds(20, 270, 120, 25);		//filter details & buttons
		taFilterDetails.setBounds(60, 300, 450, 170);
		spFilterDetails.setBounds(60, 300, 450, 170);
		bReset.setBounds(110, 490, 150, 35);
		bToFile.setBounds(310, 490, 150, 35);

		//basic functionality
		setButtonStyle(bSectorList);
		setButtonStyle(bSectorAll);
		setButtonStyle(bIndustryList);
		setButtonStyle(bIndustryAll);
		setButtonStyle(bUpdateBasics);
		setButtonStyle(bIndAdd);
		setButtonStyle(bReset);
		setButtonStyle(bToFile);
		
		taIndustry.setLineWrap(true);
		taIndustry.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		for(int i = 0; i < indicatorList.length; i++){
			cbIndicator.addItem(indicatorList[i]);
		}
		taFilterDetails.setLineWrap(true);
		taFilterDetails.setFont(monoFont);
	
		//init starting filter lines
		sf.setMarketCap(Integer.parseInt(tfStartMC.getText()), Integer.parseInt(tfEndMC.getText()));
		sf.setSectors(tfSector.getText());
		sf.setIndustries(taIndustry.getText());
		taFilterDetails.setText(sf.getText());

		//button functionality
		bSectorList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ArrayList<ArrayList<String>> secCodes = AhrIO.scanFile("./../in/sector_codes.txt", "~");
				FCI fciSC = new FCI(false, "./../in/sector_codes.txt");
				ArrayList<String> uniqSectors = new ArrayList<String>();
				for(int i = 0; i < secCodes.size(); i++){
					String itrSector = secCodes.get(i).get(fciSC.getIdx("sector"));
					if(!uniqSectors.contains(itrSector)){
						uniqSectors.add(itrSector);
					}
				}
				//std out print
				//System.out.println("******* Sector List *******");
				//for(int i = 0; i < uniqSectors.size(); i++){
				//	System.out.println("  "+(i+1)+") "+uniqSectors.get(i));
				//}
				//JOptionPane print
				String message = "";
				for(int i = 0; i < uniqSectors.size(); i++){
					message += "  "+(i+1)+") "+uniqSectors.get(i);
					if(i != (uniqSectors.size()-1)){
						message += "\n";
					}
				}
				JOptionPane.showMessageDialog(dialog, message, "All Sectors", JOptionPane.PLAIN_MESSAGE);
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
					JOptionPane.showMessageDialog(dialog, "All sectors already selected.");
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
					JOptionPane.showMessageDialog(dialog, message, "All Industries within "+secName, JOptionPane.PLAIN_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(dialog, "Only one sector must be selected for this filter"+
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
					//set taIndustry to subcodes
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
						JOptionPane.showMessageDialog(dialog, "All industries already selected.");
					}else{
						taIndustry.setText(newStr);
					}
				}else{
					JOptionPane.showMessageDialog(dialog, "Only one sector must be selected for this filter"+
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
				boolean good_vals = true;
				String errMessage = "Error(s):\n";
				if(!mcStart.matches("[0-9]+") || !mcEnd.matches("[0-9]+")){
					errMessage += "\nMarket cap values must be integers.";
					good_vals = false;
				}
				if(!rawSecStr.replace(",","").matches("[0-9]+")){
					errMessage += "\nSector values must be comma seperated integers";
					good_vals = false;
				} 
				if(!rawIndStr.replace(",","").matches("[0-9]+") && !rawIndStr.equals("")){
					errMessage += "\nIndustry values must be comma seperated integers.";
					good_vals = false;
				}
				if(good_vals){
					sf.setMarketCap(Integer.parseInt(mcStart), Integer.parseInt(mcEnd));
					sf.setSectors(rawSecStr);
					sf.setIndustries(rawIndStr);
					taFilterDetails.setText(sf.getText());
					dialog.setVisible(true);
				}else{
					JOptionPane.showMessageDialog(dialog, errMessage, "Input Error", JOptionPane.ERROR_MESSAGE);
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
					String message = "Indicator values can only be integers in range [0-65535]";
					JOptionPane.showMessageDialog(dialog, message, "Input Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		bReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				sf.resetFilter();
				taFilterDetails.setText(sf.getText());
			}
		});
		bToFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//get new filter number
				int maxFileNum = -1;
				ArrayList<String> sfFiles = AhrIO.getNamesInPath("./../data/filters/");
				for(int i = 0; i < sfFiles.size(); i++){
					int itrFileNum = Integer.parseInt(sfFiles.get(i).split("_")[1]);
					if(itrFileNum > maxFileNum){
						maxFileNum = itrFileNum;
					}
				}
				//update cb in KeyPerf GUI
				String filterFileName = "sfilter_"+String.valueOf(maxFileNum+1);
				cbFilters.addItem(filterFileName);
				cbFilters.setSelectedIndex(cbFilters.getItemCount()-1);
				//write to file
				String filePath = "./../data/filters/"+filterFileName+".txt";
				AhrIO.writeToFile(filePath, sf.getData(), "~");
				dialog.dispose();
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
		dialog.add(pBasics);
		pNormInds.add(lbIndicator);
		pNormInds.add(cbIndicator);
		pNormInds.add(lbIndRangeStart);
		pNormInds.add(tfIndRangeStart);
		pNormInds.add(lbIndRangeEnd);
		pNormInds.add(tfIndRangeEnd);
		pNormInds.add(bIndAdd);
		dialog.add(pNormInds);
		dialog.add(lbFilterDetails);
		dialog.add(spFilterDetails);
		dialog.add(bReset);
		dialog.add(bToFile);
		dialog.setVisible(true);
	}

	//generate random basis files based on GUI input params
	public void createRndBasisFile(String path, int sampleSize, String sdate, String edate, String filterName){
		ArrayList<ArrayList<String>> tf = new ArrayList<ArrayList<String>>();
		ArrayList<String> header = new ArrayList<String>();
		header.add("//(0) date");
		header.add("(1) ticker");
		header.add("(2) ttv_code");
		tf.add(header);
		ArrayList<String> bdFiles = AhrIO.getNamesInPath("./../../DB_Intrinio/Clean/ByDate/");
		FCI fciBD = new FCI(false, "./../../DB_Intrinio/Clean/ByDate/");
		ArrayList<String> dates = new ArrayList<String>();
		for(int i = 0; i < bdFiles.size(); i++){
			String itrDate = bdFiles.get(i);
			if(AhrDate.isDateInPeriod(itrDate, sdate, edate)){
				dates.add(itrDate);
			}
		}
		Collections.sort(dates);
		if(filterName.equals("None")){//no filtering of rnd selection
			for(int i = 0; i < dates.size(); i++){
				if(i%50 == 0){
					//System.out.println("--> Rnd Basis File progress: "+i+" out of "+dates.size());
				}
				ArrayList<String> tickers = AhrIO.scanCol("./../../DB_Intrinio/Clean/ByDate/"+dates.get(i)+".txt",
											"~", fciBD.getIdx("ticker"));
				int flexibleSS = sampleSize;
				if(tickers.size() < sampleSize){
					flexibleSS = tickers.size();
				}
				ArrayList<String> uniqTickers = new ArrayList<String>();
				for(int j = 0; j < flexibleSS; j++){
					ArrayList<String> line = new ArrayList<String>();
					Random rnd = new Random();
					int rndIdx = rnd.nextInt(tickers.size());
					while(uniqTickers.contains(tickers.get(rndIdx))){
						rndIdx = rnd.nextInt(tickers.size());
					}
					uniqTickers.add(tickers.get(rndIdx));
					line.add(dates.get(i));				//Date
					line.add(tickers.get(rndIdx));		//Ticker
					line.add("2");						//TTV Code, meaningless but necessary
					tf.add(line);			
				}
			}
		}else{//need to apply filter rules to rnd selection
			StockFilter sf = new StockFilter("./../data/filters/"+filterName+".txt");
			for(int i = 0; i < dates.size(); i++){
				if(i%2 == 0){
					//System.out.println("--> Rnd Basis File progress: "+i+" out of "+dates.size());
				}
				//System.out.println("==> In applyFilter("+dates.get(i)+")");
				sf.applyFilter(dates.get(i));
				ArrayList<ArrayList<String>> results = sf.getResults();
				int flexibleSS = sampleSize;
				if(results.size() < sampleSize){
					flexibleSS = results.size();
				}
				//System.out.println("-> results size = " + results.size());
				//for(int x = 0; x < 5; x++){
				//	System.out.println(results.get(x));
				//}
				ArrayList<String> uniqTickers = new ArrayList<String>();
				for(int j = 0; j < flexibleSS; j++){
					ArrayList<String> line = new ArrayList<String>();
					Random rnd = new Random();
					int rndIdx = rnd.nextInt(results.size());
					while(uniqTickers.contains(results.get(rndIdx).get(0))){
						rndIdx = rnd.nextInt(results.size());
						//System.out.print(rndIdx+", ");
						//System.out.println("--> uniqTicker size = " + uniqTickers.size());
					}
					uniqTickers.add(results.get(rndIdx).get(0));
					line.add(dates.get(i));
					line.add(results.get(rndIdx).get(0));
					tf.add(line);
				}
			}
		}
		AhrIO.writeToFile(path, tf, ",");
	}

}
