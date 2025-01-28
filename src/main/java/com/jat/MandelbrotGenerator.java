package com.jat;


import java.util.stream.IntStream;

public class MandelbrotGenerator {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;

    // Method to generate the Mandelbrot set

    private int mandelbrotIterations(double real, double imaginary, int maxIterations) {
        double zReal = 0;
        double zImaginary = 0;
        int iterations = 0;

        while (zReal * zReal + zImaginary * zImaginary <= 4 && iterations < maxIterations) {
            double temp = zReal * zReal - zImaginary * zImaginary + real;
            zImaginary = 2 * zReal * zImaginary + imaginary;
            zReal = temp;
            iterations++;
        }

        return iterations;
    }

    public int[][] generateMandelbrotSet(double minX, double maxX, double minY, double maxY, int maxIterations) {
        int[][] mandelbrotSet = new int[WIDTH][HEIGHT];

        IntStream.range(0, WIDTH).parallel().forEach(x -> {
            for (int y = 0; y < HEIGHT; y++) {
                double real = minX + x * (maxX - minX) / (WIDTH - 1);
                double imaginary = minY + y * (maxY - minY) / (HEIGHT - 1);
                mandelbrotSet[x][y] = mandelbrotIterations(real, imaginary, maxIterations);
            }
        });

        return mandelbrotSet;
    }


}
