package info.kgeorgiy.ja.Andreev.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("findStudentsShoulBeWith")
public class StudentDB implements GroupQuery {
    //java -cp . -p . -m info.kgeorgiy.java.advanced.student StudentQuery(GroupQuery)  info.kgeorgiy.ja.Andreev.student.StudentDB

    private static final Comparator<Student> COMPARATOR_STUDENT_BY_NAME = Comparator
            .comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .reversed().thenComparing(Student::getId);

    private static final String EMPTY_STRING = "";


    private <T, C extends Collection<T>>
    C getMappingCollection(final List<Student> students,
                           final Function<Student, T> function,
                           final Collector<T, ?, C> collector) {
        return students.stream().map(function)
                .collect(collector);
    }


    @Override
    public List<String> getFirstNames(final List<Student> students) {
        return getMappingCollection(students, Student::getFirstName, Collectors.toUnmodifiableList());
    }


    @Override
    public List<String> getLastNames(final List<Student> students) {
        return getMappingCollection(students, Student::getLastName, Collectors.toUnmodifiableList());
    }


    @Override
    public List<GroupName> getGroups(final List<Student> students) {
        return getMappingCollection(students, Student::getGroup, Collectors.toUnmodifiableList());
    }


    @Override
    public List<String> getFullNames(final List<Student> students) {
        return getMappingCollection(students, student -> student.getFirstName() + " " + student.getLastName(), Collectors.toUnmodifiableList());
    }


    @Override
    public Set<String> getDistinctFirstNames(final List<Student> students) {
        return getMappingCollection(students, Student::getFirstName, Collectors.toCollection(TreeSet::new));
    }


    @Override
    public String getMaxStudentFirstName(final List<Student> students) {
        return students.stream().max(Student::compareTo).map(Student::getFirstName).orElse(EMPTY_STRING);
    }

    private List<Student> sortStudent(
            final Collection<Student> students,
            final Comparator<? super Student> comparator,
            final Collector<Student, ?, List<Student>> collector) {
        return students.stream().sorted(comparator).collect(collector);
    }

    private List<Student> sortStudent(final Collection<Student> students, final Comparator<? super Student> comparator) {
        return sortStudent(students, comparator, Collectors.toUnmodifiableList());
    }



    @Override
    public List<Student> sortStudentsById(final Collection<Student> students) {
        return sortStudent(students, Student::compareTo);
    }


    @Override
    public List<Student> sortStudentsByName(final Collection<Student> students) {
        return sortStudent(students, COMPARATOR_STUDENT_BY_NAME);
    }

    private Stream<Student> filterCollectionStudents(final Collection<Student> students, final Predicate<? super Student> predicate) {
        return students.stream().filter(predicate);

    }

    private List<Student> findStudents(final Collection<Student> students, final Predicate<? super Student> predicate) {
        return filterCollectionStudents(students, predicate).
                sorted(StudentDB.COMPARATOR_STUDENT_BY_NAME).collect(Collectors.toUnmodifiableList());
    }


    @Override
    public List<Student> findStudentsByFirstName(final Collection<Student> students, final String name) {
        return findStudents(students, student -> name.equals(student.getFirstName()));
    }


    @Override
    public List<Student> findStudentsByLastName(final Collection<Student> students, final String name) {
        return findStudents(students, student -> name.equals(student.getLastName()));
    }


    @Override
    public List<Student> findStudentsByGroup(final Collection<Student> students, final GroupName group) {
        return findStudents(students, student -> group.equals(student.getGroup()));
    }


    @Override
    public Map<String, String> findStudentNamesByGroup(final Collection<Student> students, final GroupName group) {
        return filterCollectionStudents(students, student -> group.equals(student.getGroup()))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }
    //<? extends Collection<?>>

    private Stream<Map.Entry<GroupName, ArrayList<Student>>> getGroupStream(final Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toCollection(ArrayList::new)))
                .entrySet()
                .stream();
    }

    private List<Group> getGroups(final Collection<Student> students, final Function<ArrayList<Student>, List<Student>> function, final Collector<Group, ?, List<Group>> collector) {
        return getGroupStream(students).map(entry -> new Group(entry.getKey(), function.apply(entry.getValue())))
                .collect(collector);
    }



    @Override
    public List<Group> getGroupsByName(final Collection<Student> students) {
        return getGroups(students, this::sortStudentsByName, Collectors.toUnmodifiableList());
    }


    @Override
    public List<Group> getGroupsById(final Collection<Student> students) {
        return getGroups(students, this::sortStudentsById, Collectors.toUnmodifiableList());
    }

    private GroupName getLargest(final Collection<Student> students,
                                 final Function<List<Student>, ? extends Collection<?>> function,
                                 final Comparator<? super GroupName> comparatorForKey) {
        return getGroupStream(students).max(Comparator.comparingInt((Map.Entry<GroupName, ? extends List<Student>> group) -> function.apply(group.getValue()).size())
                .thenComparing(Map.Entry::getKey, comparatorForKey))
                .map(Map.Entry::getKey).orElse(null);
    }


    @Override
    public GroupName getLargestGroup(final Collection<Student> students) {
        return getLargest(students, s -> s, GroupName::compareTo);
    }


    @Override
    public GroupName getLargestGroupFirstName(final Collection<Student> students) {
        return getLargest(students, this::getDistinctFirstNames, Collections.reverseOrder(GroupName::compareTo));
    }
}
