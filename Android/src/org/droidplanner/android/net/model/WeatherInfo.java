package org.droidplanner.android.net.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by macbook on 23.04.16.
 */
public class WeatherInfo {
    private Weather weather;
    private boolean permission;

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public boolean getPermission() {
        return permission;
    }

    public void setPermission(Boolean permission) {
        this.permission = permission;
    }

    public class Weather {
        @SerializedName("t")
        private double temperature;
        @SerializedName("p")
        private double pressure;
        @SerializedName("h")
        private double humidity;
        @SerializedName("lat")
        private double latitude;
        @SerializedName("lng")
        private double longitude;
        @SerializedName("s")
        private double snow;
        @SerializedName("r")
        private double rain;
        @SerializedName("ws")
        private double windSpeed;
        @SerializedName("wd")
        private double windDirection;

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public double getPressure() {
            return pressure;
        }

        public void setPressure(double pressure) {
            this.pressure = pressure;
        }

        public double getHumidity() {
            return humidity;
        }

        public void setHumidity(double humidity) {
            this.humidity = humidity;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getSnow() {
            return snow;
        }

        public void setSnow(double snow) {
            this.snow = snow;
        }

        public double getRain() {
            return rain;
        }

        public void setRain(double rain) {
            this.rain = rain;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(double windSpeed) {
            this.windSpeed = windSpeed;
        }

        public double getWindDirection() {
            return windDirection;
        }

        public void setWindDirection(double windDirection) {
            this.windDirection = windDirection;
        }
    }
}
