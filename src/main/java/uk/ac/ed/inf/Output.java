package uk.ac.ed.inf;

import com.mapbox.geojson.FeatureCollection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class Output {

    public static void createMap(FeatureCollection pathFeatures,String day,String month,String year) {
        try {
            File myObj = new File("C:\\Users\\sarah\\Documents\\2021FirstSemester\\ilp\\drone-" + day + "-" + month + "-" + year + ".geojson");
            Files.deleteIfExists(myObj.toPath());
            if (myObj.createNewFile()){
                FileWriter myWriter = new FileWriter("C:\\Users\\sarah\\Documents\\2021FirstSemester\\ilp\\drone-" + day + "-" + month + "-" + year + ".geojson");
                myWriter.write(pathFeatures.toJson());
                myWriter.close();
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
