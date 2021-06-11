package com.example.BLEAPP2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
//31.05 deleted all x y z gyro stuff
/*
* This is a class for the accelerometer data captured from your BLE connection.
*
* */
public class GasData { // es existiert aber Android weiss noch nicht was es macht, modifizieren und anderer Name

    private ArrayList<Integer> current; //field  std::vector<int> xAcceleration, nur hier existiert er noch nicht
    private ArrayList<Integer> time; //hier wird ein Platz im Speicher reserviert
    private ArrayList<Integer> GasConcentration;
    /**private ArrayList<Date> readingTimes;
    private ArrayList<String> readingTimesFormatted;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");*/

    //Dieser Konstruktor sagt Java jetzt welche Werte AccelerometerData bei Erstellung hat (Startwerte) und was gemacht werden muss um eine neue Instanz zu bekommen
    public GasData(ArrayList<Integer> xCurrent, ArrayList<Integer> yTime, ArrayList<Integer> zGasConc, ArrayList<Date> times) {
        this.current = xCurrent; // Tue parameter xAcc in das field xAcceleration der Klasse AccelerometerData rein
        this.time = yTime;
        this.GasConcentration = zGasConc;
        //this.readingTimes = times;
        //formatReadTimes(times);
    }

    public ArrayList<Integer> getCurrent() {
        return this.current;
    } /** ab hier lässt uns das Programm Daten lesen und schreiben */
    public void setCurrent(ArrayList<Integer> xCurrent) {
        this.current = xCurrent;
    }

    public ArrayList<Integer> getTime() {
        return this.time;
    }
    public void setTime(ArrayList<Integer> yTime) {
        this.time = yTime;
    }

    public ArrayList<Integer> getGasConcentration() {
        return this.GasConcentration;
    }
    public void setGasConcentration(ArrayList<Integer> zGasConc) {
        this.GasConcentration = zGasConc;
    }

    public int getCurrentAvg() {
        return calculateAverage(this.current);

    }

    public int getTimeAvg() {
        return calculateAverage(this.time);
    }

    public int getGasConcentrationAvg() {
        return calculateAverage(this.GasConcentration);
    }


    /**public ArrayList<Date> getReadingTimes() {
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
    }*/

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
