package ui;

import java.util.*;

import data.CourseDataLoader;
import model.Course;
import scheduler.Scheduler;

public class DegreePlannerUI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Smart Degree Planner!");
        System.out.print("Enter data file path (CSV/JSON): ");
        String filename = scanner.nextLine();

        List<Course> courses = CourseDataLoader.loadCourses(filename);
        Scheduler scheduler = new Scheduler(courses);

        List<String> schedule = scheduler.generateSchedule();
        if (schedule.size() > 0) {
            System.out.println("Recommended course order:");
            for (String code : schedule) {
                System.out.println(code);
            }
        } else {
            System.out.println("Could not generate a schedule. Please check prerequisites for cycles.");
        }
    }
}
