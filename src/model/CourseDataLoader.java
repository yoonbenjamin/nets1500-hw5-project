package model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourseDataLoader {

    private static final String BASE_URL = "https://catalog.upenn.edu";

    public static List<Course> findCoursesAndPrereqsInMajor(String departmentCode) throws IOException {
        String url = BASE_URL + "/undergraduate/programs/";

        switch (departmentCode.toLowerCase()) {
            case "arin":
                url += "artificial-intelligence-bse//";
                break;
            case "be":
                url += "bioengineering-bse//";
                break;
            case "cbe":
                url += "chemical-biomolecular-engineering-bse/";
                break;
            case "cmpe":
                url += "computer-engineering-bse/";
                break;
            case "csci":
                url += "computer-science-bse/";
                break;
            case "dmd":
                url += "digital-media-design-bse/";
                break;
            case "ee":
                url += "electrical-engineering-bse/";
                break;
            case "mse":
                url += "materials-science-engineering-bse/";
                break;
            case "meam":
                url += "mechanical-engineering-applied-mechanics-bse/";
                break;
            case "nets":
                url += "networked-social-systems-engineering-bse/";
                break;
            case "sse":
                url += "systems-science-engineering-bse/";
                break;
            default:
                System.out.println("Invalid department code. Please try again.");
                return Collections.emptyList();
        }

        ArrayList<Course> courseList = new ArrayList<>();

        Document doc = Jsoup.connect(url).get();
        Elements majorTable = doc.select(".sc_courselist");

        Elements rows = majorTable.select("tr.odd,tr.even");
        for (Element row : rows) { // Each row is a tr
            Element creditColumn = row.select("td.hourscol").first();
            if (creditColumn != null) {
                if (creditColumn.text() != null && !creditColumn.text().isEmpty()) {

                    // Attempt to get the primary course name for this row first
                    String primaryCourseNameForRow = "";
                    Element titleColElement = row.select("td.titlecol").first();
                    if (titleColElement != null) {
                        primaryCourseNameForRow = titleColElement.text().trim();
                    } else {
                        // Fallback If no titlecol try to find the cell next to the first codecol
                        Element firstCodeCellInRow = row.select("td.codecol").first();
                        if (firstCodeCellInRow != null) {
                            Element potentialNameCell = firstCodeCellInRow.nextElementSibling();
                            if (potentialNameCell != null &&
                                    "td".equalsIgnoreCase(potentialNameCell.tagName()) &&
                                    !potentialNameCell.hasClass("hourscol") &&
                                    !potentialNameCell.text().trim().matches("^\\d*\\.?\\d+$")) { // Ensure it's a td
                                primaryCourseNameForRow = potentialNameCell.text().trim();
                            }
                        }
                    }

                    Elements codeCells = row.select("td.codecol");
                    if (codeCells != null) {
                        for (Element codeCell : codeCells) {
                            Elements courseLinksInCell = codeCell.select("a");

                            for (Element courseLink : courseLinksInCell) {
                                if (courseLink != null && courseLink.hasAttr("href")
                                        && courseLink.attr("href").contains("/search/?P=")) {
                                    String courseLinkHref = courseLink.attr("href");
                                    String courseUrl = BASE_URL + courseLinkHref;

                                    List<List<String>> prerequisites = findPrerequisites(courseUrl);

                                    String courseId = courseLink.text().replace("\u00a0", " ").trim();
                                    String courseName = primaryCourseNameForRow; // Start with the name found for the

                                    // If the link has a specific title attribute AND its different from just the
                                    String linkTitle = courseLink.hasAttr("title")
                                            ? courseLink.attr("title").replace("&nbsp;", " ").trim()
                                            : "";
                                    if (!linkTitle.isEmpty() && !linkTitle.equalsIgnoreCase(courseId)
                                            && !linkTitle.equalsIgnoreCase(primaryCourseNameForRow)) {
                                        // Only override if linkTitle offers a more descriptive name than the courseId
                                        if (primaryCourseNameForRow.isEmpty()
                                                || primaryCourseNameForRow.equalsIgnoreCase(courseId)
                                                || courseLinksInCell.size() > 1) {
                                            courseName = linkTitle;
                                        }
                                    }

                                    // If after all attempts courseName is empty or same as ID
                                    if ((courseName.isEmpty() || courseName.equalsIgnoreCase(courseId))
                                            && !primaryCourseNameForRow.isEmpty()) {
                                        courseName = primaryCourseNameForRow;
                                    }

                                    // Final fallback if name is still just the courseID or empty
                                    if (courseName.isEmpty() || courseName.equalsIgnoreCase(courseId)) {
                                        courseName = "Course name not found"; // Or keep as courseId if preferred
                                    }

                                    // Filter out Senior Design courses by name before adding
                                    if (courseName.toLowerCase().contains("senior")) {
                                        continue;
                                    }

                                    Course courseToAdd = new Course(courseId, courseName, prerequisites);
                                    courseList.add(courseToAdd);
                                }
                            }
                        }
                    }
                }
            }
        }
        return courseList;
    }

    // Finds the prerequisites for a course
    public static List<List<String>> findPrerequisites(String courseUrl) throws IOException {
        List<List<String>> prerequisites = new ArrayList<>();

        Document courseDoc = Jsoup.connect(courseUrl).get();
        Element courseBlock = courseDoc.selectFirst(".courseblock");
        if (courseBlock != null) {
            Elements descElements = courseBlock.select(".courseblockextra");

            for (Element descElement : descElements) {
                String descText = descElement.text();

                // Check if the description contains Prerequisite with three courses
                Pattern descPrereqThreeCoursesPattern = Pattern.compile(
                        "Prerequisite:(\\s+([A-Za-z]+\\s+)+)[0-9]+\\sAND\\s[A-Za-z0-9]+\\s[0-9]+\\s[A-Za-z0-9]+\\s[A-Za-z0-9]+\\s[0-9]+");
                Matcher descPrereqThreeCoursesMatcher = descPrereqThreeCoursesPattern.matcher(descText);
                if (descPrereqThreeCoursesMatcher.find()) {
                    // Try to extract prerequisites from description
                    String prereqText = descPrereqThreeCoursesMatcher.group();
                    prerequisites = extractPrerequisites(prereqText);
                    return prerequisites;
                }

                // Check if the description contains Prerequisite with two courses
                Pattern descPrereqTwoCoursesPattern = Pattern
                        .compile("Prerequisite:(\\s+([A-Za-z]+\\s+)+)[0-9]+\\sAND\\s[A-Za-z0-9]+\\s[0-9]+");
                Matcher descPrereqTwoCoursesMatcher = descPrereqTwoCoursesPattern.matcher(descText);
                if (descPrereqTwoCoursesMatcher.find()) {
                    // Try to extract prerequisites from description
                    String prereqText = descPrereqTwoCoursesMatcher.group();
                    prerequisites = extractPrerequisites(prereqText);
                    return prerequisites;
                }

                // Check if the description contains Prerequisite
                Pattern descPrereqTwoCoursesORPattern = Pattern
                        .compile("Prerequisite:(\\s+([A-Za-z]+\\s+)+)[0-9]+\\sOR\\s[A-Za-z0-9]+\\s[0-9]+");
                Matcher descPrereqTwoCoursesORMatcher = descPrereqTwoCoursesORPattern.matcher(descText);
                if (descPrereqTwoCoursesORMatcher.find()) {
                    // Try to extract prerequisites from description
                    String prereqText = descPrereqTwoCoursesORMatcher.group();
                    prerequisites = extractPrerequisites(prereqText);
                    return prerequisites;
                }

                // Check if the description contains Prerequisite with one course
                Pattern descPrereqOneCourseFourPattern = Pattern
                        .compile("Prerequisite: [A-Za-z][A-Za-z][A-Za-z][A-Za-z]\\s\\d\\d\\d\\d");
                Matcher descPrereqOneCourseFourMatcher = descPrereqOneCourseFourPattern.matcher(descText);
                if (descPrereqOneCourseFourMatcher.find()) {
                    // Try to extract prerequisites from description
                    String prereqText = descPrereqOneCourseFourMatcher.group();
                    prerequisites = extractPrerequisites(prereqText);
                    return prerequisites;
                }

                // Check if the description contains Prerequisite with one course
                Pattern descPrereqOneCourseThreePattern = Pattern
                        .compile("Prerequisite: [A-Za-z][A-Za-z][A-Za-z]\\s\\d\\d\\d\\d");
                Matcher descPrereqOneCourseThreeMatcher = descPrereqOneCourseThreePattern.matcher(descText);
                if (descPrereqOneCourseThreeMatcher.find()) {
                    // Try to extract prerequisites from description
                    String prereqText = descPrereqOneCourseThreeMatcher.group();
                    prerequisites = extractPrerequisites(prereqText);
                    return prerequisites;
                }
            }
        }

        return prerequisites;
    }

    // Finds the prerequisites for all courses in a given department
    public static List<Course> loadCoursesForDepartment(String departmentCode) throws IOException {
        String url = BASE_URL + "/courses/" + departmentCode.toLowerCase() + "/";
        Document doc = Jsoup.connect(url).get();

        List<Course> courseList = new ArrayList<>();

        Elements courseBlocks = doc.select(".sc_sccoursedescs .courseblock");

        for (Element block : courseBlocks) {
            String title = block.selectFirst(".courseblocktitle").text();
            Elements descElements = block.select(".courseblockextra");
            List<List<String>> prerequisites = new ArrayList<>();

            for (Element descElement : descElements) {
                String descText = descElement.text();

                // Check if the description contains Prerequisite with three courses
                Pattern descPrereqThreeCoursesPattern = Pattern.compile(
                        "Prerequisite:(\\s+([A-Za-z]+\\s+)+)[0-9]+\\sAND\\s[A-Za-z0-9]+\\s[0-9]+\\s[A-Za-z0-9]+\\s[A-Za-z0-9]+\\s[0-9]+");
                Matcher descPrereqThreeCoursesMatcher = descPrereqThreeCoursesPattern.matcher(descText);
                if (descPrereqThreeCoursesMatcher.find()) {
                    // Try to extract prerequisites from description
                    String prereqText = descPrereqThreeCoursesMatcher.group();
                    prerequisites = extractPrerequisites(prereqText);
                    break;
                }

                // Check if the description contains Prerequisite with two courses
                Pattern descPrereqTwoCoursesPattern = Pattern
                        .compile("Prerequisite:(\\s+([A-Za-z]+\\s+)+)[0-9]+\\sAND\\s[A-Za-z0-9]+\\s[0-9]+");
                Matcher descPrereqTwoCoursesMatcher = descPrereqTwoCoursesPattern.matcher(descText);
                if (descPrereqTwoCoursesMatcher.find()) {
                    // Try to extract prerequisites from description
                    String prereqText = descPrereqTwoCoursesMatcher.group();
                    prerequisites = extractPrerequisites(prereqText);
                    break;
                }

                // Check if the description contains Prerequisite
                Pattern descPrereqTwoCoursesORPattern = Pattern
                        .compile("Prerequisite:(\\s+([A-Za-z]+\\s+)+)[0-9]+\\sOR\\s[A-Za-z0-9]+\\s[0-9]+");
                Matcher descPrereqTwoCoursesORMatcher = descPrereqTwoCoursesORPattern.matcher(descText);
                if (descPrereqTwoCoursesORMatcher.find()) {
                    // Try to extract prerequisites from description
                    String prereqText = descPrereqTwoCoursesORMatcher.group();
                    prerequisites = extractPrerequisites(prereqText);
                    break;
                }

                // Check if the description contains Prerequisite
                Pattern descPrereqOneCoursePattern = Pattern.compile("Prerequisite:(\\s+([A-Za-z]+\\s+)+)\\d\\d\\d\\d");
                Matcher descPrereqOneCourseMatcher = descPrereqOneCoursePattern.matcher(descText);
                if (descPrereqOneCourseMatcher.find()) {
                    // Try to extract prerequisites from description
                    String prereqText = descPrereqOneCourseMatcher.group();
                    prerequisites = extractPrerequisites(prereqText);
                    break;
                }
            }

            String courseId = title.split("\\.")[0].trim();
            String courseName = title.substring(title.indexOf('.') + 1).trim();

            Course course = new Course(courseId, courseName, prerequisites);
            courseList.add(course);
        }

        return courseList;
    }

    // Parses the prerequisites from the description to find the course codes
    private static List<List<String>> extractPrerequisites(String description) {
        List<List<String>> prereqs = new ArrayList<>();

        // Find if there is OR logic in the description
        if (description.contains("OR")) {
            List<String> orGroup = new ArrayList<>();
            Pattern codePattern = Pattern.compile("[A-Za-z0-9]+\\s[0-9]+");
            Matcher matcher = codePattern.matcher(description);
            while (matcher.find()) {
                String code = matcher.group();
                orGroup.add(code);
            }

            // Add the OR group to the prerequisites
            prereqs.add(orGroup);
        } else { // No OR logic, just a single group or uses AND logic
            Pattern codePattern = Pattern.compile("[A-Za-z0-9]+\\s[0-9]+");
            Matcher matcher = codePattern.matcher(description);
            while (matcher.find()) {
                List<String> code = new ArrayList<>();
                String codeString = matcher.group();
                code.add(codeString);
                prereqs.add(code);
            }
        }

        return prereqs;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter BSE major (ex. CSCI, BE, etc.):");

        scanner.close();
    }
}