package models;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GoogleSheet {
  private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = Collections.singletonList(
      SheetsScopes.SPREADSHEETS_READONLY);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  /**
   * Formats values retrieved from Google Sheet in Map.
   * Organized by: ELEC, CPEN, GRAD, OTHER.
   */
  public static Map<String, List<Course>> formatValues(final List<List<Object>> values) {
      
    // Initialize lists to be grouped into tabs
    final Map<String, List<Course>> courses = new TreeMap<>();
    courses.put("ELEC", new ArrayList<>());
    courses.put("CPEN", new ArrayList<>());
    courses.put("GRAD", new ArrayList<>());
    courses.put("OTHER", new ArrayList<>());

    if (values == null || values.isEmpty()) {
      System.out.println("No data found.");
    } else {
      for (final List<Object> row : values) {
        if (row.size() > 4) {
          final Course newCourse = rowToCourse(row);
          switch (newCourse.getCourseName()) {
            case "ELEC":
              if (newCourse.isGrad()) {
                courses.get("GRAD").add(newCourse);
              } else {
                courses.get("ELEC").add(newCourse);
              }
              break;
            case "CPEN":
              if (newCourse.isGrad()) {
                courses.get("GRAD").add(newCourse);
              } else {
                courses.get("CPEN").add(newCourse);
              }
              break;
            case "EECE":
              courses.get("GRAD").add(newCourse);
              break;
            default:
              courses.get("OTHER").add(newCourse);
              break;
          }
        }
      }
    }
    return courses;
  }

  /**
   * Converts a given row into a course object.
   * See the Course class for more information on expected fields.
   */
  private static Course rowToCourse(final List<Object> row) {
    final String rawCourseValue = String.valueOf(row.get(0));
    final String courseName = rawCourseValue.substring(0, 4);
    int courseNumber;
    try {
      courseNumber = Integer.parseInt(rawCourseValue, 4, 7, 10);
    } catch (final NumberFormatException e) { // Capstone
      courseNumber = -1;
    }

    final String profName = normalizeProfName(formatString(String.valueOf(row.get(1))));
    final boolean hasResponded = Boolean.valueOf(String.valueOf(row.get(2)));
    final String courseChanges = formatString(String.valueOf(row.get(3)));
    final String profEmail = formatString(String.valueOf(row.get(4)));

    return new Course(courseName, courseNumber, profName, profEmail, hasResponded, courseChanges);
  }

  /**
   * Replaces all newline characters with nothing.
   * This helps with formatting in the output file.
   */
  private static String formatString(final String s) {
    return s.replace("\n", "").replace("\r", "");
  }

  /**
   * Reformat professor name strings as Firstname Lastname.
   * @param s the original professor name
   * @return a string in the form Firstname LastName
   */
  private static String normalizeProfName(String s) {
    if (!s.contains(",")) {
      return s;
    }
    
    String[] name = s.split(",");
    String formattedName = name[1] + " " + name[0];
    return formattedName.trim();
  }

  /**
   * Retrieves a set of values from the Google Sheet
   * This helps with formatting in the output file.
   */
  public static List<List<Object>> getValues() throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    final NetHttpTransport httpTransport = 
        GoogleNetHttpTransport.newTrustedTransport();
    final String spreadsheetId = "1ZArW4QuRbLBcfaeQD56OTcaWXRA3yqQ3PbEOc0vkgiI";
    final String range = "2021!A4:E";
    final Sheets service = new Sheets.Builder(httpTransport, JSON_FACTORY, 
        getCredentials(httpTransport))
        .setApplicationName(APPLICATION_NAME)
        .build();
    final ValueRange response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute();
    final List<List<Object>> values = response.getValues();
    return values;
  }

  /**
   * Writes to a text file the formatted shortcode.
   * @param values a map of key values pairs of a department, and a list of courses
   */
  public static void outputFormattedValues(final Map<String, List<Course>> values) {
    try {
      final Writer w = new OutputStreamWriter(
          new FileOutputStream("./data/output.txt"), StandardCharsets.UTF_8);
      w.write("[tabs]");
      for (final Map.Entry<String, List<Course>> value : values.entrySet()) {
        final String tabWithTitle = String.format("[tab title=%s]", value.getKey());
        w.write(tabWithTitle);
        w.write(getInnerContent(value.getValue()));
        w.write("[/tab]");
      }
      w.write("[/tabs]");
      w.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private static String getInnerContent(final List<Course> courses) {
    String result = "";
    for (final Course c : courses) {
      if (c.hasChanges() && c.hasCourseNumber()) {
        result += c.toOutput();
        result += "<p></p>\n";
      }
    }
    return result;
  }

  /**
   * Creates an authorized Credential object.
   * @param httpTransport The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  public static Credential getCredentials(
      final NetHttpTransport httpTransport) throws IOException {
    // Load client secrets.
    final InputStream in = GoogleSheet.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
        JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
    final LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }
}