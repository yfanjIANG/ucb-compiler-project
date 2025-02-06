# CS 164: Programming Assignment 1

Note: Users running Windows should replace the colon (`:`) with a semicolon (`;`) in the classpath argument for all command listed below.

## Getting started

Run the following command to generate and compile your parser, and then run all the provided tests:

    mvn clean package

    java -cp "chocopy-ref.jar;target/assignment.jar" chocopy.ChocoPy --pass=s --test --dir src/test/data/pa1/sample/

In the starter code, only one test should pass. Your objective is to build a parser that passes all the provided tests and meets the assignment specifications.

To manually observe the output of your parser when run on a given input ChocoPy program, run the following command (replace the last argument to change the input file):

    java -cp "chocopy-ref.jar;target/assignment.jar" chocopy.ChocoPy --pass=s src/test/data/pa1/sample/expr_plus.py

You can check the output produced by the staff-provided reference implementation on the same input file, as follows:

    java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy --pass=r src/test/data/pa1/sample/expr_plus.py

Try this with another input file as well, such as `src/test/data/pa1/sample/coverage.py`, to see what happens when the results disagree.

## Assignment specifications

See the [PA1 specification][PA1 specification] on the course
website for a detailed specification of the assignment.

Refer to the [ChocoPy Specification][ChocoPy Specification] on the CS164 web site
for the specification of the ChocoPy language.

## Receiving updates to this repository

You can either use the GitHub UI (recommended) or the command line to receive updates from the assignment template repository.

### GitHub (recommended)

Use the "Sync fork" button on the GitHub UI to merge changes from the original repository into yours.

### Command line

Add the `upstream` repository remote (you only need to do this once in your local clone):

    git remote add upstream https://github.com/cs164fa2024/programming-assignments-pa1-chocopy-parser-pa1-chocopy-parser

If you use ssh for authentication, you should instead run (and you also only need to do this once):

    git remote add upstream git@github.com:cs164fa2024/programming-assignments-pa1-chocopy-parser-pa1-chocopy-parser.git

Then, to sync with updates upstream, run:

    git pull upstream main

## Submission writeup

### Team member 1:  Yifan Jiang

### Team member 2:  Pengfei Tian

### Late hours : 0

### Question 1

The core part of hanlding with indent and dedent token is in `ChocoPy.jflex`. There are basically three parts: `Line 53 - Line 102` are some funcionts and data structure. `Line 123 - Line 158` is to deal with the indent and dedent and call function that defined in first part. While `Line 163 - Line 175` is a specific state to deal with multi dedents in a same line.

In the first part, we use a stack to store the indent level. Then use a function `getIndentLevel()` to count how many leading space in the current line, which is the indent level. The function `handleIndentation()` is to judge whether the current indent is equal or less or greater than the last line indent. If equal, then don't emit indent since it's still in the same block. If greater, then emit indent. If less, then emit dedent and pop the stack until the top of the stack is equal to the current indent and record the number of dedent as `numPendingDedents`.

In the second part, we will match basically three situations: (1)the whole line is empty, so we skip it. (2)There are some indents in the line, so we call `handleIndentation()` to handle the leading space. Then jump to the `STMT` state to deal with the following characters. But if there are multiple dedent to emit, then it will jump to `MULITDEDENT` state to handle it. (3)There are no dedent. We pushback one character we just read and jump to `STMT` to deal with the following characters.

In the third part, this is a state to handle with multi dedents to emit at same line, whose location should be exactly the same. In order to do this, we will keep track of the variable `numPengdingDedents`. In this state, we will detect one space, since we just pushback 1 characters in part 2. Then check if `numPendingDedents` is zero, if not then we will emit one dedents and pushback one character(which is space), and then jump to this state to loop. Once we emit all the dedent, we will jump to state `STMT` to handle following characters.

### Question 2

The most challenging part of the programming assignment was implementing the syntax rules for handling types. Initially, we struggled to get the parser to correctly process statements like `x: int = 5`, because it would interpret these as expressions rather than variable definitions.After investigating, we realized that the issue could be resolved by adding `var_def` to the program header, allowing the parser to distinguish between expressions and variable definitions with type annotations. The core part of the solution is located in the **`ChocoPy.cup`** file in line 230 and line 267.

[PA1 Specification]: https://drive.google.com/open?id=1oYcJ5iv7Wt8oZNS1bEfswAklbMxDtwqB
[ChocoPy Specification]: https://drive.google.com/file/d/1mrgrUFHMdcqhBYzXHG24VcIiSrymR6wt
