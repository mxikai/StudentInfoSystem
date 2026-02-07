package StudentInfoSystem.src.model;

import java.io.*;
import java.util.*;

public class CsvHandler {

    //reader
    public static List<String[]> readCSV(String filePath) {
        List<String[]> data = new ArrayList<>();

        try {
            File myFile = new File(filePath);
            Scanner reader = new Scanner(myFile);
            
            //loop while there are lines to read
            while(reader.hasNextLine()) {
                String line = reader.nextLine(); 

                //splits lines by comma
                String[] row = line.split(",");
                data.add(row);
            }
            reader.close();

        } catch (Exception e) {
            System.out.println("Error reading file: " + filePath);
        }

        return data;
    }

    //adds new student
    public static void appendToCSV(String filePath, String textToSave) {
        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
            PrintWriter printer = new PrintWriter(fileWriter);

            printer.println(textToSave);
            
            printer.close();

        } catch (IOException e) {
            System.out.println("Error writing to file: " + filePath);
        }
    }

    //deletes and updates
    public static void overwriteCSV(String filePath, List<String> newLines) {
        try {
            FileWriter fileWriter = new FileWriter(filePath, false);
            PrintWriter printer = new PrintWriter(fileWriter);

            for (String line : newLines) {
                printer.println(line);
            }

            printer.close();

        } catch (IOException e) {
            System.out.println("Error overwriting file.");  
        }
    }
}
