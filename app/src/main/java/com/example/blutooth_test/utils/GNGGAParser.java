package com.example.blutooth_test.utils;

import android.util.Log;

public class GNGGAParser {

    private static final String TAG = "GNGGAParser";

    /**
     * 解析 $GNGGA 数据
     *
     * @param gnggaString $GNGGA 数据字符串
     * @return 解析后的 GNGGAData 对象
     * @throws IllegalArgumentException 如果数据格式无效
     */
    public static GNGGAData parse(String gnggaString) {
        // 检查数据是否以 $GNGGA 开头
        if (gnggaString == null || !gnggaString.startsWith("$GNGGA")) {
            throw new IllegalArgumentException("Invalid GNGGA data: data is null or does not start with $GNGGA");
        }

        // 分割字符串
        String[] fields = gnggaString.split(",");

        // 检查字段数量
        if (fields.length < 11) {
            throw new IllegalArgumentException("Invalid GNGGA data: insufficient fields");
        }

        try {
            // 提取关键字段
            String utcTime = fields[1]; // UTC 时间
            String latitude = fields[2]; // 纬度（DDmm.mm）
            String latDirection = fields[3]; // 纬度方向（N/S）
            String longitude = fields[4]; // 经度（DDDmm.mm）
            String lonDirection = fields[5]; // 经度方向（E/W）
            int gpsQuality = parseInt(fields[6], "GPS Quality"); // GPS 质量
            int satelliteCount = parseInt(fields[7], "Satellite Count"); // 卫星数
            double altitude = parseDouble(fields[9], "Altitude"); // 海拔
            String altitudeUnit = fields[10]; // 海拔单位

            // 转换纬度和经度
            double finalLatitude = convertToDecimal(latitude, latDirection, "Latitude");
            double finalLongitude = convertToDecimal(longitude, lonDirection, "Longitude");

            // 返回解析后的数据
            return new GNGGAData(utcTime, finalLatitude, finalLongitude, gpsQuality, satelliteCount, altitude, altitudeUnit);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse GNGGA data: " + gnggaString, e);
            throw new IllegalArgumentException("Failed to parse GNGGA data: " + e.getMessage(), e);
        }
    }

    /**
     * 将度分格式（DDmm.mm）转换为十进制
     *
     * @param value     度分格式的值
     * @param direction 方向（N/S/E/W）
     * @param fieldName 字段名称（用于错误提示）
     * @return 十进制值
     * @throws IllegalArgumentException 如果值无效
     */
    private static double convertToDecimal(String value, String direction, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is null or empty");
        }

        try {
            // 找到小数点前的部分（度+分）
            int dotIndex = value.indexOf('.');
            if (dotIndex < 2) {
                throw new IllegalArgumentException("Invalid " + fieldName + " format: " + value);
            }

            // 提取度（纬度：前2位；经度：前3位）
            int degrees;
            double minutes;
            if (fieldName.equals("Latitude")) {
                degrees = Integer.parseInt(value.substring(0, 2));
                minutes = Double.parseDouble(value.substring(2));
            } else { // Longitude
                degrees = Integer.parseInt(value.substring(0, 3));
                minutes = Double.parseDouble(value.substring(3));
            }

            double decimal = degrees + minutes / 60.0;

            // 根据方向调整符号
            if ("S".equals(direction) || "W".equals(direction)) {
                decimal = -decimal;
            }

            return decimal;
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + value, e);
        }
    }

    //private static double convertToDecimal(String value, String direction, String fieldName) {
    //    if (value == null || value.isEmpty()) {
    //        throw new IllegalArgumentException(fieldName + " is null or empty");
    //    }
    //
    //    try {
    //        double degrees = Double.parseDouble(value.substring(0, value.indexOf('.')));
    //        double minutes = Double.parseDouble(value.substring(value.indexOf('.')));
    //        double decimal = degrees + minutes / 60;
    //
    //        // 根据方向调整符号
    //        if ("S".equals(direction) || "W".equals(direction)) {
    //            decimal = -decimal;
    //        }
    //
    //        return decimal;
    //    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
    //        throw new IllegalArgumentException("Invalid " + fieldName + " format: " + value, e);
    //    }
    //}


    /**
     * 解析整数
     *
     * @param value     字符串值
     * @param fieldName 字段名称（用于错误提示）
     * @return 解析后的整数
     * @throws IllegalArgumentException 如果值无效
     */
    private static int parseInt(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is null or empty");
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + value, e);
        }
    }

    /**
     * 解析浮点数
     *
     * @param value     字符串值
     * @param fieldName 字段名称（用于错误提示）
     * @return 解析后的浮点数
     * @throws IllegalArgumentException 如果值无效
     */
    private static double parseDouble(String value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is null or empty");
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + value, e);
        }
    }

    /**
     * GNGGA 数据结构
     */
    public static class GNGGAData {
        private final String utcTime; // UTC 时间
        private final double latitude; // 纬度（十进制）
        private final double longitude; // 经度（十进制）
        private final int gpsQuality; // GPS 质量
        private final int satelliteCount; // 卫星数
        private final double altitude; // 海拔
        private final String altitudeUnit; // 海拔单位

        public GNGGAData(String utcTime, double latitude, double longitude, int gpsQuality, int satelliteCount, double altitude, String altitudeUnit) {
            this.utcTime = utcTime;
            this.latitude = latitude;
            this.longitude = longitude;
            this.gpsQuality = gpsQuality;
            this.satelliteCount = satelliteCount;
            this.altitude = altitude;
            this.altitudeUnit = altitudeUnit;
        }

        public String getUtcTime() {
            return utcTime;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public int getGpsQuality() {
            return gpsQuality;
        }

        public int getSatelliteCount() {
            return satelliteCount;
        }

        public double getAltitude() {
            return altitude;
        }

        public String getAltitudeUnit() {
            return altitudeUnit;
        }

        @Override
        public String toString() {
            return String.format(
                    "UTC Time: %s, Latitude: %.6f, Longitude: %.6f, GPS Quality: %d, Satellite Count: %d, Altitude: %.2f %s",
                    utcTime, latitude, longitude, gpsQuality, satelliteCount, altitude, altitudeUnit
            );
        }
    }
}