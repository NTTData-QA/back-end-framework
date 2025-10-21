# Automation API Framework

This repository contains an API that simulates one from a bank, designed to help newcomers develop QA skills in API testing. This repository not only contains the API development but also a variety of tests to check different functionalities implemented, using Gherkin syntax. This makes it possible for anyone to understand the purpose and steps of the tests. The API has been developed using Java as the programming language and the Spring Boot library.

## How does the API work?

This API is configured to execute on your PC and respond to requests made to **localhost**. The first time it is executed, it will create a database using **H2**, where the entities _Customer_, _Accounts_, _Cards_, and _Transfer_ will be stored and ready to receive different requests, including those related to the **CRUD** operations. Once it is running, you can make those requests by calling the endpoints listed in this document. 
Additionally, the API presents a security filter that forbids clients from making any request without being authenticated. Only requests with endpoints containing the route **“/public”** can be called without passing the security filter. This security filter is based on JWT. When a user logs in, the API returns a header called **‘Cookie Token’**, which contains a new token created with the user’s email address and an expiration date set for the following day. 
This cookie will be sent with every request, and the system will check if the token is valid. If it is not, the system will return a 401 Status Code and an error message. If the user logs out, the cookies will be cleared, so the user won’t be able to make any requests unless they log in again.

## How to get started?

First, ensure you have your IDE installed and Git set up. Then, use the following command to clone the repository:

```shell
git clone https://github.com/NTTData-QA/back-end-framework.git
```
to clone the repository. Instead of this command, you can use other tools such as ‘Create a New Project > From Version Control…” from IntelIJ.

Once you have cloned it, you can use the Maven command:
```shell
 mvn -U clean install
```
in order to Reload All maven Projects incrementally.  If you're using IntelliJ, you can find the Maven symbol on the right side, where you can click to access various Maven options. Select the button with two arrows forming a circle to reload the project. Now your project is prepared to run.

However, we should understand how this framework is structured. Here is a figure illustrating how it is structured and the main purpose of each package or file.


```plaintext
automation-api-framework/
├── Data/                # Here we can find files related to the database created.
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org.example/
│   │   │       └── api/
│   │   │           ├── data/             # This package contains the controllers, entities, repositories, and DTOs that the API is based on.  
│   │   │           ├── security/         # In this package, we find all the files related to Spring Boot Security.
│   │   │           ├── service/          # This package contains the services related to each entity.
│   │   │           ├── token/            # This package only contains a file with different Java methods related to the JWT.			
│   │   │           └── Application.java  # This class is the one that runs the API, to be able to make calls to the API you must have this class running.
│   │   │       └── apicalls/
│   │   │           ├── apiconfig/        # This package contains a Java interface with all the endpoints.  
│   │   │           ├── client/           # This package is responsible for initializing a new client and saving the cookies from the responses.
│   │   │           ├── service/          # This package contains methods to make tests clearer.
│   │   │           ├── utils/            # This package contains generic methods useful for the framework, such as constants, random generators, or JSON converter.
│   │   │       └── Main.java             # Class to test if Java is working correctly
│   │   ├── resources/
│   │   │   ├── application.properties    # This file configures Spring and H2.
│   │   │   ├── ComandosUtiles
│   │   │   ├── copia.sql                # The database was created manually.
│   │   │   └── copia.zip
├── test/                  # This package contains files related to tests.
│   ├── java/
│   │   └── org/
│   │       └── example/
│   │           ├── context/
│   │           ├── steps/             # Here we find all the steps used in the tests.
│   │           └── CucumberRunner.java # This file runs all the CucumberTest.
│   │       └── testRestEasyClient/     # Class to test RestEasyClient library.
│   ├── resources/
│   │   └── features/                  # Gherkin Features
├── target/
├── pom.xml

```
Therefore, in order to make requests, the class Application.java must be running. By default, the port that handles requests is set to 8080. If you want to change the port, you can modify it in the application properties file. For this reason, all requests will be sent to the path:
```
http://localhost:8080/ + {route of our endpoint}.
```
## Endpoints List

To view all endpoints available and the information related to them, you can download the following worksheet:

[Download Endpoints worksheet by clicking here.](.readmeFiles/Endpoints.xlsx)


## How do I access to the database with H2?

Since the database is managed by H2, make sure Spring Boot is running. Application.java must be running while you are using the database or it will return a server error. When Application.java starts, open your browser and navigate to http://localhost:8080/h2-ui to access the database. You will see the following menu:

![H2 Console.](.readmeFiles/H2Console.PNG)

Fill in the entries with the information from the application.properties file. There you can change User Name and Password to use whatever you want.

## Testing
To ensure the quality of the code, we must test the functionalities implemented in this framework, hence developing automated tests plays a key role in any framework. For this reason, we have created different automated tests, but in order to make our tests understandable for anyone outside of this project, we have written our tests with the Gherkin syntax. Therefore, to run those tests we used Cucumber with Junit 5, so as to automatizing them. To configure our tests with Spring Boot, we created a java class called CucumberSpringConfiguration.java, contained in the homonymous package.

Once the test’s feature is created, we create the different steps and organize them according to their relation with the different controllers of our API. For example, a step that creates Cards will be placed in the ‘CardSteps’ file, while those unrelated to any controller will be filed in ‘GenericSteps’.  Separating the steps according to this criterion has a problem: what happens if in a step you create or update a variable that will be used in the following steps? If the steps were in the same file, it would not pose any problem, but since our steps might be in different files, the variable will be created, or updated, locally in that file, and therefore when the following steps call on the variable, it will lead to an error, because they will not recognize any variable with that name or might retrieve outdated variable.
That’s why we have created a package called ‘context’, which contains ‘TestContext’ and ‘AbstractSteps’.

![Structure Repository showing where the context package is](.readmeFiles/testContextStructure.PNG)

The idea is that every change in a variable will be saved in a ‘TestContext’ object, so if a step updates the value of a variable, it will also update its value in the testContext object, and the following steps will obtain the value of that variable by calling the testContext object. Once the test is finished, the object will be reset, and all the values will be set to null, avoiding any kind of problem in the following tests. Consequently, every step must use the same testContext object, or we will have the same problem, so we have created ‘AbstractSteps’, which only contains the testContext object, and every other ‘*Steps’ file will extend from this class. Thus, there will not be any need to initialize a new ‘TestContext’ object.

On a different topic, after each test, a method is executed to erase every entry created in the database from the test, in case you want those entries to persist over time, you can modify the method with the Cucumber annotation “@After” or create a tag to decide which tests are going to erase their entries and which are not.

## Contributors

Manuel Jesús Rodríguez Páez

Luis Rafael Argandoña Blácido

Natalia Castellano Ramos

Marco Tenorio Cortés

Rafael Alcántara Laguna

Pedro Verde Ruiz

Alexandru Roberto Rizea

Vlad Alexandru Simion

Álvaro Máximo Muñoz Pavón

Andrés Francisco Fernández Rodríguez-Acosta

Daniel Ceballos Roales

Jesús Ruslan Esquinas Herrera

Jose Julián Manzano Montenegro

Jose Miguel Iborra Conejo

Manuel Ortiz Blanco

Mohanad JD Abulatifa

Paula Calvente Lobato

## Acknowledgments

Special thanks to Álvaro Laguna García and Raúl Galera Sancho for their supervision and help along the development of this project.
