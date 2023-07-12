# ByBit Java API Client SDK (written in Kotlin)

NOTE: this library is in early development stages. There is NO WARRANTY.

File an issue if you'd to add more functionality!

**Maintainers wanted!** Pull requests greatly appreciated/wanted!


To use the SDK in a Gradle project:

```groovy

dependencies {
    implementation 'io.github.alleyway:bybit-kotlin-api:0.4.3' 
}

```

To use the SDK in a Maven project:

```xml
<dependency>
    <groupId>io.github.alleyway</groupId>
    <artifactId>bybit-kotlin-api</artifactId>
    <version>0.4.3</version>
</dependency>

```


Look in the *sample* project for examples in Java or Kotlin.


## Example code

Here's a little example to get you started... 

https://github.com/alleyway/bybit-kotlin-api/blob/3f61bcce303a8741ebe0547ade39366091b10676/sample/src/main/java/bybit/sdk/sample/KotlinUsageSample.kt#L21-L27


## Developer's Notes

### Deployment

Make sure you get the right artifacts in your ~/.m2/repository/

```bash
./gradlew build publishToMavenLocal
```


### Publish to Sonatype

Remember to use the escape ("\") for special charactors of the password in the github secrets

```bash
./gradlew build sign publish --exclude-task test -i -PsonatypeUsername= -PsonatypePassword=
```

### Release non-snapshot
 - update readme versions
 - commit non-snapshot version
 - create tag of version
 - run gradlew build sign publish locally

