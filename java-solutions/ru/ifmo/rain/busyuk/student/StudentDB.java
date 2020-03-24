package ru.ifmo.rain.busyuk.student;

import info.kgeorgiy.java.advanced.student.AdvancedStudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements AdvancedStudentGroupQuery {
    private final String EMPTY_STRING = "";
    private final Comparator<Student> COMPARATOR_BY_NAME = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName).thenComparing(Student::compareTo);
    private final Function<Student, String> GET_FULL_NAME = student -> student.getFirstName() + " " + student.getLastName();
    private final Function<Student, String> GET_FIRST_NAME = Student::getFirstName;

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return (students.stream().collect(Collectors.groupingBy(GET_FULL_NAME, Collectors.counting()))).entrySet()
                .stream().max(Comparator.comparingLong((ToLongFunction<Map.Entry<String, Long>>) Map.Entry::getValue).
                        thenComparing(Map.Entry::getKey)).map(Map.Entry::getKey).orElse(EMPTY_STRING);
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsByCriterion(students, COMPARATOR_BY_NAME);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsByCriterion(students, Student::compareTo);
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargestGroupByCriterion(students, List::size);
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupByCriterion(students, list -> getDistinctFirstNames(list).size());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getStudentsData(students, GET_FIRST_NAME);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getStudentsData(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getStudentsData(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getStudentsData(students, GET_FULL_NAME);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream().map(GET_FIRST_NAME).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Student::compareTo).map(GET_FIRST_NAME).orElse(EMPTY_STRING);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return getSortedList(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return getSortedList(students, COMPARATOR_BY_NAME);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findFilteredListOrderedByName(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findFilteredListOrderedByName(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findFilteredListOrderedByName(students, student -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students.stream().filter(student -> student.getGroup().equals(group)).
                collect(Collectors.toMap(Student::getLastName, Student::getFirstName, (s1, s2) ->
                        s1.compareTo(s2) < 0 ? s1 : s2));
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getMappedList(getEntryList(students.toArray(), indices), (student -> ((Student) student).getFirstName()));
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getMappedList(getEntryList(students.toArray(), indices), (student -> ((Student) student).getLastName()));
    }

    @Override
    public List<String> getGroups(Collection<Student> students, int[] indices) {
        return getMappedList(getEntryList(students.toArray(), indices), (student -> ((Student) student).getGroup()));
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getMappedList(getEntryList(students.toArray(), indices), (student -> GET_FULL_NAME.apply((Student) student)));
    }

    private Stream<Object> getEntryList(Object[] students, int[] indices) {
        return Arrays.stream(indices).mapToObj(i -> students[i]);
    }

    private List<String> getMappedList(Stream<Object> studentStream, Function<Object, String> function) {
        return studentStream.map(function).collect(Collectors.toList());
    }

    private List<Student> getSortedList(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream().sorted(comparator).collect(Collectors.toList());
    }

    private List<String> getStudentsData(Collection<Student> students, Function<Student, String> function) {
        return students.stream().map(function).collect(Collectors.toList());
    }

    private Stream<Map.Entry<String, List<Student>>> getGroupedStream(Collection<Student> students, Supplier<Map<String, List<Student>>> mapType) {
        return students.stream().collect(Collectors.groupingBy(Student::getGroup, mapType, Collectors.toList()))
                .entrySet().stream();
    }

    private List<Group> getGroupsByCriterion(Collection<Student> students, Comparator<Student> comparator) {
        return getGroupedStream(students, TreeMap::new).map(student -> new Group(student.getKey(),
                student.getValue().stream().sorted(comparator).collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    private String getLargestGroupByCriterion(Collection<Student> students, Function<List<Student>, Integer> sizeComparator) {
        return getGroupedStream(students, HashMap::new)
                .max(Comparator.comparingInt((Map.Entry<String, List<Student>> group) -> sizeComparator.
                        apply(group.getValue())).thenComparing(Map.Entry::getKey, Collections.reverseOrder
                        (String::compareTo))).map(Map.Entry::getKey).orElse(EMPTY_STRING);
    }

    private List<Student> findFilteredListOrderedByName(Collection<Student> students, Predicate<Student> predicate) {
        return students.stream().filter(predicate).sorted(COMPARATOR_BY_NAME).collect(Collectors.toList());
    }
}
