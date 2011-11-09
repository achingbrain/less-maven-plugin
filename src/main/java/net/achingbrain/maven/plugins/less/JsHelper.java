package net.achingbrain.maven.plugins.less;

import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Borrowed from http://code.google.com/p/maven-less-plugin
 */
public class JsHelper {
	private static JsHelper instance;
	public static String ENV_VERSION = "env-1.2.13.js";
	public static String LESS_VERSION = "less-1.1.3.min.js";

	private Context ctx;
	private Scriptable scope;

	private JsHelper() throws IOException {
		ctx = ContextFactory.getGlobal().enterContext();
		ctx.setOptimizationLevel(-1);
		ctx.setLanguageVersion(Context.VERSION_1_7);

		scope = ctx.initStandardObjects();

		loadInternalJsResource("env", ENV_VERSION);
		loadInternalJsResource("less", LESS_VERSION);
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

	private void loadInternalJsResource(String name, String relativePath) throws IOException {
		InputStream is = JsHelper.class.getResourceAsStream(relativePath);
		Reader reader = new InputStreamReader(is);
		ctx.evaluateReader(scope, reader, name, 1, null);
	}

	static public JsHelper getInstance() throws IOException {
		if (instance == null) {
			instance = new JsHelper();
		}

		return instance;
	}
}
