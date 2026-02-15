package model;

public class College {
    
    private String collegeCode;
    private String collegeName;

    public College(String collegeCode, String collegeName) {
        this.collegeCode = collegeCode;
        this.collegeName = collegeName;
    }

    public String toCSV() {
        return collegeCode + "," + collegeName;
    }

    public String getCollegeCode() {
        return collegeCode;
    }

    public void setCollegeCode(String collegeCode) {
        this.collegeCode = collegeCode;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

}
