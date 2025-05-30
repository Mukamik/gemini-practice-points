Use https://angular.dev/style-guide as the basis for this style guide.

Add the following modifications from my team with higher precedence:

## Team Style Guide Modifications

The following rules represent modifications to the standard style guide, adopted to enhance our team's development workflow.

### 1. Variable Declarations

**Rule:** Use `const` and `let` for all variable declarations; avoid `var`.

```typescript
// GOOD
const heroes = [];
let activeHero: Hero;

// AVOID
var villains = [];
```

### 2. Arrow Function Syntax

**Rule:** For simple, single-line arrow functions, omit the curly braces and implicit return.

```typescript
// GOOD
const add = (a: number, b: number) => a + b;
const greet = (name: string) => `Hello, ${name}!`;

// AVOID
const multiply = (a: number, b: number) => { return a * b; };
```

### 3. Function Definition Preference

**Rule:** Use named functions over anonymous functions for clarity and easier debugging, especially for callbacks.

```typescript
// GOOD
function onButtonClick() {
// ...
}
button.addEventListener('click', onButtonClick);

// AVOID
button.addEventListener('click', function() {
    {
    // ...
    }
);
```

### 4. Type vs. Interface Usage

**Rule:** Prefer interface for defining object shapes and type for creating aliases for primitive types, unions, or tuples.



```typescript
// GOOD
interface User {
  id: number;
  name: string;
}

type ID = string | number;

// AVOID
type Product = {
  id: number;
  name: string;
};
```

### 5. String Literal Quoting

**Rule:** Always use single quotes for strings unless interpolating a variable.


```typescript
// GOOD
const name = 'Alice';
const message = `Hello, ${name}!`;

// AVOID
const city = "New York";
```
