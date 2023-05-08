package ahrweiler.gui;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrAL;
import ahrweiler.support.FCI;
import ahrweiler.support.OrderSim;
import ahrweiler.support.RCode;
import ahrweiler.bgm.BGM_Manager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.table.DefaultTableCellRenderer;

public class PA_BimSomOpt {

	final Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	public PA_BimSomOpt(){
		drawGUI();
	}

	public void drawGUI(){
		//lists and over-arching data
		int fxDim = 500;
		int fyDim = 620;
		String[] bgmList = {"ANN"};
		Font monoFont = new Font(Font.MONOSPACED, Font.BOLD, 11);
		String[] baseOut1 = {"Trades Positive : ", "BIM Triggered   : ", "SOM Triggered   : ", "Avg Trade Len   : ", 
								"APY Appr        : "};
		String[] baseOut2 = {"Best BIM : ", "Best SOM : ", "Best APY : ",
							 "Worst BIM: ", "Worst SOM: ", "Worst APY: ",
							 "Avg APY  : "};
		String[] outHeader = {"", "% of Tot", "Close %", "Intra %", "Trig %"};
		String[][] outData = {{"All Lines", "0.0", "0.0", "0.0", "0.0"},
							  {"Open Triggered", "0.0", "0.0", "0.0", "0.0"},
							  {"Day Triggered", "0.0", "0.0", "0.0", "0.0"},
							  {"Not Triggered", "0.0", "0.0", "0.0", "0.0"}}; 
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		//layout components
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("BIM / SOM Optimization");
		frame.setSize(fxDim, fyDim);
		frame.setLayout(null);
		JTabbedPane tpBSO = new JTabbedPane();
		tpBSO.setBounds(0, 0, fxDim, fyDim-37);
		JPanel pSingle = new JPanel();
		pSingle.setLayout(null);
		JPanel pMult = new JPanel();
		pMult.setLayout(null);

		/*---------------------------------------------------
			Single BIM/SOM Panel
		----------------------------------------------------*/
		//layout
		JPanel pSingleInput = new JPanel();
		pSingleInput.setBorder(BorderFactory.createTitledBorder("Inputs"));
		pSingleInput.setBounds(10, 15, fxDim-20, 265);
		pSingleInput.setLayout(null);
		JPanel pSingleOutput = new JPanel();
		pSingleOutput.setBorder(BorderFactory.createTitledBorder("Performance"));
		pSingleOutput.setBounds(10, 330, fxDim-20, 220);
		pSingleOutput.setLayout(null);

		//init components
		JLabel lbKeyType1 = new JLabel("Key Type:");				//inputs panel
		JRadioButton rbSK1 = new JRadioButton("SK");
		JRadioButton rbAK1 = new JRadioButton("AK");
		ButtonGroup bgKeyType1 = new ButtonGroup();
		bgKeyType1.add(rbSK1);
		bgKeyType1.add(rbAK1);
		JLabel lbBgm1 = new JLabel("BGM:");
		JComboBox cbBgm1 = new JComboBox();
		JLabel lbKeyNum1 = new JLabel("Key Num:");
		JComboBox cbKeyNum1 = new JComboBox();
		JLabel lbDatasets1 = new JLabel("Datasets:");
		JCheckBox cbTrain1 = new JCheckBox("Train");
		JCheckBox cbTest1 = new JCheckBox("Test", true);
		JCheckBox cbVerify1 = new JCheckBox("Verify");
		JLabel lbSDate1 = new JLabel("Start Date:");
		JTextField tfSDate1 = new JTextField();
		JLabel lbEDate1 = new JLabel("End Date:");
		JTextField tfEDate1 = new JTextField();
		JButton bDatesAF1 = new JButton("Autofill");
		JLabel lbBim = new JLabel("BIM:");
		JTextField tfBim = new JTextField();
		JLabel lbSom = new JLabel("SOM:");
		JTextField tfSom = new JTextField();
		JButton bBimSomAF = new JButton("Autofill");
		JLabel lbMinTrig1 = new JLabel("Use Min Triggers?");
		JRadioButton rbMinYes1 = new JRadioButton("Yes");
		JRadioButton rbMinNo1 = new JRadioButton("No");
		ButtonGroup bgMinTrig1 = new ButtonGroup();
		bgMinTrig1.add(rbMinYes1);
		bgMinTrig1.add(rbMinNo1); 
		JLabel lbUseSKs1 = new JLabel("Use SK BIM/SOMs?");
		JRadioButton rbUseSKsYes1 = new JRadioButton("Yes");
		JRadioButton rbUseSKsNo1 = new JRadioButton("No");
		ButtonGroup bgUseSKs1 = new ButtonGroup();
		bgUseSKs1.add(rbUseSKsYes1);
		bgUseSKs1.add(rbUseSKsNo1);
		JButton bListSKs1 = new JButton("List");
		JButton bCompute1 = new JButton("Compute");				//compute button
		JLabel lbPosPer = new JLabel(baseOut1[0]);				//output panel
		JLabel lbThruRate = new JLabel(baseOut1[3]);
		JLabel lbTrigBIM = new JLabel(baseOut1[1]);
		JLabel lbTrigSOM = new JLabel(baseOut1[2]);
		JLabel lbYoyAppr = new JLabel(baseOut1[4]);
		JTable tApprs = new JTable(outData, outHeader);
		JScrollPane spApprs = new JScrollPane(tApprs, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
									JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		//bounds
		lbKeyType1.setBounds(10, 20, 80, 25);				//inputs panel
		rbSK1.setBounds(110, 20, 60, 25);
		rbAK1.setBounds(170, 20, 60, 25);
		lbBgm1.setBounds(10, 50, 80, 25);
		cbBgm1.setBounds(110, 50, 80, 25);
		lbKeyNum1.setBounds(10, 80, 80, 25);
		cbKeyNum1.setBounds(110, 80, 80, 25);
		lbDatasets1.setBounds(10, 110, 80, 25);
		cbTrain1.setBounds(110, 110, 75, 25);
		cbTest1.setBounds(185, 110, 75, 25);
		cbVerify1.setBounds(260, 110, 80, 25);
		lbSDate1.setBounds(10, 140, 80, 25);
		tfSDate1.setBounds(110, 140, 80, 25);
		lbEDate1.setBounds(210, 140, 80, 25);
		tfEDate1.setBounds(295, 140, 80, 25);
		bDatesAF1.setBounds(385, 140, 80, 25);
		lbBim.setBounds(10, 170, 80, 25);
		tfBim.setBounds(110, 170, 80, 25);
		lbSom.setBounds(210, 170, 80, 25);
		tfSom.setBounds(295, 170, 80, 25);
		bBimSomAF.setBounds(385, 170, 80, 25);
		lbMinTrig1.setBounds(10, 200, 140, 25);
		rbMinYes1.setBounds(160, 200, 60, 25);
		rbMinNo1.setBounds(230, 200, 60, 25);
		lbUseSKs1.setBounds(10, 230, 140, 25);
		rbUseSKsYes1.setBounds(160, 230, 60, 25);
		rbUseSKsNo1.setBounds(230, 230, 60, 25);
		bListSKs1.setBounds(300, 230, 50, 25);
		bCompute1.setBounds(10, 290, 100, 30);				//compute 
		lbPosPer.setBounds(10, 15, 300, 20);				//output panel
		lbThruRate.setBounds(10, 35, 300, 20);
		lbTrigBIM.setBounds(10, 55, 300, 20);
		lbTrigSOM.setBounds(10, 75, 300, 20);
		lbYoyAppr.setBounds(10, 95, 300, 20);
		tApprs.setBounds(10, 125, 460, 85);
		spApprs.setBounds(10, 125, 460, 85);
		
		
		//basic functionality
		rbSK1.setFont(plainFont);
		rbAK1.setFont(plainFont);
		rbAK1.setSelected(true);
		cbTrain1.setFont(plainFont);
		cbTest1.setFont(plainFont);
		cbVerify1.setFont(plainFont);
		setButtonStyle(bDatesAF1);
		setButtonStyle(bBimSomAF);
		setButtonStyle(bListSKs1);
		setButtonStyle(bCompute1);
		for(int i = 0; i < bgmList.length; i++){
			cbBgm1.addItem(bgmList[i]);
		}
		lbBgm1.setEnabled(false);
		cbBgm1.setEnabled(false);
		ArrayList<String> knList = getKeyNumList(rbSK1.isSelected(), bgmList[0]);
		for(int i = 0; i < knList.size(); i++){
			cbKeyNum1.addItem(knList.get(i));
		}
		rbMinYes1.setSelected(true);
		rbUseSKsNo1.setSelected(true);
		lbPosPer.setFont(monoFont);
		lbTrigBIM.setFont(monoFont);
		lbTrigSOM.setFont(monoFont);
		lbThruRate.setFont(monoFont);
		lbYoyAppr.setFont(monoFont);
		tApprs.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tApprs.getColumnModel().getColumn(0).setPreferredWidth(120);
		tApprs.getColumnModel().getColumn(1).setPreferredWidth(85);
		tApprs.getColumnModel().getColumn(2).setPreferredWidth(85);
		tApprs.getColumnModel().getColumn(3).setPreferredWidth(85);
		tApprs.getColumnModel().getColumn(4).setPreferredWidth(85);
		tApprs.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		tApprs.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tApprs.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		tApprs.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

	
		//listener functionality
		rbSK1.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if(rbSK1.isSelected()){
					lbBgm1.setEnabled(true);
					cbBgm1.setEnabled(true);
					lbUseSKs1.setEnabled(false);
					rbUseSKsYes1.setEnabled(false);
					rbUseSKsNo1.setEnabled(false);
					bListSKs1.setEnabled(false);
				}else{
					lbBgm1.setEnabled(false);
					cbBgm1.setEnabled(false);
					lbUseSKs1.setEnabled(true);
					rbUseSKsYes1.setEnabled(true);
					rbUseSKsNo1.setEnabled(true);
					bListSKs1.setEnabled(true);
				}
				String bgm = cbBgm1.getSelectedItem().toString();
				ArrayList<String> keyNumList = getKeyNumList(rbSK1.isSelected(), bgm);
				cbKeyNum1.removeAllItems();
				for(int i = 0; i < keyNumList.size(); i++){
					cbKeyNum1.addItem(keyNumList.get(i));
				}
			}
		});
		cbBgm1.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				String bgm = cbBgm1.getSelectedItem().toString();
				ArrayList<String> keyNumList = getKeyNumList(rbSK1.isSelected(), bgm);
				cbKeyNum1.removeAllItems();
				for(int i = 0; i < keyNumList.size(); i++){
					cbKeyNum1.addItem(keyNumList.get(i));
				}			
			}
		});
		bDatesAF1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String bgmUC = cbBgm1.getSelectedItem().toString().toUpperCase();
				String bgmLC = cbBgm1.getSelectedItem().toString().toLowerCase();
				String knum = cbKeyNum1.getSelectedItem().toString();
				String bsPath = "";
				if(rbSK1.isSelected()){
					bsPath = "./../out/sk/baseis/"+bgmLC+"/"+bgmUC+"_"+knum+".txt";
				}else{
					bsPath = "./../out/ak/baseis/"+bgmLC+"/"+bgmUC+"_"+knum+".txt";
				}
				FCI fciBS = new FCI(false, bsPath);
				ArrayList<ArrayList<String>> basis = AhrIO.scanFile(bsPath, ",");
				tfSDate1.setText(basis.get(0).get(fciBS.getIdx("date")));
				tfEDate1.setText(basis.get(basis.size()-1).get(fciBS.getIdx("date")));
			}
		});
		bBimSomAF.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String bgmLC = cbBgm1.getSelectedItem().toString().toLowerCase();
				String knum = cbKeyNum1.getSelectedItem().toString();
				String bim = "ph";
				String som = "ph";
				String call = "0";
				if(rbSK1.isSelected()){
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
				tfBim.setText(bim);
				tfSom.setText(som);
			}
		});
		rbUseSKsYes1.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if(rbUseSKsYes1.isSelected()){
					lbBim.setEnabled(false);
					tfBim.setEnabled(false);
					lbSom.setEnabled(false);
					tfSom.setEnabled(false);
					bBimSomAF.setEnabled(false);
				}else{
					lbBim.setEnabled(true);
					tfBim.setEnabled(true);
					lbSom.setEnabled(true);
					tfSom.setEnabled(true);
					bBimSomAF.setEnabled(true);
				}
			}
		});
		bCompute1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//get params from GUI
				boolean is_sk = rbSK1.isSelected();
				String bgm = cbBgm1.getSelectedItem().toString();
				int knum = Integer.parseInt(cbKeyNum1.getSelectedItem().toString());
				String sdate = tfSDate1.getText();
				String edate = tfEDate1.getText();
				double bim = Double.parseDouble(tfBim.getText());
				double som = Double.parseDouble(tfSom.getText());
				String ttvMask = "";
				boolean has_no_datasets = true;
				if(cbTrain1.isSelected()){
					ttvMask += "1";
					has_no_datasets = false;
				}else{
					ttvMask += "0";
				}
				if(cbTest1.isSelected()){
					ttvMask += "1";
					has_no_datasets = false;
				}else{
					ttvMask += "0";
				}
				if(cbVerify1.isSelected()){
					ttvMask += "1";
					has_no_datasets = false;
				}else{
					ttvMask += "0";
				}
				boolean is_min_trig = rbMinYes1.isSelected();
				boolean use_sk_bso = rbUseSKsYes1.isSelected();
				//if > 1 datasets is selected, calc BSO
				if(has_no_datasets){
					JOptionPane.showMessageDialog(frame, "At least one dataset must be selected.", "Error", 
												JOptionPane.ERROR_MESSAGE);
				}else{
					//suspend GUI while working
					bCompute1.setEnabled(false);
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					//calc BIM/SOM Opt data from BGM_Manager
					ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
					if(is_sk){
						BGM_Manager skey = new BGM_Manager(bgm, knum);
						data = skey.bsoSingle(sdate, edate, bim, som, ttvMask, is_min_trig, use_sk_bso);
					}else{
						BGM_Manager akey = new BGM_Manager(knum);
						data = akey.bsoSingle(sdate, edate, bim, som, ttvMask, is_min_trig, use_sk_bso);
					}
					//output results
					double posPercent = Double.parseDouble(data.get(0).get(5)) * 100.0;
					lbPosPer.setText(baseOut1[0] + String.format("%.2f", posPercent) + " %");
					lbThruRate.setText(baseOut1[3] + data.get(0).get(2) + " days");
					lbTrigBIM.setText(baseOut1[1] + data.get(0).get(0) + " %");
					lbTrigSOM.setText(baseOut1[2] + data.get(0).get(1) + " %");
					lbYoyAppr.setText(baseOut1[4] + data.get(0).get(4) + " %");
					for(int i = 0; i < 4; i++){
						for(int j = 0; j < 4; j++){
							tApprs.setValueAt(data.get(i+1).get(j), i, j+1);
						} 
					}
					//resume GUI
					frame.setCursor(null);
					bCompute1.setEnabled(true);
				}
			}
		});

		//add
		pSingleInput.add(lbKeyType1);
		pSingleInput.add(rbSK1);
		pSingleInput.add(rbAK1);
		pSingleInput.add(lbBgm1);
		pSingleInput.add(cbBgm1);
		pSingleInput.add(lbKeyNum1);
		pSingleInput.add(cbKeyNum1);
		pSingleInput.add(lbDatasets1);
		pSingleInput.add(cbTrain1);
		pSingleInput.add(cbTest1);
		pSingleInput.add(cbVerify1);
		pSingleInput.add(lbSDate1);
		pSingleInput.add(tfSDate1);
		pSingleInput.add(lbEDate1);
		pSingleInput.add(tfEDate1);
		pSingleInput.add(bDatesAF1);
		pSingleInput.add(lbBim);
		pSingleInput.add(tfBim);
		pSingleInput.add(lbSom);
		pSingleInput.add(tfSom);
		pSingleInput.add(bBimSomAF);
		pSingleInput.add(lbMinTrig1);
		pSingleInput.add(rbMinYes1);
		pSingleInput.add(rbMinNo1);
		pSingleInput.add(lbUseSKs1);
		pSingleInput.add(rbUseSKsYes1);
		pSingleInput.add(rbUseSKsNo1);
		pSingleOutput.add(lbPosPer);
		pSingleOutput.add(lbTrigBIM);
		pSingleOutput.add(lbTrigSOM);
		pSingleOutput.add(lbThruRate);
		pSingleOutput.add(lbYoyAppr);
		pSingleOutput.add(spApprs);
		pSingle.add(pSingleInput);
		pSingle.add(bCompute1);
		pSingle.add(pSingleOutput);

		/*---------------------------------------------------
			Multiple BIM/SOM Panel
		----------------------------------------------------*/
		String[] multPlotList = {"Heatmap", "B&W / CDF Distn"};

		//layout
		JPanel pMultInput = new JPanel();
		pMultInput.setBorder(BorderFactory.createTitledBorder("Inputs"));
		pMultInput.setBounds(10, 15, fxDim-20, 235);
		pMultInput.setLayout(null);
		JPanel pMultOutput = new JPanel();
		pMultOutput.setBorder(BorderFactory.createTitledBorder("Performance"));
		pMultOutput.setBounds(10, 300, fxDim-20, 220);
		pMultOutput.setLayout(null);

		//init components
		JLabel lbKeyType2 = new JLabel("Key Type:");				//inputs panel
		JRadioButton rbSK2 = new JRadioButton("SK");
		JRadioButton rbAK2 = new JRadioButton("AK");
		ButtonGroup bgKeyType2 = new ButtonGroup();
		bgKeyType2.add(rbSK2);
		bgKeyType2.add(rbAK2);
		JLabel lbBgm2 = new JLabel("BGM:");
		JComboBox cbBgm2 = new JComboBox();
		JLabel lbKeyNum2 = new JLabel("Key Num:");
		JComboBox cbKeyNum2 = new JComboBox();
		JLabel lbDatasets2 = new JLabel("Datasets:");
		JCheckBox cbTrain2 = new JCheckBox("Train");
		JCheckBox cbTest2 = new JCheckBox("Test", true);
		JCheckBox cbVerify2 = new JCheckBox("Verify");
		JLabel lbSDate2 = new JLabel("Start Date:");
		JTextField tfSDate2 = new JTextField();
		JLabel lbEDate2 = new JLabel("End Date:");
		JTextField tfEDate2 = new JTextField();
		JButton bDatesAF2 = new JButton("Autofill");
		JLabel lbMinTrig2 = new JLabel("Use Min Triggers?");
		JRadioButton rbMinYes2 = new JRadioButton("Yes");
		JRadioButton rbMinNo2 = new JRadioButton("No");
		ButtonGroup bgMinTrig2 = new ButtonGroup();
		bgMinTrig2.add(rbMinYes2);
		bgMinTrig2.add(rbMinNo2); 
		JLabel lbUseSKs2 = new JLabel("Use SK BIM/SOMs?");
		JRadioButton rbUseSKsYes2 = new JRadioButton("Yes");
		JRadioButton rbUseSKsNo2 = new JRadioButton("No");
		ButtonGroup bgUseSKs2 = new ButtonGroup();
		bgUseSKs2.add(rbUseSKsYes2);
		bgUseSKs2.add(rbUseSKsNo2);
		JButton bListSKs2 = new JButton("List");
		JButton bCompute2 = new JButton("Compute");				//compute button
		JLabel lbBestBim = new JLabel(baseOut2[0]);				//output panel
		JLabel lbBestSom = new JLabel(baseOut2[1]);
		JLabel lbBestYoy = new JLabel(baseOut2[2]);
		JLabel lbWorstBim = new JLabel(baseOut2[3]);
		JLabel lbWorstSom = new JLabel(baseOut2[4]);
		JLabel lbWorstYoy = new JLabel(baseOut2[5]);
		JLabel lbAvgYoy = new JLabel(baseOut2[6]);
		JLabel lbDistn = new JLabel("Distribution:");
		JComboBox cbDistnPlot = new JComboBox();
		JButton bShowPlot = new JButton("Show Plot");

		//bounds
		lbKeyType2.setBounds(10, 20, 80, 25);				//inputs panel
		rbSK2.setBounds(110, 20, 60, 25);
		rbAK2.setBounds(170, 20, 60, 25);
		lbBgm2.setBounds(10, 50, 80, 25);
		cbBgm2.setBounds(110, 50, 80, 25);
		lbKeyNum2.setBounds(10, 80, 80, 25);
		cbKeyNum2.setBounds(110, 80, 80, 25);
		lbDatasets2.setBounds(10, 110, 80, 25);
		cbTrain2.setBounds(110, 110, 75, 25);
		cbTest2.setBounds(185, 110, 75, 25);
		cbVerify2.setBounds(260, 110, 80, 25);
		lbSDate2.setBounds(10, 140, 80, 25);
		tfSDate2.setBounds(110, 140, 80, 25);
		lbEDate2.setBounds(210, 140, 80, 25);
		tfEDate2.setBounds(295, 140, 80, 25);
		bDatesAF2.setBounds(385, 140, 80, 25);
		lbMinTrig2.setBounds(10, 170, 140, 25);
		rbMinYes2.setBounds(160, 170, 60, 25);
		rbMinNo2.setBounds(230, 170, 60, 25);
		lbUseSKs2.setBounds(10, 200, 140, 25);
		rbUseSKsYes2.setBounds(160, 200, 60, 25);
		rbUseSKsNo2.setBounds(230, 200, 60, 25);
		bListSKs2.setBounds(300, 200, 50, 25);
		bCompute2.setBounds(10, 260, 100, 30);				//compute 
		lbBestBim.setBounds(10, 20, 140, 15);				//output panel
		lbBestSom.setBounds(10, 40, 140, 15);
		lbBestYoy.setBounds(10, 60, 140, 15);
		lbWorstBim.setBounds(220, 20, 140, 15);
		lbWorstSom.setBounds(220, 40, 140, 15);
		lbWorstYoy.setBounds(220, 60, 140, 15);
		lbAvgYoy.setBounds(10, 80, 140, 15);
		lbDistn.setBounds(10, 110, 110, 25);
		cbDistnPlot.setBounds(115, 110, 140, 25);
		bShowPlot.setBounds(265, 110, 80, 25);

		//basic functionality
		rbSK2.setFont(plainFont);
		rbAK2.setFont(plainFont);
		rbAK2.setSelected(true);
		cbTrain2.setFont(plainFont);
		cbTest2.setFont(plainFont);
		cbVerify2.setFont(plainFont);
		setButtonStyle(bDatesAF2);
		setButtonStyle(bListSKs2);
		setButtonStyle(bCompute2);
		setButtonStyle(bShowPlot);
		for(int i = 0; i < bgmList.length; i++){
			cbBgm2.addItem(bgmList[i]);
		}
		lbBgm2.setEnabled(false);
		cbBgm2.setEnabled(false);
		ArrayList<String> knList2 = getKeyNumList(rbSK2.isSelected(), bgmList[0]);
		for(int i = 0; i < knList2.size(); i++){
			cbKeyNum2.addItem(knList2.get(i));
		}
		rbMinYes2.setSelected(true);
		rbUseSKsNo2.setSelected(true);
		lbBestBim.setFont(monoFont);
		lbBestSom.setFont(monoFont);
		lbBestYoy.setFont(monoFont);
		lbWorstBim.setFont(monoFont);
		lbWorstSom.setFont(monoFont);
		lbWorstYoy.setFont(monoFont);
		lbAvgYoy.setFont(monoFont);
		for(int i = 0; i < multPlotList.length; i++){
			cbDistnPlot.addItem(multPlotList[i]);
		}

		//listener functionality
		rbSK2.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if(rbSK2.isSelected()){
					lbBgm2.setEnabled(true);
					cbBgm2.setEnabled(true);
					lbUseSKs2.setEnabled(false);
					rbUseSKsYes2.setEnabled(false);
					rbUseSKsNo2.setEnabled(false);
					bListSKs2.setEnabled(false);
				}else{
					lbBgm2.setEnabled(false);
					cbBgm2.setEnabled(false);
					lbUseSKs2.setEnabled(true);
					rbUseSKsYes2.setEnabled(true);
					rbUseSKsNo2.setEnabled(true);
					bListSKs2.setEnabled(true);
				}
				String bgm = cbBgm2.getSelectedItem().toString();
				ArrayList<String> keyNumList = getKeyNumList(rbSK2.isSelected(), bgm);
				cbKeyNum2.removeAllItems();
				for(int i = 0; i < keyNumList.size(); i++){
					cbKeyNum2.addItem(keyNumList.get(i));
				}
			}
		});
		cbBgm2.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				String bgm = cbBgm2.getSelectedItem().toString();
				ArrayList<String> keyNumList = getKeyNumList(rbSK2.isSelected(), bgm);
				cbKeyNum2.removeAllItems();
				for(int i = 0; i < keyNumList.size(); i++){
					cbKeyNum2.addItem(keyNumList.get(i));
				}			
			}
		});
		bDatesAF2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String bgmUC = cbBgm2.getSelectedItem().toString().toUpperCase();
				String bgmLC = cbBgm2.getSelectedItem().toString().toLowerCase();
				String knum = cbKeyNum2.getSelectedItem().toString();
				String bsPath = "";
				if(rbSK2.isSelected()){
					bsPath = "./../out/sk/baseis/"+bgmLC+"/"+bgmUC+"_"+knum+".txt";
				}else{
					bsPath = "./../out/ak/baseis/"+bgmLC+"/"+bgmUC+"_"+knum+".txt";
				}
				FCI fciBS = new FCI(false, bsPath);
				ArrayList<ArrayList<String>> basis = AhrIO.scanFile(bsPath, ",");
				tfSDate2.setText(basis.get(0).get(fciBS.getIdx("date")));
				tfEDate2.setText(basis.get(basis.size()-1).get(fciBS.getIdx("date")));
			}
		});
		bCompute2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//get params from GUI
				boolean is_sk = rbSK2.isSelected();
				String bgmUC = cbBgm2.getSelectedItem().toString();
				String bgmLC = bgmUC.toLowerCase();
				int knum = Integer.parseInt(cbKeyNum2.getSelectedItem().toString());
				String sdate = tfSDate2.getText();
				String edate = tfEDate2.getText();
				String ttvMask = "";
				boolean has_no_datasets = true;
				if(cbTrain2.isSelected()){
					ttvMask += "1";
					has_no_datasets = false;
				}else{
					ttvMask += "0";
				}
				if(cbTest2.isSelected()){
					ttvMask += "1";
					has_no_datasets = false;
				}else{
					ttvMask += "0";
				}
				if(cbVerify2.isSelected()){
					ttvMask += "1";
					has_no_datasets = false;
				}else{
					ttvMask += "0";
				}
				boolean is_min_trig = rbMinYes2.isSelected();
				boolean use_sk_bso = rbUseSKsYes2.isSelected();
				//if > 1 datasets is selected, calc BSO
				if(has_no_datasets){
					JOptionPane.showMessageDialog(frame, "At least one dataset must be selected.", "Error", 
												JOptionPane.ERROR_MESSAGE);
				}else{
					//suspend GUI while working
					bCompute2.setEnabled(false);
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					//calc BSO
					ArrayList<String> data = new ArrayList<String>();
					if(is_sk){
						BGM_Manager skey = new BGM_Manager(bgmUC, knum);
						data = skey.bsoMultiple(sdate, edate, ttvMask, is_min_trig, use_sk_bso);
					}else{
						BGM_Manager akey = new BGM_Manager(knum);
						data = akey.bsoMultiple(sdate, edate, ttvMask, is_min_trig, use_sk_bso);
					}
					//get full data from /data/bso
					ArrayList<ArrayList<String>> fc = AhrIO.scanFile("./../data/tmp/bso_multiple.txt", ",");
					int worstIdx = -1;
					double worstYoy = Double.MAX_VALUE;
					double avgYoy = 0.0;
					double medYoy = 0.0;
					for(int i = 0; i < fc.size(); i++){
						double itrYoy = Double.parseDouble(fc.get(i).get(3));
						avgYoy += itrYoy;
						if(itrYoy < worstYoy){
							worstYoy = itrYoy;
							worstIdx = i;
						}
					}
					avgYoy = avgYoy / (double)fc.size();
					//write to output panel
					lbBestBim.setText(baseOut2[0] + data.get(0));
					lbBestSom.setText(baseOut2[1] + data.get(1));
					lbBestYoy.setText(baseOut2[2] + data.get(3));
					lbWorstBim.setText(baseOut2[3] + fc.get(worstIdx).get(0));
					lbWorstSom.setText(baseOut2[4] + fc.get(worstIdx).get(1));
					lbWorstYoy.setText(baseOut2[5] + fc.get(worstIdx).get(3));
					lbAvgYoy.setText(baseOut2[6] + String.format("%.3f", avgYoy));
					//resume GUI
					frame.setCursor(null);
					bCompute2.setEnabled(true);
				}
			}
		});
		bShowPlot.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int xdim = 500;
				int ydim = 500;
				String bgmUC = cbBgm2.getSelectedItem().toString();
				String bgmLC = bgmUC.toLowerCase();
				String knum = cbKeyNum2.getSelectedItem().toString();
				String kmonik = "";
				if(rbSK2.isSelected()){
					kmonik += bgmUC + " SK"+knum;
				}else if(rbAK2.isSelected()){
					kmonik += bgmUC + " AK"+knum;
				}else{
					kmonik += "RND";
				}
				int plotIdx = cbDistnPlot.getSelectedIndex();
				if(plotIdx == 0){
					String plotPath = "./../resources/pa_bso_heat.png";
					//get BSO Mult data and write to R csv file
					FCI fciBM = new FCI(false, "./../data/tmp/bso_multiple.txt");
					ArrayList<ArrayList<String>> fc = AhrIO.scanFile("./../data/tmp/bso_multiple.txt", ",");
					ArrayList<ArrayList<String>> rfile = new ArrayList<ArrayList<String>>();
					ArrayList<String> header = AhrAL.toAL(new String[]{"xvals", "yvals", "data"});
					rfile.add(header);
					for(int i = 0; i < fc.size(); i++){
						ArrayList<String> line = new ArrayList<String>();
						line.add(fc.get(i).get(fciBM.getIdx("bim")));
						line.add(fc.get(i).get(fciBM.getIdx("som")));
						line.add(fc.get(i).get(fciBM.getIdx("yoy")));
						rfile.add(line);
					}
					AhrIO.writeToFile("./../data/r/rdata/pa_bso_heat.csv", rfile, ",");
					//create heatmap plot
					RCode rcHeat = new RCode();
					rcHeat.setXLabel("Buy-In Multiple");
					rcHeat.setYLabel("Sell-Out Multiple");
					rcHeat.setTitle("Heatmap of "+kmonik+" YoY %s For Each BIM/SOM Combination");
					rcHeat.createHeatmap("./../data/r/rdata/pa_bso_heat.csv", "./../resources/pa_bso_heat.png", xdim, ydim);
					rcHeat.printCode();
					rcHeat.writeCode("./../data/r/rscripts/pa_bso_heat.R");
					rcHeat.runScript("./../data/r/rscripts/pa_bso_heat.R");
					//show plot on new popout frame
					JFrame rframe = new JFrame();
					rframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					rframe.setTitle("BIM/SOM Opt Heatmap");
					JLabel lbPlot = new JLabel();
					lbPlot.setPreferredSize(new Dimension(xdim, ydim));
					ImageIcon ii = new ImageIcon(plotPath);
					lbPlot.setIcon(ii);
					rframe.getContentPane().add(lbPlot, BorderLayout.CENTER);
					rframe.pack();
					rframe.setVisible(true);
					ii.getImage().flush();
				}else{
					System.out.println("ERR: unknown plot index.");
				}
			}
		});

		//add
		pMultInput.add(lbKeyType2);
		pMultInput.add(rbSK2);
		pMultInput.add(rbAK2);
		pMultInput.add(lbBgm2);
		pMultInput.add(cbBgm2);
		pMultInput.add(lbKeyNum2);
		pMultInput.add(cbKeyNum2);
		pMultInput.add(lbDatasets2);
		pMultInput.add(cbTrain2);
		pMultInput.add(cbTest2);
		pMultInput.add(cbVerify2);
		pMultInput.add(lbSDate2);
		pMultInput.add(tfSDate2);
		pMultInput.add(lbEDate2);
		pMultInput.add(tfEDate2);
		pMultInput.add(bDatesAF2);
		pMultInput.add(lbMinTrig2);
		pMultInput.add(rbMinYes2);
		pMultInput.add(rbMinNo2);
		pMultInput.add(lbUseSKs2);
		pMultInput.add(rbUseSKsYes2);
		pMultInput.add(rbUseSKsNo2);
		pMultOutput.add(lbBestBim);
		pMultOutput.add(lbBestSom);
		pMultOutput.add(lbBestYoy);
		pMultOutput.add(lbWorstBim);
		pMultOutput.add(lbWorstSom);
		pMultOutput.add(lbWorstYoy);
		pMultOutput.add(lbAvgYoy);
		pMultOutput.add(lbDistn);
		pMultOutput.add(cbDistnPlot);
		pMultOutput.add(bShowPlot);
		pMult.add(pMultInput);
		pMult.add(bCompute2);
		pMult.add(pMultOutput);

		//add to tabbed panels and show visible
		tpBSO.add("Single", pSingle);
		tpBSO.add("Multiple", pMult);
		frame.add(tpBSO);
		frame.setVisible(true);
	}
	//GUI related, sets style to a JButton
	public void setButtonStyle(JButton btn){
		Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}

	//gets list of all possible keys given BGM and SK or AK
	public ArrayList<String> getKeyNumList(boolean is_sk, String bgm){
		ArrayList<String> nums = new ArrayList<String>();
		String fpath = "";
		if(is_sk){
			fpath = "./../out/sk/log/"+bgm.toLowerCase()+"/keys_struct.txt";
			FCI fciKS = new FCI(true, fpath);
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile(fpath, ",");
			for(int i = 1; i < fc.size(); i++){
				nums.add(fc.get(i).get(fciKS.getIdx("sk_num")));
			}
		}else{
			fpath = "./../out/ak/log/ak_log.txt";
			FCI fciLA = new FCI(true, fpath);
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile(fpath, ",");
			for(int i = 1; i < fc.size(); i++){
				if(!fc.get(i).get(fciLA.getIdx("bgm")).equals("RND")){
					nums.add(fc.get(i).get(fciLA.getIdx("ak_num")));
				}
			}
		}
		return nums;
	}

}
