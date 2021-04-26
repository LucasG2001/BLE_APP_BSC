package com.example.BLEAPP2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
* This is a class for the accelerometer data captured from your BLE connection.
*
* */
public class GasData { // es existiert aber Android weiss noch nicht was es macht, modifizieren und anderer Name

    private ArrayList<Integer> current; //field  std::vector<int> xAcceleration, nur hier existiert er noch nicht
    private ArrayList<Integer> time; //hier wird ein Platz im Speicher reserviert
    private ArrayList<Integer> LED;
    private ArrayList<Integer> xGyroscope;
    private ArrayList<Integer> yGyroscope;
    private ArrayList<Integer> zGyroscope;
    private ArrayList<Date> readingTimes;
    private ArrayList<String> readingTimesFormatted;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    //Dieser Konstruktor sagt Java jetzt welche Werte AccelerometerData bei Erstellung hat (Startwerte) und was gemacht werden muss um eine neue Instanz zu bekommen
    public GasData(ArrayList<Integer> xAcc, ArrayList<Integer> yAcc, ArrayList<Integer> zAcc, ArrayList<Integer> xGyro, ArrayList<Integer> yGyro, ArrayList<Integer> zGyro, ArrayList<Date> times) {
        this.current = xAcc; // Tue parameter xAcc in das field xAcceleration der Klasse AccelerometerData rein
        this.time = yAcc;
        this.LED = zAcc;
        this.xGyroscope = xGyro;
        this.yGyroscope = yGyro;
        this.zGyroscope = zGyro;
        this.readingTimes = times;
        formatReadTimes(times);
    }

    public ArrayList<Integer> getXAcceleration() {
        return this.current;
    } /** ab hier lässt uns das Programm Daten lesen und schreiben */
    public void setCurrent(ArrayList<Integer> xAcc) {
        this.current = xAcc;
    }

    public ArrayList<Integer> getYAcceleration() {
        return this.time;
    }
    public void setTime(ArrayList<Integer> yAcc) {
        this.time = yAcc;
    }

    public ArrayList<Integer> getZAcceleration() {
        return this.LED;
    }
    public void setLED(ArrayList<Integer> zAcc) {
        this.LED = zAcc;
    }

    public int getCurrentAvg() {
        return calculateAverage(this.current);

    }

    public int getYAccelerationAvg() {
        return calculateAverage(this.time);
    }

    public int getLEDAvg() {
        return calculateAverage(this.LED);
    }

    public ArrayList<Integer> getXGyroscope() {
        return this.xGyroscope;
    }
    public void setXGyroscope(ArrayList<Integer> xGyro) {
        this.xGyroscope = xGyro;
    }

    public ArrayList<Integer> getYGyroscope() {
        return this.yGyroscope;
    }
    public void setYGyroscope(ArrayList<Integer> yGyro) {
        this.yGyroscope = yGyro;
    }

    public ArrayList<Integer> getZGyroscope() {
        return this.zGyroscope;
    }
    public void setZGyroscope(ArrayList<Integer> zGyro) {
        this.zGyroscope = zGyro;
    }

    public ArrayList<Date> getReadingTimes() {
        return this.readingTimes;
    }
    public void setReadingTimes(ArrayList<Date> time) {
        this.readingTimes = time;
    }

    private void formatReadTimes(ArrayList<Date> times) {

        readingTimesFormatted = new ArrayList<String>();
        //for (int i=0; i < 5; i++) {
            //readingTimesFormatted.add(dateFormatter.format(readingTimes.get(i)));
        //}
        for (int i=0; i < 5; i++) {
            readingTimesFormatted.add("a");
        }
    }

    private int calculateAverage(ArrayList<Integer> data) {
        int sum = 0;

       /** if(!data.isEmpty()) {
            for (int num : data) {
                sum += num;
            }
            return sum / data.size();
        } */
        if(!data.isEmpty()) {
            return data.get(0);

        }else {
            return 1;
        }
            //System.out.println(sum); //19.04.2021
        //return sum;
    }
}
