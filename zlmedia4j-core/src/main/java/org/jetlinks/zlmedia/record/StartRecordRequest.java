package org.jetlinks.zlmedia.record;

import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class StartRecordRequest {

    /**
     * 0为hls，1为mp4
     */
    private Integer type = 1;
    /**
     * 应用名
     */
    private String app;
    /**
     * 流ID
     */
    private String stream;
    /**
     * 录像保存路径
     */
    private String customizedPath;

    /**
     * mp4录像切片时间大小,单位秒，置0则采用配置项
     */
    private Integer maxSecond;

}
