package at.bbrz.cvportal.frontend.dtos;

/**
 * {@code level} bleibt String - das Frontend zeigt die Stufe nur an.
 */
public record SkillResponse(Long id, String name, String level, int sortOrder) {
}