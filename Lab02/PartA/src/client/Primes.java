package client;

import compute.Task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Primes implements Task<List<Integer>>, Serializable {
    private final int min;
    private final int max;

    /**
     * Construct a task to calculate the primes
     * between the two specified numbers.
     */
    public Primes(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Calculate primes.
     */
    @Override
    public List<Integer> execute() {
        return computePrime(min, max);
    }

    /**
     *
     */
    private List<Integer> computePrime(int min, int max) {
        List<Integer> primes = new ArrayList<Integer>();
        for (int i = 0; i < max; i++) {
            if (isPrime(min + i)) {
                primes.add(min + i);
            }
        }
        return primes;
    }

    private boolean isPrime(int possiblePrime) {
        if (possiblePrime > 2 && possiblePrime % 2 == 0) {
            return false;
        }
        int topBoundary = (int) Math.sqrt(possiblePrime) + 1;
        for (int i = 3; i < topBoundary; i += 2) {
            if (possiblePrime % i == 0) {
                return false;
            }
        }
        return true;
    }
}
