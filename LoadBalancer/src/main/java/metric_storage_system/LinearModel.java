package metric_storage_system;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import java.util.ArrayList;
import java.util.List;

public class LinearModel {
    private List<double[]> xData;
    private List<Double> yData;
    private double[] coefficients;

    public LinearModel() {
        this.xData = new ArrayList<>();
        this.yData = new ArrayList<>();
    }

    // Adds xData and yData to the model and refits it
    public void refit(double[][] xData, double[] yData) {
        for (double[] x : xData) {
            this.xData.add(x);
        }
        for (double y : yData) {
            this.yData.add(y);
        }
        fit();
    }

    // Fits the model based on xData and yData
    private void fit() {
        int n = xData.size();
        int m = xData.get(0).length;

        double[][] xDataArray = new double[n][m];
        double[] yDataArray = new double[n];

        for (int i = 0; i < n; i++) {
            xDataArray[i] = xData.get(i);
            yDataArray[i] = yData.get(i);
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.setNoIntercept(false);
        try {
            regression.newSampleData(yDataArray, xDataArray);

            this.coefficients = regression.estimateRegressionParameters();
        } catch (Exception e) {
            System.out.println("Error in newSampleData: " + e.getMessage() + ". not updating coefficients.");
        }
    }

    public double predict(double[] xData) {
        if (coefficients == null) {
            // throw new IllegalStateException("Model has not been trained yet.");
            return 0; // TODO: This is a temporary fix. Should throw an exception.
        }

        double prediction = coefficients[0];
        for (int i = 0; i < xData.length; i++) {
            prediction += coefficients[i + 1] * xData[i];
        }

        return prediction;
    }

    public double[] getCoefficients() {
        return coefficients;
    }
}
