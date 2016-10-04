package org.apache.tools.ant.taskdefs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class Shade
		extends Task {

	private final Collection<Jar> jars = new ArrayList<>();
	private final Collection<Relocation> relocations = new ArrayList<>();
	private File uberJar;

	public Jar createJar() {
		final Jar jar = new Jar();
		jars.add(jar);
		return jar;
	}

	public Relocation createRelocation() {
		final Relocation relocation = new Relocation();
		relocations.add(relocation);
		return relocation;
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
		shadeRequest.setJars(geJars());
		shadeRequest.setRelocators(getRelocations());
		shadeRequest.setResourceTransformers(getResourceTransformers());
		shadeRequest.setUberJar(getUberJar());
		return shadeRequest;
	}

	// TODO
	private List<Filter> getFilters() {
		return emptyList();
	}

	private Set<File> geJars() {
		return jars
				.stream()
				.map(j -> j.path)
				.collect(toSet());
	}

	private List<Relocator> getRelocations() {
		return relocations
				.stream()
				.map(r -> new SimpleRelocator(
						r.pattern,
						r.shadedPattern,
						ofNullable(r.includes)
								.flatMap(is -> ofNullable(is.includes))
								.map(is -> is.stream().map(i -> i.include).collect(toList()))
								.orElse(null),
						ofNullable(r.excludes)
								.flatMap(es -> ofNullable(es.excludes))
								.map(es -> es.stream().map(e -> e.exclude).collect(toList()))
								.orElse(null),
						r.isRawString
				))
				.collect(toList());
	}

	// TODO
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
		private Includes includes;
		private Excludes excludes;
		private boolean isRawString;

		public void setPattern(final String pattern) {
			this.pattern = pattern;
		}

		public void setShadedPattern(final String shadedPattern) {
			this.shadedPattern = shadedPattern;
		}

		public Includes createIncludes() {
			final Includes includes = new Includes();
			this.includes = includes;
			return includes;
		}

		public Excludes createExcludes() {
			final Excludes excludes = new Excludes();
			this.excludes = excludes;
			return excludes;
		}

		public void setRawString(final boolean rawString) {
			isRawString = rawString;
		}

		public static final class Includes {

			private List<Include> includes;

			public Include createInclude() {
				final Include include = new Include();
				if ( includes == null ) {
					includes = new ArrayList<>();
				}
				includes.add(include);
				return include;
			}

			public static final class Include {

				private String include;

				public void addText(final String include) {
					this.include = include;
				}

			}

		}

		public static final class Excludes {

			private List<Exclude> excludes;

			public Exclude createExclude() {
				final Exclude exclude = new Exclude();
				if ( excludes == null ) {
					excludes = new ArrayList<>();
				}
				excludes.add(exclude);
				return exclude;
			}

			public static final class Exclude {

				private String exclude;

				public void addText(final String exclude) {
					this.exclude = exclude;
				}

			}

		}

	}

}
