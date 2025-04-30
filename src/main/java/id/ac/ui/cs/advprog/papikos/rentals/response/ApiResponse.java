package id.ac.ui.cs.advprog.papikos.rentals.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class ApiResponse<T> {

    private final int status;
    private final String message;
    private final T data;
    private final long timestamp;

    private ApiResponse(Builder<T> builder) {
        this.status = builder.status.value();
        this.message = builder.message;
        this.data = builder.data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private HttpStatus status;
        private String message;
        private T data;

        private Builder() {}

        public Builder<T> status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponse<T> build() {
            if (this.status == null) {
                this.status = HttpStatus.OK;
            }
            return new ApiResponse<>(this);
        }

        public ApiResponse<T> ok(T data) {
            return this.status(HttpStatus.OK)
                    .message("Success")
                    .data(data)
                    .build();
        }

        public ApiResponse<T> created(T data) {
            return this.status(HttpStatus.CREATED)
                    .message("Resource created successfully")
                    .data(data)
                    .build();
        }

        public ApiResponse<T> badRequest(String message) {
            return this.status(HttpStatus.BAD_REQUEST)
                    .message(message)
                    .build(); // Type cast needed if T is different
        }

        public ApiResponse<T> notFound(String message) {
            return this.status(HttpStatus.NOT_FOUND)
                    .message(message)
                    .build();
        }

        public ApiResponse<T> internalError(String message) {
            return this.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message(message)
                    .build();
        }
    }
}