package StudentInfoSystem.src.model;

public class Student {

    private String id;
    private String firstName;
    private String lastName;
    private String programCode;
    private int year;
    private String gender;

    public Student(String id, String firstName, String lastName, String programCode, int year, String gender) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.programCode = programCode;
            this.year = year;
            this.gender = gender;
    }

    public String toCSV() {
        return id + "," + firstName + "," + lastName + "," + programCode + "," + year + "," + gender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
