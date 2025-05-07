package scheduler;

import model.Course;
import java.util.*;

public class PrereqGraph {
    // Map of courseId to Course object for all valid schedulable courses
    private final Map<String, Course> courses;
    // Adjacency list key is courseId
    private final Map<String, List<String>> adjList;

    private static final String SENIOR_DESIGN_PLACEHOLDER = "Senior Design Project Courses";

    public PrereqGraph(List<Course> courseList) {
        courses = new HashMap<>();
        adjList = new HashMap<>();

        // Initialize all potential nodes from courseList first
        List<Course> schedulableCourses = new ArrayList<>();
        for (Course course : courseList) {
            if (course.getName().contains("Senior")) {
                // Skip Senior named courses if they are to be replaced by the placeholder
                continue;
            }
            schedulableCourses.add(course);
            adjList.put(course.getCourseId(), new ArrayList<>()); // Ensure node exists
            courses.put(course.getCourseId(), course);
        }

        // Add the placeholder for Senior Design as a node
        adjList.putIfAbsent(SENIOR_DESIGN_PLACEHOLDER, new ArrayList<>());
        courses.putIfAbsent(SENIOR_DESIGN_PLACEHOLDER,
                new Course(SENIOR_DESIGN_PLACEHOLDER, "Senior Design Placeholder", Collections.emptyList()));

        for (Course course : schedulableCourses) {
            String courseId = course.getCourseId();

            // Build graph from prerequisites
            for (List<String> prereqGroup : course.getPrerequisites()) {
                for (String prereqCourseId : prereqGroup) {
                    // Ensure prereqCourseId node exists in adjList if its a valid course
                    adjList.putIfAbsent(prereqCourseId, new ArrayList<>());
                    adjList.get(prereqCourseId).add(courseId);
                }
            }

            // EXTRA CODE FOR DETAILS THAT CAN"T BE CAPTURED DURING WEBSCRAPING DUE TO
            // EXTERNAL BLOCKERS
            // Special rule CIS 1100 is a soft prereq for CIS 1200 enforce it
            if (courseId.equals("CIS 1100")) {
                adjList.get(courseId).add("CIS 1200");
                // Ensure CIS 1200 is in adjList if not already processed
                adjList.putIfAbsent("CIS 1200", new ArrayList<>());
            }

            // All nonsenior schedulable courses lead to the Senior Design placeholder
            adjList.get(courseId).add(SENIOR_DESIGN_PLACEHOLDER);
        }
    }

    /**
     * Performs a topological sort of the courses.
     * 
     * @return A list of course IDs in a topologically sorted order.
     * @throws IllegalStateException if a cycle is detected in the graph.
     */
    public List<String> topoSort() throws IllegalStateException {
        List<String> sortedOrder = new ArrayList<>();
        Set<String> visited = new HashSet<>(); // Tracks nodes visited in the current DFS path
        Set<String> fullyProcessed = new HashSet<>(); // Tracks nodes for which DFS is complete

        for (String courseId : courses.keySet()) { // Iterate using schedulable courses
            if (!fullyProcessed.contains(courseId)) {
                dfs(courseId, visited, fullyProcessed, sortedOrder);
            }
        }
        return sortedOrder;
    }

    private void dfs(String courseId, Set<String> visited, Set<String> fullyProcessed, List<String> sortedOrder)
            throws IllegalStateException {
        if (!adjList.containsKey(courseId) && !courses.containsKey(courseId)) {
            // This courseId might be a prerequisite listed in a courses data
            visited.remove(courseId); // Ensure its not stuck in visited if it was added then removed
            fullyProcessed.add(courseId);
            return;
        }

        visited.add(courseId);

        for (String neighbor : adjList.getOrDefault(courseId, Collections.emptyList())) {
            if (!fullyProcessed.contains(neighbor)) { // Only proceed if neighbor hasnt been fully processed
                if (visited.contains(neighbor)) {
                    throw new IllegalStateException("Cycle detected involving course: " + neighbor);
                }
                dfs(neighbor, visited, fullyProcessed, sortedOrder);
            }
        }

        visited.remove(courseId);
        fullyProcessed.add(courseId);
        sortedOrder.add(0, courseId); // Prepend to get correct topological order
    }

    /**
     * Gets the list of direct prerequisite course IDs for a given course ID,
     * based on the constructed graph.
     * 
     * @param courseId The ID of the course.
     * @return A list of prerequisite course IDs.
     */
    public List<String> getPrereqs(String courseId) {
        List<String> prereqs = new ArrayList<>();
        if (courseId == null)
            return prereqs;

        for (Map.Entry<String, List<String>> entry : adjList.entrySet()) {
            String potentialPrereq = entry.getKey();
            List<String> successors = entry.getValue();
            if (successors.contains(courseId)) {
                prereqs.add(potentialPrereq);
            }
        }
        return prereqs;
    }

    // Getter for the courses map might be useful for the Scheduler
    public Map<String, Course> getCoursesMap() {
        return Collections.unmodifiableMap(courses);
    }

    // Getter for the adjacency list
    public Map<String, List<String>> getAdjList() {
        return Collections.unmodifiableMap(adjList);
    }
}