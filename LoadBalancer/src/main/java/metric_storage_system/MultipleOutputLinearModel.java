package metric_storage_system;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import java.util.ArrayList;
import java.util.List;

public class MultipleOutputLinearModel {
    private LinearModel[] models;

    public MultipleOutputLinearModel(int num_of_outputs) {
        models = new LinearModel[num_of_outputs];
        for (int i = 0; i < models.length; i++) {
            models[i] = new LinearModel();
        }
    }

    // Adds xData and yData to the model and refits it
    public void refit(double[][] xData, double[][] yData) {
        for (int i = 0; i < models.length; i++) {
            models[i].refit(xData, yData[i]);
        }
    }

    public void refit(double[] xData, double[] yData) {
        for (int i = 0; i < models.length; i++) {
            models[i].refit(new double[][]{xData}, new double[]{yData[i]});
        }
    }

    public double[] predict(double[] xData) {
        double[] predictions = new double[models.length];
        for (int i = 0; i < models.length; i++) {
            predictions[i] = models[i].predict(xData);
        }
        return predictions;
    }

    // Return underlying model coefficients
    public double[][] getCoefficients() {
        double[][] coefficients = new double[models.length][];
        for (int i = 0; i < models.length; i++) {
            coefficients[i] = models[i].getCoefficients();
        }
        return coefficients;
    }
}
