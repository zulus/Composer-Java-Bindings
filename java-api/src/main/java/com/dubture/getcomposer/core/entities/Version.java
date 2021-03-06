package com.dubture.getcomposer.core.entities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dubture.getcomposer.core.ComposerConstants;

public class Version extends Entity implements Comparable<Version> {

	public final static int BEGIN = 0;
	public final static int END = 1;
	
	private List<Version> versions = new ArrayList<Version>();
	
	/**
	 * Passed version string
	 */
	private String version;
	
	private String constraint = "";
	private String stabilityModifier = "";
	private String major = "";
	private String minor = "";
	private String fix = "";
	private String build = "";
	private String stability = "";
	private String suffix = "";
	private String prefix = "";
	private int devPosition = END;
	
	public Version() {
		
	}
	
	public Version(String version) {
		parse(version);
	}
	
	private void parse(String version) {
		// reset
		clear();
		
		// start parsing
		if (version.matches(",")) {
			String parts[] = version.split(",");
			
			// lowest = this
			versions.add(this);
			parseVersion(parts[0]);
			
			// all higher ones
			for (int i = 1; i < parts.length; i++) {
				Version v = new Version(parts[i]);
				v.addPropertyChangeListener(new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						reset();
					}
				});
				versions.add(v);
			}
			
			// reset
			this.version = null;
		} else {
			parseVersion(version);
		}
	}
	
	private void parseVersion(String version) {
		this.version = version;
		String parts[];
		
		// constraint
		String constraintPattern = "^(<>|!=|>=?|<=?|==?)?(.+)";
		if (version.matches(constraintPattern)) {
			constraint = version.replaceAll(constraintPattern, "$1");
			version = version.replaceAll(constraintPattern, "$2");
		}
		
		// stability modifier
		if (version.matches(".+@.+")) {
			parts = version.split("@");
			version = parts[0];
			stabilityModifier = normalizeStability(parts[1]);
		}
		
		// dev version?
		if (version.startsWith("dev-")) {
			stability = ComposerConstants.DEV;
			version = version.substring(4);
			devPosition = BEGIN;
		}
		
		parts = version.split("-");
		int len = parts.length;
		
		if (len > 0) {
			parseMain(parts[0]);
		}
		
		if (len > 1) {
			parseTail(parts[1]);
		}
		
		if (stability.isEmpty()) {
			stability = ComposerConstants.STABLE;
		}
	}
	
	private void parseMain(String main) {
		if (main.contains(".")) {
		
			String parts[] = main.split("\\.");
			int len = parts.length;
			
			if (len > 0) {
				Pattern pattern = Pattern.compile("(\\D+)?(\\d+)", Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(parts[0]);
				matcher.find();
				
				if (matcher.group(1) != null) {
					prefix = matcher.group(1);
				}
				
				if (matcher.group(2) != null) {
					major = matcher.group(2);
				}
			}
			
			if (len > 1) {
				minor = parts[1];
			}
			
			if (len > 2) {
				fix = parts[2];
			}
			
			if (len > 3) {
				build = parts[3];
			}
		} else {
			major = main;
		}
	}
	
	private void parseTail(String tail) {
		Pattern pattern = Pattern.compile("[._-]?(?:(stable|beta|b|RC|alpha|a|patch|pl|p)(?:[.-]?(\\d+))?)?([.-]?dev)?", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(tail);

		matcher.find();
		
		if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
			suffix = matcher.group(2);
		}

		// stability 
		if (stability.isEmpty()) {
			// -dev present?
			if (matcher.group(3) != null && !matcher.group(3).isEmpty()) {
				stability = ComposerConstants.DEV;
			} 
			
			// stability
			else if (matcher.group(1) != null && !matcher.group(1).isEmpty()) {
				stability = normalizeStability(matcher.group(1));
			}
		}
	}
	
	private String normalizeStability(String stabi) {
		if (stabi.equalsIgnoreCase("dev")) {
    		return ComposerConstants.DEV;
    	}
		
		if (stabi.equalsIgnoreCase("beta") || stabi.equalsIgnoreCase("b")) {
    		return ComposerConstants.BETA;
    	}
    	
    	if (stabi.equalsIgnoreCase("alpha") || stabi.equalsIgnoreCase("a")) {
    		return ComposerConstants.ALPHA;
    	}
    	
    	if (stabi.equalsIgnoreCase("rc")) {
    		return ComposerConstants.RC;
    	}
    	
    	if (stabi.equalsIgnoreCase("stable")) {
    		return ComposerConstants.STABLE;
    	}
    	
    	return "";
	}
	
	private String build() {
		StringBuilder sb = new StringBuilder();
		
		if (!constraint.isEmpty()) {
			sb.append(constraint);
		}
		
		if (devPosition == BEGIN && stability == ComposerConstants.DEV) {
			sb.append("dev-");
		}
		
		if (!prefix.isEmpty()) {
			sb.append(prefix);
		}
		
		sb.append(major);
		
		if (!minor.isEmpty()) {
			sb.append(".");
			sb.append(minor);
		}
		
		if (!fix.isEmpty()) {
			sb.append(".");
			sb.append(fix);
		}
		
		if (!build.isEmpty()) {
			sb.append(".");
			sb.append(build);
		}
		
		StringBuilder sx = new StringBuilder();
		
		if (!stability.isEmpty() && stability != ComposerConstants.STABLE
				&& (stability == ComposerConstants.DEV ? devPosition != BEGIN : true)) {
			sx.append(stability);
		}

		if (!suffix.isEmpty()) {
			sx.append(suffix);
		}
		
		if (sx.length() > 0) {
			sb.append("-");
			sb.append(sx);
		}
		
		if (!stabilityModifier.isEmpty()) {
			sb.append("@");
			sb.append(stabilityModifier);
		}
		
		if (versions.size() > 0) {
			int i = 1;
			for (Version v : versions) {
				sb.append(v.toString());
				if (i < versions.size()) {
					sb.append(",");
				}
				i++;
			}
		}
		
		return sb.toString();
	}
	
	public boolean hasRange() {
		return versions.size() > 0;
	}
	
	public Version getLowest() {
		return this;
	}
	
	public Version getHighest() {
		if (versions == null) {
			return null;
		}
		
		return versions.get(versions.size() - 1);
	}
	
	public void clear() {
		versions.clear();
		constraint = "";
		stabilityModifier = "";
		major = "";
		minor = "";
		fix = "";
		build = "";
		stability = "";
		suffix = "";
		prefix = "";
		devPosition = END;
		version = null;
	}
	
	public Version getVersion(int index) {
		if (versions.size() > index) {
			return versions.get(index);
		}
		return null;
	}
	
	public List<Version> getVersions() {
		return versions;
	}
	
	public void add(Version version) {
		versions.add(versions.size(), version);
	}
	
	public void add(int index, Version version) {
		versions.add(index, version);
		reset();
	}
	
	private void reset() {
		version = "";
	}

	/**
	 * @return the version
	 */
	public String toString() {
		if (version == "" || version == null) {
			version = build();
		}
		return version;
	}

	/**
	 * @return the constraint
	 */
	public String getConstraint() {
		return constraint;
	}

	/**
	 * @return the stabilityModifier
	 */
	public String getStabilityModifier() {
		return stabilityModifier;
	}

	/**
	 * @return the major
	 */
	public String getMajor() {
		return major;
	}

	/**
	 * @return the minor
	 */
	public String getMinor() {
		return minor;
	}

	/**
	 * @return the fix
	 */
	public String getFix() {
		return fix;
	}

	/**
	 * @return the build
	 */
	public String getBuild() {
		return build;
	}

	/**
	 * @return the stability
	 */
	public String getStability() {
		return stability;
	}
	
	/**
	 * @return the suffix
	 */
	public String getSuffix() {
		return suffix;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		String oldVersion = this.version;
		versions.clear();
		parse(version);
		firePropertyChange("version", oldVersion, this.version);
	}
	
	public void from(Version version) {
		String oldVersion = this.version;
		reset();
		
		versions.clear();
		versions.addAll(version.getVersions());
		
		constraint = version.getConstraint();
		stabilityModifier = version.getStabilityModifier();
		major = version.getMajor();
		minor = version.getMinor();
		fix = version.getFix();
		build = version.getBuild();
		stability = version.getStability();
		suffix = version.getSuffix();
		prefix = version.getPrefix();
		devPosition = version.getDevPosition();
		
		firePropertyChange("version", oldVersion, toString());
	}

	/**
	 * @param constraint the constraint to set
	 */
	public void setConstraint(String constraint) {
		reset();
		firePropertyChange("constraint", this.constraint, this.constraint = constraint);
	}

	/**
	 * @param stabilityModifier the stabilityModifier to set
	 */
	public void setStabilityModifier(String stabilityModifier) {
		reset();
		firePropertyChange("stabilityModifier", this.stabilityModifier, this.stabilityModifier = stabilityModifier);
	}

	/**
	 * @param major the major to set
	 */
	public void setMajor(String major) {
		reset();
		firePropertyChange("major", this.major, this.major = major);
	}

	/**
	 * @param minor the minor to set
	 */
	public void setMinor(String minor) {
		reset();
		firePropertyChange("minor", this.minor, this.minor = minor);
	}

	/**
	 * @param fix the fix to set
	 */
	public void setFix(String fix) {
		reset();
		firePropertyChange("fix", this.fix, this.fix = fix);
	}

	/**
	 * @param build the build to set
	 */
	public void setBuild(String build) {
		reset();
		firePropertyChange("build", this.build, this.build = build);
	}

	/**
	 * @param stability the stability to set
	 */
	public void setStability(String stability) {
		reset();
		firePropertyChange("stability", this.stability, this.stability = stability);
	}
	
	/**
	 * @param suffix the suffix to set
	 */
	public void setSuffix(String suffix) {
		reset();
		firePropertyChange("suffix", this.suffix, this.suffix = suffix);
	}

	public int getDevPosition() {
		return devPosition;
	}

	public void setDevPosition(int devPosition) {
		reset();
		firePropertyChange("devPosition", this.devPosition, this.devPosition = devPosition);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		reset();
		firePropertyChange("prefix", this.prefix, this.prefix = prefix);
	}

	@Override
	public int compareTo(Version anotherVersion) {
		if (toString() == "dev-master") {
			return -1;
		}
		
		if (anotherVersion.toString() == "dev-master") {
			return 1;
		}
		
		// major
		int major = cmp(getMajor(), anotherVersion.getMajor()); 
		if (major == 0) {
			int minor = cmp(getMinor(), anotherVersion.getMinor());
			
			if (minor == 0) {
				int fix = cmp(getFix(), anotherVersion.getFix());
				
				if (fix == 0) {
					int build = cmp(getBuild(), anotherVersion.getBuild());
					
					if (build == 0) {
						int s1 = Arrays.binarySearch(ComposerConstants.STABILITIES, getStability());
						int s2 = Arrays.binarySearch(ComposerConstants.STABILITIES, anotherVersion.getStability());
						
						if (s1 == s2) {
							return cmp(getSuffix(), anotherVersion.getSuffix());
						} else {
							return s1 > s2 ? -1 : 1;
						}
					} else {
						return build;
					}
				} else {
					return fix;
				}
			} else {
				return minor;
			}
		} else {
			return major;
		}
	}

	private int cmp(String s1, String s2) {
		// ints
		int i1 = 0, i2 = 0;
		
		// true = string contains chars, false = integer
		boolean l1 = true, l2 = true;
		
		// convert
		try {
			i1 = Integer.parseInt(s1, 10);
			l1 = false;
		} catch(Exception e) {}
		
		try {
			i2 = Integer.parseInt(s2, 10);
			l2 = false;
		} catch(Exception e) {}
		
		// both string
		if (l1 && l2) {
			return s1.compareTo(s2);
		}
		
		// l1 string, l2 integer
		if (l1 && !l2) {
			return 1;
		}
		
		// l1 integer, l2 string
		if (!l1 && l2) {
			return -1;
		}
		
		// both integer
		if (i1 == i2) {
			return 0;
		}
		
		return i1 > i2 ? 1 : -1;
	}

}
