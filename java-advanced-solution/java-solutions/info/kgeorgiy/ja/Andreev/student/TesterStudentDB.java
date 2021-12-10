package info.kgeorgiy.ja.Andreev.student;

import info.kgeorgiy.java.advanced.student.AdvancedQueryTest;
import info.kgeorgiy.java.advanced.student.GroupQueryTest;
import info.kgeorgiy.java.advanced.student.StudentQueryTest;
import info.kgeorgiy.java.advanced.walk.RecursiveWalkTest;
import info.kgeorgiy.java.advanced.walk.Tester;
import info.kgeorgiy.java.advanced.walk.WalkTest;

public class TesterStudentDB extends Tester {

    public static void main(String... args) {
        new info.kgeorgiy.java.advanced.student.Tester()
                .add("StudentQuery", StudentQueryTest.class)
                .add("GroupQuery", GroupQueryTest.class)
                .add("AdvancedQuery", AdvancedQueryTest.class)
                .run(args);
    }
}