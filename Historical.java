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

  private static ArrayList<MonthData> allMonthData = new ArrayList();
  private static LinkedList<OneDayReturn> dailyReturns = new LinkedList();

  private static HashMap<YearMonth, Integer> monthIndex = new HashMap();

  public static void main(String[] args) 
      throws FileNotFoundException, IOException, ParseException {
    retrieveData();

    double stocks = 100;
    for (OneDayReturn r : dailyReturns) {
      stocks *= r.getReturns();
    }

    System.out.println("Total stock returns over this time period: " + stocks + "%");
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
    int days = 365 + (leap ? 1 : 0);

    this.dailyRate = Math.log(tRate + ADDITIONAL_RATE + 1)/Math.log(days) - 1;
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
