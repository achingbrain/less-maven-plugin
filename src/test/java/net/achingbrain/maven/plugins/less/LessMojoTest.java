package net.achingbrain.maven.plugins.less;

import org.apache.maven.model.PatternSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class LessMojoTest {
	private LessMojo mojo;
	private Log log;
	private Map pluginContext;

	@Before
	public void setUp() throws Exception {
		log = mock(Log.class);
		pluginContext = mock(Map.class);

		mojo = new LessMojo();
		mojo.setLog(log);
		mojo.setPluginContext(pluginContext);
	}

	@Test
	public void testFindFiles() throws Exception {
		mojo.setInputDirectory(new File("src/test/resources/net/achingbrain/maven/plugins/less"));

		// only one .less file
		assertEquals(1, mojo.findFiles().size());
	}

	@Test
	public void testFindFiles_specifyIncludes() throws Exception {
		PatternSet patternSet = new PatternSet();
		patternSet.addInclude("**/*.less");
		patternSet.addInclude("**/*.css");

		mojo.setInputDirectory(new File("src/test/resources/net/achingbrain/maven/plugins/less"));
		mojo.setIncludes(patternSet);

		// should get two .css files (one in subdirectory) and one .less file
		assertEquals(3, mojo.findFiles().size());
	}

	@Test
	public void testFindFiles_ensureDirectoryExists() throws Exception {
		String path = UUID.randomUUID().toString();

		assertFalse(new File(System.getProperty("java.io.tmpdir") + "/" + path).exists());

		File file = new File(System.getProperty("java.io.tmpdir") + "/" + path + "/bar");

		mojo.ensureDirectoryExists(file);

		assertTrue(new File(System.getProperty("java.io.tmpdir") + "/" + path).exists());
	}

	@Test
	public void testExecute() throws Exception {
		PatternSet patternSet = new PatternSet();
		patternSet.addInclude("**/*.less");
		patternSet.addInclude("**/*.css");

		mojo.setInputDirectory(new File("src/test/resources/net/achingbrain/maven/plugins/less"));
		mojo.setIncludes(patternSet);
		mojo.setOutputDirectory(new File(System.getProperty("java.io.tmpdir")));
		mojo.setEnvPath("env-1.2.13.js");
		mojo.setLessPath("less-1.1.5.min.js");

		mojo.execute();

		// the method under test
		try {
			mojo.execute();
		} catch (MojoExecutionException e) {

		}
	}

	@Test
	public void testIncludeIncludedFiles() throws Exception {
		File input = new File("src/test/resources/net/achingbrain/maven/plugins/less/css/myfile.css");
		String output = mojo.includeIncludedFiles(input);

		assertTrue(output.contains("color: red"));
	}

	@Test
	public void testExecute_integration() throws Exception {
		PatternSet patternSet = new PatternSet();
		patternSet.addInclude("**/*.less");
		patternSet.addInclude("**/*.css");

		String path = UUID.randomUUID().toString();

		mojo.setInputDirectory(new File("src/test/resources/net/achingbrain/maven/plugins/less"));
		mojo.setIncludes(patternSet);
		mojo.setOutputDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + path));
		mojo.setEnvPath("env-1.2.13.js");
		mojo.setLessPath("less-1.1.5.min.js");

		mojo.execute();

		assertTrue(new File(System.getProperty("java.io.tmpdir") + "/" + path + "/css/myfile.css").exists());
		assertTrue(new File(System.getProperty("java.io.tmpdir") + "/" + path + "/css/myotherfile.css").exists());
		assertTrue(new File(System.getProperty("java.io.tmpdir") + "/" + path + "/css/dir/yetanotherfile.css").exists());
	}

	@Test
	public void testExecute_integration_absoluteFilePaths() throws Exception {
		PatternSet patternSet = new PatternSet();
		patternSet.addInclude("**/*.less");
		patternSet.addInclude("**/*.css");

		String path = UUID.randomUUID().toString();

		mojo.setInputDirectory(new File("src/test/resources/net/achingbrain/maven/plugins/less"));
		mojo.setIncludes(patternSet);
		mojo.setOutputDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + path));
		mojo.setEnvPath("src/main/resources/net/achingbrain/maven/plugins/less/env-1.2.13.js");
		mojo.setLessPath("src/main/resources/net/achingbrain/maven/plugins/less/less-1.1.5.min.js");

		mojo.execute();

		assertTrue(new File(System.getProperty("java.io.tmpdir") + "/" + path + "/css/myfile.css").exists());
		assertTrue(new File(System.getProperty("java.io.tmpdir") + "/" + path + "/css/myotherfile.css").exists());
		assertTrue(new File(System.getProperty("java.io.tmpdir") + "/" + path + "/css/dir/yetanotherfile.css").exists());
	}
}
