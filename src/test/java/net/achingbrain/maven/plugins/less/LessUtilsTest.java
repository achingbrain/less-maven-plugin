package net.achingbrain.maven.plugins.less;

import junit.framework.Assert;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LessUtilsTest {

	@Test
	public void testImportStatementPattern() throws IOException {
		// given
		String ImportStatementPattern = "@import\\s*\".+\"";
		String string = "bam bam; @import \"foo\" blub blub";

		// when
		Pattern p = Pattern.compile(ImportStatementPattern);
		Matcher m = p.matcher(string);

		// then
		Assert.assertTrue("pattern not found.", m.find());
		Assert.assertEquals("substring starts at wrong index.", 9, m.start());
		Assert.assertEquals("substring ends at wrong index", 22, m.end());
		Assert.assertFalse(m.find());
	}

	@Test
	public void testResolveImports() throws IOException {
		// given
		Map<String, String> lessFiles = new HashMap<String, String>();
		lessFiles.put(FilenameUtils.normalize("/tmp/test-1.less"), "@import \"test-2\" abc");
		lessFiles.put(FilenameUtils.normalize("/tmp/test-2.less"), "@import \"test-3.less\" dim sun");
		lessFiles.put(FilenameUtils.normalize("/tmp/test-3.less"), "foobar");

		// when
		String resolvedLess = LessUtils.resolveImports(FilenameUtils.normalize("/tmp/test-1.less"), lessFiles);

		// then
		Assert.assertEquals("failed resolving imports.", "foobar dim sun abc", StringUtils.trim(resolvedLess));
	}

	@Test
	public void testResolveImportsTravelingDirectories() throws IOException {
		// given
		Map<String, String> lessFiles = new HashMap<String, String>();
		lessFiles.put(FilenameUtils.normalize("/tmp/dir/test-1.less"), "@import \"../test-2\" abc");
		lessFiles.put(FilenameUtils.normalize("/tmp/test-2.less"), "foobar");

		// when
		String resolvedLess = LessUtils.resolveImports(FilenameUtils.normalize("/tmp/dir/test-1.less"), lessFiles);

		// then
		Assert.assertEquals("failed resolving imports.", "foobar abc", StringUtils.trim(resolvedLess));
	}
}
