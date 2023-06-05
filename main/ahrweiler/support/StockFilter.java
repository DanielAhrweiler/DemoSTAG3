package ahrweiler.support;
import ahrweiler.Globals;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrGen;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class StockFilter {

	class NameAndRange{
		private String name;
		private int start;
		private int end;
		public NameAndRange(String sname){
			this.name = sname;
			this.start = 0;
			this.end = Integer.MAX_VALUE;	
		}
		public NameAndRange(ArrayList<String> line){
			if(line.size() > 0){
				this.name = line.get(0);
			}else{
				this.name = "";
			}
			if(line.size() > 2){
				try{
					this.start = Integer.parseInt(line.get(1));
					this.end = Integer.parseInt(line.get(2));
				}catch(NullPointerException e){
					System.out.println("ERR: "+e);
				}catch(NumberFormatException e){
					System.out.println("ERR: "+e);
				}
			}else{
				this.start = Integer.MIN_VALUE;
				this.end = Integer.MAX_VALUE;
			}
		}
		public String getName(){
			return this.name;
		}
		public int getStartVal(){
			return this.start;
		}
		public int getEndVal(){
			return this.end;
		}
		public void setName(String name){
			this.name = name;
		}
		public void setStartVal(int sval){
			this.start = sval;
		}
		public void setEndVal(int eval){
			this.end = eval;
		}
	}


	ArrayList<String> tickers;
	HashMap<String, String> trackMC;
	HashMap<String, String> trackCodes;
	HashMap<String, ArrayList<Integer>> trackInds;

	ArrayList<ArrayList<String>> scodes;
	ArrayList<ArrayList<String>> results;
	ArrayList<String> sectors;
	ArrayList<String> industries;
	NameAndRange mcRange;
	ArrayList<NameAndRange> indRanges;
	

	public StockFilter(){
		this.scodes = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
		this.sectors = new ArrayList<String>();
		this.industries = new ArrayList<String>();
		this.mcRange = new NameAndRange("mc");
		this.indRanges = new ArrayList<NameAndRange>();	
	}
	public StockFilter(ArrayList<ArrayList<String>> filters){
		this.scodes = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
		this.sectors = new ArrayList<String>();
		this.industries = new ArrayList<String>();
		this.indRanges = new ArrayList<NameAndRange>();
		for(int i = 0; i < filters.size(); i++){
			if(filters.get(i).get(0).contains("ind") && !filters.get(i).get(0).equals("industry")){
				this.indRanges.add(new NameAndRange(filters.get(i)));
			}else if(filters.get(i).get(0).equals("mc")){
				this.mcRange = new NameAndRange(filters.get(i));
			}else if(filters.get(i).get(0).equals("sector")){
				if(!filters.get(i).get(1).equals("")){
					String[] allSecs = filters.get(i).get(1).split(",");
					for(int j = 0; j < allSecs.length; j++){
						this.sectors.add(allSecs[j]);
					}
				}
			}else if(filters.get(i).get(0).equals("industry")){
				if(!filters.get(i).get(1).equals("")){
					String[] allInds = filters.get(i).get(1).split(",");
					for(int j = 0; j < allInds.length; j++){
						this.industries.add(allInds[j]);
					}
				}
			}
		}
	}
	public StockFilter(String fpath){
		fpath = AhrIO.uniPath(fpath);
		this.scodes = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
		this.sectors = new ArrayList<String>();
		this.industries = new ArrayList<String>();
		this.indRanges = new ArrayList<NameAndRange>();
		//sort out the filter lines
		ArrayList<ArrayList<String>> filters = AhrIO.scanFile(fpath, "~");
		//System.out.println("--> In StockFilter(fpath) constructor, filters are ...");
		//AhrAL.print(filters);
		for(int i = 0; i < filters.size(); i++){
			if(filters.get(i).get(0).contains("ind") && !filters.get(i).get(0).equals("industry")){
				this.indRanges.add(new NameAndRange(filters.get(i)));
			}else if(filters.get(i).get(0).equals("mc")){
				this.mcRange = new NameAndRange(filters.get(i));
			}else if(filters.get(i).get(0).equals("sector")){
				if(!filters.get(i).get(1).equals("")){
					String[] allSecs = filters.get(i).get(1).split(",");
					for(int j = 0; j < allSecs.length; j++){
						this.sectors.add(allSecs[j]);
					}
				}
			}else if(filters.get(i).get(0).equals("industry")){
				if(filters.get(i).size() > 1){
					String[] allInds = filters.get(i).get(1).split(",");
					for(int j = 0; j < allInds.length; j++){
						this.industries.add(allInds[j]);
					}
				}
			}
		}
	}

	//GETERS & SETTERS
	public ArrayList<String> getTickers(){
		ArrayList<String> tickers = new ArrayList<String>();
		for(int i = 0; i < this.scodes.size(); i++){
			tickers.add(this.scodes.get(i).get(0));
		}
		return tickers;
	}
	public ArrayList<ArrayList<String>> getResults(){
		return this.results;
	}
	public ArrayList<String> getIndicators(){
		ArrayList<String> inds = new ArrayList<String>();
		for(int i = 0; i < this.indRanges.size(); i++){
			inds.add(this.indRanges.get(i).getName());
		}
		return inds;
	}
	public void setMarketCap(int startVal, int endVal){
		this.mcRange.setStartVal(startVal);
		this.mcRange.setEndVal(endVal);
	}
	public void setSectors(String secRaw){
		this.sectors = new ArrayList<String>();
		String[] allSec = secRaw.split(",");
		for(int i = 0; i < allSec.length; i++){
			String itrSec = allSec[i].replaceAll("\\s+", "");
			if(AhrGen.isInt(itrSec)){
				this.sectors.add(itrSec);
			}
		}
	}
	public void setIndustries(String indRaw){
		this.industries = new ArrayList<String>();
		String[] allInd = indRaw.split(",");
		for(int i = 0; i < allInd.length; i++){
			String itrInd = allInd[i].replaceAll("\\s+", "");
			if(AhrGen.isInt(itrInd)){
				this.industries.add(itrInd);
			}
		}
	}

	//OTHER FUNCTIONS

	//add an indicator name, start val, and end val
	public void addIndicatorFilter(ArrayList<String> filterLine){
		this.indRanges.add(new NameAndRange(filterLine));
	}
	//edit already existing indicator range
	public void editIndicatorFilter(ArrayList<String> filterLine){
		int idx = -1;
		String newName = filterLine.get(0);
		for(int i = 0; i < this.indRanges.size(); i++){
			NameAndRange itrNAR = this.indRanges.get(i);
			String oldName = itrNAR.getName();
			if(oldName.equals(newName)){
				idx = i;
				break;
			}
		}
		if(idx != -1){
			this.indRanges.set(idx, new NameAndRange(filterLine));
		}
	}

	//assign sector/industry codes to all tickers from given sector range
	public void applySecIndFilterOld(){
		//cols [0]    ticker
		//	   [1]    sec/ind code
		//System.out.println("--> In applySecIndFilter()");

		//get all (non xx) codes from sector_ticks.txt
		String stPath = AhrIO.uniPath("./../in/sector_ticks.txt");
		ArrayList<ArrayList<String>> stFC = AhrIO.scanFile(stPath, ",");
		ArrayList<Integer> allCodes = new ArrayList<Integer>();
		for(int i = 0; i < stFC.size(); i++){
			String itrCode = stFC.get(i).get(0);
			if(!itrCode.contains("xx")){
				allCodes.add(Integer.parseInt(stFC.get(i).get(0)));
			}
		}
	
		//System.out.println("--> Sector List : " + this.sectors);
		//System.out.println("--> Industry List : " + this.industries);
		//determine all sectors/industries (no xx) to get tickers from in sector_ticks.txt
		ArrayList<Integer> stList = new ArrayList<Integer>();
		if(this.industries.size() > 0){
			int secInt = Integer.parseInt(this.sectors.get(0));
			for(int i = 0; i < this.industries.size(); i++){
				stList.add((secInt*100)+Integer.parseInt(this.industries.get(i)));
			}
		}else{
			for(int i = 0; i < this.sectors.size(); i++){
				int secInt = Integer.parseInt(this.sectors.get(i));
				for(int j = 0; j < allCodes.size(); j++){
					int divBy100 = allCodes.get(j) / 100;
					if(secInt == divBy100){
						stList.add(allCodes.get(j));
					}
				}
			}
		}
		//System.out.println("  > stList = " + stList);
		//itr thru sector_ticks.txt and add all necessary tickers
		ArrayList<String> scTicks = new ArrayList<String>();
		for(int i = 0; i < stFC.size(); i++){
			String itrSCode = stFC.get(i).get(0);
			int intSCode = 0;
			try{
				intSCode = Integer.parseInt(itrSCode);
			}catch(NumberFormatException e){
				//System.out.println("ERR: "+e);
			}
			if(stList.contains(intSCode)){
				for(int j = 1; j < stFC.get(i).size(); j++){
					String tick = stFC.get(i).get(j);
					if(!scTicks.contains(tick)){
						scTicks.add(tick);
						ArrayList<String> line = new ArrayList<String>();
						line.add(tick);
						line.add(itrSCode);
						this.scodes.add(line);
					}
				}
			}
		}
		//print scode data
		//System.out.println("--> Ticker / SCode after applySecIndFilter() ... ");
		//AhrAL.print(this.scodes);
	}

	//apply just sector/industry code filter
	public void applySecIndFilter(){
		//get all (non xx) codes from sector_ticks.txt
		String stPath = AhrIO.uniPath("./../in/sector_ticks.txt");
		ArrayList<ArrayList<String>> secTicks = AhrIO.scanFile(stPath, ",");
		ArrayList<Integer> allCodes = new ArrayList<Integer>();
		for(int i = 0; i < secTicks.size(); i++){
			String itrCode = secTicks.get(i).get(0);
			if(!itrCode.contains("xx")){
				allCodes.add(Integer.parseInt(secTicks.get(i).get(0)));
			}
		}
	
		//System.out.println("--> Sector List : " + this.sectors);
		//System.out.println("--> Industry List : " + this.industries);
		//determine all sectors/industries (no xx) to get tickers from in sector_ticks.txt
		ArrayList<Integer> stList = new ArrayList<Integer>();
		if(this.industries.size() > 0){
			int secInt = Integer.parseInt(this.sectors.get(0));
			for(int i = 0; i < this.industries.size(); i++){
				stList.add((secInt*100)+Integer.parseInt(this.industries.get(i)));
			}
		}else{
			for(int i = 0; i < this.sectors.size(); i++){
				int secInt = Integer.parseInt(this.sectors.get(i));
				for(int j = 0; j < allCodes.size(); j++){
					int divBy100 = allCodes.get(j) / 100;
					if(secInt == divBy100){
						stList.add(allCodes.get(j));
					}
				}
			}
		}
		//System.out.println("  > stList = " + stList);
		//itr thru sector_ticks.txt and add all necessary tickers
		ArrayList<String> passCodes = new ArrayList<String>();
		for(int i = 0; i < secTicks.size(); i++){
			String itrCode = secTicks.get(i).get(0);
			int intCode = 0;
			try{
				intCode = Integer.parseInt(itrCode);
			}catch(NumberFormatException e){
				//System.out.println("ERR: "+e);
			}
			if(stList.contains(intCode)){
				for(int j = 1; j < secTicks.get(i).size(); j++){
					String itrTick = secTicks.get(i).get(j);
					if(tickers.contains(itrTick)){
						passCodes.add(itrTick);
						trackCodes.put(itrTick, itrCode);
					}
				}
			}
		}
		tickers = passCodes;
	}

	//apply just filter for indictors
	public void applyIndicatorFilter(ArrayList<ArrayList<String>> bdFile){
		FCI fciBD = new FCI(false, Globals.bydate_path);
		ArrayList<String> passInds = new ArrayList<String>();
		if(indRanges.size() > 0){//has indicator filters at all
			for(int i = 0; i < bdFile.size(); i++){
				String itrTick = bdFile.get(i).get(fciBD.getIdx("ticker"));
				if(tickers.contains(itrTick)){
					boolean passes_all_inds = true;
					ArrayList<Integer> indVals = new ArrayList<Integer>();
					for(int j = 0; j < indRanges.size(); j++){
						String itrInd = indRanges.get(j).getName();
						int itrLoBound = indRanges.get(j).getStartVal();
						int itrHiBound = indRanges.get(j).getEndVal();
						int itrVal = Integer.parseInt(bdFile.get(i).get(fciBD.getIdx(itrInd)));
						if(itrVal < itrLoBound || itrVal > itrHiBound){
							passes_all_inds = false;
						}else{
							indVals.add(itrVal);
						}
					}
					if(passes_all_inds){
						passInds.add(itrTick);
						trackInds.put(itrTick, indVals);
					}
				}
			}
			tickers = passInds;
		}
	}

	//apply just market cap filter
	public void applyMarketCapFilter(){
		if(Globals.uses_mysql_source){
			ArrayList<String> colNames = AhrAL.toAL(new String[]{"ticker", "market_cap"});
			SQLCode sqlc = new SQLCode(Globals.default_source);
			sqlc.setDB("sbase");
			sqlc.addWhereCond("WHERE `date` = '2022-05-27' AND `market_cap` BETWEEN "+this.mcRange.getStartVal()+
								" AND "+this.mcRange.getEndVal());
			sqlc.connect();
			ArrayList<ArrayList<String>> dataMC = sqlc.selectUnion(tickers, colNames);
			sqlc.close();
			ArrayList<String> passMC = new ArrayList<String>();
			for(int i = 0; i < dataMC.size(); i++){
				String itrTick = dataMC.get(i).get(0);
				String itrMC = dataMC.get(i).get(1);
				passMC.add(itrTick);
				trackMC.put(itrTick, itrMC);
			}
			tickers = passMC;
		}else{
			String sbPath = Globals.sbase_path;
			FCI fciSB = new FCI(false, sbPath);
			ArrayList<String> passMC = new ArrayList<String>();
			for(int i = 0; i < tickers.size(); i++){
				ArrayList<String> sbRow = AhrIO.scanRow(sbPath+tickers.get(i)+".txt", ",", "2022-05-27");
				if(sbRow.size() > 0){
					String itrMC = sbRow.get(fciSB.getIdx("market_cap"));
					int mcVal = 0;
					try{
						mcVal = Integer.parseInt(itrMC);
					}catch(NumberFormatException e){
					}
					if(mcVal >= mcRange.getStartVal() && mcVal <= mcRange.getEndVal()){
						passMC.add(tickers.get(i));
						trackMC.put(tickers.get(i), itrMC);
					}
				}
			}
			tickers = passMC;
		}
	}

	//apply all filter lines and return all stocks that fall within all ranges
	public void applyFilter(String date){
		//System.out.println("--> In applyFilter("+date+")");
		//System.out.println("--> sf text :\n"+getText());
		//cols [0]  ticker
		//	   [1]  mc
		//	   [2]  code
		//	   [3]+ other inds

		//reset tracker vars
		tickers = new ArrayList<String>();
		trackMC = new HashMap<String, String>();
		trackCodes = new HashMap<String, String>();
		trackInds = new HashMap<String, ArrayList<Integer>>();
		results = new ArrayList<ArrayList<String>>();
		//get ByDate data from given date
		String bdPath = Globals.bydate_path;
		FCI fciBD = new FCI(false, bdPath);
		ArrayList<ArrayList<String>> bdFile = new ArrayList<ArrayList<String>>();
		if(Globals.uses_mysql_source){
			SQLCode sqlc = new SQLCode(Globals.default_source);
			sqlc.setDB("bydate");
			//sqlc.setWhereCond("WHERE `date` = '"+date+"'");
			sqlc.connect();
			bdFile = sqlc.selectAll("2022-05-27", fciBD.getTags());
			sqlc.close();
		}else{
			bdFile = AhrIO.scanFile(bdPath+date+".txt", "~");
		}
		tickers = AhrAL.getCol(bdFile, fciBD.getIdx("ticker"));
		//run the seperate filters for (1) indicators, (2) sec/ind, then (3) MC
		applySecIndFilter();
		applyIndicatorFilter(bdFile);
		applyMarketCapFilter();
		//save results
		for(int i = 0; i < tickers.size(); i++){
			String itrTick = tickers.get(i);
			ArrayList<String> line = new ArrayList<String>();
			line.add(tickers.get(i));
			line.add(trackMC.get(itrTick));
			line.add(trackCodes.get(itrTick));
			if(indRanges.size() > 0){
				ArrayList<Integer> indVals = trackInds.get(itrTick);
				for(int j = 0; j < indVals.size(); j++){
					line.add(String.valueOf(indVals.get(j)));
				}
			}
			results.add(line);
		}
		
		//print out results
		//System.out.println("==> Error Ticks on "+date+" ("+errTicks.size()+" total): "+errTicks);
		//System.out.println("--> Final Results ... ");
		//AhrAL.print(this.results);
	}

	//return single string of filter data
	public String getText(){
		String ftext = "";
		//add MC data
		ftext += "mc~";
		ftext += String.valueOf(this.mcRange.getStartVal())+"~";
		ftext += String.valueOf(this.mcRange.getEndVal());
		//add sector data
		ftext += "\nsector~";
		String secRaw = "";
		for(int i = 0; i < this.sectors.size(); i++){
			if(i == (this.sectors.size()-1)){
				secRaw += this.sectors.get(i);
			}else{
				secRaw += this.sectors.get(i) + ",";
			}
		}
		ftext += secRaw;
		//add industry data
		ftext += "\nindustry~";
		String indRaw = "";
		for(int i = 0; i < this.industries.size(); i++){
			if(i == (this.industries.size()-1)){
				indRaw += this.industries.get(i);
			}else{
				indRaw += this.industries.get(i) + ",";
			}
		}
		ftext += indRaw;	
		//add all indicator data
		for(int i = 0; i < this.indRanges.size(); i++){
			NameAndRange itrNAR = this.indRanges.get(i);
			ftext += "\n"+itrNAR.getName()+"~";
			ftext += String.valueOf(itrNAR.getStartVal())+"~";
			ftext += String.valueOf(itrNAR.getEndVal());
		}
		ftext += "\n";

		return ftext;		
	}

	//return all filter data into formatted data block
	public ArrayList<ArrayList<String>> getData(){
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		//add MC data
		ArrayList<String> mcLine = AhrAL.toAL(new String[]{"mc"});
		mcLine.add(String.valueOf(this.mcRange.getStartVal()));
		mcLine.add(String.valueOf(this.mcRange.getEndVal()));
		//add sector data
		ArrayList<String> sectorLine = AhrAL.toAL(new String[]{"sector"});
		String secRaw = "";
		for(int i = 0; i < this.sectors.size(); i++){
			if(i != (this.sectors.size()-1)){
				secRaw += this.sectors.get(i) + ",";
			}else{
				secRaw += this.sectors.get(i);
			}
		}
		sectorLine.add(secRaw);
		//add industry line 
		ArrayList<String> industryLine = AhrAL.toAL(new String[]{"industry"});
		String indRaw = "";
		for(int i = 0; i < this.industries.size(); i++){
			if(i != (this.industries.size()-1)){
				indRaw += this.industries.get(i) + ",";
			}else{
				indRaw += this.industries.get(i);
			}
		}
		industryLine.add(indRaw);
		//add above data together
		data.add(mcLine);
		data.add(sectorLine);
		data.add(industryLine);
		//add all indicator data
		for(int i = 0; i < this.indRanges.size(); i++){
			NameAndRange itrNAR = this.indRanges.get(i);
			ArrayList<String> indLine = AhrAL.toAL(new String[]{itrNAR.getName()});
			indLine.add(String.valueOf(itrNAR.getStartVal()));
			indLine.add(String.valueOf(itrNAR.getEndVal()));
			data.add(indLine);
		}

		return data;	
	}

	//rest filter to def vals
	public void resetFilter(){
		this.scodes = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
		this.sectors = new ArrayList<String>();
		this.industries = new ArrayList<String>();
		this.mcRange = new NameAndRange("mc");
		this.indRanges = new ArrayList<NameAndRange>();
	}
	//reset just sector codes
	public void clearSectorCodes(){
		this.scodes = new ArrayList<ArrayList<String>>();
	}
	//reset just results al
	public void clearResults(){
		this.results = new ArrayList<ArrayList<String>>();
	}
	
	
}
