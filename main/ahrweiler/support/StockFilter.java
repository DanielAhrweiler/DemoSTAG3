package ahrweiler.support;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrGen;
import java.io.*;
import java.util.ArrayList;

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

	ArrayList<ArrayList<String>> scodes;
	ArrayList<ArrayList<String>> results;
	ArrayList<String> sectors;
	ArrayList<String> industries;
	NameAndRange mc_range;
	ArrayList<NameAndRange> ind_ranges;
	

	public StockFilter(){
		this.scodes = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
		this.sectors = new ArrayList<String>();
		this.industries = new ArrayList<String>();
		this.mc_range = new NameAndRange("mc");
		this.ind_ranges = new ArrayList<NameAndRange>();	
	}
	public StockFilter(ArrayList<ArrayList<String>> filters){
		this.scodes = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
		this.sectors = new ArrayList<String>();
		this.industries = new ArrayList<String>();
		this.ind_ranges = new ArrayList<NameAndRange>();
		for(int i = 0; i < filters.size(); i++){
			if(filters.get(i).get(0).contains("ind") && !filters.get(i).get(0).equals("industry")){
				this.ind_ranges.add(new NameAndRange(filters.get(i)));
			}else if(filters.get(i).get(0).equals("mc")){
				this.mc_range = new NameAndRange(filters.get(i));
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
		this.scodes = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
		this.sectors = new ArrayList<String>();
		this.industries = new ArrayList<String>();
		this.ind_ranges = new ArrayList<NameAndRange>();
		//sort out the filter lines
		ArrayList<ArrayList<String>> filters = AhrIO.scanFile(fpath, "~");
		System.out.println("--> In StockFilter(fpath) constructor, filters are ...");
		AhrAL.print(filters);
		for(int i = 0; i < filters.size(); i++){
			if(filters.get(i).get(0).contains("ind") && !filters.get(i).get(0).equals("industry")){
				this.ind_ranges.add(new NameAndRange(filters.get(i)));
			}else if(filters.get(i).get(0).equals("mc")){
				this.mc_range = new NameAndRange(filters.get(i));
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
		for(int i = 0; i < this.ind_ranges.size(); i++){
			inds.add(this.ind_ranges.get(i).getName());
		}
		return inds;
	}
	public void setMarketCap(int startVal, int endVal){
		this.mc_range.setStartVal(startVal);
		this.mc_range.setEndVal(endVal);
	}
	public void setSectors(String secRaw){
		this.sectors = new ArrayList<String>();
		String[] allSec = secRaw.split(",");
		for(int i = 0; i < allSec.length; i++){
			if(AhrGen.isInt(allSec[i])){
				this.sectors.add(allSec[i]);
			}
		}
	}
	public void setIndustries(String indRaw){
		this.industries = new ArrayList<String>();
		String[] allInd = indRaw.split(",");
		for(int i = 0; i < allInd.length; i++){
			if(AhrGen.isInt(allInd[i])){
				this.industries.add(allInd[i]);
			}
		}
	}

	//OTHER FUNCTIONS

	//add an indicator name, start val, and end val
	public void addIndicatorFilter(ArrayList<String> filterLine){
		this.ind_ranges.add(new NameAndRange(filterLine));
	}
	//edit already existing indicator range
	public void editIndicatorFilter(ArrayList<String> filterLine){
		int idx = -1;
		String newName = filterLine.get(0);
		for(int i = 0; i < this.ind_ranges.size(); i++){
			NameAndRange itrNAR = this.ind_ranges.get(i);
			String oldName = itrNAR.getName();
			if(oldName.equals(newName)){
				idx = i;
				break;
			}
		}
		if(idx != -1){
			this.ind_ranges.set(idx, new NameAndRange(filterLine));
		}
	}

	//apply all filter lines and return all stocks that fall within all ranges
	public void applySecIndFilter(){
		//cols [0]  ticker
		//	   [1]  mc
		//	   [2]  code
		//	   [3]+ other inds
		System.out.println("--> In applySecIndFilter()");

		//get all (non xx) codes from sector_ticks.txt
		ArrayList<ArrayList<String>> stFC = AhrIO.scanFile("./../in/sector_ticks.txt", ",");
		ArrayList<Integer> allCodes = new ArrayList<Integer>();
		for(int i = 0; i < stFC.size(); i++){
			String itrCode = stFC.get(i).get(0);
			if(!itrCode.contains("xx")){
				allCodes.add(Integer.parseInt(stFC.get(i).get(0)));
			}
		}
	
		System.out.println("--> Sector List : " + this.sectors);
		System.out.println("--> Industry List : " + this.industries);
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
		System.out.println("  > stList = " + stList);
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
		System.out.println("--> Ticker / SCode after applySecIndFilter() ... ");
		AhrAL.print(this.scodes);
	}

	//apply all filter lines and return all stocks that fall within all ranges
	public void applyFilter(String date){
		System.out.println("--> In applyFilter("+date+")");
		//cols [0]  ticker
		//	   [1]  mc
		//	   [2]  code
		//	   [3]+ other inds
		//if no tickers, run applySecIndFilters() to get ticker list
		if(this.scodes.size() == 0){
			applySecIndFilter();
		}

		//get Clean data for this date
		String bdPath = "./../../DB_Intrinio/Clean/ByDate/";
		FCI fciBD = new FCI(false, bdPath);
		ArrayList<ArrayList<String>> bdFile = AhrIO.scanFile(bdPath+date+".txt", "~");

		//get stocks that pass all indicator rules
		for(int i = 0; i < this.scodes.size(); i++){
			String ticker = this.scodes.get(i).get(0);
			String scode = this.scodes.get(i).get(1);
			if(i%500==0){
				//System.out.println("--> bdFile progress: "+i+" out of "+bdFile.size());
			}
			ArrayList<String> vals = new ArrayList<String>();
			double mcVal = -1.0;
			boolean is_in_bd = true;
			boolean passes_ind_rules = true;
			boolean passes_mc = true;
			for(int j = 0; j < this.ind_ranges.size(); j++){
				NameAndRange itrNAR = this.ind_ranges.get(j);
				String vname = itrNAR.getName();
				int vrs = itrNAR.getStartVal();
				int vre = itrNAR.getEndVal();
				int bdIdx = AhrAL.getRowIdx(bdFile, ticker);
				if(bdIdx == -1){
					is_in_bd = false;
				}else{
					int itrVal = Integer.parseInt(bdFile.get(bdIdx).get(fciBD.getIdx(vname)));
					if(itrVal < vrs || itrVal > vre){
						passes_ind_rules = false;
					}
					vals.add(String.valueOf(itrVal));
				}

			}
			if(is_in_bd && passes_ind_rules){//still have to check for mc, industry and sector
				//check market cap
				FCI fciSB = new FCI(false, "./../../DB_Intrinio/Main/S_Base/");
				ArrayList<String> sbLine = AhrIO.scanRow("./../../DB_Intrinio/Main/S_Base/"+ticker+".txt","~",date);
				//System.out.println("--> Date: "+date+"  |  Ticker: "+ticker+"  | sbLine: "+sbLine);
				if(sbLine.size() < 6){
					//System.out.println("ERR ==> sbLine: "+sbLine);
					//errTicks.add(ticker);
				}else{
					mcVal = Double.parseDouble(sbLine.get(fciSB.getIdx("market_cap")));
				}
				if(mcVal < this.mc_range.getStartVal() || mcVal > this.mc_range.getEndVal()){
					passes_mc = false;
				}
			}
			if(is_in_bd && passes_ind_rules && passes_mc){
				vals.add(0, scode);
				vals.add(0, String.format("%.4f", mcVal));
				vals.add(0, ticker);
				this.results.add(vals);
			}
		}
		//print out results
		//System.out.println("==> Error Ticks on "+date+" ("+errTicks.size()+" total): "+errTicks);
		System.out.println("--> Final Results ... ");
		AhrAL.print(this.results);
	}

	//return single string of filter data
	public String getText(){
		String ftext = "";
		//add MC data
		ftext += "mc~";
		ftext += String.valueOf(this.mc_range.getStartVal())+"~";
		ftext += String.valueOf(this.mc_range.getEndVal());
		//add sector data
		ftext += "\nsector~";
		String secRaw = "";
		for(int i = 0; i < this.sectors.size(); i++){
			if(i == this.sectors.size()-1){
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
			if(i == this.industries.size()-1){
				indRaw += this.industries.get(i);
			}else{
				indRaw += this.industries.get(i) + ",";
			}
		}
		ftext += indRaw;	
		//add all indicator data
		for(int i = 0; i < this.ind_ranges.size(); i++){
			NameAndRange itrNAR = this.ind_ranges.get(i);
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
		mcLine.add(String.valueOf(this.mc_range.getStartVal()));
		mcLine.add(String.valueOf(this.mc_range.getEndVal()));
		//add sector data
		ArrayList<String> sectorLine = AhrAL.toAL(new String[]{"sector"});
		String secRaw = "";
		for(int i = 0; i < this.sectors.size(); i++){
			if(i == this.sectors.size()-1){
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
			if(i == this.industries.size()-1){
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
		for(int i = 0; i < this.ind_ranges.size(); i++){
			NameAndRange itrNAR = this.ind_ranges.get(i);
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
		this.mc_range = new NameAndRange("mc");
		this.ind_ranges = new ArrayList<NameAndRange>();
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
