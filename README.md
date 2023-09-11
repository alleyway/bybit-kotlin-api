# ByBit jvm API Client SDK (written in Kotlin)

NOTE: this library is in early development stages. There is NO WARRANTY.

File an issue if you'd to add more functionality!

**Maintainers wanted!** Pull requests greatly appreciated/wanted!


To use the SDK in a Gradle project:

```groovy

dependencies {
    implementation 'io.github.alleyway:bybit-kotlin-api:0.4.6' 
}

```

To use the SDK in a Maven project:

```xml
<dependency>
    <groupId>io.github.alleyway</groupId>
    <artifactId>bybit-kotlin-api</artifactId>
    <version>0.4.6</version>
</dependency>

```


Look in the *sample* project for examples in Java or Kotlin.


## Example code

Here's a little example to get you started... 

https://github.com/alleyway/bybit-kotlin-api/blob/70d4b83dc8d1015a7fab04d0f1cc27bb0dcc34f3/sample/src/main/java/bybit/sdk/sample/KotlinUsageSample.kt#L21-L26

## Developer's Notes

### Deployment

Make sure you get the right artifacts in your ~/.m2/repository/

```bash
./gradlew build publishToMavenLocal
```


### Publish to Sonatype

Remember to use the escape ("\") for special charactors of the password in the github secrets

**Publish Command:**

```bash
./gradlew build sign publish --exclude-task test -i -PsonatypeUsername= -PsonatypePassword=
```

### Release non-snapshot
 - update readme versions and Version.kt file
 - commit non-snapshot version
 - create Release & tag of version in github
 - run _Publish Command_ above - check _local.properties_ for more info 

