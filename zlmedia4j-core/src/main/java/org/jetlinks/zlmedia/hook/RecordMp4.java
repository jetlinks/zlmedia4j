package org.jetlinks.zlmedia.hook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordMp4 extends HookEvent<HookResponse> {

    private String fileName;

    private String filePath;

    private long fileSize;

    private String folder;

    private long startTime;

    private String stream;

    private float timeLen;

    private String url;

}
