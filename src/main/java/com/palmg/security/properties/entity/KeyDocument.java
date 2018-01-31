package com.palmg.security.properties.entity;

import java.io.Serializable;
import java.util.UUID;

import com.palmg.security.properties.Scheme;

public class KeyDocument implements Serializable {
	private static final long serialVersionUID = -8439676997553148700L;
	private static final String DEG_PROFILE = "default";
	private byte[] key;
	private Scheme scheme;
	private String id;
	private String ver;
	private String profile;

	public KeyDocument(byte[] key, Scheme scheme) {
		this.key = key;
		this.scheme = scheme;
		this.id = UUID.randomUUID().toString().replaceAll("-", "");
		this.ver = "1.0.0";
		this.profile = KeyDocument.DEG_PROFILE;
	}

	public KeyDocument(byte[] key, Scheme scheme, String profile) {
		this.key = key;
		this.scheme = scheme;
		this.id = UUID.randomUUID().toString().replaceAll("-", "");
		this.ver = "1.0.0";
		this.profile = profile;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public Scheme getScheme() {
		return scheme;
	}

	public void setScheme(Scheme scheme) {
		this.scheme = scheme;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getVer() {
		return ver;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}

	public boolean Compare(KeyDocument other) {
		return this.id.equals(other.id) && keyCompare(other.key) && this.scheme.equals(other.scheme)
				&& this.ver.equals(other.ver) && this.profile.equals(other.profile);
	}

	private boolean keyCompare(byte[] keys) {
		boolean result = this.key.length == keys.length;
		if (result) {
			for (int index = 0; index < this.key.length; index++) {
				result = this.key[index] == keys[index];
				if (!result)
					break;
			}
		}
		return result;
	}
}
