Maven LESS css plugin
=====================

Another [LESS](http://lesscss.org/) plugin for Maven.  This one is based on the plugin by [Yevgeniy Melnichuk](http://code.google.com/p/maven-less-plugin/) with the added bonus of being hosted in a public Maven repository, a little kinder on memory consumption and being able to detect circular dependencies in your stylesheets (e.g. A imports B imports C imports A).

How to
------

1 Add the plugin repository to your pom.xml file:

	<pluginRepositories>
		<pluginRepository>
			<id>achingbrain-releases</id>
			<url>http://achingbrain.github.com/maven-repo/releases</url>
		</pluginRepository>
		<pluginRepository>
			<id>achingbrain-snapshots</id>
			<url>http://achingbrain.github.com/maven-repo/snapshots</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</pluginRepository>
	</pluginRepositories>


2 Add the plug in:

	<build>
		<plugins>
			<plugin>
				<groupId>net.achingbrain</groupId>
				<artifactId>less-maven-plugin</artifactId>
				<version>1.0.2</version>
			</plugin>
		</plugins>
	</build>


By default the plugin will recursively search the contents of your src/main/webapp directory for .less files, compile them as .css files and put them in your project's output directory at the same path.  So:

	src/main/webapp/css/foo.less

will become

	target/ROOT/css/foo.css

and

	src/main/webapp/css/someDirectory/bar.less

will become

	target/ROOT/css/someDirectory/bar.css

Configuration
-------------

You can specify the input & output directories, as well as a file include/exclude mask.  This will allow you to compile files that do not have the .less extension, if you wish:

	<build>
		<plugins>
			<plugin>
				<groupId>net.achingbrain</groupId>
				<artifactId>less-maven-plugin</artifactId>
				<version>1.0.2</version>
				<configuration>
					<inputDirectory>${project.basedir}/src/main/webapp/css/static</inputDirectory>
					<includes>
						<include>**/*.less</include>
						<include>**/*.css</include>
					</includes>
					<outputDirectory>${project.build.directory}/css/less</outputDirectory>
				</configuration>
			</plugin>
		</plugins>
	</build>

Phases
------

By default the plugin executes in the [compile phase](http://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#Lifecycle_Reference).  To change this, specify an execution blocks:

	<build>
		<plugins>
			<plugin>
				<groupId>net.achingbrain</groupId>
				<artifactId>less-maven-plugin</artifactId>
				<version>1.0.2</version>
				<executions>
					<execution>
						<id>Runs in process-sources phase</id>
						<phase>process-sources</phase>
						<goals>
							<goal>compile</goal>
						</goals>
						<configuration>
							<includes>
								<include>**/*.less</include>
								<include>**/*.css</include>
							</includes>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>

Multiple executions
-------------------

To execute multiple times, simply specify multiple execution blocks

	<build>
		<plugins>
			<plugin>
				<groupId>net.achingbrain</groupId>
				<artifactId>less-maven-plugin</artifactId>
				<version>1.0.2</version>
				<executions>
					<execution>
						<id>Compiles from folder A</id>
						<phase>compile</phase>
						<goals>
							<goal>compile</goal>
						</goals>
						<configuration>
							<inputDirectory>${project.basedir}/src/main/webapp/css/folderA</inputDirectory>
						</configuration>
					</execution>
					<execution>
						<id>Compiles from folder B</id>
						<phase>compile</phase>
						<goals>
							<goal>compile</goal>
						</goals>
						<configuration>
							<inputDirectory>${project.basedir}/src/main/webapp/css/folderB</inputDirectory>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>

Specifying an alternate version of env.js or less.js
------------------------------------------

env.js 1.2.13 and less.js 1.1.5 is bundled with this plugin, but you can specify alternate versions in case new ones have been released.  To do this, configure the lessPath and/or envPath plugin parameters:

	<build>
			<plugins>
				<plugin>
					<groupId>net.achingbrain</groupId>
					<artifactId>less-maven-plugin</artifactId>
					<version>1.0.2</version>
					<configuration>
						<lessPath>${project.basedir}/src/main/resources/less.js</lessPath>
						<envPath>${project.basedir}/src/main/resources/env.js</envPath>
					</configuration>
			</plugin>
		</plugins>
	</build>

That's it!
