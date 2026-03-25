package com.crs.entity;

import java.util.ArrayList;
import java.util.List;

public class Student extends User {

    public enum RecoveryStatus {
        NOT_STARTED, IN_PROGRESS, MILESTONE_DUE, COMPLETED, WITHDRAWN
    }

    private String program;
    private int currentLevel;
    private String currentSemester;
    private double cgpa;
    private RecoveryStatus recoveryStatus = RecoveryStatus.NOT_STARTED;
    private String recoveryPlanDetails;
    private List<String> pendingMilestones = new ArrayList<>();
    private String advisorEmail;

    public Student(int userId, String name, String email, String program, int currentLevel, String semester) {
        super(userId, name, email, "");
        setRole(Role.STUDENT);
        this.program = program;
        this.currentLevel = currentLevel;
        this.currentSemester = semester;
    }

    public String getProgram() { return program; }
    public int getCurrentLevel() { return currentLevel; }
    public String getCurrentSemester() { return currentSemester; }
    public double getCgpa() { return cgpa; }
    public RecoveryStatus getRecoveryStatus() { return recoveryStatus; }
    public String getRecoveryPlanDetails() { return recoveryPlanDetails; }
    public List<String> getPendingMilestones() { return pendingMilestones; }
    public String getAdvisorEmail() { return advisorEmail; }

    public void setProgram(String p) { this.program = p; }
    public void setCurrentLevel(int l) { this.currentLevel = l; }
    public void setCurrentSemester(String s) { this.currentSemester = s; }
    public void setCgpa(double c) { this.cgpa = c; }
    public void setRecoveryStatus(RecoveryStatus r) { this.recoveryStatus = r; }
    public void setRecoveryPlanDetails(String d) { this.recoveryPlanDetails = d; }
    public void setPendingMilestones(List<String> m) { this.pendingMilestones = m != null ? m : new ArrayList<>(); }
    public void addMilestone(String m) { if (m != null) pendingMilestones.add(m); }
    public void setAdvisorEmail(String e) { this.advisorEmail = e; }

    public boolean hasPendingMilestones() {
        return pendingMilestones != null && !pendingMilestones.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Student{id=%d, name='%s', program='%s', cgpa=%.2f}",
            getUserId(), getFullName(), program, cgpa);
    }
}
