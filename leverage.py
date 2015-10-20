from random import gauss
from sys import argv
from time import time

YEAR = 252 # Trading days, not calendar days
MONTH = 21 # Simplifying assumption
STDV = 1.5
INTEREST = 1.5 / 100 # 1.5% per year
DAILY_INTEREST = INTEREST / YEAR
MONTHLY_INTEREST = INTEREST / MONTH

def mean(nums):
  total = 0
  i = 0
  while i < len(nums):
    total += nums[i]
    i += 1
  return total / len(nums)

def median(nums):
  nums.sort()
  return nums[len(nums) / 2]

class Fund(object):
  def get_daily_returns(self):
    return 1 + gauss(0.02, STDV)/100.0

  def get_monthly_returns(self):
    returns = 1.0
    i = 0
    while i < MONTH:
      returns *= self.get_daily_returns()
      i += 1
    return returns

  def get_yearly_returns(self):
    returns = 1.0
    i = 0
    while i < YEAR:
      returns *= self.get_daily_returns()
      i += 1
    return returns

class Daily2X(Fund):
    def get_daily_returns(self):
      return 1 + gauss(0.02, STDV)/50.0 - DAILY_INTEREST

class Monthly2X(Fund):
    def get_monthly_returns(self):
      return 1 + (super(Monthly2X, self).get_monthly_returns() - 1) * 2 - MONTHLY_INTEREST

    def get_yearly_returns(self):
      returns = 1.0
      i = 0
      while i < 12:
        returns *= self.get_monthly_returns()
        i += 1
      return returns

start_time = int(round(time() * 1000))

trials = int(argv[1])

fund = Fund()
daily_2x = Daily2X()
monthly_2x = Monthly2X()

fund_results = []
daily_2x_results = []
monthly_2x_results = []

i = 0
while i < trials:
  fund_results.append(fund.get_yearly_returns())
  daily_2x_results.append(daily_2x.get_yearly_returns())
  monthly_2x_results.append(monthly_2x.get_yearly_returns())
  i += 1

print "Fund mean: " + str(mean(fund_results))
print "Daily2X mean: " + str(mean(daily_2x_results))
print "Monthly2X mean: " + str(mean(monthly_2x_results))

print "Fund median: " + str(median(fund_results))
print "Daily2X median: " + str(median(daily_2x_results))
print "Monthly2X median: " + str(median(monthly_2x_results))

end_time = int(round(time() * 1000))
print "That took " + str(end_time - start_time) + " milliseconds"
