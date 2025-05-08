# Bisaya++ Interpreter

A simple interpreter for the **Bisaya++** programming language, built using **Java** and **JavaFX**. This project provides a graphical interface that mimics a basic text editor, allowing users to write, edit, open, and run Bisaya++ code. The goal is to make learning programming more accessible to native Cebuano speakers by using familiar language constructs and syntax.

---

## 📘 About Bisaya++

**Bisaya++** is a strongly–typed high–level interpreted Cebuano-based programming language developed to teach Cebuanos the basics of programming. Its simple syntax and native keywords make programming easy to learn.

### ✨ Sample Code
```
-- this is a sample program in Bisaya++
SUGOD
  MUGNA NUMERO x, y, z=5
  MUGNA LETRA a_1=’n’
  MUGNA TINUOD t=”OO”
  x=y=4
  a_1=’c’
  -- this is a comment
  IPAKITA: x & t & z & $ & a_1 & [#] & “last”
KATAPUSAN
```
Output of the sample program:
```
4OO5
c#last
```
---

## 🧠 Language Grammar

### ✅ Program Structure

- All programs start with `SUGOD` and end with `KATAPUSAN`
- Variables are declared using `MUGNA`
- Variable names are case sensitive, starts with a letter or an underscore `_` and followed by a letter, underscore or digits.
- Each line contains only one statement
- Comments begin with `--`
- Reserved keywords are written in all caps
- `$` inserts a new line; `&` concatenates strings
- `[]` is used for escape codes

### 🧾 Data Types

- `NUMERO`: whole number
- `TIPIK`: decimal number
- `LETRA`: single character
- `TINUOD`: boolean (`"OO"` for true, `"DILI"` for false)

### 🔣 Operators

- Arithmetic: `+`, `-`, `*`, `/`, `%`
- Comparison: `>`, `<`, `>=`, `<=`, `==`, `<>`
- Logical: `UG` (and), `O` (or), `DILI` (not)
- Unary: `+`, `-`

### 🖨️ Output

Use `IPAKITA:` followed by expressions to print to output.

### 🎯 Input

Use `DAWAT:` followed by one or more variable names to prompt for input.

---

## Control Structures
### 🔹 Conditional Statements
```
KUNG (condition)             // if
PUNDOK{
  // statements to execute
}
KUNG DILI (condition)        // else if
PUNDOK{
  // statements to execute
}
KUNG WALA                    // else
PUNDOK{
  // statements to execute
}
```

### 🔹Loops
#### While Loop
```
MINTRAS (condition)
PUNDOK {
  // statements to execute while condition is true
}
```

### Do-While Loop
```
BUHATA
PUNDOK {
  // statements to execute at least once while condition is true
}
MINTRAS (condition)
```

### For Loop
```
ALANG SA (initialization, condition, update)
PUNDOK {
  // statements to execute for each iteration
}
```

---

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- JavaFX SDK (if using Java 11+)
- IDE (e.g., IntelliJ, Eclipse, VS Code with "Extension Pack for Java" extension)

### Installation

1. **Clone the repository**  
   ```bash
   git clone https://github.com/chrlskrt/bisayaplusplus.git
   cd bisayaplusplus
   ```

2. **Set up JavaFX in your IDE**  
   Make sure JavaFX is configured in your project settings or module paths.

3. **Run the project**
   Go to src/main/java/com/example/bisayaplusplus/
   Open `BisayaPlusPlusInterpreter.java` and run it. The GUI should launch, allowing you to edit and run code.
