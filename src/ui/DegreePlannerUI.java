package ui;

import java.util.*;

import model.Course;
import model.CourseDataLoader;
import scheduler.Scheduler;
import java.io.IOException;

public class DegreePlannerUI {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Smart Degree Planner!");
        System.out.print("Enter BSE major (ex. CSCI, BE, etc.): ");
        String majorCode = scanner.nextLine();

        List<Course> courses = CourseDataLoader.findCoursesAndPrereqsInMajor(majorCode);
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

        scanner.close();
    }
}
