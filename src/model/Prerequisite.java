package model;

import java.util.*;

public class Prerequisite {
    // For more advanced AND/OR prereq logic
    public List<List<String>> orGroups;

    public Prerequisite() {
        this.orGroups = new ArrayList<>();
    }

    public void addOrGroup(List<String> group) {
        this.orGroups.add(group); // e.g. [[CIS120, CIS121], [MATH104]]
    }
}
