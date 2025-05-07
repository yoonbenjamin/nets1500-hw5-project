package ui;

import java.util.*;
import java.io.IOException;

import model.Course;
import model.CourseDataLoader;
import model.DegreePlan;
import scheduler.Scheduler;

public class DegreePlannerUI {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Smart Degree Planner!");
        System.out.print("Enter BSE major (ex. CSCI, BE, etc.): ");
        String majorCode = scanner.nextLine();

        System.out.print("Max number of courses per semester: ");
        int maxPerSem = scanner.nextInt();
        scanner.nextLine(); // consume newline

        List<Course> courses = CourseDataLoader.findCoursesAndPrereqsInMajor(majorCode);
        Scheduler scheduler = new Scheduler(courses);

        DegreePlan plan = scheduler.generateDegreePlan(maxPerSem);
        System.out.println("\nRecommended semester-by-semester schedule:");
        plan.printPlan();

        scanner.close();
    }
}