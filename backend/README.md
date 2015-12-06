Package based on [go-hello-world][go_hello_world_url] application from IBM team.

## Running the app on Bluemix

You can deploy your own instance of app to Bluemix. To do this, you can follow the step below to create and deploy your app manually.

1. [Install Go][go_install_url].  If you have Go installed skip to step 2.

2. Create a Bluemix Account

    [Sign up for Bluemix][bluemix_signup_url] or use an existing account.

3. Download and install the [Cloud Foundry CLI][cloud_foundry_url] tool


4. `cd` into the `$GOPATH/src` directory

  ```
  cd $GOPATH/src
  ```

5. Clone the app to your local environment from your terminal using the following command:

  ```
  git clone https://github.com/SevenStringArgs/ColdChainSensors.git
  ```

6. `cd` into this newly created directory

  ```
  cd ColdChainSensors/backend
  ```

7. Compile the Go code:

  ```
  $ make
  ```

8. Open the `manifest.yml` file and change the `host` value to something unique

  The host you choose will determinate the subdomain of your application's URL:  `<host>.mybluemix.net`

9. Connect to Bluemix in the command line tool and follow the prompts to log in:

  ```
  $ cf api https://api.ng.bluemix.net
  $ cf login
  ```

10. Deploy your app to Bluemix:

  ```
  $ cf push
  ```


For running application on Bluemix you need to create next list of environment variables or change the code in file `env.go` not to use them:

* CLOUDANT_APIKEY
* CLOUDANT_BASE
* CLOUDANT_PASS
* CLOUDANT_URL
* MYSQL_BASE
* MYSQL_HOST
* MYSQL_LOGIN
* MYSQL_PASS
* MYSQL_PORT

## Run the app locally

1. [Install Go][go_install_url].  If you have Go installed skip to step 2.

2. `cd` into the `$GOPATH/src` directory

  ```
  cd $GOPATH/src
  ```

3. Clone the app to your local environment from your terminal using the following command:

  ```
  git clone https://github.com/SevenStringArgs/ColdChainSensors.git
  ```

4. `cd` into this newly created directory

  ```
  cd ColdChainSensors/backend
  ```


5. Compile the Go code:

  ```
  $ make
  ```

Your app will be automatically be available on port 8080. To access the app, go to localhost:8080 in your browser. Happy developing!

[bluemix_signup_url]: https://ibm.biz/go-hello-world-signup
[cloud_foundry_url]: https://github.com/cloudfoundry/cli/releases
[gin_gonic_url]: https://github.com/gin-gonic/gin
[go_install_url]: https://golang.org/doc/install
[go_hello_world_url]: https://github.com/IBM-Bluemix/go-hello-world
