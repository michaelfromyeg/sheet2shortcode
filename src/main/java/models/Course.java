package models; 

public class Course {
    private String courseName;
    private int courseNumber;
    private boolean isGrad;

    private String profName;
    private String profEmail;

    private boolean hasResponded;
    private String courseChanges;

    public Course(String courseName, int courseNumber) {
        this.courseName = courseName;
        this.courseNumber = courseNumber;
        this.profName = "";
        this.profEmail = "";
        this.hasResponded = false;
        this.courseChanges = "";
        constructIsGrad();
    }

    public Course(String courseName, int courseNumber, String profName, String profEmail) {
        this.courseName = courseName;
        this.courseNumber = courseNumber;
        this.profName = profName;
        this.profEmail = profEmail;
        this.hasResponded = false;
        this.courseChanges = "";
        constructIsGrad();
    }

    public Course(String courseName, int courseNumber, String profName, String profEmail, boolean hasResponded, String courseChanges) {
        this.courseName = courseName;
        this.courseNumber = courseNumber;
        this.profName = profName;
        this.profEmail = profEmail;
        this.hasResponded = hasResponded;
        this.courseChanges = courseChanges;
        constructIsGrad();
    }

    private void constructIsGrad() {
        if (this.courseNumber > 500) {
            this.isGrad = true;
        } else {
            this.isGrad = false;
        }
    }

    public String getCourseName() {
        return this.courseName;
    }

    public int getCourseNumber() {
        return this.courseNumber;
    }

    public String getResponse() {
        if (!this.hasResponded) {
            return "No form response";
        }
        return this.courseChanges;
    }

    public boolean isGrad() {
        return this.isGrad;
    }

    public boolean hasChanges() {
        return this.hasResponded && !this.courseChanges.contains("TBD");
    }

    public boolean hasCourseNumber() {
        return this.courseNumber > 0;
    }

    public String toOutput() {
        String result = "";
        result += String.format("<b>%s %d</b>\n", this.courseName, this.courseNumber);
        result += String.format("<i>%s</i>\n", this.profName);
        result += this.courseChanges + "\n";
        return result;
    }

    @Override 
    public String toString() {
        String message = String.format("%s %d taught by %s <%s>", this.courseName, this.courseNumber, this.profName, this.profEmail);
        return message; 
    }
}