import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrDate;
import ahrweiler.util.AhrDTF;
import ahrweiler.util.AhrGen;
import ahrweiler.Globals;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import ahrweiler.support.OrderSim;
import ahrweiler.support.StockFilter;
import ahrweiler.bgm.ANN;
import ahrweiler.bgm.BGM_Manager;
import ahrweiler.bgm.AttributesSK;
import ahrweiler.gui.DB_Charting;
import ahrweiler.gui.DB_Filter;
import ahrweiler.gui.DB_DataIntegrity;
import ahrweiler.gui.ML_CreateSK;
import ahrweiler.gui.ML_CreateAK;
import ahrweiler.gui.ML_Basis;
import ahrweiler.gui.PA_BimSomOpt;
import ahrweiler.gui.PA_KeyPerf;
import ahrweiler.gui.AutoDemo;
import ahrweiler.gui.AD_Params;
import ahrweiler.gui.AD_Acronyms;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.channels.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class STAG3 {

	public static void main(String[] args){
		STAG3 stag3 = new STAG3();
		stag3.init();
		stag3.mainGUI();
	}

	public void init(){
		System.out.print("--> Initializing Data ... ");
		//remove lines from rnd keys_perf
		String kpPath = "./../out/sk/log/rnd/keys_perf.txt";
		ArrayList<String> kpLine = AhrIO.scanRow(kpPath, ",", 0);
		ArrayList<ArrayList<String>> kpFile = new ArrayList<ArrayList<String>>();
		kpFile.add(kpLine);
		AhrIO.writeToFile(kpPath, kpFile, ",");
		//remove rnd tmp basis files
		String rbPath = "./../out/sk/baseis/rnd/";
		ArrayList<String> rndFiles = AhrIO.getFilesInPath(rbPath);
		for(int i = 0; i < rndFiles.size(); i++){
			File file = new File(rbPath+rndFiles.get(i));
			if(file.exists()){
				file.delete();
			}
		}
		System.out.println("DONE");
	}

	public void mainGUI(){
		//component initialization
		JFrame mframe = new JFrame();
		mframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mframe.setTitle("STAG 3");
		mframe.setSize(275, 610);
		mframe.setLocationRelativeTo(null);
		mframe.setLayout(null);
		JLabel lbDB = new JLabel("Database");
		Button bCharting = new Button("Stock & Indicator Charting");
		Button bFilter = new Button("Stock Filter");
		JLabel lbML = new JLabel("Machine Learning");
		Button bCreateSK = new Button("Create SK");
		Button bCreateAK = new Button("Create AK");
		Button bBasis = new Button("Basis Files");
		JLabel lbPA = new JLabel("Post Analysis");
		Button bBuyInOpt = new Button("BIM/SOM Optimization");
		Button bKeyPerf = new Button("Key Performance");
		Button bDemo = new Button("Run Automated Demo");
		Button bOther = new Button("Other");

		//component bounds
		lbDB.setBounds(10, 20, 160, 35);
		bCharting.setBounds(40, 60, 200, 35);
		bFilter.setBounds(40, 100, 200, 35);
		lbML.setBounds(10, 140, 160, 35);
		bCreateSK.setBounds(40, 180, 200, 35);
		bCreateAK.setBounds(40, 220, 200, 35);
		bBasis.setBounds(40, 260, 200, 35);
		lbPA.setBounds(10, 300, 160, 35);
		bBuyInOpt.setBounds(40, 340, 200, 35);
		bKeyPerf.setBounds(40, 380, 200, 35);
		bDemo.setBounds(10, 430, 250, 45);
		bOther.setBounds(10, 480, 250, 45);

		//component funtionality
		bCharting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				DB_Charting dbchart = new DB_Charting();
			}
		});
		bFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				DB_Filter dbfilter = new DB_Filter();
			}
		});
		bCreateSK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ML_CreateSK mlsk = new ML_CreateSK();
			}
		});
		bCreateAK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ML_CreateAK mlak = new ML_CreateAK();
			}
		});
		bBasis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ML_Basis mlbasis = new ML_Basis();
			}
		});
		bBuyInOpt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				PA_BimSomOpt pabim = new PA_BimSomOpt();
			}
		});
		bKeyPerf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				PA_KeyPerf paperf = new PA_KeyPerf();
			}
		});
		bDemo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				AutoDemo demo = new AutoDemo();
				//TestSwing tswing = new TestSwing();
			}
		});
		bOther.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				other();	
			}
		});
		
		mframe.add(lbDB);
		mframe.add(bCharting);
		mframe.add(bFilter);
		mframe.add(lbML);
		mframe.add(bCreateSK);
		mframe.add(bCreateAK);
		mframe.add(bBasis);
		mframe.add(lbPA);
		mframe.add(bBuyInOpt);
		mframe.add(bKeyPerf);
		mframe.add(bDemo);
		mframe.add(bOther);
		mframe.setVisible(true);
	}


/*========================================================================================
	Other Functions
=========================================================================================*/

	//for all other functions outside the GUI
	public void other(){
		Scanner scanner = new Scanner(System.in);
		System.out.print("==> Pick Option: \n  1) Temp Code"+
										  "\n  2) Update score_percentiles.txt"+
										  "\n  3) Check Extreme Relative SMA Vals"+
										  "\n  4) Check Health of Main/Intrinio/"+
										  "\n  5) Count All Market States"+
						"\nEnter: ");
		int pick = Integer.parseInt(scanner.nextLine());
		if(pick == 1){
			tempCode();
		}else if(pick == 2){
			updateScorePercentiles();
		}else if(pick == 3){
			checkRelativeSMAs();
		}else if(pick == 4){
			checkIntrinioFolderHealth();
		}else if(pick == 5){
			countMarketStates();
		}else{
			System.out.println("Invalid Option, Try Again.");
			other();
		}
	}

	public void tempCode(){
		ArrayList<String> test = AhrAL.toAL(new String[]{"rnd1", "rnd55", "rnd3", "rnd2", "rnd8"});
		Collections.sort(test);
		System.out.println(test);

		/*
		//test diff in basis files (cl before pred) and orderlist (open after pred)
		int skNum = 4;
		String bsPath = "./../out/sk/baseis/ann/ANN_"+String.valueOf(skNum)+".txt";
		String olPath = "./../data/tmp/os_orderlist.txt";
		FCI fciBS = new FCI(false, bsPath);
		FCI fciOL = new FCI(false, olPath);
		String ttvMask = "100";
		String colName = "appr3";
		ArrayList<ArrayList<String>> basis = AhrIO.scanFile(bsPath, ",");
		//have to run orderlist 1st to create file
		OrderSim osim = new OrderSim("ANN", skNum);
		osim.setDateRange("2016-01-01", "2020-12-31");
		osim.setBIM(0.01);
		osim.setSOM(50.0);
		osim.setTtvMask(ttvMask);
		osim.setMaxOrderSize(10000.0);
		osim.calcOrderList();
		try{
			Thread.sleep(500);
		}catch(InterruptedException e){
			System.out.println("ERR: thread interrupted.");
		}
		ArrayList<ArrayList<String>> orderlist = AhrIO.scanFile("./../data/tmp/os_orderlist_byappr.txt", ",");
		//get appr % col from basis and orderlist
		ArrayList<ArrayList<String>> capprs = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < orderlist.size(); i++){
			String olDate = orderlist.get(i).get(fciOL.getIdx("date"));
			String olTick = orderlist.get(i).get(fciOL.getIdx("ticker"));
			for(int j = 0; j < basis.size(); j++){
				String bsDate = basis.get(j).get(fciBS.getIdx("date"));
				String bsTick = basis.get(j).get(fciBS.getIdx("ticker"));
				if(olDate.equals(bsDate) && olTick.equals(bsTick)){
					String bsAppr = basis.get(j).get(fciBS.getIdx(colName)); 
					orderlist.get(i).add(bsAppr);
					break;
				}
			}
		}
		AhrIO.writeToFile("./../data/tmp/tmp_capprs.csv", orderlist, ",");
		*/
		

		System.out.println("--> tempCode() DONE");
	}

	//calc clean lines that have tbd
	public void updateCleanFiles(){
		BGM_Manager bgmm = new BGM_Manager();
		ArrayList<ArrayList<String>> tf = new ArrayList<ArrayList<String>>();
		ArrayList<String> bdFiles = AhrIO.getNamesInPath("./../../DB_Intrinio/Clean/ByDate/");
		for(int i = 0; i < bdFiles.size(); i++){
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile("./../../DB_Intrinio/Clean/ByDate/"+bdFiles.get(i)+".txt", "~");
			for(int j = 0; j < fc.size(); j++){
				if(fc.get(j).contains("tbd")){
					//add old line from Clean/ByDate
					ArrayList<String> oldLine = fc.get(j);
					oldLine.add(0, bdFiles.get(i));
					tf.add(oldLine);
					//add new line from BGM_Manager
					String ticker = oldLine.get(1);
					String date = bdFiles.get(i);
					ArrayList<String> newLine = bgmm.calcCleanLine(ticker, date, false, false);
					newLine.add(0, bdFiles.get(i));
					tf.add(newLine);
				}
			}
		}
		AhrIO.writeToFile("./../out/clean_tbd.txt", tf, "~");
	}

	//create line in ./../out/SAP/score_percentiles.txt for all SKs in all epoch AKs
	public void updateScorePercentiles(){
		String epPath = "./../in/epochs.txt";
		String spPath = "./../out/score_percentiles.txt";
		String laPath = "./../out/ak/log/ak_log.txt";
		FCI fciEP = new FCI(false, epPath);
		FCI fciLA = new FCI(true, laPath);
		ArrayList<ArrayList<String>> epochFile = AhrIO.scanFile(epPath, ",");
		ArrayList<ArrayList<String>> scoreFile = AhrIO.scanFile(spPath, ",");
		ArrayList<ArrayList<String>> logFile = AhrIO.scanFile(laPath, ",");
		//get all epoch AKs
		ArrayList<String> allAK = new ArrayList<String>();
		for(int i = 0; i < epochFile.size(); i++){
			String[] keys = epochFile.get(i).get(fciEP.getIdx("keys")).split("~");
			for(int j = 0; j < keys.length; j++){
				if(!allAK.contains(keys[j])){
					allAK.add(keys[j]);
				}
			}
		}	
		//get all uniq SK from all epoch AKs
		ArrayList<ArrayList<String>> allSK = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < allAK.size(); i++){
			int laIdx = -1;
			for(int j = 0; j < logFile.size(); j++){
				String itrAK = logFile.get(j).get(fciLA.getIdx("ak_num"));
				if(itrAK.equals(allAK.get(i))){
					laIdx = j;
					break;
				}
			}
			String[] skeys = logFile.get(laIdx).get(fciLA.getIdx("best_keys")).split("~");
			for(int j = 0; j < skeys.length; j++){
				ArrayList<String> line = new ArrayList<String>();
				line.add(logFile.get(laIdx).get(fciLA.getIdx("bgm")));
				line.add(skeys[j]);
				line.add(logFile.get(laIdx).get(fciLA.getIdx("start_date")));
				line.add(logFile.get(laIdx).get(fciLA.getIdx("end_date")));
				line.add(logFile.get(laIdx).get(fciLA.getIdx("call")));
				boolean is_repeat_sk = false;
				for(int k = 0; k < allSK.size(); k++){
					if(allSK.get(0).equals(line.get(0)) && allSK.get(1).equals(line.get(1))){
						is_repeat_sk = true;
						break;
					}
				}
				if(!is_repeat_sk){
					allSK.add(line);
				}
			}
		}
		System.out.println("--> All SKs not in score_percentiles.txt");
		AhrAL.print(allSK);
		//itr thru all new SKs, write percentiles to file
		ArrayList<ArrayList<String>> tf = AhrIO.scanFile("./../out/score_percentiles.txt", ",");
		String skbPath = "./../out/sk/baseis/";
		FCI fciSKB = new FCI(false, skbPath);
		for(int i = 0; i < allSK.size(); i++){
			String bgm = allSK.get(i).get(0);
			String skNum = allSK.get(i).get(1);
			String sdate = allSK.get(i).get(2);
			String edate = allSK.get(i).get(3);
			String call = allSK.get(i).get(4);
			ArrayList<Double> scores = new ArrayList<Double>();
			ArrayList<ArrayList<String>> skBasis = AhrIO.scanFile(skbPath+bgm+"/"+bgm+"_"+skNum+".txt", ",");
			for(int j = 0; j < skBasis.size(); j++){
				String itrDate = skBasis.get(j).get(fciSKB.getIdx("date"));
				if(AhrDate.isDateInPeriod(itrDate, sdate, edate)){
					scores.add(Double.parseDouble(skBasis.get(j).get(fciSKB.getIdx("score"))));
				}
			}
			//sort scores and calc percentiles (GAB3 and long ANNs are higher = better)
			ArrayList<Double> ptiles = new ArrayList<Double>();
			Collections.sort(scores);
			if(bgm.equals("GAB3") || (bgm.equals("ANN") && call.equals("1"))){
				Collections.reverse(scores);
			}
			int stepSize = scores.size() / 100;
			for(int j = 0; j < scores.size(); j++){
				if(j%stepSize == 0){
					ptiles.add(scores.get(j));
				}
			}
			//add line to toFile struct
			ArrayList<String> line = new ArrayList<String>();
			line.add(bgm);
			line.add(skNum);
			for(int j = 0; j < ptiles.size(); j++){
				line.add(String.valueOf(ptiles.get(j)));
			}
			tf.add(line);
		}
		AhrIO.writeToFile("./../out/score_percentiles.txt", tf, ",");
		System.out.println("--> score_precentiles.txt ... UPDATED");
	}

	//checks health of rel SMAs S_Norm (ind vals 1-8)
	public void checkRelativeSMAs(){
		String bdPath = "./../../DB_Intrinio/Clean/ByDate/";
		String snPath = "./../../DB_Intrinio/Main/S_Norm/";
		FCI fciBD = new FCI(false, bdPath);
		FCI fciSN = new FCI(false, snPath);
		ArrayList<String> bdFiles = AhrIO.getNamesInPath(bdPath);
		AhrDate.sortDates(bdFiles, true);
		ArrayList<String> snFiles = AhrIO.getNamesInPath(snPath);
		HashMap<String, Integer> bdCount = new HashMap<String, Integer>();
		HashMap<String, Integer> snCount = new HashMap<String, Integer>();
		//count ByDate
		int totCountBD = 0;
		for(int i = 0; i < bdFiles.size(); i++){
			int count = 0;
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile(bdPath+bdFiles.get(i)+".txt", "~");
			for(int j = 0; j < fc.size(); j++){
				totCountBD++;
				for(int k = 1; k <= 8; k++){
					int itrAttr = Integer.parseInt(fc.get(j).get(k));
					if(itrAttr < 100 || itrAttr > 65435){
						count++;
						break;
					}
				}
			}
			bdCount.put(bdFiles.get(i), count);
		}
		//count S_Norm
		int totCountSN = 0;
		for(int i = 0; i < snFiles.size(); i++){
			int count = 0;
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile(snPath+snFiles.get(i)+".txt", "~");
			for(int j = 0; j < fc.size(); j++){
				totCountSN++;
				String itrDate = fc.get(j).get(fciSN.getIdx("date"));
				for(int k = 1; k <= 8; k++){
					int itrAttr = 20000;
					if(!fc.get(j).get(k).equals("NA")){
						itrAttr = Integer.parseInt(fc.get(j).get(k));
					}
					if(itrAttr < 100 || itrAttr > 65435){
						if(snCount.containsKey(itrDate)){
							snCount.put(itrDate, snCount.get(itrDate) + 1);
						}else{
							snCount.put(itrDate, 1);
						}
						break;
					}
				}
			}
		}
		//get all dates and combine ByDate counts and S_Norm counts
		ArrayList<ArrayList<String>> tf = new ArrayList<ArrayList<String>>();
		ArrayList<String> allDates = AhrDate.getDatesBetween("2002-01-01", AhrDate.getTodaysDate());
		for(int i = 0; i < allDates.size(); i++){
			int countBD = 0;
			int countSN = 0;
			if(bdCount.containsKey(allDates.get(i))){
				countBD = bdCount.get(allDates.get(i));
			}
			if(snCount.containsKey(allDates.get(i))){
				countSN = snCount.get(allDates.get(i));
			}
			ArrayList<String> line = new ArrayList<String>();
			line.add(allDates.get(i));
			line.add(String.valueOf(countBD));
			line.add(String.valueOf(countSN));
			tf.add(line);
		}
		AhrIO.writeToFile("./../out/snorm_vs_bydate.txt", tf, ",");

		System.out.println("--> Lines Analyzed In ByDate : "+totCountBD);
		System.out.println("--> Lines Analyzed In S_Norm : "+totCountSN);
	}

	//check DB_Intrinio/Main/Intrinio/ for abnormalities
	public void checkIntrinioFolderHealth(){
		String itPath = "./../../DB_Intrinio/Main/Intrinio/";
		String cpPath = "./../in/scraper/company_profile.txt";
		FCI fciIT = new FCI(false, itPath);
		FCI fciCP = new FCI(false, cpPath);
		ArrayList<String> itTicks = AhrIO.getNamesInPath(itPath);
		ArrayList<ArrayList<String>> cpData = AhrIO.scanFile(cpPath, "~");
		String mrDate = AhrDate.closestDate(AhrDate.getTodaysDate());
		int totBelowLim = 0;
		int totOutOfDate = 0;
		int totInComProfButBad = 0;
		int totNotInComProf = 0;
		int totTicksUsed = 0;
		int totLinesUsed = 0;
		int totLinesNotUsed = 0;
		for(int i = 0; i < itTicks.size(); i++){
			boolean will_be_used_in_sbase = true;
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile(itPath+itTicks.get(i)+".txt", "~");
			//check file size
			if(fc.size() < Globals.min_file_size){
				totBelowLim++;
				will_be_used_in_sbase = false;
			}
			//check out of date
			String mrFileDate = fc.get(0).get(fciIT.getIdx("date"));
			if(!mrDate.equals(mrFileDate)){
				totOutOfDate++;
				will_be_used_in_sbase = false;
			}
			//check if in company_profile (if yes check if BadT)
			int cpIdx = AhrAL.getRowIdx(cpData, itTicks.get(i));
			if(cpIdx != -1){
				String itrGoodBad = cpData.get(cpIdx).get(fciCP.getIdx("good_bad"));
				if(itrGoodBad.equals("BadT")){
					totInComProfButBad++;
					will_be_used_in_sbase = false;
				}
			}else{
				totNotInComProf++;
				will_be_used_in_sbase = false;
			}
			//tally up lines
			if(will_be_used_in_sbase){
				totTicksUsed++;
				totLinesUsed += fc.size();
			}else{
				totLinesNotUsed += fc.size();
			}
		}
		System.out.println("--> Total Files in Main/Intrinio/ : "+itTicks.size()+
							"\n    -> Tot Below Limit : " + totBelowLim +
							"\n    -> Tot Out of Date : " + totOutOfDate +
							"\n    -> Tot In ComProf & Bad : " + totInComProfButBad +
							"\n    -> Tot Not In ComProf  : " + totNotInComProf +
							"\n    -> Tot Ticks Used In SBase : " + totTicksUsed + 
							"\n    -> Tot Lines Used In SBase : " + totLinesUsed +
							"\n    -> Tot Lines Not Used In SBase : " + totLinesNotUsed);
	}

	//counts hm instances of all market states and writes to file
	public void countMarketStates(){
		System.out.print("--> Counting all market states in mstates.txt ... ");
		String msPath = "./../in/mstates.txt";
		FCI fciMS = new FCI(false, msPath);
		ArrayList<String> mstates = AhrIO.scanCol(msPath, ",", fciMS.getIdx("ms_mask"));
		//save powers of 3	
		int pow1 = (int)Math.pow(3, 1);		// 3
		int pow2 = (int)Math.pow(3, 2);		// 9
		int pow3 = (int)Math.pow(3, 3);		// 27
		int pow4 = (int)Math.pow(3, 4);		// 81
		int pow5 = (int)Math.pow(3, 5);		// 243
		int pow6 = (int)Math.pow(3, 6);		// 729
		int pow7 = (int)Math.pow(3, 7);		// 2187
		int pow8 = (int)Math.pow(3, 8);		// 6561
		//create all possible values for mstates (including x vals)
		ArrayList<ArrayList<String>> allStates = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < pow8; i++){
			int bm1 = (i % pow8) / pow7;
			int bm2 = (i % pow7) / pow6;
			int bm3 = (i % pow6) / pow5;
			int bm4 = (i % pow5) / pow4;
			int bm5 = (i % pow4) / pow3;
			int bm6 = (i % pow3) / pow2;
			int bm7 = (i % pow2) / pow1;
			int bm8 = (i % pow1);
			ArrayList<String> line = new ArrayList<String>();
			String bmAll = String.valueOf(bm1) + String.valueOf(bm2) + String.valueOf(bm3) + String.valueOf(bm4) +
							String.valueOf(bm5) + String.valueOf(bm6) + String.valueOf(bm7) + String.valueOf(bm8);
			bmAll = bmAll.replaceAll("2", "x");
			line.add(bmAll);
			allStates.add(line);
		}	
		//itr thru all combos, counting instances in mstates.txt
		for(int i = 0; i < allStates.size(); i++){
			int count = 0;
			String itrMS = allStates.get(i).get(0);
			for(int j = 0; j < mstates.size(); j++){
				if(AhrGen.compareMasks(itrMS, mstates.get(j))){
					count++;
				}
			}
			allStates.get(i).add(String.valueOf(count));
		}
		//sort allStates, most count ot least count
		Collections.sort(allStates, new Comparator<ArrayList<String>>(){
			@Override
			public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
				double dcomp1 = Double.parseDouble(obj1.get(1));
				double dcomp2 = Double.parseDouble(obj2.get(1));
				return (Double.compare(dcomp1, dcomp2) * -1);
			}
		});
		//write allStates to file
		AhrIO.writeToFile("./../out/ms_count.txt", allStates, ",");
		System.out.println("DONE");
	}
}
