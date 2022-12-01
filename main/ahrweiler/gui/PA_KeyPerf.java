package ahrweiler.gui;
import ahrweiler.util.*;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import ahrweiler.support.OrderSim;
import ahrweiler.support.StockFilter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;

public class PA_KeyPerf extends JFrame {

	Font monoFont = new Font(Font.MONOSPACED, Font.BOLD, 11);

	public PA_KeyPerf(){
		drawGUI();
	}

	public void drawGUI(){
		//lists and overarching structs
		String[] bgmList = {"ANN", "GAD2", "GAB3"};
		String[] tvarList = {"Intra 1", "Inter 1", "Inter 2", "Inter 3", "Inter 5", "Inter 10"};
		ArrayList<String> sffList = AhrIO.getNamesInPath("./../data/filters/");
		sffList.add(0, "None");
		ArrayList<String> keyNumList = new ArrayList<String>();
		String[] plotList = {"Appr Distribution", "Trigger Codes", "Portfolio Growth ($)"};

		//layout components
		int fxDim = 510;
		int fyDim = 475;
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setTitle("Key Performance");
		setSize(fxDim, fyDim+37);
		setLayout(null);
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
		pBgmOutputs.setBounds(10, 295, fxDim-20, 140);
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
		Button bBgmDatesAF = new Button("Autofill");
		JLabel lbBgmBim = new JLabel("BIM:");
		JTextField tfBgmBim = new JTextField("0.95");
		JLabel lbBgmSom = new JLabel("SOM:");
		JTextField tfBgmSom = new JTextField("0.95");
		Button bBgmBsoAF = new Button("Autofill");
		JLabel lbBgmMos = new JLabel("MOS ($):");
		JTextField tfBgmMos = new JTextField("10000");
		Button bAutofill = new Button("Autofill");
		Button bBgmCalcPerf = new Button("Calculate Performance");		//Calc Perf Button
		JProgressBar pbBgmCalcPerf = new JProgressBar(0, 2000);
		JLabel lbBgmSPD = new JLabel("SPD    : -");						//Output Panel
		JLabel lbBgmPosPer = new JLabel("Pos %  : -");
		JLabel lbBgmTrigPer = new JLabel("Trig % : -");
		JLabel lbBgmSecPer = new JLabel("Sec %  : -");
		JLabel lbBgmYoyPer = new JLabel("YoY %  : -");	
		JLabel lbBgmPlots = new JLabel("Plot Results: ");
		JComboBox cbBgmPlots = new JComboBox();
		Button bBgmPlots = new Button("Plot");


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
		lbBgmMos.setBounds(10, 195, 80, 25);
		tfBgmMos.setBounds(105, 195, 80, 25);
		bBgmCalcPerf.setBounds(10, 250, 180, 40);						//Calc Perf Button
		pbBgmCalcPerf.setBounds(10, 250, 100, 30);
		lbBgmSPD.setBounds(10, 20, 200, 20);							//Outputs Panel
		lbBgmPosPer.setBounds(10, 45, 200, 20);
		lbBgmTrigPer.setBounds(240, 20, 200, 20);
		lbBgmSecPer.setBounds(240, 45, 200, 20);
		lbBgmYoyPer.setBounds(240, 70, 200, 20);
		lbBgmPlots.setBounds(10, 105, 100, 25);
		cbBgmPlots.setBounds(110, 105, 210, 25);
		bBgmPlots.setBounds(330, 105, 50, 25); 

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
		lbBgmSPD.setFont(monoFont);
		lbBgmPosPer.setFont(monoFont);
		lbBgmTrigPer.setFont(monoFont);
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
				String bgmLC = cbMethod.getSelectedItem().toString().toLowerCase();
				String knum = cbKeyNum.getSelectedItem().toString();
				if(rbSK.isSelected()){
					FCI fciKS = new FCI(true, "./../out/ml/"+bgmLC+"/keys_struct.txt");
					ArrayList<String> ksRow = AhrIO.scanRow("./../out/ml/"+bgmLC+"/keys_struct.txt", ",", knum);
					tfBgmSDate.setText(ksRow.get(fciKS.getIdx("start_date")));
					tfBgmEDate.setText(ksRow.get(fciKS.getIdx("end_date")));
				}else{
					FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
					ArrayList<String> alRow = AhrIO.scanRow("./../baseis/log/ak_log.txt", ",", knum);
					tfBgmSDate.setText(alRow.get(fciAL.getIdx("start_date")));
					tfBgmEDate.setText(alRow.get(fciAL.getIdx("end_date")));
				}
			}
		});
		bBgmBsoAF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String bgmLC = cbMethod.getSelectedItem().toString().toLowerCase();
				String knum = cbKeyNum.getSelectedItem().toString();
				if(rbSK.isSelected()){
					FCI fciKP = new FCI(true, "./../out/ml/"+bgmLC+"/keys_perf.txt");
					ArrayList<String> kpRow = AhrIO.scanRow("./../out/ml/"+bgmLC+"/keys_perf.txt", ",", knum);
					tfBgmBim.setText(kpRow.get(fciKP.getIdx("bim")));
					tfBgmSom.setText(kpRow.get(fciKP.getIdx("som")));
				}else{
					FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
					ArrayList<String> alRow = AhrIO.scanRow("./../baseis/log/ak_log.txt", ",", knum);
					String call = alRow.get(fciAL.getIdx("call"));
					String[] bsoParts = alRow.get(fciAL.getIdx("ak_bim_som")).split("\\|");
					if(bsoParts.length > 0){
						tfBgmBim.setText(bsoParts[0]);
						tfBgmSom.setText(bsoParts[1]);
					}else{
						if(call.equals("0")){
							tfBgmBim.setText("0.95");
							tfBgmSom.setText("0.95");
						}else{
							tfBgmBim.setText("1.05");
							tfBgmSom.setText("1.05");
						}
					}
				}
			}
		});
		bBgmCalcPerf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				bBgmCalcPerf.setEnabled(false);
				bBgmPlots.setEnabled(false);
				//get inputs from GUI
				String bgmUC = cbMethod.getSelectedItem().toString();
				String bgmLC = bgmUC.toLowerCase();
				int knum = Integer.parseInt(cbKeyNum.getSelectedItem().toString());
				String ttvMask = "";
				if(cbTrain.isSelected()){
					ttvMask += "1";
				}else{
					ttvMask += "0";
				}
				if(cbTest.isSelected()){
					ttvMask += "1";
				}else{
					ttvMask += "0";
				}
				if(cbVerify.isSelected()){
					ttvMask += "1";
				}else{
					ttvMask += "0";
				}
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
				osim.setMaxOrderSize(Double.parseDouble(tfBgmMos.getText()));
				osim.calcOrderList();
				lbBgmSPD.setText("SPD    : "+String.valueOf(osim.getOrderListSPD()) + " (" + 
								String.valueOf(osim.getOrderListSize()) + " total)");
				lbBgmPosPer.setText("Pos %  : " + String.format("%.4f", osim.getPosPer()));
				lbBgmTrigPer.setText("Trig % : " + String.format("%.4f", osim.getTrigAppr()));
				lbBgmSecPer.setText("Sec %  : " + String.format("%.4f", osim.getSecAppr()));
				lbBgmYoyPer.setText("YoY %  : " + String.format("%.4f", osim.getYoyAppr()));
				//Preserve data for graphing
				ArrayList<ArrayList<String>> growth = osim.calcGrowth(100000.0);
				ArrayList<String> growthHeader = new ArrayList<String>();
				growthHeader.add("date");
				growthHeader.add("growth");
				growth.add(0, growthHeader);
				AhrIO.writeToFile("./../data/r/rdata/pa_portgrowth.csv", AhrDTF.melt(growth, "date"), ",");
				bBgmCalcPerf.setEnabled(true);
				bBgmPlots.setEnabled(true);
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
				System.out.println("==> Plot Idx = " + idx);
				if(idx == 0){//appr distn: b&w and cumlative
					String plotPathBaw = "./../resources/pa_distn_baw.png";
					String plotPathCdf = "./../resources/pa_distn_cdf.png";
					String titleBaw = "All Method Appr %s for "+kmonik+" in B&W";
					String titleCdf = "All Method Appr %s for "+kmonik+" in CDF";
					xdim = 600;
					ydim = 300;
					//get all trig %s from orderlist, create Box & Whisker
					String olPath = "./../data/orderlist/orderlist.txt";
					FCI fciOL = new FCI(false, olPath);
					ArrayList<String> trigStr = AhrIO.scanCol(olPath, ",", fciOL.getIdx("method_appr"));
					ArrayList<Double> trig = new ArrayList<Double>();
					for(int i = 0; i < trigStr.size(); i++){
						trig.add(Double.parseDouble(trigStr.get(i)));
					} 
					RCode rcBaw = new RCode();
					rcBaw.setTitle(titleBaw);
					rcBaw.limY(-10, 30);
					rcBaw.flipCoords();
					rcBaw.createBAW(trig, plotPathBaw, xdim, ydim);
					rcBaw.printCode();
					rcBaw.writeCode("./../data/r/rscripts/pa_distn_baw.R");
					rcBaw.runScript("./../data/r/rscripts/pa_distn_baw.R");
					//calc CDF plot
					RCode rcCDF = new RCode();
					rcCDF.setTitle(titleCdf);
					rcCDF.limX(-10, 30);
					rcCDF.createCDF(trig, plotPathCdf, xdim, ydim);
					rcCDF.printCode();
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
					String olPath = "./../data/orderlist/orderlist.txt";
					FCI fciOL = new FCI(false, olPath);
					ArrayList<String> trigCodes = AhrIO.scanCol(olPath, ",", fciOL.getIdx("trigger_code"));
					ArrayList<ArrayList<String>> pieAL = AhrAL.countUniq(trigCodes);
					RCode rcPie = new RCode();
					rcPie.setTitle("Trigger Codes for "+kmonik);
					rcPie.setXLabel("");
					rcPie.setYLabel("");
					rcPie.createPie(pieAL, plotPath, xdim, ydim);
					rcPie.printCode();
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
					String mosStr = tfBgmMos.getText();
					ArrayList<String> fdates = AhrIO.scanCol(dataPath, ",", 0);
					fdates.remove(0);
					String plotTitle = "Portfolio Growth "+kmonik+" ($)  [BIM = "+bimStr+", SOM = "+somStr+", MOS = "+mosStr+"]";
					String startTrainDate = "";
					String endTrainDate = "";
					if(rbSK.isSelected()){
						FCI fciKS = new FCI(true, "./../out/ml/"+bgmLC+"/keys_struct.txt");
						ArrayList<String> ksRow = AhrIO.scanRow("./../out/ml/"+bgmLC+"/keys_struct.txt", ",", knum);
						startTrainDate = ksRow.get(fciKS.getIdx("start_date"));
						endTrainDate = ksRow.get(fciKS.getIdx("end_date"));
					}else{
						FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
						ArrayList<String> alRow = AhrIO.scanRow("./../baseis/log/ak_log.txt", ",", knum);
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
		pBgmInputs.add(lbBgmMos);
		pBgmInputs.add(tfBgmMos);
		pBgmOutputs.add(lbBgmSPD);
		pBgmOutputs.add(lbBgmPosPer);
		pBgmOutputs.add(lbBgmTrigPer);
		pBgmOutputs.add(lbBgmSecPer);
		pBgmOutputs.add(lbBgmYoyPer);
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
		JLabel lbSampleSize = new JLabel("Sample Size:");
		JTextField tfSampleSize = new JTextField("30");
		JLabel lbRndMos = new JLabel("MOS ($):");
		JTextField tfRndMos = new JTextField("10000");
		JLabel lbTVI = new JLabel("TVI:");
		JComboBox cbTVI = new JComboBox();
		JLabel lbMsMask = new JLabel("MS Mask:");
		JTextField tfMsMask = new JTextField("xxxxxxxx");
		JLabel lbRndSDate = new JLabel("Start Date:");
		JTextField tfRndSDate = new JTextField("2020-01-01");
		JLabel lbRndEDate = new JLabel("End Date:");
		JTextField tfRndEDate = new JTextField(AhrDate.getTodaysDate());
		JLabel lbRndBim = new JLabel("BIM:");
		JTextField tfRndBim = new JTextField("0.95");
		JLabel lbRndSom = new JLabel("SOM:");
		JTextField tfRndSom = new JTextField("0.95");
		JLabel lbFilter = new JLabel("Stock Filter:");
		JComboBox cbFilter = new JComboBox();
		Button bPrintFilter = new Button("Print Info");
		Button bCreateNewFilter = new Button("Create New");
		Button bRndCalcPerf = new Button("Calculate Performance");
		JLabel lbRndSPD = new JLabel("SPD    : -");						//Output Panel
		JLabel lbRndPosPer = new JLabel("Pos %  : -");
		JLabel lbRndTrigPer = new JLabel("Trig % : -");
		JLabel lbRndSecPer = new JLabel("Sec %  : -");
		JLabel lbRndYoyPer = new JLabel("YoY %  : -");
		JLabel lbRndPlots = new JLabel("Plot Results: ");
		JComboBox cbRndPlots = new JComboBox();
		Button bRndPlots = new Button("Plot");

		//bounds of components
		lbCall.setBounds(10, 20, 100, 25);
		rbLong.setBounds(95, 20, 60, 25);
		rbShort.setBounds(170, 20, 70, 25);
		lbTVI.setBounds(10, 55, 80, 25);
		cbTVI.setBounds(110, 55, 80, 25);
		lbSampleSize.setBounds(10, 90, 100, 25);
		tfSampleSize.setBounds(110, 90, 80, 25);
		lbRndMos.setBounds(260, 90, 80, 25);
		tfRndMos.setBounds(340, 90, 80, 25);
		lbRndSDate.setBounds(10, 125, 90, 25);
		tfRndSDate.setBounds(110, 125, 80, 25);
		lbRndEDate.setBounds(260, 125, 80, 25);
		tfRndEDate.setBounds(340, 125, 80, 25);
		lbRndBim.setBounds(10, 160, 80, 25);
		tfRndBim.setBounds(110, 160, 80, 25);
		lbRndSom.setBounds(260, 160, 80, 25);
		tfRndSom.setBounds(340, 160, 80, 25);
		lbFilter.setBounds(10, 195, 100, 25);
		cbFilter.setBounds(110, 195, 120, 25);
		bPrintFilter.setBounds(250, 195, 90, 30);
		bCreateNewFilter.setBounds(350, 195, 90, 30);
		bRndCalcPerf.setBounds(10, 250, 180, 40);
		lbRndSPD.setBounds(10, 20, 200, 20);							//Outputs Panel
		lbRndPosPer.setBounds(10, 45, 200, 20);
		lbRndTrigPer.setBounds(240, 20, 200, 20);
		lbRndSecPer.setBounds(240, 45, 200, 20);
		lbRndYoyPer.setBounds(240, 70, 200, 20);
		lbRndPlots.setBounds(10, 105, 100, 25);
		cbRndPlots.setBounds(110, 105, 210, 25);
		bRndPlots.setBounds(330, 105, 50, 25);

		//basic functionality
		rbLong.setSelected(true);
		for(int i = 0; i < tvarList.length; i++){
			cbTVI.addItem(tvarList[i]);
		}
		for(int i = 0; i < sffList.size(); i++){
			cbFilter.addItem(sffList.get(i));
		}
		lbRndSPD.setFont(monoFont);
		lbRndPosPer.setFont(monoFont);
		lbRndTrigPer.setFont(monoFont);
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
					System.out.println("==> No filter selected.");
				}else{
					ArrayList<ArrayList<String>> fc = AhrIO.scanFile("./../data/filters/"+sfName+".txt", "~");
					System.out.println("===== Active Filters for "+sfName+" ====="); 
					AhrIO.printSAL(fc);
				}
			}
		});
		bCreateNewFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String filterName = createNewFilter();
				cbFilter.addItem(filterName);
			}
		});
		bRndCalcPerf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//suspend GUI to while in progress
				bPrintFilter.setEnabled(false);
				bCreateNewFilter.setEnabled(false);
				bRndCalcPerf.setEnabled(false);
				bRndPlots.setEnabled(false);
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	
				//calc Order List
				String bgm = cbMethod.getSelectedItem().toString();
				int sampleSize = Integer.parseInt(tfSampleSize.getText());
				String sdate = tfRndSDate.getText();
				String edate = tfRndEDate.getText(); 
				String rndPath = "./../baseis/single/rnd/rnd_tmp.txt";
				createRndBasisFile(rndPath, sampleSize, sdate, edate, cbFilter.getSelectedItem().toString());
				OrderSim osim = new OrderSim(rndPath);
				System.out.println("Rnd OrderSim created.");
				osim.setIsLong(rbLong.isSelected());
				osim.setDateRange(tfRndSDate.getText(), tfRndEDate.getText());
				osim.setBIM(Double.parseDouble(tfRndBim.getText()));
				osim.setSOM(Double.parseDouble(tfRndSom.getText()));
				osim.setTtvMask("111");
				osim.calcOrderList();
				//show results
				lbRndSPD.setText("SPD    : "+String.valueOf(osim.getOrderListSPD()) + " (" + 
								String.valueOf(osim.getOrderListSize()) + " total)");
				lbRndPosPer.setText("Pos %  : " + String.format("%.4f", osim.getPosPer()));
				lbRndTrigPer.setText("Trig % : " + String.format("%.4f", osim.getTrigAppr()));
				lbRndSecPer.setText("Sec %  : " + String.format("%.4f", osim.getSecAppr()));
				lbRndYoyPer.setText("YoY %  : " + String.format("%.4f", osim.getYoyAppr()));
				//Preserve data for graphing
				ArrayList<ArrayList<String>> growth = osim.calcGrowth(100000.0);
				ArrayList<String> growthHeader = new ArrayList<String>();
				growthHeader.add("date");
				growthHeader.add("growth");
				growth.add(0, growthHeader);
				AhrIO.writeToFile("./../data/r/rdata/pa_portgrowth.csv", AhrDTF.melt(growth, "date"), ",");
				//resume GUI
				setCursor(null);
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
				System.out.println("==> Plot Idx = " + idx);
				if(idx == 0){//appr distn: b&w and cumlative
					String plotPathBaw = "./../resources/pa_distn_baw.png";
					String plotPathCdf = "./../resources/pa_distn_cdf.png";
					String titleBaw = "B&W for All Appr %s for RND Method";
					String titleCdf = "CDF for All Appr %s for RND Method";
					xdim = 600;
					ydim = 300;
					//get all trig %s from orderlist, create Box & Whisker
					String olPath = "./../data/orderlist/orderlist.txt";
					FCI fciOL = new FCI(false, olPath);
					ArrayList<String> trigStr = AhrIO.scanCol(olPath, ",", fciOL.getIdx("method_appr"));
					ArrayList<Double> trig = new ArrayList<Double>();
					for(int i = 0; i < trigStr.size(); i++){
						trig.add(Double.parseDouble(trigStr.get(i)));
					} 
					RCode rcBaw = new RCode();
					rcBaw.setTitle(titleBaw);
					rcBaw.limY(-10, 30);
					rcBaw.flipCoords();
					rcBaw.createBAW(trig, plotPathBaw, xdim, ydim);
					rcBaw.printCode();
					rcBaw.writeCode("./../data/r/rscripts/pa_distn_baw.R");
					rcBaw.runScript("./../data/r/rscripts/pa_distn_baw.R");
					//calc CDF plot
					RCode rcCDF = new RCode();
					rcCDF.setTitle(titleCdf);
					rcCDF.limX(-10, 30);
					rcCDF.createCDF(trig, plotPathCdf, xdim, ydim);
					rcCDF.printCode();
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
					String olPath = "./../data/orderlist/orderlist.txt";
					FCI fciOL = new FCI(false, olPath);
					ArrayList<String> trigCodes = AhrIO.scanCol(olPath, ",", fciOL.getIdx("trigger_code"));
					ArrayList<ArrayList<String>> pieAL = AhrAL.countUniq(trigCodes);
					RCode rcPie = new RCode();
					rcPie.setTitle("Trigger Codes for RND Method");
					rcPie.setXLabel("");
					rcPie.setYLabel("");
					rcPie.createPie(pieAL, plotPath, xdim, ydim);
					rcPie.printCode();
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
					String mosStr = tfRndMos.getText();
					//other plot attrs
					String plotTitle = "Portfolio Growth ($)  [BIM = "+bimStr+", SOM = "+somStr+", MOS = "+mosStr+"]";

					//create growth graph
					RCode rcode = new RCode();
					rcode.setXLabel("Date");
					rcode.setYLabel("Porfolio Value ($)");
					rcode.setTitle(plotTitle);
					rcode.createTimeSeries(dataPath, plotPath, xdim, ydim);
					rcode.printCode();
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
		pRndInputs.add(lbSampleSize);
		pRndInputs.add(tfSampleSize);
		pRndInputs.add(lbTVI);
		pRndInputs.add(cbTVI);
		pRndInputs.add(lbRndSDate);
		pRndInputs.add(tfRndSDate);
		pRndInputs.add(lbRndEDate);
		pRndInputs.add(tfRndEDate);
		pRndInputs.add(lbRndBim);
		pRndInputs.add(tfRndBim);
		pRndInputs.add(lbRndSom);
		pRndInputs.add(tfRndSom);
		pRndInputs.add(lbFilter);
		pRndInputs.add(cbFilter);
		pRndInputs.add(bPrintFilter);
		pRndInputs.add(bCreateNewFilter);
		pRndInputs.add(lbRndMos);
		pRndInputs.add(tfRndMos);
		pRndOutputs.add(lbRndSPD);
		pRndOutputs.add(lbRndPosPer);
		pRndOutputs.add(lbRndTrigPer);
		pRndOutputs.add(lbRndSecPer);
		pRndOutputs.add(lbRndYoyPer);
		pRndOutputs.add(lbRndPlots);
		pRndOutputs.add(cbRndPlots);
		pRndOutputs.add(bRndPlots);

		//add everything together
		pBGM.add(pBgmInputs);
		pBGM.add(bBgmCalcPerf);
		pBGM.add(pBgmOutputs);
		pRND.add(pRndInputs);
		pRND.add(bRndCalcPerf);
		pRND.add(pRndOutputs);
		tpKeyPerf.add("BGM Perf", pBGM);
		tpKeyPerf.add("RND Perf", pRND);
		this.add(tpKeyPerf);
		this.setVisible(true);
	}

	//gets list of all possible keys given BGM and SK or AK
	public ArrayList<String> getKeyNumList(boolean is_sk, String bgm){
		String bgmLC = bgm.toLowerCase();
		ArrayList<String> nums = new ArrayList<String>();
		String fpath = "";
		if(is_sk){
			fpath = "./../out/ml/"+bgmLC+"/keys_struct.txt";
			FCI fciKS = new FCI(true, fpath);
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile(fpath, ",");
			if(fc.size() > 1){
				for(int i = 1; i < fc.size(); i++){
					nums.add(fc.get(i).get(fciKS.getIdx("key_num")));
				}
			}
		}else{
			fpath = "./../baseis/log/ak_log.txt";
			FCI fciLA = new FCI(true, fpath);
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile(fpath, ",");
			if(fc.size() > 1){
				for(int i = 1; i < fc.size(); i++){
					if(!fc.get(i).get(fciLA.getIdx("bgm")).equals("RND")){
						nums.add(fc.get(i).get(fciLA.getIdx("basis_num")));
					}
				}
			}
		}
		return nums;
	}

	//creates GUI to create new filter to be used in RND panel
	public String createNewFilter(){
		//lists and overarching structs
		String[] indicatorList = {"S/M 20", "S/M 10", "S/M 5", "S/M 2", "S/I 20", "S/I 10", "S/I 5", "S/I 2", "SMA 20",
								"SMA 10", "SMA 5", "SMA 2", "RSI", "MACD", "MACD Histogram", "CMF", "Bollinger Bandwidth",
								"%B", "ROC", "MFI", "CCI", "Mass Index", "TSI", "Ult Osc"};
		String filterName = "";
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
		//JFrame frame = new JFrame();
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setTitle("Create Custom Filter");
		//frame.setSize(560, 620);
		//frame.setLayout(null);
		JDialog dialog = new JDialog();
		dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.setTitle("Create Custom Filter");
		dialog.setSize(560, 620);
		dialog.setLayout(null);
		JPanel pBasics = new JPanel();
		pBasics.setLayout(null);
		pBasics.setBorder(BorderFactory.createTitledBorder("Basic Filter Params"));
		
		//components
		JLabel lbMC = new JLabel("Market Cap:");
		JTextField tfStartMC = new JTextField("300");
		JLabel lbMil1 = new JLabel("mil  to");
		JTextField tfEndMC = new JTextField("50000");
		JLabel lbMil2 = new JLabel("mil"); 
		JLabel lbIndustry = new JLabel("Industry:");
		JTextField tfIndustry= new JTextField("01,02,03,04,05,06,07,08,09,10,11,12");
		Button bIndustryList = new Button("List");
		JLabel lbSector = new JLabel("Sector:");
		JTextArea taSector = new JTextArea(2, 30);
		Button bSectorAll = new Button("All");
		Button bSectorList = new Button("List");
		Button bApply = new Button("Apply Inputs");		
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
		Button bReset = new Button("Reset Inputs");
		Button bToFile = new Button("Save And Close");

		
		//component bounds
		pBasics.setBounds(10, 10, 540, 175);
		lbMC.setBounds(10, 20, 110, 25);
		tfStartMC.setBounds(110, 20, 100, 25);
		lbMil1.setBounds(220, 20, 60, 25);
		tfEndMC.setBounds(280, 20, 100, 25);
		lbMil2.setBounds(390, 20, 40, 25);
		lbIndustry.setBounds(10, 55, 110, 25);
		tfIndustry.setBounds(110, 55, 270, 25);
		bIndustryList.setBounds(390, 55, 50, 30);
		lbSector.setBounds(10, 90, 110, 25);
		taSector.setBounds(110, 90, 270, 30);
		bSectorList.setBounds(390, 90, 50, 30);
		bSectorAll.setBounds(450, 90, 50, 30);
		bApply.setBounds(205, 125, 150, 40);
		lbIndicator.setBounds(20, 200, 110, 25);
		cbIndicator.setBounds(120, 200, 270, 25);
		lbIndRangeStart.setBounds(60, 235, 55, 25);
		tfIndRangeStart.setBounds(120, 235, 90, 25);
		lbIndRangeEnd.setBounds(250, 235, 55, 25);
		tfIndRangeEnd.setBounds(300, 235, 90, 25);
		bIndAdd.setBounds(400, 235, 50, 30);
		lbFilterDetails.setBounds(20, 270, 120, 25);
		taFilterDetails.setBounds(60, 305, 450, 200);
		spFilterDetails.setBounds(60, 305, 450, 200);
		bReset.setBounds(70, 515, 175, 40);
		bToFile.setBounds(315, 515, 175, 40);

		//basic functionality
		taSector.setLineWrap(true);
		taSector.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		for(int i = 0; i < indicatorList.length; i++){
			cbIndicator.addItem(indicatorList[i]);
		}
		taFilterDetails.setLineWrap(true);
		taFilterDetails.setFont(monoFont);
	
		//init starting filter lines
		sf.setMarketCap(Integer.parseInt(tfStartMC.getText()), Integer.parseInt(tfEndMC.getText()));
		sf.setSectors(taSector.getText());
		sf.setIndustries(tfIndustry.getText());
		taFilterDetails.setText(sf.getText());

		//button functionality
		bIndustryList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ArrayList<ArrayList<String>> secCodes = AhrIO.scanFile("./../in/sector_codes.txt", "~");
				FCI fciSC = new FCI(false, "./../in/sector_codes.txt");
				ArrayList<String> uniqCodes = new ArrayList<String>();
				ArrayList<String> uniqInds = new ArrayList<String>();
				for(int i = 0; i < secCodes.size(); i++){
					double itrCode = Double.parseDouble(secCodes.get(i).get(fciSC.getIdx("code")));
					if(itrCode%100 == 1){
						if(!uniqCodes.contains(itrCode)){
							uniqCodes.add(String.valueOf(itrCode));
							uniqInds.add(secCodes.get(i).get(fciSC.getIdx("industry")));
						}
					}
				}
				//print out
				for(int i = 0; i < uniqInds.size(); i++){
					System.out.println((i+1)+ " - " + uniqInds.get(i));
				}
			}
		});
		bSectorList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				boolean is_good_text = false;
				String[] indCodes = {""};
				if(!tfIndustry.getText().equals("")){
					indCodes = tfIndustry.getText().split(",");
					if(indCodes.length == 1){
						is_good_text = true;
					}
				}
				if(is_good_text){
					String indStr = tfIndustry.getText();
					int indCode = Integer.parseInt(indStr) * 100;
					ArrayList<ArrayList<String>> secCodes = AhrIO.scanFile("./../in/sector_codes.txt", "~");
					FCI fciSC = new FCI(false, "./../in/sector_codes.txt");
					ArrayList<String> uniqSecs = new ArrayList<String>();
					for(int i = 0; i < secCodes.size(); i++){
						int itrCode = Integer.parseInt(secCodes.get(i).get(fciSC.getIdx("code")));
						if((itrCode-indCode) > 0 && (itrCode-indCode) < 100){
							uniqSecs.add(secCodes.get(i).get(fciSC.getIdx("sector")));
						}
					}
					//print out
					System.out.println("========== Sectors Within "+indStr+" ==========");
					for(int i = 0; i < uniqSecs.size(); i++){
						System.out.println((i+1) + " - " + uniqSecs.get(i));
					}
				}else{
					System.out.println("ERR: only 1 industry must be selected for this filter to work.");
				}
			}	
		});
		bSectorAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				boolean is_good_text = false;
				String[] indCodes = {""};
				if(!tfIndustry.getText().equals("")){
					indCodes = tfIndustry.getText().split(",");
					if(indCodes.length == 1){
						is_good_text = true;
					}
				}
				if(is_good_text){
					int nearest100th = Integer.parseInt(tfIndustry.getText()) * 100;
					ArrayList<ArrayList<String>> secCodes = AhrIO.scanFile("./../in/sector_codes.txt", "~");
					FCI fciSC = new FCI(false, "./../in/sector_codes.txt");				
					ArrayList<String> subcodes = new ArrayList<String>();
					for(int i = 0; i < secCodes.size(); i++){
						int itrCode = Integer.parseInt(secCodes.get(i).get(fciSC.getIdx("code")));
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
					taSector.setText(strSubcodes);
					dialog.setVisible(true);
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
				String rawIndStr = tfIndustry.getText();
				String rawSecStr = taSector.getText();
				boolean good_vals = true;
				if(!mcStart.matches("[0-9]+") || !mcEnd.matches("[0-9]+")){
					System.out.println("ERR: market cap values must be integers.");
					good_vals = false;
				}
				if(!rawIndStr.replace(",","").matches("[0-9]+")){
					System.out.println("ERR: industry values must be comma seperated integers");
					good_vals = false;
				} 
				if(!rawSecStr.replace(",","").matches("[0-9]+") && !rawSecStr.equals("")){
					System.out.println("ERR: sector values must be comma seperated integers.");
					good_vals = false;
				}
				if(good_vals){
					sf.setMarketCap(Integer.parseInt(mcStart), Integer.parseInt(mcEnd));
					sf.setSectors(rawSecStr);
					sf.setIndustries(rawIndStr);
					taFilterDetails.setText(sf.getText());
					dialog.setVisible(true);
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
				dialog.dispose();
			}
		});


		pBasics.add(lbMC);
		pBasics.add(tfStartMC);
		pBasics.add(lbMil1);
		pBasics.add(tfEndMC);
		pBasics.add(lbMil2);
		pBasics.add(lbIndustry);
		pBasics.add(tfIndustry);
		pBasics.add(bIndustryList);
		pBasics.add(lbSector);
		pBasics.add(taSector);
		pBasics.add(bSectorList);
		pBasics.add(bSectorAll);
		pBasics.add(bApply);
		dialog.add(pBasics);
		dialog.add(lbIndicator);
		dialog.add(cbIndicator);
		dialog.add(lbIndRangeStart);
		dialog.add(tfIndRangeStart);
		dialog.add(lbIndRangeEnd);
		dialog.add(tfIndRangeEnd);
		dialog.add(bIndAdd);
		dialog.add(lbFilterDetails);
		dialog.add(spFilterDetails);
		dialog.add(bReset);
		dialog.add(bToFile);
		dialog.setVisible(true);

		return filterName;
	}

	//generate random basis fiels based on GUI input params
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
					System.out.println("--> Rnd Basis File progress: "+i+" out of "+dates.size());
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
				System.out.println("==> In applyFilter("+dates.get(i)+")");
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
						System.out.println("--> uniqTicker size = " + uniqTickers.size());
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
