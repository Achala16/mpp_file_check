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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Service
public class MppReaderService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    public void processMppFile(MultipartFile file) throws Exception {
        // Create a temporary file to store the uploaded MPP file
        File tempFile = File.createTempFile("uploaded-", ".mpp");
        file.transferTo(tempFile); // Save the MultipartFile to the temporary file

        try {
            ProjectReader reader = new MPPReader();
            ProjectFile projectFile = reader.read(tempFile);

            // Process and save resources
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

            // Process and save tasks
            for (net.sf.mpxj.Task mpxjTask : projectFile.getTasks()) {
                if (mpxjTask.getUniqueID() != null) {
                    try {
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

                        // Store predecessors as comma-separated list
                        String predecessors = mpxjTask.getPredecessors().stream()
                                .map(rel -> String.valueOf(rel.getTargetTask().getUniqueID()))
                                .collect(Collectors.joining(","));
                        task.setPredecessors(predecessors);

                        // Store assigned resources using ResourceAssignments
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
                        taskRepository.save(task);

                    } catch (Exception e) {
                        System.err.println("Error processing task: " + mpxjTask.getName() +
                                " - " + e.getMessage());
                    }
                }
            }
        } finally {
            // Clean up the temporary file
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
