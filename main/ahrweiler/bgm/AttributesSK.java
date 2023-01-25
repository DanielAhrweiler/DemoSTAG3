package ahrweiler.bgm;
import java.util.ArrayList;

//class only for holding all attrs of a BGM SK
public class AttributesSK {

	private boolean call;
	private String annMethod;
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
		this.annMethod = "CR";
		this.sdate = "2016-01-01";
		this.edate = "2020-12-31";
		this.spd = 10;
		this.tvi = 4;
		this.plateau = 12.0;
		this.learnRate = 0.10;
		this.msMask = "xxxxxxxx";
		this.narMask = "1111";
		this.indMask = "111111111111111111111111";
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

}
