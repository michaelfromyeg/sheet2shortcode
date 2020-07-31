package models; 

public class Course {
  private String courseName;
  private int courseNumber;
  private String courseOption; // a letter, A-Z that reflects a course variant, ex., CPEN400'D'
  private boolean isGrad;

  private String profName;
  private String profEmail;

  private boolean hasResponded;
  private String courseChanges;

  /**
   * Constructs a course based on a course name and number (ex., MATH 101)
   * Defaults profName and profEmail to blank, hasResponded to false, courseChanges to blank
   */
  public Course(String courseName, int courseNumber, String courseOption) {
    this.courseName = courseName;
    this.courseNumber = courseNumber;
    this.courseOption = courseOption;
    this.profName = "";
    this.profEmail = "";
    this.hasResponded = false;
    this.courseChanges = "";
    constructIsGrad();
  }

  /**
   * Constructs a course based on a course name and number (ex., MATH 101), 
   * along with profName and profEmail
   * Defaults hasResponded to false, courseChanges to blank
   */
  public Course(String courseName, int courseNumber, String courseOption,
      String profName, String profEmail) {
    this.courseName = courseName;
    this.courseNumber = courseNumber;
    this.courseOption = courseOption;
    this.profName = profName;
    this.profEmail = profEmail;
    this.hasResponded = false;
    this.courseChanges = "";
    constructIsGrad();
  }

  /**
   * Construct a course based on all fields given.
   */
  public Course(String courseName, int courseNumber, String courseOption, String profName, 
      String profEmail, boolean hasResponded, String courseChanges) {
    this.courseName = courseName;
    this.courseNumber = courseNumber;
    this.courseOption = courseOption;
    this.profName = profName;
    this.profEmail = profEmail;
    this.hasResponded = hasResponded;
    this.courseChanges = courseChanges;
    constructIsGrad();
  }

  /**
   * If courseNumber > 500, sets isGrad to true.
   * Only called on constructor.
   */
  private void constructIsGrad() {
    if (this.courseNumber > 500) {
      this.isGrad = true;
    } else {
      this.isGrad = false;
    }
  }

  public String getProfName() {
    return this.profName;
  }

  public String getProfEmail() {
    return this.profEmail;
  }

  public String getCourseName() {
    return this.courseName;
  }

  public int getCourseNumber() {
    return this.courseNumber;
  }

  /**
   * Return the courses changes, if they exist.
   * @return this.courseChanges if this.hasResponded, else "No form response"
   */
  public String getResponse() {
    if (!this.hasResponded) {
      return "No form response";
    }
    return this.courseChanges;
  }

  /**
   * Checks whether or not a course is graduate-level.
   * @return this.isGrad directly.
   */
  public boolean isGrad() {
    return this.isGrad;
  }

  /**
   * Return whether or not a given course has changes yet.
   * @return is prof has responded to survey, and course changes do not contain "TBD"
   */
  public boolean hasChanges() {
    return this.hasResponded 
        && !this.courseChanges.contains("TBD") 
        && !this.courseChanges.equals("\"");
  }

  /**
   * Checks if course number is valid.
   * @return true if this.courseNumber > 0.
   */
  public boolean hasCourseNumber() {
    return this.courseNumber > 0;
  }

  /**
   * Constructs an output string for the WordPress shortcode based on class information.
   * @return the formatting string for the shortcode
   */
  public String toOutput() {
    checkCourseChangesQuality(this.courseName + " " + this.courseNumber + this.courseOption,
        this.courseChanges);
    String result = "";
    result += "<p>";
    result += String.format("<b>%s %d%s</b>\n", this.courseName, this.courseNumber,
        this.courseOption);
    result += String.format("<i>%s</i>\n", this.profName);
    result += this.courseChanges + "</p>\n";
    return result;
  }

  private void checkCourseChangesQuality(String courseCode, String courseChanges) {

    // Check course name is not within description
    if (courseChanges.contains(courseCode)) {
      System.out.println(String.format("Flag: %s contains its name in its description!", 
          courseCode));
    }

    // Filter certain strings from wording (remote learning is preferred)
    final String[] badStrings = {"online", "digital", "virtual"};
    for (String bs : badStrings) {
      if (courseChanges.contains(bs)) {
        System.out.println(String.format("Flag: %s contains %s in its description!", 
            courseCode, bs));
      }
    }
  }

  @Override 
  public String toString() {
    String message = String.format("%s %d%s taught by %s <%s>", 
        this.courseName, this.courseNumber, this.courseOption, this.profName, this.profEmail);
    return message; 
  }
}