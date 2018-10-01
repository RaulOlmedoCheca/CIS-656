package client;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import compute.Compute;

public class ComputePi {
    public static void main(String args[]) {
        int operation;
        int piDecimals;
        int minPrime;
        int maxPrime;

        System.out.println("Select a task\n" +
                "1) Compute Pi\n" +
                "2) Compute Primes\n" +
                "3) Exit");
        Scanner sc = new Scanner(System.in);
        operation = sc.nextInt();

        if (operation == 1) {
            System.out.println("Introduce the number of decimals of pi to be computed");
            piDecimals = sc.nextInt();

            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            try {
                String name = "Compute";
                Registry registry = LocateRegistry.getRegistry(args[0]);
                Compute comp = (Compute) registry.lookup(name);
                Pi task = new Pi(piDecimals);
                BigDecimal pi = comp.executeTask(task);
                System.out.println(pi);

            } catch (Exception e) {
                System.err.println("ComputePi exception:");
                e.printStackTrace();
            }

        } else if (operation == 2) {
            System.out.println("Introduce the min prime to be calculated");
            minPrime = sc.nextInt();
            System.out.println("Introduce the max prime to be calculated");
            maxPrime = sc.nextInt();

            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            try {
                String name = "Compute";
                Registry registry = LocateRegistry.getRegistry(args[0]);
                Compute comp = (Compute) registry.lookup(name);
                Primes task = new Primes(minPrime, maxPrime);
                List<Integer> primes = comp.executeTask(task);

                for (int item : primes) {
                    System.out.print(item + " ");
                }

            } catch (Exception e) {
                System.err.println("ComputePrimes exception:");
                e.printStackTrace();
            }

        } else {
            System.out.println("Goodbye!");
        }

        sc.close();

    }
}