package ahrweiler;

//define globals for all of STAG3
public class Globals {

    //database structure
	public static final int intrinio_num = 11;				//num of columns in 1st form Intrinio files (before cleaning)
	public static final int yahoo_num = 7;					//num of columns in 1st form Yahoo files
	public static final int snorm_num = 24;					//indicators that are in S_Raw and S_Norm
	public static final int sraw_num = 7;					//indicators that are just in S_Raw
	public static final int inorm_num = 9;					//indicators that are in I_Raw and I_Norm
	public static final int iraw_num = 1;					//indicators that are just in I_Raw
	public static final int mnorm_num = 5;					//indicators that are in M_Raw and M_Norm
	public static final int mraw_num = 1;					//indicators that are just in M_Raw
	public static final int sbp_num = 8;					//indicators calculated in backpropagation of S_Raw
	public static final int ibp_num = 4;					//indicators calculated in backpropagation of I_Raw
	public static final int sli_num = snorm_num + sraw_num;	//tot # of stock lvl indicators
	public static final int ili_num = inorm_num + iraw_num;	//tot # of industry lvl indicators
	public static final int mli_num = mnorm_num + mraw_num;	//tot # of market lvl indicators
	public static final int target_var_num = 10;			//num of target variables 

	//genetic structure
	public static final int gad2_bpi = 24;			//bits per indicator for GAD2
	public static final int gad2_part1 = 8;			//bit in weight part
	public static final int gad2_part2 = 16;		//bits in value part
	public static final int gab2_bpi = 32;			//bits per indicator for GAB2
	public static final int gab2_part1 = 16;		//bits in lower bounds
	public static final int gab2_part2 = 16;		//bits in upper bounds
	public static final int gab3_bpi = 40;			//bits per indicator for GAB3
	public static final int gab3_part1 = 8;			//bits in weight part
	public static final int gab3_part2 = 16;		//bits in lower bounds part
	public static final int gab3_part3 = 16;		//bits in upper bounds part

	//other
	public static final int min_file_size = 200;
	public static final String[] ind_names = {"S/M SMA(20)", "S/M SMA(10)", "S/M SMA(5)", "S/M SMA(2)", "S/I SMA(20)",
									"S/I SMA(10)", "S/I SMA(5)", "S/I SMA(2)", "SMA(20)", "SMA(10)", "SMA(5)",
									"SMA(2)", "RSI(14)", "MACD(12,26)", "MCDH(9,26)", "CMF(20)", "BBW(20)", "%B(20)",
									"ROC(12)", "MFI(14)", "CCI(20)", "Mass(9,25)", "TSI(25,13)", "UltOsc(7,14,28)"};
	public static final String[] tvi_names = {"appr_intra1", "appr_inter1", "appr_intra2", "appr_inter2", "appr_intra3",
									"appr_inter3", "appr_intra5", "appr_inter5", "appr_intra10", "appr_inter10"};
	public static final String[] tvi_monikers = {"1-Day Intra %", "1-Day Inter %", "2-Day Intra %", "2-Day Inter %",
									"3-Day Intra %", "3-Day Inter %", "5-Day Intra %", "5-Day Inter %", "10-Day Intra %",
									"10-Day Inter %"};


}
