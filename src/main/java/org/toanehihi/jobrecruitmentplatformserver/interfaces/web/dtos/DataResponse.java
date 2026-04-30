package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataResponse<T> {
    @Builder.Default
    private int code = ResponseCode.SUCCESS;
    private String message;
    private T data;
}
