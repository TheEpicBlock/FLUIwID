package nl.theepicblock.fluiwid;

public class RollingAverage {
    double[] averages;
    int i = 0;

    public RollingAverage(int length) {
        this.averages = new double[length];
    }

    public double add(double n) {
        averages[i] = n;
        i++;
        i %= averages.length;
        double acc = 0;
        for (var f : averages) {
            acc += f;
        }
        return acc / averages.length;
    }

    public void setAll(double f) {
        for (int i = 0; i < averages.length; i++) {
            this.averages[i] = f;
        }
    }
}
