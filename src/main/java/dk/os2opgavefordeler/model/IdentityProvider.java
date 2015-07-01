package dk.os2opgavefordeler.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class IdentityProvider implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String name;
	private String idpUrl;
	private String clientId;
	private String clientSecret;

	public IdentityProvider() {
	}

	public IdentityProvider(Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.idpUrl = builder.url;
		this.clientId = builder.clientId;
		this.clientSecret = builder.clientSecret;
	}

	//--------------------------------------------------------------------------
	// Getters/setters
	//--------------------------------------------------------------------------
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getIdpUrl() {
		return idpUrl;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	//--------------------------------------------------------------------------
	// Builder
	//--------------------------------------------------------------------------
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private int id;
		private String name;
		private String url;
		private String clientId;
		private String clientSecret;

		public IdentityProvider build() {
			return new IdentityProvider(this);
		}

		public Builder id(int id) {
			this.id = id;
			return this;
		}
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		public Builder url(String url) {
			this.url = url;
			return this;
		}
		public Builder clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}
		public Builder clientSecret(String clientSecret) {
			this.clientSecret = clientSecret;
			return this;
		}
	}
}
