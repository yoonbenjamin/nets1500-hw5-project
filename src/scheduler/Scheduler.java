package scheduler;

import java.util.*;

import model.Course;

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
}
