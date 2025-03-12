import java.util.Scanner;

public class SubarrayMeanCalculator {

    public static void main(String[] args) {
        SubarrayMeanCalculator solver = new SubarrayMeanCalculator();
        solver.run();
    }

    public void run() {
        // Read the input and initialize parameters
        Scanner scanner = new Scanner(System.in);
        int[] parameters = readInput(scanner);
        int n = parameters[0];
        int q = parameters[1];

        // Read the array of integers
        long[] arr = new long[n];
        for (int i = 0; i < n; i++) {
            arr[i] = scanner.nextLong();
        }

        // Build the prefix sum array for efficient range queries
        long[] prefixSumArray = buildPrefixSum(arr);

        // Process each query to compute and output the mean
        for (int i = 0; i < q; i++) {
            int L = scanner.nextInt();
            int R = scanner.nextInt();
            long result = computeMean(prefixSumArray, L, R);
            System.out.println(result);
        }

        scanner.close();
    }

    // Read and return the input parameters
    private int[] readInput(Scanner scanner) {
        int[] data = new int[2];
        data[0] = scanner.nextInt();
        data[1] = scanner.nextInt();
        return data;
    }

    // Build the prefix sum array for the given array
    private long[] buildPrefixSum(long[] arr) {
        long[] prefixSum = new long[arr.length + 1];
        prefixSum[0] = 0;
        for (int i = 0; i < arr.length; i++) {
            prefixSum[i + 1] = prefixSum[i] + arr[i];
        }
        return prefixSum;
    }

    // Compute the mean of the subarray from L to R
    private long computeMean(long[] prefixSum, int L, int R) {
        long sum = prefixSum[R] - prefixSum[L - 1];
        int count = R - L + 1;
        return sum / count;
    }
}
