package ahrweiler.support;
import ahrweiler.util.*;
import ahrweiler.support.FCI;
import java.io.*;
import java.util.*;
import java.lang.Math;

public class OrderSim {	//simulates ordering in reality 

	int keyNum;
	String bfPath = "";		//basis file path
	FCI fciBF;				//FCI that changes according to given basis path
	String sdate = "";
	String edate = "";
	double bim = 0.0;		//buy-in mult (of last close)
	double som = 0.0;		//sell-off mult (of buy-in)
	String ttvMask = "000";	//train test verify bit mask
	ArrayList<ArrayList<ArrayList<String>>> techBuf;
	ArrayList<String> uniqDates;
	int techLen = 0;
	//ML algo related vars
	boolean is_long;
	String dbUsed = "";
	int tvi = -1;
	double cmult = 10.0;
	double maxOrderSize = 25000;	//limit (in $) of any 1 order
	//calculations and results
	ArrayList<ArrayList<String>> orders;
	double posPer = 0.0;		//% of triggered orders that are positive
	double bimPer = 0.0;		//BIM percent triggered
	double somPer = 0.0;		//SOM percent triggered
	double tpr = 0.0;			//throughput rate (# of compounds/year) 
	double trigAppr = 0.0;		//% appr of triggered lines
	double secAppr = 0.0;		//tot % appr of whole date range given
	double yoyAppr = 0.0;		//year-over-year % appr
	boolean ismt = false;		//min-max true appr (false = min true appr)

	//constructors
	public OrderSim(String basisPath){//empty init
		System.out.println("==> In OrderSim() (CUST)");
		this.bfPath = basisPath;
		this.fciBF = new FCI(true, basisPath);
		this.techBuf = new ArrayList<ArrayList<ArrayList<String>>>();
		this.orders = new ArrayList<ArrayList<String>>();
		this.uniqDates = new ArrayList<String>();
		this.is_long = true;
		this.cmult = 1.0;
		this.tvi = 1;	
	}
	public OrderSim(int keyID){//for agg key (dont need bgm)
		System.out.println("==> In OrderSim() (AK)");
		this.keyNum = keyID;
		this.techBuf = new ArrayList<ArrayList<ArrayList<String>>>();
		this.orders = new ArrayList<ArrayList<String>>();
		this.uniqDates = new ArrayList<String>();
		ArrayList<ArrayList<String>> aggLog = AhrIO.scanFile("./../baseis/log/ak_log.txt", ",");
		FCI fciAL = new FCI(true, "./../baseis/log/ak_log.txt");
		int aggIdx = AhrDTF.transpose(aggLog).get(fciAL.getIdx("basis_num")).indexOf(String.valueOf(keyID));
		String bgm = aggLog.get(aggIdx).get(fciAL.getIdx("bgm"));
		this.dbUsed = aggLog.get(aggIdx).get(fciAL.getIdx("db_used"));
		if(this.dbUsed .equals("YH")){
			this.dbUsed = "Yahoo";
		}else{
			this.dbUsed = "Intrinio";
		}
		this.tvi = Integer.parseInt(aggLog.get(aggIdx).get(fciAL.getIdx("tvi")));
		if(Integer.parseInt(aggLog.get(aggIdx).get(fciAL.getIdx("call"))) == 0){
			this.is_long = false;
			this.cmult = -1.0;
		}else{
			this.is_long = true;
			this.cmult = 1.0;
		}
		this.bfPath = "./../baseis/aggregated/"+bgm.toLowerCase()+"/"+bgm+"_"+String.valueOf(keyID)+".txt";
		this.fciBF = new FCI(false, this.bfPath);
	}
	//TODO: double check entire function
	public OrderSim(String bgm, int keyID){//for single key
		System.out.println("==> In OrderSim() (SK)");
		String bgmLC = bgm.toLowerCase();
		this.keyNum = keyID;
		this.techBuf = new ArrayList<ArrayList<ArrayList<String>>>();
		this.orders = new ArrayList<ArrayList<String>>();
		this.uniqDates = new ArrayList<String>();//TODO: check if init for techBuf and uniqDates is correct
		ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile("./../out/ml/"+bgmLC+"/keys_struct.txt", ",");
		FCI fciKS = new FCI(true, "./../out/ml/"+bgmLC+"/keys_struct.txt");
		int ksIdx = AhrDTF.transpose(ksFile).get(fciKS.getIdx("key_num")).indexOf(String.valueOf(keyID));
		System.out.println("**********\n--> ksFile : " + ksFile.get(ksIdx));
		this.dbUsed = ksFile.get(ksIdx).get(fciKS.getIdx("db_used"));
		if(this.dbUsed.equals("YH")){
			this.dbUsed = "Yahoo";
		}else{
			this.dbUsed = "Intrinio";
		}
		this.tvi = Integer.parseInt(ksFile.get(ksIdx).get(fciKS.getIdx("tvi")));
		//set call and cmult
		if(fciKS.getIdx("call") != -1){
			if(Integer.parseInt(ksFile.get(ksIdx).get(fciKS.getIdx("call"))) == 0){
				this.is_long = false;
				this.cmult = -1.0;
			}else{
				this.is_long = true;
				this.cmult = 1.0;
			}
		}
		if(this.cmult == 10.0){
			Scanner scanner = new Scanner(System.in);
			System.out.print("Call is null, is this a long call? (y/n) : ");
			String callStr = scanner.nextLine();
			if(callStr.equals("y") || callStr.equals("Y")){
				this.is_long = true;
				this.cmult = 1.0;
			}else{
				this.is_long = false;
				this.cmult = -1.0;
			}
		}
		//set path and FCI for basis file
		this.bfPath = "./../baseis/single/"+bgmLC+"/"+bgm+"_"+String.valueOf(keyID)+".txt";
		this.fciBF = new FCI(false, this.bfPath);
	}
	//getters & setters
	public String getPath(){
		return this.bfPath;
	}
	public String getSDate(){
		return this.sdate;
	}
	public String getEDate(){
		return this.edate;
	}
	public double getBIM(){
		return this.bim;
	}
	public double getSOM(){
		return this.som;
	}
	public double getTPR(){
		return this.tpr;
	}
	public String getTtvMask(){
		return this.ttvMask;
	}
	public boolean getIsLong(){
		return this.is_long;
	}
	public String getDB(){
		return this.dbUsed;
	}
	public double getTVI(){
		return this.tvi;
	}
	public double getCMULT(){
		return this.cmult;
	}
	public double getMaxOrderSize(){
		return this.maxOrderSize;
	}
	public ArrayList<String> getUniqDates(){
		return this.uniqDates;
	}
	public double getPosPer(){
		return this.posPer;
	}
	public double getBimPer(){
		return this.bimPer;
	}
	public double getSomPer(){
		return this.somPer;
	}
	public double getTrigAppr(){
		return this.trigAppr;
	}
	public double getSecAppr(){
		return this.secAppr;
	}
	public double getYoyAppr(){
		return this.yoyAppr;
	}
	public boolean getISMT(){
		return this.ismt;
	}
	public ArrayList<ArrayList<String>> getOrderList(){
		return this.orders;
	}
	public int getOrderListSize(){
		return this.orders.size();
	}
	public int getOrderListSPD(){
		int spd = 0;
		if(this.orders.size() == 1){
			spd = 1;
		}else if(this.orders.size() > 1){
			String date1 = this.orders.get(0).get(0);
			String date2 = this.orders.get(1).get(0);
			spd = 1;
			while(date2.equals(date1) || spd > (this.orders.size()-2)){
				date2 = this.orders.get(spd+1).get(0);
				spd++;
			}
		}
		return spd;
	}
	//------------- SETTERS --------------
	public void setPath(String path){
		this.bfPath = path;
	}
	public void setDateRange(String date1, String date2){
		this.sdate = date1;
		this.edate = date2;
	}
	public void setBIM(double bimVal){
		this.bim = bimVal;
	}
	public void setSOM(double somVal){
		this.som = somVal;
	}
	public void setTPR(double tprVal){
		this.tpr = tprVal;
	}
	public void setTtvMask(String ttvVal){
		this.ttvMask = ttvVal;
	}
	public void setIsLong(boolean isLongVal){
		this.is_long = isLongVal;
		if(this.is_long){
			this.cmult = 1.0;
		}else{
			this.cmult = -1.0;
		}
	}
	public void setDB(String dbVal){
		this.dbUsed = dbVal;
	}
	public void setTVI(int tviVal){
		this.tvi = tviVal;
	}
	public void setISMT(boolean ismtVal){
		this.ismt = ismtVal;
	}
	public void setMaxOrderSize(double mosVal){
		this.maxOrderSize = mosVal;
	}
	public void setOrderList(ArrayList<ArrayList<String>> orderAL){
		this.orders = orderAL;
	}
		
	//functional methods

	//techBuffer is 3D string AL that holds the basic tech data needed to calc an order around a date
	//a 1-day order only needs 2 lines of tech data while a 3-day needs more
	public void calcBuffer(){
		ArrayList<ArrayList<String>> slist = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> basis = AhrIO.scanFile(this.bfPath, ",");
		//add lines of dates in range and according to ttvMask
		int startRowIdx = 0;
		if(fciBF.getHasHeader()){
			startRowIdx = 1;
		}
		for(int i = startRowIdx; i < basis.size(); i++){
			String itrDate = basis.get(i).get(fciBF.getIdx("date"));
			if(AhrDate.isDateInPeriod(itrDate, sdate, edate)){
				String ticker = basis.get(i).get(fciBF.getIdx("ticker"));
				ArrayList<String> line = new ArrayList<String>();
				if(checkTTV(basis.get(i).get(fciBF.getIdx("ttv_code")))){
					line.add(itrDate);
					line.add(ticker);
					slist.add(line);
				}
			}
		}
		//itr thru each passed line and calc order line
		for(int i = 0; i < slist.size(); i++){
			String date = slist.get(i).get(0);
			String tick = slist.get(i).get(1);
			//String dbPath = "./../../DB_"+this.dbUsed+"/Main/"+this.dbUsed+"/"+tick+".txt";
			String dbPath = "./../../DB_Intrinio/Main/Intrinio/"+tick+".txt";
			ArrayList<String> line = new ArrayList<String>();
			line.add(date);								// [0] date
			line.add(tick);								// [1] ticker
			line.add("ph");								// [2] % appr over order (from close before pred)
			line.add("ph");								// [3] % appr over order (from open at start of pred)
			line.add("ph");								// [4] Time when bought (OPEN, DAY, or NO)
			line.add("ph");								// [5] $ val bought in at
			line.add("ph");								// [6] $ val sold at
			line.add("ph");								// [7] % appr from method
			line.add("ph");								// [8] # of days cash needs to be reserved
			this.orders.add(line);
			if(!this.uniqDates.contains(date)){
				this.uniqDates.add(date);
			}
			//read files til you get to date
			this.techLen = 2;	//# of lines needed in the buffer
			if(this.tvi == 2 || this.tvi == 3){
				this.techLen = this.tvi + 1;
			}else if(this.tvi == 4){
				this.techLen = 5 + 1;	
			}else if(this.tvi == 5){
				this.techLen = 10 + 1;
			}
			ArrayList<ArrayList<String>> stockBuf = new ArrayList<ArrayList<String>>();
			String[] plArr = {""};	//prev line array (more recent)
			String[] clArr = {""};	//curr line array (less recent)
			try{
				BufferedReader br = new BufferedReader(new FileReader(dbPath));
				boolean date_reached = false;
				while(br.ready() && !date_reached){
					String fline = br.readLine();
					plArr = clArr;
					clArr = fline.split("~");
					if(stockBuf.size() == 0){
						//System.out.println("PL Array Length: " + plArr.length);
						stockBuf = AhrAL.addArrayToAL(stockBuf, clArr);
						stockBuf = AhrAL.addArrayToAL(stockBuf, clArr);
					}else{
						//AhrAL.print(stockBuf);
						if(stockBuf.size() < this.techLen){
							stockBuf = AhrAL.addArrayToAL(stockBuf, clArr);
						}else{
							stockBuf = AhrAL.addArrayToAL(stockBuf, clArr);
							stockBuf.remove(0);
						}
					}
					if(clArr[0].equals(date)){
						date_reached = true;
					}
				}
				br.close();
				//if date as not reached, zero the values in buffer
				if(!date_reached){
					for(int x = 0; x < stockBuf.size(); x++){
						for(int y = 1; y <= 10; y++){
							stockBuf.get(x).set(0, date);
							stockBuf.get(x).set(y, "0.0");
						}
					}		
				}
			}catch(FileNotFoundException e){
				//System.out.println("FileNotFoundException: " + e+"\n-> Ticker = "+tick+"  |  Date = "+date);
				for(int x = 0; x < this.techLen; x++){
					ArrayList<String> sbLine = new ArrayList<String>();
					sbLine.add(date);
					for(int y = 0; y < 10; y++){
						sbLine.add("0.0");
					}
					stockBuf.add(sbLine);
				}
			}catch(IOException e){
				System.out.println("IOException: " + e.getMessage());
			}
			//System.out.println("TICKER: " + tick);
			//printAL_S(stockBuf);
			this.techBuf.add(stockBuf);
		}
		//log tech to file for debugging
		ArrayList<ArrayList<String>> tfTechBuf = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < this.techBuf.size(); i++){
			for(int j = 0; j < this.techBuf.get(i).size(); j++){
				tfTechBuf.add(this.techBuf.get(i).get(j));
			}
		}
		AhrIO.writeToFile("./../data/orderlist/techbuf.txt", tfTechBuf, ",");
	}

	public void calcOrderList(){
		if(this.techBuf.size() < 1){
			calcBuffer();
		}else{
			//System.out.println("TechBuf size = " + techBuf.size());
		}
		FCI fciTB = new FCI(false, "./../../DB_Intrinio/Main/Intrinio/");
		this.trigAppr = 0.0;
		this.tpr = 0.0;
		this.posPer = 0.0;
		int bimCount = 0;
		int somCount = 0;
		//System.out.println("***** Calculating Order List *****");
		for(int i = 0; i < this.techBuf.size(); i++){
			if(i%500==0){
				//System.out.println("   "+i+" out of "+this.techBuf.size());
			}
			ArrayList<Double> opens = new ArrayList<Double>();
			ArrayList<Double> highs = new ArrayList<Double>();
			ArrayList<Double> lows = new ArrayList<Double>();
			for(int x = 0; x < this.techBuf.get(i).size(); x++){
				//System.out.println("Idx "+x+" = "+this.techBuf.get(i).get(x).get(0));
				opens.add(Double.parseDouble(this.techBuf.get(i).get(x).get(fciTB.getIdx("adj_open"))));
				highs.add(Double.parseDouble(this.techBuf.get(i).get(x).get(fciTB.getIdx("adj_high"))));
				lows.add(Double.parseDouble(this.techBuf.get(i).get(x).get(fciTB.getIdx("adj_low"))));
			}
			//printAL_S(this.techBuf.get(i));
			//System.out.println("");
			double open = opens.get(opens.size()-2);
			double firstHi = highs.get(highs.size()-2);
			double firstLo = lows.get(lows.size()-2);
			int llIdx = this.techBuf.get(i).size()-1;	//last line idx
			double lastClose = Double.parseDouble(this.techBuf.get(i).get(llIdx).get(fciTB.getIdx("adj_close")));
			double endClose = Double.parseDouble(this.techBuf.get(i).get(0).get(fciTB.getIdx("adj_close")));
			double buyPrice = lastClose * this.bim;	//price bought at (if buy triggered)
			double sellPrice = buyPrice * this.som;	//price sold at (if buy triggered)
			boolean bim_triggered = true;
			boolean som_triggered = false;
			boolean allow_first_day_selling = true;
			int daysHeld = this.techLen -1;	//# of days order was held for
			if(lastClose != 0.0){
				this.orders.get(i).set(2, String.format("%.3f", ((endClose-lastClose)/lastClose)*100.0));
			}else{
				this.orders.get(i).set(2, "0.000");
			}
			if(open != 0.0){
				this.orders.get(i).set(3, String.format("%.3f", ((endClose-open)/open)*100.0));
			}else{
				this.orders.get(i).set(3, "0.000");
			}
			if(this.cmult > 0){	//--------------------------- LONG CALL -----------------------------------
				if(open <= buyPrice){								//OPTION 1 : triggered at open
					this.orders.get(i).set(4, "OPEN");
					buyPrice = open;
					sellPrice = buyPrice * this.som;
					bimCount++;
					if(allow_first_day_selling){
						if(sellPrice > buyPrice){//lock in gains
							if(firstHi > sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;
							}
						}
						if(sellPrice < buyPrice){//stop-loss
							if(firstLo < sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;									
							}
						}
					}
				}else if(open > buyPrice && firstLo < buyPrice){	//OPTION 2 : triggered during day
					this.orders.get(i).set(4, "DAY");
					bimCount++;
					if(allow_first_day_selling){
						if(sellPrice > buyPrice){//lock in gains
							// TODO: put max trig appr info here
							if(firstHi > sellPrice && this.ismt){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;
							}
						}
						if(sellPrice < buyPrice){//stop-loss
							if(firstLo < sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;									
							}
						}
					}
				}else{												//OPTION 3 : not triggered
					bim_triggered = false;
				}
				//itr thru all days if postion is held over mult days
				if(bim_triggered && !som_triggered && sellPrice >= buyPrice){		//lock in gains
					if(this.techBuf.get(i).size() > 2){
						int x = this.techBuf.get(i).size() - 3;
						while(x >= 0 && !som_triggered){
							if(highs.get(x) >= sellPrice){
								som_triggered = true;
								daysHeld -= x;
								somCount++;
								if(sellPrice < opens.get(x)){
									sellPrice = opens.get(x);
								}
							}
							x--;
						}
					}
					this.tpr += daysHeld;
				}
				if(bim_triggered && !som_triggered && sellPrice < buyPrice){		//stop-loss
					if(this.techBuf.get(i).size() > 2){
						int x = this.techBuf.get(i).size() - 3;
						while(x >= 0 && !som_triggered){
							if(lows.get(x) <= sellPrice){
								som_triggered = true;
								daysHeld -= x;
								somCount++;
								if(sellPrice > opens.get(x)){
									sellPrice = opens.get(x);
								}
							}
							x--;
						}
					}
					this.tpr += daysHeld;
				}
				if(!som_triggered){
					sellPrice = endClose;
				}
				if(bim_triggered){
					double appr = 0.0;
					if(buyPrice != 0.0){
						appr = ((sellPrice - buyPrice) / buyPrice) * 100.0;
					}
					this.trigAppr += appr;
					this.orders.get(i).set(5, String.format("%.2f", buyPrice));
					this.orders.get(i).set(6, String.format("%.2f", sellPrice));
					this.orders.get(i).set(7, String.format("%.3f", appr));
				}else{
					this.orders.get(i).set(4, "NO");
					this.orders.get(i).set(5, "0.00");
					this.orders.get(i).set(6, "0.00");
					this.orders.get(i).set(7, "0.000");
				}					
			}else{			//--------------------------- SHORT CALL ----------------------------------
				if(open >= buyPrice){								//OPTION 1 : triggered at open
					this.orders.get(i).set(4, "OPEN");
					buyPrice = open;
					sellPrice = buyPrice * this.som;
					bimCount++;
					if(allow_first_day_selling){
						if(sellPrice < buyPrice){//lock in gains
							if(firstLo < sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;
							}
						}
						if(sellPrice > buyPrice){//stop-loss
							if(firstHi > sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;									
							}
						}
					}
				}else if(open < buyPrice && firstHi > buyPrice){	//OPTION 2 : triggered during day
					this.orders.get(i).set(4, "DAY");
					bimCount++;
					if(allow_first_day_selling){
						if(sellPrice < buyPrice){//lock in gains
							//TODO:  for max trig appr only
							if(firstLo < sellPrice && this.ismt){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;
							}
						}
						if(sellPrice > buyPrice){//stop-loss
							if(firstHi > sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;									
							}
						}
					}
				}else{												//OPTION 3 : not triggered
					bim_triggered = false;
				}
				if(bim_triggered && !som_triggered && sellPrice <= buyPrice){		//lock in gains
					if(this.techBuf.get(i).size() > 2){
						int x = this.techBuf.get(i).size() - 3;
						while(x >= 0 && !som_triggered){
							if(lows.get(x) <= sellPrice){
								som_triggered = true;
								daysHeld -= x;
								somCount++;
								if(sellPrice > opens.get(x)){
									sellPrice = opens.get(x);
								}
							}
							x--;
						}
					}
					this.tpr += daysHeld;
				}
				if(bim_triggered && !som_triggered && sellPrice > buyPrice){		//stop-loss
					if(this.techBuf.get(i).size() > 2){
						int x = this.techBuf.get(i).size() - 3;
						while(x >= 0 && !som_triggered){
							if(highs.get(x) >= sellPrice){
								som_triggered = true;
								daysHeld -= x;
								somCount++;
								if(sellPrice < opens.get(x)){
									sellPrice = opens.get(x);
								}
							}
							x--;
						}
					}
					this.tpr += daysHeld;
				}
				if(!som_triggered){
					sellPrice = endClose;
				}
				if(bim_triggered){
					double appr = 0.0;
					if(buyPrice != 0.0){
						appr = ((sellPrice - buyPrice) / buyPrice) * 100.0;
					}
					this.trigAppr += appr;
					this.orders.get(i).set(5, String.format("%.2f", buyPrice));
					this.orders.get(i).set(6, String.format("%.2f", sellPrice));
					this.orders.get(i).set(7, String.format("%.3f", appr));
				}else{
					this.orders.get(i).set(4, "NO");
					this.orders.get(i).set(5, "0.00");
					this.orders.get(i).set(6, "0.00");
					this.orders.get(i).set(7, "0.000");
				}
			}
			this.orders.get(i).set(8, String.valueOf(daysHeld));
			if(Double.parseDouble(this.orders.get(i).get(7)) > 0.0){
				this.posPer++;
			}
			//print out order line if seems fishy
			//double olAppr = Double.parseDouble(this.orders.get(i).get(7));
			//if(olAppr > 100.0){
			//	System.out.println("BIG VAL: "+this.orders.get(i));
			//}
		}
		this.posPer = this.posPer / (double)bimCount;
		this.tpr = this.tpr / (double)bimCount;
		this.trigAppr = this.trigAppr / (double)bimCount;

		//calc compound interest values
		this.bimPer = ((double)bimCount / (double)this.techBuf.size()) * 100.0;
		this.somPer = ((double)somCount / (double)this.techBuf.size()) * 100.0;
		int compounds = AhrDate.getDatesBetween(sdate, edate).size();
		double secStart = 30000;
		this.secAppr = secStart;
		for(int i = 0; i < (int)Math.floor(compounds/this.tpr); i++){
			this.secAppr += (this.secAppr * (this.bimPer/100.0)) * ((this.cmult*this.trigAppr)/100.0);
		}
		this.secAppr = ((this.secAppr - secStart) / secStart) * 100.0;
		double yoyStart = 30000;
		this.yoyAppr = yoyStart;
		for(int i = 0; i < (int)Math.floor(250.0/tpr); i++){
			this.yoyAppr += (this.yoyAppr * (this.bimPer/100.0)) * ((this.cmult*this.trigAppr)/100.0);
		}
		this.yoyAppr = ((this.yoyAppr - yoyStart) / yoyStart) * 100.0;

		//log to file for debugging
		AhrIO.writeToFile("./../data/orderlist/orderlist.txt", this.orders, ",");
	}

	//calc growth on a porfolio using the calcualted order list
	public ArrayList<ArrayList<String>> calcGrowth2(double principle){
		int orderLim = 10000;	//no single order can worth more than this in $
		//TODO make sure you calc order list first??
		ArrayList<ArrayList<String>> growth = new ArrayList<ArrayList<String>>();
		double value = principle;
		String date = orders.get(0).get(0);
		double dayAppr = Double.parseDouble(orders.get(0).get(7)) * cmult;
		int daySize = 1;
		for(int i = 1; i < orders.size(); i++){
			if(this.orders.get(i).get(0).equals(date)){
				dayAppr += (Double.parseDouble(orders.get(i).get(7)) * cmult);
				daySize++;
			}else{
				dayAppr = dayAppr / (double)daySize;
				value += value * (dayAppr / 100.0);
				ArrayList<String> line = new ArrayList<String>();
				line.add(date);
				line.add(String.format("%.4f", value));
				growth.add(line);
				date = orders.get(i).get(0);
				dayAppr = (Double.parseDouble(orders.get(i).get(7)) * cmult);
				daySize = 1;
			}
		}
		return growth;
	}

	//calc growth on a porfolio using the calculated order list
	public ArrayList<ArrayList<String>> calcGrowth(double principle){
		ArrayList<ArrayList<String>> growth = new ArrayList<ArrayList<String>>();
		double value = principle;
		String date = orders.get(0).get(0);
		ArrayList<Double> apprs = new ArrayList<Double>();
		apprs.add(Double.parseDouble(orders.get(0).get(7)) * cmult);
		for(int i = 1; i < orders.size(); i++){
			if(orders.get(i).get(0).equals(date)){	//on same date
				apprs.add(Double.parseDouble(orders.get(i).get(7)) * cmult);
			}else{									//date changes
				for(int j = 0; j < apprs.size(); j++){
					double orderSize = (value / apprs.size());
					if(orderSize > maxOrderSize){
						orderSize = maxOrderSize;
					}
					value += orderSize * (apprs.get(j) / 100.0);
				}
				ArrayList<String> line = new ArrayList<String>();
				line.add(date);
				line.add(String.format("%.4f", value));
				growth.add(line);
				date = orders.get(i).get(0);
				apprs = new ArrayList<Double>();
				apprs.add(Double.parseDouble(orders.get(i).get(7)) * cmult);
			}
		}
		return growth;
	}

	//given a TTV code from a basis line, returns bool whether code pass OrderSim ttvMask
	//code: 0 = is train line in basis, 1 = is test line, 2 = is verify line
	public boolean checkTTV(String code){
		char bitInMask = ttvMask.charAt(Integer.parseInt(code));
		boolean pass_ttv = false;
		if(bitInMask == '1'){
			pass_ttv = true;
		}
		return pass_ttv;
	}

}


