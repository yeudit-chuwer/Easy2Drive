package model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private int messageId;

    @ManyToOne
    @JoinColumn(name = "teacher_id") 
    private Teacher teacher;
    
    
    @ManyToOne
    @JoinColumn(name = "student_id") 
    private Student student;

    @Column(name = "content")
    private String content;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();
    
    public Message() {}

    // Getters and Setters
    public int getMessageId() { return messageId; }
    
    public void setMessageId(int messageId) { this.messageId = messageId; }
    
    public Teacher getTeacher() { 
        return teacher; 
    }
    
    public void setTeacher(Teacher teacher) { 
        this.teacher = teacher; 
    }

    public Student getStudent() { 
        return student; 
    }
    
    public void setStudent(Student student) { 
        this.student = student; 
    }
    public String getContent() { return content; }
    
    public void setContent(String content) { this.content = content; }

    public boolean isRead() { return isRead; }
    
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getSentAt() { return sentAt; }
    
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
}