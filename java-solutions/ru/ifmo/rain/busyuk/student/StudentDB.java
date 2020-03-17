package ru.ifmo.rain.busyuk.student;

import info.kgeorgiy.java.advanced.student.AdvancedStudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements AdvancedStudentGroupQuery {
    private final String EMPTY_STRING = "";
    private final Comparator<Student> comparatorByName = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName).thenComparing(Student::compareTo);
    private final Function<Student, String> getFullName = student -> student.getFirstName() + " " + student.getLastName();
    private final Function<Student, String> getFirstName = Student::getFirstName;

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return groupStudentByAttribute(students, getFullName).entrySet().stream()
                .max(Comparator.comparing((Map.Entry<String, List<Student>> entry) ->
                        entry.getValue().stream().map(Student::getGroup).distinct().count())
                        .thenComparing(Map.Entry::getKey)).map(Map.Entry::getKey).orElse(EMPTY_STRING);
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsByCriteria(students, comparatorByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsByCriteria(students, Student::compareTo);
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargestGroupBuCriteria(students, List::size);
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupBuCriteria(students, list -> getDistinctFirstNames(list).size());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getAnyStudentsList(students, getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getAnyStudentsList(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getAnyStudentsList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getAnyStudentsList(students, getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream().map(getFirstName).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Student::compareTo).map(getFirstName).orElse(EMPTY_STRING);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return getSortedList(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return getSortedList(students, comparatorByName);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsByCriteria(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsByCriteria(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentsByCriteria(students, student -> student.getGroup().equals(group));
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
        return getMappedList(getEntryList(students.toArray(), indices), (student -> getFullName.apply((Student) student)));
    }

    private <E> Map<E, List<Student>> groupStudentByAttribute(final Collection<Student> students, final Function<Student, E> function) {
        return students.stream().collect(Collectors.groupingBy(function));
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

    private List<String> getAnyStudentsList(Collection<Student> students, Function<Student, String> function) {
        return students.stream().map(function).collect(Collectors.toList());
    }

    private Stream<Map.Entry<String, List<Student>>> getGroupedStream(Collection<Student> students, Supplier<Map<String, List<Student>>> mapType) {
        return students.stream().collect(Collectors.groupingBy(Student::getGroup, mapType, Collectors.toList()))
                .entrySet().stream();
    }

    private List<Group> getGroupsByCriteria(Collection<Student> students, Comparator<Student> comparator) {
        return getGroupedStream(students, TreeMap::new).map(student -> new Group(student.getKey(),
                student.getValue().stream().sorted(comparator).collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    private String getLargestGroupBuCriteria(Collection<Student> students, Function<List<Student>, Integer> sizeComparator) {
        return getGroupedStream(students, HashMap::new)
                .max(Comparator.comparingInt((Map.Entry<String, List<Student>> group) -> sizeComparator.
                        apply(group.getValue())).thenComparing(Map.Entry::getKey, Collections.reverseOrder
                        (String::compareTo))).map(Map.Entry::getKey).orElse(EMPTY_STRING);
    }

    private List<Student> findStudentsByCriteria(Collection<Student> students, Predicate<Student> predicate) {
        return students.stream().filter(predicate).sorted(comparatorByName).collect(Collectors.toList());
    }
}
