package scheduler;

import java.util.*;

import model.Course;

public class PrereqGraph {
    private Map<String, Course> courses;
    private Map<String, List<String>> adjList;

    public PrereqGraph(List<Course> courseList) {
        courses = new HashMap<>();
        adjList = new HashMap<>();
        for (Course c : courseList) {
            courses.put(c.code, c);
            adjList.put(c.code, new ArrayList<>(c.prerequisites));
        }
    }

    // Topological sort: Returns courses in an order satisfying prerequisites
    public List<String> topoSort() throws Exception {
        List<String> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        for (String code : adjList.keySet()) {
            if (!visited.contains(code)) {
                dfs(code, visited, recStack, sorted);
            }
        }
        Collections.reverse(sorted);
        return sorted;
    }

    private void dfs(String node, Set<String> visited, Set<String> recStack, List<String> sorted) throws Exception {
        if (recStack.contains(node))
            throw new Exception("Cycle detected!");
        if (visited.contains(node))
            return;
        recStack.add(node);
        for (String neighbor : adjList.getOrDefault(node, new ArrayList<>())) {
            dfs(neighbor, visited, recStack, sorted);
        }
        recStack.remove(node);
        visited.add(node);
        sorted.add(node);
    }
}
