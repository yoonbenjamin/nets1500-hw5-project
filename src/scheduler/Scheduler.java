package scheduler;

import java.util.*;
import model.Course;
import model.DegreePlan;

public class Scheduler {
    private PrereqGraph graph;

    public Scheduler(List<Course> courses) {
        graph = new PrereqGraph(courses);
    }

    public List<String> generateSchedule() {
        try {
            return graph.topoSort();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public DegreePlan generateDegreePlan(int maxCoursesPerSemester) {
        DegreePlan plan = new DegreePlan();

        try {
            List<String> sorted = graph.topoSort();
            Map<String, Integer> indegree = new HashMap<>();
            Map<String, List<String>> prereqMap = new HashMap<>();

            for (String course : sorted) {
                indegree.put(course, 0);
                prereqMap.put(course, new ArrayList<>());
            }

            for (String course : sorted) {
                for (String prereq : graph.getPrereqs(course)) {
                    prereqMap.get(course).add(prereq);
                    indegree.put(course, indegree.get(course) + 1);
                }
            }

            Set<String> completed = new HashSet<>();
            Queue<String> available = new LinkedList<>();

            for (String course : sorted) {
                if (indegree.get(course) == 0) {
                    available.add(course);
                }
            }

            while (!available.isEmpty()) {
                List<String> semesterCourses = new ArrayList<>();
                int count = 0;
                int size = available.size();

                while (count < maxCoursesPerSemester && size-- > 0) {
                    String course = available.poll();
                    semesterCourses.add(course);
                    completed.add(course);
                    count++;

                    for (String next : sorted) {
                        if (!completed.contains(next) && prereqMap.get(next).contains(course)) {
                            indegree.put(next, indegree.get(next) - 1);
                            if (indegree.get(next) == 0) {
                                available.add(next);
                            }
                        }
                    }
                }

                plan.addSemester(semesterCourses);
            }
        } catch (Exception e) {
            System.err.println("Scheduling error: " + e.getMessage());
        }

        return plan;
    }
}