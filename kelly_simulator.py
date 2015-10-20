from math import log
from random import gauss

def binary_search(funct, lower, upper, precision):
    if upper - lower < precision:
        return lower
    
    middle = (lower + upper) / 2.0

    if funct(lower) > funct(upper):
        return binary_search(funct, lower, middle, precision)
    else:
        return binary_search(funct, middle, upper, precision)

def exhaustive_search(funct, lower, upper, precision):
    current = lower
    best = lower
    best_result = funct(lower)
    current += precision
    while current <= upper:
        current_result = funct(current)
        if current_result > best_result:
            best_result = current_result
            best = current
        current += precision
    return best

def make_simulator(mu, sigma, interest, trials):
    def simulator(leverage):
        tot = 0
        for i in range(trials):
            portfolio_return = gauss(mu, sigma)

            if leverage <= 1:
                leveraged_return = portfolio_return * leverage
            else:
                leveraged_return = portfolio_return + \
                        (portfolio_return - interest) * (leverage - 1)

            if leveraged_return <= -1:
                tot += log(0.1)
            else:
                tot += log(leveraged_return + 1)

        return tot / trials

    return simulator

print("This tool is for informational purposes only. For more information, visit https://github.com/chris-hallquist/kelly_simulator")
print("Answer the following questions in decimal form, e.g. for 6%, type 0.06")

mu = float(input("1. What return, in excess of the risk-free rate of return, do you expect to get on your portfolio?\n"))
sigma = float(input("2. What is the standard deviation of the return you expect to get on your portfolio?\n"))
interest = float(input("3. What interest does your broker charge on margin, above the risk free rate of return? Enter 0 if you can borrow at the risk-free rate of return.\n"))

print("Calculating. This may take awhile...")

simulator = make_simulator(mu, sigma, interest, 1000000)
ideal_leverage = exhaustive_search(simulator, 0, 2.5, 0.1)

print("According to our simulations, you should use a leverage ratio of {0:.2f}.".format(ideal_leverage))
