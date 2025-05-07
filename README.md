# nets1500-hw5-project

# Smart Degree Planner

**Penn Engineering | NETS 1500 | Spring 2025**

**Team Members:**
* Cherilynn Chow (ccheril@sas.upenn.edu)
* Sam Park (sampark@sas.upenn.edu)
* Ben Yoon (yoonb@seas.upenn.edu)

---

## Project Description

Smart Degree Planner is a Java application designed to assist Penn Engineering (BSE) students in planning their academic paths. The tool automatically generates a potential semester-by-semester course schedule based on the user's chosen major. It achieves this by scraping course and prerequisite data directly from the official Penn course catalog website. This data is used to build a dependency graph, and scheduling algorithms are applied to create a valid sequence that respects prerequisite constraints and basic placement rules, aiming for a balanced course load per semester as specified by the user.

---

## Concepts Used

This project primarily utilizes concepts from:

1.  **Graph and Graph Algorithms:** Course prerequisites are modeled as a directed graph where an edge `A -> B` means course A must be taken before course B. Topological sorting and custom scheduling logic based on graph traversal are used to determine a valid course sequence and distribute courses across semesters. Cycle detection is included within the topological sort algorithm.
2.  **Information Networks (World Wide Web):** The application scrapes data (course IDs, names, prerequisites) from the live Penn course catalog website (`catalog.upenn.edu`) using the Jsoup library for HTML parsing. This involves navigating web pages and extracting structured information from the HTML source.

---

## Work Breakdown

* **Cherilynn Chow:** Developed the web scraping component (`CourseDataLoader.java`) using Jsoup to fetch course information and prerequisites from the Penn course catalog for various BSE majors. Implemented regex-based parsing to extract prerequisite details from course description text and structure it for the Course model. Handled data cleaning and de-duplication of loaded courses.
* **Sam Park:** Implemented the command-line user interface (`DegreePlannerUI.java`, `Main.java`) to handle user input (major code, max courses per semester) and display the generated schedule output using `DegreePlan.java`. Added input validation and user-friendly error messages. Responsible for overall testing coordination and final documentation (User Manual, Report contributions).
* **Ben Yoon:** Designed and implemented the graph model (`PrereqGraph.java`) to represent course dependencies. Developed and implemented the core scheduling algorithm including prerequisite logic, topological sort integration, load balancing based on max courses per semester, and logic for special course placements. Added phantom course creation for missing prerequisites.

---

## Getting Started

**Prerequisites:**
* Java Development Kit (JDK) 11 or higher installed.
* `jsoup-1.16.1.jar` library file (place in a `lib` directory relative to the project root).

**Compilation (from project root directory):**
* Create an output directory: `mkdir bin`
* Compile (use `;` instead of `:` for classpath on Windows):
    ```bash
    javac -d bin -cp lib/jsoup-1.16.1.jar src/model/*.java src/scheduler/*.java src/ui/*.java src/Main.java
    ```

**Running (from project root directory):**
* Run (use `;` instead of `:` for classpath on Windows):
    ```bash
    java -cp bin:lib/jsoup-1.16.1.jar Main
    ```
* Follow the prompts to enter the desired BSE major code and the maximum number of courses per semester.

---

## Notes & Assumptions

* The planner primarily focuses on listed major requirements for BSE degrees at Penn. Electives and general requirements are not explicitly scheduled.
* Accuracy depends heavily on the structure and content of `catalog.upenn.edu`. Changes to the website may break the scraper.
* Prerequisite parsing uses specific patterns; complex or non-standard prerequisite descriptions may not be fully captured.
* Auto-added ("phantom") prerequisites (courses needed but not listed on the major page) are assumed to have no prerequisites themselves.
* Specific courses (e.g., Writing Seminar, Senior Project I, Senior Project II) have forced placement rules applied.
* In the case of major requirements where students can choose between multiple courses to fulfill a requirement, the first course is chosen since the chosce is dependent on the student.
* In courses where there is an OR in the prerequisites, the courses are contained in a list.
* Due to the differences in major requirement set-ups in Penn's major websites, some major requirement courses where the student selects multiple from a set of courses may be outputted incorrectly (for example, for the DMD major, the major requirement of selecting two courses from CIS 4610, CIS 5610, CIS 4620, CIS 5620, CIS 4550, or CIS 5550). Thus, in these cases, the courses are not included.