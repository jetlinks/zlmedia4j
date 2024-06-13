package org.jetlinks.zlmedia.record;

import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class GetMp4RecordRequest {

    private String app;

    private String stream;

    private String period;

    private String customizedPath;

}
