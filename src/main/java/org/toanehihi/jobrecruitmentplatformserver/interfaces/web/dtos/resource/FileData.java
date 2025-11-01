package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource;

import lombok.Data;

@Data
public class FileData {
    private byte[] content;

    public FileData(byte[] fileBytes) {
        this.content = fileBytes;
    }
}
