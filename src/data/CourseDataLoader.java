package data;

import java.util.*;

import model.Course;

public class CourseDataLoader {
    // Loads data from CSV/JSON (from scraper) and returns list of courses

    public static List<Course> loadCourses(String filename) {
        List<Course> courses = new ArrayList<>();
        // TODO: Implement CSV/JSON loading logic
        // For now, add sample data for testing:
        Course c1 = new Course("CIS120", "Programming Languages");
        Course c2 = new Course("CIS121", "Data Structures");
        c2.prerequisites.add("CIS120");
        courses.add(c1);
        courses.add(c2);
        return courses;
    }
}
