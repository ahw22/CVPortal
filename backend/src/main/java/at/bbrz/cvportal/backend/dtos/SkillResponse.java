package at.bbrz.cvportal.backend.dtos;

import at.bbrz.cvportal.backend.entities.SkillLevel;

public record SkillResponse(Long id, String name, SkillLevel level, int sortOrder) {
}
