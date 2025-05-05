# nets1500-hw5-project

# Smart Degree Planner

_Automated Class Scheduling Based on Major Prerequisites_  
**Penn Engineering | NETS 1500 | Spring 2025**

**Team Members:**  
- Ben Yoon (yoonb@seas.upenn.edu)  
- Cherilynn Chow (ccheril@sas.upenn.edu)  
- Sam Park (sampark@sas.upenn.edu)

---

## Project Overview

Smart Degree Planner helps Penn students generate optimal, semester-by-semester course schedules for their major. The tool scrapes or loads course data, models prerequisites as a directed graph, and uses graph algorithms (like topological sort) to ensure all requirements are satisfied in a feasible order.

---

## Features

- **Automated Course Scheduling:**  
  Generate a valid course plan for your selected major that satisfies all prerequisite dependencies.
- **Graph Modeling:**  
  Courses and their prerequisites are represented as a directed graph.
- **Custom User Preferences:**  
  Input your major, choose electives/gen eds, set maximum courses per semester, and block semesters (e.g. for study abroad).
- **Cycle Detection & Error Handling:**  
  Detects prerequisite cycles and warns users about impossible course plans.
- **Extensible:**  
  Easily add more majors, advanced prerequisite logic, or visualizations.

---

## How It Works# 

1. **Data Input:**  
   Load or scrape course and prerequisite data from UPenn's course/program websites into the program.
2. **Graph Construction:**  
   Build a directed graph with courses as nodes and prerequisites as edges.
3. **Scheduling Algorithm:**  
   Use topological sort to produce a valid course order. Additional logic for preferences, electives, and error checking.
4. **User Interface:**  
   Simple CLI to input your options and view your recommended schedule.

---

## Notes / Assumptions

- Only considers major requirements since the electives are dependent on the student.
- Only works for BSE majors.
- Senior project courses are grouped into one section (Senior Design Project Courses).
- In the case of major requirements where students can choose between multiple courses to fulfill a requirement, the first course is chosen since the chosce is dependent on the student.
- In courses where there is an OR in the prerequisites, the courses are contained in a list.
- Due to the differences in major requirement set-ups in Penn's major websites, some major requirement courses where the student selects multiple from a set of courses may be outputted incorrectly (for example, for the DMD major, the major requirement of selecting two courses from CIS 4610, CIS 5610, CIS 4620, CIS 5620, CIS 4550, or CIS 5550). Thus, in these cases, the courses are not included. 

---

## Directory Structure

```text
root/
├── src/
│   ├── data/              # Data loaders and parsers
│   ├── model/             # Course and prerequisite objects
│   ├── scheduler/         # Graph, scheduling, and algorithms
│   └── ui/                # Command-line interface
├── data/                  # Input data files (CSV/JSON)
└── README.md
```

<!-- TODO: -->

<!-- ---

## Getting Started

1. **Clone the repository:**  
   `git clone <nets1500-hw5-project>`

2. **Compile and run:**  
   - Compile all Java files in `src/`.
   - Run `Main.java` or launch `DegreePlannerUI`.

3. **Use the CLI:**  
   Follow prompts to input your major and preferences, then view your recommended schedule. -->

<!-- TODO: -->

<!-- ---

## Work Breakdown

- **Ben Yoon:**  
  Graph modeling, prerequisite logic, scheduling algorithms (topological sort, load balancing).
- **Cherilynn Chow:**  
  Web scraping & parsing, course data prep.
- **Sam Park:**  
  User interface (CLI), displaying schedules, documentation.

---

## Concepts Used

- **Graph and Graph Algorithms** (prerequisite modeling, topological sort)
- **Information Networks** (web scraping and data processing)

---

## Future Extensions

- Support for multiple majors/minors
- Visualization of prerequisite graphs
- Advanced user preferences (semester blocking, elective planning, etc.)
- Handling of ambiguous or complex prerequisites (AND/OR, corequisites)

---

## Contact

Questions? Email a team member or open an issue on the repository. -->
