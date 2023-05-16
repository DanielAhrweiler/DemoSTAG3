package ahrweiler.bgm;
import ahrweiler.Globals;
import ahrweiler.util.*;
import ahrweiler.bgm.ANN;
import ahrweiler.bgm.ann.Node;
import ahrweiler.bgm.ann.Network;
import ahrweiler.support.FCI;
import ahrweiler.support.OrderSim;
import java.util.*;

//Buy-In & Order List Manager
//over arching struct that allows you track and manipulate all BGM basis and order list info and files

public class BGM_Manager {

	private String bgm;				//basis generating method
	private boolean is_ak;			//is AK (or SK if false)
	private int id;					//id num for either the AK or SK
	private int focusSK;			//SK that class is currently focused on (has vals stored)
	private int[] bestSK;			//best SKs that provide 100% cov of an AK
	private double[][] skBuyIn;		//opt BIM/SOM pair for every SK
	private double[] akBuyIn;		//opt BIM/SOM pair for 1 AK
	private ArrayList<ArrayList<String>> statesSK;	//all best SK and their ms states

	//ANN ann;
	AttributesSK kattr;

	//contructors
	public BGM_Manager(AttributesSK skAttrs){//just need sk attrs, useful if RND
		this.kattr = skAttrs;
	}
	public BGM_Manager(String bgmVal, int idVal){//if SK, need BGM and basis num (id)
		this.bgm = bgmVal.toLowerCase();
		this.is_ak = false;
		this.id = idVal;	
		setFocusSK(idVal);
	}	
	public BGM_Manager(int idVal){//if is AK just need basis num (id)
		this.is_ak = true;
		this.id = idVal;
		//get info from agg basis log file
		String laPath = "./../out/ak/log/ak_log.txt";
		FCI fciLA = new FCI(true, laPath);
		ArrayList<String> laRow = AhrIO.scanRow(laPath, ",", String.valueOf(idVal));
		if(laRow.size() < 1){
			System.out.println("ERR: Row "+idVal+" not found in ak_log.txt");
		}
		this.bgm = laRow.get(fciLA.getIdx("bgm")).toLowerCase();
		//set list of best single keys for this agg key
		String[] bestSKstr = laRow.get(fciLA.getIdx("best_keys")).split("~");
		this.bestSK = new int[bestSKstr.length];
		for(int i = 0; i < bestSKstr.length; i++){
			this.bestSK[i] = Integer.parseInt(bestSKstr[i]);
		}
		//set best BIM/SOM for each best single key (if calced yet)
		System.out.println("--> In BGM_Manager(), sk_bso = "+laRow.get(fciLA.getIdx("sk_bso")));
		if(!laRow.get(fciLA.getIdx("sk_bso")).equals("ph")){
			String[] skBimSom = laRow.get(fciLA.getIdx("sk_bso")).split("~");
			this.skBuyIn = new double[skBimSom.length][2];
			for(int i = 0; i < skBimSom.length; i++){
				String[] skPair = skBimSom[i].split("\\|");
				this.skBuyIn[i][0] = Double.parseDouble(skPair[0]);
				this.skBuyIn[i][1] = Double.parseDouble(skPair[1]);
			}
		}
		//set best BIM/SOM for agg key in general (if calced yet)
		System.out.println("--> In BGM_Manager(), ak_bso = "+laRow.get(fciLA.getIdx("ak_bso")));
		if(!laRow.get(fciLA.getIdx("ak_bso")).equals("ph")){
			String[] akPair = laRow.get(fciLA.getIdx("ak_bso")).split("\\|");
			this.akBuyIn = new double[2];
			this.akBuyIn[0] = Double.parseDouble(akPair[0]);
			this.akBuyIn[1] = Double.parseDouble(akPair[1]);
		}
		//couple each SK with its market state mask
		this.statesSK = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile("./../out/sk/log/"+this.bgm+"/keys_struct.txt", ",");
		FCI fciKS = new FCI(true, "./../out/sk/log/"+this.bgm+"/keys_struct.txt");
		ArrayList<String> ksKeyCol = AhrIO.scanCol("./../out/sk/log/"+this.bgm+"/keys_struct.txt", ",", fciKS.getIdx("sk_num"));
		for(int i = 0; i < bestSK.length; i++){
			ArrayList<String> stateLine = new ArrayList<String>();
			stateLine.add(String.valueOf(bestSK[i]));
			int ksIdx = ksKeyCol.indexOf(String.valueOf(bestSK[i]));
			if(ksIdx != -1){
				stateLine.add(ksFile.get(ksIdx).get(fciKS.getIdx("ms_mask")));
			}else{
				System.out.println("ERROR: SK"+this.bestSK[i]+" not found in "+this.bgm+"/keys_struct.txt");
				stateLine.add("");
			}
			this.statesSK.add(stateLine);	
		}
	
		//focus on a single key (also inits an AttributesSK obj)
		if(bestSK.length > 0){
			System.out.println("--> BGM Initialized w/ SK"+bestSK[0]);
			setFocusSK(bestSK[0]); 
		}else{
			System.out.println("ERROR in constructor : no SKs found");
		}
	}
	

	//getters
	public int getID(){
		return this.id;
	}
	public String getBGM(){
		return this.bgm;
	}
	public boolean getCall(){
		//return this.is_long;
		return this.kattr.getCall();
	}
	public int getFocusSK(){
		return this.focusSK;
	}
	public int[] getSKs(){
		return this.bestSK;
	}
	public int getSK(String date){
		//get market state of QQQ for given date
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		ArrayList<String> msRow = AhrIO.scanRow("./../in/mstates.txt", ",", date);
		String msOfDate = msRow.get(fciMS.getIdx("ms_mask"));		
		//get the best performing SK that matches with QQQ market state of this date
		String bkOfDate = "";
		for(int i = statesSK.size() - 1; i >= 0; i--){
			if(AhrGen.compareMasks(statesSK.get(i).get(1), msOfDate)){
				bkOfDate = statesSK.get(i).get(0);
			}
		}
		return Integer.parseInt(bkOfDate);
	}
	//given a SK and a date, return if SKs MS Mask matches the MS of date
	public boolean getMsMatch(int skID, String date){
		if(getFocusSK() != skID){
			setFocusSK(skID);
		}
		String msMask = getMsMask();
		//get market state of QQQ for given date
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		ArrayList<String> msRow = AhrIO.scanRow("./../in/mstates.txt", ",", date);
		String msOfDate = msRow.get(fciMS.getIdx("ms_mask"));		
		//yes or no if matches
		boolean does_match = AhrGen.compareMasks(msMask, msOfDate);
		return does_match;
	}

	public double getBimSK(int skID){
		double bim = 0.0;
		int idx = -1;
		for(int i = 0; i < this.bestSK.length; i++){
			if(this.bestSK[i] == skID){
				idx = i;
				break;
			}
		}
		if(idx != -1 && this.skBuyIn != null){
			bim = this.skBuyIn[idx][0];
		}
		return bim;
	}
	public double getSomSK(int skID){
		double som = 0.0;
		int idx = -1;
		for(int i = 0; i < this.bestSK.length; i++){
			if(this.bestSK[i] == skID){
				idx = i;
				break;
			}
		}
		if(idx != -1 && this.skBuyIn != null){
			som = this.skBuyIn[idx][1];
		}
		return som;
	}
	public double getBimAK(){
		double bim = 0.0;
		if(this.akBuyIn != null){
			bim = this.akBuyIn[0];
		}
		return bim;
	}
	public double getSomAK(){
		double som = 0.0;
		if(this.akBuyIn != null){
			som = this.akBuyIn[1];
		}
		return som;
	}
	public int getSPD(){
		//if(this.bgm.equals("ANN") && this.ann != null){
		//	return this.ann.getSPD();
		//}else{
		//	return -1;
		//}
		return this.kattr.getSPD();
	}
	public int getTVI(){
		//if(this.bgm.equals("ANN") && this.ann != null){
		//	return this.ann.getTVI();
		//}else{
		//	return -1;
		//}
		return this.kattr.getTVI();
	}
	public String getSDate(){
		//if(this.bgm.equals("ANN") && this.ann != null){
		//	return this.ann.getSDate();
		//}else{
		//	return "";
		//}
		return this.kattr.getSDate();
	}
	public String getEDate(){
		//if(this.bgm.equals("ANN") && this.ann != null){
		//	return this.ann.getEDate();
		//}else{
		//	return "";
		//}
		return this.kattr.getEDate();
	}
	public double getPlateau(){
		//if(this.bgm.equals("ANN") && this.ann != null){
		//	return this.ann.getPlateau();
		//}else{
		//	return -1.0;
		//}
		return this.kattr.getPlateau();
	}
	public String getMsMask(){
		//if(this.bgm.equals("ANN") && this.ann != null){
		//	return this.ann.getMsMask();
		//}else{
		//	return "";
		//}
		return this.kattr.getMsMask();
	}
	public ArrayList<ArrayList<String>> getStatesSK(){
		return this.statesSK;
	}	

	//setters
	public void setBGM(String bgmVal){
		this.bgm = bgmVal;
	}
	public void setFocusSK(int skID){
		String ksPath = "./../out/sk/log/"+this.bgm+"/keys_struct.txt";
		this.kattr = new AttributesSK(this.bgm, ksPath, String.valueOf(skID));
		this.focusSK = skID;
		//if(this.bgm.equals("ANN")){
		//	this.ann = new ANN(skID);
		//	this.focusSK = skID;
		//}
	}
	public void setFocusSK(String date){//set focus to best SK for this date
		ArrayList<String> msRow = AhrIO.scanRow("./../in/mstates.txt", ",", date);
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		String msOfDate = msRow.get(fciMS.getIdx("ms_mask"));

		/*
		ArrayList<ArrayList<String>> mstates = AhrIO.scanFile("./../in/mstates.txt", ",");
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		//itr thru mstates until you get line that matches date
		int c = 0;
		String itrDate = mstates.get(c).get(fciMS.getIdx("date"));
		while(!itrDate.equals(date)){
			c++;
			itrDate = mstates.get(c).get(fciMS.getIdx("date"));
		}
		String msOfDate = mstates.get(c).get(fciMS.getIdx("ms_mask"));
		*/

		//itr thru statesSK backwards to get best SK
		String bkOfDate = "";
		for(int i = statesSK.size()-1; i >= 0; i--){
			String msItr = statesSK.get(i).get(1);
			if(AhrGen.compareMasks(msItr, msOfDate)){
				bkOfDate = statesSK.get(i).get(0);
			}
		}
		int bestKey = Integer.parseInt(bkOfDate);
		//set focus according to BGM
		setFocusSK(bestKey);
	}


	/*-------------------------------------------------------------------------------------
		Prediction & Score Related Functions
	---------------------------------------------------------------------------------------*/
	
	//CONTROLLER FUNCT: return set of predicted stocks for a given date
	public ArrayList<ArrayList<String>> makePredictions(int skID, String date){
		ArrayList<ArrayList<String>> dbuf = new ArrayList<ArrayList<String>>();
		if(bgm.equals("gad2")){
			dbuf = makePred_GAD2(skID, date);
		}else if(bgm.equals("gab3")){
			dbuf = makePred_GAB3(skID, date);
		}else if(bgm.equals("ann")){
			dbuf = makePred_ANN(skID, date);
		}else{
			System.out.println("ERROR: Invalid BGM in makePredictions()");
		}
		return dbuf;
	}
	private ArrayList<ArrayList<String>> makePred_GAD2(int skID, String date){
		//redacted
		return new ArrayList<ArrayList<String>>();
	}
	private ArrayList<ArrayList<String>> makePred_GAB3(int skID, String date){
		//redacted
		return new ArrayList<ArrayList<String>>();
	}
	private ArrayList<ArrayList<String>> makePred_ANN(int skID, String date){
		if(getFocusSK() != skID){
			setFocusSK(skID);
		}
		//get relv info
		boolean isLong = kattr.getCall();
		int spd = kattr.getSPD();
		int tvi = kattr.getTVI();
		int tvn = Globals.target_var_num;
		double plateau = kattr.getPlateau();
		String narMask = kattr.getNarMask();
		String indMask = kattr.getIndMask();
		Network network = new Network("./../out/sk/log/ann/structure/struct_"+String.valueOf(skID)+".txt");
		//get lines from Clean DB for this date (that also pass NAR)
		String bdPath = "./../../DB_Intrinio/Clean/ByDate/"+date+".txt";
		ArrayList<ArrayList<String>> allClean = AhrIO.scanFile(bdPath, "~");
		ArrayList<ArrayList<String>> narClean = new ArrayList<ArrayList<String>>();
		FCI fciBD = new FCI(false, "./../../DB_Intrinio/Clean/ByDate/");
		for(int i = 0; i < allClean.size(); i++){
			String narMaskTmp = narMask;
			String narItr = allClean.get(i).get(fciBD.getIdx("nar_mask"));
			while(narMaskTmp.length() < narItr.length()){
				narMaskTmp += "x";
			}
			if(AhrGen.compareMasks(narMaskTmp, narItr)){
				narClean.add(allClean.get(i));
			}
		}
		//init day buffer
		ArrayList<ArrayList<String>> dbuf = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < spd; i++){
			ArrayList<String> line = new ArrayList<String>();
			line.add("tick");								// [0] ticker
			if(isLong){										// [1] score
				line.add("0.0");
			}else{
				line.add(String.valueOf(Double.MAX_VALUE));
			}
			line.add("0.0");								// [2] 1-day Intra %
			line.add("0.0");								// [3] 1-day Inter %
			line.add("0.0");								// [4] 2-day Intra %
			line.add("0.0");								// [5] 2-day Inter %
			line.add("0.0");								// [6] 3-day Intra %
			line.add("0.0");								// [7] 3-day Inter %
			line.add("0.0");								// [8] 5-day Intra %
			line.add("0.0");								// [9] 5-day Inter %
			line.add("0.0");								// [10] 10-day Intra %
			line.add("0.0");								// [11] 10-day Inter %
			dbuf.add(line);
		}	
		//calc score
		//itr thru all clean lines of this date, getting all lines with best score
		String tvColName = Globals.tvi_names[tvi];
		for(int i = 0; i < narClean.size(); i++){
			//create data line of normalized ind vals [0-65535] to [0-1]
			ArrayList<String> dline = new ArrayList<String>();
			for(int j = 0; j < indMask.length(); j++){
				String indCol = "ind"+String.valueOf(j);
				double pval = Double.parseDouble(narClean.get(i).get(fciBD.getIdx(indCol)));
				pval = pval * (1.0/65535.0);
				dline.add(String.valueOf(pval));
			}
			//get plateaued target val appr, normalize target val appr and add to dline
			double tvAppr = 0.0;
			if(!narClean.get(i).get(fciBD.getIdx(tvColName)).equals("tbd")){
				tvAppr = Double.parseDouble(narClean.get(i).get(fciBD.getIdx(tvColName)));
			}
			if(tvAppr > plateau){
				tvAppr = plateau;
			}
			if(tvAppr < (plateau * -1.0)){
				tvAppr = (plateau * -1.0);
			}
			tvAppr = tvAppr + plateau;
			tvAppr = tvAppr * (1.0 / (plateau * 2.0));
			dline.add(String.valueOf(tvAppr));
			//calc score for 1 line
			double score = 0.0;
			Network tnet = network;	//tmp network so no change happens to main network
			tnet.feedForward(dline);
			for(int j = 0; j < tnet.outputLayer.size(); j++){
				score += tnet.outputLayer.get(j).getValue();
			}
			//update day buffer
			int llIdx = dbuf.size()-1;
			if(isLong){
				if(score > Double.parseDouble(dbuf.get(llIdx).get(1))){
					String ticker = narClean.get(i).get(fciBD.getIdx("ticker"));
					dbuf.get(llIdx).set(0, ticker);
					dbuf.get(llIdx).set(1, String.format("%.7f", score));
					for(int j = 0; j < tvn; j++){
						String itrColName = Globals.tvi_names[j];
						String apprStr = narClean.get(i).get(fciBD.getIdx(itrColName));
						double appr = 0.0;
						try{
							appr = Double.parseDouble(apprStr);
						}catch(NumberFormatException e){
						}
						dbuf.get(llIdx).set(2+j, String.format("%.4f", appr));
					}
					Collections.sort(dbuf, new Comparator<ArrayList<String>>(){
						@Override
						public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
							double dcomp1 = Double.parseDouble(obj1.get(1));
							double dcomp2 = Double.parseDouble(obj2.get(1));
							return (Double.compare(dcomp1, dcomp2) * -1);//descending
						}
					});
				}
			}else{
				if(score < Double.parseDouble(dbuf.get(llIdx).get(1))){
					String ticker = narClean.get(i).get(fciBD.getIdx("ticker"));
					dbuf.get(llIdx).set(0, ticker);
					dbuf.get(llIdx).set(1, String.format("%.7f", score));
					for(int j = 0; j < tvn; j++){
						String itrColName = Globals.tvi_names[j];
						String apprStr = narClean.get(i).get(fciBD.getIdx(itrColName));
						double appr = 0.0;
						try{
							appr = Double.parseDouble(apprStr);
						}catch(NumberFormatException e){
						}
						dbuf.get(llIdx).set(2+j, String.format("%.4f", appr));
					}
					Collections.sort(dbuf, new Comparator<ArrayList<String>>(){
						@Override
						public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
							double dcomp1 = Double.parseDouble(obj1.get(1));
							double dcomp2 = Double.parseDouble(obj2.get(1));
							return Double.compare(dcomp1, dcomp2);//ascending
						}
					});
				}			
			}
		}
		return dbuf;
	}

	//CONTROLLER FUNCT: return set of predicted stocks for a given date
	public ArrayList<ArrayList<String>> makePredictions(int skID, String date, int inSPD){
		ArrayList<ArrayList<String>> dbuf = new ArrayList<ArrayList<String>>();
		if(bgm.equals("gad2")){
			dbuf = makePred_GAD2(skID, date, inSPD);
		}else if(bgm.equals("gab3")){
			dbuf = makePred_GAB3(skID, date, inSPD);
		}else if(bgm.equals("ann")){
			dbuf = makePred_ANN(skID, date, inSPD);
		}else{
			System.out.println("ERROR: Invalid BGM in makePredictions()");
		}
		return dbuf;
	}
	private ArrayList<ArrayList<String>> makePred_GAD2(int skID, String date, int inSPD){
		//redacted
		return new ArrayList<ArrayList<String>>();
	}
	private ArrayList<ArrayList<String>> makePred_GAB3(int skID, String date, int inSPD){
		//redacted
		return new ArrayList<ArrayList<String>>();
	}
	private ArrayList<ArrayList<String>> makePred_ANN(int skID, String date, int inSPD){
		if(getFocusSK() != skID){
			setFocusSK(skID);
		}
		//get relv info
		boolean isLong = kattr.getCall();
		int spd = kattr.getSPD();
		int tvi = kattr.getTVI();
		int tvn = Globals.target_var_num;
		double plateau = kattr.getPlateau();
		String narMask = kattr.getNarMask();
		String indMask = kattr.getIndMask();
		Network network = new Network("./../out/sk/log/ann/structure/struct_"+String.valueOf(skID)+".txt");
		//get lines from Clean DB for this date (that also pass NAR)
		String bdPath = "./../../DB_Intrinio/Clean/ByDate/"+date+".txt";
		ArrayList<ArrayList<String>> allClean = AhrIO.scanFile(bdPath, "~");
		ArrayList<ArrayList<String>> narClean = new ArrayList<ArrayList<String>>();
		FCI fciBD = new FCI(false, "./../../DB_Intrinio/Clean/ByDate/");
		for(int i = 0; i < allClean.size(); i++){
			String narMaskTmp = narMask;
			String narItr = allClean.get(i).get(fciBD.getIdx("nar_mask"));
			while(narMaskTmp.length() < narItr.length()){
				narMaskTmp += "x";
			}
			if(AhrGen.compareMasks(narMaskTmp, narItr)){
				narClean.add(allClean.get(i));
			}
		}
		//init day buffer
		ArrayList<ArrayList<String>> dbuf = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < inSPD; i++){
			ArrayList<String> line = new ArrayList<String>();
			line.add("tick");								// [0] ticker
			if(isLong){										// [1] score
				line.add("0.0");
			}else{
				line.add(String.valueOf(Double.MAX_VALUE));
			}
			line.add("0.0");								// [2] 1-day intra %
			line.add("0.0");								// [3] 1-day inter %
			line.add("0.0");								// [4] 2-day intra %
			line.add("0.0");								// [5] 2-day inter %
			line.add("0.0");								// [6] 3-day intra %
			line.add("0.0");								// [7] 3-day inter %
			line.add("0.0");								// [8] 5-day intra %
			line.add("0.0");								// [9] 5-day inter %
			line.add("0.0");								// [10] 10-day intra %
			line.add("0.0");								// [11] 10-day inter %
			dbuf.add(line);
		}	
		//calc score
		//itr thru all clean lines of this date, getting all lines with best score
		String tvColName = Globals.tvi_names[tvi];
		for(int i = 0; i < narClean.size(); i++){
			//create data line of normalized ind vals [0-65535] to [0-1]
			ArrayList<String> dline = new ArrayList<String>();
			for(int j = 0; j < indMask.length(); j++){
				String indCol = "ind"+String.valueOf(j);
				double pval = Double.parseDouble(narClean.get(i).get(fciBD.getIdx(indCol)));
				pval = pval * (1.0/65535.0);
				dline.add(String.valueOf(pval));
			}
			//get plateaued target val appr, normalize target val appr and add to dline
			double tvAppr = 0.0;
			if(!narClean.get(i).get(fciBD.getIdx(tvColName)).equals("tbd")){
				tvAppr = Double.parseDouble(narClean.get(i).get(fciBD.getIdx(tvColName)));
			}
			if(tvAppr > plateau){
				tvAppr = plateau;
			}
			if(tvAppr < (plateau * -1.0)){
				tvAppr = (plateau * -1.0);
			}
			tvAppr = tvAppr + plateau;
			tvAppr = tvAppr * (1.0 / (plateau * 2.0));
			dline.add(String.valueOf(tvAppr));
			//calc score for 1 line
			double score = 0.0;
			Network tnet = network;	//tmp netowrk so no change happens to main network
			tnet.feedForward(dline);
			for(int j = 0; j < tnet.outputLayer.size(); j++){
				score += tnet.outputLayer.get(j).getValue();
			}
			//update day buffer
			int llIdx = dbuf.size()-1;
			if(isLong){
				if(score > Double.parseDouble(dbuf.get(llIdx).get(1))){
					String ticker = narClean.get(i).get(fciBD.getIdx("ticker"));
					dbuf.get(llIdx).set(0, ticker);
					dbuf.get(llIdx).set(1, String.format("%.7f", score));
					for(int j = 0; j < tvn; j++){
						String itrColName = Globals.tvi_names[j];
						String apprStr = narClean.get(i).get(fciBD.getIdx(itrColName));
						double appr = 0.0;
						try{
							appr = Double.parseDouble(apprStr);
						}catch(NumberFormatException e){
						}
						dbuf.get(llIdx).set(2+j, String.format("%.4f", appr));
					}
					Collections.sort(dbuf, new Comparator<ArrayList<String>>(){
						@Override
						public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
							double dcomp1 = Double.parseDouble(obj1.get(1));
							double dcomp2 = Double.parseDouble(obj2.get(1));
							return (Double.compare(dcomp1, dcomp2) * -1);//descending
						}
					});
				}
			}else{
				if(score < Double.parseDouble(dbuf.get(llIdx).get(1))){
					String ticker = narClean.get(i).get(fciBD.getIdx("ticker"));
					dbuf.get(llIdx).set(0, ticker);
					dbuf.get(llIdx).set(1, String.format("%.7f", score));
					for(int j = 0; j < tvn; j++){
						String itrColName = Globals.tvi_names[j];
						String apprStr = narClean.get(i).get(fciBD.getIdx(itrColName));
						double appr = 0.0;
						try{
							appr = Double.parseDouble(apprStr);
						}catch(NumberFormatException e){
						}
						dbuf.get(llIdx).set(2+j, String.format("%.4f", appr));
					}
					Collections.sort(dbuf, new Comparator<ArrayList<String>>(){
						@Override
						public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
							double dcomp1 = Double.parseDouble(obj1.get(1));
							double dcomp2 = Double.parseDouble(obj2.get(1));
							return Double.compare(dcomp1, dcomp2);//ascending
						}
					});
				}			
			}
		}
		return dbuf;
	}



	//CONTROLLER FUNCT: return a single score given a SK, ticker, and date
	public double calcScore(int skID, String date, String ticker){
		double score;
		if(bgm.equals("gad2")){
			score = calcScore_GAD2(skID, date, ticker);
		}else if(bgm.equals("gab3")){
			score = calcScore_GAB3(skID, date, ticker);
		}else if(bgm.equals("ann")){
			score = calcScore_ANN(skID, date, ticker);
		}else{
			System.out.println("ERROR: Invalid BGM in makePredictions()");
			score = -1;
		}
		return score;	
	}
	private double calcScore_GAD2(int skID, String date, String ticker){
		//redacted
		return 0.0;
	}
	private double calcScore_GAB3(int skID, String date, String ticker){
		//redacted
		return 0.0;
	}
	private double calcScore_ANN(int skID, String date, String ticker){
		if(getFocusSK() != skID){
			setFocusSK(skID);
		}
		//get relv info for this ANN SK
		String indMask = kattr.getIndMask();
		Network network = new Network("./../out/sk/log/ann/structure/struct_"+String.valueOf(skID)+".txt");
		ArrayList<String> dline = new ArrayList<String>();
		//find line in Clean/ByDate that matches stock and date
		ArrayList<ArrayList<String>> bdFile = AhrIO.scanFile("./../../DB_Intrinio/Clean/ByDate/"+date+".txt", "~");
		FCI fciBD = new FCI(false, "./../../DB_Intrinio/Clean/ByDate/");
		ArrayList<String> bdLine = new ArrayList<String>();
		for(int i = 0; i < bdFile.size(); i++){
			if(bdFile.get(i).get(fciBD.getIdx("ticker")).equals(ticker)){
				bdLine = bdFile.get(i);
			}
		}
		//calc score from ByDate line
		double score = 0.0;
		for(int j = 0; j < indMask.length(); j++){
			String indCol = "ind"+String.valueOf(j);
			double pval = Double.parseDouble(bdLine.get(fciBD.getIdx(indCol)));
			pval = pval * (1.0/65535.0);
			dline.add(String.valueOf(pval));
		}
		dline.add("0.0");
		Network tnet = network;	//tmp netowrk so no change happens to main network
		tnet.feedForward(dline);
		for(int j = 0; j < tnet.outputLayer.size(); j++){
			score += tnet.outputLayer.get(j).getValue();
		}
		return score;
	}	
	

	public ArrayList<Integer> scorePercentiles(int skID, ArrayList<String> scores){
		//get row in score_percentiles.txt file that matches BGM & SK
		String spPath = "./../out/score_percentiles.txt";
		ArrayList<ArrayList<String>> spFile = AhrIO.scanFile(spPath, ",");
		ArrayList<String> spRow = new ArrayList<String>();
		for(int i = 0; i < spFile.size(); i++){
			spRow = spFile.get(i);
			if(kattr.getBGM().equals(spRow.get(0)) && skID == Integer.parseInt(spRow.get(1))){
				break;
			}
		}
		//get if score is better higher or lower
		boolean score_higher_is_better = true;
		if(spRow.size() > 0){
			double firstScore = Double.parseDouble(spRow.get(2));
			double lastScore = Double.parseDouble(spRow.get(spRow.size()-1));
			if(lastScore > firstScore){
				score_higher_is_better = false;
			}
		}
		//calc scpt val for each prediction
		ArrayList<Integer> spVals = new ArrayList<Integer>();
		if(spRow.size() > 0 && score_higher_is_better){//GAB3 and ANN long
			for(int i = 0; i < scores.size(); i++){
				double itrScore = Double.parseDouble(scores.get(i));
				double bestScore = Double.parseDouble(spRow.get(2));
				//calc score percentile
				int scpt = 101;
				if(itrScore > bestScore){
					scpt = 0;
				}else{
					for(int j = 2; j < (spRow.size()-1); j++){
						double hiVal = Double.parseDouble(spRow.get(j));
						double loVal = Double.parseDouble(spRow.get(j+1));
						if(itrScore <= hiVal && itrScore > loVal){
							scpt = (j-1);
							break;
						}
					}
				}
				spVals.add(scpt);
			}
		}else if(spRow.size() > 0 && !score_higher_is_better){//GAD2 and ANN short
			for(int i = 0; i < scores.size(); i++){
				double itrScore = Double.parseDouble(scores.get(i));
				double bestScore = Double.parseDouble(spRow.get(2));
				//calc score percentile		
				int scpt = 101;
				if(itrScore < bestScore){
					scpt = 0;
				}else{
					for(int j = 2; j < (spRow.size()-1); j++){
						double loVal = Double.parseDouble(spRow.get(j));
						double hiVal = Double.parseDouble(spRow.get(j+1));
						if(itrScore >= loVal && itrScore < hiVal){
							scpt = (j-1);
							break;
						}
					}
				}
				spVals.add(scpt);
			}
		}else{
			for(int i = 0; i < scores.size(); i++){
				spVals.add(-1);
			}
		}
		return spVals;
	}
	
	//split all scores into percentiles (100 bins) for given Single Key
	public void calcScorePercentiles(){
		String bgmLC = kattr.getBGM();
		String bgmUC = bgmLC.toUpperCase();
		String spPath = "./../out/score_percentiles.txt";
		String alPath = "./../out/ak/log/ak_log.txt";
		FCI fciAL = new FCI(true, alPath);
		ArrayList<ArrayList<String>> spFile = AhrIO.scanFile(spPath, ",");
		ArrayList<String> alRow = AhrIO.scanRow(alPath, ",", String.valueOf(this.id));
		String call = alRow.get(fciAL.getIdx("call"));
		String sdate = alRow.get(fciAL.getIdx("start_date"));
		String edate = alRow.get(fciAL.getIdx("end_date"));
		FCI fciSK = new FCI(false, "./../out/sk/baseis/");
		for(int i = 0; i < this.bestSK.length; i++){
			String skNum = String.valueOf(this.bestSK[i]);
			String skPath = "./../out/sk/baseis/"+bgmLC+"/"+bgmUC+"_"+skNum+".txt";
			ArrayList<Double> scores = new ArrayList<Double>();
			ArrayList<ArrayList<String>> skBasis = AhrIO.scanFile(skPath, ",");
			for(int j = 0; j < skBasis.size(); j++){
				String itrDate = skBasis.get(j).get(fciSK.getIdx("date"));
				if(AhrDate.isDateInPeriod(itrDate, sdate, edate)){
					scores.add(Double.parseDouble(skBasis.get(j).get(fciSK.getIdx("score"))));
				}
			}
			//sort scores and calc percentiles (GAB3 and long ANNs are higher = better)
			ArrayList<Double> ptiles = new ArrayList<Double>();
			Collections.sort(scores);
			if(bgmUC.equals("GAB3") || (bgmUC.equals("ANN") && call.equals("1"))){
				Collections.reverse(scores);
			}
			int stepSize = scores.size() / 100;
			for(int j = 0; j < scores.size(); j++){
				if(j%stepSize == 0){
					ptiles.add(scores.get(j));
				}
			}
			//update the file
			ArrayList<String> line = new ArrayList<String>();
			line.add(bgmUC);
			line.add(skNum);
			for(int j = 0; j < ptiles.size(); j++){
				line.add(String.valueOf(ptiles.get(j)));
			}
			//insert new line or update line depending
			int skIdx = -1;
			for(int j = 0; j < spFile.size(); j++){
				String bgmItr = spFile.get(j).get(0);
				String skItr = spFile.get(j).get(1);
				if(bgmItr.equals(bgmUC) && skItr.equals(skNum)){
					skIdx = j;
					break;
				}
			}
			if(skIdx == -1){
				spFile.add(line);
			}else{
				spFile.set(skIdx, line);
			}
		}
		AhrIO.writeToFile("./../out/score_percentiles.txt", spFile, ",");
	}	

	/*-------------------------------------------------------------------------------------
		Basic Key Perf Related Functions
	---------------------------------------------------------------------------------------*/

	//get performance data from reading it in from basis file
	public ArrayList<String> perfFromBasisFile(String path){
		FCI fciBS;
		if(path.contains("ak")){
			fciBS = new FCI(false, "./../out/ak/baseis/");
		}else{
			fciBS = new FCI(false, "./../out/sk/baseis/");
		}
		double plateau = getPlateau();
		//init general perf data
		int trainCount = 0;
		int testCount = 0;
		double[][] ttData = {{0.0, 0.0, 0.0}, 		//(0) train pappr, (1) train tappr, (2) train pos %
							{0.0, 0.0, 0.0}};		//(0) test pappr, (1) test tappr, (2) test pos %
		//itr thru basis file
		ArrayList<ArrayList<String>> basis = AhrIO.scanFile(path, ",");
		for(int i = 0; i < basis.size(); i++){
			String itrTTV = basis.get(i).get(fciBS.getIdx("ttv_code"));
			double itrAppr = 0.0;
			try{
				itrAppr = Double.parseDouble(basis.get(i).get(fciBS.getIdx("appr")));
			}catch(NumberFormatException e){
				System.out.println("ERR: " + e.getMessage());
			}
			if(itrTTV.equals("0")){
				double platAppr = itrAppr;
				if(platAppr > plateau){
					platAppr = plateau;
				}else if(platAppr < (plateau * -1.0)){
					platAppr = (plateau * -1.0);
				}
				ttData[0][0] += platAppr;
				ttData[0][1] += itrAppr;
				if(itrAppr > 0){
					ttData[0][2]++;
				}
				trainCount++;
			}else if(itrTTV.equals("1")){
				double platAppr = itrAppr;
				if(platAppr > plateau){
					platAppr = plateau;
				}else if(platAppr < (plateau * -1.0)){
					platAppr = (plateau * -1.0);
				}
				ttData[1][0] += platAppr;
				ttData[1][1] += itrAppr;
				if(itrAppr > 0){
					ttData[1][2]++;
				}
				testCount++;
			}
		}
		//convert data into easier format and return
		ArrayList<String> data = new ArrayList<String>();
		data.add(String.format("%.5f", (ttData[0][0] / (double)trainCount)));			//[0] train plat APAPT
		data.add(String.format("%.5f", (ttData[0][1] / (double)trainCount)));			//[1] train true APAPT
		data.add(String.format("%.3f", ((ttData[0][2] / (double)trainCount) * 100.0)));	//[2] train pos %
		data.add(String.format("%.5f", (ttData[1][0] / (double)testCount)));			//[3] test plat APAPT
		data.add(String.format("%.5f", (ttData[1][1] / (double)testCount)));			//[4] test true APAPT
		data.add(String.format("%.3f", ((ttData[1][2] / (double)testCount) * 100.0)));	//[5] test pos %
		return data;
	}


	//CONTROLLER FUNCTION: calc the perf vals of a SK
	public ArrayList<String> keyPerformance(){
		ArrayList<String> data = new ArrayList<String>();
		if(bgm.equals("gad2")){
			data = keyPerfSK_GAD2();
		}else if(bgm.equals("gab3")){
			data = keyPerfSK_GAB3();
		}else if(bgm.equals("ann")){
			data = keyPerfSK_ANN();
		}else{
			System.out.println("ERROR: Invalid BGM in keyPerformance()");
		}
		return data;
	}
	public ArrayList<String> keyPerfSK_GAD2(){
		//redacted
		return new ArrayList<String>();
	}
	public ArrayList<String> keyPerfSK_GAB3(){
		//redacted
		return new ArrayList<String>();
	}
	public ArrayList<String> keyPerfSK_ANN(){
		Network knet = new Network("./../out/sk/log/ann/structure/struct_"+id+".txt");
		boolean isLong = kattr.getCall();
		double plateau = kattr.getPlateau();
		int spd = kattr.getSPD();
		int tvi = kattr.getTVI();
		String msMask = kattr.getMsMask();
		String indMask = kattr.getIndMask();
		String sdate = kattr.getSDate();
		String edate = kattr.getEDate();
		ArrayList<String> dates = AhrDate.getDatesBetween(sdate, edate);
		ArrayList<String> mdates = AhrDate.getDatesThatPassMarketMask(dates, msMask);
		//init general perf data
		int evenCount = 0;
		int oddCount = 0;
		double[][] ttData = {{0.0, 0.0, 0.0}, 		//(0) train pappr, (1) train tappr, (2) train pos %
							{0.0, 0.0, 0.0}};		//(0) test pappr, (1) test tappr, (2) test pos %
		//itr thru all mdates, get dbuf for each date
		for(int i = 0; i < mdates.size(); i++){
			ArrayList<ArrayList<String>> dbuf = makePred_ANN(id, mdates.get(i), spd);
			//check if train or test
			boolean is_odd_date = false;
			if(Integer.parseInt(mdates.get(i).split("-")[2]) % 2 == 1){
				is_odd_date = true;
			}
			int ttri = 0;		//ttData row index
			if(is_odd_date){
				ttri = 1;
			}
			//calc perf vals from dbuf
			for(int j = 0; j < dbuf.size(); j++){
				double tvAppr = Double.parseDouble(dbuf.get(j).get(2+tvi));
				double platAppr = tvAppr;
				if(platAppr > plateau){
					platAppr = plateau;
				}
				if(platAppr < (plateau * -1)){
					platAppr = (plateau * -1);
				}
				ttData[ttri][0] += platAppr;
				ttData[ttri][1] += tvAppr;
				if(tvAppr > 0.0){
					ttData[ttri][2] += 1.0;
				}
			}
			//calc total lines for even/odd
			if(is_odd_date){
				oddCount += spd;
			}else{
				evenCount += spd;
			}
		}
		//add data to perf AL and return
		ArrayList<String> data = new ArrayList<String>();
		data.add(String.format("%.5f", (ttData[0][0] / (double)evenCount)));			//[0] train plat APAPT
		data.add(String.format("%.5f", (ttData[0][1] / (double)evenCount)));			//[1] train true APAPT
		data.add(String.format("%.3f", ((ttData[0][2] / (double)evenCount) * 100.0)));	//[2] train pos %
		data.add(String.format("%.5f", (ttData[1][0] / (double)oddCount)));				//[3] test plat APAPT
		data.add(String.format("%.5f", (ttData[1][1] / (double)oddCount)));				//[4] test true APAPT
		data.add(String.format("%.3f", ((ttData[1][2] / (double)oddCount) * 100.0)));	//[5] test pos %
		return data;
	}

	//set perf data (from either basis file or makePred) to SK keys_perf.txt file
	public void perfToFileSK(ArrayList<String> perf){
		String bgmLC = kattr.getBGM();
		String kpPath = "./../out/sk/log/"+bgmLC+"/keys_perf.txt";
		FCI fciKP = new FCI(true, kpPath);
		ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");
		ArrayList<String> kpRow = AhrAL.getRow(kpFile, String.valueOf(id));
		kpRow.set(fciKP.getIdx("plat_train_apapt"), perf.get(0));
		kpRow.set(fciKP.getIdx("plat_test_apapt"), perf.get(3));
		kpRow.set(fciKP.getIdx("true_train_apapt"), perf.get(1));
		kpRow.set(fciKP.getIdx("true_test_apapt"), perf.get(4));
		kpRow.set(fciKP.getIdx("true_train_posp"), perf.get(2));
		kpRow.set(fciKP.getIdx("true_test_posp"), perf.get(5));
		int rowIdx = AhrAL.getRowIdx(kpFile, String.valueOf(id));
		kpFile.set(rowIdx, kpRow);
		AhrIO.writeToFile(kpPath, kpFile, ",");
	}

	//set perf data (from either basis file or makePred) to AK ak_log.txt file
	public void perfToFileAK(ArrayList<String> perf){
		String laPath = "./../out/ak/log/ak_log.txt";
		FCI fciLA = new FCI(true, laPath);
		ArrayList<ArrayList<String>> laFile = AhrIO.scanFile(laPath, ",");
		ArrayList<String> laRow = AhrAL.getRow(laFile, String.valueOf(id));
		laRow.set(fciLA.getIdx("true_train_apapt"), perf.get(1));
		laRow.set(fciLA.getIdx("true_test_apapt"), perf.get(4));
		laRow.set(fciLA.getIdx("true_train_posp"), perf.get(2));
		laRow.set(fciLA.getIdx("true_test_posp"), perf.get(5));
		int rowIdx = AhrAL.getRowIdx(laFile, String.valueOf(id));
		laFile.set(rowIdx, laRow);
		AhrIO.writeToFile(laPath, laFile, ",");
	}


	
	/*-------------------------------------------------------------------------------------
		BIM/SOM Related Functions
	---------------------------------------------------------------------------------------*/

	//calcs BIM/SOM values to write to keys_perf file
	public void bsoPerfToFileSK(boolean is_min_trig, boolean use_sk_bso){
		String bgmLC = kattr.getBGM();
		//calc bso files
		ArrayList<String> bsoTrain = bsoMultiple(getSDate(), getEDate(), "100", is_min_trig, use_sk_bso);
		ArrayList<String> bsoTest = bsoMultiple(getSDate(), getEDate(), "010", is_min_trig, use_sk_bso);
		System.out.println("==> BSO Train Data: " + bsoTrain);
		System.out.println("==> BSO Test Data: " + bsoTest);
		//get row from keys_perf
		String kpPath = "./../out/sk/log/"+bgmLC+"/keys_perf.txt";
		FCI fciKP = new FCI(true, kpPath);
		ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");		
		ArrayList<String> kpRow = AhrAL.getRow(kpFile, String.valueOf(id));
		int rowIdx = AhrAL.getRowIdx(kpFile, String.valueOf(id));
		//add bso vals to row and write back to file
		kpRow.set(fciKP.getIdx("bim"), bsoTest.get(0));
		kpRow.set(fciKP.getIdx("som"), bsoTest.get(1));
		kpRow.set(fciKP.getIdx("bso_train_apapt"), bsoTrain.get(2));
		kpRow.set(fciKP.getIdx("bso_test_apapt"), bsoTest.get(2));
		kpRow.set(fciKP.getIdx("bso_train_posp"), bsoTrain.get(4));
		kpRow.set(fciKP.getIdx("bso_test_posp"), bsoTest.get(4));
		kpFile.set(rowIdx, kpRow);
		AhrIO.writeToFile(kpPath, kpFile, ",");
	}

	//calcs BIM/SOM values to write to ak_log.txt file
	public void bsoPerfToFileAK(boolean is_min_trig, boolean use_sk_bso){
		//calc bso files
		ArrayList<String> bsoTrain = bsoMultiple(getSDate(), getEDate(), "100", is_min_trig, use_sk_bso);
		ArrayList<String> bsoTest = bsoMultiple(getSDate(), getEDate(), "010", is_min_trig, use_sk_bso);
		//get row from ak_log
		String laPath = "./../out/ak/log/ak_log.txt";
		FCI fciLA = new FCI(true, laPath);
		ArrayList<ArrayList<String>> laFile = AhrIO.scanFile(laPath, ",");		
		ArrayList<String> laRow = AhrAL.getRow(laFile, String.valueOf(id));
		int rowIdx = AhrAL.getRowIdx(laFile, String.valueOf(id));
		//combine bim & som and add 
		String akBSO = bsoTest.get(0)+"|"+bsoTest.get(1);
		laRow.set(fciLA.getIdx("ak_bso"), akBSO);
		//add rest of BSO perf vals
		laRow.set(fciLA.getIdx("bso_train_apapt"), bsoTrain.get(2));
		laRow.set(fciLA.getIdx("bso_test_apapt"), bsoTest.get(2));
		laRow.set(fciLA.getIdx("bso_train_posp"), bsoTrain.get(4));
		laRow.set(fciLA.getIdx("bso_test_posp"), bsoTest.get(4));
		laFile.set(rowIdx, laRow);
		AhrIO.writeToFile(laPath, laFile, ",");
	}
	//write BIM/SOM Opt values to file when given an OrderSim obj, assumes BIM/SOM is already calced
	public void bsoPerfToFileAK(OrderSim osim){
		//get row from ak_log
		String laPath = "./../out/ak/log/ak_log.txt";
		FCI fciLA = new FCI(true, laPath);
		ArrayList<ArrayList<String>> laFile = AhrIO.scanFile(laPath, ",");		
		ArrayList<String> laRow = AhrAL.getRow(laFile, String.valueOf(osim.getID()));
		int rowIdx = AhrAL.getRowIdx(laFile, String.valueOf(id));
		//combine BIM and SOM and add to file
		String akBSO = String.format("%.3f", osim.getBIM()) +"|"+ String.format("%.3f", osim.getSOM());
		laRow.set(fciLA.getIdx("ak_bso"), akBSO);
		//calc train data
		osim.setTtvMask("100");
		osim.calcOrderList();
		laRow.set(fciLA.getIdx("bso_train_apapt"), String.format("%.5f", osim.getTrigAppr())); 
		laRow.set(fciLA.getIdx("bso_train_posp"), String.format("%.3f", (osim.getPosPer()*100.0)));
		//calc test data
		osim.setTtvMask("010");
		osim.calcOrderList();
		laRow.set(fciLA.getIdx("bso_test_apapt"), String.format("%.5f", osim.getTrigAppr())); 
		laRow.set(fciLA.getIdx("bso_test_posp"), String.format("%.3f", (osim.getPosPer()*100.0)));
		//update ak_log
		laFile.set(rowIdx, laRow);
		AhrIO.writeToFile(laPath, laFile, ",");
	}
		
	//calc the optimized prices to a buy-in and sell-out pair, runs thru a basis file
	public ArrayList<ArrayList<String>> bsoSingle(String sdate, String edate, double bim, double som, String ttvMask,
										boolean is_min_trig, boolean use_sk_bso){
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		OrderSim osim;
		if(is_ak){
			osim = new OrderSim(id);
		}else{
			osim = new OrderSim(kattr.getBGM(), id);
		}
		osim.setDateRange(sdate, edate);
		osim.setBIM(bim);
		osim.setSOM(som);
		osim.setTtvMask(ttvMask);
		osim.setISMT(!is_min_trig);
		data = bsoSingle(osim);	
		return data;
	}
	public ArrayList<ArrayList<String>> bsoSingle(OrderSim osim){
		osim.calcOrderList();
		ArrayList<ArrayList<String>> orders = osim.getOrderList();
		//init vals
		ArrayList<String> genVals = new ArrayList<String>();
		double[] closeApprs = {0.0, 0.0, 0.0, 0.0};		//[0] all, [1] open, [2] day, [3] not triggered
		double[] intraApprs = {0.0, 0.0, 0.0, 0.0};		//		^
		double[] trigApprs = {0.0, 0.0, 0.0, 0.0};		//		^
		double[] perOfTot = {0.0, 0.0, 0.0, 0.0};		// 		^
		//itr thru all orders and tally vals
		for(int i = 0; i < orders.size(); i++){
			String trigType = orders.get(i).get(4);
			closeApprs[0] += Double.parseDouble(orders.get(i).get(2));
			intraApprs[0] += Double.parseDouble(orders.get(i).get(3));
			perOfTot[0] += 1.0;
			if(trigType.equals("OPEN")){
				closeApprs[1] += Double.parseDouble(orders.get(i).get(2));
				intraApprs[1] += Double.parseDouble(orders.get(i).get(3));
				trigApprs[1] += Double.parseDouble(orders.get(i).get(7));
				perOfTot[1] += 1.0;
			}else if(trigType.equals("DAY")){
				closeApprs[2] += Double.parseDouble(orders.get(i).get(2));
				intraApprs[2] += Double.parseDouble(orders.get(i).get(3));
				trigApprs[2] += Double.parseDouble(orders.get(i).get(7));
				perOfTot[2] += 1.0;
			}else if(trigType.equals("NO")){
				closeApprs[3] += Double.parseDouble(orders.get(i).get(2));
				intraApprs[3] += Double.parseDouble(orders.get(i).get(3));
				trigApprs[3] += Double.parseDouble(orders.get(i).get(7));
				perOfTot[3] += 1.0;
			}else{
				System.out.println("ERR: Invalid Trig Type.");
			}
		}
		//avg out vals
		for(int i = 0; i < 4; i++){
			closeApprs[i] = closeApprs[i] / perOfTot[i];
			intraApprs[i] = intraApprs[i] / perOfTot[i];
			trigApprs[i] = trigApprs[i] / perOfTot[i];
			perOfTot[i] = (perOfTot[i] / (double)orders.size()) * 100.0;
		}
		trigApprs[0] = osim.getTrigAppr();
		//set gen vals 
		genVals.add(String.format("%.2f", osim.getBimPer()));		//[0] BIM Triggered %
		genVals.add(String.format("%.2f", osim.getSomPer()));		//[1] SOM Triggered %
		genVals.add(String.format("%.3f", osim.getTPR()));			//[2] Throughput Rate
		genVals.add(String.format("%.3f", osim.getSecAppr()));		//[3] Section Appr %
		genVals.add(String.format("%.3f", osim.getYoyAppr()));		//[4] YoY %
		genVals.add(String.format("%.4f", osim.getPosPer()));		//[5] Pos %
		//combine gen vals and double arrays and return
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		ArrayList<String> allLine = new ArrayList<String>();
		allLine.add(String.format("%.2f", perOfTot[0]));
		allLine.add(String.format("%.3f", closeApprs[0]));
		allLine.add(String.format("%.3f", intraApprs[0]));
		allLine.add(String.format("%.3f", trigApprs[0]));
		ArrayList<String> openLine = new ArrayList<String>();
		openLine.add(String.format("%.2f", perOfTot[1]));
		openLine.add(String.format("%.3f", closeApprs[1]));
		openLine.add(String.format("%.3f", intraApprs[1]));
		openLine.add(String.format("%.3f", trigApprs[1]));
		ArrayList<String> dayLine = new ArrayList<String>();
		dayLine.add(String.format("%.2f", perOfTot[2]));
		dayLine.add(String.format("%.3f", closeApprs[2]));
		dayLine.add(String.format("%.3f", intraApprs[2]));
		dayLine.add(String.format("%.3f", trigApprs[2]));
		ArrayList<String> notLine = new ArrayList<String>();
		notLine.add(String.format("%.2f", perOfTot[3]));
		notLine.add(String.format("%.3f", closeApprs[3]));
		notLine.add(String.format("%.3f", intraApprs[3]));
		notLine.add(String.format("%.3f", trigApprs[3]));
		data.add(genVals);
		data.add(allLine);
		data.add(openLine);
		data.add(dayLine);
		data.add(notLine);

		return data;
	}

	//calc the best BIM/SOM pair w/ given params, by brute force
	public ArrayList<String> bsoMultiple(String sdate, String edate, String ttvMask, boolean is_min_trig, boolean use_sk_bso){
		ArrayList<String> data = new ArrayList<String>();
		OrderSim osim;
		if(is_ak){
			osim = new OrderSim(id);
		}else{
			osim = new OrderSim(kattr.getBGM(), id);
		}
		osim.setDateRange(sdate, edate);
		osim.setTtvMask(ttvMask);
		osim.setISMT(!is_min_trig);
		data = bsoMultiple(osim);
		return data;
	}
	public ArrayList<String> bsoMultiple(OrderSim osim){
		//System.out.println("===> Calculating all BIM SOM Combinations ...");
		//init values
		ArrayList<ArrayList<String>> allVals = new ArrayList<ArrayList<String>>();
		ArrayList<Double> bestVals = new ArrayList<Double>();
		bestVals.add(0.0);	//[0] BIM
		bestVals.add(0.0);	//[1] SOM
		bestVals.add(0.0);	//[2] Trig % 
		bestVals.add(0.0);	//[3] YoY %
		bestVals.add(0.0);	//[4] Pos %
		//go thru all combos you want of BIM and SOM
		int sBIM = 80;
		int eBIM = 120;
		int sSOM = 50;
		int eSOM = 155;
		int totCount = (eBIM - sBIM) * (eSOM - sSOM);
		int count = 0;
		for(int i = 80; i <= 120; i++){
			for(int j = 50; j <= 155; j++){
				if((count % 500) == 0){
					//System.out.println("   "+count+" out of "+totCount);
				}
				count++;
				double bim = (double)i / 100.0;
				double som = (double)j / 100.0;
				osim.setBIM(bim);
				osim.setSOM(som);
				osim.calcOrderList();
				double trigAppr = osim.getTrigAppr();
				double yoyAppr = osim.getYoyAppr();
				double posp = osim.getPosPer();
				//System.out.println("--> BIM = "+String.format("%.3f",bim)+" & SOM = "+String.format("%.3f",som)+
				//	"  :  Trig % = " + String.format("%.4f", osim.getTrigAppr())+
				//	"  |  YoY % = " + String.format("%.4f", osim.getYoyAppr()));
				//update best vals if needed
				boolean good_som_value = true;
				if(som >= 0.97 && som <= 1.03){
					good_som_value = false;
				}
				ArrayList<Double> itrVals = new ArrayList<Double>();
				itrVals.add(bim);
				itrVals.add(som);
				itrVals.add(trigAppr);
				itrVals.add(yoyAppr);
				itrVals.add(posp);
				ArrayList<String> tfLine = new ArrayList<String>();
				tfLine.add(String.format("%.3f", bim));
				tfLine.add(String.format("%.3f", som));
				tfLine.add(String.format("%.3f", trigAppr));
				tfLine.add(String.format("%.3f", yoyAppr));
				tfLine.add(String.format("%.2f", posp * 100.0));
				if(good_som_value){
					allVals.add(tfLine);
					if(yoyAppr > bestVals.get(3)){
						bestVals = itrVals;
					}
				}else{
					ArrayList<String> zeroLine = new ArrayList<String>();
					zeroLine.add(String.format("%.3f", bim));
					zeroLine.add(String.format("%.3f", som));
					zeroLine.add("0.0");
					zeroLine.add("0.0");
					zeroLine.add("0.0");
					allVals.add(zeroLine);
				}
			} 
		}
		AhrIO.writeToFile("./../data/tmp/bso_multiple.txt", allVals, ",");
		//System.out.println("\n--> Best Vals 1st Round : " + bestVals);
		//narrow down BIM/SOM to third sigfig
		int bimItr = (int)(bestVals.get(0) * 1000.0);
		int somItr = (int)(bestVals.get(1) * 1000.0);
		ArrayList<Double> bestVals2 = new ArrayList<Double>();
		bestVals2.add(0.0);	//[0] BIM
		bestVals2.add(0.0);	//[1] SOM
		bestVals2.add(0.0);	//[2] Trig %
		bestVals2.add(0.0);	//[3] YoY %
		bestVals2.add(0.0);	//[4] Pos %
		for(int i = bimItr-9; i < (bimItr+9); i++){
			for(int j = somItr-9; j < (somItr+9); j++){
				double bim = (double)i / 1000.0;
				double som = (double)j / 1000.0;
				osim.setBIM(bim);
				osim.setSOM(som);
				osim.calcOrderList();
				double trigAppr = osim.getTrigAppr();
				double yoyAppr = osim.getYoyAppr();
				double posp = osim.getPosPer() * 100.0;
				//System.out.println("--> BIM = "+String.format("%.3f",bim)+" & SOM = "+String.format("%.3f",som)+
				//	"  :  Trig % = " + String.format("%.4f", osim.getTrigAppr())+
				//	"  |  YoY % = " + String.format("%.4f", osim.getYoyAppr()));
				//update best vals if needed
				boolean good_som_value = true;
				if(som >= 0.97 && som <= 1.03){
					good_som_value = false;
				}
				if(yoyAppr > bestVals2.get(3) && good_som_value){
					ArrayList<Double> newVals = new ArrayList<Double>();
					newVals.add(bim);
					newVals.add(som);
					newVals.add(trigAppr);
					newVals.add(yoyAppr);
					newVals.add(posp);
					bestVals2 = newVals;
				}
			}
		}
		//convert to string AL and return
		ArrayList<String> data = new ArrayList<String>();
		data.add(String.format("%.3f", bestVals2.get(0)));
		data.add(String.format("%.3f", bestVals2.get(1)));
		data.add(String.format("%.5f", bestVals2.get(2)));
		data.add(String.format("%.3f", bestVals2.get(3)));
		data.add(String.format("%.3f", bestVals2.get(4)));
		//System.out.println("========== Done With Calculations ==========");
		return data;
	}
	//do only one section of BIM/SOM but for intent of doing mult
	public ArrayList<String> bsoBySection(OrderSim osim){
		//calc order list and relv data for this specific BIM/SOM combo
		osim.calcOrderList();
		double trigAppr = osim.getTrigAppr();
		double yoyAppr = osim.getYoyAppr();
		double posp = osim.getPosPer() * 100.0;
		//convert to string AL and return
		ArrayList<String> data = new ArrayList<String>();
		data.add(String.format("%.5f", trigAppr));		//[0] Trig %
		data.add(String.format("%.3f", yoyAppr));		//[1] APY %
		data.add(String.format("%.3f", posp));			//[2] Pos %
		return data;

	}

	//convert ttvMask to old ttSwitch (-1 = Train only, 1 = Test only, 0 = both)
	private int convertTT(String ttvMask){
		int ttVal = 1;
		if(ttvMask.charAt(0) == '1' && ttvMask.charAt(1) == '0'){
			ttVal = -1;
		}else if(ttvMask.charAt(0) == '0' && ttvMask.charAt(1) == '1'){
			ttVal = 1;
		}else if(ttvMask.charAt(0) == '1' && ttvMask.charAt(1) == '1'){
			ttVal = 0;
		}else{
			System.out.println("ERR: no dataset selected, default is Test Only.");
		}
		return ttVal;
	}

	/*------------------------------------------------------------------------------------
		Basis Related Functions
	--------------------------------------------------------------------------------------*/

	//CONTROLLER FUNCT: generate basis file for SK
	public void genBasisSK(int skID){
		if(bgm.equals("gad2")){
			genBasisSK_GAD2(skID);
		}else if(bgm.equals("gab3")){
			genBasisSK_GAB3(skID);
		}else if(bgm.equals("ann")){
			genBasisSK_ANN(skID);
		}else{
			System.out.println("ERROR: Invalid BGM in generateBasis()");
		}
	}
	private void genBasisSK_GAD2(int skID){
		//redacted
	}
	private void genBasisSK_GAB3(int skID){
		//redacted
	}
	public void genBasisSK_ANN(int skID){
		if(getFocusSK() != skID || true){
			System.out.print("--> Changed focus from "+getFocusSK());
			setFocusSK(skID);
			System.out.println(" to " + getFocusSK());
		}
		String sdate = kattr.getSDate();
		String edate = AhrDate.getTodaysDate();
		int tvi = kattr.getTVI();
		String msMask = kattr.getMsMask();
		ArrayList<String> dates = AhrDate.getDatesBetween(sdate, edate);
		ArrayList<String> mdates = AhrDate.getDatesThatPassMarketMask(dates, msMask);
		Collections.reverse(mdates);
		//get dbuf for each date, format, then add to their basis file 
		ArrayList<ArrayList<String>> basis = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < mdates.size(); i++){
			ArrayList<ArrayList<String>> dbuf = makePred_ANN(skID, mdates.get(i));
			for(int j = 0; j < dbuf.size(); j++){
				ArrayList<String> tfLine = new ArrayList<String>();
				tfLine.add(mdates.get(i));							//[0] Date
				tfLine.add(String.valueOf(skID));					//[1] Single Key #
				tfLine.add(calcTTV(mdates.get(i)));					//[2] TTV Code
				tfLine.add(dbuf.get(j).get(0));						//[3] Ticker
				tfLine.add(dbuf.get(j).get(1));						//[4] Score
				tfLine.add(dbuf.get(j).get(2+tvi));					//[5] TV Appr
				basis.add(tfLine);
			}
		}
		AhrIO.writeToFile("./../out/sk/baseis/ann/ANN_"+String.valueOf(skID)+".txt", basis, ",");
	}

	//CONTROLLER FUNCT: generate basis file for AK
	public void genBasisAK(){
		if(bgm.equals("gad2")){
			genBasisAK_GAD2();
		}else if(bgm.equals("gab3")){
			genBasisAK_GAB3();
		}else if(bgm.equals("ann")){
			genBasisAK_ANN();
		}else{
			System.out.println("ERROR: Invalid BGM in generateBasis()");
		}
	}
	private void genBasisAK_GAD2(){
		//redacted
	}
	private void genBasisAK_GAB3(){
		//redacted
	}
	private void genBasisAK_ANN(){
		ArrayList<ArrayList<String>> basis = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> laFile = AhrIO.scanFile("./../out/ak/log/ak_log.txt", ",");
		FCI fciLA = new FCI(true, "./../out/ak/log/ak_log.txt");
		ArrayList<String> laRow = new ArrayList<String>();
		for(int i = 1; i < laFile.size(); i++){
			if(this.id == Integer.parseInt(laFile.get(i).get(fciLA.getIdx("ak_num")))){
				laRow = laFile.get(i);
			}
		}
		//get relv info
		String sdate = kattr.getSDate();
		String edate = AhrDate.getTodaysDate();
		int tvi = kattr.getTVI();
		ArrayList<String> dates = AhrDate.getDatesBetween(sdate, edate);
		//itr thru all dates, match it to best SK, get predictions
		for(int i = 0; i < dates.size(); i++){
			int bkOfDate = getSK(dates.get(i));
			setFocusSK(bkOfDate);
			ArrayList<ArrayList<String>> dbuf = makePred_ANN(bkOfDate, dates.get(i));
			for(int j = 0; j < dbuf.size(); j++){
				ArrayList<String> tfLine = new ArrayList<String>();
				tfLine.add(dates.get(i));								//[0] Date
				tfLine.add(String.valueOf(bkOfDate));					//[1] Single Key #
				tfLine.add(calcTTV(dates.get(i)));						//[2] TTV Code
				tfLine.add(dbuf.get(j).get(0));							//[3] ticker
				tfLine.add(dbuf.get(j).get(1));							//[4] Score
				tfLine.add(dbuf.get(j).get(2+tvi));						//[5] TV Appr
				basis.add(tfLine);
			}	
		}
		AhrIO.writeToFile("./../out/ak/baseis/ann/ANN_"+String.valueOf(this.id)+".txt", basis, ",");
	}
	//generate basis file for random sampling
	//probability is val b/w [0-1], is chance of a file added for sampling (slims down files and computation)
	public void genBasisRnd(double probability){
		Random rnd = new Random();
		ArrayList<ArrayList<String>> basis = new ArrayList<ArrayList<String>>();
		//get data from AttributesSK
		String sdate = kattr.getSDate();
		String edate = kattr.getEDate();
		int spd = kattr.getSPD();
		int tvi = kattr.getTVI();
		String msMask = kattr.getMsMask();
		String narMask = kattr.getNarMask();
		//get all possible dates from Clean/ByDate
		String bdPath = "./../../DB_Intrinio/Clean/ByDate/";
		ArrayList<String> bdFilesAll = AhrIO.getNamesInPath(bdPath);
		//only get dates that fit market mask and within date range
		bdFilesAll = AhrDate.getDatesThatPassMarketMask(bdFilesAll, msMask);
		Collections.sort(bdFilesAll);
		ArrayList<String> bdFiles = new ArrayList<String>();
		for(int i = 0; i < bdFilesAll.size(); i++){
			if(AhrDate.isDateInPeriod(bdFilesAll.get(i), sdate, edate)){
				if(rnd.nextDouble() <= probability){
					bdFiles.add(bdFilesAll.get(i));
				}
			}
		}
		//itr thru dates, get lines from Clean DB that fit narMask
		FCI fciBD = new FCI(false, bdPath);
		for(int i = 0; i < bdFiles.size(); i++){
			ArrayList<ArrayList<String>> bdFC = AhrIO.scanFile(bdPath+bdFiles.get(i)+".txt", "~");
			ArrayList<ArrayList<String>> rndLines = new ArrayList<ArrayList<String>>();
			while(rndLines.size() < spd){
				int rndIdx = rnd.nextInt(bdFC.size());
				String itrTick = bdFC.get(rndIdx).get(fciBD.getIdx("ticker"));
				String itrNar = bdFC.get(rndIdx).get(fciBD.getIdx("nar_mask"));
				String tvColName = Globals.tvi_names[tvi]; 
				String itrAppr = bdFC.get(rndIdx).get(fciBD.getIdx(tvColName));
				boolean add_line = true;
				if(!AhrGen.compareMasks(narMask, itrNar)){
					add_line = false;
				}
				if(AhrAL.getCol(rndLines, 0).contains(itrTick)){
					add_line = false;	
				}
				if(add_line){
					ArrayList<String> line = new ArrayList<String>();
					line.add(bdFiles.get(i));						//[0] date
					line.add("0");									//[1] SK 
					line.add(calcTTV(bdFiles.get(i)));				//[2] TTV
					line.add(itrTick);								//[3] Ticker
					line.add(String.valueOf(rndLines.size()));		//[4] Score (rank)
					line.add(itrAppr);								//[5] TVIs % Appr
					rndLines.add(line);
				}
			}
			//translate rndLines over to basis
			for(int j = 0; j < rndLines.size(); j++){
				basis.add(rndLines.get(j));
			}
		}
		//write RND SK data to keys_struct
		String ksPath = "./../out/sk/log/rnd/keys_struct.txt";
		FCI fciKS = new FCI(true, ksPath);
		ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile(ksPath, ",");
		int skNum = -1;
		for(int i = 1; i < ksFile.size(); i++){
			int itrNum = Integer.parseInt(ksFile.get(i).get(fciKS.getIdx("sk_num")));
			if(itrNum > skNum){
				skNum = itrNum;
			}
		}
		skNum++;
		ArrayList<String> ksRow = new ArrayList<String>();
		ksRow.add(String.valueOf(skNum));							//[0] SK Num
		ksRow.add("IT");											//[1] DB Used
		ksRow.add(AhrDate.getTodaysDate());							//[2] Date Ran
		ksRow.add(sdate);											//[3] Start Date
		ksRow.add(edate);											//[4] End Date
		ksRow.add("1");												//[5] Call
		ksRow.add(String.valueOf(spd));								//[6] SPD
		ksRow.add(String.valueOf(tvi));								//[7] TVI
		ksRow.add(msMask);											//[8] MS Mask
		ksRow.add(narMask);											//[9] NAR Mask
		ksFile.add(ksRow);
		AhrIO.writeToFile(ksPath, ksFile, ",");
		//calc APAPT and Pos % of basis lines
		double apapt = 0.0;
		double posp = 0.0;
		for(int i = 0; i < basis.size(); i++){
			double itrAppr = 0.0;
			try{
				itrAppr = Double.parseDouble(basis.get(i).get(5));
			}catch(NumberFormatException e){
			}
			apapt += itrAppr;
			if(itrAppr > 0.0){
				posp += 1.0;
			}
		}
		apapt = apapt / (double)basis.size();
		posp = (posp / (double)basis.size()) * 100.0;
		//write basis to file and add to ./../out/sk/log/rnd/key_perf.txt
		String kpPath = "./../out/sk/log/rnd/keys_perf.txt";
		FCI fciKP = new FCI(true, kpPath);
		ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");
		ArrayList<String> kpLine = new ArrayList<String>();
		kpLine.add(String.valueOf(skNum));					//[0] SK Num
		kpLine.add("1");									//[1] Call
		kpLine.add(String.valueOf(spd));					//[2] SPD
		kpLine.add(String.valueOf(tvi));					//[3] TVI
		kpLine.add(msMask);									//[4] MS Mask
		kpLine.add(narMask);								//[5] NAR Mask
		kpLine.add("tbd");									//[6] BIM	
		kpLine.add("tbd");									//[7] SOM
		kpLine.add("tbd");									//[8] BSO APAPT
		kpLine.add("tbd");									//[9] BSO Pos %
		kpLine.add(String.format("%.5f", apapt));			//[10] True APAPT
		kpLine.add(String.format("%.3f", posp));			//[11] True Pos %
		kpFile.add(kpLine);
		AhrIO.writeToFile(kpPath, kpFile, ",");
		AhrIO.writeToFile("./../out/sk/baseis/rnd/RND_"+String.valueOf(skNum)+".txt", basis, ",");
	}
	public void genBasisRnd(String sdate, String edate, int spd, int tvi, String msMask, String narMask, String filter){
		ArrayList<ArrayList<String>> basis = new ArrayList<ArrayList<String>>();
	}


	//CONTROLLER FUNCT: update a basis file, keeping the old data intact
	public void updateBasisSK(int skID){
		if(bgm.equals("gad2")){
			updateBasisSK_GAD2(skID);
		}else if(bgm.equals("gab3")){
			updateBasisSK_GAB3(skID);
		}else if(bgm.equals("ann")){
			updateBasisSK_ANN(skID);
		}else{
			System.out.println("ERR: wrong BGM type in updateBasisSK()");
		}
	}
	private void updateBasisSK_GAD2(int skID){
		//redacted
	}
	private void updateBasisSK_GAB3(int skID){
		//redacted
	}
	private void updateBasisSK_ANN(int skID){
		if(getFocusSK() != skID){
			setFocusSK(skID);
		}
		ArrayList<ArrayList<String>> basis = new ArrayList<ArrayList<String>>();
		String msMask = kattr.getMsMask();
		String bsPath = "./../out/sk/baseis/";
		FCI fciBS = new FCI(false, bsPath);
		ArrayList<ArrayList<String>> fc = AhrIO.scanFile(bsPath+"ann/ANN_"+String.valueOf(skID)+".txt", ",");
		//get dates needed to update
		String mrdate = fc.get(fc.size()-1).get(fciBS.getIdx("date"));
		String edate = AhrDate.getTodaysDate();
		ArrayList<String> dates = AhrDate.getDatesBetween(mrdate, edate);
		Collections.sort(dates);
		dates.remove(0);
		if(dates.size() < 1){
			System.out.println("--> ANN SK"+skID+" ... ALREADY UP-TO-DATE");
			return;
		}
		//find dates that have tbd vals from last update
		for(int i = 0; i < fc.size(); i++){
			String itrAppr = fc.get(i).get(fciBS.getIdx("appr"));
			if(itrAppr.equals("tbd")){
				dates.add(fc.get(i).get(fciBS.getIdx("date")));
			}
		}
		//get list of new dates that matches MS mask of SK or already have tdb in file
		ArrayList<String> mdates = AhrDate.getDatesThatPassMarketMask(dates, msMask);
		Collections.sort(mdates);
		//moves lines from old basis file
		for(int i = 0; i < fc.size(); i++){
			String itrDate = fc.get(i).get(fciBS.getIdx("date"));
			if(!mdates.contains(itrDate)){
				basis.add(fc.get(i));
			}
		}
		//create new lines
		int tvi = kattr.getTVI();
		for(int i = 0; i < mdates.size(); i++){
			ArrayList<ArrayList<String>> dbuf = makePred_ANN(skID, mdates.get(i));
			for(int j = 0; j < dbuf.size(); j++){
				ArrayList<String> tfLine = new ArrayList<String>();
				tfLine.add(mdates.get(i));								//[0] Date
				tfLine.add(String.valueOf(skID));						//[1] Single Key #
				tfLine.add(calcTTV(mdates.get(i)));						//[2] TTV Code
				tfLine.add(dbuf.get(j).get(0));							//[3] Ticker
				tfLine.add(dbuf.get(j).get(1));							//[4] Distance (score)
				tfLine.add(dbuf.get(j).get(2+tvi));						//[5] TV Appr
				basis.add(tfLine);
			}
		}
		AhrIO.writeToFile(bsPath+"ann/ANN_"+String.valueOf(skID)+".txt", basis, ",");
		System.out.println("--> ANN SK"+skID+" ... UPDATED");
	}

	//updates a AK basis file
	public void updateBasisAK(){
		ArrayList<ArrayList<String>> basis = new ArrayList<ArrayList<String>>();
		String alPath = "./../out/ak/log/ak_log.txt";
		ArrayList<String> alRow = AhrIO.scanRow(alPath, ",", String.valueOf(this.id));
		FCI fciAL = new FCI(true, alPath);
		String bgmLC = kattr.getBGM();
		String bgmUC = bgmLC.toUpperCase();
		//update single keys first
		String[] skeys = alRow.get(fciAL.getIdx("best_keys")).split("~");
		for(int i = 0; i < skeys.length; i++){
			updateBasisSK(Integer.parseInt(skeys[i]));
		}
		//get dates needed for update
		String akPath = "./../out/ak/baseis/"+bgmLC+"/"+bgmUC+"_"+String.valueOf(this.id)+".txt";
		ArrayList<ArrayList<String>> fc = AhrIO.scanFile(akPath, ",");
		FCI fciBS = new FCI(false, "./../out/ak/baseis/");
		String mrDate = fc.get(fc.size()-1).get(fciBS.getIdx("date"));
		String edate = AhrDate.getTodaysDate();
		ArrayList<String> dates = AhrDate.getDatesBetween(mrDate, edate);
		if(dates.size() <= 1){
			System.out.println("--> "+bgmUC+" AK"+this.id+" ... already UP-TO-DATE.");
			return;
		}
		//find dates that have tbd vals from last update
		for(int i = 0; i < fc.size(); i++){
			String itrAppr = fc.get(i).get(fciBS.getIdx("appr"));
			if(itrAppr.equals("tbd")){
				dates.add(fc.get(i).get(fciBS.getIdx("date")));
			}
		}
		Collections.sort(dates);
		//transfer old lines from basis file
		for(int i = 0; i < fc.size(); i++){
			String itrDate = fc.get(i).get(fciBS.getIdx("date"));
			if(!dates.contains(itrDate)){
				basis.add(fc.get(i));
			}
		}	
		//update, add new lines
		fciBS = new FCI(false, "./../out/sk/baseis/");
		for(int i = 0; i < dates.size(); i++){
			int skNum = getSK(dates.get(i));
			String skPath = "./../out/sk/baseis/"+bgmLC+"/"+bgmUC+"_"+String.valueOf(skNum);
			skPath += ".txt";
			ArrayList<ArrayList<String>> skRows = AhrIO.scanSelectRows(skPath, ",", dates.get(i), fciBS.getIdx("date"));
			for(int j = 0; j < skRows.size(); j++){
				basis.add(skRows.get(j));
			}
		}
		//write to file
		AhrIO.writeToFile(akPath, basis, ",");
		System.out.println("--> "+bgmUC+" AK"+this.id+" ... UPDATED");
	}

	//calc TTV for a given date
	public String calcTTV(String date){
		String ttvCode = "2";
		if(AhrDate.isDateInPeriod(date, getSDate(), getEDate())){
			int day = Integer.parseInt(date.split("-")[2]);
			if(day%2 == 0){
				ttvCode = "0";	//is train date
			}else{
				ttvCode = "1";	//is test date
			}
		}
		return ttvCode;	
	}

	/*-------------------------------------------------------------------
		GENERAL FUNCTIONS
	--------------------------------------------------------------------*/

	//CONTROLLER FUNCT: decode a binary string and return its components
	public ArrayList<ArrayList<String>> decodeBGS(){
		ArrayList<ArrayList<String>> comps = new ArrayList<ArrayList<String>>();
		if(bgm.equals("gad2")){
			comps = decodeBGS_GAD2();
		}else if(bgm.equals("gab2")){
			comps = decodeBGS_GAB2();
		}else if(bgm.equals("gab3")){
			comps = decodeBGS_GAB3();
		}
		return comps;
	}
	private ArrayList<ArrayList<String>> decodeBGS_GAD2(){
		//redacted
		return new ArrayList<ArrayList<String>>();
	}
	private ArrayList<ArrayList<String>> decodeBGS_GAB2(){
		//redacted
		return new ArrayList<ArrayList<String>>();
	}
	private ArrayList<ArrayList<String>> decodeBGS_GAB3(){
		//redacted
		return new ArrayList<ArrayList<String>>();
	}
	
	//print out BuyIn info for a given agg key (AK)
	public void printInfo(){
		System.out.println("============ Single Keys ================");
		for(int i = 0; i < this.bestSK.length; i++){
			System.out.println("--> SK "+this.bestSK[i]+" : BIM = "+this.skBuyIn[i][0]+"  |  SOM = "+this.skBuyIn[i][1]);
		}
		System.out.println("=========== Agg Key "+this.id+" =================");
		System.out.println("--> BIM = "+this.akBuyIn[0]+"\n--> SOM = "+this.akBuyIn[1]);
		
	}

}
