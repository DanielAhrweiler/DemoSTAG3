package ahrweiler.gui;
import ahrweiler.Globals;
import ahrweiler.util.*;
import ahrweiler.support.FCI;
import ahrweiler.bgm.*;
import ahrweiler.bgm.ann.Network;
import ahrweiler.bgm.ann.Node;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ML_CreateAK {

	final Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	boolean coverage_is_100 = false;

	public ML_CreateAK(){
		drawGUI();
	}

	public void drawGUI(){
		//lists and over-arching structs
		int fxDim = 500;
		int fyDim = 650;
		String[] bgmList = {"ANN"};
		String[] methodList = {"Binomial", "Continuous"};
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		//layout components
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("Basis Generating Methods: Aggregate Keys");
		frame.setSize(fxDim, fyDim+37);
		frame.setLayout(null);
		JPanel pInputs = new JPanel();
		pInputs.setBounds(10, 10, fxDim-20, 270);
		pInputs.setBorder(BorderFactory.createTitledBorder("Input Parameters"));
		pInputs.setLayout(null);
		JPanel pKeys = new JPanel();
		pKeys.setBounds(10, 320, fxDim-20, 280);
		pKeys.setBorder(BorderFactory.createTitledBorder("Single Keys"));
		pKeys.setLayout(null);	

		//init components
		JLabel lbBGM = new JLabel("BGM");
		JComboBox cbBGM = new JComboBox();
		JLabel lbCall = new JLabel("Call:");
		JRadioButton rbLong = new JRadioButton("Long");
		JRadioButton rbShort = new JRadioButton("Short");
		ButtonGroup bgCall = new ButtonGroup();
		bgCall.add(rbLong);
		bgCall.add(rbShort);
		JLabel lbMethod = new JLabel("Method:");
		JComboBox cbMethod = new JComboBox();
		JLabel lbTargetVar = new JLabel("Target Var:");
		JComboBox cbTargetVar = new JComboBox();
		JLabel lbSDate = new JLabel("Start Date:");
		JTextField tfSDate = new JTextField("2016-01-01");
		JLabel lbEDate = new JLabel("End Date:");
		JTextField tfEDate = new JTextField("2020-12-31");
		JLabel lbSPD = new JLabel("SPD:");
		JTextField tfSPD = new JTextField("10");
		JLabel lbNarMask = new JLabel("NAR Mask:");
		JTextField tfNarMask = new JTextField("1111");
		JLabel lbIndMask = new JLabel("Ind Mask:");
		JTextField tfIndMask = new JTextField("111111111111111111111111");
		JButton bLastKeyAF = new JButton("Autofill Params From Last SK");

		JButton bGetKeys = new JButton("Get Matching SKs");

		DefaultTableModel dtmSK = new DefaultTableModel();
		dtmSK.addColumn("Key Num");
		dtmSK.addColumn("Is Best");
		dtmSK.addColumn("MS Mask");
		dtmSK.addColumn("APAPT");
		JTable tSK = new JTable(dtmSK);
		JScrollPane spSK = new JScrollPane(tSK, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JLabel lbBestKeys = new JLabel("Best Keys:");
		JTextArea taBestKeys = new JTextArea();
		JLabel lbCov = new JLabel("Coverage :     0.00 %");
		JButton bCheckCov = new JButton("Check Coverage");
		JButton bSaveAK = new JButton("Save AK");
		
		//bounds
		lbBGM.setBounds(10, 20, 80, 25);
		cbBGM.setBounds(110, 20, 80, 25);
		lbCall.setBounds(10, 50, 80, 25);
		rbLong.setBounds(110, 50, 80, 25);
		rbShort.setBounds(200, 50, 80, 25);
		lbMethod.setBounds(10, 80, 80, 25);
		cbMethod.setBounds(110, 80, 160, 25);
		lbTargetVar.setBounds(10, 110, 80, 25);
		cbTargetVar.setBounds(110, 110, 160, 25);
		lbSDate.setBounds(10, 140, 80, 25);
		tfSDate.setBounds(110, 140, 80, 25);
		lbEDate.setBounds(210, 140, 80, 25);
		tfEDate.setBounds(300, 140, 80, 25);
		lbSPD.setBounds(210, 170, 80, 25);
		tfSPD.setBounds(300, 170, 80, 25);
		lbNarMask.setBounds(10, 170, 80, 25);
		tfNarMask.setBounds(110, 170, 80, 25);
		lbIndMask.setBounds(10, 200, 70, 25);
		tfIndMask.setBounds(110, 200, 210, 25);
		bLastKeyAF.setBounds(10, 230, 200, 25);
		bGetKeys.setBounds(10, 285, 180, 30);
		tSK.setBounds(10, 20, 370, 180);
		spSK.setBounds(10, 20, 370, 180);
		lbBestKeys.setBounds(10, 205, 90, 25);
		taBestKeys.setBounds(100, 205, 230, 30);
		lbCov.setBounds(10, 240, 170, 25);
		bCheckCov.setBounds(210, 240, 120, 30);
		bSaveAK.setBounds(10, 605, 180, 30);

		//basic functionality
		rbLong.setFont(plainFont);
		rbShort.setFont(plainFont);
		rbLong.setSelected(true);
		setButtonStyle(bLastKeyAF);
		setButtonStyle(bGetKeys);
		setButtonStyle(bCheckCov);
		setButtonStyle(bSaveAK);
		for(int i = 0; i < bgmList.length; i++){
			cbBGM.addItem(bgmList[i]);
		}
		for(int i = 0; i < methodList.length; i++){
			cbMethod.addItem(methodList[i]);
		}
		cbMethod.setSelectedIndex(1);
		for(int i = 0; i < Globals.target_var_num; i++){
			cbTargetVar.addItem(Globals.tvi_monikers[i]);
		}
		tSK.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		tSK.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		tSK.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tSK.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		tSK.setAutoCreateRowSorter(true);
		taBestKeys.setLineWrap(true);
		taBestKeys.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		bCheckCov.setEnabled(false);
	
		//listener functionality
		cbBGM.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				String bgm = cbBGM.getSelectedItem().toString();
				if(bgm.equals("ANN")){
					lbMethod.setEnabled(true);
					cbMethod.setEnabled(true);
				}else{
					lbMethod.setEnabled(false);
					cbMethod.setEnabled(false);
				}
			}
		});
		bLastKeyAF.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String bgm = cbBGM.getSelectedItem().toString();
				bgm = bgm.toLowerCase();
				String ksPath = "./../out/sk/log/"+bgm+"/keys_struct.txt";
				FCI fciKS = new FCI(true, ksPath);
				ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile(ksPath, ",");
				if(ksFile.size() <= 1){
					JOptionPane.showMessageDialog(frame, "No single keys (SK) have been created.", "Error",
												JOptionPane.ERROR_MESSAGE);
				}else{
					ArrayList<String> ksRow = ksFile.get(ksFile.size()-1);
					String call = ksRow.get(fciKS.getIdx("call"));
					String tvi = ksRow.get(fciKS.getIdx("tvi"));
					String sdate = ksRow.get(fciKS.getIdx("start_date"));
					String edate = ksRow.get(fciKS.getIdx("end_date"));
					String narMask = ksRow.get(fciKS.getIdx("nar_mask"));
					String spd = ksRow.get(fciKS.getIdx("spd"));
					String indMask = ksRow.get(fciKS.getIdx("ind_mask"));
					if(call.equals("1")){
						rbLong.setSelected(true);
					}else{
						rbShort.setSelected(true);
					}
					cbTargetVar.setSelectedIndex(Integer.parseInt(tvi));
					tfSDate.setText(sdate);
					tfEDate.setText(edate);
					tfNarMask.setText(narMask);
					tfSPD.setText(spd);
					tfIndMask.setText(indMask);
				}	
			}
		});
		bGetKeys.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//get input params
				String bgm = cbBGM.getSelectedItem().toString();
				String method = cbMethod.getSelectedItem().toString();
				boolean isLong = rbLong.isSelected();
				if(bgm.equals("ANN")){
					if(method.equals("Binomial")){
						method = "BN";
					}
					if(method.equals("Continuous")){
						method = "CR";
					}
				}
				String sdate = tfSDate.getText();
				String edate = tfEDate.getText();
				String spd = tfSPD.getText();
				String tvi = String.valueOf(cbTargetVar.getSelectedIndex());
				String narMask = tfNarMask.getText();
				String indMask = tfIndMask.getText();
				//get matching SKs from keys_struct and best key that give 100% coverage
				ArrayList<ArrayList<String>> mlines = getMatchingKeys(bgm, isLong, method, sdate, edate,
																 spd, tvi, narMask, indMask);
				if(mlines.size() == 0){
					JOptionPane.showMessageDialog(frame, "No key matches given parameters.", "Error", JOptionPane.ERROR_MESSAGE);
				}else{
					ArrayList<String> mkeys = AhrAL.getCol(mlines, 0);
					ArrayList<ArrayList<String>> rcKeys = calcCovKeys(bgm, isLong, mkeys);
					ArrayList<ArrayList<String>> cov = AhrAL.getSelectRows(rcKeys, "0", 1);
					ArrayList<String> bestKeys = AhrAL.getCol(cov, 0);
					//clear table
					DefaultTableModel dtm = (DefaultTableModel)tSK.getModel();
					while(dtm.getRowCount() > 0){
						dtm.removeRow(0);
					}
					//update table
					for(int i = 0; i < rcKeys.size(); i++){
						String[] row = new String[4];
						String itrKey = rcKeys.get(i).get(0);
						row[0] = itrKey;
						if(bestKeys.contains(itrKey)){
							row[1] = "Yes";
						}else{
							row[1] = "No";
						}
						row[2] = rcKeys.get(i).get(2);
						row[3] = rcKeys.get(i).get(3);
						dtm.addRow(row);
					}
					//update taBestKeys and lbCov
					double covNum = calcJustCov(AhrAL.getCol(cov, 2));
					String bkeyStr = "";
					for(int i = 0; i < cov.size(); i++){
						bkeyStr += cov.get(i).get(0);
						if(i != cov.size()-1){
							bkeyStr += ", ";
						}
					}
					taBestKeys.setText(bkeyStr);
					lbCov.setText("Coverage :     "+String.format("%.2f", covNum)+" %");
					bCheckCov.setEnabled(false);
					//update GUI
					frame.revalidate();
					frame.repaint();
					frame.setVisible(true);
				}
			}
		});
		taBestKeys.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void insertUpdate(DocumentEvent e){
				if(bCheckCov.isEnabled() == false){
					bCheckCov.setEnabled(true);
				}
			}
			@Override
			public void removeUpdate(DocumentEvent e){
				if(bCheckCov.isEnabled() == false){
					bCheckCov.setEnabled(true);
				}
			}
			@Override
			public void changedUpdate(DocumentEvent e){
			}
		});
		bCheckCov.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//get masks from SKs in taBestKeys
				ArrayList<String> covMasks = new ArrayList<String>();
				String[] bkeys = taBestKeys.getText().replaceAll("\\s+","").split(",");
				for(int i = 0; i < tSK.getRowCount(); i++){
					String itrKey = String.valueOf(tSK.getValueAt(i, 0));
					if(AhrGen.contains(bkeys, itrKey)){
						covMasks.add(String.valueOf(tSK.getValueAt(i, 2)));
					}
				}			
				double covNum = calcJustCov(covMasks);
				lbCov.setText("Coverage :     "+String.format("%.2f", covNum)+" %");
				bCheckCov.setEnabled(false);		
			}
		});
		bSaveAK.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//1st assert cov is 100%
				if(!coverage_is_100){
					JOptionPane.showMessageDialog(frame, "An AK requires coverage of 100% to be created.", "Error",
													JOptionPane.ERROR_MESSAGE);
				}else{
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					String bgm = cbBGM.getSelectedItem().toString();
					String[] skeys = taBestKeys.getText().replaceAll("\\s+","").split(",");
					String kpPath = "./../out/sk/log/"+bgm.toLowerCase()+"/keys_perf.txt";
					String alPath = "./../out/ak/log/ak_log.txt";
					FCI fciKP = new FCI(true, kpPath);
					FCI fciAL = new FCI(true, alPath);
					String bestKeys = "";
					String skBimSom = "";
					for(int i = 0; i < skeys.length; i++){
						//System.out.println("--> SK"+skeys[i]);
						ArrayList<String> kpRow = AhrIO.scanRow(kpPath, ",", skeys[i]);
						String bim = kpRow.get(fciKP.getIdx("bim"));
						String som = kpRow.get(fciKP.getIdx("som"));
						if(i == skeys.length-1){
							bestKeys += skeys[i];
							skBimSom += bim+"|"+som;
						}else{
							bestKeys += skeys[i]+"~";
							skBimSom += bim+"|"+som+"~";
						}
					}
					if(skBimSom.contains("ph")){
						skBimSom = "ph";
					}
					//get new ak_num
					ArrayList<ArrayList<String>> alFile = AhrIO.scanFile(alPath, ",");
					int maxID = -1;
					for(int i = 1; i < alFile.size(); i++){
						int itrID = Integer.parseInt(alFile.get(i).get(fciAL.getIdx("ak_num")));
						if(itrID > maxID){
							maxID = itrID;
						}
					}
					int newID = maxID + 1;
					//write new AK to log_agg
					ArrayList<String> akLine = new ArrayList<String>();
					akLine.add(String.valueOf(newID));							//[0]  ak_num
					akLine.add(bgm);											//[1]  bgm
					akLine.add("IT");											//[2]  db_used
					akLine.add(AhrDate.getTodaysDate());						//[3]  date_ran
					akLine.add(tfSDate.getText());								//[4]  start_date
					akLine.add(tfEDate.getText());								//[5]  end_date
					if(rbLong.isSelected()){									//[6]  call
						akLine.add("1");
					}else{
						akLine.add("0");
					}
					akLine.add(tfSPD.getText());								//[7]  spd
					akLine.add(String.valueOf(cbTargetVar.getSelectedIndex()));	//[8]  tvi
					akLine.add(tfIndMask.getText());							//[9]  ind_mask
					akLine.add(tfNarMask.getText());							//[10] nar_mask
					akLine.add(bestKeys);										//[11] best_keys
					akLine.add(skBimSom);										//[12] sk_bso
					akLine.add("ph");											//[13] ak_bso
					akLine.add("ph");											//[14] bso_train_apapt
					akLine.add("ph");											//[15] bso_test_apapt
					akLine.add("ph");											//[16] bso_train_posp
					akLine.add("ph");											//[17] bso_test_posp
					akLine.add("ph");											//[18] true_train_apapt
					akLine.add("ph");											//[19] true_test_apapt
					akLine.add("ph");											//[20] true_train_posp
					akLine.add("ph");											//[21] true_test_posp
					alFile.add(akLine);
					AhrIO.writeToFile(alPath, alFile, ",");
					//write basis file
					BGM_Manager akey = new BGM_Manager(newID);
					//System.out.print("--> Generating Basis File ... ");
					akey.genBasisAK();
					//System.out.println("DONE");
					//calc basic perf data
					ArrayList<String> perfMetrics = new ArrayList<String>();
					//System.out.print("--> Calculating Basic AK Performance ... ");
					String basisPath = "./../out/ak/baseis/ann/ANN_"+String.valueOf(newID)+".txt";
					ArrayList<String> perf = akey.perfFromBasisFile(basisPath);
					perfMetrics.add(perf.get(3));
					perfMetrics.add(perf.get(4));
					akey.perfToFileAK(perf);
					//System.out.println("DONE");
					//calc ak_bso and replace in ak_log
					//System.out.print("--> Calculating BSO AK Performance ... ");
					akey.bsoPerfToFileAK(true, false);
					perfMetrics.add(AhrIO.scanCell(alPath, ",", String.valueOf(newID), fciAL.getIdx("bso_test_apapt")));
					//System.out.println("DONE");				
					//add all SKs in AK to score_percentiles
					//System.out.print("--> Calculating Score Percentiles ... ");
					akey.calcScorePercentiles();

					//display done w/ JOptionPane
					frame.setCursor(null);
					String message = "AK"+String.valueOf(newID)+" created successfully."+
									"\nSome test dataset metrics ..."+
									"\n   > Plateau APAPT = "+perfMetrics.get(0)+
									"\n   > True APAPT    = "+perfMetrics.get(1)+
									"\n   > BSO APAPT     = "+perfMetrics.get(2)+ 
									"\nIts parameters and full performance metrics can be seen at :"+
									"\n   Machine Learning -> SK, AK, Basis Info -> Aggregate Keys";
					JOptionPane.showMessageDialog(frame, message, "Key Created", JOptionPane.PLAIN_MESSAGE);
					//System.out.println("DONE");
					//System.out.println("--> AK Creation ... DONE");
				}
			}
		});

		//add
		pInputs.add(lbBGM);
		pInputs.add(cbBGM);
		pInputs.add(lbCall);
		pInputs.add(rbLong);
		pInputs.add(rbShort);
		pInputs.add(lbMethod);
		pInputs.add(cbMethod);
		pInputs.add(lbTargetVar);
		pInputs.add(cbTargetVar);
		pInputs.add(lbSDate);
		pInputs.add(tfSDate);
		pInputs.add(lbEDate);
		pInputs.add(tfEDate);
		pInputs.add(lbSPD);
		pInputs.add(tfSPD);
		pInputs.add(lbNarMask);
		pInputs.add(tfNarMask);
		pInputs.add(lbIndMask);
		pInputs.add(tfIndMask);
		pInputs.add(bLastKeyAF);
		pKeys.add(spSK);
		pKeys.add(lbBestKeys);
		pKeys.add(taBestKeys);
		pKeys.add(lbCov);
		pKeys.add(bCheckCov);
		frame.add(pInputs);
		frame.add(bGetKeys);
		frame.add(pKeys);
		frame.add(bSaveAK);
		frame.setVisible(true);
	}
	//GUI related, sets style to a JButton
	public void setButtonStyle(JButton btn){
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}

	//get list of single keys that match params
	public ArrayList<ArrayList<String>> getMatchingKeys(String bgm, boolean isLong, String method, String sdate, 
											String edate, String spd, String tvi, String narMask, String indMask){
		//get data from keys_struct
		String ksPath = "./../out/sk/log/"+bgm.toLowerCase()+"/keys_struct.txt";
		FCI fciKS = new FCI(true, ksPath);
		ArrayList<ArrayList<String>> fc = AhrIO.scanFile(ksPath, ",");
		ArrayList<ArrayList<String>> goodLines = new ArrayList<ArrayList<String>>();
		//get relv SKs from keys_struct
		for(int i = 1; i < fc.size(); i++){
			boolean is_good_sk = true;
			if(bgm.equals("ANN")){
				if(!fc.get(i).get(fciKS.getIdx("method")).equals(method)){
					is_good_sk = false;
				}
			}
			boolean isLongItr = false;
			if(fc.get(i).get(fciKS.getIdx("call")).equals("1")){
				isLongItr = true;
			}
			if(isLong != isLongItr){
				is_good_sk = false;
			}
			if(!fc.get(i).get(fciKS.getIdx("start_date")).equals(sdate)){
				is_good_sk = false;
			}
			if(!fc.get(i).get(fciKS.getIdx("end_date")).equals(edate)){
				is_good_sk = false;
			}
			if(!fc.get(i).get(fciKS.getIdx("spd")).equals(spd)){
				is_good_sk = false;
			}
			if(!fc.get(i).get(fciKS.getIdx("tvi")).equals(tvi)){
				is_good_sk = false;
			}
			if(!fc.get(i).get(fciKS.getIdx("nar_mask")).equals(narMask)){
				is_good_sk = false;
			}
			if(!fc.get(i).get(fciKS.getIdx("ind_mask")).equals(indMask)){
				is_good_sk = false;
			}
			if(is_good_sk){
				goodLines.add(fc.get(i));
			}
		}
		//System.out.println("==> Matching SKs ...");
		//AhrAL.print(goodLines);
		return goodLines;
	}

	//calc best SKs from selected SKs that give 100% of market states
	public ArrayList<ArrayList<String>> calcCovKeys(String bgm, boolean isLong, ArrayList<String> keys){
		String bpath = "./../out/sk/log/"+bgm.toLowerCase()+"/";
		FCI fciKS = new FCI(true, bpath+"keys_struct.txt");
		FCI fciKP = new FCI(true, bpath+"keys_perf.txt");
		ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile(bpath+"keys_struct.txt", ",");
		ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(bpath+"keys_perf.txt", ",");
		//get market state mask for every key
		ArrayList<ArrayList<String>> msData = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < keys.size(); i++){
			ArrayList<String> kpLine = AhrAL.getRow(kpFile, keys.get(i));
			ArrayList<String> line = new ArrayList<String>();
			line.add(keys.get(i));
			line.add(kpLine.get(fciKP.getIdx("ms_mask")));
			line.add(kpLine.get(fciKP.getIdx("true_test_apapt")));
			msData.add(line);
		}
		//sort by true appr
		Collections.sort(msData, new Comparator<ArrayList<String>>(){
			@Override
			public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
				double dcomp1 = Double.parseDouble(obj1.get(2));
				double dcomp2 = Double.parseDouble(obj2.get(2));
				if(isLong){
					return Double.compare(dcomp1, dcomp2) * -1;
				}else{
					return Double.compare(dcomp1, dcomp2);
				}
			}
		});
		//System.out.println("==> Sorted Matched Keys ...");
		//AhrAL.print(msData);
		//get rid of redundant keys
		ArrayList<ArrayList<String>> allmKeys = new ArrayList<ArrayList<String>>();
		ArrayList<String> masks = new ArrayList<String>();
		boolean all_states_met = false;
		double prevCovBuf = 0.0;		//holds cov last value, dont add key if cur is same as prev
		for(int i = 0; i < msData.size(); i++){
			masks.add(msData.get(i).get(1));
			double coverage = calcJustCov(masks);
			boolean is_useless_key = false;
			if(coverage == prevCovBuf){
				is_useless_key = true;
			}
			ArrayList<String> line = new ArrayList<String>();
			line.add(msData.get(i).get(0));
			if(all_states_met || is_useless_key){
				line.add("1");
			}else{
				line.add("0");
			}
			line.add(msData.get(i).get(1));
			line.add(msData.get(i).get(2));
			allmKeys.add(line);
			if(coverage > 99.99){
				all_states_met = true;
			}
			prevCovBuf = coverage;
		}
		//System.out.println("==> After Redundancy Check ...");
		//AhrAL.print(allmKeys);	
		
		return allmKeys;
	}

	//calc % of states covered by given MS Masks
	public double calcJustCov(ArrayList<String> keys){
		double covPercent = 0.0;
		if(keys.size() > 0){
			int bits = keys.get(0).length();
			int combos = (int)Math.pow(2.0, (double)bits);
			for(int i = 0; i < combos; i++){
				String itrState = Integer.toBinaryString(i);
				while(itrState.length() < bits){
					itrState = "0" + itrState;
				}
				boolean this_state_met = false;
				for(int j = 0; j < keys.size(); j++){
					if(AhrGen.compareMasks(keys.get(j), itrState)){
						this_state_met = true;
						//System.out.println("Mask = "+keys.get(j)+"  |  State = "+itrState+"  |  YES");
					}else{
						//System.out.println("Mask = "+keys.get(j)+"  |  State = "+itrState+"  |  NO");
					}
				}
				if(this_state_met){
					covPercent += 1.0;
				}
				//System.out.println("  --> State Met : "+this_state_met);
			}
			covPercent = (covPercent / (double)combos) * 100.0;
		}else{
			String message = "No market state masks given."; 
			JOptionPane.showMessageDialog(null, message, "Input Error", JOptionPane.ERROR_MESSAGE);
		}
		if(covPercent > 99.99){
			coverage_is_100 = true;
		}else{
			coverage_is_100 = false;
		}
		return covPercent;
	}

}
