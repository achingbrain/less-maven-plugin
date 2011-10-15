package net.achingbrain.maven.plugins.less;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.PatternSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import yevgeniy.melnichuk.JsHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.writeStringToFile;

/**
 * @goal compile
 * @phase compile
 */
public class LessMojo extends AbstractMojo {
	/**
	 * A set of files to include.  Should contain LESS markup.
	 *
	 * @parameter
	*/
	private PatternSet includes;

	/**
	 * @parameter expression="${project.build.directory}/${project.build.finalName}"
	 */
	private File outputDirectory;

	/**
	 * @parameter expression="${basedir}/src/main/webapp"
	 */
	private File inputDirectory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			List<File> inputFiles = findFiles();

			for(File lessFile : inputFiles) {
				// gloms the whole file together into one string
				String less = includeIncludedFiles(lessFile);

				// compiles LESS to css
				JsHelper jsHelper = JsHelper.getInstance();
				String compiled = jsHelper.compileLess(less);

				// set up output file
				File cssFile = createOutputFile(lessFile);
				ensureDirectoryExists(cssFile);

				// write out output file
				writeStringToFile(cssFile, compiled);
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Could not compile LESS", e);
		}
	}

	protected String includeIncludedFiles(File file) throws IOException, MojoFailureException {
		return includeIncludedFiles(file, new ArrayList<String>());
	}

	protected String includeIncludedFiles(File file, List<String> importedPaths) throws IOException, MojoFailureException {
		if(!file.exists()) {
			throw new MojoFailureException("Input file does not exist: " + file.getAbsolutePath());
		}

		String content = readFileToString(file);

		if(content == null) {
			throw new MojoFailureException("Could not read input file: " + file.getAbsolutePath());
		}

		if(StringUtils.isBlank(content)) {
			getLog().info("Input file was empty: " + file.getAbsolutePath());

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

			importedFile = file.getParent() + File.separator + importedFile;
			importedFile = FilenameUtils.normalize(importedFile);

			File included = new File(importedFile);

			// did the user miss the .less extension?
			if(!included.exists() && !importedFile.endsWith(".less")) {
				importedFile += ".less";
				included = new File(importedFile);
			}

			// make sure we don't import a file that will end up importing the current file
			if(included.exists() && importedPaths.contains(included.getAbsolutePath())) {
				getLog().warn("Detected circular dependency in LESS files.");
				getLog().warn("Processing " + file.getAbsolutePath());
				getLog().warn("Stack:");

				for(String path : importedPaths) {
					getLog().warn(path);
				}

				getLog().warn("Tried to import " + included.getAbsolutePath());

				// do not import the file again, but do omit the import statement
				content = content.substring(0, importStatementMatcher.start()) + content.substring(importStatementMatcher.end());

				continue;
			} else {
				importedPaths.add(included.getAbsolutePath());
			}

			content = content.substring(0, importStatementMatcher.start()) + " " + includeIncludedFiles(included, importedPaths) + " " + content.substring(importStatementMatcher.end());
			importStatementMatcher = importStatementPattern.matcher(content);
		}

		return content;
	}

	protected File createOutputFile(File inputFile) {
		String cssFileLocation = inputFile.getPath().replace(".less", ".css").replace(inputDirectory.getPath(), "");
		return new File(outputDirectory, cssFileLocation);
	}

	protected void ensureDirectoryExists(File cssFile) {
		if(!cssFile.getParentFile().exists()) {
			cssFile.getParentFile().mkdirs();
		}
	}

	protected List<File> findFiles() throws IOException {
		if(includes == null) {
			// default behaviour - return every .less file in the input directory
			return FileUtils.getFiles(inputDirectory, "**/*.less", "");
		}

		return FileUtils.getFiles(inputDirectory, StringUtils.join(includes.getIncludes(), ","), StringUtils.join(includes.getExcludes(), ","));
	}

	public void setIncludes(PatternSet includes) {
		this.includes = includes;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void setInputDirectory(File inputDirectory) {
		this.inputDirectory = inputDirectory;
	}
}
