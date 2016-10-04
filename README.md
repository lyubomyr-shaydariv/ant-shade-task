What is it?
===

`ant-shade-task` is a port of [Apache Maven](https://maven.apache.org/) [Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/) (or `maven-shade-plugin`) for [Ant](http://ant.apache.org/).

Requirements
---

* Use: Java 8 and Apache Ant.
* Build: Java 8 and Apache Maven.

How to use
===

Build with:

* Build with `mvn clean package`.
* Pick `ant-shade-task-0.1-SNAPSHOT.jar` or `ant-shade-task-0.1-SNAPSHOT-jar-with-dependencies.jar` from the `target` directory and copy it to elsewhere.

Register the picked up JAR in `build.xml`:

```
<taskdef
	classpath="ant-shade-task-0.1-SNAPSHOT-jar-with-dependencies.jar"
	resource="org/apache/tools/ant/taskdefs/shade.properties"
/>
```

And then apply (an adapted example taken from the Maven Shade Plugin [Relocating Classes example](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html)):

```
<shade uberJar="foo-bar-shaded.jar">
	<jar path="foo-bar.jar"/>
	<relocation pattern="org.codehaus.plexus.util" shadedPattern="org.shaded.plexus.util">
		<exclude value="org.codehaus.plexus.util.xml.Xpp3Dom"/>
		<exclude value="org.codehaus.plexus.util.xml.pull.*"/>
	</relocation>
</shade>
```

The snippet above is an Ant adaptation of the following Maven Shade Plugin configuration snippet:

```
<relocations>
	<relocation>
	<pattern>org.codehaus.plexus.util</pattern>
		<shadedPattern>org.shaded.plexus.util</shadedPattern>
		<excludes>
			<exclude>org.codehaus.plexus.util.xml.Xpp3Dom</exclude>
			<exclude>org.codehaus.plexus.util.xml.pull.*</exclude>
		</excludes>
	</relocation>
</relocations>
```

Version history
===

Nothing tagged yet.

Known issues
===

* Only `<relocations>` are exposed through the `<shade>` task so far.
* `ant-shade-task-0.1-SNAPSHOT.jar` does not include any dependencies and the dependencies should be picked up either manually or using `ant-shade-task-0.1-SNAPSHOT-jar-with-dependencies.jar` that's currently large.
* Not a part of the standard Apache Ant taskdef library. Currently it occupies the `org.apache.tools.ant.taskdefs.Shade` class, but probably should be moved to another non-Apache package.
