package ahrweiler.util;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrGen;
import ahrweiler.support.FCI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.time.LocalDate;

public class AhrDate {

	//sort dates chronologically
	public static void sortDates(ArrayList<String> al, boolean lr_first){
		Collections.sort(al, new Comparator<String>(){
			@Override
			public int compare(String str1, String str2){
				LocalDate date1 = LocalDate.parse(str1);
				LocalDate date2 = LocalDate.parse(str2);
				if(lr_first){
					return date1.compareTo(date2);
				}else{
					return (date1.compareTo(date2) * -1);
				}
			}
		});
	}
	public static void sortDates2D(ArrayList<ArrayList<String>> al, boolean lr_first, int colIdx){
		Collections.sort(al, new Comparator<ArrayList<String>>(){
			@Override
			public int compare(ArrayList<String> str1, ArrayList<String> str2){
				LocalDate date1 = LocalDate.parse(str1.get(colIdx));
				LocalDate date2 = LocalDate.parse(str2.get(colIdx));
				if(lr_first){
					return date1.compareTo(date2);
				}else{
					return (date1.compareTo(date2) * -1);
				}
			}
		});
	}
	
	//get todays date in YYYY-MM-DD format
	public static String getTodaysDate(){
		LocalDate local = LocalDate.now();
		String year = String.valueOf(local.getYear());
		String month = String.valueOf(local.getMonthValue());
		if(month.length() == 1){
			month = "0" + month;
		}
		String day = String.valueOf(local.getDayOfMonth());
		if(day.length() == 1){
			day = "0" + day;
		}
		String date = year + "-" + month + "-" + day;
		return date; 
	}

	//get list of dates (in stock DB) inbetween (and including) a start and end date
	public static ArrayList<String> getDatesBetween(String sdate, String edate){
		ArrayList<String> dates = new ArrayList<String>();
		FCI fciOD = new FCI(false, "./../in/open_dates.txt");
		int didx = fciOD.getIdx("date");
		ArrayList<String> odates = AhrIO.scanCol("./../in/open_dates.txt", ",", didx);
		for(int i = 0; i < odates.size(); i++){
			String itrDate = odates.get(i);
			if(compareDates(itrDate, sdate) == 0 || compareDates(itrDate, sdate) == -1){
				if(compareDates(itrDate, edate) == 0 || compareDates(itrDate, edate) == 1){
					dates.add(itrDate);
				}
			}
		}
		Collections.reverse(dates);
		return dates;	//returns least recent to most recent 
	}

	//compare two dates and return code for which is the more recent, or if they are equal
	//return -1 if date1 is >, 0 if they are equal, 1 if date2 >
	public static int compareDates(String date1, String date2){
		int comparer;
		String[] parts1 = date1.split("-");
		int y1 = Integer.parseInt(parts1[0]);
		int m1 = Integer.parseInt(parts1[1]);
		int d1 = Integer.parseInt(parts1[2]);
		String[] parts2 = date2.split("-");
		int y2 = Integer.parseInt(parts2[0]);
		int m2 = Integer.parseInt(parts2[1]);
		int d2 = Integer.parseInt(parts2[2]);
		int score1 = (m1*100) + d1;
		int score2 = (m2*100) + d2;
		if(y1 > y2){
		    comparer = -1;
		}else if(y1 == y2){
	    	if(score1 > score2){
				comparer = -1;
	    	}else if(score1 == score2){
				comparer = 0;
	    	}else{
				comparer = 1;
	    	}
		}else{
	    	comparer = 1;
		}
		return comparer;
	}

	//boolean for if a date is within a period of 2 other dates
	public static boolean isDateInPeriod(String itrDate, String sdate, String edate){
		boolean is_in_period = false;
		if(compareDates(itrDate, sdate) <= 0){
			if(compareDates(itrDate, edate) >= 0){
				is_in_period = true;
			}
		}
		return is_in_period;
	}

	//get most recent date in list of dates
	public static String mostRecentDate(ArrayList<String> dates){
		String mrDate = dates.get(0);
		for(int i = 1; i < dates.size(); i++){
			if(compareDates(mrDate, dates.get(i)) == 1){
				mrDate = dates.get(i);
			}
		}
		return mrDate;
	}

	//given any date, return closest date in open_dates.txt
	public static String closestDate(String inDate){
		FCI fciOD = new FCI(false, "./../in/open_dates.txt");
		ArrayList<ArrayList<String>> fc = AhrIO.scanFile("./../in/open_dates.txt", ",");
		int sstate = compareDates(fc.get(0).get(fciOD.getIdx("date")), inDate);//starting state
		if(sstate == 1){
			return fc.get(0).get(fciOD.getIdx("date"));
		}
		if(sstate == 0){
			return inDate;
		}
		String itrDate = "";
		for(int i = 1; i < fc.size(); i++){
			itrDate = fc.get(i).get(fciOD.getIdx("date"));
			int itrState = compareDates(itrDate, inDate);
			if(itrState != sstate){
				break;
			}
		}
		return itrDate;
	}
	//given date and AL, find datein AL that is closest to date (can be =)
	public static String closestDateInAL(String inDate, ArrayList<String> inAL){
		ArrayList<String> al = new ArrayList<String>(inAL);
		//alway make the order most recent to least recent
		if(compareDates(al.get(0), al.get(al.size()-1)) == 1){
			Collections.reverse(al);
		}
		//edge cases
		if(compareDates(inDate, al.get(0)) == -1){
			return al.get(0);
		}
		if(compareDates(inDate, al.get(al.size()-1)) == 1){
			return al.get(al.size()-1);
		}
		int sstate = compareDates(al.get(0), inDate);
		if(sstate == 0){
			return inDate;
		}
		//itr thru al
		String itrDate = "";
		for(int i = 0; i < al.size(); i++){
			itrDate = al.get(i);
			int itrState = compareDates(itrDate, inDate);
			if(itrState != sstate){
				break;
			}
		}
		return itrDate;
	}
	
	//merge 2 String ALs according to date (1st col)
	public static ArrayList<ArrayList<String>> mergeByDate(ArrayList<ArrayList<String>> al1, ArrayList<ArrayList<String>> al2,
														boolean has_header){
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
		int colNum1 = al1.get(1).size()-1;
		int colNum2 = al2.get(1).size()-1;
		//calc if al1 date order is ascending or descending
		boolean is_asc = true;
		int startIdx = 0;
		if(has_header){
			startIdx = 1;
		}
		String firstDate = al1.get(startIdx).get(0);
		String lastDate = al1.get(al1.size()-1).get(0);
		if(compareDates(firstDate, lastDate) < 0){
			is_asc = false;
		}
		final boolean is_asc_f = is_asc;
		//save and del header
		ArrayList<String> header = new ArrayList<String>();
		if(has_header){
			for(int i = 0; i < al1.get(0).size(); i++){
				header.add(al1.get(0).get(i));
			}
			for(int i = 1; i < al2.get(0).size(); i++){
				header.add(al2.get(0).get(i));
			}
			al1.remove(0);
			al2.remove(0);
		}
		//get all uniq dates from both ALs
		ArrayList<String> allUniqDates = new ArrayList<String>();
		ArrayList<String> dates1 = new ArrayList<String>();
		for(int i = 0; i < al1.size(); i++){
			dates1.add(al1.get(i).get(0));
			if(!allUniqDates.contains(al1.get(i).get(0))){
				allUniqDates.add(al1.get(i).get(0));
			}
		}
		ArrayList<String> dates2 = new ArrayList<String>();
		for(int i = 0; i < al2.size(); i++){
			dates2.add(al2.get(i).get(0));
			if(!allUniqDates.contains(al2.get(i).get(0))){
				allUniqDates.add(al2.get(i).get(0));
			}
		}
		//fill in missing date lines for both ALs
		for(int i = 0; i < allUniqDates.size(); i++){
			ArrayList<String> line = new ArrayList<String>();
			String itrDate = allUniqDates.get(i);
			line.add(itrDate);
			int idx1 = dates1.indexOf(itrDate);
			if(idx1 != -1){
				for(int j = 0; j < colNum1; j++){
					line.add(al1.get(idx1).get(j+1));
				}
			}else{
				for(int j = 0; j < colNum1; j++){
					line.add("NA");
				}
			}
			int idx2 = dates2.indexOf(itrDate);
			if(idx2 != -1){
				for(int j = 0; j < colNum2; j++){
					line.add(al2.get(idx2).get(j+1));
				}
			}else{
				for(int j = 0; j < colNum2; j++){
					line.add("NA");
				}
			}
			res.add(line);
		}
		//use comparator to order by date
		Collections.sort(res, new Comparator<ArrayList<String>>(){
			@Override
			public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
				String str1 = obj1.get(0);
				String str2 = obj2.get(0);
				if(is_asc_f){
					return str1.compareTo(str2);
				}else{
					return str1.compareTo(str2) * -1;
				}
			}
		});
		//add back header and return
		if(has_header){
			res.add(0, header);
		}
		return res;
	}	

	//sort a 2D AL by date (assuming date is 1st col)
	public static void sortByDate(ArrayList<ArrayList<String>> al, boolean is_asc){
		System.out.println("--> in sortByDate()");
		Collections.sort(al, new Comparator<ArrayList<String>>(){
			@Override
			public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
				String str1 = obj1.get(0);
				String str2 = obj2.get(0);
				int compVal = 1;
				if(!str2.equals("date")){
					if(is_asc){
						compVal = str1.compareTo(str2);
					}
				}
				System.out.println("str1 = " +str1+"  |  str2 = "+str2+"\n--> compVal = "+compVal);
				return compVal;
			}
		});	
	}
	
	//get list opf dates that passes market mask
	public static ArrayList<String> getDatesThatPassMarketMask(ArrayList<String> dates, String msMask){
		ArrayList<String> mdates = new ArrayList<String>();
		ArrayList<ArrayList<String>> mstates = AhrIO.scanFile("./../in/mstates.txt", ",");
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		for(int i = 0; i < mstates.size(); i++){
			String itrDate = mstates.get(i).get(fciMS.getIdx("date"));
			String itrMask = mstates.get(i).get(fciMS.getIdx("ms_mask"));
			if(dates.contains(itrDate) && AhrGen.compareMasks(msMask, itrMask)){
				mdates.add(itrDate);
			}
		}
		return mdates;
	}	
	
	//return mr date of out list of dates
	public static String maxDateInAL(ArrayList<String> al){
		String mrDate = al.get(0);
		for(int i = 1; i < al.size(); i++){
			if(compareDates(al.get(i), al.get(i-1)) == -1){
				mrDate = al.get(i);
			}
		}
		return mrDate;
	}
	public static String maxDate(String date1, String date2){
		String mrDate = date1;
		if(compareDates(date1, date2) == 1){
			mrDate = date2;
		}
		return mrDate;
	}

	//return least recent date of out list of dates
	public static String minDateInAL(ArrayList<String> al){
		String lrDate = al.get(0);
		for(int i = 1; i < al.size(); i++){
			if(compareDates(al.get(i), al.get(i-1)) == 1){
				lrDate = al.get(i);
			}
		}
		return lrDate;
	}
	public static String minDate(String date1, String date2){
		String lrDate = date1;
		if(compareDates(date1, date2) == -1){
			lrDate = date2;
		}
		return lrDate;
	}

}
