package org.jetlinks.zlmedia.record;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class RecordFileInfo {

    private List<String> paths;

    private String rootPath;

}
