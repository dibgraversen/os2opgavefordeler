package dk.os2opgavefordeler.model.presentation;

/**
 * Presentation object for API key
 */
public class ApiKeyPO {

	public ApiKeyPO() {}

	/**
	 * Creates an instance of ApiKeyPO with the specified API key
	 *
	 * @param apiKey API key
	 */
	public ApiKeyPO(String apiKey) {
		this.apiKey = apiKey;
	}

	private ApiKeyPO(Builder builder) {
		this();
		this.apiKey = builder.apiKey;
	}

	private String apiKey;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	//--------------------------------------------------------------------------
	// Builder
	//--------------------------------------------------------------------------
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String apiKey;

		public ApiKeyPO build() {
			return new ApiKeyPO(this);
		}

		public Builder apiKey(String apiKey) {
			this.apiKey = apiKey;
			return this;
		}
	}
}
