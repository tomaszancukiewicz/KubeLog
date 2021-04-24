# KubeLog

## Log query language

- Logical keywords: `AND`, `OR`, `NOT`
- Functions, applied to log line, before check query inside: `upperCase()`, `lowerCase()`
- Function to make case-insensitive queries `ignoreCase()`
- String literals marked with `"` or `'`, e.g. `"Hello \"World\""` or `'Hello "World"'`
- Regex patterns, e.g. `r"[0-9]+"` or `r'[0-9]+'`

For more, check [grammar file](src/main/antlr/SearchQuery.g4)

### Example

Search
`r"[0-9]+" AND upperCase("HELLO" OR '"WORLD"')`
matches all lines, that: 
- have any digit 
- after applying upperCase to log line, 
  it has to have `Hello` or `"World"` word
  
| Log line           | Is matched? |
| -----------------  | ----------- |
| 1 hello            | yes         |
| hello              | no          |
| "World" 2          | yes         |
| World 2            | no          |
| 123 alfa           | no          |
| "HELLO" "WORLD" 3  | yes         |