package com.its.tugas4sqlite.model;

import java.io.Serializable;

/**
 * Model class merepresentasikan data Task (Tugas/Pekerjaan).
 * Mengimplementasikan Serializable agar dapat dipassing antar Activity/Fragment jika diperlukan.
 */
public class Task implements Serializable {
    private long id;
    private String title;
    private String description;
    private String category;
    private String priority; // "Tinggi", "Sedang", "Rendah"
    private String dueDate;  // Format "YYYY-MM-DD"
    private double budget;   // Tipe REAL pada SQLite
    private boolean isCompleted; // Tipe INTEGER (0 / 1) pada SQLite
    private String createdAt;

    public Task() {
    }

    public Task(String title, String description, String category, String priority,
                String dueDate, double budget, boolean isCompleted, String createdAt) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.budget = budget;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    public Task(long id, String title, String description, String category, String priority,
                String dueDate, double budget, boolean isCompleted, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.budget = budget;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
