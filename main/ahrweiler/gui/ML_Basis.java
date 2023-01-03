package ahrweiler.gui;
import ahrweiler.util.*;
import ahrweiler.bgm.BGM_Manager;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.Thread;
import javax.swing.SwingUtilities;

public class ML_Basis extends JFrame {

	Font monoFont = new Font(Font.MONOSPACED, Font.BOLD, 11);
	String[] bgmList = {"ANN", "GAD2", "GAB3"};

	public ML_Basis(){
		drawGUI();
	}

	public void drawGUI(){
		//lists and over-arching structs
		int fxDim = 500;
		int fyDim = 600;

		//layout components
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setTitle("Basis File Management");
		this.setSize(fxDim, fyDim);
		this.setLayout(null);

		//lists and overaching structs
		ArrayList<String> epochList = AhrIO.scanCol("./../in/epochs.txt", ",", 0);
		ArrayList<String> akList = new ArrayList<String>();
		FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
		ArrayList<ArrayList<String>> fcAL = AhrIO.scanFile("./../baseis/log/ak_log.txt", ",");
		for(int i = 1; i < fcAL.size(); i++){
			if(!fcAL.get(i).get(fciAL.getIdx("bgm")).equals("RND")){
				akList.add(fcAL.get(i).get(fciAL.getIdx("basis_num")));
			}
		}
		//layout
		JPanel pBasisGenNew = new JPanel();
		pBasisGenNew.setBorder(BorderFactory.createTitledBorder("Generate New Basis Files"));
		pBasisGenNew.setBounds(10, 100, 420, 180);
		pBasisGenNew.setLayout(null);
		JPanel pBasisUpdate = new JPanel();
		pBasisUpdate.setBorder(BorderFactory.createTitledBorder("Update Basis Files"));
		pBasisUpdate.setBounds(10, 290, 340, 240);
		pBasisUpdate.setLayout(null);

		//init components
		JLabel lbBsTot = new JLabel("Total Basis Files           : ... fetching");
		JLabel lbBsFull = new JLabel("Fully Updated Basis Files   : ... fetching");
		JLabel lbBsLim = new JLabel("Limited Updated Basis Files : ... fetching");
		JLabel lbBsNot = new JLabel("Not Updated Basis Files     : ... fetching");
		Button bBasisList = new Button("List");
		JRadioButton rbGenAllSK = new JRadioButton("All SK");
		JRadioButton rbGenAllAK = new JRadioButton("All AK");
		JRadioButton rbGenSingleSK = new JRadioButton("Single SK  |  BGM:");
		JRadioButton rbGenSingleAK = new JRadioButton("Single AK  |  Key:");
		ButtonGroup bgGenSelect = new ButtonGroup();
		bgGenSelect.add(rbGenAllSK);
		bgGenSelect.add(rbGenAllAK);
		bgGenSelect.add(rbGenSingleSK);
		bgGenSelect.add(rbGenSingleAK);
		JComboBox cbBgmSingleSK = new JComboBox();
		JLabel lbKeySingleSK = new JLabel("Key:");
		JComboBox cbKeySingleSK = new JComboBox();
		JComboBox cbKeySingleAK = new JComboBox();
		Button bBasisGenNew = new Button("Generate");
		JRadioButton rbNoKeys = new JRadioButton("No Keys");
		JRadioButton rbAllKeys = new JRadioButton("All Keys");
		JRadioButton rbByEpoch = new JRadioButton("By Epoch");
		JRadioButton rbByAK = new JRadioButton("By AK");
		JRadioButton rbBySK = new JRadioButton("By Single SK");
		ButtonGroup bgKeySelect = new ButtonGroup();
		bgKeySelect.add(rbNoKeys);
		bgKeySelect.add(rbAllKeys);
		bgKeySelect.add(rbByEpoch);
		bgKeySelect.add(rbByAK);
		bgKeySelect.add(rbBySK);
		JComboBox cbByEpoch = new JComboBox();
		JComboBox cbByAK = new JComboBox();
		JLabel lbBySKBgm = new JLabel("BGM:");
		JComboBox cbBySKBgm = new JComboBox();
		JLabel lbBySKKey = new JLabel("Key:");
		JComboBox cbBySKKey = new JComboBox();
		Button bBasisUpdate = new Button("Update");

		//components bounds
		lbBsTot.setBounds(10, 10, 350, 20);
		lbBsFull.setBounds(10, 30, 350, 20);
		lbBsLim.setBounds(10, 50, 350, 20);
		lbBsNot.setBounds(10, 70, 350, 20);
		bBasisList.setBounds(370, 40, 60, 30);
		rbGenAllSK.setBounds(10, 20, 120, 25);
		rbGenAllAK.setBounds(10, 50, 120, 25);
		rbGenSingleSK.setBounds(10, 80, 150, 25);
		cbBgmSingleSK.setBounds(160, 80, 80, 25);
		lbKeySingleSK.setBounds(250, 80, 60, 25);
		cbKeySingleSK.setBounds(320, 80, 80, 25); 
		rbGenSingleAK.setBounds(10, 110, 150, 25);		
		cbKeySingleAK.setBounds(160, 110, 80, 25);
		bBasisGenNew.setBounds(10, 140, 120, 30);
		rbNoKeys.setBounds(10, 20, 100, 25);
		rbAllKeys.setBounds(10, 50, 100, 25);
		rbByEpoch.setBounds(10, 80, 100, 25);
		cbByEpoch.setBounds(110, 80, 90, 25);
		rbByAK.setBounds(10, 110, 100, 25);
		cbByAK.setBounds(110, 110, 90, 25);
		rbBySK.setBounds(10, 140, 140, 25);
		lbBySKBgm.setBounds(40, 170, 50, 25);
		cbBySKBgm.setBounds(90, 170, 80, 25);
		lbBySKKey.setBounds(200, 170, 45, 25);
		cbBySKKey.setBounds(245, 170, 80, 25);
		bBasisUpdate.setBounds(10, 200, 120, 30);
				
		//async stuff
		class BasisStatusThread implements Runnable{
			@Override
			public void run(){
				System.out.print("--> Basis Status Thread running ... ");
				ArrayList<Integer> bstatuses = getBasisFileStatuses();
				ArrayList<Double> bpercents = getBasisFilePercents(bstatuses);
				lbBsTot.setText("Total Basis Files           : " + bstatuses.get(0));
				lbBsFull.setText("Fully Updated Basis Files   : " + bstatuses.get(1) +" ("+
								String.format("%.3f", bpercents.get(0)) + " %)");
				lbBsLim.setText("Limited Updated Basis Files : " + bstatuses.get(2) + " (" +
								String.format("%.3f", bpercents.get(1)) + " %)");
				lbBsNot.setText("Not Updated Basis Files     : " + bstatuses.get(3) + " (" +
								String.format("%.3f", bpercents.get(2)) + " %)");
				System.out.println("DONE");
			}
		}
		Thread bsfThread = new Thread(new BasisStatusThread());
		bsfThread.start();

		//basic functionality
		lbBsTot.setFont(monoFont);
		lbBsFull.setFont(monoFont);
		lbBsLim.setFont(monoFont);
		lbBsNot.setFont(monoFont);
		rbGenSingleSK.setSelected(true);
		rbNoKeys.setSelected(true);
		for(int i = 0; i < bgmList.length; i++){
			cbBgmSingleSK.addItem(bgmList[i]);
		}
		ArrayList<String> skByBgm1 = getSKeysByBgm(cbBgmSingleSK.getSelectedItem().toString());
		for(int i = 0; i < skByBgm1.size(); i++){
			cbKeySingleSK.addItem(skByBgm1.get(i));
		}		
		for(int i = 0; i < epochList.size(); i++){
			cbByEpoch.addItem(epochList.get(i));
		}
		for(int i = 0; i < akList.size(); i++){
			cbByAK.addItem(akList.get(i));
		}
		for(int i = 0; i < bgmList.length; i++){
			cbBySKBgm.addItem(bgmList[i]);
		}
		ArrayList<String> skByBgm2 = getSKeysByBgm(cbBySKBgm.getSelectedItem().toString());
		for(int i = 0; i < skByBgm2.size(); i++){
			cbBySKKey.addItem(skByBgm2.get(i));
		}

		//listener functionality
		cbBgmSingleSK.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				ArrayList<String> skByBgmList = getSKeysByBgm(cbBgmSingleSK.getSelectedItem().toString());
				cbKeySingleSK.removeAllItems();
				for(int i = 0; i < skByBgmList.size(); i++){
					cbKeySingleSK.addItem(skByBgmList.get(i));
				}
			}
		});
		cbBySKBgm.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				ArrayList<String> skByBgmList = getSKeysByBgm(cbBySKBgm.getSelectedItem().toString());
				cbBySKKey.removeAllItems();
				for(int i = 0; i < skByBgmList.size(); i++){
					cbBySKKey.addItem(skByBgmList.get(i));
				}
			}
		});
		bBasisList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				listBasisFileStatuses();
			}
		});
		bBasisGenNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				bBasisGenNew.setEnabled(false);
				bBasisUpdate.setEnabled(false);
				if(rbGenAllSK.isSelected()){
					for(int i = 0; i < bgmList.length; i++){
						String bgmUC = bgmList[i];
						String bgmLC = bgmList[i].toLowerCase();
						String ksPath = "./../out/ml/"+bgmLC+"/keys_struct.txt";
						FCI fciKS = new FCI(true, ksPath);
						ArrayList<String> keyNums = AhrIO.scanCol(ksPath, ",", fciKS.getIdx("key_num"));
						keyNums.remove(0);
						for(int j = 0; j < keyNums.size(); j++){
							String basisPath = "./../baseis/single/"+bgmLC+"/"+bgmUC+"_"+keyNums.get(j);
							File bfile = new File(basisPath+".txt");
							if(!bfile.exists()){
								BGM_Manager skey = new BGM_Manager(bgmUC, Integer.parseInt(keyNums.get(j)));
								skey.genBasisSK(Integer.parseInt(keyNums.get(j)));							
								System.out.println("--> "+bgmUC+" SK"+keyNums.get(j)+" ... CREATED");
							}
						} 
					}
				}else if(rbGenAllAK.isSelected()){
					FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
					ArrayList<ArrayList<String>> alFC = AhrIO.scanFile("./../baseis/log/ak_log.txt", ",");
					for(int i = 1; i < alFC.size(); i++){
						String knum = alFC.get(i).get(fciAL.getIdx("basis_num"));
						String bgmUC = alFC.get(i).get(fciAL.getIdx("bgm"));
						String bgmLC = bgmUC.toLowerCase();
						if(AhrGen.contains(bgmList, bgmUC)){
							File bfile = new File("./../baseis/aggregated/"+bgmLC+"/"+bgmUC+"_"+knum+".txt");
							if(!bfile.exists()){
								BGM_Manager akey = new BGM_Manager(Integer.parseInt(knum));
								akey.genBasisAK();
								System.out.println("--> "+bgmUC+" AK"+knum+" ... CREATED");
							} 
						}
					}
				}else if(rbGenSingleSK.isSelected()){
					String bgmUC = cbBgmSingleSK.getSelectedItem().toString();
					String bgmLC = bgmUC.toLowerCase();
					String knum = cbKeySingleSK.getSelectedItem().toString();
					File bfile = new File("./../baseis/single/"+bgmLC+"/"+bgmUC+"_"+knum+".txt");
					if(bfile.exists()){
						System.out.println("--> "+bgmUC+" SK"+knum+" basis file already exists.");
					}else{
						BGM_Manager skey = new BGM_Manager(bgmUC, Integer.parseInt(knum));
						skey.genBasisSK(Integer.parseInt(knum));
						System.out.println("--> "+bgmUC+" SK"+knum+" ... CREATED");
					}
				}else if(rbGenSingleAK.isSelected()){
					String knum = cbKeySingleAK.getSelectedItem().toString();
					FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
					String bgmUC = AhrIO.scanCell("./../baseis/log/ak_log.txt", ",", knum, fciAL.getIdx("bgm"));
					String bgmLC = bgmUC.toLowerCase();
					File bfile = new File("./../baseis/aggregated/"+bgmLC+"/"+bgmUC+"_"+knum+".txt");
					if(bfile.exists()){
						System.out.println("--> "+bgmUC+" AK"+knum+" basis file already exists.");	
					}else{
						BGM_Manager akey = new BGM_Manager(Integer.parseInt(knum));
						akey.genBasisAK();
						System.out.println("--> "+bgmUC+" AK"+knum+" ... CREATED");
					}	
				}else{

				}
				//update GUI
				lbBsTot.setText("Total Basis Files           : ... calculating");
				lbBsFull.setText("Fully Updated Basis Files   : ... calculating");
				lbBsLim.setText("Limited Updated Basis Files : ... calculating");
				lbBsNot.setText("Not Updated Basis Files     : ... calculating");
				BasisStatusThread bst = new BasisStatusThread();
				bst.run();
				bBasisGenNew.setEnabled(true);
				bBasisUpdate.setEnabled(true);
			}
		});
		bBasisUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				bBasisGenNew.setEnabled(false);
				bBasisUpdate.setEnabled(false);
				if(rbNoKeys.isSelected()){
					System.out.println("==> No basis files updated.");
				}else if(rbAllKeys.isSelected()){
					ArrayList<ArrayList<String>> allKeys = new ArrayList<ArrayList<String>>();
					// ^^ 1 line contains: 0) SK/AK 1) bgm 2) key ID
					for(int i = 0; i < bgmList.length; i++){
						String bgmLC = bgmList[i].toLowerCase();
						ArrayList<String> skNames = AhrIO.getNamesInPath("./../baseis/single/"+bgmLC+"/");
						for(int j = 0; j < skNames.size(); j++){
							String[] skParts = skNames.get(j).split("_");
							String keyID = skParts[1];
							ArrayList<String> line = new ArrayList<String>();
							line.add("SK");
							line.add(bgmList[i]);
							line.add(keyID);
							allKeys.add(line);
						}
						ArrayList<String> akNames = AhrIO.getNamesInPath("./../baseis/aggregated/"+bgmLC+"/");
						for(int j = 0; j < akNames.size(); j++){
							String keyID = akNames.get(j).split("_")[1];
							ArrayList<String> line = new ArrayList<String>();
							line.add("AK");
							line.add(bgmList[i]);
							line.add(keyID);
							allKeys.add(line);
						}
					}
					System.out.println("***** All Keys *****");
					AhrAL.print(allKeys);
					System.out.println("--> allKeys size = " + allKeys.size());
					for(int i = 0; i < allKeys.size(); i++){
						int keyID = Integer.parseInt(allKeys.get(i).get(2));
						if(allKeys.get(i).get(0).equals("SK")){
							BGM_Manager skey = new BGM_Manager(allKeys.get(i).get(1), keyID);
							skey.updateBasisSK(keyID); 
						}else if(allKeys.get(i).get(0).equals("AK")){
							BGM_Manager akey = new BGM_Manager(keyID);
							akey.updateBasisAK();
						}else{
							System.out.println("ERR: key not recognized in " + allKeys.get(i));
						}
					}
				}else if(rbByEpoch.isSelected()){
					int epochIdx = cbByEpoch.getSelectedIndex();
					FCI fciEP = new FCI(false, "./../in/epochs.txt");
					ArrayList<String> epoch = AhrIO.scanRow("./../in/epochs.txt", ",", epochIdx);
					ArrayList<String> keys = AhrAL.toAL(epoch.get(fciEP.getIdx("keys")).split("~"));
					System.out.println("Epoch Line : " + epoch);
					System.out.println("--> Agg Keys = " + keys);
					for(int i = 0; i < keys.size(); i++){
						int keyID = Integer.parseInt(keys.get(i));
						BGM_Manager akey = new BGM_Manager(keyID);
						akey.updateBasisAK();
					}
				}else if(rbByAK.isSelected()){
					int keyID = Integer.parseInt(cbByAK.getSelectedItem().toString());
					BGM_Manager akey = new BGM_Manager(keyID);
					akey.updateBasisAK();
				}else if(rbBySK.isSelected()){
					String bgmUC = cbBySKBgm.getSelectedItem().toString();
					int keyID = Integer.parseInt(cbBySKKey.getSelectedItem().toString());
					BGM_Manager skey = new BGM_Manager(bgmUC, keyID);
					skey.updateBasisSK(keyID);
				}
				System.out.println("==> Basis Files Updated.");	
				//update GUI
				lbBsTot.setText("Total Basis Files           : ... calculating");
				lbBsFull.setText("Fully Updated Basis Files   : ... calculating");
				lbBsLim.setText("Limited Updated Basis Files : ... calculating");
				lbBsNot.setText("Not Updated Basis Files     : ... calculating");
				BasisStatusThread bst = new BasisStatusThread();
				bst.run();
				bBasisGenNew.setEnabled(true);
				bBasisUpdate.setEnabled(true);
			}
		});
 
		//add
		pBasisGenNew.add(rbGenAllSK);
		pBasisGenNew.add(rbGenAllAK);
		pBasisGenNew.add(rbGenSingleSK);
		pBasisGenNew.add(cbBgmSingleSK);
		pBasisGenNew.add(lbKeySingleSK);
		pBasisGenNew.add(cbKeySingleSK);
		pBasisGenNew.add(rbGenSingleAK);
		pBasisGenNew.add(cbKeySingleAK);
		pBasisGenNew.add(bBasisGenNew);
		pBasisUpdate.add(rbNoKeys);
		pBasisUpdate.add(rbAllKeys);
		pBasisUpdate.add(rbByEpoch);
		pBasisUpdate.add(cbByEpoch);
		pBasisUpdate.add(rbByAK);
		pBasisUpdate.add(cbByAK);
		pBasisUpdate.add(rbBySK);
		pBasisUpdate.add(lbBySKBgm);
		pBasisUpdate.add(cbBySKBgm);
		pBasisUpdate.add(lbBySKKey);
		pBasisUpdate.add(cbBySKKey);
		pBasisUpdate.add(bBasisUpdate);

		this.add(lbBsTot);
		this.add(lbBsFull);
		this.add(lbBsLim);
		this.add(lbBsNot);
		this.add(bBasisList);
		this.add(pBasisGenNew);
		this.add(pBasisUpdate);
		this.setVisible(true);	
	}

	//gets list of single keys given a BGM
	public ArrayList<String> getSKeysByBgm(String bgm){
		String bgmLC = bgm.toLowerCase();
		String fpath =  "./../out/ml/"+bgmLC+"/keys_struct.txt";
		FCI fciKS = new FCI(true, fpath);
		ArrayList<String> keys = AhrIO.scanCol(fpath, ",", fciKS.getIdx("key_num"));
		keys.remove(0);
		return keys;
	}

	//list all  basis (ASK & AK) files and their statues, fully updated, limit update, or not updated
	public void listBasisFileStatuses(){
		System.out.println("========== Calculating Every Key Status ==========");
		ArrayList<ArrayList<String>> fullSK = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> fullAK = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> limSK = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> limAK = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> notSK = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> notAK = new ArrayList<ArrayList<String>>();
		
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		String mrDate = AhrIO.scanCell("./../in/open_dates.txt", ",", 0, 0);
		ArrayList<ArrayList<String>> mstates = AhrIO.scanColWithIndex("./../in/mstates.txt", ",", fciMS.getIdx("ms_mask"));
		//itr thru all SKs
		for(int i = 0; i < bgmList.length; i++){
			String bgmUC = bgmList[i];
			String bgmLC = bgmUC.toLowerCase();
			String ksPath = "./../out/ml/"+bgmLC+"/keys_struct.txt";
			FCI fciKS = new FCI(true, ksPath);
			ArrayList<String> skNums = AhrIO.scanCol(ksPath, ",", fciKS.getIdx("key_num"));
			skNums.remove(0);
			for(int j = 0; j < skNums.size(); j++){
				boolean basis_file_exists = false;
				//find date that is most recent in file
				String mrBasisDate = "";
				String sbPath = "./../baseis/single/"+bgmLC+"/"+bgmUC+"_"+skNums.get(j)+".txt";
				File bfile = new File(sbPath);
				if(bfile.exists()){
					basis_file_exists = true;
					mrBasisDate = AhrIO.scanCell(sbPath, ",", Integer.MAX_VALUE, 0);
				}
				//get market state of this SK
				String msMask = AhrIO.scanCell(ksPath, ",", skNums.get(j), fciKS.getIdx("ms_mask")); 
				//find date that file would have to have to be fully updated
				String fullUpdateDate = "";
				int msIdx = 0;
				while(!AhrGen.compareMasks(msMask, mstates.get(msIdx).get(1))){
					msIdx++;
				}
				fullUpdateDate = mstates.get(msIdx).get(0);
				//check if mr date in basis matches full update date or not
				ArrayList<String> line = new ArrayList<String>();
				line.add(bgmUC);
				line.add(skNums.get(j));
				if(basis_file_exists){
					if(AhrDate.compareDates(fullUpdateDate, mrBasisDate) != -1){
						fullSK.add(line);
					}else{
						limSK.add(line);
					}
				}else{
					notSK.add(line);
				}
			}
		}
		//itr thru all AKs
		ArrayList<ArrayList<String>> aggLog = AhrIO.scanFile("./../baseis/log/ak_log.txt", ",");
		FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
		for(int i = 1; i < aggLog.size(); i++){
			String akNum = aggLog.get(i).get(fciAL.getIdx("basis_num"));
			String bgmUC = aggLog.get(i).get(fciAL.getIdx("bgm"));
			String bgmLC = bgmUC.toLowerCase();
			if(AhrGen.contains(bgmList, bgmUC)){
				ArrayList<String> line = new ArrayList<String>();
				line.add(bgmUC);
				line.add(akNum);
				String abPath = "./../baseis/aggregated/"+bgmLC+"/"+bgmUC+"_"+akNum+".txt";
				File bfile = new File(abPath);
				if(bfile.exists()){
					String mrBasisDate = AhrIO.scanCell(abPath, ",", Integer.MAX_VALUE, 0);
					if(AhrDate.compareDates(mrDate, mrBasisDate) != -1){
						fullAK.add(line);
					}else{
						limAK.add(line);
					}
				}else{
					notAK.add(line);
				}
			}
		}
		//sort all ALs and print out
		class KeyCmp implements Comparator<ArrayList<String>> {
			public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
				if(obj1.get(0).equals(obj2.get(0))){
					double dcomp1 = Double.parseDouble(obj1.get(1));
					double dcomp2 = Double.parseDouble(obj2.get(1));
					return Double.compare(dcomp1, dcomp2);
				}else{
					return obj1.get(0).compareTo(obj2.get(1));
				}
			}
		}
		if(fullSK.size() > 0){
			Collections.sort(fullSK, new KeyCmp());
			System.out.println("==> Fully Updated SK ...");
			for(int i = 0; i < fullSK.size(); i++){
				System.out.println("   "+fullSK.get(i).get(0)+": SK"+fullSK.get(i).get(1));
			}
		}else{
			System.out.println("==> Fully Updated SK ... NONE");
		}
		if(fullAK.size() > 0){
			Collections.sort(fullAK, new KeyCmp());
			System.out.println("==> Fully Updated AK ...");
			for(int i = 0; i < fullAK.size(); i++){
				System.out.println("   "+fullAK.get(i).get(0)+": AK"+fullAK.get(i).get(1));
			}
		}else{
			System.out.println("==> Fully Updated AK ... NONE");
		}
		if(limSK.size() > 0){
			Collections.sort(limSK, new KeyCmp());
			System.out.println("==> Limited Updated SK ...");
			for(int i = 0; i < limSK.size(); i++){
				System.out.println("   "+limSK.get(i).get(0)+": SK"+limSK.get(i).get(1));
			}
		}else{
			System.out.println("==> Limited Updated SK ... NONE");
		}
		if(limAK.size() > 0){
			Collections.sort(limAK, new KeyCmp());
			System.out.println("==> Limited Updated AK ...");
			for(int i = 0; i < limAK.size(); i++){
				System.out.println("   "+limAK.get(i).get(0)+": AK"+limAK.get(i).get(1));
			}
		}else{
			System.out.println("==> Limited Updated AK ... NONE");
		}
		if(notSK.size() > 0){
			Collections.sort(notSK, new KeyCmp());
			System.out.println("==> Not Updated SK ...");
			for(int i = 0; i < notSK.size(); i++){
				System.out.println("   "+notSK.get(i).get(0)+": SK"+notSK.get(i).get(1));
			}
		}else{
			System.out.println("==> Not Updated SK ... NONE");
		}
		if(notAK.size() > 0){
			Collections.sort(notAK, new KeyCmp());
			System.out.println("==> Not Updated AK ...");
			for(int i = 0; i < notAK.size(); i++){
				System.out.println("   "+notAK.get(i).get(0)+": AK"+notAK.get(i).get(1));
			}
		}else{
			System.out.println("==> Not Updated AK ... NONE");
		}
		
	}

	//gets num of basis files that fit each category (fully, limited, not updated)
	public ArrayList<Integer> getBasisFileStatuses(){
		ArrayList<Integer> stats = new ArrayList<Integer>();
		int cFull = 0;
		int cLim = 0;
		int cNot = 0;
		String mrDate = AhrIO.scanCell("./../in/open_dates.txt", ",", 0, 0);
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		ArrayList<ArrayList<String>> mstates = AhrIO.scanColWithIndex("./../in/mstates.txt", ",", fciMS.getIdx("ms_mask"));
		//count SKs
		for(int i = 0; i < bgmList.length; i++){
			String bgmUC = bgmList[i];
			String bgmLC = bgmUC.toLowerCase();
			String ksPath = "./../out/ml/"+bgmLC+"/keys_struct.txt";
			FCI fciKS = new FCI(true, ksPath);
			ArrayList<String> skNums = AhrIO.scanCol(ksPath, ",", fciKS.getIdx("key_num"));
			skNums.remove(0);
			for(int j = 0; j < skNums.size(); j++){
				boolean basis_file_exists = false;
				//find date that is most recent in file
				String mrBasisDate = "";
				String sbPath = "./../baseis/single/"+bgmLC+"/"+bgmUC+"_"+skNums.get(j)+".txt";
				File bfile = new File(sbPath);
				if(bfile.exists()){
					basis_file_exists = true;
					mrBasisDate = AhrIO.scanCell(sbPath, ",", Integer.MAX_VALUE, 0);
				}
				//get market state of this SK
				String msMask = AhrIO.scanCell(ksPath, ",", skNums.get(j), fciKS.getIdx("ms_mask")); 
				//find date that file would have to have to be fully updated
				String fullUpdateDate = "";
				int msIdx = 0;
				while(!AhrGen.compareMasks(msMask, mstates.get(msIdx).get(1))){
					msIdx++;
				}
				fullUpdateDate = mstates.get(msIdx).get(0);
				//check if mr date in basis matches full update date or not
				if(basis_file_exists){
					if(AhrDate.compareDates(fullUpdateDate, mrBasisDate) != -1){
						cFull++;
					}else{
						cLim++;
					}
				}else{
					cNot++;
				}
			}
		}
		//count AKs
		ArrayList<ArrayList<String>> aggLog = AhrIO.scanFile("./../baseis/log/ak_log.txt", ",");
		FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
		for(int i = 1; i < aggLog.size(); i++){
			String akNum = aggLog.get(i).get(fciAL.getIdx("basis_num"));
			String bgmUC = aggLog.get(i).get(fciAL.getIdx("bgm"));
			String bgmLC = bgmUC.toLowerCase();
			if(AhrGen.contains(bgmList, bgmUC)){
				String abPath = "./../baseis/aggregated/"+bgmLC+"/"+bgmUC+"_"+akNum+".txt";
				File bfile = new File(abPath);
				if(bfile.exists()){
					String mrBasisDate = AhrIO.scanCell(abPath, ",", Integer.MAX_VALUE, 0);
					if(AhrDate.compareDates(mrDate, mrBasisDate) != -1){
						cFull++;
					}else{
						cLim++;
					}
				}else{
					cNot++;
				}
			}
		}
		stats.add(cFull+cLim+cNot);
		stats.add(cFull);
		stats.add(cLim);
		stats.add(cNot);
		return stats;
	}
	public ArrayList<Double> getBasisFilePercents(ArrayList<Integer> bstatuses){
		ArrayList<Double> bpercents = new ArrayList<Double>();
		for(int i = 1; i < bstatuses.size(); i++){
			double perc = ((double)bstatuses.get(i)/(double)bstatuses.get(0)) * 100.0;
			bpercents.add(perc);
		}
		return bpercents;
	}

}
