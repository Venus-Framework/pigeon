package com.dianping.pigeon.component.xmap;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * A resource that can be retrieved using the class loader.
 * <p>
 * This is wrapping an URL as returned by the class loader.
 * <p>
 * The URL class cannot be used directly because it already has a factory
 * associated to it that constructs the URL using its constructor.
 * 
 * 
 */
public class Resource {

	private final URL url;

	public Resource(URL url) {
		this.url = url;
	}

	public Resource(Context ctx, String path) {
		url = ctx.getResource(path);
	}

	public URL toURL() {
		return url;
	}

	public URI toURI() throws URISyntaxException {
		return url != null ? url.toURI() : null;
	}

	public File toFile() throws URISyntaxException {
		return url != null ? new File(url.toURI()) : null;
	}

}
