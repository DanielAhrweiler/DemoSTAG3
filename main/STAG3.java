import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrDate;
import ahrweiler.util.AhrDTF;
import ahrweiler.util.AhrGen;
import ahrweiler.Globals;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import ahrweiler.support.SQLCode;
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
import ahrweiler.gui.TableSortPanel;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.channels.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.*;

public class STAG3 {

	public static void main(String[] args){
		STAG3 stag3 = new STAG3();
		stag3.init();
		stag3.mainGUI();
	}

	public void init(){
		System.out.print("--> Initializing Data ... ");
		//remove lines from rnd keys_Struct & keys_perf
		String ksPath = AhrIO.uniPath("./../out/sk/log/rnd/keys_struct.txt");
		String kpPath = AhrIO.uniPath("./../out/sk/log/rnd/keys_perf.txt");
		ArrayList<String> ksRow = AhrIO.scanRow(ksPath, ",", 0);
		ArrayList<String> kpRow = AhrIO.scanRow(kpPath, ",", 0);
		ArrayList<ArrayList<String>> ksFile = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> kpFile = new ArrayList<ArrayList<String>>();
		ksFile.add(ksRow);
		kpFile.add(kpRow);
		AhrIO.writeToFile(ksPath, ksFile, ",");
		AhrIO.writeToFile(kpPath, kpFile, ",");
		//remove rnd tmp basis files
		String rbPath = AhrIO.uniPath("./../out/sk/baseis/rnd/");
		ArrayList<String> rndFiles = AhrIO.getFilesInPath(rbPath);
		for(int i = 0; i < rndFiles.size(); i++){
			File file = new File(rbPath+rndFiles.get(i));
			if(file.exists()){
				file.delete();
			}
		}
		//set sk_attrs.txt to default values
		AttributesSK kattr = new AttributesSK();
		kattr.saveToFile(AhrIO.uniPath("./../data/tmp/sk_attrs.txt"));
		System.out.println("DONE");
	}

	public void mainGUI(){
		//component initialization
		JFrame mframe = new JFrame();
		mframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mframe.setTitle("STAG 3");
		mframe.setSize(275, 600);
		mframe.setLocationRelativeTo(null);
		mframe.setLayout(null);
		JLabel lbDB = new JLabel("Database");
		JButton bCharting = new JButton("Stock & Indicator Charting");
		JButton bFilter = new JButton("Stock Filter");
		JLabel lbML = new JLabel("Machine Learning");
		JButton bCreateSK = new JButton("Create SK");
		JButton bCreateAK = new JButton("Create AK");
		JButton bBasis = new JButton("SK, AK, & Basis Info");
		JLabel lbPA = new JLabel("Post Analysis");
		JButton bBuyInOpt = new JButton("BIM/SOM Optimization");
		JButton bKeyPerf = new JButton("Key Performance");
		JButton bAutoDemo = new JButton("Run Automated Demo");
		JButton bOther = new JButton("Other");

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
		bAutoDemo.setBounds(10, 430, 250, 45);
		bOther.setBounds(10, 480, 250, 45);
	
		//change look of the buttons
		setButtonStyle(bCharting);
		setButtonStyle(bFilter);
		setButtonStyle(bCreateSK);
		setButtonStyle(bCreateAK);
		setButtonStyle(bBasis);
		setButtonStyle(bBuyInOpt);
		setButtonStyle(bKeyPerf);
		setButtonStyle(bAutoDemo);
		setButtonStyle(bOther);

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
		bAutoDemo.addActionListener(new ActionListener() {
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
		mframe.add(bAutoDemo);
		mframe.add(bOther);
		mframe.setVisible(true);
	}

	//GUI related, set specific style for a JButton
	public void setButtonStyle(JButton btn){
		Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}


/*========================================================================================
	Other Functions
=========================================================================================*/

	//for all other functions outside the GUI
	public void other(){
		Scanner scanner = new Scanner(System.in);
		System.out.print("==> Pick Option: \n  1) Temp Code"+
										  "\n  2) Update score_percentiles.txt"+
										  "\n  3) Count All Market States"+
										  "\n  4) Update ./in/nar_by_date.txt"+
						"\nEnter: ");
		int pick = Integer.parseInt(scanner.nextLine());
		if(pick == 1){
			tempCode();
		}else if(pick == 2){
			updateScorePercentiles();
		}else if(pick == 3){
			countMarketStates();
		}else if(pick == 4){
			updateNarByDate();
		}else{
			System.out.println("Invalid Option, Try Again.");
			other();
		}
	}

	public void tempCode(){
		//fix null vals in MySQL bydate DB
		SQLCode sqlc = new SQLCode("aws");
		sqlc.fixNullInByDate();

		System.out.println("--> tempCode() DONE");
	}

	//create line in ./../out/SAP/score_percentiles.txt for all SKs in all epoch AKs
	public void updateScorePercentiles(){
		String epPath = AhrIO.uniPath("./../in/epochs.txt");
		String spPath = AhrIO.uniPath("./../out/score_percentiles.txt");
		String laPath = AhrIO.uniPath("./../out/ak/log/ak_log.txt");
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
		//System.out.println("--> All SKs not in score_percentiles.txt");
		//AhrAL.print(allSK);
		//itr thru all new SKs, write percentiles to file
		ArrayList<ArrayList<String>> tf = AhrIO.scanFile(spPath, ",");
		String skbPath = AhrIO.uniPath("./../out/sk/baseis/");
		FCI fciSKB = new FCI(false, skbPath);
		for(int i = 0; i < allSK.size(); i++){
			String bgm = allSK.get(i).get(0);
			String skNum = allSK.get(i).get(1);
			String sdate = allSK.get(i).get(2);
			String edate = allSK.get(i).get(3);
			String call = allSK.get(i).get(4);
			ArrayList<Double> scores = new ArrayList<Double>();
			String skbPathFull = AhrIO.uniPath(skbPath+bgm+"/"+bgm+"_"+skNum+".txt");
			ArrayList<ArrayList<String>> skBasis = AhrIO.scanFile(skbPathFull, ",");
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
		AhrIO.writeToFile(spPath, tf, ",");
		String message = "File score_percentiles.txt has been update.";
		JOptionPane.showMessageDialog(null, message, "Update", JOptionPane.PLAIN_MESSAGE);
	}



	//counts hm instances of all market states and writes to file
	public void countMarketStates(){
		System.out.print("--> Counting all market states in mstates.txt ... ");
		String msPath = AhrIO.uniPath("./../in/mstates.txt");
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
		AhrIO.writeToFile(AhrIO.uniPath("./../out/ms_count.txt"), allStates, ",");
		System.out.println("DONE");
	}

	//updates ./../in/nar_by_date.txt which show which tickers have 1111 NAR mask by date
	public void updateNarByDate(){
		String nar = "1111";
		FCI fciSN = new FCI(false, Globals.snorm_path);
		SQLCode sqlc = new SQLCode();
		sqlc.setDB("snorm");
		ArrayList<String> snTables = sqlc.getTables();
		ArrayList<String> dates = AhrIO.scanCol(AhrIO.uniPath("./../in/open_dates.txt"), ",", 0);
		ArrayList<ArrayList<String>> narByDate = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < dates.size(); i++){
			String itrDate = dates.get(i);
			ArrayList<String> tickList = new ArrayList<String>();
			for(int j = 0; j < snTables.size(); j++){
				ArrayList<String> line = sqlc.selectRow(snTables.get(j), AhrAL.toAL(new String[]{"*"}), dates.get(i), true);
				boolean pass_nar = false;
				if(line.size() == fciSN.getTags().size()){
					String itrNar = line.get(fciSN.getIdx("nar_mask"));
					if(AhrGen.compareMasks(nar, itrNar)){
						pass_nar = true;
					}
				}
				if(pass_nar){
					tickList.add(snTables.get(j));
				}
			}
			ArrayList<String> nbdLine = tickList;
			nbdLine.add(0, dates.get(i));
			narByDate.add(nbdLine);
		}
		AhrIO.writeToFile(AhrIO.uniPath("./../in/nar_by_date.txt"), narByDate, ",");
	}
}
