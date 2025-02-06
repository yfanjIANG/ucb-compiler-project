# CS 164: Programming Assignment 2

Note: Users running Windows should replace the colon (`:`) with a semicolon (`;`) in the classpath argument for all command listed below.

## Getting started

Run the following command to build your semantic analysis, and then run all the provided tests:

    mvn clean package

    java -cp "chocopy-ref.jar;target/assignment.jar" chocopy.ChocoPy
    --pass=.s --dir src/test/data/pa2/sample/ --test

In the starter code, only two tests should pass. Your objective is to implement a semantic analysis that passes all the provided tests and meets the assignment specifications.

You can also run the semantic analysis on one input file at at time. In general, running the semantic analysis on a ChocoPy program is a two-step process. First, run the reference parser to get an AST JSON:

    java -cp "chocopy-ref.jar;target/assignment.jar" chocopy.ChocoPy
    --pass=r <chocopy_input_file> --out <ast_json_file>

Second, run the semantic analysis on the AST JSON to get a typed AST JSON:

    java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy
    -pass=.s  <ast_json_file> --out <typed_ast_json_file>

The `src/tests/data/pa2/sample` directory already contains the AST JSONs for the test programs (with extension `.ast`); therefore, you can skip the first step for the sample test programs.

To observe the output of the reference implementation of the semantic analysis, replace the second step with the following command:

    java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy
    --pass=.r <ast_json_file> --out <typed_ast_json_file>

In either step, you can omit the `--out <output_file>` argument to have the JSON be printed to standard output instead.

You can combine AST generation by the reference parser with your
semantic analysis as well:

    java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy
    --pass=rs <chocopy_input_file> --out <typed_ast_json_file>

## Assignment specifications

See the [PA2 specification][PA2 specification] on the course
website for a detailed specification of the assignment.

Refer to the [ChocoPy Specification][ChocoPy Specification] on the CS164 web site
for the specification of the ChocoPy language.

## Receiving updates to this repository

You can either use the GitHub UI (recommended) or the command line to receive updates from the assignment template repository.

### GitHub (recommended)

Use the "Sync fork" button on the GitHub UI to merge changes from the original repository into yours.

### Command line

Add the `upstream` repository remote (you only need to do this once in your local clone):

    git remote add upstream https://github.com/cs164fa2024/pa2-chocopy-semantic-analysis

If you use ssh for authentication, you should instead run (and you also only need to do this once):

    git remote add upstream git@github.com:cs164fa2024/pa2-chocopy-semantic-analysis.git

Then, to sync with updates upstream, run:

    git pull upstream main

## Submission writeup

Team member 1:Yifan Jiang

Team member 2:Pengfei Tian

We have used 0 late hours.

Q1: Our semantic analysis has performed 2 passes over the AST. For the first pass, we have our implementation in the DeclarationAnalyzer.java file. In this file, we scaned the whole AST and fetched all global declarations like VarDef, FuncDef, ClassDef, but we don't step into the function or the class even though there are some nested function defined in them. Then, after analyzing these declaratioins, we can get the globals symbol table which contains all global variable and the function and class definition in the global scope.

For the second pass, we have our implementation in our TypeChecker.java file.  In this file, we pass the global symbol table and the class symbol table to the typechecker, which are derived from the previous  DeclrationAnalyzer. After that, we started to scan the file for the second time. This time, if we encounter a  function or a class we can step into it, and then recursively call the corresponding analyzeFuncdef or analyzeDeclaration to check whether the inner defined function is type check.

Q2: Our team found that the most challenging part for this assignment is how to write a analyzeFuncDef in the TypeChecker.java file elegantly. Based on our implementation described above, we should step into the nested function in TypeChecker file. However, if we straightly call the analyzeDeclartion when encountering an inner function in the function body, we will have no access to the variables defined below this inner function when we want to use nonlocal because we scaned the AST in order and we haven't get to these variables so far. Here is the example:

```python
def():
    def():
        nonlocal x
    x:int = 1
```

In this example,nonlocal x will not get the access to x because it was defined below the nested function. Considering that, we have modified our implementation. When we encounter a FuncDef declration in the function body, we will first store it into a list and proceed to proccess the other type of declration first. After processing all declration in the function body, we then turn to that list and started to process those Funcdef declrations by calling analyzeFuncDef recursively.

Q3: By inferring the most specific type, we can preserve some meaningful information about the variables and their intended use, which helps the type checker understand how the values can be utilized in the program. As mentioned in the PA2 description, for a list expression of type [T], if its index is not a INT_TYPE, we still need to infer the index expression to have T type because it is determined when the list was created. However, if we simply infer the type object for every ill-typed expression, we will lose a lot of important type information. And this will make us lose some important error information because every type is included in the object type, which will enable an error program to pass some type checks.

[PA2 Specification]: https://drive.google.com/open?id=1HlPMeFUFBjUVtdYCeUxf38sT6oSUKRE7
[ChocoPy Specification]: https://drive.google.com/file/d/1mrgrUFHMdcqhBYzXHG24VcIiSrymR6wt
