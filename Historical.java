import java.io.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class Historical {
  private static final String STOCK_FILENAME = "table.csv";
  private static final String TBILL_FILENAME = "TB3MS.csv";
  private static final int PRICE_COL = 6;

  private static final DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
  private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

  private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

  private static ArrayList<MonthData> allMonthData = new ArrayList();
  private static LinkedList<OneDayReturn> dailyReturns = new LinkedList();

  private static HashMap<YearMonth, Integer> monthIndex = new HashMap();

  public static void main(String[] args) 
      throws FileNotFoundException, IOException, ParseException {
    retrieveData();

    ConfigurableFund stocks = new ConfigurableFund(100, 1);
    ConfigurableFund mixed = new ConfigurableFund(100, 1.20);
    ConfigurableFund etf2x = new ConfigurableFund(100, 2);
    ConfigurableFund etf3x = new ConfigurableFund(100, 3);

    Date previous = null;

    for (OneDayReturn r : dailyReturns) {
      double returns = r.getReturns();

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

      stocks.updateValue(returns, interest, elapsed); 
      mixed.updateValue(returns, interest, elapsed);
      etf2x.updateValue(returns, interest, elapsed);
      etf3x.updateValue(returns, interest, elapsed);

      previous = d;
    }

    System.out.println("Last day: " + previous.toString());
    System.out.println("Total stock returns over this time period: " + stocks.getValue() + "%");
    System.out.println("Total mixed returns over this time period: " + mixed.getValue() + "%");
    System.out.println("Total 2XETF returns over this time preiod: " + etf2x.getValue() + "%");
    System.out.println("Total 3XETF returns over this time preiod: " + etf3x.getValue() + "%");
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

      double todaysClose = Double.parseDouble(rawArr.get(i)[PRICE_COL]);
      double yesterdaysClose = Double.parseDouble(rawArr.get(i + 1)[PRICE_COL]);
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

class ConfigurableFund {
  private final double leverage;
  private double value;

  public ConfigurableFund(double initial_value, double leverage) {
    this.value = initial_value;
    this.leverage = leverage;
  }

  public double getValue() {
    return value;
  }

  public void updateValue(double returns, double interest, int elapsed) {
    if (leverage > 1)
      value *= Math.pow(1 - interest * (leverage - 1), elapsed);
    value *= (returns * leverage - (leverage - 1));
  }
}

class MonthData {
  private static final GregorianCalendar gCalendar = new GregorianCalendar();

  private YearMonth month;
  private double tRate;
  private double dailyRate;

  private static final double ADDITIONAL_RATE = 0.005;

  public MonthData(YearMonth month, double tRate) {
    this.month = month;
    this.tRate = tRate;

    boolean leap = gCalendar.isLeapYear(month.getYear());
    float days = 365 + (leap ? 1 : 0);

    this.dailyRate = Math.pow(tRate + ADDITIONAL_RATE + 1, 1 / days) - 1;
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
