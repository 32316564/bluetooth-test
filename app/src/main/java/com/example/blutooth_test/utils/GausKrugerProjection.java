package com.example.blutooth_test.utils;

public class GausKrugerProjection {
    private static final double a = 6378137.0;         // WGS84椭球长半轴
    private static final double f = 1.0 / 298.257223563; // WGS84扁率

    public static class Point {
        public double X;
        public double Y;
    }

    public static Point longLatToXY(double longitude, double latitude) {
        Point result = new Point();
        int zoneWide = 3; // 3度带宽

        // 计算带号(3度带)
        int projNo = (int)(longitude / zoneWide + 0.5);
        // 中央子午线经度(弧度)
        double longitude0 = Math.toRadians(projNo * zoneWide);

        // 将经纬度转换为弧度
        double longitude1 = Math.toRadians(longitude);
        double latitude1 = Math.toRadians(latitude);

        // 计算辅助参数
        double e2 = 2 * f - f * f;
        double ee = e2 * (1.0 - e2);
        double NN = a / Math.sqrt(1.0 - e2 * Math.sin(latitude1) * Math.sin(latitude1));
        double T = Math.tan(latitude1) * Math.tan(latitude1);
        double C = ee * Math.cos(latitude1) * Math.cos(latitude1);
        double A = (longitude1 - longitude0) * Math.cos(latitude1);

        // 计算子午线弧长M
        double M = a * ((1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256) * latitude1
                - (3 * e2 / 8 + 3 * e2 * e2 / 32 + 45 * e2 * e2 * e2 / 1024) * Math.sin(2 * latitude1)
                + (15 * e2 * e2 / 256 + 45 * e2 * e2 * e2 / 1024) * Math.sin(4 * latitude1)
                - (35 * e2 * e2 * e2 / 3072) * Math.sin(6 * latitude1));

        // 计算平面坐标
        double xval = NN * (A + (1 - T + C) * A * A * A / 6
                + (5 - 18 * T + T * T + 72 * C - 58 * ee) * A * A * A * A * A / 120);
        double yval = M + NN * Math.tan(latitude1) * (A * A / 2
                + (5 - T + 9 * C + 4 * C * C) * A * A * A * A / 24
                + (61 - 58 * T + T * T + 600 * C - 330 * ee) * A * A * A * A * A * A / 720);

        // 加上带号和偏移量
        double X0 = 1000000L * projNo + 500000L; // 3度带
        double Y0 = 0;

        result.X = xval + X0;
        result.Y = yval + Y0;

        return result;
    }
}