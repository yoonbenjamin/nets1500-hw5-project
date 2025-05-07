package ui;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;

import model.Course;
import model.CourseDataLoader;
import model.DegreePlan;
import scheduler.Scheduler;

public class DegreePlannerUI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Smart Degree Planner!");

        String majorCode = "";
        List<Course> courses = null;

        // Loop until valid major courses are loaded
        while (courses == null || courses.isEmpty()) {
            System.out.print("Enter BSE major (ex. CSCI, BE, etc.): ");
            majorCode = scanner.nextLine().trim(); // trim to remove accidental leadingtrailing spaces

            if (majorCode.equalsIgnoreCase("exit")) { // Allow user to exit
                System.out.println("Exiting Smart Degree Planner.");
                scanner.close();
                return;
            }

            try {
                courses = CourseDataLoader.findCoursesAndPrereqsInMajor(majorCode);
                // System.out.println(courses);
                if (courses.isEmpty()) {
                    System.out.println("No courses found for major '" + majorCode +
                            "'. Please check the major code or type 'exit' to quit.");
                }
            } catch (IOException e) {
                System.err.println(
                        "An error occurred while loading course data for major '" + majorCode + "': " + e.getMessage());
                System.out.println("Please check your internet connection and the major code, or type 'exit' to quit.");
            }
        }

        int maxPerSem = 0;
        boolean validInput = false;
        while (!validInput) {
            System.out.print("Max number of courses per semester (e.g., 5): ");
            try {
                maxPerSem = scanner.nextInt();
                if (maxPerSem > 0) {
                    validInput = true;
                } else {
                    System.out.println("Please enter a positive number for max courses per semester.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            } finally {
                scanner.nextLine(); // Always consume the rest of the line
            }
        }

        Scheduler scheduler = new Scheduler(courses);
        DegreePlan plan = scheduler.generateDegreePlan(maxPerSem);

        System.out.println("\nRecommended semester-by-semester schedule for " + majorCode.toUpperCase() + ":");
        if (plan.getSemesters().isEmpty() && !courses.isEmpty()) {
            System.out.println("Could not generate a schedule. This might be due to issues with course data, " +
                    "unsatisfiable prerequisites, or a cycle detected earlier.");
        } else if (plan.getSemesters().isEmpty() && courses.isEmpty()) {
            System.out.println("No schedule to display as no courses were loaded.");
        } else {
            plan.printPlan();
        }

        scanner.close();
        System.out.println("\nThank you for using Smart Degree Planner!");
    }
}