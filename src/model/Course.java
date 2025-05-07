package model;

import java.util.List;

public class Course {
    private final String courseId; // e.g. CIS 1200
    private final String name; // Might not need this
    private final  List<List<String>> prerequisites; // List of a lists to include both OR and AND groups

    public Course(String courseId, String name,  List<List<String>> prerequisites) {
        this.courseId = courseId;
        this.name = name;
        this.prerequisites = prerequisites;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getName() {
        return name;
    }

    public  List<List<String>> getPrerequisites() {
        return prerequisites;
    }

    @Override
    public String toString() {
        return courseId + ": " + name + " | Prereqs: " + prerequisites;
    }
}