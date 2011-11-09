package net.achingbrain.maven.plugins.less;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Borrowed from http://code.google.com/p/maven-less-plugin
 */
public class LessUtils {
	static public String compileLess(String absolutePath, Map<String, String> lessFiles) throws IOException {
		System.out.println("compiling " + absolutePath);
		String less = LessUtils.resolveImports(absolutePath, lessFiles);

		JsHelper jsHelper = JsHelper.getInstance();
		return jsHelper.compileLess(less);
	}

	static public Map<String, String> readFiles(Collection<File> files) {
		Map<String, String> result = new HashMap<String, String>();
		for (File file : files) {
			try {
				String content = FileUtils.readFileToString(file);
				String absolutePath = file.getAbsolutePath();
				result.put(absolutePath, content);
			} catch (IOException e) {
				System.err.println("failed to read file: " + file);
			}
		}
		return result;
	}

	static public String resolveImports(String filePath, Map<String, String> lessFiles) {
		String content = lessFiles.get(filePath);
		if (content == null) {
			System.out.println("file not found: " + filePath);
			return null;
		}

		if (StringUtils.isBlank(content)) {
			System.out.println(filePath + " is empty.");
			return "";
		}

		Pattern importStatementPattern = Pattern.compile("@import\\s*\".+\"\\s*;*");
		Pattern importedFilePattern = Pattern.compile("\".*\"");

		Matcher importStatementMatcher = importStatementPattern.matcher(content);
		while (importStatementMatcher.find()) {
			String importStatement = content.substring(importStatementMatcher.start(), importStatementMatcher.end());

			Matcher importedFileMatcher = importedFilePattern.matcher(importStatement);
			importedFileMatcher.find();
			String importedFile = importStatement.substring(importedFileMatcher.start() + 1, importedFileMatcher.end() - 1);
			if (!importedFile.endsWith(".less")) {
				importedFile += ".less";
			}

			importedFile = FilenameUtils.getFullPath(filePath) + importedFile;
			importedFile = FilenameUtils.normalize(importedFile);

			content = content.substring(0, importStatementMatcher.start()) + " " + resolveImports(importedFile, lessFiles) + " " + content.substring(importStatementMatcher.end());
			importStatementMatcher = importStatementPattern.matcher(content);
		}

		return content;
	}

	public static void main(String[] args) throws IOException {
		String dirParameter = null;

		if (args.length < 1) {
			dirParameter = ".";
		} else {
			dirParameter = args[0];
		}

		File dir = new File(dirParameter);
		if (!dir.exists()) {
			System.err.println("specified directory not found.");
		}

		String dirNormalized = FilenameUtils.normalize(dir.getAbsolutePath());

		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(new File(dirNormalized), new String[] { "less" }, true);
		Map<String, String> lessFiles = LessUtils.readFiles(files);
		for (Entry<String, String> entry : lessFiles.entrySet()) {
			String lessFileWithPath = entry.getKey();
			String css = LessUtils.compileLess(lessFileWithPath, lessFiles);
			if (StringUtils.isEmpty(css)) {
				continue;
			}

			String absolutePath = FilenameUtils.getFullPath(lessFileWithPath);
			String basename = FilenameUtils.getBaseName(lessFileWithPath);

			String cssFile = absolutePath + basename + ".css";
			try {
				System.out.println("writing css to " + cssFile);
				FileUtils.writeStringToFile(new File(cssFile), css);
			} catch (IOException e) {
				System.err.println("failed to write to file: " + cssFile);
			}
		}
	}
}
