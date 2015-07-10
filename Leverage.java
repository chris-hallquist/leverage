import java.util.*;

public class Leverage {
  public static void main(String[] args) {
    int trials = Integer.parseInt(args[0]);

    Fund fund = new Fund();
    Daily2X daily2X = new Daily2X();

    double[] fundResults = new double[trials];
    double[] daily2XResults = new double[trials];

    double total = 0;
    for (int i = 0; i < trials; i++) {
      fundResults[i] = fund.getYearlyReturns();
    }

    double daily2XTotal = 0;
    for (int j = 0; j < trials; j++) {
      daily2XResults[j] = daily2X.getYearlyReturns();
    }

    System.out.println("Fund mean: " + mean(fundResults));
    System.out.println("Daily2X mean: " + mean(daily2XResults));

    System.out.println("Fund median: " + median(fundResults));
    System.out.println("Daily2X median: " + median(daily2XResults));
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

  static final Random rand = new Random();

  double getDailyReturns() {
    return 1 + (rand.nextGaussian() + 0.02)/100.0;
  }

  double getMontlyReturns() {
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
  @Override
  double getDailyReturns() {
    return 1 + (rand.nextGaussian() + 0.02)/50.0;
  }
}
