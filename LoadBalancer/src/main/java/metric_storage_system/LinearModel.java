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
        regression.newSampleData(yDataArray, xDataArray);

        this.coefficients = regression.estimateRegressionParameters();
    }

    public void addDataPoint(double[] xData, double yData) {
        this.xData.add(xData);
        this.yData.add(yData);
        refitModel();
    }

    public double predict(double[] xData) {
        if (coefficients == null) {
            throw new IllegalStateException("Model has not been trained yet.");
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
