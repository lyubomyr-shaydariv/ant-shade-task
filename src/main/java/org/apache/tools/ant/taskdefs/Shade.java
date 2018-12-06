package org.apache.tools.ant.taskdefs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

public final class Shade
		extends Task {

	@Nullable
	private File jar;

	@Nullable
	private File uberJar;

	@Nullable
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
			log("Shading: " + shadeRequest.getJars().stream().map(File::toString).collect(Collectors.joining()));
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
		shadeRequest.setJars(prepareJars(jar));
		shadeRequest.setRelocators(prepareRelocations(relocations));
		shadeRequest.setResourceTransformers(prepareResourceTransformers());
		shadeRequest.setUberJar(prepareUberJar(uberJar));
		return shadeRequest;
	}

	@Nonnull
	private static List<Filter> prepareFilters() {
		return Collections.emptyList();
	}

	@Nonnull
	private static Set<File> prepareJars(@Nullable final File jar)
			throws BuildException {
		if ( jar == null ) {
			throw new BuildException("The jar option requires a non-null value");
		}
		return Collections.unmodifiableSet(Collections.singleton(jar));
	}

	@Nonnull
	private static List<Relocator> prepareRelocations(@Nullable final Collection<Relocation> relocations)
			throws BuildException {
		if ( relocations == null ) {
			throw new BuildException("The relocations option requires a non-null value");
		}
		return relocations
				.stream()
				.map(r -> new SimpleRelocator(
						r.pattern,
						r.shadedPattern,
						r.includes != null
								? r.includes.stream().map(StringValue::getValue).collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList))
								: null,
						r.excludes != null
								? r.excludes.stream().map(StringValue::getValue).collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList))
								: null,
						r.isRawString
				))
				.collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
	}

	@Nonnull
	private static List<ResourceTransformer> prepareResourceTransformers() {
		return Collections.emptyList();
	}

	@Nonnull
	private static File prepareUberJar(@Nullable final File uberJar)
			throws BuildException {
		if ( uberJar == null ) {
			throw new BuildException("The uberJar option requires a non-null value");
		}
		return uberJar;
	}

}
