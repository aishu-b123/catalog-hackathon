import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.math.BigInteger;
import java.util.*;

public class PolynomialDecoder {

    public static class Point {
        BigInteger x, y;

        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    // Decode value string from given base
    public static BigInteger decodeBase(String value, int base) {
        return new BigInteger(value, base);
    }

    // Lagrange Interpolation to calculate f(0)
    public static BigInteger lagrangeAtZero(List<Point> points) {
        int k = points.size();
        BigInteger resultNum = BigInteger.ZERO;
        BigInteger resultDen = BigInteger.ONE;

        for (int i = 0; i < k; i++) {
            BigInteger num = points.get(i).y;
            BigInteger den = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    num = num.multiply(points.get(j).x.negate());
                    den = den.multiply(points.get(i).x.subtract(points.get(j).x));
                }
            }
            // Add this term to the result (as a fraction)
            resultNum = resultNum.multiply(den).add(num.multiply(resultDen));
            resultDen = resultDen.multiply(den);
            // Reduce fraction
            BigInteger gcd = resultNum.gcd(resultDen);
            if (!gcd.equals(BigInteger.ZERO)) {
                resultNum = resultNum.divide(gcd);
                resultDen = resultDen.divide(gcd);
            }
        }
        // Final reduction
        BigInteger gcd = resultNum.gcd(resultDen);
        if (!gcd.equals(BigInteger.ZERO)) {
            resultNum = resultNum.divide(gcd);
            resultDen = resultDen.divide(gcd);
        }
        if (resultDen.signum() < 0) {
            resultDen = resultDen.negate();
            resultNum = resultNum.negate();
        }
        if (resultDen.equals(BigInteger.ONE)) {
            return resultNum;
        } else {
            System.out.println("Secret (c) = f(0) = " + resultNum + " / " + resultDen);
            return null;
        }
    }

    public static void main(String[] args) {
        String fileName = "test2.json";
        List<Point> allPoints = new ArrayList<>();
        int k = 0;

        try (FileReader reader = new FileReader(fileName)) {
            JSONObject data = new JSONObject(new JSONTokener(reader));

            k = data.getJSONObject("keys").getInt("k");

            for (String key : data.keySet()) {
                if (key.equals("keys"))
                    continue;

                BigInteger x = new BigInteger(key);
                JSONObject obj = data.getJSONObject(key);
                int base = Integer.parseInt(obj.getString("base"));
                String value = obj.getString("value");
                BigInteger y = decodeBase(value, base);
                allPoints.add(new Point(x, y));
            }

        } catch (Exception e) {
            System.out.println("Error reading JSON: " + e.getMessage());
            return;
        }

        if (allPoints.size() < k) {
            System.out.println("Not enough roots to determine the polynomial.");
            return;
        }

        List<Point> selected = allPoints.subList(0, k);

        System.out.println("Decoded Points (x, y):");
        for (Point p : selected) {
            System.out.println("(" + p.x + ", " + p.y + ")");
        }

        BigInteger secret = lagrangeAtZero(selected);
        if (secret != null) {
            System.out.println("Secret (c) = f(0) = " + secret);
        }

    }
}
