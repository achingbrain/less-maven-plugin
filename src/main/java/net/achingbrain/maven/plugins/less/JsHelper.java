package net.achingbrain.maven.plugins.less;

import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import java.io.*;

/**
 * Borrowed from http://code.google.com/p/maven-less-plugin
 */
public class JsHelper {
	private static JsHelper instance;
	private Context ctx;
	private Scriptable scope;

	private JsHelper(String lessPath, String envPath) throws IOException {
		ctx = ContextFactory.getGlobal().enterContext();
		ctx.setOptimizationLevel(-1);
		ctx.setLanguageVersion(Context.VERSION_1_7);

		scope = ctx.initStandardObjects();

		loadInternalJsResource("env", envPath);
		loadInternalJsResource("less", lessPath);
	}

	public String compileLess(String less) {
		if (null == less) {
			throw new IllegalArgumentException("less must not be null.");
		}

		if (StringUtils.isBlank(less)) {
			return "";
		}

		scope.put("lessSourceCode", scope, less);
		scope.put("result", scope, "");

		String js = "var result; var p = new less.Parser(); p.parse(lessSourceCode, function(e, tree){ result=tree.toCSS(); });";
		ctx.evaluateString(scope, js, "compileLess.js", 1, null);
		// scope.put("lessSourceCode", scope, "");

		Object result = scope.get("result", scope);
		String css = null;
		if (result instanceof Undefined) {
			System.err.println("result is undefined");
		} else {
			css = result.toString();
		}

		scope.put("result", scope, "");
		return css;
	}

	private void loadInternalJsResource(String name, String path) throws IOException {
		InputStream is;
		File file = new File(path);

		if(file.exists()) {
			// passed absolute file path
			is = new FileInputStream(file);
		} else {
			// load from classpath
			is = JsHelper.class.getResourceAsStream(path);
		}

		Reader reader = new InputStreamReader(is);
		ctx.evaluateReader(scope, reader, name, 1, null);
	}

	static public JsHelper getInstance(String lessVersion, String envVersion) throws IOException {
		if (instance == null) {
			instance = new JsHelper(lessVersion, envVersion);
		}

		return instance;
	}
}
