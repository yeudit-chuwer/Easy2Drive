package model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "work_days")
public class WorkDay {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne @JoinColumn(name = "teacher_id")
    private Teacher teacher;
    private LocalDate work_date;
    private LocalTime start_time;
    private LocalTime end_time;
    private String break_start; 
    private int break_duration; 
    
	public WorkDay() { }
    
    // Getters and Setters 
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Teacher getTeacher() {
		return teacher;
	}
	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}
	public LocalDate getWork_date() {
		return work_date;
	}
	public void setWork_date(LocalDate work_date) {
		this.work_date = work_date;
	}
	public LocalTime getStart_time() {
		return start_time;
	}
	public void setStart_time(LocalTime start_time) {
		this.start_time = start_time;
	}
	public LocalTime getEnd_time() {
		return end_time;
	}
	public void setEnd_time(LocalTime end_time) {
		this.end_time = end_time;
	}
	
	public String getBreak_start() {
		return break_start;
	}
	public void setBreak_start(String break_start) {
		this.break_start = break_start;
	}
	public int getBreak_duration() {
		return break_duration;
	}
	public void setBreak_duration(int break_duration) {
		this.break_duration = break_duration;
	} 

}