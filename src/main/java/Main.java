import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import models.Course;
import models.GoogleSheet;

public class Main {
  /**
   * Prints the names and majors of students in a sample spreadsheet:
   * https://docs.google.com/spreadsheets/d/1ZArW4QuRbLBcfaeQD56OTcaWXRA3yqQ3PbEOc0vkgiI/edit#gid=2142564393
   */
  public static void main(String... args) throws IOException, GeneralSecurityException {
    List<List<Object>> values = GoogleSheet.getValues();
    Map<String, List<Course>> formattedValues = GoogleSheet.formatValues(values);
    GoogleSheet.outputFormattedValues(formattedValues);
  }
}