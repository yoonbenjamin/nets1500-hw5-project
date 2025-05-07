package model;

import java.util.List;
import java.util.Objects;

public class Course {
    private final String courseId; // eg CIS 1200
    private final String name; // eg Programming Languages and Techniques I

    // Represents prerequisite conditions
    private final List<List<String>> prerequisites;

    public Course(String courseId, String name, List<List<String>> prerequisites) {
        this.courseId = Objects.requireNonNull(courseId, "Course ID cannot be null");
        this.name = Objects.requireNonNull(name, "Course name cannot be null");
        this.prerequisites = Objects.requireNonNull(prerequisites, "Prerequisites list cannot be null");
    }

    public String getCourseId() {
        return courseId;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the prerequisite structure for this course
     * The outer list signifies an AND relationship between prerequisite groups
     * Each inner list signifies an OR relationship between course IDs within that
     * group
     * An empty outer list means no prerequisites
     * An inner list with a single course ID means that specific course is required
     */
    public List<List<String>> getPrerequisites() {
        return prerequisites;
    }

    @Override
    public String toString() {
        // Omitting full prerequisite details for brevity in default toString
        return courseId + ": " + name + (prerequisites.isEmpty() ? "" : " | Has Prerequisites");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Course course = (Course) o;
        // Courses are uniquely identified by their ID
        return courseId.equals(course.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId);
    }
}