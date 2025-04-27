package model;

import java.util.*;

public class Course {
    public String code;
    public String name;
    public List<String> prerequisites; // List of course codes (IDs)
    public boolean isGenEd;
    public boolean isElective;

    public Course(String code, String name) {
        this.code = code;
        this.name = name;
        this.prerequisites = new ArrayList<>();
        this.isGenEd = false;
        this.isElective = false;
    }

    // Add more attributes as needed (semester, credits, etc.)
}
