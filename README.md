# Project Explanation

**Author:** Krzysztof Wieconkowski  
**Program Name:** DependencyExtractor  
**Source Code Link (Java):** [https://github.com/conek/DependencyExtractor](https://github.com/conek/DependencyExtractor)  
**Compressed Binary Application Link (Java):** [https://github.com/conek/DependencyExtractor/dist/blob/main/dependencyExtractor_binary.zip](https://github.com/conek/DependencyExtractor/dist/blob/main/dependencyExtractor_binary.zip)  

## Project Objective

The task was to visualize dependencies in the source code. I selected projects using Maven as the data source. The application can extract dependencies from both single and interconnected projects (modular) of any complexity. I identified two potential solutions to this problem and will describe both to outline their advantages and disadvantages. For the implementation, I chose the **second** solution, which guarantees correct results while being easy to understand, maintain, and develop.

### Rejected Solution

The first "naive" solution would be to extract dependencies directly from the "pom.xml" files (belonging to Maven projects). This approach is not advisable for several reasons:

1. **Limited Value:** We only operate on explicit dependencies declared by the developer. This does not account for which versions of dependencies will ultimately be used.
2. **Complexity and Knowledge Requirement:** Achieving reliability requires detailed knowledge of the Maven project structure, including how subprojects/modules interconnect, exclusions, and dependency overrides.
3. **Incomplete Data Risk:** Since we rely solely on the project's source code, not all dependencies may have an accessible "pom.xml" (in the source code or downloaded in .m2). Consequently, the dependency tree for some artifacts may be incomplete due to missing data.
4. **Duplication of Maven's Logic:** To gather dependencies, we would need to replicate Maven's actions/algorithms. This solution is complex, difficult to maintain, and fragile, as these behaviors may change over time.

However, the advantage is that the application code would be independent of external tools, requiring no additional actions.

### Implemented Solution

My goal was to create an application that generates reliable results and is easy to understand, maintain, and develop. Instead of mimicking or duplicating specific Maven algorithms, this solution works alongside Maven. Maven is responsible for building the project and thus becomes the ultimate source of truth regarding which dependencies are used. The approach involves directly obtaining answers from Maven using the command: **`dependency:tree`** with a specified **`outputType`** (e.g., **`json`**).

#### Advantages

1. **Reversed Responsibility:** Maven is tasked with providing the dependency tree, making it the primary source of truth. Our role is "limited" to writing an adapter that directly communicates with Maven, retrieves, and parses the data for our needs. This reduces the chance of errors related to dependency management.
2. **Scalable and Flexible:** The application handles projects of any complexity and modularity, consistently providing accurate dependency trees. This ensures that even for large modular projects, all dependencies are correctly identified and reported.
3. **Resistance to change:** Although theoretically the output of the Maven command can change depending on the version, for most cases it does not affect the stability and correctness of the application. Maven promises that it will return the result in JSON form, where the application extracts this message without going into detail about its structure. 
4. **Simplified Maintenance:** By relying on Maven to do the heavy lifting of dependency resolution and only focusing on data parsing and presentation, maintaining the application becomes simpler and more focused on its primary functionality.

#### Disadvantages

1. **Dependency on Maven:** The solution requires Maven to be available on the system where the application is running. The application implements several strategies; it can use Maven accessible from environment variables, from a user-designated path, or in the 'binary/apache-maven' or 'apache-maven' directories relative to the application.
2. **Potential Access Issues:** When Maven retrieves dependencies, it might need to access private repositories that require authentication. If the user does not have the necessary permissions or the repositories are configured with strict security settings, this could prevent the application from retrieving some dependencies.

## Application Flow (CLI in Java)

The application is launched from the command line, featuring a help section that describes parameters and their validation. To standardize user experience, the Apache Commons CLI library is used.

### Application flow

1. **Invoke Maven** to analyze the dependencies of the given project. Maven can be located through:
   - The user-specified path.
   - Environment variables.
   - Directories `binary/apache-maven` or `apache-maven` at the application's execution location.
2. **Parse the obtained information** to transform Maven's output into a format suitable for the application's internal use.
3. **Format and Present Data:** Convert the internal format to a user-friendly format, such as JSON, and display the dependency tree either directly in the console or save it to a file.

## Diagram

![Screenshot of a class diagram](https://github.com/conek/DependencyExtractor/diagrams/blob/main/classDiagram.png)

## Available Options in the Program

```
usage: dependencyExtractor
 -f,--file <arg>        Path to where generate output report (required if
                        output is 'file')
 -m,--mavenHome <arg>   Path to Maven home (optional)
 -o,--output <arg>      Output destination: 'console' or 'file' (required)
 -p,--pom <arg>         Project pom path (required)
```

## Example Usage (windows syntax)

```
java -jar dependencyExtractor.jar --pom="exampleProject/pom.xml" --output="file" --file="report.json"
```
