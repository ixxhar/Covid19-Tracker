package com.ixxhar.covid19tracker.helperclass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVFileWriter {

    private PrintWriter csvWriter;
    private File file;

    public CSVFileWriter(File file) {
        this.file = file;

    }

    public void generateHeader() {

        try {
            csvWriter = new PrintWriter(new FileWriter(file, true));
            csvWriter.print("id");
            csvWriter.print(",");
            csvWriter.print("nearByDevice");
            csvWriter.print(",");
            csvWriter.print("discoveredAt");
            csvWriter.print("\n");
            csvWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeDataCSV(String id, String nearByDevice, String discoveredAt) {

        try {
            if (id != null && nearByDevice != null && discoveredAt != null) {

                csvWriter = new PrintWriter(new FileWriter(file, true));
                csvWriter.print(id);
                csvWriter.print(",");
                csvWriter.print(nearByDevice);
                csvWriter.print(",");
                csvWriter.print(discoveredAt);
                csvWriter.print("\n");
                csvWriter.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}