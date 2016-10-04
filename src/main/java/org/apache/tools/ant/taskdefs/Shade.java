package org.apache.tools.ant.taskdefs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.shade.DefaultShader;
import org.apache.maven.plugins.shade.ShadeRequest;
import org.apache.maven.plugins.shade.Shader;
import org.apache.maven.plugins.shade.filter.Filter;
import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.relocation.SimpleRelocator;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public final class Shade
		extends Task {

	private File jar;
	private File uberJar;
	private Collection<Relocation> relocations;

	public void setJar(final File jar) {
		this.jar = jar;
	}

	public void setUberJar(final File uberJar) {
		this.uberJar = uberJar;
	}

	public Relocation createRelocation() {
		return new Relocation();
	}

	public void addConfiguredRelocation(final Relocation relocation) {
		if ( relocations == null ) {
			relocations = new ArrayList<>();
		}
		relocations.add(relocation);
	}

	@Override
	public void execute() {
		try {
			final Shader shader = new DefaultShader() {{
				enableLogging(new ConsoleLogger());
			}};
			final ShadeRequest shadeRequest = getShadeRequest();
			log("Shading: " + shadeRequest.getJars().stream().map(File::toString).collect(joining()));
			shader.shade(shadeRequest);
		} catch ( final IOException | MojoExecutionException ex ) {
			throw new BuildException(ex);
		}
	}

	public abstract static class StringValue {

		private String value;

		public final void setValue(final String value) {
			this.value = value;
		}

		public final String getValue() {
			return value;
		}

	}

	public static final class Relocation {

		private String pattern;
		private String shadedPattern;
		private Collection<Include> includes;
		private Collection<Exclude> excludes;
		private boolean isRawString;

		public void setPattern(final String pattern) {
			this.pattern = pattern;
		}

		public void setShadedPattern(final String shadedPattern) {
			this.shadedPattern = shadedPattern;
		}

		public Include createInclude() {
			return new Include();
		}

		public void addConfiguredInclude(final Include include) {
			if ( includes == null ) {
				includes = new ArrayList<>();
			}
			includes.add(include);
		}

		public Exclude createExclude() {
			return new Exclude();
		}

		public void addConfiguredExclude(final Exclude exclude) {
			if ( excludes == null ) {
				excludes = new ArrayList<>();
			}
			excludes.add(exclude);
		}

		public void setRawString(final boolean rawString) {
			isRawString = rawString;
		}

		public static final class Include
				extends StringValue {
		}

		public static final class Exclude
				extends StringValue {
		}

	}

	private ShadeRequest getShadeRequest() {
		final ShadeRequest shadeRequest = new ShadeRequest();
		shadeRequest.setFilters(prepareFilters());
		shadeRequest.setJars(prepareJars());
		shadeRequest.setRelocators(prepareRelocations());
		shadeRequest.setResourceTransformers(prepareResourceTransformers());
		shadeRequest.setUberJar(prepareUberJar());
		return shadeRequest;
	}

	private static List<Filter> prepareFilters() {
		return emptyList();
	}

	private Set<File> prepareJars() {
		final Set<File> files = ofNullable(jar)
				.map(Collections::singleton)
				.orElse(emptySet());
		return unmodifiableSet(files);
	}

	private List<Relocator> prepareRelocations() {
		final List<Relocator> relocators = relocations
				.stream()
				.map(r -> new SimpleRelocator(
						r.pattern,
						r.shadedPattern,
						ofNullable(r.includes)
								.map(is -> is.stream().map(StringValue::getValue).collect(toList()))
								.orElse(null),
						ofNullable(r.excludes)
								.map(es -> es.stream().map(StringValue::getValue).collect(toList()))
								.orElse(null),
						r.isRawString
				))
				.collect(toList());
		return unmodifiableList(relocators);
	}

	private static List<ResourceTransformer> prepareResourceTransformers() {
		return emptyList();
	}

	private File prepareUberJar() {
		return uberJar;
	}

}
