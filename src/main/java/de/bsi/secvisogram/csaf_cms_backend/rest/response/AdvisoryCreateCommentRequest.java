package de.bsi.secvisogram.csaf_cms_backend.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdvisoryCreateComment")
public class AdvisoryCreateCommentRequest {

    @Schema(
            description = "A comment is added to an object in the CSAF document." +
                    " This name specifies the concrete value and its path in the object the comment belongs to." +
                    " When it is empty, the comment belongs to the whole object.",
            example = "document.csaf_version"
    )
    private final String fieldName;

    @Schema(description = "The text of the comment.")
    private final String commentText;

    public AdvisoryCreateCommentRequest(String fieldName, String commentText) {
        this.fieldName = fieldName;
        this.commentText = commentText;
    }

    @Schema(
            description = "A comment is added to an object in the CSAF document." +
                    " This name specifies the concrete value and its path in the object the comment belongs to." +
                    " When its empty, the comment belongs to the whole object.",
            example = "document.csaf_version"
    )
    public String getFieldName() {
        return fieldName;
    }

    @Schema(description = "The text of the comment.", example = "Is this value correct?")
    public String getCommentText() {
        return commentText;
    }
}