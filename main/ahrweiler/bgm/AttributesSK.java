package ahrweiler.bgm;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrAL;
import java.util.ArrayList;

//class only for holding all attrs of a BGM SK
public class AttributesSK {

	private boolean call;
	private String annMethod;
	private String gaMethod;
	private String sdate;
	private String edate;
	private int spd;
	private int tvi;
	private double plateau;
	private double learnRate;
	private String msMask;
	private String narMask;
	private String indMask;
	private int activeIndNum;
	private ArrayList<String> activeIndNames;
	

	//constructors

	//default attrs
	public AttributesSK(){
		this.call = true;
		this.annMethod = "CR";			//continuous range
		this.gaMethod = "FD";			//fast decrease
		this.sdate = "2016-01-01";
		this.edate = "2020-12-31";
		this.spd = 10;
		this.tvi = 6;
		this.plateau = 20.0;
		this.learnRate = 0.10;
		this.msMask = "xxxxxxxx";
		this.narMask = "1111";
		this.indMask = "111111111111111111111111";
	}
	//get attrs from file
	public AttributesSK(String path){
		//default values in case line is not in file
		this.call = true;
		this.annMethod = "CR";			//continuous range
		this.gaMethod = "FD";			//fast decrease
		this.sdate = "2016-01-01";
		this.edate = "2020-12-31";
		this.spd = 10;
		this.tvi = 6;
		this.plateau = 20.0;
		this.learnRate = 0.10;
		this.msMask = "xxxxxxxx";
		this.narMask = "1111";
		this.indMask = "111111111111111111111111";
		//get data from file is possible
		ArrayList<ArrayList<String>> fc = AhrIO.scanFile(path, ",");
		int callIdx = AhrAL.getRowIdx(fc, "call");
		if(callIdx != -1){
			String callStr = fc.get(callIdx).get(1).toLowerCase();
			if(callStr.equals("long")){
				this.call = true;
			}else if(callStr.equals("short")){
				this.call = false;
			}
		}
		int annMethIdx = AhrAL.getRowIdx(fc, "ann_method");
		if(annMethIdx != -1){
			String annMethStr = fc.get(annMethIdx).get(1).toLowerCase();
			if(annMethStr.equals("cr") || annMethStr.equals("continuous range") || annMethStr.equals("cont range")){
				this.annMethod = "CR";
			}else if(annMethStr.equals("bn") || annMethStr.equals("binomial")){
				this.annMethod = "BN";
			}
		}
		int sdateIdx = AhrAL.getRowIdx(fc, "start_date");
		if(sdateIdx != -1){
			this.sdate = fc.get(sdateIdx).get(1);
		}
		int edateIdx = AhrAL.getRowIdx(fc, "end_date");
		if(edateIdx != -1){
			this.edate = fc.get(edateIdx).get(1);
		}
		int spdIdx = AhrAL.getRowIdx(fc, "spd");
		if(spdIdx != -1){
			this.spd = Integer.parseInt(fc.get(spdIdx).get(1));
		}
		int tviIdx = AhrAL.getRowIdx(fc, "tvi");
		if(tviIdx != -1){
			this.tvi = Integer.parseInt(fc.get(tviIdx).get(1));
		}
		int platIdx = AhrAL.getRowIdx(fc, "plateau");
		if(platIdx != -1){
			this.plateau = Double.parseDouble(fc.get(platIdx).get(1));
		}
		int lrateIdx = AhrAL.getRowIdx(fc, "learn_rate");
		if(lrateIdx != -1){
			this.learnRate = Double.parseDouble(fc.get(lrateIdx).get(1));
		}
		int msMaskIdx = AhrAL.getRowIdx(fc, "ms_mask");
		if(msMaskIdx != -1){
			this.msMask = fc.get(msMaskIdx).get(1);
		}
		int narMaskIdx = AhrAL.getRowIdx(fc, "nar_mask");
		if(narMaskIdx != -1){
			this.narMask = fc.get(narMaskIdx).get(1);
		}
		int indMaskIdx = AhrAL.getRowIdx(fc, "ind_mask");
		if(indMaskIdx != -1){
			this.indMask = fc.get(indMaskIdx).get(1);
		}
	}

	//can pull attrs saved in file
	public AttributesSK(String bgm, int id){

	}

	//getters & setters
	public boolean getCall(){
		return this.call;
	}
	public void setCall(boolean call){
		this.call = call;
	}
	public String getAnnMethod(){
		return this.annMethod;
	}
	public void setAnnMethod(String method){
		this.annMethod = method;
	}
	public String getSDate(){
		return this.sdate;
	}
	public void setSDate(String date){
		this.sdate = date;
	}
	public String getEDate(){
		return this.edate;
	}
	public void setEDate(String date){
		this.edate = date;
	}
	public int getSPD(){
		return this.spd;
	}
	public void setSPD(int spd){
		this.spd = spd;
	}
	public int getTVI(){
		return this.tvi;
	}
	public void setTVI(int tvi){
		this.tvi = tvi;
	}
	public double getPlateau(){
		return this.plateau;
	}
	public void setPlateau(double plateau){
		this.plateau = plateau;
	}
	public double getLearnRate(){
		return this.learnRate;
	}
	public void setLearnRate(double rate){
		this.learnRate = rate;
	}
	public String getMsMask(){
		return this.msMask;
	}
	public void setMsMask(String mask){
		this.msMask = mask;
	}
	public String getNarMask(){
		return this.narMask;
	}
	public void setNarMask(String mask){
		this.narMask = mask;
	}
	public String getIndMask(){
		return this.indMask;
	}
	public void setIndMask(String mask){
		this.indMask = mask;
	}

	//save attrs to file
	public void saveToFile(String path){
		ArrayList<ArrayList<String>> tf = new ArrayList<ArrayList<String>>();
		tf.add(AhrAL.toAL(new String[]{"call", (new Boolean(this.call)).toString()}));
		tf.add(AhrAL.toAL(new String[]{"ann_method", this.annMethod}));
		tf.add(AhrAL.toAL(new String[]{"ga_method", this.gaMethod}));
		tf.add(AhrAL.toAL(new String[]{"start_date", this.sdate}));
		tf.add(AhrAL.toAL(new String[]{"end_date", this.edate}));
		tf.add(AhrAL.toAL(new String[]{"spd", String.valueOf(spd)}));
		tf.add(AhrAL.toAL(new String[]{"tvi", String.valueOf(tvi)}));
		tf.add(AhrAL.toAL(new String[]{"plateau", String.format("%.3f", this.plateau)}));
		tf.add(AhrAL.toAL(new String[]{"learn_rate", String.format("%.4f", this.learnRate)}));
		tf.add(AhrAL.toAL(new String[]{"ms_mask", this.msMask}));
		tf.add(AhrAL.toAL(new String[]{"nar_mask", this.narMask}));
		tf.add(AhrAL.toAL(new String[]{"ind_mask", this.indMask}));
		AhrIO.writeToFile(path, tf, ",");
	}

}
