package model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private int teacherId;

    private String name;
    private String phone;

    @Column(name = "lesson_duration")
    private int lessonDuration;

    @Column(name = "lesson_price")
    private double lessonPrice = 150;

    @Column(name = "max_lessons")
    private Integer maxLessons = 5;

    @Column(name = "cancel_hours")
    private Integer cancelHours = 24;

    @Column(name = "booking_range")
    private Integer bookingRange = 7; 

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<Student> students;
    
    @Column(name = "reminder_hours")
    private Integer reminderHours = 2;
    
    @OneToMany(mappedBy = "teacher")
    private List<Message> messages;
    
    public Teacher() {}

    // Getters and Setters 
    public int getReminderHours() {
        return (reminderHours == null) ? 2 : reminderHours;
    }

    public void setReminderHours(int reminderHours) {
        this.reminderHours = reminderHours;
    }

    public int getBookingRange() {
        return (bookingRange == null) ? 7 : bookingRange;
    }

    public void setBookingRange(int bookingRange) {
        this.bookingRange = bookingRange;
    }

    public int getMaxLessons() {
        return (maxLessons == null) ? 5 : maxLessons;
    }

    public int getCancelHours() {
        return (cancelHours == null) ? 24 : cancelHours;
    }

    public void setMaxLessons(int maxLessons) {
        this.maxLessons = maxLessons;
    }

    public void setCancelHours(int cancelHours) {
        this.cancelHours = cancelHours;
    }
    
    public void setPhone(String phone) { this.phone = phone; }
    public String getPhone() { return this.phone; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLessonDuration() { return lessonDuration; }
    public void setLessonDuration(int lessonDuration) { this.lessonDuration = lessonDuration; }

    public double getLessonPrice() { return lessonPrice; }
    public void setLessonPrice(double lessonPrice) { this.lessonPrice = lessonPrice; }

    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }


}