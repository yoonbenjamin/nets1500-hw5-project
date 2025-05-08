package scheduler;

import model.Course;
import java.util.*;

public class PrereqGraph {
    private final Map<String, Course> courses; // Map of courseId to Course object for ALL courses in the graph
    private final Map<String, List<String>> adjList; // Adjacency list key is courseId value is list of its successors

    private static final String SENIOR_PROJECT_1 = "Senior Project I";
    private static final String SENIOR_PROJECT_2 = "Senior Project II";
    private static final String WRITING_SEMINAR = "Writing Seminar";

    public PrereqGraph(List<Course> courseListFromLoader) {
        courses = new HashMap<>();
        adjList = new HashMap<>();
        Set<String> knownCourseIds = new HashSet<>(); // Tracks all course IDs we know about

        // Initial population from the loaders list
        for (Course course : courseListFromLoader) {
            // The Senior filter is now expected to be done in CourseDataLoader
            String courseId = course.getCourseId();
            courses.put(courseId, course);
            adjList.put(courseId, new ArrayList<>());
            knownCourseIds.add(courseId);
        }

        // Add the nodes for Senior Project I and II
        courses.putIfAbsent(SENIOR_PROJECT_1,
                new Course(SENIOR_PROJECT_1, SENIOR_PROJECT_1, Collections.emptyList()));
        adjList.putIfAbsent(SENIOR_PROJECT_1, new ArrayList<>());
        knownCourseIds.add(SENIOR_PROJECT_1);

        courses.putIfAbsent(SENIOR_PROJECT_2,
                new Course(SENIOR_PROJECT_2, SENIOR_PROJECT_2, Collections.emptyList()));
        adjList.putIfAbsent(SENIOR_PROJECT_2, new ArrayList<>());
        knownCourseIds.add(SENIOR_PROJECT_2);

        // Add the node for Writing Seminar
        courses.putIfAbsent(WRITING_SEMINAR,
                new Course(WRITING_SEMINAR, WRITING_SEMINAR, Collections.emptyList()));
        adjList.putIfAbsent(WRITING_SEMINAR, new ArrayList<>());
        knownCourseIds.add(WRITING_SEMINAR);

        Set<String> processedForPhantomDiscovery = new HashSet<>(); // Avoid reprocessing a course for phantom discovery

        boolean newPhantomAddedInPass;
        do {
            newPhantomAddedInPass = false;
            // Iterate over a snapshot of current known actual courses to discover their
            List<Course> currentCoursesSnapshot = new ArrayList<>(courses.values());

            for (Course currentCourse : currentCoursesSnapshot) {
                if (processedForPhantomDiscovery.contains(currentCourse.getCourseId())) {
                }

                for (List<String> prereqGroup : currentCourse.getPrerequisites()) {
                    for (String prereqCourseId : prereqGroup) {
                        if (!knownCourseIds.contains(prereqCourseId)) {
                            Course phantomPrereq = new Course(prereqCourseId,
                                    prereqCourseId + " (auto-added)",
                                    Collections.emptyList());

                            courses.put(prereqCourseId, phantomPrereq);
                            adjList.put(prereqCourseId, new ArrayList<>());
                            knownCourseIds.add(prereqCourseId);
                            newPhantomAddedInPass = true;
                        }
                    }
                }
                processedForPhantomDiscovery.add(currentCourse.getCourseId());
            }
        } while (newPhantomAddedInPass); // Loop if new phantoms were added to check their prereqs

        // Second pass Now that all nodes exist
        for (Course course : courses.values()) {
            String courseId = course.getCourseId();
            for (List<String> prereqGroup : course.getPrerequisites()) {
                for (String prereqCourseId : prereqGroup) {
                    // adjList should contain prereqCourseId as a key from the phantom creation step
                    if (adjList.containsKey(prereqCourseId)) {
                        adjList.get(prereqCourseId).add(courseId);
                    } else {
                        System.err.println("WARNING: Prerequisite " + prereqCourseId + " for " + courseId +
                                " not found in adjList during edge creation. Skipping edge.");
                    }
                }
            }
        }

        // Apply special graph rules
        if (knownCourseIds.contains("CIS 1100") && knownCourseIds.contains("CIS 1200")) {
            adjList.get("CIS 1100").add("CIS 1200");
        }

        // Link regular courses to Senior Project I and Project I to Project II
        for (String courseId : knownCourseIds) {
            if (!courseId.equals(SENIOR_PROJECT_1) && !courseId.equals(SENIOR_PROJECT_2)) {
                if (adjList.containsKey(courseId)) {
                    adjList.get(courseId).add(SENIOR_PROJECT_1); // All other courses are prereqs for Project I
                }
            }
        }
        // Add explicit dependency
        if (adjList.containsKey(SENIOR_PROJECT_1)) {
            adjList.get(SENIOR_PROJECT_1).add(SENIOR_PROJECT_2);
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
            visited.remove(courseId);
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