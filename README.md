# zlmedia4j

基于Projector Reactor封装的Java版ZLMediaKit客户端SDK.

## 依赖

```xml

<dependencies>
    <dependency>
        <groupId>org.jetlinks</groupId>
        <artifactId>zlmedia4j-native</artifactId>
        <version>1.0.1</version>
    </dependency>

    <dependency>
        <groupId>org.jetlinks</groupId>
        <artifactId>zlmedia4j-native</artifactId>
        <version>1.0.1</version>
        <!-- linux-x86_64、linux-aarch_64、osx-x86_64、osx-aarch_64、windows-x86_64 -->
        <classifier>linux-x86_64</classifier>
    </dependency>
</dependencies>

```

快照版本需要使用一下仓库配置
```xml

<repository>
    <id>jetlinks-nexus</id>
    <name>Nexus Release Repository</name>
    <url>https://nexus.jetlinks.cn/content/groups/public/</url>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
    </snapshots>
</repository>

```

## 使用

```java

public static void main(String[] args) {

    //配置信息
    ZLMediaConfigs configs = new ZLMediaConfigs();

    configs.getPorts().setRtsp(11554);
    configs.getPorts().setRtmp(11935);
    configs.getPorts().setSrt(19000);

    EmbeddedProcessMediaRuntime runtime = new EmbeddedProcessMediaRuntime(
        //zlmedia的工作目录
        "target/zlmedia",
        configs);

    //启动并阻塞等待
    runtime.start().block();

    // 详细请看 ZLMediaOperations 相关接口
    ZLMediaOperations operations = runtime.getOperations();



}

```
