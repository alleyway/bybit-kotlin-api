# ByBit JVM Client SDK written in Kotlin


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
