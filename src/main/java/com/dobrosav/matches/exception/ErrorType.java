package com.dobrosav.matches.exception;

public enum ErrorType {

    UNRESOLVED_ERROR("100", "General error"),
    NOT_SUPPORTED_HTTP_METHOD("101", "Not supported http method"),
    NOT_VALID_REQUEST_FORMAT("103", "Not valid request format"),
    INVALID_ARGUMENT("104", "Invalid fields in the request"),
    NOT_FOUND_POST("105", "Post not found"),
    NOT_FOUND_COMMENT("106", "Comment not found"),
    CLIENT_NOT_AVAILABLE("107", "Client not available"),
    USER_ALREADY_EXISTS("108", "User already exists"),
    USER_NOT_FOUND("109", "User not found" ),
    MAX_IMAGES_REACHED("110", "User has reached the maximum number of images"),
    IMAGE_NOT_FOUND("111", "Image not found"),
    INVALID_IMAGE("112", "Invalid image file"),
    ALREADY_LIKED("113", "User already liked"),
    CANNOT_LIKE_SELF("114", "User cannot like themselves"),
    DAILY_LIKE_LIMIT_REACHED("115", "Daily like limit reached for non-premium users"),
    PREMIUM_FEATURE_ONLY("116", "This feature is available for premium users only");

    private final String code;
    private final String message;

    ErrorType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}