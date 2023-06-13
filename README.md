# ByBit JVM Client SDK written in Kotlin

NOTE: this library is in early development stages. There is NO WARRANTY.

File an issue if you'd to add more functionality!

**Maintainers wanted!** Pull requests greatly appreciated/wanted!


To use the SDK in a Gradle project:

```groovy

dependencies {
    implementation 'io.github.alleyway:bybit-kotlin-api:vX.Y.Z' 
}

```

To use the SDK in a Maven project:

```xml
<dependency>
    <groupId>io.github.alleyway</groupId>
    <artifactId>bybit-kotlin-api</artifactId>
    <version>vX.Y.Z</version>
</dependency>

```


## Developer's Notes

### Deployment

Make sure you get the right artifacts in your ~/.m2/repository/

```bash
./gradlew build publishToMavenLocal
```


### Publish to Sonatype

Remember to use the escape ("\") for special charactors of the password in the github secrets

```bash
./gradlew build publish --exclude-task test -i -PsonatypeUsername= -PsonatypePassword=
```
