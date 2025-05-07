package model;

import java.util.*;

public class DegreePlan {
    private List<List<String>> semesters;

    public DegreePlan() {
        this.semesters = new ArrayList<>();
    }

    public void addSemester(List<String> courses) {
        semesters.add(courses);
    }

    public List<List<String>> getSemesters() {
        return semesters;
    }

    public void printPlan() {
        for (int i = 0; i < semesters.size(); i++) {
            System.out.println("Semester " + (i + 1) + ": " + semesters.get(i));
        }
    }
}