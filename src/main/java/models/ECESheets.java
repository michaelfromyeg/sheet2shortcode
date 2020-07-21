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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ECESheets {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    public static Map<String, List<Course>> formatValues(List<List<Object>> values) {
        
        // Initialize lists to be grouped into tabs
        Map<String, List<Course>> courses = new HashMap<>();
        courses.put("ELEC", new ArrayList<>());
        courses.put("CPEN", new ArrayList<>());
        courses.put("GRAD", new ArrayList<>());
        courses.put("OTHER", new ArrayList<>());

        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List<Object> row : values) {
                if (row.size() > 4) {
                    Course newCourse = rowToCourse(row);
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

    private static Course rowToCourse(List<Object> row) {
        String rawCourseValue = String.valueOf(row.get(0));
        String courseName = rawCourseValue.substring(0,4);
        int courseNumber;
        try {
            courseNumber = Integer.parseInt(rawCourseValue, 4, 7, 10);
        } catch (NumberFormatException e) { // Capstone
            courseNumber = -1;
        }

        String profName = formatString(String.valueOf(row.get(1)));
        boolean hasResponded = Boolean.valueOf(String.valueOf(row.get(2)));
        String courseChanges = formatString(String.valueOf(row.get(3)));
        String profEmail = formatString(String.valueOf(row.get(4)));

        return new Course(courseName, courseNumber, profName, profEmail, hasResponded, courseChanges);
    }

    private static String formatString(String s) {
        return s.replace("\n", "").replace("\r", "");
    }

    public static List<List<Object>> getValues() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1ZArW4QuRbLBcfaeQD56OTcaWXRA3yqQ3PbEOc0vkgiI";
        final String range = "2021!A4:E";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        return values;
    }

    public static void outputFormattedValues(Map<String, List<Course>> values) {
        try {
            FileWriter w = new FileWriter("./data/output.txt");
            w.write("[tabs]");
            
            for (Map.Entry<String, List<Course>> value : values.entrySet()) {
                String tabWithTitle = String.format("[tab title=%s]", value.getKey());
                w.write(tabWithTitle);
                w.write(getInnerContent(value.getValue()));
                w.write("[/tab]");
            }

            w.write("[/tabs]");
            w.close();
          } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getInnerContent(List<Course> courses) {
        String result = "";
        for (Course c : courses) {
            if (c.hasChanges() && c.hasCourseNumber()) {
                result += c.toOutput();
                result += "<br />\n";
            }
        }
        return result;
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = ECESheets.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}