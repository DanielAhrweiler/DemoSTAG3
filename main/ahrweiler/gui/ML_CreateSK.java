package ahrweiler.gui;
import ahrweiler.Globals;
import ahrweiler.util.AhrIO;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import ahrweiler.support.StockFilter;
import ahrweiler.bgm.*;
import ahrweiler.bgm.ann.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ML_CreateSK {

	final Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	public ML_CreateSK(){
		drawGUI();
	}

	public void drawGUI(){
		//lists and overarching structs
		String[] geneticBgmList = {"GAD2", "GAB3"};
		String[] geneticFitFuncts = {"Fast Decrease"};
		String defIndMask = "";		//default ind mask string
		for(int i = 0; i < Globals.snorm_num; i++){
			String itrNum = String.valueOf(i);
			while(itrNum.length() < 2){
				itrNum = "0" + itrNum;
			}
			if(i != (Globals.snorm_num-1)){
				itrNum += ",";
			}
			defIndMask += itrNum;
		}

		//layout components
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("Basis Generating Methods: Single Key");
		frame.setSize(550, 500);
		frame.setLayout(null);
		JPanel pANN = new JPanel();
		pANN.setLayout(null);
		JPanel pGenetic = new JPanel();
		pGenetic.setLayout(null);
		JTabbedPane tpBGM = new JTabbedPane();
		tpBGM.setBounds(0, 0, 550, 550);
		
		
		/*----------------------------------------------------------
			ANN Panel
		------------------------------------------------------------*/
		//layout
		JPanel pBasic1 = new JPanel();
		pBasic1.setBounds(10, 10, 530, 150);
		pBasic1.setBorder(BorderFactory.createTitledBorder("Basic ANN Variables"));
		pBasic1.setLayout(null);
		JPanel pDBS1 = new JPanel();		//DB State Panel
		pDBS1.setBounds(10, 170, 530, 185);
		pDBS1.setBorder(BorderFactory.createTitledBorder("DB Structure"));
		pDBS1.setLayout(null);
						
		
		//init components
		JLabel lbMethod1 = new JLabel("Method: ");			//Basic Panel
		JRadioButton rbContRange1 = new JRadioButton("Cont Range");
		JRadioButton rbBinomial1 = new JRadioButton("Binomial");				
		ButtonGroup bgMethod1 = new ButtonGroup();				
		bgMethod1.add(rbContRange1);
		bgMethod1.add(rbBinomial1);
		JLabel lbSPD1 = new JLabel("SPD: ");
		JTextField tfSPD1 = new JTextField("10");
		JLabel lbTargetVars1 = new JLabel("Target Var: ");
		JComboBox cbTargetVars1 = new JComboBox();
		JLabel lbPlat1 = new JLabel("Plateau Val: ");
		JTextField tfPlat1 = new JTextField("10.0");
		JLabel lbLearnRate1 = new JLabel("Learn Rate: ");
		JTextField tfLearnRate1 = new JTextField("0.10");
		JLabel lbSDate1 = new JLabel("Start Date:");			//DB State Panel
		JTextField tfSDate1 = new JTextField("2009-01-01");
		JLabel lbEDate1 = new JLabel("End Date:");
		JTextField tfEDate1 = new JTextField("2019-12-31");
		JLabel lbMsMask1 = new JLabel("MS Mask:");
		JTextField tfMsMask1 = new JTextField("xxxxxxxx");
		JLabel lbNarMask1 = new JLabel("NAR Mask: ");
		JTextField tfNarMask1 = new JTextField("1111");		
		JLabel lbIndMask1 = new JLabel("Indicator Mask");
		JTextArea taIndMask1 = new JTextArea(defIndMask, 2, 40);
		JButton bListInds1 = new JButton("List");
		JButton bCalcSK1 = new JButton("Calculate SK");

		//bounds of components
		lbMethod1.setBounds(10, 20, 65, 25);				//Basic Panel
		rbContRange1.setBounds(80, 20, 120, 25);
		rbBinomial1.setBounds(200, 20, 120, 25);
		lbSPD1.setBounds(350, 20, 100, 25);
		tfSPD1.setBounds(450, 20, 50, 25);
		lbTargetVars1.setBounds(10, 60, 90, 25);
		cbTargetVars1.setBounds(100, 60, 150, 25);
		lbPlat1.setBounds(350, 60, 100, 25);
		tfPlat1.setBounds(450, 60, 50, 25);
		lbLearnRate1.setBounds(350, 100, 100, 25);
		tfLearnRate1.setBounds(450, 100, 50, 25);
		lbSDate1.setBounds(10, 20, 90, 25);				//DB State Panel
		tfSDate1.setBounds(100, 20, 100, 25);
		lbEDate1.setBounds(300, 20, 100, 25);
		tfEDate1.setBounds(400, 20, 100, 25);
		lbMsMask1.setBounds(10, 60, 90, 25);
		tfMsMask1.setBounds(100, 60, 100, 25);	
		lbNarMask1.setBounds(300, 60, 100, 25);
		tfNarMask1.setBounds(400, 60, 100, 25);
		lbIndMask1.setBounds(10, 95, 300, 25);
		taIndMask1.setBounds(10, 120, 420, 50);
		bListInds1.setBounds(440, 130, 60, 30);
		bCalcSK1.setBounds(200, 370, 150, 40);

		//basic functionality
		rbContRange1.setFont(plainFont);
		rbBinomial1.setFont(plainFont);
		setButtonStyle(bListInds1);
		setButtonStyle(bCalcSK1);
		rbContRange1.setSelected(true);
		for(int i = 0; i < Globals.target_var_num; i++){
			cbTargetVars1.addItem(Globals.tvi_monikers[i]);
		}
		taIndMask1.setLineWrap(true);

		//button functionality
		bListInds1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				String message = "";
				for(int i = 0; i < Globals.ind_names.length; i++){
					String printInd = String.valueOf(i);
					if(printInd.length() == 1){
						printInd = "0" + printInd;
					}
					printInd += " - " + Globals.ind_names[i];
					message += printInd;
					if(i != (Globals.ind_names.length-1)){
						message += "\n";
					}
					//System.out.println(printInd);
				}
				JOptionPane.showMessageDialog(frame, message, "All Indicators", JOptionPane.PLAIN_MESSAGE);
			}
		});
		bCalcSK1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//System.out.println("--> Calculate SK for ANN");
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				//set up algo info
				String ksPath = AhrIO.uniPath("./../out/sk/log/ann/keys_struct.txt");
				String kpPath = AhrIO.uniPath("./../out/sk/log/ann/keys_perf.txt");
				FCI fciKS = new FCI(true, ksPath);
				FCI fciKP = new FCI(true, kpPath);
				ArrayList<ArrayList<String>> keys = AhrIO.scanFile(ksPath,",");
				int maxID = -1;
				if(keys.size() > 1){
					for(int i = 1; i < keys.size(); i++){
						int itrKey = Integer.parseInt(keys.get(i).get(fciKS.getIdx("sk_num")));
						if(itrKey > maxID){
							maxID = itrKey;
						}
					}
				}
				int id = maxID + 1;
				AttributesSK kattrs = new AttributesSK();
				if(rbContRange1.isSelected()){
					kattrs.setAnnMethod("CR");
				}else{
					kattrs.setAnnMethod("BN");
				}
				kattrs.setSDate(tfSDate1.getText());
				kattrs.setEDate(tfEDate1.getText());
				kattrs.setSPD(Integer.parseInt(tfSPD1.getText()));
				kattrs.setTVI(cbTargetVars1.getSelectedIndex());
				kattrs.setPlateau(Double.parseDouble(tfPlat1.getText()));
				kattrs.setLearnRate(Double.parseDouble(tfLearnRate1.getText()));
				kattrs.setMsMask(tfMsMask1.getText());
				kattrs.setNarMask(tfNarMask1.getText());
				ANN algo = new ANN(kattrs);
				algo.setID(id);
				String[] actInds = taIndMask1.getText().split(",");
				ArrayList<Integer> actIndInts = new ArrayList<Integer>();
				for(int i = 0 ; i < actInds.length; i++){
					actIndInts.add(Integer.parseInt(actInds[i]));
				}
				String indMask = "";
				for(int i = 0 ; i < Globals.ind_names.length; i++){
					if(actIndInts.contains(i)){
						indMask += "1";
					}else{
						indMask += "0";
					}
				}
				algo.setIndMask(indMask);
				//run the ANN
				//System.out.println("--> In ML_CreateSK, calc SK"+id+" and SK"+(id+1));
				long time1 = System.currentTimeMillis();
				algo.calcSK();
				//create basis file
				long time2 = System.currentTimeMillis();
				BGM_Manager skShort = new BGM_Manager("ANN", id);
				skShort.genBasisSK(id);
				//calc perf for short SK
				ArrayList<String> perfMetrics = new ArrayList<String>();
				long time3 = System.currentTimeMillis();
				String shortBasisPath = AhrIO.uniPath("./../out/sk/baseis/ann/ANN_"+String.valueOf(id)+".txt");
				ArrayList<String> shortPerf = skShort.perfFromBasisFile(shortBasisPath);
				perfMetrics.add(shortPerf.get(3));
				perfMetrics.add(shortPerf.get(4));
				skShort.perfToFileSK(shortPerf);			//save short basic SK perf to keys_perf
				skShort.bsoPerfToFileSK(true, false);		//save short BSO perf to keys_perf
				perfMetrics.add(AhrIO.scanCell(kpPath, ",", String.valueOf(id), fciKP.getIdx("bso_test_apapt")));
				//calc perf for long SK
				long time4 = System.currentTimeMillis();
				BGM_Manager skLong = new BGM_Manager("ANN", id+1);
				skLong.genBasisSK(id+1);
				long time5 = System.currentTimeMillis();
				String longBasisPath = AhrIO.uniPath("./../out/sk/baseis/ann/ANN_"+String.valueOf(id+1)+".txt");
				ArrayList<String> longPerf = skLong.perfFromBasisFile(longBasisPath);
				perfMetrics.add(longPerf.get(3));
				perfMetrics.add(longPerf.get(4));
				skLong.perfToFileSK(longPerf);				//save long basic SK perf to keys_perf
				skLong.bsoPerfToFileSK(true, false);		//save long BSO perf to keys_perf
				perfMetrics.add(AhrIO.scanCell(kpPath, ",", String.valueOf(id+1), fciKP.getIdx("bso_test_apapt")));
				long time6 = System.currentTimeMillis();


				//System.out.println("******* Time Marks *****\n"+
				//					"--> calcSK() : "+(time2-time1)+" ms\n"+
				//					"--> Gen Short Basis : "+(time3-time2)+" ms\n"+
				//					"--> Calc Short Perf : "+(time4-time3)+" ms\n"+
				//					"--> Gen Long Basis  : "+(time5-time4)+" ms\n"+
				//					"--> Calc Long Perf  : "+(time6-time5)+" ms\n");

				//display sk info
				frame.setCursor(null);
				String message = "SK"+String.valueOf(id)+" created successfully."+
								"\nSome test dataset metrics ..."+
								"\n   > Plateau APAPT = "+perfMetrics.get(0)+
								"\n   > True APAPT    = "+perfMetrics.get(1)+
								"\n   > BSO APAPT     = "+perfMetrics.get(2)+ 
								"\nSK"+String.valueOf(id+1)+" created successfully."+
								"\nSome test dataset metrics ..."+
								"\n   > Plateau APAPT = "+perfMetrics.get(3)+
								"\n   > True APAPT    = "+perfMetrics.get(4)+
								"\n   > BSO APAPT     = "+perfMetrics.get(5)+
								"\nTheir parameters and full performance metrics can be seen at :"+
								"\n   Machine Learning -> SK, AK, Basis Info -> Single Keys";
				JOptionPane.showMessageDialog(frame, message, "Key Created", JOptionPane.PLAIN_MESSAGE);
			}
		});

		/*----------------------------------------------------------
			Genetic Panel
		------------------------------------------------------------*/
		//layout
		JPanel pBasic2 = new JPanel();			//Basic Panel
		pBasic2.setBounds(10, 40, 530, 160);
		pBasic2.setBorder(BorderFactory.createTitledBorder("Basic GA Variables"));
		pBasic2.setLayout(null);
		JPanel pMasks2 = new JPanel();			//Mask Panel
		pMasks2.setBounds(10, 210, 530, 150);
		pMasks2.setBorder(BorderFactory.createTitledBorder("Mask Variables"));
		pMasks2.setLayout(null);
		
		//init components
		JLabel lbBgm2 = new JLabel("Method:");
		JComboBox cbBgm2 = new JComboBox();
		JLabel lbCall2 = new JLabel("Call: ");			//Basic Panel
		JRadioButton rbLong2 = new JRadioButton("Long");
		JRadioButton rbShort2 = new JRadioButton("Short");
		ButtonGroup bgCall2 = new ButtonGroup();
		bgCall2.add(rbLong2);
		bgCall2.add(rbShort2);
		JLabel lbPop2 = new JLabel("Pop Size: ");
		JTextField tfPop2 = new JTextField("500");
		JLabel lbFit2 = new JLabel("Fitness:");
		JComboBox cbFit2 = new JComboBox();
		JLabel lbSPD2 = new JLabel("SPD:");
		JTextField tfSPD2 = new JTextField("10");
		JLabel lbTVar2 = new JLabel("Target Var: ");
		JComboBox cbTVar2 = new JComboBox();
		JLabel lbPlateau2 = new JLabel("Plateau:");
		JTextField tfPlateau2 = new JTextField("10.0");
		JLabel lbSDate2 = new JLabel("Start Date:");
		JTextField tfSDate2 = new JTextField("2009-01-01");
		JLabel lbEDate2 = new JLabel("End Date:");
		JTextField tfEDate2 = new JTextField("2019-12-31");
		JLabel lbMsMask2 = new JLabel("MS Mask: ");		//Mask Panel
		JTextField tfMsMask2 = new JTextField("xxxxxxxx");
		JLabel lbNarMask2 = new JLabel("NAR Mask:");
		JTextField tfNarMask2 = new JTextField("1111");
		JLabel lbIndMask2 = new JLabel("Indicator Mask");
		JTextArea taIndMask2 = new JTextArea(defIndMask, 2, 40);
		JButton bListInds2 = new JButton("List");
		JButton bCalcSK2 = new JButton("Calculate SK");	//Calc SK Button
		
		//bounds of components
		lbBgm2.setBounds(10, 10, 80, 25);
		cbBgm2.setBounds(90, 10, 80, 25);
		lbCall2.setBounds(10, 20, 50, 25);				//Basic Panel
		rbLong2.setBounds(90, 20, 60, 25);
		rbShort2.setBounds(160, 20, 70, 25);
		lbPop2.setBounds(310, 20, 90, 25);
		tfPop2.setBounds(410, 20, 60, 25);
		lbFit2.setBounds(10, 55, 80, 25);
		cbFit2.setBounds(100, 55, 150, 25);
		lbSPD2.setBounds(310, 55, 90, 25);
		tfSPD2.setBounds(410, 55, 60, 25);
		lbTVar2.setBounds(10, 90, 100, 25);
		cbTVar2.setBounds(100, 90, 150, 25);
		lbPlateau2.setBounds(310, 90, 80, 25);
		tfPlateau2.setBounds(410, 90, 60, 25);
		lbSDate2.setBounds(10, 125, 800, 25);
		tfSDate2.setBounds(100, 125, 100, 25);
		lbEDate2.setBounds(310, 125, 80, 25);
		tfEDate2.setBounds(410, 125, 100, 25);
		lbMsMask2.setBounds(10, 10, 80, 40);				//Mask Panel
		tfMsMask2.setBounds(100, 20, 100, 25);
		lbNarMask2.setBounds(310, 20, 90, 25);
		tfNarMask2.setBounds(410, 20, 50, 25);
		lbIndMask2.setBounds(10, 50, 300, 25);
		taIndMask2.setBounds(10, 85, 420, 50);
		bListInds2.setBounds(440, 90, 60, 40);
		bCalcSK2.setBounds(200, 370, 150, 40);			//Calc SK button
		
		//basic functionality
		setButtonStyle(bListInds2);
		setButtonStyle(bCalcSK2);
		for(int i = 0; i < geneticBgmList.length; i++){
			cbBgm2.addItem(geneticBgmList[i]);
		}
		rbLong2.setSelected(true);	
		for(int i = 0 ; i < geneticFitFuncts.length; i++){
			cbFit2.addItem(geneticFitFuncts[i]);
		}
		for(int i = 0; i < Globals.target_var_num; i++){
			cbTVar2.addItem(Globals.tvi_monikers[i]);
		}
		taIndMask2.setLineWrap(true);
		bListInds2.setEnabled(false);
		bCalcSK2.setEnabled(false);
		
		//button functionality

		//add everything
		pBasic1.add(lbMethod1);
		pBasic1.add(rbContRange1);
		pBasic1.add(rbBinomial1);
		pBasic1.add(lbSPD1);
		pBasic1.add(tfSPD1);
		pBasic1.add(lbTargetVars1);
		pBasic1.add(cbTargetVars1);
		pBasic1.add(lbPlat1);
		pBasic1.add(tfPlat1);
		pBasic1.add(lbLearnRate1);
		pBasic1.add(tfLearnRate1);
		pDBS1.add(lbSDate1);
		pDBS1.add(tfSDate1);
		pDBS1.add(lbEDate1);
		pDBS1.add(tfEDate1);
		pDBS1.add(lbMsMask1);
		pDBS1.add(tfMsMask1);
		pDBS1.add(lbNarMask1);
		pDBS1.add(tfNarMask1);
		pDBS1.add(lbIndMask1);
		pDBS1.add(taIndMask1);
		pDBS1.add(bListInds1);
		pBasic2.add(lbCall2);
		pBasic2.add(rbLong2);
		pBasic2.add(rbShort2);
		pBasic2.add(lbPop2);
		pBasic2.add(tfPop2);
		pBasic2.add(lbFit2);
		pBasic2.add(cbFit2);
		pBasic2.add(lbSPD2);
		pBasic2.add(tfSPD2);
		pBasic2.add(lbTVar2);
		pBasic2.add(cbTVar2);
		pBasic2.add(lbPlateau2);
		pBasic2.add(tfPlateau2);
		pBasic2.add(lbSDate2);
		pBasic2.add(tfSDate2);
		pBasic2.add(lbEDate2);
		pBasic2.add(tfEDate2);
		pMasks2.add(lbMsMask2);
		pMasks2.add(tfMsMask2);
		pMasks2.add(lbNarMask2);
		pMasks2.add(tfNarMask2);
		pMasks2.add(lbIndMask2);
		pMasks2.add(taIndMask2);
		pMasks2.add(bListInds2);
		pANN.add(pBasic1);
		pANN.add(pDBS1);
		pANN.add(bCalcSK1);
		pGenetic.add(lbBgm2);
		pGenetic.add(cbBgm2);
		pGenetic.add(pBasic2);
		pGenetic.add(pMasks2);
		pGenetic.add(bCalcSK2);
		tpBGM.add("ANN", pANN);
		tpBGM.add("Genetic", pGenetic);
		frame.add(tpBGM);
		frame.setVisible(true);
		
	}
	//GUI related, sets style to a JButton
	public void setButtonStyle(JButton btn){
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}
}
