package com.courselylabs.courselylab.exception;

import java.util.List;

import com.courselylabs.courselylab.dto.BlockedPrerequisiteDTO;

public class PrerequisiteConflictException extends RuntimeException {

    private final List<BlockedPrerequisiteDTO> blockedPrerequisites;

    public PrerequisiteConflictException(String message, List<BlockedPrerequisiteDTO> blockedPrerequisites) {
        super(message);
        this.blockedPrerequisites = blockedPrerequisites;
    }

    public List<BlockedPrerequisiteDTO> getBlockedPrerequisites() {
        return blockedPrerequisites;
    }
}