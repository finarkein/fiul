# ⛽ FIUL

> Pronounced: Fuel, फिऊल

The **F**inancial **I**nformation **U**ser **L**ayer (FIUL) is an open source java based implementation for the Account
Aggregator FIUs designed with performance, security and privacy in mind.

## Getting Started

### Using Docker

Docker image for `fiul` is hosted on Github Packages. Refer following steps for get it running:

> Prerequisite: Set up github authentication, refer this official [documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry). 

1. To pull the latest `dev` image, run: 
```sh
docker pull ghcr.io/finarkein/fiul:dev
```
2. Configure your docker environment with required [environment variables](#required-environment-variables).
3. Run the following command to start your FIU instance:
```sh
docker run -e AA_API_CREDENTIALS -e SECRET_KEYSET -p 7065:7065 --name fiul ghcr.io/finarkein/fiul:dev`
```

## Configuration

Checkout the complete available configuration [here](fiul-rest/fiul-rest-app/src/main/resources/application.properties).

### Required Environment Variables

Variable            | Comment
------------------- | -------
`SECRET_KEYSET`     | JSON string of the JWK Pair Set provided to Sahamati Central Registry (CR). Reference json [format](docs/secret_keyset_format.json).
`AA_API_CREDENTIALS`| Path to AA client credentials json file. Reference file [format](docs/aa_api_credentials_format.json).

## License

Refer to [LICENSE](license/LICENSE) for more details. Please note that this is a work in progress, and we're working to 
finalize the terms. Feel free to reach out to us using: oss (at) finarkein (dot) com, to discuss further.