package ua.dmytrolutsiuk.server;

import java.util.Random;

public class MatrixColumnFiller implements Runnable {
    private final int[][] matrix;
    private final int matrixSize;
    private final int threadsAmount;
    private final int threadId;
    private final Random random = new Random();

        public MatrixColumnFiller(int[][] matrix, int matrixSize, int threadsAmount, int threadId) {
        this.matrix = matrix;
        this.matrixSize = matrixSize;
        this.threadsAmount = threadsAmount;
        this.threadId = threadId;
    }

    @Override
    public void run() {
        for (int col = threadId; col < matrixSize; col += threadsAmount) {
            int max = Integer.MIN_VALUE;
            for (int row = 0; row < matrixSize; row++) {
                int val = random.nextInt(1000);
                matrix[row][col] = val;
                if (val > max) max = val;
            }
            matrix[col][col] = max;
        }
    }
}