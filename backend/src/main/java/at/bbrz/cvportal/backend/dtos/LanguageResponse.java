package at.bbrz.cvportal.backend.dtos;

import at.bbrz.cvportal.backend.entities.LanguageLevel;

public record LanguageResponse(Long id, String language, LanguageLevel level, int sortOrder) {
}