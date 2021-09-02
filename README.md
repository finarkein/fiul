# ⛽ FIUL

> Pronounced: Fuel, फिऊल

The **F**inancial **I**nformation **U**ser **L**ayer (FIUL) is an open source java based implementation for the Account
Aggregator FIUs designed with performance, security and privacy in mind.

## Getting Started

### Using Docker

Docker image for `fiul` is hosted on Github Packages. Refer following steps for get it running:

> Prerequisite: Set up github authentication, refer this official [documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry). 

1. To pull the latest `dev` image, run: 
```shell
docker pull ghcr.io/finarkein/fiul:dev
```
2. Configure your docker environment with required [environment variables](#required-environment-variables).
```shell
# Export environment variables to run the docker image
export SECRET_KEYSET=$(cat path_to_keypairset.json)
# Set clients credentials path w.r.t the container
export AA_API_CREDENTIALS=/etc/secret/aa_credentials.json
```
3. Run the following command to start your FIU instance:
```shell
docker run -e AA_API_CREDENTIALS -e SECRET_KEYSET -p 7065:7065 /
 -v local_path_to_aa_credentials.json:/etc/secret/aa_credentials.json /
 --name fiul ghcr.io/finarkein/fiul:dev
```

Replace `path_to_keypairset` & `local_path_to_aa_credentials` to file paths available in your environment.

## Configuration

Checkout the complete available configuration [here](fiul-rest/fiul-rest-app/src/main/resources/application.properties).

### Required Environment Variables

Variable            | Comment
------------------- | -------
`SECRET_KEYSET`     | JSON string of the JWK Pair Set provided to Sahamati Central Registry (CR). Reference json [format](docs/secret_keyset_format.json).
`AA_API_CREDENTIALS`| Path to AA client credentials json file. Reference file [format](docs/aa_api_credentials_format.json). **Optional** if `aa_credentials.json` file is already present in classpath.

## Getting Involved / Contributing

To contribute, simply make a pull request and add a brief description of your addition or change. For
more details, check the [contribution guidelines](.github/CONTRIBUTING.md).

## License

Refer to [LICENSE](license/LICENSE) for more details. Please note that this is a work in progress, and we're working to 
finalize the terms. Feel free to reach out to us using: oss (at) finarkein (dot) com, to discuss further.