package com.example.iot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ECGDataGenerator {
    public static List<Double> generateECGData(int durationInSeconds, int samplingRate) {
        List<Double> ecgData = new ArrayList<>();
        Random random = new Random();

        // Generate ECG data for the given duration
        for (int t = 0; t < durationInSeconds * samplingRate; t++) {
            double time = (double) t / samplingRate;
            double ecgValue = generateECGValue(time, random);
            ecgData.add(ecgValue);
        }

        return ecgData;
    }

    // Function to generate ECG value at a given time
    private static double generateECGValue(double time, Random random) {
        // Example: Generate ECG value using a sine wave with added noise
        double noise = random.nextGaussian() * 0.1; // Add Gaussian noise with standard deviation 0.1
        double amplitude = 1.0; // Adjust amplitude as needed
        double frequency = 1.0; // Adjust frequency as needed
        double ecgValue = amplitude * Math.sin(2 * Math.PI * frequency * time) + noise;
        return ecgValue;
    }

    public static void main(String[] args) {
        int durationInSeconds = 10;
        int samplingRate = 1000; // Samples per second (Hz)
        List<Double> ecgData = generateECGData(durationInSeconds, samplingRate);
        // Print the generated ECG data
        System.out.println("ECG Data:");
        for (int i = 0; i < ecgData.size(); i++) {
            System.out.println(ecgData.get(i));
        }
    }
}
