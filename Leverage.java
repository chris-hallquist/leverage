import java.util.*;

public class Leverage {
  public static void main(String[] args) {
    long startTime = System.currentTimeMillis();

    int trials = Integer.parseInt(args[0]);

    Fund fund = new Fund();
    Daily2X daily2X = new Daily2X();
    Monthly2X monthly = new Monthly2X();

    double[] fundResults = new double[trials];
    double[] daily2XResults = new double[trials];
    double[] monthlyResults = new double[trials];

    for (int i = 0; i < trials; i++) {
      fundResults[i] = fund.getYearlyReturns();
      daily2XResults[i] = daily2X.getYearlyReturns();
      monthlyResults[i] = monthly.getYearlyReturns();
    }

    System.out.println("Fund mean: " + mean(fundResults));
    System.out.println("Daily2X mean: " + mean(daily2XResults));
    System.out.println("Monthly2X mean: " + mean(monthlyResults));

    System.out.println("Fund median: " + median(fundResults));
    System.out.println("Daily2X median: " + median(daily2XResults));
    System.out.println("Monthly2X median: " + median(monthlyResults));

    long endTime = System.currentTimeMillis();
    System.out.println("That took " + (endTime - startTime) + " milliseconds");
   }

  static double mean (double[] nums) {
    double sum = 0;
    for (int i = 0; i < nums.length; i++) {
      sum += nums[i];
    }
    return sum / nums.length;
  }

  static double median (double[] nums) {
    Arrays.sort(nums);
    return nums[nums.length / 2];
  }
}

class Fund {
  static final int YEAR = 252; // Trading days, not calendar days
  static final int MONTH = 21; // Simplifying assumption
  static final double STDV = 1.5;
  static final double INTEREST = 1.5 / 100; // 1.5% per year

  static final Random rand = new Random();

  double getDailyReturns() {
    return 1 + (rand.nextGaussian()*STDV + 0.02)/100.0;
  }

  double getMonthlyReturns() {
    double returns = 1.0;

    for (int i = 0; i < MONTH; i++) {
      returns *= getDailyReturns();
    }

    return returns;
  }

  double getYearlyReturns() {
    double returns = 1.0;

    for (int i = 0; i < YEAR; i++) {
      returns *= getDailyReturns();
    }  

    return returns;
  }
}

class Daily2X extends Fund {
  static final double DAILY_INTEREST = INTEREST / YEAR;

  @Override
  double getDailyReturns() {
    return 1 + (rand.nextGaussian()*STDV + 0.02)/50.0 - DAILY_INTEREST;
  }
}

class Monthly2X extends Fund {
  static final double MONTHLY_INTEREST = INTEREST / MONTH;

  @Override
  double getMonthlyReturns() {
    return 1 + (super.getMonthlyReturns() - 1) * 2 - MONTHLY_INTEREST;
  }

  @Override
  double getYearlyReturns() {
    double returns = 1.0;

    for (int i = 0; i < 12; i++) {
      returns *= getMonthlyReturns();
    }

    return returns;
  }
}
