package com.example.MppToWeb.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "task_name")
    private String name;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "finish_date")
    private Date finishDate;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "percentage_complete")
    private Integer percentageComplete;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "predecessors")
    private String predecessors;

    @Column(name = "resources")
    private String resources;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Task parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<Task> subtasks = new HashSet<>();

    @Column(name = "outline_level")
    private Integer outlineLevel;

    @Column(name = "outline_number")
    private String outlineNumber;

    // Default constructor
    public Task() {
        this.subtasks = new HashSet<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Integer getPercentageComplete() {
        return percentageComplete;
    }

    public void setPercentageComplete(Integer percentageComplete) {
        this.percentageComplete = percentageComplete;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPredecessors() {
        return predecessors;
    }

    public void setPredecessors(String predecessors) {
        this.predecessors = predecessors;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public Task getParent() {
        return parent;
    }

    public void setParent(Task parent) {
        this.parent = parent;
    }

    public Set<Task> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(Set<Task> subtasks) {
        this.subtasks = subtasks;
    }

    public Integer getOutlineLevel() {
        return outlineLevel;
    }

    public void setOutlineLevel(Integer outlineLevel) {
        this.outlineLevel = outlineLevel;
    }

    public String getOutlineNumber() {
        return outlineNumber;
    }

    public void setOutlineNumber(String outlineNumber) {
        this.outlineNumber = outlineNumber;
    }

    // Helper method to add a subtask
    public void addSubtask(Task subtask) {
        subtasks.add(subtask);
        subtask.setParent(this);
    }

    // Helper method to remove a subtask
    public void removeSubtask(Task subtask) {
        subtasks.remove(subtask);
        subtask.setParent(null);
    }
}