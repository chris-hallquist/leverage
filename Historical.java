import java.io.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class Historical {
  private static final String STOCK_FILENAME = "table.csv";
  private static final String TBILL_FILENAME = "TB3MS.csv";

  private static final DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
  private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

  private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

  private static ArrayList<MonthData> allMonthData = new ArrayList();
  private static LinkedList<OneDayReturn> dailyReturns = new LinkedList();

  private static HashMap<YearMonth, Integer> monthIndex = new HashMap();

  public static void main(String[] args) 
      throws FileNotFoundException, IOException, ParseException {
    retrieveData();

    double stocks = 100;
    double mixed = 100; // Half a normal fund, half a 2x fund
    double etf2x = 100;
    double etf3x = 100;

    Date previous = null;

    for (OneDayReturn r : dailyReturns) {
      double returns = r.getReturns();
      stocks *= returns;

      Date d = r.getDate();
      
      int elapsed;
      if (previous == null) {
        elapsed = 1; // By default, assume 1 day since the last trading day
      } else {
        elapsed = (int) ((d.getTime() - previous.getTime())/DAY_IN_MILLIS);        
      }

      YearMonth ym = r.getYearMonth();
      Integer mi = monthIndex.get(ym);

      if (mi == null)
        break;

      MonthData mData = allMonthData.get(mi);
      double interest = mData.getDailyRate();

      mixed *= Math.pow(1 - interest / 2, elapsed);
      mixed *= (returns * 1.5 - 0.5);

      etf2x *= Math.pow(1 - interest, elapsed);
      etf2x *= (returns * 2 - 1);

      etf3x *= Math.pow(1 - interest * 2, elapsed);
      etf3x *= (returns * 3 - 2);
  
      previous = d;
    }

    System.out.println("Last day: " + previous.toString());
    System.out.println("Total stock returns over this time period: " + stocks + "%");
    System.out.println("Total mixed returns over this time period: " + mixed + "%");
    System.out.println("Total 2XETF returns over this time preiod: " + etf2x + "%");
    System.out.println("Total 3XETF returns over this time preiod: " + etf3x + "%");
  }

  public static void retrieveData() 
      throws FileNotFoundException, IOException, ParseException {
    ArrayList<String[]> rawArr = new ArrayList();

    BufferedReader stockReader = new BufferedReader(new FileReader(STOCK_FILENAME));
    String sl = stockReader.readLine(); // Skip first line

    while ((sl = stockReader.readLine()) != null) {
      String[] splitSl = sl.split(",");
      rawArr.add(splitSl);
    }

    for (int i = 0; i < rawArr.size() - 1; i++) {
      Date date = dFormat.parse(rawArr.get(i)[0]);

      double todaysClose = Double.parseDouble(rawArr.get(i)[4]);
      double yesterdaysClose = Double.parseDouble(rawArr.get(i + 1)[4]);
      double returns = todaysClose / yesterdaysClose;

      dailyReturns.addFirst(new OneDayReturn(date, returns));
    }

    BufferedReader tReader = new BufferedReader(new FileReader(TBILL_FILENAME));
    String tl = tReader.readLine(); // Skip first line

    while ((tl = tReader.readLine()) != null) {
      String[] splitTl = tl.split(",");
      YearMonth ym = YearMonth.parse(splitTl[0], dtFormatter);
      double rate = Double.parseDouble(splitTl[1]) / 100;

      allMonthData.add(new MonthData(ym, rate));

      monthIndex.put(ym, allMonthData.size() - 1);
    }
  }
}

class MonthData {
  private static final GregorianCalendar gCalendar = new GregorianCalendar();

  private YearMonth month;
  private double tRate;
  private double dailyRate;

  private static final double ADDITIONAL_RATE = 0.01;

  public MonthData(YearMonth month, double tRate) {
    this.month = month;
    this.tRate = tRate;

    boolean leap = gCalendar.isLeapYear(month.getYear());
    float days = 365 + (leap ? 1 : 0);

    this.dailyRate = Math.pow(/* tRate */ + ADDITIONAL_RATE + 1, 1 / days) - 1;
  }

  public YearMonth getMonth() {
    return month;
  }

  public double getTRate() {
    return tRate;
  }

  public double getDailyRate() {
    return dailyRate;
  }
}

class OneDayReturn {
  private Date date;
  private double returns;

  private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
  private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

  public OneDayReturn(Date date, double returns) {
    this.date = date;
    this.returns = returns;
  }

  public Date getDate() {
    return date;
  }

  public double getReturns() {
    return returns;
  }

  public YearMonth getYearMonth() {
    return YearMonth.parse(format.format(date), dtFormatter);    
  }
}
