package scheduler;

import java.util.*;
import model.Course;

public class PrereqGraph {
    private Map<String, Course> courses;
    private Map<String, List<String>> adjList;

    public PrereqGraph(List<Course> courseList) {
        courses = new HashMap<>();
        adjList = new HashMap<>();

        for (Course course : courseList) {
            String courseId = course.getCourseId();
            courses.put(courseId, course);
            adjList.putIfAbsent(courseId, new ArrayList<>()); // Ensure course is in adjList

            for (List<String> prereqGroup : course.getPrerequisites()) {
                for (String prereqCourse : prereqGroup) {
                    adjList.putIfAbsent(prereqCourse, new ArrayList<>());
                    adjList.get(prereqCourse).add(courseId); // prereq → course
                }
            }
        }
    }

    public List<String> topoSort() throws Exception {
        List<String> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();

        for (String courseId : courses.keySet()) {
            if (!visited.contains(courseId)) {
                dfs(courseId, visited, recStack, sorted);
            }
        }

        return sorted; // Already in correct topological order
    }

    private void dfs(String node, Set<String> visited, Set<String> recStack, List<String> sorted) throws Exception {
        if (recStack.contains(node)) {
            throw new Exception("Cycle detected involving course: " + node);
        }
        if (visited.contains(node)) return;

        recStack.add(node);
        for (String neighbor : adjList.getOrDefault(node, new ArrayList<>())) {
            dfs(neighbor, visited, recStack, sorted);
        }
        recStack.remove(node);
        visited.add(node);
        sorted.add(0, node); // prepend to maintain topo order
    }
}
