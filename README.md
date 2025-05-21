# KubeLog

## Installation

Pkg installs application in `/Applications` by default. You can change it to `$HOME/Applications` during
installation.

When you see "unidentified developer" error,
open installer in `System Settings` > `Privacy & Security` > `Security` > `Open anyway`

## Shortcuts

* `CMD + T` - close/open pod list
* `CMD + F` - open/close log search
* `CMD + K` - clear logs in active tab
* `CMD + W` - close active tab
* `CMD + SHIFT + W` - close all tabs

## Log query language

- Logical keywords: `AND`/`and`, `OR`/`or`, `NOT`/`not`
  - if there is no operator added between literals, then `AND` is assumed
- String literals
  - marked with `"` or `'`, e.g. `"Hello \"World\""` or `'Hello "World"'` - it's an exact match
  - without `"` or `'` - it's an ignore case match
- Regex patterns, e.g. `r"[0-9]+"` or `r'[0-9]+'`

For more, check [grammar file](src/main/antlr/com/kube/log/search/query/SearchQuery.g4)

### Example

Search
`r"[0-9]+" Hello World (ab OR '"c d"')`
matches all lines, that: 
- have any number
- have the words `Hello`, `World` (ignore case)
- have word `ab`(ignore case) or `"C D"`(exact)
  
| Log line               | Is it matched?                       |
|------------------------|--------------------------------------|
| 1 hello world ab       | yes                                  |
| 40 HELLO WORLD AB      | yes                                  |
| 1043 HELLO WORLD "c d" | yes                                  |
| hello world            | no - missing number, `ab` or `"C D"` |
| 1234 ab                | no - missing `hello`, `world`        |

