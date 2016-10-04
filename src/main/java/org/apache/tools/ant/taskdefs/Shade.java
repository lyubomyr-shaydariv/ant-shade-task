package org.apache.tools.ant.taskdefs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
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
import org.apache.tools.ant.taskdefs.Shade.Relocation.StringValue;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class Shade
		extends Task {

	private Set<Jar> jars;
	private Collection<Relocation> relocations;
	private File uberJar;

	public Jar createJar() {
		return new Jar();
	}

	public void addConfiguredJar(final Jar jar) {
		if ( jars == null ) {
			jars = new LinkedHashSet<>();
		}
		jars.add(jar);
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

	public void setUberJar(final File uberJar) {
		this.uberJar = uberJar;
	}

	@Override
	public void execute() {
		try {
			final Logger logger = new ConsoleLogger();
			final Shader shader = new DefaultShader() {{
				enableLogging(logger);
			}};
			final ShadeRequest shadeRequest = getShadeRequest();
			log("Shading: " + shadeRequest.getJars().stream().map(File::toString).collect(joining()));
			shader.shade(shadeRequest);
		} catch ( final IOException | MojoExecutionException ex ) {
			throw new BuildException(ex);
		}
	}

	private ShadeRequest getShadeRequest() {
		final ShadeRequest shadeRequest = new ShadeRequest();
		shadeRequest.setFilters(getFilters());
		shadeRequest.setJars(getJars());
		shadeRequest.setRelocators(getRelocations());
		shadeRequest.setResourceTransformers(getResourceTransformers());
		shadeRequest.setUberJar(getUberJar());
		return shadeRequest;
	}

	private List<Filter> getFilters() {
		return emptyList();
	}

	private Set<File> getJars() {
		return ofNullable(jars).map(js -> js.stream().map(j -> j.path).collect(toSet())).orElse(emptySet());
	}

	private List<Relocator> getRelocations() {
		return relocations
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
	}

	private List<ResourceTransformer> getResourceTransformers() {
		return emptyList();
	}

	private File getUberJar() {
		return uberJar;
	}

	public static final class Jar {

		private File path;

		public void setPath(final File path) {
			this.path = path;
		}

		@Override
		public boolean equals(final Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			final Jar jar = (Jar) o;
			return path != null ? path.equals(jar.path) : jar.path == null;
		}

		@Override
		public int hashCode() {
			return path != null ? path.hashCode() : 0;
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

		public abstract static class StringValue {

			private String value;

			public final void setValue(final String value) {
				this.value = value;
			}

			public final String getValue() {
				return value;
			}

		}

		public static final class Include
				extends StringValue {
		}

		public static final class Exclude
				extends StringValue {
		}

	}

}
