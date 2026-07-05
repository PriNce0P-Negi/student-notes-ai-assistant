package com.prince.notesai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a Qdrant payload filter used in search requests.
 *
 * Qdrant filter JSON structure:
 * {
 *   "must": [
 *     { "key": "sessionId",  "match": { "value": "abc-123" } },
 *     { "key": "documentId", "match": { "any": [1, 2, 3] } }
 *   ]
 * }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QdrantFilter {

    /** All conditions in this list must be satisfied (AND logic). */
    private List<Condition> must;

    // ── Condition ─────────────────────────────────────────────────────

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Condition {

        /** The payload field key (e.g. "sessionId", "documentId"). */
        private String key;

        /** The match rule for this condition. */
        private Match match;
    }

    // ── Match ─────────────────────────────────────────────────────────

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)   // omits null fields in JSON
    public static class Match {

        /**
         * Exact-value match.
         * e.g. { "value": "session-abc-123" }
         */
        private Object value;

        /**
         * Any-of match (for lists).
         * e.g. { "any": [1, 2, 3] }
         */
        private List<?> any;

        // ── Static factories ──────────────────────────────────────────

        public static Match exact(Object value) {
            return new Match(value, null);
        }

        public static Match anyOf(List<?> values) {
            return new Match(null, values);
        }
    }
}
