package model;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "student_id")
	private int studentId; 
    private String name;
    private String phone;
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;
    
    @OneToMany(mappedBy = "student")
    private List<Message> messages;
    
	public Student() { }
	
	// Getters and Setters 
	public int getStudentId() {
		return studentId;
	}
	public void setStudentId(int studentId) {
		this.studentId = studentId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public Teacher getTeacher() {
		return teacher;
	}
	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}
	
}