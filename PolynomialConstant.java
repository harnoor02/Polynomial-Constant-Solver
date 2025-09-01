import java.io.IOException;
import java.nio.file.*;
import java.math.BigInteger;
import java.util.*;

public class PolynomialConstant {

    // Read file into String
    private static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    // Decode string in base N → BigInteger
    private static BigInteger decodeFromBase(String number, int base) {
        return new BigInteger(number, base);
    }

    // Extract integer (without quotes) from "keys"
    private static int extractInt(String json, String subKey) {
        String subPattern = "\"" + subKey + "\"";
        int subIdx = json.indexOf(subPattern);
        int colonIdx = json.indexOf(":", subIdx);
        StringBuilder num = new StringBuilder();
        for (int i = colonIdx + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c)) {
                num.append(c);
            } else if (num.length() > 0) {
                break;
            }
        }
        return Integer.parseInt(num.toString());
    }

    // Extract value between quotes after a given key
    private static String extractValue(String json, String key, String subKey) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;

        // find subKey after this
        String subPattern = "\"" + subKey + "\"";
        int subIdx = json.indexOf(subPattern, idx);
        if (subIdx == -1) return null;

        int colonIdx = json.indexOf(":", subIdx);
        int quote1 = json.indexOf("\"", colonIdx);
        int quote2 = json.indexOf("\"", quote1 + 1);

        return json.substring(quote1 + 1, quote2);
    }

    // Perform Lagrange interpolation at x=0
    private static BigInteger lagrangeAtZero(List<BigInteger> xs, List<BigInteger> ys) {
        BigInteger result = BigInteger.ZERO;
        int k = xs.size();

        for (int j = 0; j < k; j++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int m = 0; m < k; m++) {
                if (m != j) {
                    numerator = numerator.multiply(xs.get(m).negate());   // (0 - x_m)
                    denominator = denominator.multiply(xs.get(j).subtract(xs.get(m)));
                }
            }
            // y_j * (numerator/denominator)
            BigInteger term = ys.get(j).multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }

    // Process one JSON file
    private static void processFile(String fileName) throws Exception {
        String json = readFile(fileName);

        int n = extractInt(json, "n");
        int k = extractInt(json, "k");
        int m = k - 1;

        System.out.println("\n=== Processing file: " + fileName + " ===");
        System.out.println("n = " + n + ", k = " + k + ", degree m = " + m);

        List<BigInteger> xs = new ArrayList<>();
        List<BigInteger> ys = new ArrayList<>();

        // take first k points
        for (int i = 1; i <= k; i++) {
            String base = extractValue(json, String.valueOf(i), "base");
            String value = extractValue(json, String.valueOf(i), "value");

            if (base == null || value == null) continue;

            BigInteger y = decodeFromBase(value, Integer.parseInt(base));
            BigInteger x = BigInteger.valueOf(i);

            xs.add(x);
            ys.add(y);

            System.out.println("Point (" + x + "," + y + ")");
        }

        BigInteger constant = lagrangeAtZero(xs, ys);
        System.out.println("Constant term c = " + constant);
    }

    public static void main(String[] args) {
        try {
            processFile("TC.json");   // First test case
            processFile("TC2.json");  // Second test case
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
