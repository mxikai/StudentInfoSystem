package StudentInfoSystem.src.model;

public class Program {
    
    private String code;
    private String name;
    private String college;

    public Program(String code, String name, String college) {
        this.code = code;
        this.name = name;
        this.college = college;
    }

    public String toCSV() {
        return code + "," + name + "," + college;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }
}
