package org.jetlinks.zlmedia.restful.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.zlmedia.record.GetMp4RecordRequest;
import org.jetlinks.zlmedia.record.RecordFileInfo;
import org.jetlinks.zlmedia.restful.RestfulRequest;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetMp4RecordFile implements RestfulRequest<RecordFileInfo> {

    private GetMp4RecordRequest request;

    @Override
    public Object params() {
        return request;
    }

}
