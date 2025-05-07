package scheduler;

import model.Course;
import model.DegreePlan;
import java.util.*;
import java.util.stream.Collectors;

public class Scheduler {
    private final PrereqGraph graph;
    private final Map<String, Course> allCoursesMap; // For accessing Course objects by ID

    public Scheduler(List<Course> coursesFromLoader) {
        this.graph = new PrereqGraph(coursesFromLoader);
        this.allCoursesMap = Collections.unmodifiableMap(this.graph.getCoursesMap());
    }

    // Returns a single valid linear sequence of courses
    public List<String> generateLinearSchedule() {
        try {
            return graph.topoSort();
        } catch (IllegalStateException e) {
            System.err.println("Scheduling error (cycle detected): " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Generates a semesterbysemester plan respecting prerequisites and load
    public DegreePlan generateDegreePlan(int maxCoursesPerSemester) {
        if (maxCoursesPerSemester <= 0) {
            throw new IllegalArgumentException("Max courses per semester must be positive.");
        }
        DegreePlan plan = new DegreePlan();
        Set<String> completedCourses = new HashSet<>();
        // Use course IDs from the graphs understanding of schedulable courses
        Set<String> coursesToSchedule = new HashSet<>(this.allCoursesMap.keySet());

        try {
            // Use topological sort to influence processing order of eligibles
            List<String> initialProcessingOrder = graph.topoSort();
            // // TEMPORARY DEBUG
            // System.out.println("DEBUG: Initial allCoursesMap keys: " +
            // allCoursesMap.keySet());
            // Course meam1100 = allCoursesMap.get("MEAM 1100");
            // if (meam1100 != null) {
            // System.out.println("DEBUG: MEAM 1100 Prereqs from DataLoader: " +
            // meam1100.getPrerequisites());
            // } else {
            // System.out.println("DEBUG: MEAM 1100 not found in allCoursesMap!");
            // }
            // Course phys0151 = allCoursesMap.get("PHYS 0151");
            // if (phys0151 != null) {
            // System.out.println("DEBUG: PHYS 0151 Prereqs from DataLoader: " +
            // phys0151.getPrerequisites());
            // } else {
            // System.out.println("DEBUG: PHYS 0151 not found in allCoursesMap!");
            // }
            // // END TEMPORARY DEBUG
            while (!coursesToSchedule.isEmpty()) {
                List<String> currentSemesterCourses = new ArrayList<>();
                List<String> eligibleNow = new ArrayList<>();

                // Determine eligible courses based on logical prerequisites
                List<String> candidatesToConsider = initialProcessingOrder.stream()
                        .filter(coursesToSchedule::contains)
                        .collect(Collectors.toList());
                // Add any remaining courses not in topoSort
                coursesToSchedule.stream().filter(c -> !candidatesToConsider.contains(c))
                        .forEach(candidatesToConsider::add);

                for (String courseId : candidatesToConsider) {
                    Course course = this.allCoursesMap.get(courseId);
                    if (course != null && arePrerequisitesMet(course, completedCourses)) {
                        eligibleNow.add(courseId);
                    }
                }

                if (eligibleNow.isEmpty() && !coursesToSchedule.isEmpty()) {
                    // This could happen if theres an unresolvable situation not caught by graph
                    System.err.println(
                            "Error: Cannot find eligible courses to schedule. Remaining: " + coursesToSchedule);
                    System.err.println("This might indicate unsatisfiable prerequisites or a data issue.");
                    // Add remaining courses to a final problematic semester or handle as error
                    plan.addSemester(new ArrayList<>(coursesToSchedule)); // Add remaining to indicate issue
                    coursesToSchedule.clear(); // Exit loop
                    break;
                }

                int coursesAddedThisSemester = 0;
                for (String courseToTake : eligibleNow) {
                    if (coursesAddedThisSemester < maxCoursesPerSemester) {
                        currentSemesterCourses.add(courseToTake);
                        coursesAddedThisSemester++;
                    } else {
                        break; // Semester full
                    }
                }

                if (!currentSemesterCourses.isEmpty()) {
                    plan.addSemester(currentSemesterCourses);
                    completedCourses.addAll(currentSemesterCourses);
                    coursesToSchedule.removeAll(currentSemesterCourses);
                } else if (!coursesToSchedule.isEmpty()) {
                    // If eligibleNow was not empty
                    System.err.println(
                            "Warning: No courses added to semester despite eligibles. Remaining: " + coursesToSchedule);
                    // To prevent infinite loop break or throw For now
                    plan.addSemester(new ArrayList<>(coursesToSchedule));
                    coursesToSchedule.clear();
                    break;
                }
            }
        } catch (IllegalStateException e) { // Catch cycle from topoSort
            System.err.println("Scheduling error (cycle detected by topoSort): " + e.getMessage());
            // Plan will be partial or empty
        }
        return plan;
    }

    // Helper method to check if logical prerequisites for a course are met
    private boolean arePrerequisitesMet(Course course, Set<String> completedCourses) {
        if (course == null)
            return false; // Should not happen if courseId came from allCoursesMap

        List<List<String>> prereqGroups = course.getPrerequisites();
        if (prereqGroups.isEmpty()) {
            return true; // No prerequisites
        }

        for (List<String> orGroup : prereqGroups) { // Each inner list is an OR group
            if (orGroup.isEmpty()) { // An empty OR group within AND groups means this path is unsatisfiable
                // This case should ideally be prevented by data validation
                continue; // Or treat as true if it means optional category satisfied
            }
            boolean orGroupSatisfied = false;
            for (String prereqCourseId : orGroup) {
                if (completedCourses.contains(prereqCourseId)) {
                    orGroupSatisfied = true;
                    break; // This OR group is satisfied
                }
            }
            if (!orGroupSatisfied) {
                return false; // This ANDconnected prerequisite group is not satisfied
            }
        }
        return true; // All ANDconnected prerequisite groups are satisfied
    }
}