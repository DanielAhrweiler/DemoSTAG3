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
	private boolean is_long;		//is call long or short
	private int id;					//id num for either the AK or SK
	private int focusSK;			//SK that class is currently focused on (has vals stored)
	private int[] bestSK;			//best SKs that provide 100% cov of an AK
	private double[][] skBuyIn;		//opt BIM/SOM pair for every SK
	private double[] akBuyIn;		//opt BIM/SOM pair for 1 AK
	private ArrayList<ArrayList<String>> statesSK;	//all best SK and their ms states

	ANN ann;

	//contructors
	public BGM_Manager(){
		
	}
	public BGM_Manager(String bgmVal){
		this.bgm = bgmVal;
	}
	public BGM_Manager(String bgmVal, int idVal){//if SK, need BGM and basis num (id)
		this.bgm = bgmVal;
		this.is_ak = false;
		this.id = idVal;	
		setFocusSK(idVal);
	}	
	public BGM_Manager(int idVal){//if is AK just need basis num (id)
		this.is_ak = true;
		this.id = idVal;
		//get info from agg basis log file
		ArrayList<ArrayList<String>> aggLog = AhrIO.scanFile("./../baseis/log/ak_log.txt", ",");
		ArrayList<String> aggLine = new ArrayList<String>();
		FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
		for(int i = 1; i < aggLog.size(); i++){
			int bnum = Integer.parseInt(aggLog.get(i).get(fciAL.getIdx("basis_num")));
			if(bnum == this.id){
				this.bgm = aggLog.get(i).get(fciAL.getIdx("bgm"));
				aggLine = aggLog.get(i);
			}
		}
		String bgmLC = this.bgm.toLowerCase();
		//set call
		String callChar = aggLine.get(fciAL.getIdx("call"));
		this.is_long = false;
		if(callChar.equals("1")){
			this.is_long = true;
		}
		//set list of best single keys for this agg key
		String[] bestSKstr = aggLine.get(fciAL.getIdx("best_keys")).split("~");
		this.bestSK = new int[bestSKstr.length];
		for(int i = 0; i < bestSKstr.length; i++){
			this.bestSK[i] = Integer.parseInt(bestSKstr[i]);
		}
		//set best BIM/SOM for each best single key (if calced yet)
		if(!aggLine.get(fciAL.getIdx("sk_bim_som")).equals("ph")){
			String[] skBimSom = aggLine.get(fciAL.getIdx("sk_bim_som")).split("~");
			this.skBuyIn = new double[skBimSom.length][2];
			for(int i = 0; i < skBimSom.length; i++){
				String[] skPair = skBimSom[i].split("\\|");
				this.skBuyIn[i][0] = Double.parseDouble(skPair[0]);
				this.skBuyIn[i][1] = Double.parseDouble(skPair[1]);
			}
		}
		//set best BIM/SOM for agg key in general (if calced yet)
		if(!aggLine.get(fciAL.getIdx("ak_bim_som")).equals("ph")){
			String[] akPair = aggLine.get(fciAL.getIdx("ak_bim_som")).split("\\|");
			this.akBuyIn = new double[2];
			this.akBuyIn[0] = Double.parseDouble(akPair[0]);
			this.akBuyIn[1] = Double.parseDouble(akPair[1]);
		}
		//couple each SK with its market state mask
		this.statesSK = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile("./../out/ml/"+bgmLC+"/keys_struct.txt", ",");
		FCI fciKS = new FCI(true, "./../out/ml/"+bgmLC+"/keys_struct.txt");
		ArrayList<String> ksKeyCol = AhrIO.scanCol("./../out/ml/"+bgmLC+"/keys_struct.txt", ",", fciKS.getIdx("key_num"));
		for(int i = 0; i < bestSK.length; i++){
			ArrayList<String> stateLine = new ArrayList<String>();
			stateLine.add(String.valueOf(bestSK[i]));
			int ksIdx = ksKeyCol.indexOf(String.valueOf(bestSK[i]));
			if(ksIdx != -1){
				stateLine.add(ksFile.get(ksIdx).get(fciKS.getIdx("ms_mask")));
			}else{
				System.out.println("ERROR: SK"+this.bestSK[i]+" not found in "+bgmLC+"/keys_struct.txt");
				stateLine.add("");
			}
			this.statesSK.add(stateLine);	
		}
	
		//focus on a single key (also inits a BGM algo)
		if(bestSK.length > 0){
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
		return this.is_long;
	}
	public int getFocusSK(){
		return this.focusSK;
	}
	public int[] getSKs(){
		return this.bestSK;
	}
	public int getSK(String date){
		//get market state of QQQ for given date
		ArrayList<ArrayList<String>> mstates = AhrIO.scanFile("./../in/mstates.txt", ",");
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		ArrayList<String> msLine = new ArrayList<String>();
		for(int i = 0; i < mstates.size(); i++){
			if(date.equals(mstates.get(i).get(fciMS.getIdx("date")))){
				msLine = mstates.get(i);
				break;
			}
		}
		if(msLine.size() == 0){
			System.out.println("ERR: date not in mstates ("+date+")");
		}
		String msOfDate = msLine.get(fciMS.getIdx("ms_mask"));
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
		ArrayList<ArrayList<String>> mstates = AhrIO.scanFile("./../in/mstates.txt", ",");
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		ArrayList<String> msLine = new ArrayList<String>();
		for(int i = 0; i < mstates.size(); i++){
			if(date.equals(mstates.get(i).get(fciMS.getIdx("date")))){
				msLine = mstates.get(i);
				break;
			}
		}
		if(msLine.size() == 0){
			System.out.println("ERR: date not in mstates ("+date+")");
		}
		String msOfDate = msLine.get(fciMS.getIdx("ms_mask"));
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
		if(this.bgm.equals("ANN") && this.ann != null){
			return this.ann.getSPD();
		}else{
			return -1;
		}
	}
	public int getTVI(){
		if(this.bgm.equals("ANN") && this.ann != null){
			return this.ann.getTVI();
		}else{
			return -1;
		}
	}
	public String getSDate(){
		if(this.bgm.equals("ANN") && this.ann != null){
			return this.ann.getSDate();
		}else{
			return "";
		}
	}
	public String getEDate(){
		if(this.bgm.equals("ANN") && this.ann != null){
			return this.ann.getEDate();
		}else{
			return "";
		}
	}
	public String getMsMask(){
		if(this.bgm.equals("ANN") && this.ann != null){
			return this.ann.getMsMask();
		}else{
			return "";
		}
	}
	public ArrayList<ArrayList<String>> getStatesSK(){
		return this.statesSK;
	}	

	//setters
	public void setBGM(String bgmVal){
		this.bgm = bgmVal;
	}
	public void setFocusSK(int skID){
		if(this.bgm.equals("ANN")){
			this.ann = new ANN(skID);
			this.focusSK = skID;
		}
	}
	public void setFocusSK(String date){//set focus to best SK for this date
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
		if(bgm.equals("GAD2")){
			dbuf = makePred_GAD2(skID, date);
		}else if(bgm.equals("GAB3")){
			dbuf = makePred_GAB3(skID, date);
		}else if(bgm.equals("ANN")){
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
		boolean isLong = ann.getCall();
		int spd = ann.getSPD();
		int tvi = ann.getTVI();
		int tvn = Globals.target_var_num;
		double plateau = ann.getPlateau();
		String narMask = ann.getNarMask();
		String indMask = ann.getIndMask();
		Network network = new Network("./../out/ml/ann/structure/struct_"+String.valueOf(skID)+".txt");
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
			line.add("0.0");								// [2] 1 day intra %
			line.add("0.0");								// [3] 1 day inter %
			line.add("0.0");								// [4] 2 day inter %
			line.add("0.0");								// [5] 3 day inter %
			line.add("0.0");								// [6] 5 day inter %
			line.add("0.0");								// [7] 10 day inter %
			dbuf.add(line);
		}	
		//calc score
		//itr thru all clean lines of this date, getting all lines with best score
		String tviCol = "appr"+String.valueOf(fciBD.convertTVI(tvi));
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
			if(!narClean.get(i).get(fciBD.getIdx(tviCol)).equals("tbd")){
				tvAppr = Double.parseDouble(narClean.get(i).get(fciBD.getIdx(tviCol)));
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
						String apprCol = "appr"+String.valueOf(fciBD.convertTVI(j));
						double appr = 0.0;
						if(!narClean.get(i).get(fciBD.getIdx(apprCol)).equals("tbd")){
							appr = Double.parseDouble(narClean.get(i).get(fciBD.getIdx(apprCol)));
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
						String apprCol = "appr"+String.valueOf(fciBD.convertTVI(j));
						double appr = 0.0;
						if(!narClean.get(i).get(fciBD.getIdx(apprCol)).equals("tbd")){
							appr = Double.parseDouble(narClean.get(i).get(fciBD.getIdx(apprCol)));
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
		if(bgm.equals("GAD2")){
			dbuf = makePred_GAD2(skID, date, inSPD);
		}else if(bgm.equals("GAB3")){
			dbuf = makePred_GAB3(skID, date, inSPD);
		}else if(bgm.equals("ANN")){
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
		boolean isLong = ann.getCall();
		int spd = ann.getSPD();
		int tvi = ann.getTVI();
		int tvn = Globals.target_var_num;
		double plateau = ann.getPlateau();
		String narMask = ann.getNarMask();
		String indMask = ann.getIndMask();
		Network network = new Network("./../out/ml/ann/structure/struct_"+String.valueOf(skID)+".txt");
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
			line.add("0.0");								// [2] 1 day intra %
			line.add("0.0");								// [3] 1 day inter %
			line.add("0.0");								// [4] 2 day inter %
			line.add("0.0");								// [5] 3 day inter %
			line.add("0.0");								// [6] 5 day inter %
			line.add("0.0");								// [7] 10 day inter %
			dbuf.add(line);
		}	
		//calc score
		//itr thru all clean lines of this date, getting all lines with best score
		String tviCol = "appr"+String.valueOf(fciBD.convertTVI(tvi));
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
			if(!narClean.get(i).get(fciBD.getIdx(tviCol)).equals("tbd")){
				tvAppr = Double.parseDouble(narClean.get(i).get(fciBD.getIdx(tviCol)));
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
						String apprCol = "appr"+String.valueOf(fciBD.convertTVI(j));
						double appr = 0.0;
						if(!narClean.get(i).get(fciBD.getIdx(apprCol)).equals("tbd")){
							appr = Double.parseDouble(narClean.get(i).get(fciBD.getIdx(apprCol)));
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
						String apprCol = "appr"+String.valueOf(fciBD.convertTVI(j));
						double appr = 0.0;
						if(!narClean.get(i).get(fciBD.getIdx(apprCol)).equals("tbd")){
							appr = Double.parseDouble(narClean.get(i).get(fciBD.getIdx(apprCol)));
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
		if(bgm.equals("GAD2")){
			score = calcScore_GAD2(skID, date, ticker);
		}else if(bgm.equals("GAB3")){
			score = calcScore_GAB3(skID, date, ticker);
		}else if(bgm.equals("ANN")){
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
		String indMask = ann.getIndMask();
		Network network = new Network("./../out/ml/ann/structure/struct_"+String.valueOf(skID)+".txt");
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
			if(this.bgm.equals(spRow.get(0)) && skID == Integer.parseInt(spRow.get(1))){
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
		String bgmUC = this.bgm;
		String bgmLC = bgmUC.toLowerCase();
		String spPath = "./../out/score_percentiles.txt";
		String laPath = "./../baseis/log/ak_log.txt";
		FCI fciLA = new FCI(true, laPath);
		ArrayList<ArrayList<String>> spFile = AhrIO.scanFile(spPath, ",");
		ArrayList<String> laRow = AhrIO.scanRow(laPath, ",", String.valueOf(this.id));
		String call = laRow.get(fciLA.getIdx("call"));
		String sdate = laRow.get(fciLA.getIdx("start_date"));
		String edate = laRow.get(fciLA.getIdx("end_date"));
		FCI fciSK = new FCI(false, "./../baseis/single/");
		for(int i = 0; i < this.bestSK.length; i++){
			String skNum = String.valueOf(this.bestSK[i]);
			String skPath = "./../baseis/single/"+bgmLC+"/"+bgmUC+"_"+skNum+".txt";
			int skIdx = -1;
			for(int j = 0; j < spFile.size(); j++){
				String bgmItr = spFile.get(j).get(0);
				String skItr = spFile.get(j).get(1);
				if(bgmItr.equals(this.bgm) && skItr.equals("skNum")){
					skIdx = j;
					break;
				}
			}
			if(skIdx == -1){
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
				if(this.bgm.equals("GAB3") || (this.bgm.equals("ANN") && call.equals("1"))){
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
				line.add(this.bgm);
				line.add(skNum);
				for(int j = 0; j < ptiles.size(); j++){
					line.add(String.valueOf(ptiles.get(j)));
				}
				spFile.add(line);
			}
		}
		AhrIO.writeToFile("./../out/score_percentiles.txt", spFile, ",");
	}	

	/*-------------------------------------------------------------------------------------
		Basic Key Perf Related Functions
	---------------------------------------------------------------------------------------*/

	//calcs basics perf values to write to keys_perf file right after SK is generated
	public void keyPerfToFile(){
		String bgmLC = this.bgm.toLowerCase();
		String kpPath = "./../out/ml/"+bgmLC+"/keys_perf.txt";
		FCI fciKP = new FCI(true, kpPath);
		ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");
		ArrayList<String> kpRow = AhrAL.getRow(kpFile, String.valueOf(id));
		ArrayList<String> perf = keyPerformance();
		kpRow.set(fciKP.getIdx("train_p_apapt"), perf.get(0));
		kpRow.set(fciKP.getIdx("test_p_apapt"), perf.get(3));
		kpRow.set(fciKP.getIdx("train_t_apapt"), perf.get(1));
		kpRow.set(fciKP.getIdx("test_t_apapt"), perf.get(4));
		kpRow.set(fciKP.getIdx("train_posp"), perf.get(2));
		kpRow.set(fciKP.getIdx("test_posp"), perf.get(5));
		int rowIdx = AhrAL.getRowIdx(kpFile, String.valueOf(id));
		kpFile.set(rowIdx, kpRow);
		AhrIO.writeToFile(kpPath, kpFile, ",");
	}

	//CONTROLLER FUNCTION: calc the perf vals of a SK
	public ArrayList<String> keyPerformance(){
		ArrayList<String> data = new ArrayList<String>();
		if(bgm.equals("GAD2")){
			data = keyPerfSK_GAD2();
		}else if(bgm.equals("GAB3")){
			data = keyPerfSK_GAB3();
		}else if(bgm.equals("ANN")){
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
		Network knet = new Network("./../out/ml/ann/structure/struct_"+id+".txt");
		boolean isLong = ann.getCall();
		double plateau = ann.getPlateau();
		int spd = ann.getSPD();
		int tvi = ann.getTVI();
		String msMask = ann.getMsMask();
		String indMask = ann.getIndMask();
		String sdate = ann.getSDate();
		String edate = ann.getEDate();
		ArrayList<String> dates = AhrDate.getDatesBetween(sdate, edate);
		//add dates w/ matching MS to mdates
		ArrayList<String> mdates = new ArrayList<String>();
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		ArrayList<ArrayList<String>> mstates = AhrIO.scanFile("./../in/mstates.txt", ",");
		ArrayList<String> msDates = AhrAL.getCol(mstates, fciMS.getIdx("date"));
		for(int i = 0; i < dates.size(); i++){
			int msIdx = msDates.indexOf(dates.get(i));
			String msOfDate = mstates.get(msIdx).get(2);
			if(AhrGen.compareMasks(msMask, msOfDate)){
				mdates.add(dates.get(i));
			}
		}
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
	
	/*-------------------------------------------------------------------------------------
		BIM/SOM Related Functions
	---------------------------------------------------------------------------------------*/

	//calcs BIM/SOM values to write to keys_perf file right after SK is generated
	public void bsoPerfToFile(boolean is_min_trig, boolean use_sk_bso){
		String bgmLC = this.bgm.toLowerCase();
		//calc bso files
		ArrayList<String> bsoTrain = bsoMultiple(getSDate(), getEDate(), "100", is_min_trig, use_sk_bso);
		ArrayList<String> bsoTest = bsoMultiple(getSDate(), getEDate(), "010", is_min_trig, use_sk_bso);
		System.out.println("==> BSO Train Data: " + bsoTrain);
		System.out.println("==> BSO Test Data: " + bsoTest);
		//get row from keys_perf
		String kpPath = "./../out/ml/"+bgmLC+"/keys_perf.txt";
		FCI fciKP = new FCI(true, kpPath);
		ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");		
		ArrayList<String> kpRow = AhrAL.getRow(kpFile, String.valueOf(id));
		int rowIdx = AhrAL.getRowIdx(kpFile, String.valueOf(id));
		//add bso vals to row and write back to file
		kpRow.set(fciKP.getIdx("bim"), bsoTest.get(0));
		kpRow.set(fciKP.getIdx("som"), bsoTest.get(1));
		kpRow.set(fciKP.getIdx("train_bs_apapt"), bsoTrain.get(2));
		kpRow.set(fciKP.getIdx("test_bs_apapt"), bsoTest.get(2));
		kpRow.set(fciKP.getIdx("train_bs_posp"), bsoTrain.get(4));
		kpRow.set(fciKP.getIdx("test_bs_posp"), bsoTest.get(4));
		kpFile.set(rowIdx, kpRow);
		AhrIO.writeToFile(kpPath, kpFile, ",");
	}
		
	//calc the optimized prices to a buy-in and sell-out pair, runs thru a basis file
	public ArrayList<ArrayList<String>> bsoSingle(String sdate, String edate, double bim, double som, String ttvMask,
										boolean is_min_trig, boolean use_sk_bso){
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		OrderSim osim;
		if(is_ak){
			osim = new OrderSim(id);
		}else{
			osim = new OrderSim(bgm, id);
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
		genVals.add(String.format("%.3f", osim.getPosPer()));		//[5] Pos %
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
			osim = new OrderSim(bgm, id);
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
				if(som >= 0.981 && som <= 1.029){
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
				allVals.add(tfLine);
				if(yoyAppr > bestVals.get(3) && good_som_value){
					bestVals = itrVals;
				}
			} 
		}
		AhrIO.writeToFile("./../data/bso/multiple.txt", allVals, ",");
		//System.out.println("\n--> Best Vals 1st Round : " + bestVals);
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
				if(som >= 0.981 && som <= 1.029){
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
		if(bgm.equals("GAD2")){
			genBasisSK_GAD2(skID);
		}else if(bgm.equals("GAB3")){
			genBasisSK_GAB3(skID);
		}else if(bgm.equals("ANN")){
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
		if(getFocusSK() != skID){
			System.out.print("--> Changed focus from "+getFocusSK());
			setFocusSK(skID);
			System.out.println(" to " + getFocusSK());
		}
		String sdate = ann.getSDate();
		String edate = AhrDate.getTodaysDate();
		String msMask = ann.getMsMask();
		ArrayList<String> dates = AhrDate.getDatesBetween(sdate, edate);
		//get list of dates that match with ms_mask of this SK
		ArrayList<String> mdates = new ArrayList<String>();	//matched dates
		ArrayList<ArrayList<String>> mstates = AhrIO.scanFile("./../in/mstates.txt", ",");
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		for(int i = 0; i < mstates.size(); i++){
			boolean date_in_period = false;
			String itrDate = mstates.get(i).get(fciMS.getIdx("date"));
			if(AhrDate.isDateInPeriod(itrDate, sdate, edate)){
				String itrState = mstates.get(i).get(fciMS.getIdx("ms_mask"));
				if(AhrGen.compareMasks(msMask, itrState)){
					mdates.add(itrDate);
				}
			}
		}
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
				tfLine.add(dbuf.get(j).get(2));						//[5] 1-day Intra %
				tfLine.add(dbuf.get(j).get(3));						//[6] 1-day Inter %
				tfLine.add(dbuf.get(j).get(4));						//[7] 2-day Inter %
				tfLine.add(dbuf.get(j).get(5));						//[8] 3-day Inter %
				tfLine.add(dbuf.get(j).get(6));						//[9] 5-day Inter %
				tfLine.add(dbuf.get(j).get(7));						//[10] 10-day Inter %
				basis.add(tfLine);
			}
		}
		AhrIO.writeToFile("./../baseis/single/ann/ANN_"+String.valueOf(skID)+".txt", basis, ",");
	}

	//CONTROLLER FUNCT: generate basis file for SK
	public void genBasisAK(){
		if(bgm.equals("GAD2")){
			genBasisAK_GAD2();
		}else if(bgm.equals("GAB3")){
			genBasisAK_GAB3();
		}else if(bgm.equals("ANN")){
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
		ArrayList<ArrayList<String>> aggLog = AhrIO.scanFile("./../baseis/log/ak_log.txt", ",");
		FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
		ArrayList<String> aggLine = new ArrayList<String>();
		for(int i = 1; i < aggLog.size(); i++){
			if(this.id == Integer.parseInt(aggLog.get(i).get(fciAL.getIdx("basis_num")))){
				aggLine = aggLog.get(i);
			}
		}
		//get relv info
		String sdate = "2009-10-16";
		String edate = AhrDate.getTodaysDate();
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
				tfLine.add(dbuf.get(j).get(2));							//[5] 1 day intra %
				tfLine.add(dbuf.get(j).get(3));							//[6] 1 day inter %
				tfLine.add(dbuf.get(j).get(4));							//[7] 2 day inter %
				tfLine.add(dbuf.get(j).get(5));							//[8] 3 day inter %
				tfLine.add(dbuf.get(j).get(6));							//[9] 5 day inter %
				tfLine.add(dbuf.get(j).get(7));							//[10] 10 day inter %
				basis.add(tfLine);
			}	
		}
		AhrIO.writeToFile("./../baseis/aggregated/ann/ANN_"+String.valueOf(this.id)+".txt", basis, ",");
	}

	//CONTROLLER FUNCT: update a basis file, keeping the old data intact
	public void updateBasisSK(int skID){
		if(bgm.equals("GAD2")){
			updateBasisSK_GAD2(skID);
		}else if(bgm.equals("GAB3")){
			updateBasisSK_GAB3(skID);
		}else if(bgm.equals("ANN")){
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
		String msMask = ann.getMsMask();
		FCI fciBS = new FCI(false, "./../baseis/single/");
		String path = "./../baseis/single/ann/ANN_"+String.valueOf(skID)+".txt";
		ArrayList<ArrayList<String>> fc = AhrIO.scanFile(path, ",");
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
		for(int i = fc.size()-1; i >= 0; i--){
			if(fc.get(i).contains("tbd")){
				if(!dates.contains(fc.get(i).get(fciBS.getIdx("date")))){
					dates.add(fc.get(i).get(fciBS.getIdx("date")));
				}
			}else{
				double a10 = Double.parseDouble(fc.get(i).get(fciBS.getIdx("appr10")));
				if(a10 == 0.0){
					if(!dates.contains(fc.get(i).get(fciBS.getIdx("date")))){
						dates.add(fc.get(i).get(fciBS.getIdx("date")));
					}
				}else{
					break;
				}
			}
		}
		//get list of new dates that matches MS mask of SK or already have tdb in file
		ArrayList<String> mdates = AhrGen.getDatesThatPassMarketMask(dates, msMask);
		Collections.sort(mdates);
		//moves lines from old basis file
		for(int i = 0; i < fc.size(); i++){
			String itrDate = fc.get(i).get(fciBS.getIdx("date"));
			if(!mdates.contains(itrDate)){
				basis.add(fc.get(i));
			}
		}
		//create new lines
		for(int i = 0; i < mdates.size(); i++){
			ArrayList<ArrayList<String>> dbuf = makePred_ANN(skID, mdates.get(i));
			for(int j = 0; j < dbuf.size(); j++){
				ArrayList<String> tfLine = new ArrayList<String>();
				tfLine.add(mdates.get(i));								//[0] Date
				tfLine.add(String.valueOf(skID));						//[1] Single Key #
				tfLine.add(calcTTV(mdates.get(i)));						//[2] TTV Code
				tfLine.add(dbuf.get(j).get(0));							//[3] Ticker
				tfLine.add(dbuf.get(j).get(1));							//[4] Distance (score)
				tfLine.add(dbuf.get(j).get(2));							//[5] 1 day intra %
				tfLine.add(dbuf.get(j).get(3));							//[6] 1 day inter %
				tfLine.add(dbuf.get(j).get(4));							//[7] 2 day inter %
				tfLine.add(dbuf.get(j).get(5));							//[8] 3 day inter %
				tfLine.add(dbuf.get(j).get(6));							//[9] 5 day inter %
				tfLine.add(dbuf.get(j).get(7));							//[10] 10 day inter %
				basis.add(tfLine);
			}
		}
		AhrIO.writeToFile(path, basis, ",");
		System.out.println("--> ANN SK"+skID+" ... UPDATED");
	}

	//updates a AK basis file
	public void updateBasisAK(){
		ArrayList<ArrayList<String>> basis = new ArrayList<ArrayList<String>>();
		ArrayList<String> alRow = AhrIO.scanRow("./../baseis/log/ak_log.txt", ",", String.valueOf(this.id));
		FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
		String bgmUC = this.bgm;
		String bgmLC = bgmUC.toLowerCase();
		//update single keys first
		String[] skeys = alRow.get(fciAL.getIdx("best_keys")).split("~");
		for(int i = 0; i < skeys.length; i++){
			updateBasisSK(Integer.parseInt(skeys[i]));
		}
		//get dates needed for update
		String akPath = "./../baseis/aggregated/"+bgmLC+"/"+bgmUC+"_"+String.valueOf(this.id)+".txt";
		ArrayList<ArrayList<String>> fc = AhrIO.scanFile(akPath, ",");
		FCI fciBA = new FCI(false, "./../baseis/aggregated/");
		String mrDate = fc.get(fc.size()-1).get(fciBA.getIdx("date"));
		String edate = AhrDate.getTodaysDate();
		ArrayList<String> dates = AhrDate.getDatesBetween(mrDate, edate);
		if(dates.size() <= 1){
			System.out.println("--> "+bgmUC+" AK"+this.id+" ... already UP-TO-DATE.");
			return;
		}
		//find dates that have tbd vals from last update
		for(int i = fc.size()-1; i >= 0; i--){
			if(fc.get(i).contains("tbd")){
				if(!dates.contains(fc.get(i).get(fciBA.getIdx("date")))){
					dates.add(fc.get(i).get(fciBA.getIdx("date")));
				}
			}else{
				double a10 = Double.parseDouble(fc.get(i).get(fciBA.getIdx("appr10")));
				if(a10 == 0.0){
					if(!dates.contains(fc.get(i).get(fciBA.getIdx("date")))){
						dates.add(fc.get(i).get(fciBA.getIdx("date")));
					}
				}else{
					break;
				}
			}
		}
		Collections.sort(dates);
		//transfer old lines from basis file
		for(int i = 0; i < fc.size(); i++){
			String itrDate = fc.get(i).get(fciBA.getIdx("date"));
			if(!dates.contains(itrDate)){
				basis.add(fc.get(i));
			}
		}	
		//update, add new lines

		FCI fciBS = new FCI(false, "./../baseis/single/");
		for(int i = 0; i < dates.size(); i++){
			int skNum = getSK(dates.get(i));
			String skPath = "./../baseis/single/"+bgmLC+"/"+bgmUC+"_"+String.valueOf(skNum);
			skPath += ".txt";
			ArrayList<ArrayList<String>> skRows = AhrIO.scanSelectRows(skPath, ",", dates.get(i), fciBS.getIdx("date"));
			for(int j = 0; j < skRows.size(); j++){
				basis.add(skRows.get(j));
			}
		}
		//write to file
		AhrIO.writeToFile(akPath, basis, ",");
		System.out.println("--> "+this.bgm+" AK"+this.id+" ... UPDATED");
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
		if(this.bgm.equals("GAD2")){
			comps = decodeBGS_GAD2();
		}else if(this.bgm.equals("GAB2")){
			comps = decodeBGS_GAB2();
		}else if(this.bgm.equals("GAB3")){
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
	
	//return single Clean DB line from ticker, date, and some bools
	public ArrayList<String> calcCleanLine(String ticker, String date, boolean is_bs, boolean allow_tbd){
		FCI fciSN = new FCI(false, "./../../DB_Intrinio/Main/S_Norm/");
		FCI fciSR = new FCI(false, "./../../DB_Intrinio/Main/S_Raw/");
		ArrayList<String> nline = AhrIO.scanRow("./../../DB_Intrinio/Main/S_Norm/"+ticker+".txt", "~", date);
		String narMask = AhrIO.scanCell("./../../DB_Intrinio/Main/S_Raw/"+ticker+".txt", "~", date, fciSR.getIdx("nar_mask"));
		//System.out.println("Ticker = "+ticker+"  |  Date = "+date);
		//System.out.println("==> Scanned Row: " + nline);
		//System.out.println("==> Scanned Cell: " + narMask);
		if(nline.size() < 1){
			return nline;
		}

		int appr0Idx = fciSN.getIdx("appr0");
		int appr1Idx = fciSN.getIdx("appr1");
		int appr2Idx = fciSN.getIdx("appr2");
		int appr3Idx = fciSN.getIdx("appr3");
		int appr5Idx = fciSN.getIdx("appr5");
		int appr10Idx = fciSN.getIdx("appr10");

		if(!nline.get(appr0Idx).equals("tbd")){
			nline.set(appr0Idx, String.format("%.5f", Double.parseDouble(nline.get(appr0Idx))));
		}else{
			if(!allow_tbd){
				nline.set(appr0Idx, "0.000");
			}
		}
		if(!nline.get(appr1Idx).equals("tbd")){
			nline.set(appr1Idx, String.format("%.5f", Double.parseDouble(nline.get(appr1Idx))));
		}else{
			if(!allow_tbd){
				nline.set(appr1Idx, "0.000");
			}
		}
		if(!nline.get(appr2Idx).equals("tbd")){
			nline.set(appr2Idx, String.format("%.5f", Double.parseDouble(nline.get(appr2Idx))));
		}else{
			if(!allow_tbd){
				nline.set(appr2Idx, "0.000");
			}
		}
		if(!nline.get(appr3Idx).equals("tbd")){
			nline.set(appr3Idx, String.format("%.5f", Double.parseDouble(nline.get(appr3Idx))));
		}else{
			if(!allow_tbd){
				nline.set(appr3Idx, "0.000");
			}
		}
		if(!nline.get(appr5Idx).equals("tbd")){
			nline.set(appr5Idx, String.format("%.5f", Double.parseDouble(nline.get(appr5Idx))));
		}else{
			if(!allow_tbd){
				nline.set(appr5Idx, "0.000");
			}
		}
		if(!nline.get(appr10Idx).equals("tbd")){
			nline.set(appr10Idx, String.format("%.5f", Double.parseDouble(nline.get(appr10Idx))));
		}else{
			if(!allow_tbd){
				nline.set(appr10Idx, "0.000");
			}
		}
		nline.add(appr0Idx, narMask);
		return nline;
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
