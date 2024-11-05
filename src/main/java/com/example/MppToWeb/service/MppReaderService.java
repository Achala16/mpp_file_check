package com.example.MppToWeb.service;

import com.example.MppToWeb.model.Task;
import com.example.MppToWeb.model.Resource;
import com.example.MppToWeb.repository.TaskRepository;
import com.example.MppToWeb.repository.ResourceRepository;
import net.sf.mpxj.*;
import net.sf.mpxj.reader.ProjectReader;
import net.sf.mpxj.mpp.MPPReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MppReaderService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    public void processMppFile(MultipartFile file) throws Exception {
        File tempFile = File.createTempFile("uploaded-", ".mpp");
        file.transferTo(tempFile);

        try {
            ProjectReader reader = new MPPReader();
            ProjectFile projectFile = reader.read(tempFile);

            // Process resources (unchanged)
            processResources(projectFile);

            // Process tasks with parent-child relationships
            processTasks(projectFile);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private void processResources(ProjectFile projectFile) {
        for (net.sf.mpxj.Resource mpxjResource : projectFile.getResources()) {
            if (mpxjResource.getUniqueID() != null) {
                try {
                    Resource resource = new Resource();
                    resource.setResourceId(mpxjResource.getUniqueID());
                    resource.setName(mpxjResource.getName());
                    resource.setEmail(mpxjResource.getEmailAddress());
                    resource.setStandardRate(mpxjResource.getStandardRate() != null ?
                            mpxjResource.getStandardRate().getAmount() : null);
                    resource.setNotes(mpxjResource.getNotes());
                    resourceRepository.save(resource);
                } catch (Exception e) {
                    System.err.println("Error processing resource: " + mpxjResource.getName() +
                            " - " + e.getMessage());
                }
            }
        }
    }

    private void processTasks(ProjectFile projectFile) {
        // Map to store tasks by their unique IDs for establishing relationships later
        Map<Integer, Task> taskMap = new HashMap<>();

        // First pass: Create all tasks
        for (net.sf.mpxj.Task mpxjTask : projectFile.getTasks()) {
            if (mpxjTask.getUniqueID() != null) {
                try {
                    Task task = createTaskFromMpxj(mpxjTask, projectFile);
                    taskMap.put(mpxjTask.getUniqueID(), task);
                } catch (Exception e) {
                    System.err.println("Error processing task: " + mpxjTask.getName() +
                            " - " + e.getMessage());
                }
            }
        }

        // Second pass: Establish parent-child relationships
        for (net.sf.mpxj.Task mpxjTask : projectFile.getTasks()) {
            if (mpxjTask.getUniqueID() != null) {
                Task task = taskMap.get(mpxjTask.getUniqueID());
                net.sf.mpxj.Task parentMpxjTask = mpxjTask.getParentTask();

                if (parentMpxjTask != null && parentMpxjTask.getUniqueID() != null) {
                    Task parentTask = taskMap.get(parentMpxjTask.getUniqueID());
                    if (parentTask != null) {
                        task.setParent(parentTask);
                        parentTask.addSubtask(task);
                    }
                }
            }
        }

        // Save all tasks
        for (Task task : taskMap.values()) {
            if (task.getParent() == null) {  // Save only root tasks, cascading will handle children
                taskRepository.save(task);
            }
        }
    }

    private Task createTaskFromMpxj(net.sf.mpxj.Task mpxjTask, ProjectFile projectFile) {
        Task task = new Task();
        task.setTaskId(mpxjTask.getUniqueID());
        task.setName(mpxjTask.getName());
        task.setStartDate(mpxjTask.getStart());
        task.setFinishDate(mpxjTask.getFinish());
        task.setDuration(mpxjTask.getDuration() != null ?
                mpxjTask.getDuration().getDuration() : null);
        task.setPercentageComplete(mpxjTask.getPercentageComplete() != null ?
                mpxjTask.getPercentageComplete().intValue() : null);
        task.setNotes(mpxjTask.getNotes());

        // Set outline level and number
        task.setOutlineLevel(mpxjTask.getOutlineLevel());
        task.setOutlineNumber(mpxjTask.getOutlineNumber());

        // Store predecessors
        String predecessors = mpxjTask.getPredecessors().stream()
                .map(rel -> String.valueOf(rel.getTargetTask().getUniqueID()))
                .collect(Collectors.joining(","));
        task.setPredecessors(predecessors);

        // Store assigned resources
        StringBuilder resourcesList = new StringBuilder();
        ResourceAssignmentContainer assignments = projectFile.getResourceAssignments();
        boolean first = true;

        for (ResourceAssignment assignment : assignments) {
            if (assignment.getTask() != null &&
                    assignment.getTask().getUniqueID().equals(mpxjTask.getUniqueID()) &&
                    assignment.getResource() != null) {

                if (!first) {
                    resourcesList.append(",");
                }
                resourcesList.append(assignment.getResource().getUniqueID());
                first = false;
            }
        }

        task.setResources(resourcesList.toString());
        return task;
    }
}