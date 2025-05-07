package scheduler;

import model.Course;
import model.DegreePlan;
import java.util.*;
// import java.util.stream.Collectors;

public class Scheduler {
    private final PrereqGraph graph;
    private final Map<String, Course> allCoursesMap; // For accessing Course objects by ID

    private static final String SENIOR_PROJECT_1 = "Senior Project I";
    private static final String SENIOR_PROJECT_2 = "Senior Project II";
    private static final String CIS_1100 = "CIS 1100";
    private static final String CIS_1200 = "CIS 1200";
    private static final String WRITING_SEMINAR = "Writing Seminar";

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

        // Remove Senior Projects from the pool before any scheduling happens
        coursesToSchedule.remove(SENIOR_PROJECT_1);
        coursesToSchedule.remove(SENIOR_PROJECT_2);

        // Prepare for forced placements
        List<String> semester1Courses = new ArrayList<>();
        List<String> semester2Courses = new ArrayList<>();

        // Force placement for Semester 1
        if (coursesToSchedule.contains(WRITING_SEMINAR)) {
            semester1Courses.add(WRITING_SEMINAR);
            coursesToSchedule.remove(WRITING_SEMINAR);
            completedCourses.add(WRITING_SEMINAR);
        }
        // CIS 1100
        if (coursesToSchedule.contains(CIS_1100)) {
            // Check if its prerequisites are met
            Course cis1100 = allCoursesMap.get(CIS_1100);
            if (cis1100 != null && arePrerequisitesMet(cis1100, completedCourses)) { // Pass empty set initially
                if (semester1Courses.size() < maxCoursesPerSemester) { // Check space
                    semester1Courses.add(CIS_1100);
                    coursesToSchedule.remove(CIS_1100);
                    completedCourses.add(CIS_1100); // Add to completed after adding to semester 1
                } else {
                    System.err.println("Warning: Not enough space in Semester 1 to force placement of " + CIS_1100);
                    // CIS 1100 will be scheduled later by the main loop
                }
            } else if (cis1100 != null) {
                System.err.println("Warning: Cannot force " + CIS_1100
                        + " into Semester 1, prerequisites not met (should be unusual).");
            }
        }

        // Fill rest of Semester 1
        List<String> eligibleForSem1All = findEligibleCourses(coursesToSchedule, completedCourses);

        // If CIS 1100 was placed in Sem 1
        List<String> eligibleForSem1 = new ArrayList<>();
        boolean cis1100_in_sem1 = semester1Courses.contains(CIS_1100); // Check if CIS 1100 was successfully forced
        for (String eligibleCourse : eligibleForSem1All) {
            if (cis1100_in_sem1 && eligibleCourse.equals(CIS_1200)) {
                // Skip CIS 1200 if CIS 1100 is being forced into Sem 1
                continue;
            }
            eligibleForSem1.add(eligibleCourse);
        }

        int sem1FillCount = maxCoursesPerSemester - semester1Courses.size();
        for (int i = 0; i < sem1FillCount && i < eligibleForSem1.size(); i++) {
            String courseToAdd = eligibleForSem1.get(i);
            semester1Courses.add(courseToAdd);
            coursesToSchedule.remove(courseToAdd);
            completedCourses.add(courseToAdd);
        }
        if (!semester1Courses.isEmpty()) {
            completedCourses.addAll(semester1Courses);
            plan.addSemester(semester1Courses);
        }

        // Force placement for Semester 2
        if (coursesToSchedule.contains(CIS_1200) && completedCourses.contains(CIS_1100)) {
            Course cis1200 = allCoursesMap.get(CIS_1200);
            // Check if its other prerequisites are met by completedCourses
            if (cis1200 != null && arePrerequisitesMet(cis1200, completedCourses)) {
                if (semester2Courses.size() < maxCoursesPerSemester) { // Check space
                    semester2Courses.add(CIS_1200);
                    coursesToSchedule.remove(CIS_1200);
                } else {
                    System.err.println("Warning: Not enough space in Semester 2 to force placement of " + CIS_1200);
                }
            } else if (cis1200 != null) {
                System.err.println("Warning: Cannot force " + CIS_1200 + " into Semester 2, prerequisites not met.");
            }
        }

        // Fill rest of Semester 2
        List<String> eligibleForSem2 = findEligibleCourses(coursesToSchedule, completedCourses);
        int sem2FillCount = maxCoursesPerSemester - semester2Courses.size();
        List<String> addedToSem2 = new ArrayList<>();
        for (int i = 0; i < sem2FillCount && i < eligibleForSem2.size(); i++) {
            String courseToAdd = eligibleForSem2.get(i);
            semester2Courses.add(courseToAdd);
            addedToSem2.add(courseToAdd);
        }
        // Update completedCourses and coursesToSchedule after filling semester 2
        coursesToSchedule.removeAll(addedToSem2);
        completedCourses.addAll(semester2Courses); // Add everything from sem 2
        if (!semester2Courses.isEmpty()) {
            plan.addSemester(semester2Courses);
        }

        // Main loop for remaining semesters
        try {
            List<String> initialProcessingOrder = graph.topoSort();

            while (!coursesToSchedule.isEmpty()) {
                List<String> currentSemesterCourses = new ArrayList<>();
                List<String> eligibleNow = findEligibleCourses(coursesToSchedule, completedCourses);

                eligibleNow.sort(Comparator.comparingInt(courseId -> {
                    int index = initialProcessingOrder.indexOf(courseId);
                    return index == -1 ? Integer.MAX_VALUE : index;
                }));

                if (eligibleNow.isEmpty() && !coursesToSchedule.isEmpty()) {
                    System.err.println(
                            "Error: Cannot find eligible courses to schedule during main loop. Remaining: "
                                    + coursesToSchedule);
                    System.err.println("This might indicate unsatisfiable prerequisites or a data issue.");
                    plan.addSemester(new ArrayList<>(coursesToSchedule));
                    coursesToSchedule.clear();
                    break;
                }

                int coursesAddedThisSemester = 0;
                List<String> addedThisSem = new ArrayList<>();
                for (String courseToTake : eligibleNow) {
                    if (coursesAddedThisSemester < maxCoursesPerSemester) {
                        currentSemesterCourses.add(courseToTake);
                        addedThisSem.add(courseToTake);
                        coursesAddedThisSemester++;
                    } else {
                        break;
                    }
                }

                if (!currentSemesterCourses.isEmpty()) {
                    plan.addSemester(currentSemesterCourses);
                    completedCourses.addAll(currentSemesterCourses);
                    coursesToSchedule.removeAll(addedThisSem);
                } else if (!coursesToSchedule.isEmpty()) {
                    System.err.println(
                            "Warning: No courses added to semester despite eligibles during main loop. Remaining: "
                                    + coursesToSchedule);
                    plan.addSemester(new ArrayList<>(coursesToSchedule));
                    coursesToSchedule.clear();
                    break;
                }
            }
        } catch (IllegalStateException e) {
            System.err.println("Scheduling error (cycle detected by topoSort): " + e.getMessage());
        }

        // Force placement of Senior Projects
        List<List<String>> semesters = plan.getSemesters();

        int sp1_final_index = -1;

        // Place Senior Project I
        if (allCoursesMap.containsKey(SENIOR_PROJECT_1) && !completedCourses.contains(SENIOR_PROJECT_1)) {
            boolean sp1_placed = false;
            int currentNumSemesters = semesters.size();

            // Ensure we have semesters to work with add if plan is empty
            if (currentNumSemesters == 0) {
                semesters.add(new ArrayList<>());
                currentNumSemesters++;
            }

            // Try secondtolast semester first
            if (currentNumSemesters >= 2) {
                int targetIndex = currentNumSemesters - 2;
                List<String> targetSem = semesters.get(targetIndex);
                if (targetSem.size() < maxCoursesPerSemester) {
                    targetSem.add(SENIOR_PROJECT_1);
                    completedCourses.add(SENIOR_PROJECT_1);
                    sp1_final_index = targetIndex;
                    sp1_placed = true;
                }
            }
            // If not placed yet try the last semester
            if (!sp1_placed) {
                int targetIndex = currentNumSemesters - 1;
                List<String> targetSem = semesters.get(targetIndex);
                if (targetSem.size() < maxCoursesPerSemester) {
                    targetSem.add(SENIOR_PROJECT_1);
                    completedCourses.add(SENIOR_PROJECT_1);
                    sp1_final_index = targetIndex;
                    sp1_placed = true;
                }
            }
            if (!sp1_placed) {
                List<String> newSem = new ArrayList<>(List.of(SENIOR_PROJECT_1));
                plan.addSemester(newSem); // Appends to the end
                semesters = plan.getSemesters(); // Update local reference
                completedCourses.add(SENIOR_PROJECT_1);
                sp1_final_index = semesters.size() - 1; // Its in the new last semester
                sp1_placed = true;
            }
        } else if (completedCourses.contains(SENIOR_PROJECT_1)) {
            // If SP1 was already marked completed
            for (int i = 0; i < semesters.size(); i++) {
                if (semesters.get(i).contains(SENIOR_PROJECT_1)) {
                    sp1_final_index = i;
                    break;
                }
            }
            if (sp1_final_index == -1 && allCoursesMap.containsKey(SENIOR_PROJECT_1)) {
                System.err.println(
                        "Warning: SP1 marked completed but not found in plan! Cannot determine placement for SP2.");
            }
        }

        // Place Senior Project II
        if (allCoursesMap.containsKey(SENIOR_PROJECT_2) && !completedCourses.contains(SENIOR_PROJECT_2)) {
            if (!completedCourses.contains(SENIOR_PROJECT_1) || sp1_final_index == -1) {
                System.err.println("Error: Cannot place " + SENIOR_PROJECT_2 + " because prerequisite "
                        + SENIOR_PROJECT_1 + " was not placed successfully.");
            } else {
                boolean sp2_placed = false;
                int currentNumSemesters = semesters.size();
                int targetIndexForSP2 = sp1_final_index + 1; // Target semester AFTER SP1

                if (targetIndexForSP2 < currentNumSemesters) {
                    List<String> targetSem = semesters.get(targetIndexForSP2);
                    if (targetSem.size() < maxCoursesPerSemester) {
                        targetSem.add(SENIOR_PROJECT_2);
                        completedCourses.add(SENIOR_PROJECT_2);
                        sp2_placed = true;
                    }
                }

                // If not placed
                if (!sp2_placed) {
                    List<String> newSem = new ArrayList<>(List.of(SENIOR_PROJECT_2));
                    plan.addSemester(newSem); // Appends
                    completedCourses.add(SENIOR_PROJECT_2);
                    sp2_placed = true;
                }
            }
        }

        return plan;
    } // End of generateDegreePlan method

    private List<String> findEligibleCourses(Set<String> coursesToConsider, Set<String> completedCourses) {
        List<String> eligible = new ArrayList<>();
        for (String courseId : coursesToConsider) {
            Course course = this.allCoursesMap.get(courseId);
            if (course != null && arePrerequisitesMet(course, completedCourses)) {
                eligible.add(courseId);
            }
        }
        return eligible;
    }

    // Helper method to check if logical prerequisites for a course are met
    private boolean arePrerequisitesMet(Course course, Set<String> completedCourses) {
        if (course == null)
            return false;

        List<List<String>> prereqGroups = course.getPrerequisites();
        if (prereqGroups.isEmpty()) {
            return true; // No prerequisites
        }

        for (List<String> orGroup : prereqGroups) { // Each inner list is an OR group
            if (orGroup.isEmpty()) { // An empty OR group within AND groups means this path is unsatisfiable
                continue;
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