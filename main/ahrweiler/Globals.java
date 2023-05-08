package ahrweiler;

//define globals for all of STAG3
public class Globals {

	//MySQL data source
	public static final boolean uses_mysql_source = true;
	public static final String mysql_sbase_path = "jdbc:mysql://danielahrweiler.com:3306/daniela5_sbase";
	public static final String mysql_snorm_path = "jdbc:mysql://danielahrweiler.com:3306/daniela5_snorm";
	public static final String mysql_ibase_path = "jdbc:mysql://danielahrweiler.com:3306/daniela5_ibase";
	public static final String mysql_mbase_path = "jdbc:mysql://danielahrweiler.com:3306/daniela5_mbase";
	public static final String mysql_bydate_path = "jdbc:mysql://danielahrweiler.com:3306/daniela5_bydate";
	public static final String mysql_username = "daniela5_daniel";
	public static final String mysql_password = "mill138squid.";
	public static final String[] mysql_sbase_cols = new String[]{"date", "open", "high", "low", "close", "vol",
						"daily_market_cap", "market_cap", "ticker"};
	public static final String[] mysql_snorm_cols = new String[]{"date", "ind0", "ind1", "ind2", "ind3", "ind4",
						"ind5", "ind6", "ind7", "ind8", "ind9", "ind10", "ind11", "ind12", "ind13", "ind14",
						 "ind15", "ind16", "ind17", "ind18", "ind19", "ind20", "ind21", "ind22", "ind23",
						"appr_intra1", "appr_inter1", "appr_intra2", "appr_inter2", "appr_intra3", "appr_inter3",
						"appr_intra5", "appr_inter5", "appr_intra10", "appr_inter10", "ticker"};
	//note: ibase cols are specific to each sector file, according to hm industries there are per sector
	public static final String[] mysql_mbase_cols = new String[]{"date", "dmc_close_all", "mc_close_all", 
						"dmc_close_amex", "mc_close_amex", "dmc_close_nasdaq", "mc_close_nasdaq", "dmc_close_nyse",
						"mc_close_nyse", "dmc_close_other", "mc_close_other"};
	public static final String[] mysql_bydate_cols = new String[]{"ticker", "ind0", "ind1", "ind2", "ind3", "ind4",
						"ind5", "ind6", "ind7", "ind8", "ind9", "ind10", "ind11", "ind12", "ind13", "ind14",
						 "ind15", "ind16", "ind17", "ind18", "ind19", "ind20", "ind21", "ind22", "ind23", "nar_mask",
						"appr_intra1", "appr_inter1", "appr_intra2", "appr_inter2", "appr_intra3", "appr_inter3",
						"appr_intra5", "appr_inter5", "appr_intra10", "appr_inter10"};
	
	//database paths
	public static final String sbase_path = "./../../DB_Intrinio/Main/S_Base/";
	public static final String sraw_path = "./../../DB_Intrinio/Main/S_Raw/";
	public static final String snorm_path = "./../../DB_Intrinio/Main/S_Norm/";
	public static final String ibase_path = "./../../DB_Intrinio/Main/I_Base/";
	public static final String mbase_path = "./../../DB_Intrinio/Main/M_Base/";
	public static final String bydate_path = "./../../DB_Intrinio/Clean/ByDate/";
	public static final String bystock_path = "./../../DB_Intrinio/Clean/ByStock/";

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
