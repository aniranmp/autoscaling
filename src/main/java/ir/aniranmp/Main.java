package ir.aniranmp;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.Scanner;
import java.util.Random;


public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Please enter t: ");
        int timeSlots = scan.nextInt();

        // Create an instance of the Random class
        Random rand = new Random();

        // Create an instance of the RandomDataGenerator class
        RandomDataGenerator randomData = new RandomDataGenerator();

        int countRejected = 0;
        int countRequest = 0;
        // Set the mean (Lambda) of the Poisson distribution
        System.out.println("Please enter value of lambda: ");
        double lambda = scan.nextDouble();
        // Generate a random number from a Poisson distribution - +1 is for not getting 0 and 1
        int minute = 60;

        for (int i = 0; i < timeSlots; i++) {
            long k = randomData.nextPoisson(lambda) + 2;
            Request request[] = new Request[(int) k];
            for (int l = 0; l < k; l++) {
                request[l].id = countRequest++;
                request[l].creationTime = rand.nextInt(60);
                request[l].executionTime = rand.nextInt(4);

            }
            for (int j = 0; j < minute; j++) {

            }
        }

    }
}
